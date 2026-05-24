package com.example.hospitalManagement.api;

import com.example.hospitalManagement.dto.AppointmentDTO;
import com.example.hospitalManagement.dto.AppointmentFilterRequestDTO;
import com.example.hospitalManagement.dto.CreateAppointmentRequestDTO;
import com.example.hospitalManagement.entity.Enum.AppointmentStatus;
import com.example.hospitalManagement.service.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/**
 * REST API xử lý Lịch khám (Appointments)
 * Chỉ chứa các endpoint REST trả về JSON, không render view.
 * Base URL: /api/appointments
 */
@RestController
@RequestMapping("/api/appointments")
@CrossOrigin(origins = "*")
public class AppointmentApi {

    @Autowired
    private AppointmentService appointmentService;

    // ===================== ĐẶT LỊCH KHÁM =====================

    /**
     * POST /api/appointments
     * Đặt lịch khám mới
     * Body: CreateAppointmentRequest (patientId, doctorId, appointmentDate, appointmentTime, reason)
     */
    @PostMapping
    public ResponseEntity<?> createAppointment(@Valid @RequestBody CreateAppointmentRequestDTO request) {
        try {
            AppointmentDTO result = appointmentService.createAppointment(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // ===================== XÁC NHẬN LỊCH KHÁM (Admin) =====================

    /**
     * PUT /api/appointments/{id}/confirm
     * Admin xác nhận lịch khám: PENDING → CONFIRMED
     */
    @PutMapping("/{id}/confirm")
    public ResponseEntity<?> confirmAppointment(@PathVariable Long id) {
        try {
            AppointmentDTO result = appointmentService.confirmAppointment(id);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // ===================== HỦY LỊCH KHÁM =====================

    /**
     * PUT /api/appointments/{id}/cancel
     * Hủy lịch khám - validate trạng thái hợp lệ (không hủy COMPLETED/IN_PROGRESS)
     * Body: { "reason": "Lý do hủy" }
     */
    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancelAppointment(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {
        try {
            String reason = (body != null) ? body.get("reason") : null;
            AppointmentDTO result = appointmentService.cancelAppointment(id, reason);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // ===================== DỜI LỊCH KHÁM =====================

    /**
     * PUT /api/appointments/{id}/reschedule
     * Dời lịch khám sang ngày/giờ mới
     * Body: { "newDate": "2024-06-01", "newTime": "09:00" }
     */
    @PutMapping("/{id}/reschedule")
    public ResponseEntity<?> rescheduleAppointment(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        try {
            LocalDate newDate = LocalDate.parse(body.get("newDate"));
            LocalTime newTime = LocalTime.parse(body.get("newTime"));
            AppointmentDTO result = appointmentService.rescheduleAppointment(id, newDate, newTime);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // ===================== XEM DANH SÁCH + FILTER + PAGINATION =====================

    /**
     * GET /api/appointments
     * Lấy danh sách lịch khám với filter và phân trang
     * Query params: doctorId, patientId, status, fromDate, toDate, search, page, size
     */
    @GetMapping
    public ResponseEntity<Page<AppointmentDTO>> getAppointments(
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        AppointmentFilterRequestDTO filter = new AppointmentFilterRequestDTO();
        filter.setDoctorId(doctorId);
        filter.setPatientId(patientId);
        filter.setFromDate(fromDate);
        filter.setToDate(toDate);
        filter.setSearch(search);
        filter.setPage(page);
        filter.setSize(size);

        // Parse status enum an toàn, không throw exception nếu sai
        if (status != null && !status.isBlank()) {
            try {
                filter.setStatus(AppointmentStatus.valueOf(status.toUpperCase()));
            } catch (IllegalArgumentException ignored) {}
        }

        return ResponseEntity.ok(appointmentService.getAppointments(filter));
    }

    /**
     * GET /api/appointments/{id}
     * Xem chi tiết 1 lịch khám
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getAppointmentById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(appointmentService.getAppointmentById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * GET /api/appointments/doctor/{doctorId}/schedule
     * Lấy lịch khám của bác sĩ trong khoảng thời gian (dùng cho popup lịch làm việc)
     * Query params: fromDate (mặc định hôm nay), toDate (mặc định 7 ngày tới)
     */
    @GetMapping("/doctor/{doctorId}/schedule")
    public ResponseEntity<List<AppointmentDTO>> getDoctorSchedule(
            @PathVariable Long doctorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {

        LocalDate from = (fromDate != null) ? fromDate : LocalDate.now();
        LocalDate to   = (toDate   != null) ? toDate   : from.plusDays(6);

        List<AppointmentDTO> schedule = appointmentService.getDoctorSchedule(doctorId, from, to);
        return ResponseEntity.ok(schedule);
    }
}
