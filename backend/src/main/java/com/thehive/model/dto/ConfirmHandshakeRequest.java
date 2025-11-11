package com.thehive.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmHandshakeRequest {
    
    @NotNull(message = "Completion date is required")
    private LocalDateTime completedAt;
}

