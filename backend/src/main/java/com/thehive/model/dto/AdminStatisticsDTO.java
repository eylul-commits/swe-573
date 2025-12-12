package com.thehive.model.dto;

import lombok.Data;

@Data
public class AdminStatisticsDTO {
    private Long totalUsers;
    private Long activeUsers;
    private Long deactivatedUsers;
    private Long totalOffers;
    private Long activeOffers;
    private Long totalRequests;
    private Long activeRequests;
    private Long totalReports;
    private Long openReports;
    private Long inReviewReports;
    private Long resolvedReports;
    private Long totalHandshakes;
    private Long totalMessages;
}

