package com.example.hospitalManagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PrescriptionDetailDTO {

    @NotBlank(message = "Tên thuốc không được để trống")
    private String medicineName;

    @NotBlank(message = "Liều lượng không được để trống")
    private String lieuLuong;

    @NotBlank(message = "Tần suất không được để trống")
    private String tanSuat;

    @NotBlank(message = "Liệu trình không được để trống")
    private String lieuTrinh;

    @NotBlank(message = "Hướng dẫn sử dụng không được để trống")
    @Size(max = 500, message = "Hướng dẫn tối đa 500 ký tự")
    private String gioiThieu;
    public String getMedicineName() {
        return medicineName;
    }

    public void setMedicineName(String medicineName) {
        this.medicineName = medicineName;
    }

    public String getLieuLuong() {
        return lieuLuong;
    }

    public void setLieuLuong(String lieuLuong) {
        this.lieuLuong = lieuLuong;
    }

    public String getTanSuat() {
        return tanSuat;
    }

    public void setTanSuat(String tanSuat) {
        this.tanSuat = tanSuat;
    }

    public String getLieuTrinh() {
        return lieuTrinh;
    }

    public void setLieuTrinh(String lieuTrinh) {
        this.lieuTrinh = lieuTrinh;
    }

    public String getGioiThieu() {
        return gioiThieu;
    }

    public void setGioiThieu(String gioiThieu) {
        this.gioiThieu = gioiThieu;
    }
}
