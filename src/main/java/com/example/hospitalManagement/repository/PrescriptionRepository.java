package com.example.hospitalManagement.repository;

import com.example.hospitalManagement.entity.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrescriptionRepository  extends JpaRepository<Prescription,Long> {
}
