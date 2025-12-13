package com.thehive.model.dto;

import com.thehive.model.enums.ReportStatus;
import com.thehive.model.enums.ReportType;
import com.thehive.model.enums.UserRole;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReportDTO {
    private Integer id;
    private Integer reporterId;
    private String reporterName;
    private String reporterEmail;
    private Integer reportedUserId;
    private String reportedUserName;
    private String reportedUserEmail;
    private UserRole reportedUserRole;
    private ReportType reportType;
    private Integer reportedOfferId;
    private String reportedOfferTitle;
    private Integer reportedRequestId;
    private String reportedRequestTitle;
    private Integer reportedForumPostId;
    private Integer reportedForumTopicId;
    private String message;
    private String adminNotes;
    private ReportStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
    private Integer resolvedById;
    private String resolvedByName;
}

