package com.thehive.service;

import com.thehive.client.StreamChatClient;
import com.thehive.config.StreamChatConfig;
import com.thehive.model.dto.AuthResponse;
import com.thehive.model.dto.LoginRequest;
import com.thehive.model.dto.RegisterRequest;
import com.thehive.model.dto.UpdateProfileRequest;
import com.thehive.model.dto.UserDTO;
import com.thehive.model.entity.User;
import com.thehive.model.entity.UserAction;
import com.thehive.model.enums.UserRole;
import com.thehive.model.enums.UserStatus;
import com.thehive.repository.TimebankTransactionRepository;
import com.thehive.repository.UserActionRepository;
import com.thehive.repository.UserRepository;
import com.thehive.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private TimebankTransactionRepository timebankTransactionRepository;

    @Mock
    private UserActionRepository userActionRepository;

    @Mock
    private StreamChatConfig streamChatConfig;

    @Mock
    private StreamChatClient streamChatClient;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        // Setup test user
        testUser = new User();
        testUser.setId(1);
        testUser.setEmail("test@example.com");
        testUser.setPasswordHash("$2a$10$hashedPassword");
        testUser.setName("Test User");
        testUser.setBalanceHours(3);
        testUser.setRole(UserRole.USER);

        // Setup register request
        registerRequest = new RegisterRequest();
        registerRequest.setEmail("newuser@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setName("New User");

        // Setup login request
        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");
    }

    //Register Tests
    @Test
    void register_WithNewEmail_ShouldSucceed() {
        // Arrange
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("$2a$10$hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtUtil.generateToken(anyString(), any(Integer.class))).thenReturn("jwt-token");
        when(timebankTransactionRepository.findBySenderId(any(Integer.class))).thenReturn(Collections.emptyList());
        when(timebankTransactionRepository.findByReceiverId(any(Integer.class))).thenReturn(Collections.emptyList());
        when(streamChatConfig.isConfigured()).thenReturn(true);
        when(streamChatConfig.generateUserToken(anyString())).thenReturn("stream-chat-token");
        doNothing().when(streamChatClient).upsertUser(any(Integer.class), anyString());

        // Act
        AuthResponse response = authService.register(registerRequest);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getToken());
        assertEquals("jwt-token", response.getToken());
        assertNotNull(response.getUser());
        assertEquals(testUser.getEmail(), response.getUser().getEmail());
        assertEquals(testUser.getName(), response.getUser().getName());
        assertEquals(3, response.getUser().getBalanceHours());
        assertEquals(0, response.getUser().getHoursGiven());
        assertEquals(0, response.getUser().getHoursReceived());

        // Verify interactions
        verify(userRepository).existsByEmail(registerRequest.getEmail());
        verify(passwordEncoder).encode(registerRequest.getPassword());
        verify(userRepository).save(any(User.class));
        verify(jwtUtil).generateToken(testUser.getEmail(), testUser.getId());
        verify(streamChatClient).upsertUser(testUser.getId(), testUser.getName());
    }

    @Test
    void register_WithExistingEmail_ShouldThrowException() {
        // Arrange
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);

        // Act and Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.register(registerRequest);
        });

        assertEquals("Email already registered", exception.getMessage());

        // Verify that save was never called
        verify(userRepository).existsByEmail(registerRequest.getEmail());
        verify(userRepository, never()).save(any(User.class));
        verify(jwtUtil, never()).generateToken(anyString(), any(Integer.class));
    }

    //Login Tests
    @Test
    void login_WithValidCredentials_ShouldSucceed() {
        // Arrange
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(loginRequest.getPassword(), testUser.getPasswordHash())).thenReturn(true);
        when(jwtUtil.generateToken(testUser.getEmail(), testUser.getId())).thenReturn("jwt-token");
        when(timebankTransactionRepository.findBySenderId(any(Integer.class))).thenReturn(Collections.emptyList());
        when(timebankTransactionRepository.findByReceiverId(any(Integer.class))).thenReturn(Collections.emptyList());
        when(streamChatConfig.isConfigured()).thenReturn(true);
        when(streamChatConfig.generateUserToken(anyString())).thenReturn("stream-chat-token");

        // Act
        AuthResponse response = authService.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getToken());
        assertEquals("jwt-token", response.getToken());
        assertNotNull(response.getUser());
        assertEquals(testUser.getEmail(), response.getUser().getEmail());
        assertEquals(testUser.getName(), response.getUser().getName());
        assertEquals(testUser.getBalanceHours(), response.getUser().getBalanceHours());
        assertEquals(0, response.getUser().getHoursGiven());
        assertEquals(0, response.getUser().getHoursReceived());

        // Verify interactions
        verify(userRepository).findByEmail(loginRequest.getEmail());
        verify(passwordEncoder).matches(loginRequest.getPassword(), testUser.getPasswordHash());
        verify(jwtUtil).generateToken(testUser.getEmail(), testUser.getId());
    }

    @Test
    void login_WithNonExistentEmail_ShouldThrowException() {
        // Arrange
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.login(loginRequest);
        });

        assertEquals("Invalid email or password", exception.getMessage());

        // Verify
        verify(userRepository).findByEmail(loginRequest.getEmail());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtUtil, never()).generateToken(anyString(), any(Integer.class));
    }

    @Test
    void login_WithWrongPassword_ShouldThrowException() {
        // Arrange
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(loginRequest.getPassword(), testUser.getPasswordHash())).thenReturn(false);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.login(loginRequest);
        });

        assertEquals("Invalid email or password", exception.getMessage());

        // Verify
        verify(userRepository).findByEmail(loginRequest.getEmail());
        verify(passwordEncoder).matches(loginRequest.getPassword(), testUser.getPasswordHash());
        verify(jwtUtil, never()).generateToken(anyString(), any(Integer.class));
    }

    @Test
    void login_WithDeactivatedAccount_ShouldThrowException() {
        // Arrange
        testUser.setAccountStatus(UserStatus.DEACTIVATED);
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(loginRequest.getPassword(), testUser.getPasswordHash())).thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.login(loginRequest);
        });

        assertEquals("Account is deactivated", exception.getMessage());

        // Verify
        verify(jwtUtil, never()).generateToken(anyString(), any(Integer.class));
    }

    //Get Current User Tests
    @Test
    void getCurrentUser_WithValidUserId_ShouldSucceed() {
        // Arrange
        Integer userId = 1;
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(timebankTransactionRepository.findBySenderId(userId)).thenReturn(Collections.emptyList());
        when(timebankTransactionRepository.findByReceiverId(userId)).thenReturn(Collections.emptyList());

        // Act
        UserDTO result = authService.getCurrentUser(userId);

        // Assert
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        assertEquals(testUser.getEmail(), result.getEmail());
        assertEquals(testUser.getName(), result.getName());
        assertEquals(testUser.getBio(), result.getBio());
        assertEquals(testUser.getProvince(), result.getProvince());
        assertEquals(testUser.getDistrict(), result.getDistrict());
        assertEquals(testUser.getGeohash(), result.getGeohash());
        assertEquals(testUser.getRole(), result.getRole());
        assertEquals(testUser.getBalanceHours(), result.getBalanceHours());
        assertEquals(0, result.getHoursGiven());
        assertEquals(0, result.getHoursReceived());

        // Verify
        verify(userRepository).findById(userId);
        verify(timebankTransactionRepository).findBySenderId(userId);
        verify(timebankTransactionRepository).findByReceiverId(userId);
    }

    @Test
    void getCurrentUser_WithInvalidUserId_ShouldThrowException() {
        // Arrange
        Integer userId = 999;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.getCurrentUser(userId);
        });

        assertEquals("User not found", exception.getMessage());

        // Verify
        verify(userRepository).findById(userId);
    }

    //Update Profile Tests
    @Test
    void updateProfile_WithValidUserId_ShouldUpdateName() {
        // Arrange
        Integer userId = 1;
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setName("Updated Name");
        
        User updatedUser = new User();
        updatedUser.setId(userId); // Required for convertToDTO
        updatedUser.setName("Updated Name");
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);
        when(timebankTransactionRepository.findBySenderId(userId)).thenReturn(Collections.emptyList());
        when(timebankTransactionRepository.findByReceiverId(userId)).thenReturn(Collections.emptyList());
        doNothing().when(streamChatClient).upsertUser(any(Integer.class), anyString());

        // Act
        UserDTO result = authService.updateProfile(userId, request);

        // Assert
        assertNotNull(result);
        assertEquals("Updated Name", result.getName());
        
        // Verify
        verify(userRepository).findById(userId);
        verify(userRepository).save(any(User.class));
        verify(streamChatClient).upsertUser(userId, "Updated Name");
    }

    @Test
    void updateProfile_WithValidUserId_ShouldUpdateAvatarUrl() {
        // Arrange
        Integer userId = 1;
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setAvatarUrl("https://example.com/avatar.jpg");
        
        User updatedUser = new User();
        updatedUser.setId(userId); // Required for convertToDTO
        updatedUser.setAvatarUrl("https://example.com/avatar.jpg");
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);
        when(timebankTransactionRepository.findBySenderId(userId)).thenReturn(Collections.emptyList());
        when(timebankTransactionRepository.findByReceiverId(userId)).thenReturn(Collections.emptyList());

        // Act
        UserDTO result = authService.updateProfile(userId, request);

        // Assert
        assertNotNull(result);
        assertEquals("https://example.com/avatar.jpg", result.getAvatarUrl());
        
        // Verify
        verify(userRepository).findById(userId);
        verify(userRepository).save(any(User.class));
        verify(streamChatClient, never()).upsertUser(any(Integer.class), anyString());
    }

    @Test
    void updateProfile_WithValidUserId_ShouldUpdateBio() {
        // Arrange
        Integer userId = 1;
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setBio("Updated bio text");
        
        User updatedUser = new User();
        updatedUser.setId(userId); // Required for convertToDTO
        updatedUser.setBio("Updated bio text");
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);
        when(timebankTransactionRepository.findBySenderId(userId)).thenReturn(Collections.emptyList());
        when(timebankTransactionRepository.findByReceiverId(userId)).thenReturn(Collections.emptyList());

        // Act
        UserDTO result = authService.updateProfile(userId, request);

        // Assert
        assertNotNull(result);
        assertEquals("Updated bio text", result.getBio());
        
        // Verify
        verify(userRepository).findById(userId);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateProfile_WithValidUserId_ShouldUpdateLocationFields() {
        // Arrange
        Integer userId = 1;
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setProvince("Ankara");
        request.setDistrict("Çankaya");
        request.setGeohash("sx1y2z3");
        
        User updatedUser = new User();
        updatedUser.setId(userId); // Required for convertToDTO
        updatedUser.setProvince("Ankara");
        updatedUser.setDistrict("Çankaya");
        updatedUser.setGeohash("sx1y2z3");
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);
        when(timebankTransactionRepository.findBySenderId(userId)).thenReturn(Collections.emptyList());
        when(timebankTransactionRepository.findByReceiverId(userId)).thenReturn(Collections.emptyList());

        // Act
        UserDTO result = authService.updateProfile(userId, request);

        // Assert
        assertNotNull(result);
        assertEquals("Ankara", result.getProvince());
        assertEquals("Çankaya", result.getDistrict());
        assertEquals("sx1y2z3", result.getGeohash());
        
        // Verify
        verify(userRepository).findById(userId);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateProfile_WithValidUserId_ShouldUpdateMultipleFields() {
        // Arrange
        Integer userId = 1;
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setName("New Name");
        request.setBio("New Bio");
        request.setAvatarUrl("https://example.com/new-avatar.jpg");
        
        User updatedUser = new User();
        updatedUser.setId(userId); // Required for convertToDTO
        updatedUser.setName("New Name");
        updatedUser.setBio("New Bio");
        updatedUser.setAvatarUrl("https://example.com/new-avatar.jpg");
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);
        when(timebankTransactionRepository.findBySenderId(userId)).thenReturn(Collections.emptyList());
        when(timebankTransactionRepository.findByReceiverId(userId)).thenReturn(Collections.emptyList());
        doNothing().when(streamChatClient).upsertUser(any(Integer.class), anyString());

        // Act
        UserDTO result = authService.updateProfile(userId, request);

        // Assert
        assertNotNull(result);
        assertEquals("New Name", result.getName());
        assertEquals("New Bio", result.getBio());
        assertEquals("https://example.com/new-avatar.jpg", result.getAvatarUrl());
        
        // Verify
        verify(userRepository).findById(userId);
        verify(userRepository).save(any(User.class));
        verify(streamChatClient).upsertUser(userId, "New Name");
    }

    @Test
    void updateProfile_WithInvalidUserId_ShouldThrowException() {
        // Arrange
        Integer userId = 999;
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setName("New Name");
        
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.updateProfile(userId, request);
        });

        assertEquals("User not found", exception.getMessage());

        // Verify
        verify(userRepository).findById(userId);
        verify(userRepository, never()).save(any(User.class));
        verify(streamChatClient, never()).upsertUser(any(Integer.class), anyString());
    }

    @Test
    void updateProfile_WithNullFields_ShouldNotUpdateFields() {
        // Arrange
        Integer userId = 1;
        UpdateProfileRequest request = new UpdateProfileRequest();
        // All fields are null
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(timebankTransactionRepository.findBySenderId(userId)).thenReturn(Collections.emptyList());
        when(timebankTransactionRepository.findByReceiverId(userId)).thenReturn(Collections.emptyList());

        // Act
        UserDTO result = authService.updateProfile(userId, request);

        // Assert
        assertNotNull(result);
        assertEquals(testUser.getName(), result.getName());
        
        // Verify
        verify(userRepository).findById(userId);
        verify(userRepository).save(any(User.class));
        verify(streamChatClient, never()).upsertUser(any(Integer.class), anyString());
    }

    @Test
    void getCurrentUser_WithActiveStatus_ShouldNotCheckWarnings() {
        // Arrange
        Integer userId = 1;
        testUser.setAccountStatus(UserStatus.ACTIVE);
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(timebankTransactionRepository.findBySenderId(userId)).thenReturn(Collections.emptyList());
        when(timebankTransactionRepository.findByReceiverId(userId)).thenReturn(Collections.emptyList());

        // Act
        UserDTO result = authService.getCurrentUser(userId);

        // Assert
        assertEquals(UserStatus.ACTIVE, result.getAccountStatus());
        verify(userActionRepository, never()).findByUserIdOrderByCreatedAtDesc(any());
    }

    @Test
    void getCurrentUser_WithWarnedStatusAndActiveWarnings_ShouldStayWarned() {
        // Arrange
        Integer userId = 1;
        testUser.setAccountStatus(UserStatus.WARNED);
        
        UserAction activeWarning = new UserAction();
        activeWarning.setActionType("WARN");
        activeWarning.setCreatedAt(LocalDateTime.now().minusDays(10)); // Within 30 days
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userActionRepository.findByUserIdOrderByCreatedAtDesc(userId)).thenReturn(List.of(activeWarning));
        when(timebankTransactionRepository.findBySenderId(userId)).thenReturn(Collections.emptyList());
        when(timebankTransactionRepository.findByReceiverId(userId)).thenReturn(Collections.emptyList());

        // Act
        UserDTO result = authService.getCurrentUser(userId);

        // Assert
        assertEquals(UserStatus.WARNED, result.getAccountStatus());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getCurrentUser_WithWarnedStatusAndExpiredWarnings_ShouldResetToActive() {
        // Arrange
        Integer userId = 1;
        testUser.setAccountStatus(UserStatus.WARNED);
        
        UserAction expiredWarning = new UserAction();
        expiredWarning.setActionType("WARN");
        expiredWarning.setCreatedAt(LocalDateTime.now().minusDays(35)); // Older than 30 days
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userActionRepository.findByUserIdOrderByCreatedAtDesc(userId)).thenReturn(List.of(expiredWarning));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            assertEquals(UserStatus.ACTIVE, savedUser.getAccountStatus());
            return savedUser;
        });
        when(timebankTransactionRepository.findBySenderId(userId)).thenReturn(Collections.emptyList());
        when(timebankTransactionRepository.findByReceiverId(userId)).thenReturn(Collections.emptyList());

        // Act
        UserDTO result = authService.getCurrentUser(userId);

        // Assert
        assertEquals(UserStatus.ACTIVE, result.getAccountStatus());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void getCurrentUser_WithWarnedStatusAndMixedWarnings_ShouldStayWarned() {
        // Arrange
        Integer userId = 1;
        testUser.setAccountStatus(UserStatus.WARNED);
        
        UserAction expiredWarning = new UserAction();
        expiredWarning.setActionType("WARN");
        expiredWarning.setCreatedAt(LocalDateTime.now().minusDays(35)); // Older than 30 days
        
        UserAction activeWarning = new UserAction();
        activeWarning.setActionType("WARN");
        activeWarning.setCreatedAt(LocalDateTime.now().minusDays(10)); // Within 30 days
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userActionRepository.findByUserIdOrderByCreatedAtDesc(userId)).thenReturn(List.of(activeWarning, expiredWarning));
        when(timebankTransactionRepository.findBySenderId(userId)).thenReturn(Collections.emptyList());
        when(timebankTransactionRepository.findByReceiverId(userId)).thenReturn(Collections.emptyList());

        // Act
        UserDTO result = authService.getCurrentUser(userId);

        // Assert
        assertEquals(UserStatus.WARNED, result.getAccountStatus());
        verify(userRepository, never()).save(any(User.class));
    }
}