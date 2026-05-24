package com.example.hospitalManagement.dto;

import com.example.hospitalManagement.entity.Enum.AppointmentStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * DTO trả về thông tin lịch khám cho frontend
 * Dùng trong API GET /api/appointments và GET /api/appointments/{id}
 */
public class AppointmentDTO {

    private Long id;

    // Thông tin bệnh nhân
    private Long patientId;
    private String patientName;
    private String patientCode;
    private String patientPhone;

    // Thông tin bác sĩ
    private Long doctorId;
    private String doctorName;
    private String doctorLicenseNumber;
    private String departmentName;

    // Thông tin lịch khám
    private LocalDate appointmentDate;
    private LocalTime appointmentTime;
    private String reason;
    private AppointmentStatus status;

    // Người tạo và thời gian tạo
    private String createdByName;
    private LocalDateTime createdAt;

    // ===================== GETTERS & SETTERS =====================

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getPatientCode() { return patientCode; }
    public void setPatientCode(String patientCode) { this.patientCode = patientCode; }

    public String getPatientPhone() { return patientPhone; }
    public void setPatientPhone(String patientPhone) { this.patientPhone = patientPhone; }

    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }

    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }

    public String getDoctorLicenseNumber() { return doctorLicenseNumber; }
    public void setDoctorLicenseNumber(String doctorLicenseNumber) { this.doctorLicenseNumber = doctorLicenseNumber; }

    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }

    public LocalDate getAppointmentDate() { return appointmentDate; }
    public void setAppointmentDate(LocalDate appointmentDate) { this.appointmentDate = appointmentDate; }

    public LocalTime getAppointmentTime() { return appointmentTime; }
    public void setAppointmentTime(LocalTime appointmentTime) { this.appointmentTime = appointmentTime; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public AppointmentStatus getStatus() { return status; }
    public void setStatus(AppointmentStatus status) { this.status = status; }

    public String getCreatedByName() { return createdByName; }
    public void setCreatedByName(String createdByName) { this.createdByName = createdByName; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
