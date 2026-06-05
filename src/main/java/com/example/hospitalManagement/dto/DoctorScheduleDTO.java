package com.example.hospitalManagement.dto;

import com.example.hospitalManagement.entity.Enum.DoctorSchedulesStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public class DoctorScheduleDTO {
    private Long id;
    private Long doctorId;

    @NotNull(message = "Ngày làm việc không được để trống")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate workDate;

    @NotNull(message = "Giờ bắt đầu không được để trống")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime startTime;

    @NotNull(message = "Giờ kết thúc không được để trống")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime endTime;

    @NotNull(message = "Trạng thái lịch làm việc không được để trống")
    private DoctorSchedulesStatus status;

    private String doctorName;

    

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }

    public LocalDate getWorkDate() {
        return workDate;
    }

    public void setWorkDate(LocalDate workDate) {
        this.workDate = workDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public DoctorSchedulesStatus getStatus() {
        return status;
    }

    public void setStatus(DoctorSchedulesStatus status) {
        this.status = status;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }
}