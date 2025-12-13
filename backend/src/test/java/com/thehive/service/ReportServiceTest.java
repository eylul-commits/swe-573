package com.thehive.service;

import com.thehive.exception.ResourceNotFoundException;
import com.thehive.model.dto.CreateReportRequest;
import com.thehive.model.dto.ReportDTO;
import com.thehive.model.entity.*;
import com.thehive.model.enums.ReportStatus;
import com.thehive.model.enums.ReportType;
import com.thehive.model.enums.UserRole;
import com.thehive.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OfferRepository offerRepository;

    @Mock
    private RequestRepository requestRepository;

    @Mock
    private ForumPostRepository forumPostRepository;

    @Mock
    private ForumTopicRepository forumTopicRepository;

    @InjectMocks
    private ReportService reportService;

    private User reporter;
    private User reportedUser;
    private Offer testOffer;
    private Request testRequest;
    private ForumPost testForumPost;
    private ForumTopic testForumTopic;
    private Report testReport;

    @BeforeEach
    void setUp() {
        // Setup reporter
        reporter = new User();
        reporter.setId(1);
        reporter.setEmail("reporter@example.com");
        reporter.setName("Reporter User");
        reporter.setRole(UserRole.USER);

        // Setup reported user
        reportedUser = new User();
        reportedUser.setId(2);
        reportedUser.setEmail("reported@example.com");
        reportedUser.setName("Reported User");
        reportedUser.setRole(UserRole.USER);

        // Setup test offer
        testOffer = new Offer();
        testOffer.setId(10);
        testOffer.setTitle("Test Offer");

        // Setup test request
        testRequest = new Request();
        testRequest.setId(20);
        testRequest.setTitle("Test Request");

        // Setup test forum post
        testForumPost = new ForumPost();
        testForumPost.setId(30);

        // Setup test forum topic
        testForumTopic = new ForumTopic();
        testForumTopic.setId(40);

        // Setup test report
        testReport = new Report();
        testReport.setId(1);
        testReport.setReporter(reporter);
        testReport.setReportedUser(reportedUser);
        testReport.setReportType(ReportType.USER);
        testReport.setMessage("Test report message");
        testReport.setStatus(ReportStatus.OPEN);
        testReport.setCreatedAt(LocalDateTime.now());
    }

    // ========== createReport Tests ==========

    @Test
    void createReport_WithUserType_ShouldSucceed() {
        // Arrange
        CreateReportRequest request = new CreateReportRequest();
        request.setReportType(ReportType.USER);
        request.setReportedUserId(2);
        request.setMessage("User report message");

        when(userRepository.findById(1)).thenReturn(Optional.of(reporter));
        when(userRepository.findById(2)).thenReturn(Optional.of(reportedUser));
        when(reportRepository.save(any(Report.class))).thenAnswer(invocation -> {
            Report report = invocation.getArgument(0);
            report.setId(1);
            return report;
        });

        // Act
        ReportDTO result = reportService.createReport(request, 1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals(1, result.getReporterId());
        assertEquals("Reporter User", result.getReporterName());
        assertEquals("reporter@example.com", result.getReporterEmail());
        assertEquals(2, result.getReportedUserId());
        assertEquals("Reported User", result.getReportedUserName());
        assertEquals("reported@example.com", result.getReportedUserEmail());
        assertEquals(ReportType.USER, result.getReportType());
        assertEquals("User report message", result.getMessage());
        assertEquals(ReportStatus.OPEN, result.getStatus());

        // Verify interactions
        verify(userRepository).findById(1);
        verify(userRepository).findById(2);
        verify(reportRepository).save(any(Report.class));
        verify(offerRepository, never()).findById(anyInt());
        verify(requestRepository, never()).findById(anyInt());
        verify(forumPostRepository, never()).findById(anyInt());
        verify(forumTopicRepository, never()).findById(anyInt());
    }

    @Test
    void createReport_WithOfferType_ShouldSucceed() {
        // Arrange
        CreateReportRequest request = new CreateReportRequest();
        request.setReportType(ReportType.OFFER);
        request.setReportedUserId(2);
        request.setReportedOfferId(10);
        request.setMessage("Offer report message");

        when(userRepository.findById(1)).thenReturn(Optional.of(reporter));
        when(userRepository.findById(2)).thenReturn(Optional.of(reportedUser));
        when(offerRepository.findById(10)).thenReturn(Optional.of(testOffer));
        when(reportRepository.save(any(Report.class))).thenAnswer(invocation -> {
            Report report = invocation.getArgument(0);
            report.setId(1);
            report.setReportedOffer(testOffer);
            return report;
        });

        // Act
        ReportDTO result = reportService.createReport(request, 1);

        // Assert
        assertNotNull(result);
        assertEquals(ReportType.OFFER, result.getReportType());
        assertEquals(10, result.getReportedOfferId());
        assertEquals("Test Offer", result.getReportedOfferTitle());

        // Verify interactions
        verify(userRepository).findById(1);
        verify(userRepository).findById(2);
        verify(offerRepository).findById(10);
        verify(reportRepository).save(any(Report.class));
    }

    @Test
    void createReport_WithRequestType_ShouldSucceed() {
        // Arrange
        CreateReportRequest request = new CreateReportRequest();
        request.setReportType(ReportType.REQUEST);
        request.setReportedUserId(2);
        request.setReportedRequestId(20);
        request.setMessage("Request report message");

        when(userRepository.findById(1)).thenReturn(Optional.of(reporter));
        when(userRepository.findById(2)).thenReturn(Optional.of(reportedUser));
        when(requestRepository.findById(20)).thenReturn(Optional.of(testRequest));
        when(reportRepository.save(any(Report.class))).thenAnswer(invocation -> {
            Report report = invocation.getArgument(0);
            report.setId(1);
            report.setReportedRequest(testRequest);
            return report;
        });

        // Act
        ReportDTO result = reportService.createReport(request, 1);

        // Assert
        assertNotNull(result);
        assertEquals(ReportType.REQUEST, result.getReportType());
        assertEquals(20, result.getReportedRequestId());
        assertEquals("Test Request", result.getReportedRequestTitle());

        // Verify interactions
        verify(userRepository).findById(1);
        verify(userRepository).findById(2);
        verify(requestRepository).findById(20);
        verify(reportRepository).save(any(Report.class));
    }

    @Test
    void createReport_WithForumPostType_ShouldSucceed() {
        // Arrange
        CreateReportRequest request = new CreateReportRequest();
        request.setReportType(ReportType.FORUM_POST);
        request.setReportedUserId(2);
        request.setReportedForumPostId(30);
        request.setMessage("Forum post report message");

        when(userRepository.findById(1)).thenReturn(Optional.of(reporter));
        when(userRepository.findById(2)).thenReturn(Optional.of(reportedUser));
        when(forumPostRepository.findById(30)).thenReturn(Optional.of(testForumPost));
        when(reportRepository.save(any(Report.class))).thenAnswer(invocation -> {
            Report report = invocation.getArgument(0);
            report.setId(1);
            report.setReportedForumPost(testForumPost);
            return report;
        });

        // Act
        ReportDTO result = reportService.createReport(request, 1);

        // Assert
        assertNotNull(result);
        assertEquals(ReportType.FORUM_POST, result.getReportType());
        assertEquals(30, result.getReportedForumPostId());

        // Verify interactions
        verify(userRepository).findById(1);
        verify(userRepository).findById(2);
        verify(forumPostRepository).findById(30);
        verify(reportRepository).save(any(Report.class));
    }

    @Test
    void createReport_WithForumTopicType_ShouldSucceed() {
        // Arrange
        CreateReportRequest request = new CreateReportRequest();
        request.setReportType(ReportType.FORUM_TOPIC);
        request.setReportedUserId(2);
        request.setReportedForumTopicId(40);
        request.setMessage("Forum topic report message");

        when(userRepository.findById(1)).thenReturn(Optional.of(reporter));
        when(userRepository.findById(2)).thenReturn(Optional.of(reportedUser));
        when(forumTopicRepository.findById(40)).thenReturn(Optional.of(testForumTopic));
        when(reportRepository.save(any(Report.class))).thenAnswer(invocation -> {
            Report report = invocation.getArgument(0);
            report.setId(1);
            report.setReportedForumTopic(testForumTopic);
            return report;
        });

        // Act
        ReportDTO result = reportService.createReport(request, 1);

        // Assert
        assertNotNull(result);
        assertEquals(ReportType.FORUM_TOPIC, result.getReportType());
        assertEquals(40, result.getReportedForumTopicId());

        // Verify interactions
        verify(userRepository).findById(1);
        verify(userRepository).findById(2);
        verify(forumTopicRepository).findById(40);
        verify(reportRepository).save(any(Report.class));
    }

    @Test
    void createReport_WithReporterNotFound_ShouldThrowException() {
        // Arrange
        CreateReportRequest request = new CreateReportRequest();
        request.setReportType(ReportType.USER);
        request.setReportedUserId(2);
        request.setMessage("Test message");

        when(userRepository.findById(1)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            reportService.createReport(request, 1);
        });

        assertEquals("Reporter not found with id: 1", exception.getMessage());

        // Verify
        verify(userRepository).findById(1);
        verify(userRepository, never()).findById(2);
        verify(reportRepository, never()).save(any(Report.class));
    }

    @Test
    void createReport_WithReportedUserNotFound_ShouldThrowException() {
        // Arrange
        CreateReportRequest request = new CreateReportRequest();
        request.setReportType(ReportType.USER);
        request.setReportedUserId(2);
        request.setMessage("Test message");

        when(userRepository.findById(1)).thenReturn(Optional.of(reporter));
        when(userRepository.findById(2)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            reportService.createReport(request, 1);
        });

        assertEquals("Reported user not found with id: 2", exception.getMessage());

        // Verify
        verify(userRepository).findById(1);
        verify(userRepository).findById(2);
        verify(reportRepository, never()).save(any(Report.class));
    }

    @Test
    void createReport_WithOfferTypeAndOfferNotFound_ShouldThrowException() {
        // Arrange
        CreateReportRequest request = new CreateReportRequest();
        request.setReportType(ReportType.OFFER);
        request.setReportedUserId(2);
        request.setReportedOfferId(10);
        request.setMessage("Test message");

        when(userRepository.findById(1)).thenReturn(Optional.of(reporter));
        when(userRepository.findById(2)).thenReturn(Optional.of(reportedUser));
        when(offerRepository.findById(10)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            reportService.createReport(request, 1);
        });

        assertEquals("Offer not found with id: 10", exception.getMessage());

        // Verify
        verify(userRepository).findById(1);
        verify(userRepository).findById(2);
        verify(offerRepository).findById(10);
        verify(reportRepository, never()).save(any(Report.class));
    }

    @Test
    void createReport_WithRequestTypeAndRequestNotFound_ShouldThrowException() {
        // Arrange
        CreateReportRequest request = new CreateReportRequest();
        request.setReportType(ReportType.REQUEST);
        request.setReportedUserId(2);
        request.setReportedRequestId(20);
        request.setMessage("Test message");

        when(userRepository.findById(1)).thenReturn(Optional.of(reporter));
        when(userRepository.findById(2)).thenReturn(Optional.of(reportedUser));
        when(requestRepository.findById(20)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            reportService.createReport(request, 1);
        });

        assertEquals("Request not found with id: 20", exception.getMessage());

        // Verify
        verify(userRepository).findById(1);
        verify(userRepository).findById(2);
        verify(requestRepository).findById(20);
        verify(reportRepository, never()).save(any(Report.class));
    }

    @Test
    void createReport_WithForumPostTypeAndPostNotFound_ShouldThrowException() {
        // Arrange
        CreateReportRequest request = new CreateReportRequest();
        request.setReportType(ReportType.FORUM_POST);
        request.setReportedUserId(2);
        request.setReportedForumPostId(30);
        request.setMessage("Test message");

        when(userRepository.findById(1)).thenReturn(Optional.of(reporter));
        when(userRepository.findById(2)).thenReturn(Optional.of(reportedUser));
        when(forumPostRepository.findById(30)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            reportService.createReport(request, 1);
        });

        assertEquals("Forum post not found with id: 30", exception.getMessage());

        // Verify
        verify(userRepository).findById(1);
        verify(userRepository).findById(2);
        verify(forumPostRepository).findById(30);
        verify(reportRepository, never()).save(any(Report.class));
    }

    @Test
    void createReport_WithForumTopicTypeAndTopicNotFound_ShouldThrowException() {
        // Arrange
        CreateReportRequest request = new CreateReportRequest();
        request.setReportType(ReportType.FORUM_TOPIC);
        request.setReportedUserId(2);
        request.setReportedForumTopicId(40);
        request.setMessage("Test message");

        when(userRepository.findById(1)).thenReturn(Optional.of(reporter));
        when(userRepository.findById(2)).thenReturn(Optional.of(reportedUser));
        when(forumTopicRepository.findById(40)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            reportService.createReport(request, 1);
        });

        assertEquals("Forum topic not found with id: 40", exception.getMessage());

        // Verify
        verify(userRepository).findById(1);
        verify(userRepository).findById(2);
        verify(forumTopicRepository).findById(40);
        verify(reportRepository, never()).save(any(Report.class));
    }

    @Test
    void createReport_WithOfferTypeAndMissingOfferId_ShouldThrowException() {
        // Arrange
        CreateReportRequest request = new CreateReportRequest();
        request.setReportType(ReportType.OFFER);
        request.setReportedUserId(2);
        request.setReportedOfferId(null); // Missing offer ID
        request.setMessage("Test message");

        when(userRepository.findById(1)).thenReturn(Optional.of(reporter));
        when(userRepository.findById(2)).thenReturn(Optional.of(reportedUser));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            reportService.createReport(request, 1);
        });

        assertEquals("Reported offer ID is required for OFFER report type", exception.getMessage());

        // Verify
        verify(userRepository).findById(1);
        verify(userRepository).findById(2);
        verify(offerRepository, never()).findById(anyInt());
        verify(reportRepository, never()).save(any(Report.class));
    }

    @Test
    void createReport_WithRequestTypeAndMissingRequestId_ShouldThrowException() {
        // Arrange
        CreateReportRequest request = new CreateReportRequest();
        request.setReportType(ReportType.REQUEST);
        request.setReportedUserId(2);
        request.setReportedRequestId(null); // Missing request ID
        request.setMessage("Test message");

        when(userRepository.findById(1)).thenReturn(Optional.of(reporter));
        when(userRepository.findById(2)).thenReturn(Optional.of(reportedUser));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            reportService.createReport(request, 1);
        });

        assertEquals("Reported request ID is required for REQUEST report type", exception.getMessage());

        // Verify
        verify(userRepository).findById(1);
        verify(userRepository).findById(2);
        verify(requestRepository, never()).findById(anyInt());
        verify(reportRepository, never()).save(any(Report.class));
    }

    @Test
    void createReport_WithForumPostTypeAndMissingPostId_ShouldThrowException() {
        // Arrange
        CreateReportRequest request = new CreateReportRequest();
        request.setReportType(ReportType.FORUM_POST);
        request.setReportedUserId(2);
        request.setReportedForumPostId(null); // Missing post ID
        request.setMessage("Test message");

        when(userRepository.findById(1)).thenReturn(Optional.of(reporter));
        when(userRepository.findById(2)).thenReturn(Optional.of(reportedUser));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            reportService.createReport(request, 1);
        });

        assertEquals("Reported forum post ID is required for FORUM_POST report type", exception.getMessage());

        // Verify
        verify(userRepository).findById(1);
        verify(userRepository).findById(2);
        verify(forumPostRepository, never()).findById(anyInt());
        verify(reportRepository, never()).save(any(Report.class));
    }

    @Test
    void createReport_WithForumTopicTypeAndMissingTopicId_ShouldThrowException() {
        // Arrange
        CreateReportRequest request = new CreateReportRequest();
        request.setReportType(ReportType.FORUM_TOPIC);
        request.setReportedUserId(2);
        request.setReportedForumTopicId(null); // Missing topic ID
        request.setMessage("Test message");

        when(userRepository.findById(1)).thenReturn(Optional.of(reporter));
        when(userRepository.findById(2)).thenReturn(Optional.of(reportedUser));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            reportService.createReport(request, 1);
        });

        assertEquals("Reported forum topic ID is required for FORUM_TOPIC report type", exception.getMessage());

        // Verify
        verify(userRepository).findById(1);
        verify(userRepository).findById(2);
        verify(forumTopicRepository, never()).findById(anyInt());
        verify(reportRepository, never()).save(any(Report.class));
    }

    // ========== getReportsByReporter Tests ==========

    @Test
    void getReportsByReporter_WithExistingReports_ShouldReturnReports() {
        // Arrange
        Report report1 = new Report();
        report1.setId(1);
        report1.setReporter(reporter);
        report1.setReportedUser(reportedUser);
        report1.setReportType(ReportType.USER);
        report1.setMessage("Report 1");
        report1.setStatus(ReportStatus.OPEN);

        Report report2 = new Report();
        report2.setId(2);
        report2.setReporter(reporter);
        report2.setReportedUser(reportedUser);
        report2.setReportType(ReportType.OFFER);
        report2.setMessage("Report 2");
        report2.setStatus(ReportStatus.OPEN);
        report2.setReportedOffer(testOffer);

        when(reportRepository.findByReporterId(1)).thenReturn(Arrays.asList(report1, report2));

        // Act
        List<ReportDTO> result = reportService.getReportsByReporter(1);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getId());
        assertEquals(2, result.get(1).getId());
        assertEquals(ReportType.USER, result.get(0).getReportType());
        assertEquals(ReportType.OFFER, result.get(1).getReportType());

        // Verify
        verify(reportRepository).findByReporterId(1);
    }

    @Test
    void getReportsByReporter_WithNoReports_ShouldReturnEmptyList() {
        // Arrange
        when(reportRepository.findByReporterId(1)).thenReturn(Collections.emptyList());

        // Act
        List<ReportDTO> result = reportService.getReportsByReporter(1);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        // Verify
        verify(reportRepository).findByReporterId(1);
    }

    // ========== getAllReports Tests ==========

    @Test
    void getAllReports_WithExistingReports_ShouldReturnAllReports() {
        // Arrange
        Report report1 = new Report();
        report1.setId(1);
        report1.setReporter(reporter);
        report1.setReportedUser(reportedUser);
        report1.setReportType(ReportType.USER);
        report1.setStatus(ReportStatus.OPEN);

        Report report2 = new Report();
        report2.setId(2);
        report2.setReporter(reporter);
        report2.setReportedUser(reportedUser);
        report2.setReportType(ReportType.REQUEST);
        report2.setStatus(ReportStatus.RESOLVED);

        when(reportRepository.findAll()).thenReturn(Arrays.asList(report1, report2));

        // Act
        List<ReportDTO> result = reportService.getAllReports();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getId());
        assertEquals(2, result.get(1).getId());

        // Verify
        verify(reportRepository).findAll();
    }

    @Test
    void getAllReports_WithNoReports_ShouldReturnEmptyList() {
        // Arrange
        when(reportRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<ReportDTO> result = reportService.getAllReports();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        // Verify
        verify(reportRepository).findAll();
    }

    // ========== getReportsByStatus Tests ==========

    @Test
    void getReportsByStatus_WithOpenStatus_ShouldReturnOpenReports() {
        // Arrange
        Report report1 = new Report();
        report1.setId(1);
        report1.setReporter(reporter);
        report1.setReportedUser(reportedUser);
        report1.setStatus(ReportStatus.OPEN);

        Report report2 = new Report();
        report2.setId(2);
        report2.setReporter(reporter);
        report2.setReportedUser(reportedUser);
        report2.setStatus(ReportStatus.OPEN);

        when(reportRepository.findByStatus(ReportStatus.OPEN)).thenReturn(Arrays.asList(report1, report2));

        // Act
        List<ReportDTO> result = reportService.getReportsByStatus(ReportStatus.OPEN);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(ReportStatus.OPEN, result.get(0).getStatus());
        assertEquals(ReportStatus.OPEN, result.get(1).getStatus());

        // Verify
        verify(reportRepository).findByStatus(ReportStatus.OPEN);
    }

    @Test
    void getReportsByStatus_WithResolvedStatus_ShouldReturnResolvedReports() {
        // Arrange
        Report report1 = new Report();
        report1.setId(1);
        report1.setReporter(reporter);
        report1.setReportedUser(reportedUser);
        report1.setStatus(ReportStatus.RESOLVED);

        when(reportRepository.findByStatus(ReportStatus.RESOLVED)).thenReturn(Collections.singletonList(report1));

        // Act
        List<ReportDTO> result = reportService.getReportsByStatus(ReportStatus.RESOLVED);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(ReportStatus.RESOLVED, result.get(0).getStatus());

        // Verify
        verify(reportRepository).findByStatus(ReportStatus.RESOLVED);
    }

    @Test
    void getReportsByStatus_WithNoReports_ShouldReturnEmptyList() {
        // Arrange
        when(reportRepository.findByStatus(ReportStatus.OPEN)).thenReturn(Collections.emptyList());

        // Act
        List<ReportDTO> result = reportService.getReportsByStatus(ReportStatus.OPEN);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        // Verify
        verify(reportRepository).findByStatus(ReportStatus.OPEN);
    }

    // ========== getReportById Tests ==========

    @Test
    void getReportById_WithExistingReport_ShouldReturnReport() {
        // Arrange
        testReport.setReportedOffer(testOffer);
        when(reportRepository.findById(1)).thenReturn(Optional.of(testReport));

        // Act
        ReportDTO result = reportService.getReportById(1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals(1, result.getReporterId());
        assertEquals("Reporter User", result.getReporterName());
        assertEquals(2, result.getReportedUserId());
        assertEquals("Reported User", result.getReportedUserName());
        assertEquals(ReportType.USER, result.getReportType());
        assertEquals("Test report message", result.getMessage());
        assertEquals(ReportStatus.OPEN, result.getStatus());
        assertEquals(10, result.getReportedOfferId());
        assertEquals("Test Offer", result.getReportedOfferTitle());

        // Verify
        verify(reportRepository).findById(1);
    }

    @Test
    void getReportById_WithResolvedReport_ShouldReturnReportWithResolvedInfo() {
        // Arrange
        User admin = new User();
        admin.setId(3);
        admin.setName("Admin User");

        testReport.setStatus(ReportStatus.RESOLVED);
        testReport.setResolvedBy(admin);
        testReport.setResolvedAt(LocalDateTime.now());
        testReport.setAdminNotes("Resolved by admin");

        when(reportRepository.findById(1)).thenReturn(Optional.of(testReport));

        // Act
        ReportDTO result = reportService.getReportById(1);

        // Assert
        assertNotNull(result);
        assertEquals(ReportStatus.RESOLVED, result.getStatus());
        assertEquals(3, result.getResolvedById());
        assertEquals("Admin User", result.getResolvedByName());
        assertNotNull(result.getResolvedAt());
        assertEquals("Resolved by admin", result.getAdminNotes());

        // Verify
        verify(reportRepository).findById(1);
    }

    @Test
    void getReportById_WithNonExistentReport_ShouldThrowException() {
        // Arrange
        when(reportRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            reportService.getReportById(999);
        });

        assertEquals("Report not found with id: 999", exception.getMessage());

        // Verify
        verify(reportRepository).findById(999);
    }

    @Test
    void getReportById_WithReportContainingAllContentTypes_ShouldReturnCompleteDTO() {
        // Arrange
        // Create a report with all possible content references
        Report complexReport = new Report();
        complexReport.setId(1);
        complexReport.setReporter(reporter);
        complexReport.setReportedUser(reportedUser);
        complexReport.setReportType(ReportType.OFFER);
        complexReport.setReportedOffer(testOffer);
        complexReport.setMessage("Complex report");
        complexReport.setStatus(ReportStatus.OPEN);
        complexReport.setCreatedAt(LocalDateTime.now());

        when(reportRepository.findById(1)).thenReturn(Optional.of(complexReport));

        // Act
        ReportDTO result = reportService.getReportById(1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals(ReportType.OFFER, result.getReportType());
        assertEquals(10, result.getReportedOfferId());
        assertEquals("Test Offer", result.getReportedOfferTitle());
        assertNull(result.getReportedRequestId());
        assertNull(result.getReportedForumPostId());
        assertNull(result.getReportedForumTopicId());

        // Verify
        verify(reportRepository).findById(1);
    }
}

