package com.thehive.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceRatingDTO {
    private Integer id;
    private AuthorDTO rater;
    private Integer punctuality;
    private Integer friendliness;
    private Integer communicative;
    private Integer preparedness;
    private String comment;
    private LocalDateTime createdAt;
    private Integer serviceId;
    private String serviceTitle;
}


