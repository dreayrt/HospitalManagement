package com.example.hospitalManagement.service;

import com.example.hospitalManagement.dto.AppointmentDTO;
import com.example.hospitalManagement.dto.AppointmentFilterRequestDTO;
import com.example.hospitalManagement.dto.CreateAppointmentRequestDTO;
import com.example.hospitalManagement.entity.Appointments;
import com.example.hospitalManagement.entity.Doctor;
import com.example.hospitalManagement.entity.Enum.AppointmentStatus;
import com.example.hospitalManagement.entity.Patient;
import com.example.hospitalManagement.entity.User;
import com.example.hospitalManagement.kafka.producer.NotificationProducer;
import com.example.hospitalManagement.mapper.AppointmentMapper;
import com.example.hospitalManagement.repository.AppointmentRepository;
import com.example.hospitalManagement.repository.DoctorRepository;
import com.example.hospitalManagement.repository.userRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service xử lý nghiệp vụ quản lý lịch khám bệnh viện
 * Áp dụng Redis Cache-Aside:
 *   - GET: Đọc Redis trước → cache miss → query DB → lưu Redis
 *   - POST/PUT/DELETE: Ghi DB xong → xóa cache liên quan (invalidate)
 * KHÔNG sửa RedisService, chỉ gọi save/get/remove/exists.
 */
@Service
public class AppointmentService {

    // ===================== CACHE KEY CONSTANTS =====================
    /** Cache lịch khám theo ID: appointment:{id} */
    private static final String KEY_APPT        = "appointment:";
    /** Cache lịch của bác sĩ: appt:doctor:{doctorId}:{fromDate}:{toDate} */
    private static final String KEY_DOC_SCHED   = "appt:doctor:";
    /** Cache danh sách phân trang: appt:list:{hashParams} */
    private static final String KEY_APPT_LIST   = "appt:list:";
    /** TTL mặc định: 5 phút (giây) */
    private static final long   CACHE_TTL       = 300L;
    /** TTL danh sách: 2 phút (thay đổi thường xuyên hơn) */
    private static final long   CACHE_LIST_TTL  = 120L;

    // ===================== DEPENDENCIES =====================

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private userRepository userRepo;

    @Autowired
    private AppointmentMapper appointmentMapper;

    @Autowired
    private NotificationProducer notificationProducer;

    // Dùng RedisService có sẵn, KHÔNG sửa file RedisService.java
    @Autowired
    private RedisService redisService;

    // ObjectMapper dùng để serialize/deserialize DTO → JSON String để lưu Redis
    private final ObjectMapper objectMapper;

    public AppointmentService() {
        this.objectMapper = new ObjectMapper();
        // Đăng ký module hỗ trợ LocalDate, LocalTime, LocalDateTime
        this.objectMapper.registerModule(new JavaTimeModule());
        // Tắt ghi dates dạng timestamp array, dùng ISO string
        this.objectMapper.disable(
                com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS
        );
    }

    // ===================== HELPER: Serialize/Deserialize cho Redis =====================

