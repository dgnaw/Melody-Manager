package com.project.mange.service;

import com.project.mange.dto.UserLoginDTO;
import com.project.mange.dto.UserRegisterDTO;
import com.project.mange.dto.UserResponseDTO;
import com.project.mange.model.User;
import com.project.mange.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private UserRepo userRepo;

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
        newUser.setPassword(request.getPassword());
        newUser.setFullName(request.getFullName());
        newUser.setRole("USER");

        User savedUser = userRepo.save(newUser);

        return convertToDTO(savedUser);
    }

    public UserResponseDTO loginUser(UserLoginDTO request){
        User user = userRepo.findByUsername(request.getUsername());
        if (user != null && user.getPassword().equals(request.getPassword())){
            return convertToDTO(user);
        }
        return null;
    }
}
