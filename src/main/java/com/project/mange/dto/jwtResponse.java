package com.project.mange.dto;

import lombok.Data;

@Data
public class jwtResponse {
    private String token;
    private String type = "Bearer";
    private Long id;
    private String username;
    private String fullName;
    private String email;
    private String role;
    private String avatar;

    public jwtResponse(String token, Long id, String username, String fullName, String email, String role, String avatar) {
        this.token = token;
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.avatar = avatar;
    }
}
