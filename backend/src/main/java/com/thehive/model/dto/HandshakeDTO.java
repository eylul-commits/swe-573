package com.thehive.model.dto;

import com.thehive.model.enums.HandshakeStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HandshakeDTO {
    private Integer id;
    private Integer offerId;
    private String offerTitle;
    private AuthorDTO seeker;
    private AuthorDTO provider;
    private HandshakeStatus status;
    private Integer agreedHours;
    private Boolean seekerConfirmed;
    private Boolean providerConfirmed;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private Boolean canRate; // true if completedAt has passed and user hasn't rated yet
}

