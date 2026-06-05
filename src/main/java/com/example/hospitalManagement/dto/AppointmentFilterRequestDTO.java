package com.example.hospitalManagement.dto;

import com.example.hospitalManagement.entity.Enum.AppointmentStatus;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;






public class AppointmentFilterRequestDTO {

    
    private Long doctorId;

    
    private Long patientId;

    
    private AppointmentStatus status;

    
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fromDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate toDate;

    
    private String search;

    
    private int page = 0;
    private int size = 10;

    

    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }

    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }

    public AppointmentStatus getStatus() { return status; }
    public void setStatus(AppointmentStatus status) { this.status = status; }

    public LocalDate getFromDate() { return fromDate; }
    public void setFromDate(LocalDate fromDate) { this.fromDate = fromDate; }

    public LocalDate getToDate() { return toDate; }
    public void setToDate(LocalDate toDate) { this.toDate = toDate; }

    public String getSearch() { return search; }
    public void setSearch(String search) { this.search = search; }

    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }

    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }
}
