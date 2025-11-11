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
    
    @NotNull(message = "Offer ID is required")
    private Integer offerId;
    
    @NotNull(message = "Provider ID is required")
    private Integer providerId;
    
    @Positive(message = "Agreed hours must be positive")
    private Integer agreedHours;
}

