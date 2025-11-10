package com.thehive.service;

import com.thehive.model.dto.AuthResponse;
import com.thehive.model.dto.LoginRequest;
import com.thehive.model.dto.RegisterRequest;
import com.thehive.model.dto.UserDTO;
import com.thehive.model.entity.User;
import com.thehive.repository.UserRepository;
import com.thehive.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Check if user already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        // Create new user
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setName(request.getName());
        user.setBalanceHours(3); // Default starting balance

        user = userRepository.save(user);

        // Generate JWT token
        String token = jwtUtil.generateToken(user.getEmail(), user.getId());

        // Convert to DTO
        UserDTO userDTO = convertToDTO(user);

        return new AuthResponse(token, userDTO);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        // Find user by email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        // Check password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid email or password");
        }

        // Generate JWT token
        String token = jwtUtil.generateToken(user.getEmail(), user.getId());

        // Convert to DTO
        UserDTO userDTO = convertToDTO(user);

        return new AuthResponse(token, userDTO);
    }

    @Transactional(readOnly = true)
    public UserDTO getCurrentUser(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return convertToDTO(user);
    }

    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setName(user.getName());
        dto.setBio(user.getBio());
        dto.setProvince(user.getProvince());
        dto.setDistrict(user.getDistrict());
        dto.setGeohash(user.getGeohash());
        dto.setRole(user.getRole());
        dto.setBalanceHours(user.getBalanceHours());
        return dto;
    }
}

