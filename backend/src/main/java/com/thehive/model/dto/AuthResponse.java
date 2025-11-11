package com.thehive.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private UserDTO user;
    private String streamChatToken;
    
    // Constructor without streamChatToken for backward compatibility
    public AuthResponse(String token, UserDTO user) {
        this.token = token;
        this.user = user;
        this.streamChatToken = null;
    }
}

