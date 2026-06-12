package com.example.hospitalManagement.dto;

import com.example.hospitalManagement.entity.Enum.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

public class DoctorDTO {
    private Long id;

    @NotNull(message = "Bác sĩ phải gắn với một tài khoản người dùng")
    private Long userId;
    private String userName;
    private String password;

    @NotBlank(message = "Họ tên bác sĩ không được để trống")
    @Size(max = 120, message = "Họ tên bác sĩ không được vượt quá 120 ký tự")
    private String fullName;

    @Email(message = "Email không đúng định dạng")
    @Size(max = 120, message = "Email không được vượt quá 120 ký tự")
    private String email;

    @Pattern(regexp = "^(0|\\+84)[0-9]{9,10}$", message = "Số điện thoại không đúng định dạng Việt Nam")
    private String phone;

    @NotBlank(message = "Số chứng chỉ hành nghề không được để trống")
    @Size(max = 30, message = "Số chứng chỉ hành nghề không được vượt quá 30 ký tự")
    private String licenseNumber;

    @NotNull(message = "Số năm kinh nghiệm không được để trống")
    @Min(value = 0, message = "Số năm kinh nghiệm không được âm")
    @Max(value = 60, message = "Số năm kinh nghiệm không hợp lệ")
    private Integer experienceYears;

    @NotNull(message = "Trạng thái bác sĩ không được để trống")
    private UserStatus status; 

    @NotNull(message = "Khoa/phòng làm việc không được để trống")
    private Long departmentId;

    private String departmentName;
    private String specialization;
    private String workingStatus;
    private long todayScheduleCount;
    private long totalScheduleCount;
    private List<DoctorScheduleDTO> schedules = new ArrayList<>();

    

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public Integer getExperienceYears() {
        return experienceYears;
    }

    public void setExperienceYears(Integer experienceYears) {
        this.experienceYears = experienceYears;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public String getWorkingStatus() {
        return workingStatus;
    }

    public void setWorkingStatus(String workingStatus) {
        this.workingStatus = workingStatus;
    }

    public long getTodayScheduleCount() {
        return todayScheduleCount;
    }

    public void setTodayScheduleCount(long todayScheduleCount) {
        this.todayScheduleCount = todayScheduleCount;
    }

    public long getTotalScheduleCount() {
        return totalScheduleCount;
    }

    public void setTotalScheduleCount(long totalScheduleCount) {
        this.totalScheduleCount = totalScheduleCount;
    }

    public List<DoctorScheduleDTO> getSchedules() {
        return schedules;
    }

    public void setSchedules(List<DoctorScheduleDTO> schedules) {
        this.schedules = schedules;
    }
}
