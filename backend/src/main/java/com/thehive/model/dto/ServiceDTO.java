package com.thehive.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Unified DTO for both Offers and Requests
 * This is used in the frontend to display services in a uniform way
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceDTO {
    private Integer id;
    private String type; // "OFFER" or "REQUEST"
    private String title;
    private String description;
    private Integer timebank; // duration in hours
    private LocalDate startDate;
    private LocalDate endDate;
    private String location; // combined province + district
    private String province;
    private String district;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private AuthorDTO poster; // provider for offers, seeker for requests
    private List<String> tags;
    private String distance; // optional, for nearby services
}

