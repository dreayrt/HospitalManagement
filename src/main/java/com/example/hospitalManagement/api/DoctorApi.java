package com.example.hospitalManagement.api;

import com.example.hospitalManagement.dto.DoctorDTO;
import com.example.hospitalManagement.dto.DoctorFilterRequest;
import com.example.hospitalManagement.dto.DoctorScheduleDTO;
import com.example.hospitalManagement.entity.Enum.UserStatus;
import com.example.hospitalManagement.service.DoctorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/doctors")
@Validated
public class DoctorApi {
    private final DoctorService doctorService;

    public DoctorApi(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    @GetMapping("/findAll")
    public List<DoctorDTO> findAll(@ModelAttribute DoctorFilterRequest filterRequest) {
        return doctorService.findAll(filterRequest);
    }

    @GetMapping("/statuses")
    public List<Map<String, String>> findDoctorStatuses() {
        return List.of(
                Map.of("value", UserStatus.ACTIVE.name(), "label", "Đang hoạt động"),
                Map.of("value", UserStatus.INACTIVE.name(), "label", "Tạm ngưng"),
                Map.of("value", UserStatus.LOCKED.name(), "label", "Đã khóa")
        );
    }

    @GetMapping("/{id}")
    public DoctorDTO findById(@PathVariable Long id) {
        return doctorService.findById(id);
    }

    @PostMapping
    public ResponseEntity<DoctorDTO> create(@RequestBody DoctorDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(doctorService.create(request));
    }

    @PutMapping("/{id}")
    public DoctorDTO update(@PathVariable Long id, @Valid @RequestBody DoctorDTO request) {
        return doctorService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        doctorService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/departments")
    public List<Map<String, Object>> findAllDepartments() {
        return doctorService.findAllDepartments();
    }

    @GetMapping("/users/available")
    public List<Map<String, Object>> findAvailableUsers(@RequestParam(required = false) Long doctorId) {
        return doctorService.findAvailableUsers(doctorId);
    }

    @GetMapping("/{doctorId}/schedules")
    public List<DoctorScheduleDTO> findSchedules(@PathVariable Long doctorId) {
        return doctorService.findSchedules(doctorId);
    }

    @PostMapping("/{doctorId}/schedules")
    public ResponseEntity<DoctorScheduleDTO> createSchedule(
            @PathVariable Long doctorId,
            @Valid @RequestBody DoctorScheduleDTO request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(doctorService.createSchedule(doctorId, request));
    }

    @PutMapping("/{doctorId}/schedules/{scheduleId}")
    public DoctorScheduleDTO updateSchedule(
            @PathVariable Long doctorId,
            @PathVariable Long scheduleId,
            @Valid @RequestBody DoctorScheduleDTO request
    ) {
        return doctorService.updateSchedule(doctorId, scheduleId, request);
    }

    @DeleteMapping("/{doctorId}/schedules/{scheduleId}")
    public ResponseEntity<Void> deleteSchedule(@PathVariable Long doctorId, @PathVariable Long scheduleId) {
        doctorService.deleteSchedule(doctorId, scheduleId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{doctorId}/activity-status")
    public Map<String, Object> getActivityStatus(@PathVariable Long doctorId) {
        return doctorService.getActivityStatus(doctorId);
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(NoSuchElementException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorBody(exception.getMessage()));
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<Map<String, String>> handleBadRequest(RuntimeException exception) {
        return ResponseEntity.badRequest().body(errorBody(exception.getMessage()));
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ResponseEntity<Map<String, String>> handleValidationException(Exception exception) {
        Map<String, String> body = new LinkedHashMap<>();
        body.put("message", "Dữ liệu đầu vào không hợp lệ");
        return ResponseEntity.badRequest().body(body);
    }

    private Map<String, String> errorBody(String message) {
        Map<String, String> body = new LinkedHashMap<>();
        body.put("message", message);
        return body;
    }
}
