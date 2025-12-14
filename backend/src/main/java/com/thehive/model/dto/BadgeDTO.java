package com.thehive.model.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BadgeDTO {
    private Integer id;
    private String name;
    private String description;
    private String iconUrl;
    private LocalDateTime earnedAt;
}

