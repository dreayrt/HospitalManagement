package com.example.hospitalManagement.repository;

import com.example.hospitalManagement.entity.WaitingQueue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface WaitingQueueRepository extends JpaRepository<WaitingQueue, Long> {

    @Query("SELECT MAX(w.queueNumber) FROM WaitingQueue w WHERE CAST(w.joinedTime AS data) = :today AND w.doctorId = :doctorId")
    Optional<Integer> findMaxQueueNumberTodayByDoctor(@Param("today") LocalDate today, @Param("doctorId") Long doctorId);

    @Query("SELECT w FROM WaitingQueue w WHERE w.doctorId = :doctorId AND w.status = 'WAITING' ORDER BY w.isPriority DESC, w.joinedTime ASC")
    List<WaitingQueue> findActiveQueueByDoctor(@Param("doctorId") Long doctorId);
}