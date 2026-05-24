package com.example.hospitalManagement.dto;

import com.example.hospitalManagement.entity.Enum.UserStatus;

public class DoctorFilterRequest {
    private String keyword;
    private Long departmentId;
    private UserStatus userStatus; // <-- Đổi từ status thành userStatus cho đồng bộ hệ thống
    private Boolean availableToday;

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public UserStatus getUserStatus() {
        return userStatus;
    }

    public void setUserStatus(UserStatus userStatus) {
        this.userStatus = userStatus;
    }

    public Boolean getAvailableToday() {
        return availableToday;
    }

    public void setAvailableToday(Boolean availableToday) {
        this.availableToday = availableToday;
    }
}