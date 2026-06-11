package com.example.hospitalManagement.repository;

import com.example.hospitalManagement.entity.PrescriptionDetail;
import org.springframework.data.jpa.repository.JpaRepository;


public interface PrescriptionDetailRepository extends JpaRepository<PrescriptionDetail, Long> {
}
