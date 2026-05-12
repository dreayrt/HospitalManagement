package com.example.hospitalManagement.controller;

import com.example.hospitalManagement.entity.Doctor;
import com.example.hospitalManagement.entity.DoctorSchedule;
import com.example.hospitalManagement.service.DoctorService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/doctors")
public class DoctorController {

    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    // 1. ADMIN quản lý: Tạo hồ sơ bác sĩ mới
    @PostMapping("/admin/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Doctor> createDoctor(@RequestBody Doctor doctor) {
        return ResponseEntity.ok(doctorService.createDoctor(doctor));
    }

    // 2. ADMIN quản lý: Gán chuyên khoa cho bác sĩ
    @PutMapping("/admin/{doctorId}/assign-specialty/{specialtyId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> assignSpecialty(@PathVariable Long doctorId, @PathVariable Long specialtyId) {
        doctorService.assignSpecialty(doctorId, specialtyId);
        return ResponseEntity.ok("Admin đã gán chuyên khoa thành công!");
    }

    // 3. DOCTOR xem lịch của chính mình
    @GetMapping("/my-schedules/{doctorId}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<List<DoctorSchedule>> getMySchedules(@PathVariable Long doctorId) {
        List<DoctorSchedule> schedules = doctorService.getSchedulesByDoctor(doctorId);
        return ResponseEntity.ok(schedules);
    }

    // 4. Xem danh sách bác sĩ đang hoạt động
    @GetMapping("/active")
    public ResponseEntity<List<Doctor>> getActiveDoctors() {
        return ResponseEntity.ok(doctorService.getDoctorsByStatus(Doctor.Status.ACTIVE));
    }
}