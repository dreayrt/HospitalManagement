package com.example.hospitalManagement.service;

import com.example.hospitalManagement.dto.CheckInDTO;
import com.example.hospitalManagement.entity.Appointments;
import com.example.hospitalManagement.entity.CheckIn;
import com.example.hospitalManagement.entity.Enum.AppointmentStatus;
import com.example.hospitalManagement.entity.Enum.CheckInStatus;
import com.example.hospitalManagement.repository.AppointmentRepository;
import com.example.hospitalManagement.repository.CheckInRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class CheckInService {

    private final CheckInRepository checkInRepository;
    private final AppointmentRepository appointmentRepository;
    private final RedisService redisService;
    private final AppointmentService appointmentService;
    private final ObjectMapper objectMapper;

    private static final String KEY_CHECKIN_ALL = "checkin:all";
    private static final long CACHE_TTL = 120L;

    public CheckInService(CheckInRepository checkInRepository,
                          AppointmentRepository appointmentRepository,
                          RedisService redisService,
                          AppointmentService appointmentService) {
        this.checkInRepository = checkInRepository;
        this.appointmentRepository = appointmentRepository;
        this.redisService = redisService;
        this.appointmentService = appointmentService;

        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    


    @Transactional
    public CheckInDTO doCheckIn(Long appointmentId) {
        Appointments appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy lịch hẹn khám"));

        if (checkInRepository.existsByAppointmentsId(appointmentId)) {
            throw new IllegalStateException("Lịch hẹn này đã được xác nhận check-in trước đó rồi!");
        }

        LocalDate today = LocalDate.now();
        Integer maxQueue = checkInRepository.findMaxQueueNumberForToday(today);
        int queueNumber = (maxQueue == null ? 0 : maxQueue) + 1;

        CheckIn checkIn = new CheckIn();
        checkIn.setAppointments(appointment);
        checkIn.setCheckInTime(LocalDateTime.now());
        checkIn.setStatus(CheckInStatus.WAITING);
        checkIn.setQueueNumber(queueNumber);
        CheckIn saved = checkInRepository.save(checkIn);

        appointment.setStatus(AppointmentStatus.IN_PROGRESS);
        appointmentRepository.save(appointment);

        redisService.remove(KEY_CHECKIN_ALL);
        
        appointmentService.evictAppointmentCache(appointment.getId(), appointment.getDoctor() != null ? appointment.getDoctor().getId() : null);
        return mapToDTO(saved);
    }

    



    @Transactional
    public CheckInDTO doCheckIn(Long appointmentId, Integer priority, String notes) {
        Appointments appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy lịch hẹn khám"));

        if (checkInRepository.existsByAppointmentsId(appointmentId)) {
            throw new IllegalStateException("Lịch hẹn này đã được xác nhận check-in trước đó rồi!");
        }

        LocalDate today = LocalDate.now();
        Integer maxQueue = checkInRepository.findMaxQueueNumberForToday(today);
        int queueNumber = (maxQueue == null ? 0 : maxQueue) + 1;

        
        CheckIn checkIn = new CheckIn();
        checkIn.setAppointments(appointment);
        checkIn.setCheckInTime(LocalDateTime.now());
        checkIn.setStatus(CheckInStatus.WAITING);
        checkIn.setQueueNumber(queueNumber);
        CheckIn saved = checkInRepository.save(checkIn);

        appointment.setStatus(AppointmentStatus.IN_PROGRESS);
        appointmentRepository.save(appointment);

        redisService.remove(KEY_CHECKIN_ALL);
        
        appointmentService.evictAppointmentCache(appointment.getId(), appointment.getDoctor() != null ? appointment.getDoctor().getId() : null);

        
        CheckInDTO dto = mapToDTO(saved);
        if (priority != null) {
            dto.setPriority(priority);
            dto.setPriorityLabel(buildPriorityLabel(priority));
        }
        if (notes != null) {
            dto.setNotes(notes);
        }
        return dto;
    }
    @Transactional(readOnly = true)
    public Page<CheckInDTO> findWithFilters(CheckInStatus status,
                                             LocalDate fromDate,
                                             LocalDate toDate,
                                             Long doctorId,
                                             String search,
                                             int page,
                                             int size) {
        Pageable pageable = PageRequest.of(page, size);
        return checkInRepository.findWithFilters(status, fromDate, toDate, doctorId, search, pageable)
                .map(this::mapToDTO);
    }

    @Transactional(readOnly = true)
    public List<CheckInDTO> findAll() {
        if (redisService.exists(KEY_CHECKIN_ALL)) {
            Object raw = redisService.get(KEY_CHECKIN_ALL);
            if (raw != null) {
                try {
                    List<CheckInDTO> cached = objectMapper.readValue(
                            raw.toString(),
                            new TypeReference<List<CheckInDTO>>() {}
                    );
                    if (cached != null) return cached;
                } catch (Exception ignored) {}
            }
        }

        List<CheckInDTO> result = checkInRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        try {
            String json = objectMapper.writeValueAsString(result);
            redisService.save(KEY_CHECKIN_ALL, json, CACHE_TTL);
        } catch (Exception ignored) {}

        return result;
    }

    


    @Transactional(readOnly = true)
    public Map<String, Long> getStatsForDate(LocalDate date) {
        Map<String, Long> stats = new HashMap<>();
        stats.put("WAITING",    checkInRepository.countByDateAndStatus(date, CheckInStatus.WAITING));
        stats.put("CHECKED_IN", checkInRepository.countByDateAndStatus(date, CheckInStatus.CHECKED_IN));
        stats.put("DONE",       checkInRepository.countByDateAndStatus(date, CheckInStatus.DONE));
        long total = stats.values().stream().mapToLong(Long::longValue).sum();
        stats.put("TOTAL", total);
        return stats;
    }
    @Transactional(readOnly = true)
    public Map<String, Long> getTodayStats() {
        return getStatsForDate(LocalDate.now());
    }
    @Transactional
    public CheckInDTO confirmArrival(Long checkInId) {
        CheckIn checkIn = checkInRepository.findById(checkInId)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy check-in ID: " + checkInId));

        if (checkIn.getStatus() != CheckInStatus.WAITING) {
            throw new IllegalStateException("Check-in không ở trạng thái WAITING. Hiện tại: " + checkIn.getStatus());
        }

        checkIn.setStatus(CheckInStatus.CHECKED_IN);
        
        CheckIn updated = checkInRepository.save(checkIn);
        redisService.remove(KEY_CHECKIN_ALL);
        return mapToDTO(updated);
    }
    @Transactional
    public CheckInDTO completeCheckIn(Long checkInId) {
        CheckIn checkIn = checkInRepository.findById(checkInId)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy check-in ID: " + checkInId));

        if (checkIn.getStatus() != CheckInStatus.CHECKED_IN) {
            throw new IllegalStateException("Check-in không ở trạng thái CHECKED_IN. Hiện tại: " + checkIn.getStatus());
        }

        checkIn.setStatus(CheckInStatus.DONE);
        Appointments appt = checkIn.getAppointments();
        if (appt != null) {
            appt.setStatus(AppointmentStatus.COMPLETED);
            appointmentRepository.save(appt);
        }

        CheckIn updated = checkInRepository.save(checkIn);
        redisService.remove(KEY_CHECKIN_ALL);
        
        if (appt != null) {
            appointmentService.evictAppointmentCache(appt.getId(), appt.getDoctor() != null ? appt.getDoctor().getId() : null);
        }
        return mapToDTO(updated);
    }
    @Transactional
    public CheckInDTO updatePriority(Long checkInId, Integer priority) {
        CheckIn checkIn = checkInRepository.findById(checkInId)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy check-in ID: " + checkInId));

        
        CheckInDTO dto = mapToDTO(checkIn);
        if (priority != null) {
            dto.setPriority(priority);
            dto.setPriorityLabel(buildPriorityLabel(priority));
        }
        return dto;
    }

    private String buildPriorityLabel(Integer priority) {
        if (priority == null) return "Bình thường";
        return switch (priority) {
            case 3 -> "Khẩn cấp";
            case 2 -> "Ưu tiên";
            default -> "Bình thường";
        };
    }

    private CheckInDTO mapToDTO(CheckIn checkIn) {
        CheckInDTO dto = new CheckInDTO();
        
        dto.setId(checkIn.getId());
        dto.setCheckInTime(checkIn.getCheckInTime());
        dto.setQueueNumber(checkIn.getQueueNumber());
        dto.setStatus(checkIn.getStatus());

        
        dto.setPriority(1);
        dto.setPriorityLabel("Bình thường");

        if (checkIn.getStatus() != null) {
            switch (checkIn.getStatus()) {
                case WAITING    -> dto.setStatusLabel("Đang chờ");
                case CHECKED_IN -> dto.setStatusLabel("Đã vào khám");
                case DONE       -> dto.setStatusLabel("Hoàn thành");
                default         -> dto.setStatusLabel(checkIn.getStatus().name());
            }
        }

        Appointments appt = checkIn.getAppointments();
        if (appt != null) {
            dto.setAppointmentId(appt.getId());
            dto.setAppointmentDate(appt.getAppointmentDate());
            dto.setAppointmentTime(appt.getAppointmentTime());
            dto.setAppointmentReason(appt.getReason());

            if (appt.getPatient() != null) {
                dto.setPatientId(appt.getPatient().getId());
                dto.setPatientCode(appt.getPatient().getPatientCode());
                dto.setPatientDob(appt.getPatient().getDateOfBirth());
                if (appt.getPatient().getUser() != null) {
                    dto.setPatientName(appt.getPatient().getUser().getFullName());
                    dto.setPatientPhone(appt.getPatient().getUser().getPhone());
                }
            }

            if (appt.getDoctor() != null) {
                dto.setDoctorId(appt.getDoctor().getId());
                if (appt.getDoctor().getUser() != null) {
                    dto.setDoctorName(appt.getDoctor().getUser().getFullName());
                }
                if (appt.getDoctor().getDepartment() != null) {
                    dto.setDepartmentName(appt.getDoctor().getDepartment().getName());
                }
            }
        }

        return dto;
    }
}
