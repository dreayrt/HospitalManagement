package com.example.hospitalManagement.repository;

import com.example.hospitalManagement.entity.Departments;
import jakarta.persistence.Id;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeparmentRepository extends JpaRepository<Departments, Long> {

}
