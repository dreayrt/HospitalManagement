package com.example.hospitalManagement.repository;

import com.example.hospitalManagement.entity.DoctorSchedules;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface DoctorScheduleRepository extends JpaRepository<DoctorSchedules, Long> {

    
    List<DoctorSchedules> findByDoctorIdOrderByWorkDateAscStartTimeAsc(Long doctorId);

    
    @Query("""
            select case when count(ds) > 0 then true else false end
            from DoctorSchedules ds
            where ds.doctor.id = :doctorId
              and ds.workDate = :workDate
              and ds.startTime < :endTime
              and ds.endTime > :startTime
              and (:excludeId is null or ds.id <> :excludeId)
            """)
    boolean existsOverlappingSchedule(
            @Param("doctorId") Long doctorId,
            @Param("workDate") LocalDate workDate,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("excludeId") Long excludeId
    );
}