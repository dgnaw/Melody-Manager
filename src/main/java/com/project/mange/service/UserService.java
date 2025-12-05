package com.project.mange.service;

import com.project.mange.dto.UserLoginDTO;
import com.project.mange.dto.UserRegisterDTO;
import com.project.mange.dto.UserResponseDTO;
import com.project.mange.model.User;
import com.project.mange.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
        return dto;
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
}
