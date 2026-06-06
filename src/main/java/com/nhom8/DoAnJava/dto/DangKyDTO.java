package com.nhom8.DoAnJava.dto;

import com.nhom8.DoAnJava.validator.FieldMatch;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
@FieldMatch(first = "password", second = "confirmPassword", message = "Mật khẩu và xác nhận mật khẩu không khớp.")
public class DangKyDTO {
    @NotBlank(message = "Vui lòng nhập Email.")
    @Email(message = "Email không đúng định dạng.")
    private String email;

    @NotBlank(message = "Vui lòng nhập Mật khẩu.")
    @Size(min = 6, max = 100, message = "Mật khẩu phải dài ít nhất 6 ký tự.")
    private String password;

    @NotBlank(message = "Vui lòng nhập lại Mật khẩu.")
    private String confirmPassword;


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
    
}
