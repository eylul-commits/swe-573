package com.thehive.controller;

import com.thehive.model.dto.AdminStatisticsDTO;
import com.thehive.model.dto.ReportDTO;
import com.thehive.model.dto.ResolveReportRequest;
import com.thehive.model.dto.UserDTO;
import com.thehive.model.dto.UserManagementRequest;
import com.thehive.model.enums.ReportStatus;
import com.thehive.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/statistics")
    public ResponseEntity<AdminStatisticsDTO> getStatistics() {
        Integer adminId = (Integer) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        AdminStatisticsDTO stats = adminService.getStatistics();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        Integer adminId = (Integer) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<UserDTO> users = adminService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @PostMapping("/users/manage")
    public ResponseEntity<UserDTO> manageUser(@Valid @RequestBody UserManagementRequest request) {
        Integer adminId = (Integer) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserDTO user = adminService.manageUser(request, adminId);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/reports")
    public ResponseEntity<List<ReportDTO>> getAllReports(
            @RequestParam(required = false) ReportStatus status) {
        Integer adminId = (Integer) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<ReportDTO> reports;
        if (status != null) {
            reports = adminService.getReportsByStatus(status);
        } else {
            reports = adminService.getAllReports();
        }
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/reports/{id}")
    public ResponseEntity<ReportDTO> getReportById(@PathVariable Integer id) {
        Integer adminId = (Integer) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        ReportDTO report = adminService.getReportById(id);
        return ResponseEntity.ok(report);
    }

    @PostMapping("/reports/{id}/resolve")
    public ResponseEntity<ReportDTO> resolveReport(
            @PathVariable Integer id,
            @Valid @RequestBody ResolveReportRequest request) {
        Integer adminId = (Integer) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        ReportDTO report = adminService.resolveReport(id, request, adminId);
        return ResponseEntity.ok(report);
    }
}

