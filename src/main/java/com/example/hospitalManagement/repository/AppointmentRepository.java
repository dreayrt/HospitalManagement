package com.example.hospitalManagement.repository;

import com.example.hospitalManagement.entity.Appointments;
import com.example.hospitalManagement.entity.Enum.AppointmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Repository quản lý Lịch khám
 */
@Repository
public interface AppointmentRepository extends JpaRepository<Appointments, Long> {

    /**
     * Lấy toàn bộ lịch khám của một bác sĩ theo ngày (dùng cho xem lịch làm việc bác sĩ)
     */
    List<Appointments> findByDoctorIdAndAppointmentDate(Long doctorId, LocalDate date);

    /**
     * Kiểm tra trùng lịch: bác sĩ đã có lịch vào ngày + giờ đó chưa?
     */
    boolean existsByDoctorIdAndAppointmentDateAndAppointmentTimeAndStatusNot(
            Long doctorId,
            LocalDate appointmentDate,
            LocalTime appointmentTime,
            AppointmentStatus status
    );

    /**
     * Tìm kiếm lịch khám với filter đa điều kiện + phân trang
     * Hỗ trợ: lọc bác sĩ, bệnh nhân, trạng thái, khoảng ngày, tìm tên/mã bệnh nhân
     */
    @Query("""
            SELECT a FROM Appointments a
            LEFT JOIN a.doctor d
            LEFT JOIN d.user du
            LEFT JOIN a.patient p
            LEFT JOIN p.user pu
            WHERE (:doctorId IS NULL OR d.id = :doctorId)
              AND (:patientId IS NULL OR p.id = :patientId)
              AND (:status IS NULL OR a.status = :status)
              AND (:fromDate IS NULL OR a.appointmentDate >= :fromDate)
              AND (:toDate IS NULL OR a.appointmentDate <= :toDate)
              AND (:search IS NULL OR :search = ''
                   OR LOWER(pu.fullName) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(p.patientCode) LIKE LOWER(CONCAT('%', :search, '%')))
            ORDER BY a.appointmentDate DESC, a.appointmentTime DESC
            """)
    Page<Appointments> findWithFilters(
            @Param("doctorId") Long doctorId,
            @Param("patientId") Long patientId,
            @Param("status") AppointmentStatus status,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("search") String search,
            Pageable pageable
    );

    /**
     * Lấy lịch khám theo bác sĩ (tất cả trạng thái) - dùng cho popup lịch làm việc
     */
    @Query("""
            SELECT a FROM Appointments a
            WHERE a.doctor.id = :doctorId
              AND a.appointmentDate >= :fromDate
              AND a.appointmentDate <= :toDate
            ORDER BY a.appointmentDate ASC, a.appointmentTime ASC
            """)
    List<Appointments> findByDoctorIdAndDateRange(
            @Param("doctorId") Long doctorId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );
}
