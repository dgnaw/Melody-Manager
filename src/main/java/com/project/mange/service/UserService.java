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
import java.util.Optional;

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

    public User loginOrRegisterGoogle(UserGGDTO request) {
        // 1. Kiểm tra xem email đã tồn tại chưa
        // (Giả sử bạn có hàm findByEmail trong UserRepo, nếu chưa thì thêm vào UserRepo nhé)
        Optional<User> existingUser = userRepo.findByEmail(request.getEmail());

        if (existingUser.isPresent()) {
            // A. Đã tồn tại -> Trả về thông tin user đó (Login)
            return existingUser.get();
        } else {
            // B. Chưa tồn tại -> Tạo user mới (Register)
            User newUser = new User();
            newUser.setEmail(request.getEmail());
            newUser.setFullName(request.getFullName());
            newUser.setAvatar(request.getAvatar());

            String email = request.getEmail();
            String generatedUsername = email.substring(0, email.indexOf("@"));

            if(userRepo.findByUsername(generatedUsername) != null) {
                generatedUsername = generatedUsername + "_" + System.currentTimeMillis(); // ví dụ: nam.nguyen_170123123
            }

            newUser.setUsername(generatedUsername);
            newUser.setPassword(passwordEncoder.encode("GOOGLE_LOGIN_NP_PASSWORD"));

            User savedUser = userRepo.save(newUser);
            return savedUser;
        }
    }

    public UserResponseDTO registerUser(UserRegisterDTO request){
        if (userRepo.findByUsername(request.getUsername())!= null){
            throw new RuntimeException("Tên đăng nhập đã tồn tại!");
        }
        if (userRepo.findByEmail(request.getEmail()).isPresent()){
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

    public UserResponseDTO updateUser(Long userId, String newUsername, MultipartFile avatarFile) throws IOException {
        User user = userRepo.findById(userId).orElseThrow(
                () -> new RuntimeException("User không tồn tại!"));

        // 1. Cập nhật tên (nếu có)
        if (newUsername != null && !newUsername.trim().isEmpty()) {
            // Loại bỏ khoảng trắng thừa
            String cleanUsername = newUsername.trim();

            // Chỉ xử lý nếu username mới KHÁC username cũ
            if (!cleanUsername.equals(user.getUsername())) {
                // Kiểm tra xem username mới đã có ai dùng chưa
                if (userRepo.findByUsername(cleanUsername) != null) {
                    throw new RuntimeException("Tên đăng nhập (Username) này đã được sử dụng!");
                }
                user.setUsername(cleanUsername);
            }
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
