package com.project.mange.controller;

import com.project.mange.dto.UserResponseDTO;
import com.project.mange.dto.UserUpdateDTO;
import com.project.mange.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserService userService;

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(
            @PathVariable Long id,
            @RequestParam(value = "fullName", required = false) String fullName,
            @RequestParam(value = "avatar", required = false) MultipartFile avatar) {
        try {
            UserResponseDTO updatedUser = userService.updateUser(id, fullName, avatar);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi Server: " + e.getMessage());
        }
    }
}
