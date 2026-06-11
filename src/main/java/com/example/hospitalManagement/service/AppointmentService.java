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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AppointmentService {
    private static final String KEY_APPT        = "appointment:";
    
    private static final String KEY_DOC_SCHED   = "appt:doctor:";
    
    private static final String KEY_APPT_LIST   = "appt:list:";
    
    private static final long   CACHE_TTL       = 300L;
    
    private static final long   CACHE_LIST_TTL  = 120L;

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

    
    @Autowired
    private RedisService redisService;

    @Autowired
    private RedisTemplate redisTemplate;

    
    private final ObjectMapper objectMapper;

    public AppointmentService() {
        this.objectMapper = new ObjectMapper();
        
        this.objectMapper.registerModule(new JavaTimeModule());
        
        this.objectMapper.disable(
                com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS
        );
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return null; 
        }
    }

    
    private AppointmentDTO fromJsonDTO(Object raw) {
        try {
            if (raw == null) return null;
            return objectMapper.readValue(raw.toString(), AppointmentDTO.class);
        } catch (Exception e) {
            return null;
        }
    }
    
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

    public void evictAppointmentCache(Long appointmentId, Long doctorId) {
        redisService.remove(KEY_APPT + appointmentId);
        if (doctorId != null) {
            LocalDate today = LocalDate.now();
            for (int i = 0; i < 30; i++) {
                LocalDate d = today.plusDays(i);
                
                redisService.remove(KEY_DOC_SCHED + doctorId + ":" + d + ":" + d);
            }
            redisService.remove(KEY_DOC_SCHED + doctorId + ":" + today + ":" + today.plusDays(6));
            redisService.remove(KEY_DOC_SCHED + doctorId + ":" + today + ":" + today.plusDays(29));
        }
        try {
            Set<String> keys = redisTemplate.keys(KEY_APPT_LIST + "*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        } catch (Exception ignored) {}
    }

    @Transactional
    public AppointmentDTO createAppointment(CreateAppointmentRequestDTO request) {
        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new RuntimeException(
                        "Không tìm thấy bác sĩ với ID: " + request.getDoctorId()));
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
        }

        Appointments saved = appointmentRepository.save(appointment);

        
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

        
        String json = toJson(result);
        if (json != null) {
            redisService.save(KEY_APPT + saved.getId(), json, CACHE_TTL);
        }

        
        evictAppointmentCache(saved.getId(), request.getDoctorId());

        return result;
    }
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

        
        Long doctorId = updated.getDoctor() != null ? updated.getDoctor().getId() : null;
        evictAppointmentCache(appointmentId, doctorId);
        String json = toJson(result);
        if (json != null) redisService.save(KEY_APPT + appointmentId, json, CACHE_TTL);

        return result;
    }
    @Transactional
    public AppointmentDTO cancelAppointment(Long appointmentId, String cancelReason) {
        
        Long doctorId = null;

        Appointments appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lịch khám ID: " + appointmentId));

        if (appointment.getDoctor() != null) {
            doctorId = appointment.getDoctor().getId();
        }

        
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

        
        try {
            String msg = String.format(
                    "{\"type\":\"APPOINTMENT_CANCELLED\",\"appointmentId\":%d,\"cancelReason\":\"%s\"}",
                    updated.getId(), cancelReason != null ? cancelReason : ""
            );
            notificationProducer.sendCancellation(msg);
        } catch (Exception ignored) {}

        AppointmentDTO result = appointmentMapper.toDTO(updated);

        
        evictAppointmentCache(appointmentId, doctorId);
        String json = toJson(result);
        if (json != null) {
            redisService.save(KEY_APPT + appointmentId, json, CACHE_TTL);
        }

        return result;
    }
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

        
        evictAppointmentCache(appointmentId, doctorId);
        String json = toJson(result);
        if (json != null) {
            redisService.save(KEY_APPT + appointmentId, json, CACHE_TTL);
        }

        return result;
    }

    public Page<AppointmentDTO> getAppointments(AppointmentFilterRequestDTO filter) {
        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize());

        
        boolean useListCache = false;
        String cacheKey = buildListCacheKey(filter);

        
        if (useListCache && redisService.exists(cacheKey)) {
            Object raw = redisService.get(cacheKey);
            List<AppointmentDTO> cached = fromJsonList(raw);
            if (cached != null) {
                
                return new PageImpl<>(cached, pageable, cached.size());
            }
        }

        
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

        
        if (useListCache && filter.getPage() == 0 && result.getContent().size() <= 50) {
            String json = toJson(result.getContent());
            if (json != null) {
                redisService.save(cacheKey, json, CACHE_LIST_TTL);
            }
        }

        return result;
    }
    
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

    public AppointmentDTO getAppointmentById(Long id) {
        String cacheKey = KEY_APPT + id;

        
        if (redisService.exists(cacheKey)) {
            Object raw = redisService.get(cacheKey);
            AppointmentDTO cached = fromJsonDTO(raw);
            if (cached != null) {
                return cached; 
            }
        }

        
        AppointmentDTO result = appointmentRepository.findById(id)
                .map(appointmentMapper::toDTO)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lịch khám ID: " + id));

        String json = toJson(result);
        if (json != null) {
            redisService.save(cacheKey, json, CACHE_TTL);
        }

        return result;
    }

    public List<AppointmentDTO> getDoctorSchedule(Long doctorId,
                                                   LocalDate fromDate,
                                                   LocalDate toDate) {
        String cacheKey = KEY_DOC_SCHED + doctorId + ":" + fromDate + ":" + toDate;

        
        if (redisService.exists(cacheKey)) {
            Object raw = redisService.get(cacheKey);
            List<AppointmentDTO> cached = fromJsonList(raw);
            if (cached != null) {
                return cached; 
            }
        }
        List<AppointmentDTO> result = appointmentRepository
                .findByDoctorIdAndDateRange(doctorId, fromDate, toDate)
                .stream()
                .map(appointmentMapper::toDTO)
                .collect(Collectors.toList());

        String json = toJson(result);
        if (json != null) {
            redisService.save(cacheKey, json, CACHE_TTL);
        }

        return result;
    }
}
