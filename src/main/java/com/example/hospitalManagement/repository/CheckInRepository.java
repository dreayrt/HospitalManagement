package com.example.hospitalManagement.repository;

import com.example.hospitalManagement.entity.CheckIn;
import com.example.hospitalManagement.entity.Enum.CheckInStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;





@Repository
public interface CheckInRepository extends JpaRepository<CheckIn, Long> {

    Optional<CheckIn> findByAppointmentsId(Long appointmentId);
    boolean existsByAppointmentsId(Long appointmentId);
    @Query("""
            SELECT COALESCE(MAX(c.queueNumber), 0)
            FROM CheckIn c
            WHERE CAST(c.checkInTime AS date) = :today
            """)
    Integer findMaxQueueNumberForToday(@Param("today") LocalDate today);
    @Query("""
            SELECT c FROM CheckIn c
            LEFT JOIN FETCH c.appointments a
            LEFT JOIN FETCH a.patient p
            LEFT JOIN FETCH p.user pu
            LEFT JOIN FETCH a.doctor d
            LEFT JOIN FETCH d.user du
            WHERE CAST(c.checkInTime AS date) = :date
              AND (:status IS NULL OR c.status = :status)
            ORDER BY c.queueNumber ASC
            """)
    List<CheckIn> findQueueByDateAndStatus(
            @Param("date") LocalDate date,
            @Param("status") CheckInStatus status
    );

    @Query("""
            SELECT c FROM CheckIn c
            LEFT JOIN c.appointments a
            LEFT JOIN a.patient p
            LEFT JOIN p.user pu
            LEFT JOIN a.doctor d
            LEFT JOIN d.user du
            WHERE (:status IS NULL OR c.status = :status)
              AND (:fromDate IS NULL OR CAST(c.checkInTime AS date) >= :fromDate)
              AND (:toDate IS NULL OR CAST(c.checkInTime AS date) <= :toDate)
              AND (:doctorId IS NULL OR d.id = :doctorId)
              AND (:search IS NULL OR :search = ''
                   OR LOWER(pu.fullName) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(p.patientCode) LIKE LOWER(CONCAT('%', :search, '%')))
            ORDER BY CAST(c.checkInTime AS date) DESC, c.queueNumber ASC
            """)
    Page<CheckIn> findWithFilters(
            @Param("status") CheckInStatus status,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("doctorId") Long doctorId,
            @Param("search") String search,
            Pageable pageable
    );

    @Query("""
            SELECT COUNT(c) FROM CheckIn c
            WHERE CAST(c.checkInTime AS date) = :date
              AND c.status = :status
            """)
    Long countByDateAndStatus(
            @Param("date") LocalDate date,
            @Param("status") CheckInStatus status
    );
}
