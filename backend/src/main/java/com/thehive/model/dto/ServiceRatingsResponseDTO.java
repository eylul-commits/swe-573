package com.thehive.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceRatingsResponseDTO {
    private List<ServiceRatingDTO> ratings;
    private ServiceRatingSummaryDTO summary;
}


