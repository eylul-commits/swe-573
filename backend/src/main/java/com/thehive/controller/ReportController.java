package com.thehive.controller;

import com.thehive.model.dto.CreateReportRequest;
import com.thehive.model.dto.ReportDTO;
import com.thehive.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    public ResponseEntity<ReportDTO> createReport(@Valid @RequestBody CreateReportRequest request) {
        Integer reporterId = (Integer) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        ReportDTO report = reportService.createReport(request, reporterId);
        return ResponseEntity.status(HttpStatus.CREATED).body(report);
    }

    @GetMapping("/my-reports")
    public ResponseEntity<List<ReportDTO>> getMyReports() {
        Integer reporterId = (Integer) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<ReportDTO> reports = reportService.getReportsByReporter(reporterId);
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReportDTO> getReportById(@PathVariable Integer id) {
        ReportDTO report = reportService.getReportById(id);
        return ResponseEntity.ok(report);
    }
}

