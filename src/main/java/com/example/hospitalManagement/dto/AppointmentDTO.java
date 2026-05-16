package com.example.hospitalManagement.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class AppointmentDTO {
    private Long id;
    private Long patientId;
    private Long doctorId;
    private Long scheduleId;
    private LocalDate appointmentDate;
    private LocalTime appointmentTime;
    private String status;
}