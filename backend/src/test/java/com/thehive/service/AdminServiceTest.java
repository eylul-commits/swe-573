package com.thehive.service;

import com.thehive.exception.ResourceNotFoundException;
import com.thehive.model.dto.AdminStatisticsDTO;
import com.thehive.model.dto.ReportDTO;
import com.thehive.model.dto.ResolveReportRequest;
import com.thehive.model.dto.UserDTO;
import com.thehive.model.dto.UserManagementRequest;
import com.thehive.model.entity.Report;
import com.thehive.model.entity.User;
import com.thehive.model.enums.ItemStatus;
import com.thehive.model.enums.ReportStatus;
import com.thehive.model.enums.ReportType;
import com.thehive.model.enums.UserRole;
import com.thehive.model.enums.UserStatus;
import com.thehive.repository.HandshakeRepository;
import com.thehive.repository.MessageRepository;
import com.thehive.repository.OfferRepository;
import com.thehive.repository.ReportRepository;
import com.thehive.repository.RequestRepository;
import com.thehive.repository.UserRepository;
import com.thehive.util.AdminUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private OfferRepository offerRepository;

    @Mock
    private RequestRepository requestRepository;

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private HandshakeRepository handshakeRepository;

    @Mock
    private MessageRepository messageRepository;

    @InjectMocks
    private AdminService adminService;

    private User adminUser;
    private User regularUser;
    private Report testReport;

    @BeforeEach
    void setUp() {
        // Setup admin user
        adminUser = new User();
        adminUser.setId(1);
        adminUser.setEmail("admin@example.com");
        adminUser.setName("Admin User");
        adminUser.setRole(UserRole.ADMIN);
        adminUser.setAccountStatus(UserStatus.ACTIVE);
        adminUser.setBalanceHours(10);
        adminUser.setWarningCount(0);

        // Setup regular user
        regularUser = new User();
        regularUser.setId(2);
        regularUser.setEmail("user@example.com");
        regularUser.setName("Regular User");
        regularUser.setRole(UserRole.USER);
        regularUser.setAccountStatus(UserStatus.ACTIVE);
        regularUser.setBalanceHours(5);
        regularUser.setWarningCount(0);

        // Setup test report
        testReport = new Report();
        testReport.setId(1);
        testReport.setReporter(regularUser);
        testReport.setReportedUser(regularUser);
        testReport.setReportType(ReportType.USER);
        testReport.setMessage("Test report message");
        testReport.setStatus(ReportStatus.OPEN);
        testReport.setCreatedAt(LocalDateTime.now());
    }

    private void setupSecurityContext() {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(adminUser.getId());
        when(userRepository.findById(adminUser.getId())).thenReturn(Optional.of(adminUser));
    }

    // ==================== GET STATISTICS TESTS ====================

    @Test
    void getStatistics_WithValidAdmin_ShouldReturnStatistics() {
        // Arrange
        setupSecurityContext();
        List<User> users = Arrays.asList(adminUser, regularUser);
        when(userRepository.count()).thenReturn(2L);
        when(userRepository.findAll()).thenReturn(users);
        when(offerRepository.count()).thenReturn(10L);
        when(offerRepository.findByStatus(ItemStatus.ACTIVE)).thenReturn(Collections.emptyList());
        when(requestRepository.count()).thenReturn(5L);
        when(requestRepository.findByStatus(ItemStatus.ACTIVE)).thenReturn(Collections.emptyList());
        when(reportRepository.count()).thenReturn(3L);
        when(reportRepository.findByStatus(ReportStatus.OPEN)).thenReturn(Arrays.asList(testReport));
        when(reportRepository.findByStatus(ReportStatus.IN_REVIEW)).thenReturn(Collections.emptyList());
        when(reportRepository.findByStatus(ReportStatus.RESOLVED)).thenReturn(Collections.emptyList());
        when(handshakeRepository.count()).thenReturn(7L);
        when(messageRepository.count()).thenReturn(15L);

        // Act
        AdminStatisticsDTO result = adminService.getStatistics();

        // Assert
        assertNotNull(result);
        assertEquals(2L, result.getTotalUsers());
        assertEquals(2L, result.getActiveUsers());
        assertEquals(0L, result.getDeactivatedUsers());
        assertEquals(10L, result.getTotalOffers());
        assertEquals(0L, result.getActiveOffers());
        assertEquals(5L, result.getTotalRequests());
        assertEquals(0L, result.getActiveRequests());
        assertEquals(3L, result.getTotalReports());
        assertEquals(1L, result.getOpenReports());
        assertEquals(0L, result.getInReviewReports());
        assertEquals(0L, result.getResolvedReports());
        assertEquals(7L, result.getTotalHandshakes());
        assertEquals(15L, result.getTotalMessages());

        verify(userRepository).count();
        verify(offerRepository).count();
        verify(requestRepository).count();
        verify(reportRepository).count();
        verify(handshakeRepository).count();
        verify(messageRepository).count();
    }

    @Test
    void getStatistics_WithDeactivatedUsers_ShouldCountCorrectly() {
        // Arrange
        setupSecurityContext();
        User deactivatedUser = new User();
        deactivatedUser.setId(3);
        deactivatedUser.setAccountStatus(UserStatus.DEACTIVATED);
        List<User> users = Arrays.asList(adminUser, regularUser, deactivatedUser);
        
        when(userRepository.count()).thenReturn(3L);
        when(userRepository.findAll()).thenReturn(users);
        when(offerRepository.count()).thenReturn(0L);
        when(offerRepository.findByStatus(ItemStatus.ACTIVE)).thenReturn(Collections.emptyList());
        when(requestRepository.count()).thenReturn(0L);
        when(requestRepository.findByStatus(ItemStatus.ACTIVE)).thenReturn(Collections.emptyList());
        when(reportRepository.count()).thenReturn(0L);
        when(reportRepository.findByStatus(any(ReportStatus.class))).thenReturn(Collections.emptyList());
        when(handshakeRepository.count()).thenReturn(0L);
        when(messageRepository.count()).thenReturn(0L);

        // Act
        AdminStatisticsDTO result = adminService.getStatistics();

        // Assert
        assertNotNull(result);
        assertEquals(3L, result.getTotalUsers());
        assertEquals(2L, result.getActiveUsers());
        assertEquals(1L, result.getDeactivatedUsers());
    }

    // ==================== GET ALL USERS TESTS ====================

    @Test
    void getAllUsers_WithValidAdmin_ShouldReturnAllUsers() {
        // Arrange
        setupSecurityContext();
        List<User> users = Arrays.asList(adminUser, regularUser);
        when(userRepository.findAll()).thenReturn(users);

        // Act
        List<UserDTO> result = adminService.getAllUsers();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(adminUser.getId(), result.get(0).getId());
        assertEquals(adminUser.getEmail(), result.get(0).getEmail());
        assertEquals(adminUser.getName(), result.get(0).getName());
        assertEquals(regularUser.getId(), result.get(1).getId());

        verify(userRepository).findAll();
    }

    @Test
    void getAllUsers_WithEmptyRepository_ShouldReturnEmptyList() {
        // Arrange
        setupSecurityContext();
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<UserDTO> result = adminService.getAllUsers();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userRepository).findAll();
    }

    // ==================== MANAGE USER TESTS ====================

    @Test
    void manageUser_WithWarnAction_ShouldIncrementWarningCount() {
        // Arrange
        UserManagementRequest request = new UserManagementRequest();
        request.setUserId(regularUser.getId());
        request.setAction("WARN");

        when(userRepository.findById(regularUser.getId())).thenReturn(Optional.of(regularUser));
        when(userRepository.findById(adminUser.getId())).thenReturn(Optional.of(adminUser));
        when(userRepository.save(any(User.class))).thenReturn(regularUser);

        // Act
        UserDTO result = adminService.manageUser(request, adminUser.getId());

        // Assert
        assertNotNull(result);
        assertEquals(1, regularUser.getWarningCount());
        assertEquals(UserStatus.WARNED, regularUser.getAccountStatus());
        verify(userRepository).save(regularUser);
    }

    @Test
    void manageUser_WithDeactivateAction_ShouldDeactivateUser() {
        // Arrange
        UserManagementRequest request = new UserManagementRequest();
        request.setUserId(regularUser.getId());
        request.setAction("DEACTIVATE");

        when(userRepository.findById(regularUser.getId())).thenReturn(Optional.of(regularUser));
        when(userRepository.findById(adminUser.getId())).thenReturn(Optional.of(adminUser));
        when(userRepository.save(any(User.class))).thenReturn(regularUser);

        // Act
        UserDTO result = adminService.manageUser(request, adminUser.getId());

        // Assert
        assertNotNull(result);
        assertEquals(UserStatus.DEACTIVATED, regularUser.getAccountStatus());
        verify(userRepository).save(regularUser);
    }

    @Test
    void manageUser_WithActivateAction_ShouldActivateUser() {
        // Arrange
        regularUser.setAccountStatus(UserStatus.DEACTIVATED);
        UserManagementRequest request = new UserManagementRequest();
        request.setUserId(regularUser.getId());
        request.setAction("ACTIVATE");

        when(userRepository.findById(regularUser.getId())).thenReturn(Optional.of(regularUser));
        when(userRepository.findById(adminUser.getId())).thenReturn(Optional.of(adminUser));
        when(userRepository.save(any(User.class))).thenReturn(regularUser);

        // Act
        UserDTO result = adminService.manageUser(request, adminUser.getId());

        // Assert
        assertNotNull(result);
        assertEquals(UserStatus.ACTIVE, regularUser.getAccountStatus());
        verify(userRepository).save(regularUser);
    }

    @Test
    void manageUser_WithInvalidAction_ShouldThrowException() {
        // Arrange
        UserManagementRequest request = new UserManagementRequest();
        request.setUserId(regularUser.getId());
        request.setAction("INVALID_ACTION");

        when(userRepository.findById(regularUser.getId())).thenReturn(Optional.of(regularUser));
        when(userRepository.findById(adminUser.getId())).thenReturn(Optional.of(adminUser));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            adminService.manageUser(request, adminUser.getId());
        });

        assertEquals("Invalid action: INVALID_ACTION", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void manageUser_WithNonAdminUser_ShouldThrowException() {
        // Arrange
        User nonAdminUser = new User();
        nonAdminUser.setId(3);
        nonAdminUser.setRole(UserRole.USER);

        UserManagementRequest request = new UserManagementRequest();
        request.setUserId(regularUser.getId());
        request.setAction("WARN");

        when(userRepository.findById(regularUser.getId())).thenReturn(Optional.of(regularUser));
        when(userRepository.findById(nonAdminUser.getId())).thenReturn(Optional.of(nonAdminUser));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            adminService.manageUser(request, nonAdminUser.getId());
        });

        assertEquals("Only admins can manage users", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void manageUser_WithNonExistentUser_ShouldThrowException() {
        // Arrange
        UserManagementRequest request = new UserManagementRequest();
        request.setUserId(999);
        request.setAction("WARN");

        when(userRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            adminService.manageUser(request, adminUser.getId());
        });

        assertTrue(exception.getMessage().contains("User not found"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void manageUser_WithNonExistentAdmin_ShouldThrowException() {
        // Arrange
        UserManagementRequest request = new UserManagementRequest();
        request.setUserId(regularUser.getId());
        request.setAction("WARN");

        when(userRepository.findById(regularUser.getId())).thenReturn(Optional.of(regularUser));
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            adminService.manageUser(request, 999);
        });

        assertTrue(exception.getMessage().contains("Admin not found"));
        verify(userRepository, never()).save(any(User.class));
    }

    // ==================== RESOLVE REPORT TESTS ====================

    @Test
    void resolveReport_WithValidRequest_ShouldResolveReport() {
        // Arrange
        ResolveReportRequest request = new ResolveReportRequest();
        request.setStatus(ReportStatus.RESOLVED);
        request.setAdminNotes("Report resolved");

        when(reportRepository.findById(testReport.getId())).thenReturn(Optional.of(testReport));
        when(userRepository.findById(adminUser.getId())).thenReturn(Optional.of(adminUser));
        when(reportRepository.save(any(Report.class))).thenReturn(testReport);

        // Act
        ReportDTO result = adminService.resolveReport(testReport.getId(), request, adminUser.getId());

        // Assert
        assertNotNull(result);
        assertEquals(ReportStatus.RESOLVED, testReport.getStatus());
        assertEquals("Report resolved", testReport.getAdminNotes());
        assertEquals(adminUser, testReport.getResolvedBy());
        assertNotNull(testReport.getResolvedAt());
        verify(reportRepository).save(testReport);
    }

    @Test
    void resolveReport_WithWarnAction_ShouldWarnUser() {
        // Arrange
        ResolveReportRequest request = new ResolveReportRequest();
        request.setStatus(ReportStatus.RESOLVED);
        request.setUserId(regularUser.getId());
        request.setAction("WARN");

        when(reportRepository.findById(testReport.getId())).thenReturn(Optional.of(testReport));
        when(userRepository.findById(adminUser.getId())).thenReturn(Optional.of(adminUser));
        when(userRepository.findById(regularUser.getId())).thenReturn(Optional.of(regularUser));
        when(reportRepository.save(any(Report.class))).thenReturn(testReport);
        when(userRepository.save(any(User.class))).thenReturn(regularUser);

        // Act
        ReportDTO result = adminService.resolveReport(testReport.getId(), request, adminUser.getId());

        // Assert
        assertNotNull(result);
        assertEquals(1, regularUser.getWarningCount());
        assertEquals(UserStatus.WARNED, regularUser.getAccountStatus());
        verify(userRepository).save(regularUser);
        verify(reportRepository).save(testReport);
    }

    @Test
    void resolveReport_WithDeactivateAction_ShouldDeactivateUser() {
        // Arrange
        ResolveReportRequest request = new ResolveReportRequest();
        request.setStatus(ReportStatus.RESOLVED);
        request.setUserId(regularUser.getId());
        request.setAction("DEACTIVATE");

        when(reportRepository.findById(testReport.getId())).thenReturn(Optional.of(testReport));
        when(userRepository.findById(adminUser.getId())).thenReturn(Optional.of(adminUser));
        when(userRepository.findById(regularUser.getId())).thenReturn(Optional.of(regularUser));
        when(reportRepository.save(any(Report.class))).thenReturn(testReport);
        when(userRepository.save(any(User.class))).thenReturn(regularUser);

        // Act
        ReportDTO result = adminService.resolveReport(testReport.getId(), request, adminUser.getId());

        // Assert
        assertNotNull(result);
        assertEquals(UserStatus.DEACTIVATED, regularUser.getAccountStatus());
        verify(userRepository).save(regularUser);
    }

    @Test
    void resolveReport_WithNoAction_ShouldNotModifyUser() {
        // Arrange
        ResolveReportRequest request = new ResolveReportRequest();
        request.setStatus(ReportStatus.RESOLVED);
        request.setUserId(regularUser.getId());
        request.setAction("NO_ACTION");

        when(reportRepository.findById(testReport.getId())).thenReturn(Optional.of(testReport));
        when(userRepository.findById(adminUser.getId())).thenReturn(Optional.of(adminUser));
        when(userRepository.findById(regularUser.getId())).thenReturn(Optional.of(regularUser));
        when(reportRepository.save(any(Report.class))).thenReturn(testReport);

        // Act
        ReportDTO result = adminService.resolveReport(testReport.getId(), request, adminUser.getId());

        // Assert
        assertNotNull(result);
        assertEquals(UserStatus.ACTIVE, regularUser.getAccountStatus());
        verify(userRepository).save(regularUser);
    }

    @Test
    void resolveReport_WithInvalidAction_ShouldThrowException() {
        // Arrange
        ResolveReportRequest request = new ResolveReportRequest();
        request.setStatus(ReportStatus.RESOLVED);
        request.setUserId(regularUser.getId());
        request.setAction("INVALID_ACTION");

        when(reportRepository.findById(testReport.getId())).thenReturn(Optional.of(testReport));
        when(userRepository.findById(adminUser.getId())).thenReturn(Optional.of(adminUser));
        when(userRepository.findById(regularUser.getId())).thenReturn(Optional.of(regularUser));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            adminService.resolveReport(testReport.getId(), request, adminUser.getId());
        });

        assertEquals("Invalid action: INVALID_ACTION", exception.getMessage());
        verify(reportRepository, never()).save(any(Report.class));
    }

    @Test
    void resolveReport_WithNonAdminUser_ShouldThrowException() {
        // Arrange
        User nonAdminUser = new User();
        nonAdminUser.setId(3);
        nonAdminUser.setRole(UserRole.USER);

        ResolveReportRequest request = new ResolveReportRequest();
        request.setStatus(ReportStatus.RESOLVED);

        when(reportRepository.findById(testReport.getId())).thenReturn(Optional.of(testReport));
        when(userRepository.findById(nonAdminUser.getId())).thenReturn(Optional.of(nonAdminUser));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            adminService.resolveReport(testReport.getId(), request, nonAdminUser.getId());
        });

        assertEquals("Only admins can resolve reports", exception.getMessage());
        verify(reportRepository, never()).save(any(Report.class));
    }

    @Test
    void resolveReport_WithNonExistentReport_ShouldThrowException() {
        // Arrange
        ResolveReportRequest request = new ResolveReportRequest();
        request.setStatus(ReportStatus.RESOLVED);

        when(reportRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            adminService.resolveReport(999, request, adminUser.getId());
        });

        assertTrue(exception.getMessage().contains("Report not found"));
        verify(reportRepository, never()).save(any(Report.class));
    }

    // ==================== GET ALL REPORTS TESTS ====================

    @Test
    void getAllReports_WithValidAdmin_ShouldReturnAllReports() {
        // Arrange
        setupSecurityContext();
        List<Report> reports = Arrays.asList(testReport);
        when(reportRepository.findAll()).thenReturn(reports);

        // Act
        List<ReportDTO> result = adminService.getAllReports();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testReport.getId(), result.get(0).getId());
        verify(reportRepository).findAll();
    }

    @Test
    void getAllReports_WithEmptyRepository_ShouldReturnEmptyList() {
        // Arrange
        setupSecurityContext();
        when(reportRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<ReportDTO> result = adminService.getAllReports();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(reportRepository).findAll();
    }

    // ==================== GET REPORTS BY STATUS TESTS ====================

    @Test
    void getReportsByStatus_WithOpenStatus_ShouldReturnOpenReports() {
        // Arrange
        setupSecurityContext();
        List<Report> reports = Arrays.asList(testReport);
        when(reportRepository.findByStatus(ReportStatus.OPEN)).thenReturn(reports);

        // Act
        List<ReportDTO> result = adminService.getReportsByStatus(ReportStatus.OPEN);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(ReportStatus.OPEN, result.get(0).getStatus());
        verify(reportRepository).findByStatus(ReportStatus.OPEN);
    }

    @Test
    void getReportsByStatus_WithResolvedStatus_ShouldReturnResolvedReports() {
        // Arrange
        setupSecurityContext();
        testReport.setStatus(ReportStatus.RESOLVED);
        List<Report> reports = Arrays.asList(testReport);
        when(reportRepository.findByStatus(ReportStatus.RESOLVED)).thenReturn(reports);

        // Act
        List<ReportDTO> result = adminService.getReportsByStatus(ReportStatus.RESOLVED);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(ReportStatus.RESOLVED, result.get(0).getStatus());
        verify(reportRepository).findByStatus(ReportStatus.RESOLVED);
    }

    // ==================== GET REPORT BY ID TESTS ====================

    @Test
    void getReportById_WithValidId_ShouldReturnReport() {
        // Arrange
        setupSecurityContext();
        when(reportRepository.findById(testReport.getId())).thenReturn(Optional.of(testReport));

        // Act
        ReportDTO result = adminService.getReportById(testReport.getId());

        // Assert
        assertNotNull(result);
        assertEquals(testReport.getId(), result.getId());
        assertEquals(testReport.getReporter().getId(), result.getReporterId());
        assertEquals(testReport.getReportedUser().getId(), result.getReportedUserId());
        verify(reportRepository).findById(testReport.getId());
    }

    @Test
    void getReportById_WithInvalidId_ShouldThrowException() {
        // Arrange
        setupSecurityContext();
        when(reportRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            adminService.getReportById(999);
        });

        assertTrue(exception.getMessage().contains("Report not found"));
        verify(reportRepository).findById(999);
    }
}

