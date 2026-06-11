package com.example.hospitalManagement.repository;

import com.example.hospitalManagement.entity.Doctor;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DoctorRepository extends JpaRepository<Doctor, Long>, JpaSpecificationExecutor<Doctor> {
    boolean existsByLicenseNumber(String licenseNumber);

    boolean existsByLicenseNumberAndIdNot(String licenseNumber, Long id);

    @EntityGraph(attributePaths = {"user"})
    List<Doctor> findAll(Specification<Doctor> specification, Sort sort);

    @Query("SELECT d.id FROM Doctor d WHERE d.user.fullName = :fullName")
    Long findIdByUserFullName(@org.springframework.data.repository.query.Param("fullName") String fullName);

    List<Doctor> findByDepartmentId(Long departmentId);
}
