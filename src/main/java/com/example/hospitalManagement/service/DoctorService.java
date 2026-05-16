package com.example.hospitalManagement.service;

import com.example.hospitalManagement.entity.*;
import com.example.hospitalManagement.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class DoctorService {
    private final DoctorRepository doctorRepository;
    private final DoctorScheduleRepository scheduleRepository;
    private final SpecialtyRepository specialtyRepository;

    public DoctorService(DoctorRepository dr, DoctorScheduleRepository dsr, SpecialtyRepository sr) {
        this.doctorRepository = dr;
        this.scheduleRepository = dsr;
        this.specialtyRepository = sr;
    }

    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAll();
    }

    public java.util.Optional<Doctor> findByUserId(Long userId) {
        return doctorRepository.findByUserId(userId);
    }

    // 1. Quản lý hồ sơ (CRUD)
    @Transactional
    public Doctor createDoctor(Doctor doctor) {
        return doctorRepository.save(doctor);
    }

    @Transactional
    public Doctor updateDoctor(Long id, Doctor details) {
        Doctor doctor = doctorRepository.findById(id).orElseThrow();
        if (details.getUserId() != null) doctor.setUserId(details.getUserId());
        if (details.getDepartmentId() != null) doctor.setDepartmentId(details.getDepartmentId());
        doctor.setSpecialization(details.getSpecialization());
        doctor.setLicenseNumber(details.getLicenseNumber());
        doctor.setExperienceYears(details.getExperienceYears());
        doctor.setStatus(details.getStatus());
        return doctorRepository.save(doctor);
    }

    @Transactional
    public void deleteDoctor(Long id) {
        doctorRepository.deleteById(id);
    }

    // 2. Gán chuyên khoa cho bác sĩ
    @Transactional
    public void assignSpecialty(Long doctorId, Long specialtyId) {
        Doctor doctor = doctorRepository.findById(doctorId).orElseThrow();
        Specialty specialty = specialtyRepository.findById(specialtyId).orElseThrow();
        doctor.setSpecialization(specialty.getName());
        doctorRepository.save(doctor);
    }

    // 3. Quản lý lịch làm việc
    public DoctorSchedule addSchedule(DoctorSchedule schedule) {
        return scheduleRepository.save(schedule);
    }

    public List<DoctorSchedule> getSchedulesByDoctor(Long doctorId) {
        return scheduleRepository.findByDoctorId(doctorId);
    }

    // 4. Xem trạng thái hoạt động
    public List<Doctor> getDoctorsByStatus(Doctor.Status status) {
        return doctorRepository.findAll().stream()
                .filter(d -> d.getStatus() == status)
                .toList();
    }
}