package com.example.hospitalManagement.dto;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class DoctorScheduleDTO {
    private Long doctorId;
    private LocalDate workDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String status;
}