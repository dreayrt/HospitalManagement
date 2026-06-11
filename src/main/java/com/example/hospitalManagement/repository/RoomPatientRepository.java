package com.example.hospitalManagement.repository;

import com.example.hospitalManagement.entity.RoomPatient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoomPatientRepository extends JpaRepository<RoomPatient, Long> {
    long countByRoomIdAndCheckOutTimeIsNull(long roomId);
    List<RoomPatient> findByRoomIdAndCheckOutTimeIsNull(long roomId);
    Optional<RoomPatient> findByRoomIdAndPatientIdAndCheckOutTimeIsNull(long roomId, long patientId);
    boolean existsByPatientIdAndCheckOutTimeIsNull(long patientId);
}
