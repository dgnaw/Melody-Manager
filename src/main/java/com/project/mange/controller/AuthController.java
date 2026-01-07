package com.project.mange.controller;

import com.project.mange.dto.*;
import com.project.mange.model.User;
import com.project.mange.security.CustomUserDetails;
import com.project.mange.security.JwtTokenProvider;
import com.project.mange.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    @Autowired
    private UserService userService;

    @Autowired
    JwtTokenProvider tokenProvider;

    @Autowired
    AuthenticationManager authenticationManager;

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
            // 1. Nho Spring Security kiem tra username/password
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            // 2. Neu dung, luu thong tin vao Context
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 3. Lay thong tin User ra de in token
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            String jwt = tokenProvider.generateToken(userDetails);

            // 4. Tra ve Token + thong tin cua User
            return ResponseEntity.ok(new jwtResponse(
                    jwt,
                    userDetails.getUser().getId(),
                    userDetails.getUser().getUsername(),
                    userDetails.getUser().getFullName(),
                    userDetails.getUser().getEmail(),
                    userDetails.getUser().getRole(),
                    userDetails.getUser().getAvatar()
            ));
        }catch (Exception e){
            return ResponseEntity.status(401).body("Tên đăng nhập hoặc mật khẩu không chính xác!");
        }
    }
    // Trong AuthController.java

    @PostMapping("/google")
    public ResponseEntity<?> googleLogin(@RequestBody UserGGDTO request) {
        try {
            User userModel = userService.loginOrRegisterGoogle(request);

            CustomUserDetails userDetails = new CustomUserDetails(userModel);

            String jwt = tokenProvider.generateToken(userDetails);

            return ResponseEntity.ok(new jwtResponse(
                    jwt,
                    userModel.getId(),
                    userModel.getUsername(),
                    userModel.getFullName(),
                    userModel.getEmail(),
                    userModel.getRole(),
                    userModel.getAvatar()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi Google Login: " + e.getMessage());
        }
    }
}


