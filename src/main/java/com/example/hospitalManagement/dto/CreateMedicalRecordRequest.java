package com.example.hospitalManagement.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public class CreateMedicalRecordRequest {

    private Long appointmentId;

    @NotBlank(message = "Không chuẩn đoán?")
    @Size(max = 500, message = "Chuẩn đoán tối đa 500 ký tự")
    private String chuanDoan;
    @NotBlank(message = "Không có triệu chứng gì thì cho người ta về đi")
    @Size(max = 1000, message = "Triệu chứng tối đa 1000 ký tự")
    private String trieuChung;
    @NotBlank(message = "Anh gì ơi anh quên ghi liệu trình nè")
    private String lieuTrinh;
    @NotBlank(message = "Có tâm thì ghi chú nữa chứ")
    private String ghiChu;

    @Valid
    @NotNull(message = "Danh sách thuốc không được để trống")
    @Size(min = 1,message = "Phải ít nhất 1 loại thuốc")
    private List<PrescriptionDetailDTO> prescriptionDetailDTO;

    public Long getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(Long appointmentId) {
        this.appointmentId = appointmentId;
    }

    public String getChuanDoan() {
        return chuanDoan;
    }

    public void setChuanDoan(String chuanDoan) {
        this.chuanDoan = chuanDoan;
    }

    public String getTrieuChung() {
        return trieuChung;
    }

    public void setTrieuChung(String trieuChung) {
        this.trieuChung = trieuChung;
    }

    public String getLieuTrinh() {
        return lieuTrinh;
    }

    public void setLieuTrinh(String lieuTrinh) {
        this.lieuTrinh = lieuTrinh;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    public List<PrescriptionDetailDTO> getPrescriptionDetailDTO() {
        return prescriptionDetailDTO;
    }

    public void setPrescriptionDetailDTO(List<PrescriptionDetailDTO> prescriptionDetailDTO) {
        this.prescriptionDetailDTO = prescriptionDetailDTO;
    }
}
