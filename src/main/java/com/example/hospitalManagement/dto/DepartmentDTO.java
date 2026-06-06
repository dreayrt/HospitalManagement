package com.example.hospitalManagement.dto;

import jakarta.validation.constraints.NotBlank;

public class DepartmentDTO {
    @NotBlank(message = "Bạn đang bỏ trống tên khoa")
    private String departmentName;
    @NotBlank(message = "Bạn đang bỏ trống phân mô tả này")
    private String departmentDescription;
    private String departmentStatus;


    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public String getDepartmentDescription() {
        return departmentDescription;
    }

    public void setDepartmentDescription(String departmentDescription) {
        this.departmentDescription = departmentDescription;
    }

    public String getDepartmentStatus() {
        return departmentStatus;
    }

    public void setDepartmentStatus(String departmentStatus) {
        this.departmentStatus = departmentStatus;
    }
}
