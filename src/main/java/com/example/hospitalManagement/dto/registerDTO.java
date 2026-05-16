package com.example.hospitalManagement.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class registerDTO {
    @NotBlank(message = "Bạn quên điền tên nè")
    private String fullName;
    @NotBlank(message = "Bạn quên điền username")
    private String userName;
    @NotBlank(message = "Bạn quên điền số điện thoại nè")
    @Pattern(
            regexp = "^\\d{10}$",
            message = "Số điện thoại phải gồm 10 chữ số"
    )
    private String phone;
    @NotBlank(message = "Bạn quên điền email")
    @Email(message = "Email không hợp lệ")
    private String email;
    @NotBlank(message = "Bạn quên điền mật khẩu")
    private String password;
    @NotBlank(message = "Chưa xác nhận mật khẩu")
    private String confirmPassword;
    @AssertTrue(message = "Bạn phải đồng ý điều khoản")
    private boolean clause;

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public boolean isClause() {
        return clause;
    }

    public void setClause(boolean clause) {
        this.clause = clause;
    }
}
