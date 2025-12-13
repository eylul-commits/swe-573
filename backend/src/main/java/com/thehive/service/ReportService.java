package com.thehive.service;

import com.thehive.exception.ResourceNotFoundException;
import com.thehive.model.dto.CreateReportRequest;
import com.thehive.model.dto.ReportDTO;
import com.thehive.model.entity.Report;
import com.thehive.model.entity.User;
import com.thehive.model.entity.Offer;
import com.thehive.model.entity.Request;
import com.thehive.model.entity.ForumPost;
import com.thehive.model.entity.ForumTopic;
import com.thehive.model.enums.ReportType;
import com.thehive.model.enums.ReportStatus;
import com.thehive.repository.ReportRepository;
import com.thehive.repository.UserRepository;
import com.thehive.repository.OfferRepository;
import com.thehive.repository.RequestRepository;
import com.thehive.repository.ForumPostRepository;
import com.thehive.repository.ForumTopicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final OfferRepository offerRepository;
    private final RequestRepository requestRepository;
    private final ForumPostRepository forumPostRepository;
    private final ForumTopicRepository forumTopicRepository;

    @Transactional
    public ReportDTO createReport(CreateReportRequest request, Integer reporterId) {
        // Get reporter
        User reporter = userRepository.findById(reporterId)
                .orElseThrow(() -> new ResourceNotFoundException("Reporter not found with id: " + reporterId));

        // Get reported user
        User reportedUser = userRepository.findById(request.getReportedUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Reported user not found with id: " + request.getReportedUserId()));

        // Validate report type and content references
        validateReportRequest(request);

        // Create report
        Report report = new Report();
        report.setReporter(reporter);
        report.setReportedUser(reportedUser);
        report.setReportType(request.getReportType());
        report.setMessage(request.getMessage());
        report.setStatus(ReportStatus.OPEN);

        // Set content references based on report type
        if (request.getReportType() == ReportType.OFFER && request.getReportedOfferId() != null) {
            Offer offer = offerRepository.findById(request.getReportedOfferId())
                    .orElseThrow(() -> new ResourceNotFoundException("Offer not found with id: " + request.getReportedOfferId()));
            report.setReportedOffer(offer);
        } else if (request.getReportType() == ReportType.REQUEST && request.getReportedRequestId() != null) {
            Request req = requestRepository.findById(request.getReportedRequestId())
                    .orElseThrow(() -> new ResourceNotFoundException("Request not found with id: " + request.getReportedRequestId()));
            report.setReportedRequest(req);
        } else if (request.getReportType() == ReportType.FORUM_POST && request.getReportedForumPostId() != null) {
            ForumPost post = forumPostRepository.findById(request.getReportedForumPostId())
                    .orElseThrow(() -> new ResourceNotFoundException("Forum post not found with id: " + request.getReportedForumPostId()));
            report.setReportedForumPost(post);
        } else if (request.getReportType() == ReportType.FORUM_TOPIC && request.getReportedForumTopicId() != null) {
            ForumTopic topic = forumTopicRepository.findById(request.getReportedForumTopicId())
                    .orElseThrow(() -> new ResourceNotFoundException("Forum topic not found with id: " + request.getReportedForumTopicId()));
            report.setReportedForumTopic(topic);
        }

        report = reportRepository.save(report);
        return convertToDTO(report);
    }

    private void validateReportRequest(CreateReportRequest request) {
        // Validate that content ID matches report type
        switch (request.getReportType()) {
            case OFFER:
                if (request.getReportedOfferId() == null) {
                    throw new IllegalArgumentException("Reported offer ID is required for OFFER report type");
                }
                break;
            case REQUEST:
                if (request.getReportedRequestId() == null) {
                    throw new IllegalArgumentException("Reported request ID is required for REQUEST report type");
                }
                break;
            case FORUM_POST:
                if (request.getReportedForumPostId() == null) {
                    throw new IllegalArgumentException("Reported forum post ID is required for FORUM_POST report type");
                }
                break;
            case FORUM_TOPIC:
                if (request.getReportedForumTopicId() == null) {
                    throw new IllegalArgumentException("Reported forum topic ID is required for FORUM_TOPIC report type");
                }
                break;
            case USER:
                // No additional content ID needed
                break;
        }
    }

    @Transactional(readOnly = true)
    public List<ReportDTO> getReportsByReporter(Integer reporterId) {
        List<Report> reports = reportRepository.findByReporterId(reporterId);
        return reports.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ReportDTO> getAllReports() {
        List<Report> reports = reportRepository.findAll();
        return reports.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ReportDTO> getReportsByStatus(ReportStatus status) {
        List<Report> reports = reportRepository.findByStatus(status);
        return reports.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ReportDTO getReportById(Integer id) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found with id: " + id));
        return convertToDTO(report);
    }

    private ReportDTO convertToDTO(Report report) {
        ReportDTO dto = new ReportDTO();
        dto.setId(report.getId());
        dto.setReporterId(report.getReporter().getId());
        dto.setReporterName(report.getReporter().getName());
        dto.setReporterEmail(report.getReporter().getEmail());
        dto.setReportedUserId(report.getReportedUser().getId());
        dto.setReportedUserName(report.getReportedUser().getName());
        dto.setReportedUserEmail(report.getReportedUser().getEmail());
        dto.setReportedUserRole(report.getReportedUser().getRole());
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

