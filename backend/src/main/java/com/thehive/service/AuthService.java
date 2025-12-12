package com.thehive.service;

import com.thehive.client.StreamChatClient;
import com.thehive.config.StreamChatConfig;
import com.thehive.model.dto.AuthResponse;
import com.thehive.model.dto.LoginRequest;
import com.thehive.model.dto.RegisterRequest;
import com.thehive.model.dto.UpdateProfileRequest;
import com.thehive.model.dto.UserDTO;
import com.thehive.model.entity.TimebankTransaction;
import com.thehive.model.entity.User;
import com.thehive.repository.TimebankTransactionRepository;
import com.thehive.repository.UserRepository;
import com.thehive.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final TimebankTransactionRepository timebankTransactionRepository;
    private final StreamChatConfig streamChatConfig;
    private final StreamChatClient streamChatClient;

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

        // Upsert user to Stream Chat (create user in Stream Chat system)
        streamChatClient.upsertUser(user.getId(), user.getName());

        // Generate JWT token
        String token = jwtUtil.generateToken(user.getEmail(), user.getId());

        // Convert to DTO
        UserDTO userDTO = convertToDTO(user);

        // Generate Stream Chat token
        String streamChatToken = generateStreamChatToken(user);

        return new AuthResponse(token, userDTO, streamChatToken);
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

        // Generate Stream Chat token
        String streamChatToken = generateStreamChatToken(user);

        return new AuthResponse(token, userDTO, streamChatToken);
    }

    /*
      Generate Stream Chat token for user // No test added for this method
    */
    private String generateStreamChatToken(User user) {
        try {
            if (streamChatConfig.isConfigured()) {
                String token = streamChatConfig.generateUserToken(user.getId().toString());
                System.out.println("✓ Generated Stream Chat token for user " + user.getId() + ": " + token.substring(0, Math.min(20, token.length())) + "...");
                return token;
            } else {
                System.err.println("✗ Stream Chat not configured, cannot generate token");
            }
        } catch (Exception e) {
            // Log error but don't fail the auth process
            System.err.println("✗ Failed to generate Stream Chat token: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    @Transactional(readOnly = true)
    public UserDTO getCurrentUser(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return convertToDTO(user);
    }

    @Transactional
    public UserDTO updateProfile(Integer userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Update fields if provided
        if (request.getName() != null) {
            user.setName(request.getName());
        }
        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }
        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl());
        }
        if (request.getProvince() != null) {
            user.setProvince(request.getProvince());
        }
        if (request.getDistrict() != null) {
            user.setDistrict(request.getDistrict());
        }
        if (request.getGeohash() != null) {
            user.setGeohash(request.getGeohash());
        }
        
        user = userRepository.save(user);
        
        // Update Stream Chat user if name changed
        if (request.getName() != null) {
            streamChatClient.upsertUser(user.getId(), user.getName());
        }
        
        return convertToDTO(user);
    }

    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setName(user.getName());
        dto.setBio(user.getBio());
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setProvince(user.getProvince());
        dto.setDistrict(user.getDistrict());
        dto.setGeohash(user.getGeohash());
        dto.setRole(user.getRole());
        dto.setAccountStatus(user.getAccountStatus());
        dto.setWarningCount(user.getWarningCount());
        dto.setBalanceHours(user.getBalanceHours());
        
        // Calculate hours given and received from timebank transactions
        List<TimebankTransaction> sentTransactions = timebankTransactionRepository.findBySenderId(user.getId());
        List<TimebankTransaction> receivedTransactions = timebankTransactionRepository.findByReceiverId(user.getId());
        
        int hoursGiven = sentTransactions.stream()
                .mapToInt(t -> t.getAmount() != null ? t.getAmount() : 0)
                .sum();
        
        int hoursReceived = receivedTransactions.stream()
                .mapToInt(t -> t.getAmount() != null ? t.getAmount() : 0)
                .sum();
        
        dto.setHoursGiven(hoursGiven);
        dto.setHoursReceived(hoursReceived);
        
        return dto;
    }
}

