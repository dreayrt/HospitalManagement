package com.example.hospitalManagement.service;

import com.example.hospitalManagement.dto.CheckInDTO;
import com.example.hospitalManagement.entity.CheckIn;
import com.example.hospitalManagement.entity.WaitingQueue;
import com.example.hospitalManagement.repository.CheckInRepository;
import com.example.hospitalManagement.repository.WaitingQueueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CheckInService {

    private final CheckInRepository checkInRepository;
    private final WaitingQueueRepository waitingQueueRepository;

    @Transactional
    public CheckIn processPatientCheckIn(CheckInDTO dto) {
        CheckIn checkIn = CheckIn.builder()
                .appointmentId(dto.getAppointmentId())
                .patientId(dto.getPatientId())
                .checkInTime(LocalDateTime.now())
                .status(CheckIn.CheckInStatus.CHECKED_IN)
                .build();

        CheckIn savedCheckIn = checkInRepository.save(checkIn);

        int nextNumber = waitingQueueRepository.findMaxQueueNumberTodayByDoctor(LocalDate.now(), dto.getDoctorId())
                .orElse(0) + 1;

        WaitingQueue queue = WaitingQueue.builder()
                .patientId(dto.getPatientId())
                .doctorId(dto.getDoctorId())
                .queueNumber(nextNumber)
                .isPriority(dto.getIsPriority())
                .joinedTime(LocalDateTime.now())
                .status(WaitingQueue.QueueStatus.WAITING)
                .build();

        waitingQueueRepository.save(queue);

        return savedCheckIn;
    }
}