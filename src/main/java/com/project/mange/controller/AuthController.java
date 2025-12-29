package com.project.mange.controller;

import com.project.mange.dto.UserLoginDTO;
import com.project.mange.dto.UserRegisterDTO;
import com.project.mange.dto.UserResponseDTO;
import com.project.mange.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserRegisterDTO request) {
        try{
            UserResponseDTO newUser = userService.registerUser(request);
            return ResponseEntity.ok(newUser);
        }catch (RuntimeException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserLoginDTO request) {
        try{
            UserResponseDTO user = userService.loginUser(request);
            if (user == null) {
                return ResponseEntity.status(401).body("Sai tên đăng nhập hoặc mật khẩu!");
            }
            return ResponseEntity.ok(user);
        }catch (RuntimeException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    // Trong AuthController.java

    @PostMapping("/google")
    public ResponseEntity<?> googleLogin(@RequestBody com.project.mange.dto.UserGGDTO request) {
        try {
            UserResponseDTO user = userService.loginOrRegisterGoogle(request);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi Google Login: " + e.getMessage());
        }
    }
}


