package com.thehive.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceRatingSummaryDTO {
    private double punctuality;
    private double friendliness;
    private double communicative;
    private double preparedness;
    private int totalReviews;
}


