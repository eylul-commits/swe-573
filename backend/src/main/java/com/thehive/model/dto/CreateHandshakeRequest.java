package com.thehive.model.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateHandshakeRequest {
    
    // Either offerId or requestId must be provided (but not both)
    private Integer offerId;
    
    private Integer requestId;
    
    @NotNull(message = "Provider ID is required")
    private Integer providerId;
    
    @Positive(message = "Duration hours must be positive")
    private Integer durationHours;
}

