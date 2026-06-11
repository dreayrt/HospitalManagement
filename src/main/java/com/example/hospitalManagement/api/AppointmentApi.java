package com.example.hospitalManagement.api;

import com.example.hospitalManagement.dto.AppointmentDTO;
import com.example.hospitalManagement.dto.AppointmentFilterRequestDTO;
import com.example.hospitalManagement.dto.CreateAppointmentRequestDTO;
import com.example.hospitalManagement.entity.Enum.AppointmentStatus;
import com.example.hospitalManagement.entity.User;
import com.example.hospitalManagement.repository.userRepository;
import com.example.hospitalManagement.service.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@RestController
@RequestMapping("/api/appointments")
@CrossOrigin(origins = "*")
public class AppointmentApi {

    @Autowired
    private AppointmentService appointmentService;
    @Autowired
    private userRepository userRepository;

    @GetMapping("/statuses")
    public List<Map<String, String>> getStatuses() {
        return List.of(
                Map.of("value", AppointmentStatus.PENDING.name(), "label", "Chờ xác nhận"),
                Map.of("value", AppointmentStatus.CONFIRMED.name(), "label", "Đã xác nhận"),
                Map.of("value", AppointmentStatus.COMPLETED.name(), "label", "Hoàn thành")
        );
    }

//    @PostMapping
//    public ResponseEntity<?> createAppointment(@Valid @RequestBody CreateAppointmentRequestDTO request) {
//        try {
//            AppointmentDTO result = appointmentService.createAppointment(request);
//            return ResponseEntity.status(HttpStatus.CREATED).body(result);
//        } catch (RuntimeException e) {
//            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
//        }
//    }


//    @PutMapping("/{id}/confirm")
//    public ResponseEntity<?> confirmAppointment(@PathVariable Long id) {
//        try {
//            AppointmentDTO result = appointmentService.confirmAppointment(id);
//            return ResponseEntity.ok(result);
//        } catch (RuntimeException e) {
//            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
//        }
//    }


//    @PutMapping("/{id}/cancel")
//    public ResponseEntity<?> cancelAppointment(
//            @PathVariable Long id,
//            @RequestBody(required = false) Map<String, String> body) {
//        try {
//            String reason = (body != null) ? body.get("reason") : null;
//            AppointmentDTO result = appointmentService.cancelAppointment(id, reason);
//            return ResponseEntity.ok(result);
//        } catch (RuntimeException e) {
//            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
//        }
//    }


//    @PutMapping("/{id}/reschedule")
//    public ResponseEntity<?> rescheduleAppointment(
//            @PathVariable Long id,
//            @RequestBody Map<String, String> body) {
//        try {
//            LocalDate newDate = LocalDate.parse(body.get("newDate"));
//            LocalTime newTime = LocalTime.parse(body.get("newTime"));
//            AppointmentDTO result = appointmentService.rescheduleAppointment(id, newDate, newTime);
//            return ResponseEntity.ok(result);
//        } catch (RuntimeException e) {
//            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
//        }
//    }


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

        
        if (status != null && !status.isBlank()) {
            try {
                filter.setStatus(AppointmentStatus.valueOf(status.toUpperCase()));
            } catch (IllegalArgumentException ignored) {}
        }

        return ResponseEntity.ok(appointmentService.getAppointments(filter));
    }



    @GetMapping("/{id}")
    public ResponseEntity<?> getAppointmentById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(appointmentService.getAppointmentById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

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
