package com.example.hospitalManagement.repository;

import com.example.hospitalManagement.entity.Patient;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {
    Optional<Patient> findByPatientCode(String patientCode);
    @Query("SELECT p.id FROM Patient p WHERE p.user.fullName = :fullName")
    Long findIdByUserFullName(@Param("fullName") String fullName);
}
