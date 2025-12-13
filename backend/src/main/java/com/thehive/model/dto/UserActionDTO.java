package com.thehive.model.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserActionDTO {
    private Integer id;
    private Integer userId;
    private Integer adminId;
    private String adminName;
    private String adminEmail;
    private String actionType; // WARN, DEACTIVATE, ACTIVATE
    private String reason;
    private Integer reportId;
    private LocalDateTime createdAt;
}

