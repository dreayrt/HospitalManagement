package com.example.hospitalManagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentFilterRequest {
    
    private Long patientId;
    
    private Long doctorId;
    
    private String status;  // Filter by status
    
    private LocalDateTime fromDate;
    
    private LocalDateTime toDate;
    
    @Builder.Default
    private Integer page = 0;
    
    @Builder.Default
    private Integer size = 20;
    
    @Builder.Default
    private String sortBy = "appointmentDate";
    
    @Builder.Default
    private String sortOrder = "DESC";  // ASC or DESC
}

