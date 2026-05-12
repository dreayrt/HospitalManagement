package com.example.hospitalManagement.repository;
import com.example.hospitalManagement.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    Optional<Doctor> findByLicenseNumber(String licenseNumber);
    Optional<Doctor> findByUserId(Long userId);
}