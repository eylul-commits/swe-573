package com.thehive.model.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateRatingRequest {
    
    @NotNull(message = "Handshake ID is required")
    private Integer handshakeId;
    
    @NotNull(message = "Ratee ID is required")
    private Integer rateeId;
    
    @Min(value = 1, message = "Punctuality rating must be between 1 and 5")
    @Max(value = 5, message = "Punctuality rating must be between 1 and 5")
    private Integer punctuality;
    
    @Min(value = 1, message = "Friendliness rating must be between 1 and 5")
    @Max(value = 5, message = "Friendliness rating must be between 1 and 5")
    private Integer friendliness;
    
    @Min(value = 1, message = "Communicative rating must be between 1 and 5")
    @Max(value = 5, message = "Communicative rating must be between 1 and 5")
    private Integer communicative;
    
    @Min(value = 1, message = "Preparedness rating must be between 1 and 5")
    @Max(value = 5, message = "Preparedness rating must be between 1 and 5")
    private Integer preparedness;
    
    private String comment;
}

