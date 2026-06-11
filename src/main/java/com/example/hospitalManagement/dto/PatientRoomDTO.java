package com.example.hospitalManagement.dto;

import java.time.LocalDateTime;

public class PatientRoomDTO {
    private long patientId;
    private String fullName;
    private String patientCode;
    private LocalDateTime checkInTime;

    public PatientRoomDTO() {}

    public PatientRoomDTO(long patientId, String fullName, String patientCode, LocalDateTime checkInTime) {
        this.patientId = patientId;
        this.fullName = fullName;
        this.patientCode = patientCode;
        this.checkInTime = checkInTime;
    }

    public long getPatientId() {
        return patientId;
    }

    public void setPatientId(long patientId) {
        this.patientId = patientId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPatientCode() {
        return patientCode;
    }

    public void setPatientCode(String patientCode) {
        this.patientCode = patientCode;
    }

    public LocalDateTime getCheckInTime() {
        return checkInTime;
    }

    public void setCheckInTime(LocalDateTime checkInTime) {
        this.checkInTime = checkInTime;
    }
}
