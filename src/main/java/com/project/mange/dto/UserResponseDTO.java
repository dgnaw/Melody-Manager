package com.project.mange.dto;

import lombok.Data;

@Data
public class UserResponseDTO {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String role;
    private String avatar;
}
