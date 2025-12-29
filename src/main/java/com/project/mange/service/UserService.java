package com.project.mange.service;

import com.project.mange.dto.*;
import com.project.mange.model.User;
import com.project.mange.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Service
public class UserService {
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public UserResponseDTO convertToDTO(User user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setRole(user.getRole());
        dto.setAvatar(user.getAvatar());
        return dto;
    }

    public UserResponseDTO loginOrRegisterGoogle(UserGGDTO request) {
        // 1. Kiểm tra xem email đã tồn tại chưa
        // (Giả sử bạn có hàm findByEmail trong UserRepo, nếu chưa thì thêm vào UserRepo nhé)
        User existingUser = userRepo.findByEmail(request.getEmail());

        if (existingUser != null) {
            // A. Đã tồn tại -> Trả về thông tin user đó (Login)
            return convertToDTO(existingUser);
        } else {
            // B. Chưa tồn tại -> Tạo user mới (Register)
            User newUser = new User();
            newUser.setEmail(request.getEmail());
            newUser.setFullName(request.getFullName());
            newUser.setAvatar(request.getAvatar());

            // Username lấy luôn là email (hoặc cắt phần trước @)
            newUser.setUsername(request.getEmail());

            // Password để ngẫu nhiên hoặc trống (vì dùng Google login không cần pass)
            newUser.setPassword("");

            User savedUser = userRepo.save(newUser);
            return convertToDTO(savedUser);
        }
    }

    public UserResponseDTO registerUser(UserRegisterDTO request){
        if (userRepo.findByUsername(request.getUsername())!= null){
            throw new RuntimeException("Tên đăng nhập đã tồn tại!");
        }
        if (userRepo.findByEmail(request.getEmail()) != null){
            throw new RuntimeException("Email đã sử dụng!");
        }

        User newUser = new User();
        newUser.setUsername(request.getUsername());
        newUser.setEmail(request.getEmail());
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        newUser.setFullName(request.getFullName());
        if (request.getRole() != null && !request.getRole().isEmpty()) {
            newUser.setRole(request.getRole());
        } else {
            newUser.setRole("USER");
        }

        User savedUser = userRepo.save(newUser);

        return convertToDTO(savedUser);
    }

    public UserResponseDTO loginUser(UserLoginDTO request){
        User user = userRepo.findByUsername(request.getUsername());
        if (user != null && passwordEncoder.matches(request.getPassword(), user.getPassword())){
            return convertToDTO(user);
        }
        return null;
    }

    public UserResponseDTO updateUser(Long userId, String fullName, MultipartFile avatarFile) throws IOException {
        User user = userRepo.findById(userId).orElseThrow(
                () -> new RuntimeException("User không tồn tại!"));

        // 1. Cập nhật tên (nếu có)
        if (fullName != null && !fullName.trim().isEmpty()) {
            user.setFullName(fullName.trim());
        }

        // 2. Cập nhật avatar (nếu có gửi file)
        if (avatarFile != null && !avatarFile.isEmpty()) {
            // Tao ten file duy nhat
            String fileName = System.currentTimeMillis() + "_" + avatarFile.getOriginalFilename();

            // Luu file vao folder
            Path uploadPath = Paths.get("uploads/avatars");
            if (!Files.exists(uploadPath)){
                Files.createDirectory(uploadPath);
            }

            Path filePath = uploadPath.resolve(fileName);
            Files.copy(avatarFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            String avatarUrl = "/files/avatars/" + fileName;
            user.setAvatar(avatarUrl);
        }

        User savedUser = userRepo.save(user);
        return convertToDTO(savedUser);
    }
}
