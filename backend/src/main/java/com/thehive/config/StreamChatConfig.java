package com.thehive.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Configuration
public class StreamChatConfig {

    @Value("${stream.chat.secret:}")
    private String streamChatSecret;

    /**
     * Generate Stream Chat token for a user
     * 
     * @param userId The user ID
     * @return JWT token for Stream Chat
     */
    public String generateUserToken(String userId) {
        if (streamChatSecret == null || streamChatSecret.isEmpty()) {
            throw new IllegalStateException("Stream Chat secret not configured");
        }

        try {
            // Create header
            String header = Base64.getUrlEncoder().withoutPadding()
                    .encodeToString("{\"alg\":\"HS256\",\"typ\":\"JWT\"}".getBytes(StandardCharsets.UTF_8));

            // Create payload
            String payload = String.format("{\"user_id\":\"%s\"}", userId);
            String encodedPayload = Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(payload.getBytes(StandardCharsets.UTF_8));

            // Create signature
            String dataToSign = header + "." + encodedPayload;
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(
                    streamChatSecret.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            );
            mac.init(secretKey);
            byte[] signature = mac.doFinal(dataToSign.getBytes(StandardCharsets.UTF_8));
            String encodedSignature = Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(signature);

            // Return complete token
            return header + "." + encodedPayload + "." + encodedSignature;
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate Stream Chat token", e);
        }
    }

    /**
     * Check if Stream Chat is configured
     */
    public boolean isConfigured() {
        return streamChatSecret != null && !streamChatSecret.isEmpty();
    }
}

