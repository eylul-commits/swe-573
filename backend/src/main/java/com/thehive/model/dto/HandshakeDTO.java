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
    private Integer offerId; // null if this is for a request
    private Integer requestId; // null if this is for an offer
    private String offerTitle; // title of the service (either offer or request)
    private AuthorDTO seeker;
    private AuthorDTO provider;
    private HandshakeStatus status;
    private Integer durationHours;
    private Boolean seekerConfirmed;
    private Boolean providerConfirmed;
    private LocalDateTime createdAt;
    private LocalDateTime agreedDate;
    private Boolean canRate; // true if agreedDate has passed and user hasn't rated yet
}

