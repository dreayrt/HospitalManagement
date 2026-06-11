package com.example.hospitalManagement.repository;

import com.example.hospitalManagement.entity.MedicalRecords;
import com.example.hospitalManagement.entity.Enum.AppointmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecords, Long> {
    Page<MedicalRecords> findByPatientId(Long patientId, Pageable pageable);
    java.util.List<MedicalRecords> findByDoctorId(Long doctorId);

    @Query(value = """
            SELECT mr FROM MedicalRecords mr
            LEFT JOIN FETCH mr.appointment a
            LEFT JOIN FETCH mr.patient p
            LEFT JOIN FETCH p.user pu
            LEFT JOIN FETCH mr.doctor d
            LEFT JOIN FETCH d.user du
            LEFT JOIN FETCH d.department dept
            WHERE (:status IS NULL OR a.status = :status)
              AND (:search IS NULL OR :search = ''
                   OR LOWER(pu.fullName) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(p.patientCode) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(du.fullName) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(mr.diagnosis) LIKE LOWER(CONCAT('%', :search, '%')))
            """,
            countQuery = """
            SELECT COUNT(mr) FROM MedicalRecords mr
            LEFT JOIN mr.patient p
            LEFT JOIN p.user pu
            LEFT JOIN mr.doctor d
            LEFT JOIN d.user du
            LEFT JOIN mr.appointment a
            WHERE (:status IS NULL OR a.status = :status)
              AND (:search IS NULL OR :search = ''
                   OR LOWER(pu.fullName) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(p.patientCode) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(du.fullName) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(mr.diagnosis) LIKE LOWER(CONCAT('%', :search, '%')))
            """)
    Page<MedicalRecords> findWithSearch(
            @Param("search") String search,
            @Param("status") AppointmentStatus status,
            Pageable pageable);

    @Query("""
            SELECT COUNT(mr) FROM MedicalRecords mr
            LEFT JOIN mr.patient p
            LEFT JOIN p.user u
            LEFT JOIN mr.doctor d
            LEFT JOIN d.user du
            LEFT JOIN mr.appointment a
            WHERE (:status IS NULL OR a.status = :status)
              AND (:search IS NULL OR :search = ''
                   OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(p.patientCode) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(du.fullName) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(mr.diagnosis) LIKE LOWER(CONCAT('%', :search, '%')))
            """)
    long countWithSearch(@Param("search") String search, @Param("status") AppointmentStatus status);
}
