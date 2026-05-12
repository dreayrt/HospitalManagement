package com.example.hospitalManagement.dto;
import lombok.Data;

@Data
public class DoctorFilterRequest {
    private String specialization;
    private Integer minExperience;
    private String status;
}