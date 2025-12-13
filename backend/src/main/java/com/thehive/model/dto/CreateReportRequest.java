package com.thehive.model.dto;

import com.thehive.model.enums.ReportType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateReportRequest {
    
    @NotNull(message = "Report type is required")
    private ReportType reportType;
    
    @NotNull(message = "Reported user ID is required")
    private Integer reportedUserId;
    
    private Integer reportedOfferId;
    
    private Integer reportedRequestId;
    
    private Integer reportedForumPostId;
    
    private Integer reportedForumTopicId;
    
    @NotBlank(message = "Message is required")
    private String message;
}

