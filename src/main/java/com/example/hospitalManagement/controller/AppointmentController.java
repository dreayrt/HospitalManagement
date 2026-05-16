package com.example.hospitalManagement.controller;

import com.example.hospitalManagement.dto.AppointmentDTO;
import com.example.hospitalManagement.dto.AppointmentFilterRequest;
import com.example.hospitalManagement.dto.CreateAppointmentRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    // ============ PUBLIC ENDPOINTS ============

    /**
     * Get appointment by ID (accessible to all authenticated users)
     */
    @GetMapping("/{appointmentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AppointmentDTO> getAppointmentById(@PathVariable Long appointmentId) {
        AppointmentDTO appointment = appointmentService.getAppointmentById(appointmentId);
        return ResponseEntity.ok(appointment);
    }

    // ============ PATIENT ENDPOINTS ============

    /**
     * PATIENT: Book a new appointment
     * POST /api/v1/appointments/book
     */
    @PostMapping("/book")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<Map<String, Object>> bookAppointment(@Valid @RequestBody CreateAppointmentRequest request) {
        try {
            AppointmentDTO appointment = appointmentService.bookAppointment(request);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Appointment booked successfully (pending confirmation)");
            response.put("data", appointment);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * PATIENT: Cancel their own appointment
     * DELETE /api/v1/appointments/{appointmentId}/cancel?reason=reason
     */
    @DeleteMapping("/{appointmentId}/cancel")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<Map<String, Object>> cancelAppointment(
            @PathVariable Long appointmentId,
            @RequestParam(required = false, defaultValue = "Patient cancelled") String reason) {
        try {
            AppointmentDTO appointment = appointmentService.cancelAppointment(appointmentId, reason);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Appointment cancelled successfully");
            response.put("data", appointment);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * PATIENT: Reschedule their own appointment
     * PUT /api/v1/appointments/{appointmentId}/reschedule
     */
    @PutMapping("/{appointmentId}/reschedule")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<Map<String, Object>> rescheduleAppointment(
            @PathVariable Long appointmentId,
            @Valid @RequestBody CreateAppointmentRequest newRequest) {
        try {
            AppointmentDTO appointment = appointmentService.rescheduleAppointment(appointmentId, newRequest);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Appointment rescheduled successfully");
            response.put("data", appointment);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * PATIENT: View their own appointments
     * GET /api/v1/appointments/my-appointments?page=0&size=20
     */
    @GetMapping("/my-appointments")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<List<AppointmentDTO>> getMyAppointments() {
        // This should use the authenticated patient's ID from SecurityContext
        // For now, assuming patientId is passed; in production, extract from @AuthenticationPrincipal
        return ResponseEntity.ok(List.of());
    }

    // ============ RECEPTIONIST ENDPOINTS ============

    /**
     * RECEPTIONIST: Confirm appointment (move from PENDING to CONFIRMED)
     * PUT /api/v1/appointments/{appointmentId}/confirm
     */
    @PutMapping("/{appointmentId}/confirm")
    @PreAuthorize("hasRole('RECEPTIONIST')")
    public ResponseEntity<Map<String, Object>> confirmAppointment(@PathVariable Long appointmentId) {
        try {
            AppointmentDTO appointment = appointmentService.confirmAppointment(appointmentId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Appointment confirmed successfully");
            response.put("data", appointment);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * RECEPTIONIST: View all appointments with filters
     * GET /api/v1/appointments/list?patientId=1&doctorId=2&status=CONFIRMED&page=0&size=20
     */
    @GetMapping("/list")
    @PreAuthorize("hasRole('RECEPTIONIST') or hasRole('ADMIN')")
    public ResponseEntity<Page<AppointmentDTO>> getAllAppointments(AppointmentFilterRequest filter) {
        Page<AppointmentDTO> appointments = appointmentService.getAllAppointments(filter);
        return ResponseEntity.ok(appointments);
    }

    /**
     * RECEPTIONIST: View appointments by patient
     * GET /api/v1/appointments/patient/{patientId}
     */
    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasRole('RECEPTIONIST') or hasRole('ADMIN')")
    public ResponseEntity<List<AppointmentDTO>> getAppointmentsByPatient(@PathVariable Long patientId) {
        List<AppointmentDTO> appointments = appointmentService.getAppointmentsByPatient(patientId);
        return ResponseEntity.ok(appointments);
    }

    // ============ DOCTOR ENDPOINTS ============

    /**
     * DOCTOR: View their own appointments
     * GET /api/v1/appointments/doctor/{doctorId}
     */
    @GetMapping("/doctor/{doctorId}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<List<AppointmentDTO>> getAppointmentsByDoctor(@PathVariable Long doctorId) {
        List<AppointmentDTO> appointments = appointmentService.getAppointmentsByDoctor(doctorId);
        return ResponseEntity.ok(appointments);
    }

    /**
     * DOCTOR: View their appointments in date range
     * GET /api/v1/appointments/doctor/{doctorId}/date-range?from=2026-05-16T08:00:00&to=2026-05-16T17:00:00
     */
    @GetMapping("/doctor/{doctorId}/date-range")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<List<AppointmentDTO>> getDoctorAppointmentsByDateRange(
            @PathVariable Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        List<AppointmentDTO> appointments = appointmentService.getDoctorAppointmentsByDateRange(doctorId, from, to);
        return ResponseEntity.ok(appointments);
    }

    /**
     * DOCTOR: Mark appointment as completed (after visiting patient)
     * PUT /api/v1/appointments/{appointmentId}/complete
     */
    @PutMapping("/{appointmentId}/complete")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<Map<String, Object>> completeAppointment(@PathVariable Long appointmentId) {
        try {
            AppointmentDTO appointment = appointmentService.completeAppointment(appointmentId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Appointment marked as completed");
            response.put("data", appointment);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * DOCTOR: Mark appointment as no-show (patient didn't attend)
     * PUT /api/v1/appointments/{appointmentId}/no-show
     */
    @PutMapping("/{appointmentId}/no-show")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<Map<String, Object>> markAsNoShow(@PathVariable Long appointmentId) {
        try {
            AppointmentDTO appointment = appointmentService.markAsNoShow(appointmentId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Appointment marked as no-show");
            response.put("data", appointment);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    // ============ ADMIN/RECEPTIONIST ENDPOINTS ============

    /**
     * ADMIN: View appointments by status
     * GET /api/v1/appointments/status/{status}
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RECEPTIONIST')")
    public ResponseEntity<List<AppointmentDTO>> getAppointmentsByStatus(@PathVariable String status) {
        try {
            List<AppointmentDTO> appointments = appointmentService.getAppointmentsByStatus(status);
            return ResponseEntity.ok(appointments);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * ADMIN: View patient appointments in date range
     * GET /api/v1/appointments/patient/{patientId}/date-range?from=2026-05-16T08:00:00&to=2026-05-16T17:00:00
     */
    @GetMapping("/patient/{patientId}/date-range")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RECEPTIONIST')")
    public ResponseEntity<List<AppointmentDTO>> getPatientAppointmentsByDateRange(
            @PathVariable Long patientId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        List<AppointmentDTO> appointments = appointmentService.getPatientAppointmentsByDateRange(patientId, from, to);
        return ResponseEntity.ok(appointments);
    }
}

