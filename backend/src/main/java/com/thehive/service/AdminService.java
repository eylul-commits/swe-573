package com.thehive.service;

import com.thehive.exception.ResourceNotFoundException;
import com.thehive.model.dto.AdminStatisticsDTO;
import com.thehive.model.dto.ReportDTO;
import com.thehive.model.dto.ResolveReportRequest;
import com.thehive.model.dto.UserDTO;
import com.thehive.model.dto.UserManagementRequest;
import com.thehive.model.entity.Report;
import com.thehive.model.entity.User;
import com.thehive.model.enums.ReportStatus;
import com.thehive.model.enums.UserRole;
import com.thehive.model.enums.UserStatus;
import com.thehive.repository.ReportRepository;
import com.thehive.repository.UserRepository;
import com.thehive.repository.OfferRepository;
import com.thehive.repository.RequestRepository;
import com.thehive.repository.HandshakeRepository;
import com.thehive.repository.MessageRepository;
import com.thehive.util.AdminUtil;
import com.thehive.model.enums.ItemStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final OfferRepository offerRepository;
    private final RequestRepository requestRepository;
    private final ReportRepository reportRepository;
    private final HandshakeRepository handshakeRepository;
    private final MessageRepository messageRepository;

    @Transactional(readOnly = true)
    public AdminStatisticsDTO getStatistics() {
        AdminUtil.requireAdmin(userRepository);
        AdminStatisticsDTO stats = new AdminStatisticsDTO();
        
        // User statistics
        stats.setTotalUsers(userRepository.count());
        stats.setActiveUsers(userRepository.findAll().stream()
                .filter(u -> u.getAccountStatus() == UserStatus.ACTIVE)
                .count());
        stats.setDeactivatedUsers(userRepository.findAll().stream()
                .filter(u -> u.getAccountStatus() == UserStatus.DEACTIVATED)
                .count());
        
        // Offer statistics
        stats.setTotalOffers(offerRepository.count());
        stats.setActiveOffers((long) offerRepository.findByStatus(ItemStatus.ACTIVE).size());
        
        // Request statistics
        stats.setTotalRequests(requestRepository.count());
        stats.setActiveRequests((long) requestRepository.findByStatus(ItemStatus.ACTIVE).size());
        
        // Report statistics
        stats.setTotalReports(reportRepository.count());
        stats.setOpenReports((long) reportRepository.findByStatus(ReportStatus.OPEN).size());
        stats.setInReviewReports((long) reportRepository.findByStatus(ReportStatus.IN_REVIEW).size());
        stats.setResolvedReports((long) reportRepository.findByStatus(ReportStatus.RESOLVED).size());
        
        // Handshake statistics
        stats.setTotalHandshakes(handshakeRepository.count());
        
        // Message statistics
        stats.setTotalMessages(messageRepository.count());
        
        return stats;
    }

    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        AdminUtil.requireAdmin(userRepository);
        return userRepository.findAll().stream()
                .map(this::convertToUserDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserDTO manageUser(UserManagementRequest request, Integer adminId) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getUserId()));

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found with id: " + adminId));

        if (admin.getRole() != UserRole.ADMIN) {
            throw new RuntimeException("Only admins can manage users");
        }

        switch (request.getAction().toUpperCase()) {
            case "WARN":
                user.setWarningCount(user.getWarningCount() + 1);
                user.setAccountStatus(UserStatus.WARNED);
                break;
            case "DEACTIVATE":
                user.setAccountStatus(UserStatus.DEACTIVATED);
                break;
            case "ACTIVATE":
                user.setAccountStatus(UserStatus.ACTIVE);
                break;
            default:
                throw new IllegalArgumentException("Invalid action: " + request.getAction());
        }

        user = userRepository.save(user);
        return convertToUserDTO(user);
    }

    @Transactional
    public ReportDTO resolveReport(Integer reportId, ResolveReportRequest request, Integer adminId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found with id: " + reportId));

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found with id: " + adminId));

        if (admin.getRole() != UserRole.ADMIN) {
            throw new RuntimeException("Only admins can resolve reports");
        }

        report.setStatus(request.getStatus());
        report.setAdminNotes(request.getAdminNotes());
        report.setResolvedBy(admin);
        report.setResolvedAt(LocalDateTime.now());

        // Handle user actions if specified
        if (request.getUserId() != null && request.getAction() != null) {
            User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getUserId()));

            switch (request.getAction().toUpperCase()) {
                case "WARN":
                    user.setWarningCount(user.getWarningCount() + 1);
                    user.setAccountStatus(UserStatus.WARNED);
                    break;
                case "DEACTIVATE":
                    user.setAccountStatus(UserStatus.DEACTIVATED);
                    break;
                case "NO_ACTION":
                    // No action needed
                    break;
                default:
                    throw new IllegalArgumentException("Invalid action: " + request.getAction());
            }
            userRepository.save(user);
        }

        report = reportRepository.save(report);
        return convertToReportDTO(report);
    }

    @Transactional(readOnly = true)
    public List<ReportDTO> getAllReports() {
        AdminUtil.requireAdmin(userRepository);
        return reportRepository.findAll().stream()
                .map(this::convertToReportDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ReportDTO> getReportsByStatus(ReportStatus status) {
        AdminUtil.requireAdmin(userRepository);
        return reportRepository.findByStatus(status).stream()
                .map(this::convertToReportDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ReportDTO getReportById(Integer id) {
        AdminUtil.requireAdmin(userRepository);
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found with id: " + id));
        return convertToReportDTO(report);
    }

    private UserDTO convertToUserDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setName(user.getName());
        dto.setBio(user.getBio());
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setProvince(user.getProvince());
        dto.setDistrict(user.getDistrict());
        dto.setGeohash(user.getGeohash());
        dto.setRole(user.getRole());
        dto.setAccountStatus(user.getAccountStatus());
        dto.setWarningCount(user.getWarningCount());
        dto.setBalanceHours(user.getBalanceHours());
        return dto;
    }

    private ReportDTO convertToReportDTO(Report report) {
        ReportDTO dto = new ReportDTO();
        dto.setId(report.getId());
        dto.setReporterId(report.getReporter().getId());
        dto.setReporterName(report.getReporter().getName());
        dto.setReporterEmail(report.getReporter().getEmail());
        dto.setReportedUserId(report.getReportedUser().getId());
        dto.setReportedUserName(report.getReportedUser().getName());
        dto.setReportedUserEmail(report.getReportedUser().getEmail());
        dto.setReportType(report.getReportType());
        
        if (report.getReportedOffer() != null) {
            dto.setReportedOfferId(report.getReportedOffer().getId());
            dto.setReportedOfferTitle(report.getReportedOffer().getTitle());
        }
        
        if (report.getReportedRequest() != null) {
            dto.setReportedRequestId(report.getReportedRequest().getId());
            dto.setReportedRequestTitle(report.getReportedRequest().getTitle());
        }
        
        if (report.getReportedForumPost() != null) {
            dto.setReportedForumPostId(report.getReportedForumPost().getId());
        }
        
        if (report.getReportedForumTopic() != null) {
            dto.setReportedForumTopicId(report.getReportedForumTopic().getId());
        }
        
        dto.setMessage(report.getMessage());
        dto.setAdminNotes(report.getAdminNotes());
        dto.setStatus(report.getStatus());
        dto.setCreatedAt(report.getCreatedAt());
        dto.setResolvedAt(report.getResolvedAt());
        
        if (report.getResolvedBy() != null) {
            dto.setResolvedById(report.getResolvedBy().getId());
            dto.setResolvedByName(report.getResolvedBy().getName());
        }
        
        return dto;
    }
}

