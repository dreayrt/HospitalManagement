package com.example.hospitalManagement.repository;

import com.example.hospitalManagement.entity.Departments;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<Departments,Long> {
}
