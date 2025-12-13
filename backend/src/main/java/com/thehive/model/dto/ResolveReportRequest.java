package com.thehive.model.dto;

import com.thehive.model.enums.ReportStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ResolveReportRequest {
    
    @NotNull(message = "Status is required")
    private ReportStatus status;
    
    private String adminNotes;
    
    private Integer userId; // if action taken on user
    private String action; // "WARN", "DEACTIVATE", "NO_ACTION"
}