    /** Chuyển object → JSON String để lưu vào Redis */
    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return null; // Nếu lỗi serialize thì bỏ qua cache
        }
    }

    /** Chuyển JSON String từ Redis → AppointmentDTO */
    private AppointmentDTO fromJsonDTO(Object raw) {
        try {
            if (raw == null) return null;
            return objectMapper.readValue(raw.toString(), AppointmentDTO.class);
        } catch (Exception e) {
            return null;
        }
    }

    /** Chuyển JSON String từ Redis → List<AppointmentDTO> */
    private List<AppointmentDTO> fromJsonList(Object raw) {
        try {
            if (raw == null) return null;
            return objectMapper.readValue(
                    raw.toString(),
                    new TypeReference<List<AppointmentDTO>>() {}
            );
        } catch (Exception e) {
            return null;
        }
    }

    // ===================== HELPER: Xóa các cache liên quan =====================

    /**
     * Xóa cache của 1 appointment cụ thể và cache danh sách
     * Gọi sau mỗi thao tác thay đổi dữ liệu (create/cancel/reschedule)
     */
    private void evictAppointmentCache(Long appointmentId, Long doctorId) {
        // Xóa cache chi tiết
        redisService.remove(KEY_APPT + appointmentId);

        // Xóa cache lịch bác sĩ (dùng wildcard theo pattern doctorId)
        // Vì RedisService không hỗ trợ pattern delete → xóa 30 ngày tới
        if (doctorId != null) {
            LocalDate today = LocalDate.now();
            for (int i = 0; i < 30; i++) {
                LocalDate d = today.plusDays(i);
                // Xóa cache lịch bác sĩ cho ngày đó (các khoảng có thể chứa ngày này)
                redisService.remove(KEY_DOC_SCHED + doctorId + ":" + d + ":" + d);
            }
            // Xóa cache tuần phổ biến
            redisService.remove(KEY_DOC_SCHED + doctorId + ":" + today + ":" + today.plusDays(6));
            redisService.remove(KEY_DOC_SCHED + doctorId + ":" + today + ":" + today.plusDays(29));
        }

        // Xóa cache danh sách trang 0 (trang hay được xem nhất)
        redisService.remove(KEY_APPT_LIST + "page0");
        redisService.remove(KEY_APPT_LIST + "page0:doctor:" + doctorId);
    }

    // ===================== ĐẶT LỊCH KHÁM =====================

    /**
     * Đặt lịch khám mới cho bệnh nhân
     * Validate: bác sĩ tồn tại, không trùng lịch
     * → Sau khi lưu DB: xóa cache liên quan
     */
    @Transactional
    public AppointmentDTO createAppointment(CreateAppointmentRequestDTO request) {
        // Kiểm tra bác sĩ tồn tại
        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new RuntimeException(
                        "Không tìm thấy bác sĩ với ID: " + request.getDoctorId()));

        // Kiểm tra trùng lịch: bác sĩ đã có lịch giờ đó chưa?
        boolean isConflict = appointmentRepository
                .existsByDoctorIdAndAppointmentDateAndAppointmentTimeAndStatusNot(
                        request.getDoctorId(),
                        request.getAppointmentDate(),
                        request.getAppointmentTime(),
                        AppointmentStatus.CANCELLED
                );
        if (isConflict) {
            throw new RuntimeException("Bác sĩ đã có lịch khám vào ngày "
                    + request.getAppointmentDate() + " lúc " + request.getAppointmentTime()
                    + ". Vui lòng chọn khung giờ khác.");
        }

        // Tạo entity
        Appointments appointment = new Appointments();
        appointment.setAppointmentDate(request.getAppointmentDate());
        appointment.setAppointmentTime(request.getAppointmentTime());
        appointment.setReason(request.getReason());
        appointment.setStatus(AppointmentStatus.PENDING);
        appointment.setCreatedAt(LocalDateTime.now());
        appointment.setDoctor(doctor);

        Patient patient = new Patient();
        patient.setId(request.getPatientId());
        appointment.setPatient(patient);

        if (request.getCreatedById() != null) {
            User createdBy = new User();
            createdBy.setId(request.getCreatedById());
            appointment.setCreatedBy(createdBy);
        }

        Appointments saved = appointmentRepository.save(appointment);

        // Gửi thông báo qua Kafka
        try {
            String doctorName = (doctor.getUser() != null) ? doctor.getUser().getFullName() : "Bác sĩ";
            String msg = String.format(
                    "{\"type\":\"APPOINTMENT_BOOKED\",\"appointmentId\":%d,\"doctorName\":\"%s\",\"date\":\"%s\",\"time\":\"%s\"}",
                    saved.getId(), doctorName,
                    request.getAppointmentDate(),
                    request.getAppointmentTime()
            );
            notificationProducer.sendAppointment(msg);
        } catch (Exception ignored) {}

        AppointmentDTO result = appointmentMapper.toDTO(saved);

        // Cache chi tiết lịch vừa tạo luôn
        String json = toJson(result);
        if (json != null) {
            redisService.save(KEY_APPT + saved.getId(), json, CACHE_TTL);
        }

        // Invalidate cache danh sách và lịch bác sĩ
        evictAppointmentCache(saved.getId(), request.getDoctorId());

        return result;
    }

    // ===================== XÁC NHẬN LỊCH KHÁM =====================

    /**
     * Admin xác nhận lịch khám: PENDING → CONFIRMED
     */
    @Transactional
    public AppointmentDTO confirmAppointment(Long appointmentId) {
        Appointments appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lịch khám ID: " + appointmentId));

        if (appointment.getStatus() != AppointmentStatus.PENDING) {
            throw new RuntimeException("Chỉ có thể xác nhận lịch khám ở trạng thái PENDING. Trạng thái hiện tại: "
                    + appointment.getStatus());
        }

        appointment.setStatus(AppointmentStatus.CONFIRMED);
        Appointments updated = appointmentRepository.save(appointment);
        AppointmentDTO result = appointmentMapper.toDTO(updated);

        // Cập nhật cache
        Long doctorId = updated.getDoctor() != null ? updated.getDoctor().getId() : null;
        evictAppointmentCache(appointmentId, doctorId);
        String json = toJson(result);
        if (json != null) redisService.save(KEY_APPT + appointmentId, json, CACHE_TTL);

        return result;
    }

    // ===================== HỦY LỊCH KHÁM =====================

    /**
     * Hủy lịch khám - validate trạng thái hợp lệ trước khi hủy
     * → Sau khi cập nhật DB: xóa cache liên quan
     */
    @Transactional
    public AppointmentDTO cancelAppointment(Long appointmentId, String cancelReason) {
        // Ưu tiên lấy từ Redis trước để có thông tin doctorId
        Long doctorId = null;

        Appointments appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lịch khám ID: " + appointmentId));

        if (appointment.getDoctor() != null) {
            doctorId = appointment.getDoctor().getId();
        }

        // Validate trạng thái
        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new RuntimeException("Không thể hủy lịch khám đã HOÀN THÀNH.");
        }
        if (appointment.getStatus() == AppointmentStatus.IN_PROGRESS) {
            throw new RuntimeException("Không thể hủy lịch khám đang TRONG TIẾN TRÌNH khám.");
        }
        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new RuntimeException("Lịch khám này đã bị hủy trước đó.");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        if (cancelReason != null && !cancelReason.isBlank()) {
            appointment.setReason(appointment.getReason() != null
                    ? appointment.getReason() + " [HỦY: " + cancelReason + "]"
                    : "[HỦY: " + cancelReason + "]");
        }

        Appointments updated = appointmentRepository.save(appointment);

        // Gửi thông báo Kafka
        try {
            String msg = String.format(
                    "{\"type\":\"APPOINTMENT_CANCELLED\",\"appointmentId\":%d,\"cancelReason\":\"%s\"}",
                    updated.getId(), cancelReason != null ? cancelReason : ""
            );
            notificationProducer.sendCancellation(msg);
        } catch (Exception ignored) {}

        AppointmentDTO result = appointmentMapper.toDTO(updated);

        // Xóa cache cũ, lưu trạng thái mới vào Redis
        evictAppointmentCache(appointmentId, doctorId);
        String json = toJson(result);
        if (json != null) {
            redisService.save(KEY_APPT + appointmentId, json, CACHE_TTL);
        }

        return result;
    }

    // ===================== DỜI LỊCH KHÁM =====================

    /**
     * Dời lịch khám sang ngày/giờ mới
     * → Sau khi cập nhật DB: xóa cache liên quan
     */
    @Transactional
    public AppointmentDTO rescheduleAppointment(Long appointmentId,
                                                LocalDate newDate,
                                                java.time.LocalTime newTime) {
        Appointments appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lịch khám ID: " + appointmentId));

        if (appointment.getStatus() != AppointmentStatus.PENDING
                && appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new RuntimeException("Chỉ có thể dời lịch khám ở trạng thái PENDING hoặc CONFIRMED.");
        }

        boolean isConflict = appointmentRepository
                .existsByDoctorIdAndAppointmentDateAndAppointmentTimeAndStatusNot(
                        appointment.getDoctor().getId(),
                        newDate, newTime,
                        AppointmentStatus.CANCELLED
                );
        if (isConflict) {
            throw new RuntimeException("Bác sĩ đã có lịch khám vào ngày " + newDate + " lúc " + newTime);
        }

        Long doctorId = appointment.getDoctor() != null ? appointment.getDoctor().getId() : null;

        appointment.setAppointmentDate(newDate);
        appointment.setAppointmentTime(newTime);

        Appointments updated = appointmentRepository.save(appointment);
        AppointmentDTO result = appointmentMapper.toDTO(updated);

        // Xóa cache cũ, lưu trạng thái mới vào Redis
        evictAppointmentCache(appointmentId, doctorId);
        String json = toJson(result);
        if (json != null) {
            redisService.save(KEY_APPT + appointmentId, json, CACHE_TTL);
        }

        return result;
    }

    // ===================== XEM DANH SÁCH LỊCH KHÁM =====================

    /**
     * Lấy danh sách lịch khám có filter + phân trang
     * Cache-Aside: chỉ cache trang đầu (page=0) không có filter để giảm độ phức tạp key.
     * Các trang có filter vẫn query DB trực tiếp vì dữ liệu thay đổi nhiều.
     */
    public Page<AppointmentDTO> getAppointments(AppointmentFilterRequestDTO filter) {
        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize());

        // Tạo cache key từ các tham số filter
        String cacheKey = buildListCacheKey(filter);

        // Thử đọc từ Redis
        if (redisService.exists(cacheKey)) {
            Object raw = redisService.get(cacheKey);
            List<AppointmentDTO> cached = fromJsonList(raw);
            if (cached != null) {
                // Trả về Page từ cache (không có totalElements chính xác → dùng size cached)
                return new PageImpl<>(cached, pageable, cached.size());
            }
        }

        // Cache miss → query DB
        Page<Appointments> page = appointmentRepository.findWithFilters(
                filter.getDoctorId(),
                filter.getPatientId(),
                filter.getStatus(),
                filter.getFromDate(),
                filter.getToDate(),
                filter.getSearch(),
                pageable
        );
        Page<AppointmentDTO> result = page.map(appointmentMapper::toDTO);

        // Lưu vào Redis nếu là trang đầu, không quá 50 bản ghi
        if (filter.getPage() == 0 && result.getContent().size() <= 50) {
            String json = toJson(result.getContent());
            if (json != null) {
                redisService.save(cacheKey, json, CACHE_LIST_TTL);
            }
        }

        return result;
    }

    /** Tạo cache key duy nhất từ bộ filter */
    private String buildListCacheKey(AppointmentFilterRequestDTO filter) {
        return KEY_APPT_LIST
                + "p" + filter.getPage()
                + "_s" + filter.getSize()
                + (filter.getDoctorId()  != null ? "_doc"    + filter.getDoctorId()  : "")
                + (filter.getPatientId() != null ? "_pat"    + filter.getPatientId() : "")
                + (filter.getStatus()    != null ? "_st"     + filter.getStatus()    : "")
                + (filter.getFromDate()  != null ? "_from"   + filter.getFromDate()  : "")
                + (filter.getToDate()    != null ? "_to"     + filter.getToDate()    : "")
                + (filter.getSearch()    != null && !filter.getSearch().isBlank()
                        ? "_q" + filter.getSearch().hashCode() : "");
    }

    /**
     * Lấy chi tiết 1 lịch khám
     * Cache-Aside: Đọc Redis trước → miss → query DB → lưu Redis
     */
    public AppointmentDTO getAppointmentById(Long id) {
        String cacheKey = KEY_APPT + id;

        // Thử đọc từ Redis
        if (redisService.exists(cacheKey)) {
            Object raw = redisService.get(cacheKey);
            AppointmentDTO cached = fromJsonDTO(raw);
            if (cached != null) {
                return cached; // Trả từ Redis, không cần query DB
            }
        }

        // Cache miss → query DB
        AppointmentDTO result = appointmentRepository.findById(id)
                .map(appointmentMapper::toDTO)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lịch khám ID: " + id));

        // Lưu vào Redis
        String json = toJson(result);
        if (json != null) {
            redisService.save(cacheKey, json, CACHE_TTL);
        }

        return result;
    }

    /**
     * Lấy lịch khám của bác sĩ trong khoảng thời gian (dùng cho popup lịch làm việc)
     * Cache-Aside: cache theo doctorId + fromDate + toDate
     */
    public List<AppointmentDTO> getDoctorSchedule(Long doctorId,
                                                   LocalDate fromDate,
                                                   LocalDate toDate) {
        String cacheKey = KEY_DOC_SCHED + doctorId + ":" + fromDate + ":" + toDate;

        // Thử đọc từ Redis
        if (redisService.exists(cacheKey)) {
            Object raw = redisService.get(cacheKey);
            List<AppointmentDTO> cached = fromJsonList(raw);
            if (cached != null) {
                return cached; // Trả từ Redis
            }
        }

        // Cache miss → query DB
        List<AppointmentDTO> result = appointmentRepository
                .findByDoctorIdAndDateRange(doctorId, fromDate, toDate)
                .stream()
                .map(appointmentMapper::toDTO)
                .collect(Collectors.toList());

        // Lưu vào Redis
        String json = toJson(result);
        if (json != null) {
            redisService.save(cacheKey, json, CACHE_TTL);
        }

        return result;
    }
}
