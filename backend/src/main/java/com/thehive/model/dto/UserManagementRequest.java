package com.thehive.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserManagementRequest {
    
    @NotNull(message = "User ID is required")
    private Integer userId;
    
    private String action; // "WARN", "DEACTIVATE", "ACTIVATE"
    
    private String reason;
}

