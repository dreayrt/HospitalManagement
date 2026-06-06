package com.example.hospitalManagement.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;





public class CreateAppointmentRequestDTO {

    @NotNull(message = "ID bệnh nhân không được trống")
    private Long patientId;

    @NotNull(message = "ID bác sĩ không được trống")
    private Long doctorId;

    @NotNull(message = "Ngày khám không được trống")
    @FutureOrPresent(message = "Ngày khám phải là hôm nay hoặc trong tương lai")
    private LocalDate appointmentDate;

    @NotNull(message = "Giờ khám không được trống")
    private LocalTime appointmentTime;

    @Size(max = 500, message = "Lý do không được vượt quá 500 ký tự")
    private String reason;

    
    private Long createdById;

    

    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }

    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }

    public LocalDate getAppointmentDate() { return appointmentDate; }
    public void setAppointmentDate(LocalDate appointmentDate) { this.appointmentDate = appointmentDate; }

    public LocalTime getAppointmentTime() { return appointmentTime; }
    public void setAppointmentTime(LocalTime appointmentTime) { this.appointmentTime = appointmentTime; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public Long getCreatedById() { return createdById; }
    public void setCreatedById(Long createdById) { this.createdById = createdById; }
}
