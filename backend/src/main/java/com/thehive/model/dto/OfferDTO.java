package com.thehive.model.dto;

import com.thehive.model.enums.ItemStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OfferDTO {
    private Integer id;
    private String title;
    private String description;
    private Integer durationHours;
    private LocalDate startDate;
    private LocalDate endDate;
    private String province;
    private String district;
    private String geohash;
    private ItemStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private AuthorDTO provider;
    private List<String> tags;
}

