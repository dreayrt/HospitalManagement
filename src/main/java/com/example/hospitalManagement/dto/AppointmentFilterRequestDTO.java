package com.example.hospitalManagement.dto;

import com.example.hospitalManagement.entity.Enum.AppointmentStatus;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * Bộ lọc tìm kiếm lịch khám
 * Dùng trong API GET /api/appointments với query params
 * Ví dụ: GET /api/appointments?doctorId=1&status=PENDING&fromDate=2024-05-01&page=0&size=10
 */
public class AppointmentFilterRequestDTO {

    // Lọc theo bác sĩ
    private Long doctorId;

    // Lọc theo bệnh nhân
    private Long patientId;

    // Lọc theo trạng thái (PENDING, CONFIRMED, CANCELLED, COMPLETED, RESCHEDULED)
    private AppointmentStatus status;

    // Lọc theo khoảng ngày
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fromDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate toDate;

    // Tìm kiếm theo tên bệnh nhân hoặc mã bệnh nhân
    private String search;

    // Phân trang
    private int page = 0;
    private int size = 10;

    // ===================== GETTERS & SETTERS =====================

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
