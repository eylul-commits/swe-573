package com.thehive.service;

import com.thehive.model.dto.AuthorDTO;
import com.thehive.model.dto.OfferDTO;
import com.thehive.model.dto.RequestDTO;
import com.thehive.model.dto.ServiceDTO;
import com.thehive.model.entity.*;
import com.thehive.model.enums.ItemStatus;
import com.thehive.repository.OfferRepository;
import com.thehive.repository.RequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MarketplaceServiceTest {

    @Mock
    private OfferRepository offerRepository;

    @Mock
    private RequestRepository requestRepository;

    @InjectMocks
    private MarketplaceService marketplaceService;

    private User testUser;
    private User testUserWithBadge;
    private Offer testOffer;
    private Request testRequest;
    private SemanticTag tag1;
    private SemanticTag tag2;

    @BeforeEach
    void setUp() {
        // Create test user without badges
        testUser = new User();
        testUser.setId(1);
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setBalanceHours(15);
        testUser.setUserBadges(new HashSet<>());

        // Create test user with badge
        testUserWithBadge = new User();
        testUserWithBadge.setId(2);
        testUserWithBadge.setName("Badged User");
        testUserWithBadge.setEmail("badged@example.com");
        testUserWithBadge.setBalanceHours(50);

        Badge badge = new Badge();
        badge.setId(1);
        badge.setName("Top Contributor");
        badge.setIconUrl("/images/badges/top-contributor.png");

        UserBadge userBadge = new UserBadge();
        userBadge.setId(new UserBadgeId(testUserWithBadge.getId(), badge.getId()));
        userBadge.setUser(testUserWithBadge);
        userBadge.setBadge(badge);
        userBadge.setEarnedAt(LocalDateTime.now());

        Set<UserBadge> userBadges = new HashSet<>();
        userBadges.add(userBadge);
        testUserWithBadge.setUserBadges(userBadges);

        // Create test tags
        tag1 = new SemanticTag();
        tag1.setId(1);
        tag1.setName("Cooking");
        tag1.setWikidataId("http://example.com/tags/cooking");

        tag2 = new SemanticTag();
        tag2.setId(2);
        tag2.setName("Teaching");
        tag2.setWikidataId("http://example.com/tags/teaching");

        // Create test offer
        testOffer = new Offer();
        testOffer.setId(1);
        testOffer.setProvider(testUser);
        testOffer.setTitle("Cooking Lessons");
        testOffer.setDescription("I can teach you how to cook Italian cuisine");
        testOffer.setDurationHours(2);
        testOffer.setStartDate(LocalDate.now());
        testOffer.setEndDate(LocalDate.now().plusMonths(1));
        testOffer.setProvince("Istanbul");
        testOffer.setDistrict("Kadikoy");
        testOffer.setStatus(ItemStatus.ACTIVE);
        testOffer.setCreatedAt(LocalDateTime.now());
        testOffer.setUpdatedAt(LocalDateTime.now());

        Set<SemanticTag> offerTags = new HashSet<>();
        offerTags.add(tag1);
        testOffer.setTags(offerTags);

        // Create test request
        testRequest = new Request();
        testRequest.setId(1);
        testRequest.setSeeker(testUserWithBadge);
        testRequest.setTitle("Math Tutoring Needed");
        testRequest.setDescription("Looking for help with calculus");
        testRequest.setDurationHours(3);
        testRequest.setStartDate(LocalDate.now());
        testRequest.setEndDate(LocalDate.now().plusWeeks(2));
        testRequest.setProvince("Ankara");
        testRequest.setDistrict("Cankaya");
        testRequest.setStatus(ItemStatus.ACTIVE);
        testRequest.setCreatedAt(LocalDateTime.now());
        testRequest.setUpdatedAt(LocalDateTime.now());

        Set<SemanticTag> requestTags = new HashSet<>();
        requestTags.add(tag2);
        testRequest.setTags(requestTags);
    }

    // ==================== GET ALL OFFERS TESTS ====================

    @Test
    void getAllOffers_ShouldReturnListOfOfferDTOs() {
        // Arrange
        when(offerRepository.findAll()).thenReturn(Arrays.asList(testOffer));

        // Act
        List<OfferDTO> result = marketplaceService.getAllOffers();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Cooking Lessons", result.get(0).getTitle());
        assertEquals("I can teach you how to cook Italian cuisine", result.get(0).getDescription());
        assertEquals(2, result.get(0).getDurationHours());
        assertEquals("Istanbul", result.get(0).getProvince());
        assertEquals("Kadikoy", result.get(0).getDistrict());
        assertEquals(ItemStatus.ACTIVE, result.get(0).getStatus());
        assertEquals("Test User", result.get(0).getProvider().getName());
        assertEquals(1, result.get(0).getTags().size());
        assertTrue(result.get(0).getTags().contains("Cooking"));
        verify(offerRepository, times(1)).findAll();
    }

    @Test
    void getAllOffers_ShouldHandleOffersWithMultipleTags() {
        // Arrange
        testOffer.getTags().add(tag2);
        when(offerRepository.findAll()).thenReturn(Arrays.asList(testOffer));

        // Act
        List<OfferDTO> result = marketplaceService.getAllOffers();

        // Assert
        assertEquals(2, result.get(0).getTags().size());
        assertTrue(result.get(0).getTags().contains("Cooking"));
        assertTrue(result.get(0).getTags().contains("Teaching"));
    }

    // ==================== GET ACTIVE OFFERS TESTS ====================

    @Test
    void getActiveOffers_ShouldReturnOnlyActiveOffers() {
        // Arrange
        when(offerRepository.findByStatus(ItemStatus.ACTIVE))
                .thenReturn(Arrays.asList(testOffer));

        // Act
        List<OfferDTO> result = marketplaceService.getActiveOffers();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(ItemStatus.ACTIVE, result.get(0).getStatus());
        verify(offerRepository, times(1)).findByStatus(ItemStatus.ACTIVE);
    }

    // ==================== GET OFFER BY ID TESTS ====================

    @Test
    void getOfferById_ShouldReturnOfferDTO_WhenOfferExists() {
        // Arrange
        when(offerRepository.findById(1)).thenReturn(Optional.of(testOffer));

        // Act
        OfferDTO result = marketplaceService.getOfferById(1);

        // Assert
        assertNotNull(result);
        assertEquals("Cooking Lessons", result.getTitle());
        assertEquals(1, result.getId());
        verify(offerRepository, times(1)).findById(1);
    }

    @Test
    void getOfferById_ShouldThrowException_WhenOfferNotFound() {
        // Arrange
        when(offerRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> marketplaceService.getOfferById(999));

        assertEquals("Offer not found with id: 999", exception.getMessage());
        verify(offerRepository, times(1)).findById(999);
    }

    // ==================== GET ALL REQUESTS TESTS ====================

    @Test
    void getAllRequests_ShouldReturnListOfRequestDTOs() {
        // Arrange
        when(requestRepository.findAll()).thenReturn(Arrays.asList(testRequest));

        // Act
        List<RequestDTO> result = marketplaceService.getAllRequests();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Math Tutoring Needed", result.get(0).getTitle());
        assertEquals("Looking for help with calculus", result.get(0).getDescription());
        assertEquals(3, result.get(0).getDurationHours());
        assertEquals("Ankara", result.get(0).getProvince());
        assertEquals("Cankaya", result.get(0).getDistrict());
        assertEquals(ItemStatus.ACTIVE, result.get(0).getStatus());
        assertEquals("Badged User", result.get(0).getSeeker().getName());
        assertEquals(1, result.get(0).getTags().size());
        assertTrue(result.get(0).getTags().contains("Teaching"));
        verify(requestRepository, times(1)).findAll();
    }

    // ==================== GET ACTIVE REQUESTS TESTS ====================

    @Test
    void getActiveRequests_ShouldReturnOnlyActiveRequests() {
        // Arrange
        when(requestRepository.findByStatus(ItemStatus.ACTIVE))
                .thenReturn(Arrays.asList(testRequest));

        // Act
        List<RequestDTO> result = marketplaceService.getActiveRequests();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(ItemStatus.ACTIVE, result.get(0).getStatus());
        verify(requestRepository, times(1)).findByStatus(ItemStatus.ACTIVE);
    }

    // ==================== GET REQUEST BY ID TESTS ====================

    @Test
    void getRequestById_ShouldReturnRequestDTO_WhenRequestExists() {
        // Arrange
        when(requestRepository.findById(1)).thenReturn(Optional.of(testRequest));

        // Act
        RequestDTO result = marketplaceService.getRequestById(1);

        // Assert
        assertNotNull(result);
        assertEquals("Math Tutoring Needed", result.getTitle());
        assertEquals(1, result.getId());
        verify(requestRepository, times(1)).findById(1);
    }

    @Test
    void getRequestById_ShouldThrowException_WhenRequestNotFound() {
        // Arrange
        when(requestRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> marketplaceService.getRequestById(999));

        assertEquals("Request not found with id: 999", exception.getMessage());
        verify(requestRepository, times(1)).findById(999);
    }

    // ==================== GET ALL SERVICES TESTS ====================

    @Test
    void getAllServices_ShouldReturnCombinedOffersAndRequests() {
        // Arrange
        when(offerRepository.findAll()).thenReturn(Arrays.asList(testOffer));
        when(requestRepository.findAll()).thenReturn(Arrays.asList(testRequest));

        // Act
        List<ServiceDTO> result = marketplaceService.getAllServices();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());

        // Check that we have both types
        long offerCount = result.stream().filter(s -> s.getType().equals("OFFER")).count();
        long requestCount = result.stream().filter(s -> s.getType().equals("REQUEST")).count();
        assertEquals(1, offerCount);
        assertEquals(1, requestCount);

        verify(offerRepository, times(1)).findAll();
        verify(requestRepository, times(1)).findAll();
    }

    // ==================== GET ACTIVE SERVICES TESTS ====================

    @Test
    void getActiveServices_ShouldReturnCombinedActiveOffersAndRequests() {
        // Arrange
        when(offerRepository.findByStatus(ItemStatus.ACTIVE))
                .thenReturn(Arrays.asList(testOffer));
        when(requestRepository.findByStatus(ItemStatus.ACTIVE))
                .thenReturn(Arrays.asList(testRequest));

        // Act
        List<ServiceDTO> result = marketplaceService.getActiveServices();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());

        // Verify all returned services are active
        result.forEach(service -> assertEquals("ACTIVE", service.getStatus()));

        verify(offerRepository, times(1)).findByStatus(ItemStatus.ACTIVE);
        verify(requestRepository, times(1)).findByStatus(ItemStatus.ACTIVE);
    }

    // ==================== DTO CONVERSION TESTS ====================

    @Test
    void convertOfferToServiceDTO_ShouldFormatLocation() {
        // Arrange
        when(offerRepository.findAll()).thenReturn(Arrays.asList(testOffer));

        // Act
        List<ServiceDTO> result = marketplaceService.getAllServices();

        // Assert
        ServiceDTO offer = result.stream()
                .filter(s -> s.getType().equals("OFFER"))
                .findFirst()
                .orElse(null);

        assertNotNull(offer);
        assertEquals("Kadikoy, Istanbul", offer.getLocation());
    }

    @Test
    void formatLocation_ShouldHandleNullProvince() {
        // Arrange
        testOffer.setProvince(null);
        testOffer.setDistrict("Kadikoy");
        when(offerRepository.findAll()).thenReturn(Arrays.asList(testOffer));

        // Act
        List<ServiceDTO> result = marketplaceService.getAllServices();

        // Assert
        ServiceDTO offer = result.get(0);
        assertEquals("Kadikoy", offer.getLocation());
    }

    @Test
    void formatLocation_ShouldHandleNullDistrict() {
        // Arrange
        testOffer.setProvince("Istanbul");
        testOffer.setDistrict(null);
        when(offerRepository.findAll()).thenReturn(Arrays.asList(testOffer));

        // Act
        List<ServiceDTO> result = marketplaceService.getAllServices();

        // Assert
        ServiceDTO offer = result.get(0);
        assertEquals("Istanbul", offer.getLocation());
    }

    @Test
    void formatLocation_ShouldReturnUnknownWhenBothNull() {
        // Arrange
        testOffer.setProvince(null);
        testOffer.setDistrict(null);
        when(offerRepository.findAll()).thenReturn(Arrays.asList(testOffer));

        // Act
        List<ServiceDTO> result = marketplaceService.getAllServices();

        // Assert
        ServiceDTO offer = result.get(0);
        assertEquals("Unknown", offer.getLocation());
    }

    @Test
    void convertToAuthorDTO_ShouldUseDefaultBadgeWhenNoBadges() {
        // Arrange
        when(offerRepository.findById(1)).thenReturn(Optional.of(testOffer));

        // Act
        OfferDTO result = marketplaceService.getOfferById(1);

        // Assert
        assertEquals("Newcomer", result.getProvider().getBadge());
    }

    @Test
    void convertToAuthorDTO_ShouldUseDatabaseBadgeWhenAvailable() {
        // Arrange
        when(requestRepository.findById(1)).thenReturn(Optional.of(testRequest));

        // Act
        RequestDTO result = marketplaceService.getRequestById(1);

        // Assert
        assertEquals("Top Contributor", result.getSeeker().getBadge());
    }

    @Test
    void convertToAuthorDTO_ShouldUseEmailWhenNameIsNull() {
        // Arrange
        testUser.setName(null);
        when(offerRepository.findById(1)).thenReturn(Optional.of(testOffer));

        // Act
        OfferDTO result = marketplaceService.getOfferById(1);

        // Assert
        assertEquals("test@example.com", result.getProvider().getName());
    }

    @Test
    void convertToServiceDTO_ShouldHandleNullStatus() {
        // Arrange
        testOffer.setStatus(null);
        when(offerRepository.findAll()).thenReturn(Arrays.asList(testOffer));

        // Act
        List<ServiceDTO> result = marketplaceService.getAllServices();

        // Assert
        ServiceDTO offer = result.get(0);
        assertEquals("ACTIVE", offer.getStatus());
    }

    // ==================== MULTIPLE ENTITIES TESTS ====================

    @Test
    void getAllServices_ShouldHandleMultipleOffersAndRequests() {
        // Arrange
        Offer offer2 = new Offer();
        offer2.setId(2);
        offer2.setProvider(testUser);
        offer2.setTitle("Second Offer");
        offer2.setDescription("Description");
        offer2.setDurationHours(1);
        offer2.setProvince("Izmir");
        offer2.setDistrict("Karsiyaka");
        offer2.setStatus(ItemStatus.ACTIVE);
        offer2.setCreatedAt(LocalDateTime.now());
        offer2.setUpdatedAt(LocalDateTime.now());
        offer2.setTags(new HashSet<>());

        Request request2 = new Request();
        request2.setId(2);
        request2.setSeeker(testUserWithBadge);
        request2.setTitle("Second Request");
        request2.setDescription("Description");
        request2.setDurationHours(2);
        request2.setProvince("Izmir");
        request2.setDistrict("Bornova");
        request2.setStatus(ItemStatus.ACTIVE);
        request2.setCreatedAt(LocalDateTime.now());
        request2.setUpdatedAt(LocalDateTime.now());
        request2.setTags(new HashSet<>());

        when(offerRepository.findAll()).thenReturn(Arrays.asList(testOffer, offer2));
        when(requestRepository.findAll()).thenReturn(Arrays.asList(testRequest, request2));

        // Act
        List<ServiceDTO> result = marketplaceService.getAllServices();

        // Assert
        assertEquals(4, result.size());
        
        long offerCount = result.stream().filter(s -> s.getType().equals("OFFER")).count();
        long requestCount = result.stream().filter(s -> s.getType().equals("REQUEST")).count();
        assertEquals(2, offerCount);
        assertEquals(2, requestCount);
    }
}

