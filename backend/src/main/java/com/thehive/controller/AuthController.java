package com.thehive.controller;

import com.thehive.model.dto.AuthResponse;
import com.thehive.model.dto.LoginRequest;
import com.thehive.model.dto.RegisterRequest;
import com.thehive.model.dto.UpdateProfileRequest;
import com.thehive.model.dto.UserDTO;
import com.thehive.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser() {
        Integer userId = (Integer) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserDTO user = authService.getCurrentUser(userId);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/me")
    public ResponseEntity<UserDTO> updateProfile(@RequestBody UpdateProfileRequest request) {
        Integer userId = (Integer) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserDTO user = authService.updateProfile(userId, request);
        return ResponseEntity.ok(user);
    }
}

