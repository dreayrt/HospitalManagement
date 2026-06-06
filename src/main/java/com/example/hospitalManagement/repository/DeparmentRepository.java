package com.example.hospitalManagement.repository;

import com.example.hospitalManagement.entity.Departments;
import com.example.hospitalManagement.entity.Enum.DepartmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeparmentRepository extends JpaRepository<Departments, Long> {
    List<Departments> findByStatus(DepartmentStatus status);

    @Query("""
            select d.id as id, d.name as name, d.description as description
            from Departments d
            where d.status = :status
            order by d.name
            """)
    List<DepartmentSummary> findSummariesByStatus(@Param("status") DepartmentStatus status);

    @Query("""
            select d.id as id, d.name as name, d.description as description
            from Departments d
            where d.id = :id
            """)
    Optional<DepartmentSummary> findSummaryById(@Param("id") Long id);

    interface DepartmentSummary {
        Long getId();

        String getName();

        String getDescription();
    }
}
