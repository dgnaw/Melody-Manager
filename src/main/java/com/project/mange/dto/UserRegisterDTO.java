package com.project.mange.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRegisterDTO {
    @NotBlank(message = "Username không được để trống")
    private String username;

    @NotBlank(message = "Password không được để trống")
    @Size(min = 8, message = "Mật khẩu phải từ 8 ký tự trở lên")
    private String password;

    @Email(message = "Email không được để trống")
    private String email;
    private String fullName;
}
