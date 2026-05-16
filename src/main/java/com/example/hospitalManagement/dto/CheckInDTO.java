package com.example.hospitalManagement.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CheckInDTO {
    private Long appointmentId;
    private Long patientId;
    private Long doctorId;
    private Boolean isPriority;
}