package com.thehive.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtUtilTest {

    private JwtUtil jwtUtil;
    private static final String TEST_SECRET = "test-secret-key-for-jwt-testing-purposes-only-must-be-long-enough";
    private static final Long TEST_EXPIRATION = 86400000L; // 24 hours

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtUtil, "expiration", TEST_EXPIRATION);
    }

    @Test
    void generateToken_WithValidInput_ShouldGenerateToken() {
        // Arrange
        String email = "test@example.com";
        Integer userId = 123;

        // Act
        String token = jwtUtil.generateToken(email, userId);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3); // JWT has 3 parts separated by dots
    }

    @Test
    void extractEmail_WithValidToken_ShouldExtractEmail() {
        // Arrange
        String email = "test@example.com";
        Integer userId = 123;
        String token = jwtUtil.generateToken(email, userId);

        // Act
        String extractedEmail = jwtUtil.extractEmail(token);

        // Assert
        assertEquals(email, extractedEmail);
    }

    @Test
    void extractEmail_WithInvalidToken_ShouldThrowException() {
        // Arrange
        String invalidToken = "invalid.token.here";

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            jwtUtil.extractEmail(invalidToken);
        });
        assertTrue(exception.getMessage().contains("Failed to extract email from token"));
        assertNotNull(exception.getCause());
        assertTrue(exception.getCause() instanceof JwtException);
    }

    @Test
    void extractUserId_WithValidToken_ShouldExtractUserId() {
        // Arrange
        String email = "test@example.com";
        Integer userId = 456;
        String token = jwtUtil.generateToken(email, userId);

        // Act
        Integer extractedUserId = jwtUtil.extractUserId(token);

        // Assert
        assertEquals(userId, extractedUserId);
    }

    @Test
    void extractUserId_WithInvalidToken_ShouldThrowException() {
        // Arrange
        String invalidToken = "invalid.token.here";

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            jwtUtil.extractUserId(invalidToken);
        });
        assertTrue(exception.getMessage().contains("Failed to extract user ID from token"));
        assertNotNull(exception.getCause());
        assertTrue(exception.getCause() instanceof JwtException);
    }

    @Test
    void extractExpiration_WithValidToken_ShouldExtractExpiration() {
        // Arrange
        String email = "test@example.com";
        Integer userId = 123;
        String token = jwtUtil.generateToken(email, userId);

        // Act
        Date expiration = jwtUtil.extractExpiration(token);

        // Assert
        assertNotNull(expiration);
        assertTrue(expiration.after(new Date())); // Should be in the future
    }

    @Test
    void extractExpiration_WithInvalidToken_ShouldThrowException() {
        // Arrange
        String invalidToken = "invalid.token.here";

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            jwtUtil.extractExpiration(invalidToken);
        });
        assertTrue(exception.getMessage().contains("Failed to extract expiration from token"));
        assertNotNull(exception.getCause());
        assertTrue(exception.getCause() instanceof JwtException);
    }

    @Test
    void validateToken_WithValidToken_ShouldReturnTrue() {
        // Arrange
        String email = "test@example.com";
        Integer userId = 123;
        String token = jwtUtil.generateToken(email, userId);

        // Act
        Boolean isValid = jwtUtil.validateToken(token);

        // Assert
        assertTrue(isValid);
    }

    @Test
    void validateToken_WithNullToken_ShouldReturnFalse() {
        // Act
        Boolean isValid = jwtUtil.validateToken(null);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void validateToken_WithEmptyToken_ShouldReturnFalse() {
        // Act
        Boolean isValid = jwtUtil.validateToken("");

        // Assert
        assertFalse(isValid);
    }

    @Test
    void validateToken_WithWhitespaceToken_ShouldReturnFalse() {
        // Act
        Boolean isValid = jwtUtil.validateToken("   ");

        // Assert
        assertFalse(isValid);
    }

    @Test
    void validateToken_WithInvalidToken_ShouldReturnFalse() {
        // Arrange
        String invalidToken = "invalid.token.here";

        // Act
        Boolean isValid = jwtUtil.validateToken(invalidToken);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void validateToken_WithExpiredToken_ShouldReturnFalse() throws InterruptedException {
        // Arrange - create a JwtUtil with very short expiration
        JwtUtil shortExpirationJwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(shortExpirationJwtUtil, "secret", TEST_SECRET);
        ReflectionTestUtils.setField(shortExpirationJwtUtil, "expiration", 100L); // 100ms expiration

        String email = "test@example.com";
        Integer userId = 123;
        String token = shortExpirationJwtUtil.generateToken(email, userId);

        // Wait for token to expire
        Thread.sleep(200);

        // Act
        Boolean isValid = jwtUtil.validateToken(token);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void validateToken_WithTokenSignedWithDifferentKey_ShouldReturnFalse() {
        // Arrange - create token with different secret
        JwtUtil differentSecretJwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(differentSecretJwtUtil, "secret", "different-secret-key-for-testing-purposes-only");
        ReflectionTestUtils.setField(differentSecretJwtUtil, "expiration", TEST_EXPIRATION);

        String email = "test@example.com";
        Integer userId = 123;
        String token = differentSecretJwtUtil.generateToken(email, userId);

        // Act - validate with original JwtUtil
        Boolean isValid = jwtUtil.validateToken(token);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void extractClaim_WithValidToken_ShouldExtractCustomClaim() {
        // Arrange
        String email = "test@example.com";
        Integer userId = 789;
        String token = jwtUtil.generateToken(email, userId);

        // Act
        //using lamda identity function to test 
        Claims claims = jwtUtil.extractClaim(token, claims1 -> claims1);

        // Assert
        assertNotNull(claims);
        assertEquals(email, claims.getSubject());
        assertEquals(userId, claims.get("userId", Integer.class));
    }

    @Test
    void generateToken_WithDifferentUsers_ShouldGenerateDifferentTokens() {
        // Arrange
        String email1 = "user1@example.com";
        Integer userId1 = 1;
        String email2 = "user2@example.com";
        Integer userId2 = 2;

        // Act
        String token1 = jwtUtil.generateToken(email1, userId1);
        String token2 = jwtUtil.generateToken(email2, userId2);

        // Assert
        assertNotEquals(token1, token2);
    }

    @Test
    void generateToken_WithSameInput_ShouldGenerateDifferentTokens() {
        // Arrange
        String email = "test@example.com";
        Integer userId = 123;

        // Act
        String token1 = jwtUtil.generateToken(email, userId);
        try {
            Thread.sleep(2000);
        } catch (Exception e) {
            System.err.println("Error sleeping: " + e.getMessage());
        }
        String token2 = jwtUtil.generateToken(email, userId);

        // Assert
        assertNotEquals(token1, token2); // Different issuedAt timestamps
    }

    @Test
    void extractEmail_WithTokenContainingSpecialCharacters_ShouldExtractCorrectly() {
        // Arrange
        String email = "test+user@example-domain.com";
        Integer userId = 123;
        String token = jwtUtil.generateToken(email, userId);

        // Act
        String extractedEmail = jwtUtil.extractEmail(token);

        // Assert
        assertEquals(email, extractedEmail);
    }

    @Test
    void validateToken_WithValidToken_ShouldHaveCorrectExpiration() {
        // Arrange
        String email = "test@example.com";
        Integer userId = 123;
        String token = jwtUtil.generateToken(email, userId);

        // Act
        Date expiration = jwtUtil.extractExpiration(token);
        Date now = new Date();
        long timeDifference = expiration.getTime() - now.getTime();

        // Assert
        assertTrue(timeDifference > 0);
        //5 second tolerance
        assertTrue(Math.abs(timeDifference - TEST_EXPIRATION) < 5000);
    }
}

