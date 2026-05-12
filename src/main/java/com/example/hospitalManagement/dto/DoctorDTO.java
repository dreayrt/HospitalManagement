package com.example.hospitalManagement.dto;
import lombok.Data;

@Data
public class DoctorDTO {
    private Long id;
    private String specialization;
    private String licenseNumber;
    private Integer experienceYears;
    private String status;
}