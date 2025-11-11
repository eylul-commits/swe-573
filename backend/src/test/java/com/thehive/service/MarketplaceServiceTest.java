package com.thehive.service;

import com.thehive.exception.ResourceNotFoundException;
import com.thehive.model.dto.AuthorDTO;
import com.thehive.model.dto.CreateOfferRequest;
import com.thehive.model.dto.CreateRequestRequest;
import com.thehive.model.dto.OfferDTO;
import com.thehive.model.dto.RequestDTO;
import com.thehive.model.dto.ServiceDTO;
import com.thehive.model.entity.*;
import com.thehive.model.enums.ItemStatus;
import com.thehive.repository.OfferRepository;
import com.thehive.repository.QuestionRepository;
import com.thehive.repository.RatingRepository;
import com.thehive.repository.RequestRepository;
import com.thehive.repository.SemanticTagRepository;
import com.thehive.repository.UserRepository;
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

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private RatingRepository ratingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SemanticTagRepository semanticTagRepository;

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
        testOffer.setGeohash("sxk3");
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
        testRequest.setGeohash("syet");
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

    // ==================== CREATE OFFER TESTS ====================

    @Test
    void createOffer_ShouldSuccessfullyCreateOfferWithTags() {
        // Arrange
        CreateOfferRequest request = new CreateOfferRequest();
        request.setTitle("Guitar Lessons");
        request.setDescription("Learn to play guitar from beginner to advanced");
        request.setDurationHours(2);
        request.setStartDate(LocalDate.now());
        request.setEndDate(LocalDate.now().plusMonths(1));
        request.setProvince("Istanbul");
        request.setDistrict("Besiktas");
        request.setGeohash("sxk3uq9");
        request.setTags(Arrays.asList("Music", "Teaching"));

        Offer savedOffer = new Offer();
        savedOffer.setId(10);
        savedOffer.setProvider(testUser);
        savedOffer.setTitle(request.getTitle());
        savedOffer.setDescription(request.getDescription());
        savedOffer.setDurationHours(request.getDurationHours());
        savedOffer.setStartDate(request.getStartDate());
        savedOffer.setEndDate(request.getEndDate());
        savedOffer.setProvince(request.getProvince());
        savedOffer.setDistrict(request.getDistrict());
        savedOffer.setGeohash(request.getGeohash());
        savedOffer.setStatus(ItemStatus.ACTIVE);
        savedOffer.setCreatedAt(LocalDateTime.now());
        savedOffer.setUpdatedAt(LocalDateTime.now());

        SemanticTag musicTag = new SemanticTag();
        musicTag.setId(10);
        musicTag.setName("Music");

        SemanticTag teachingTag = new SemanticTag();
        teachingTag.setId(11);
        teachingTag.setName("Teaching");

        savedOffer.getTags().add(musicTag);
        savedOffer.getTags().add(teachingTag);

        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(semanticTagRepository.findByName("Music")).thenReturn(Optional.of(musicTag));
        when(semanticTagRepository.findByName("Teaching")).thenReturn(Optional.of(teachingTag));
        when(offerRepository.save(any(Offer.class))).thenReturn(savedOffer);

        // Act
        OfferDTO result = marketplaceService.createOffer(request, 1);

        // Assert
        assertNotNull(result);
        assertEquals(10, result.getId());
        assertEquals("Guitar Lessons", result.getTitle());
        assertEquals("Learn to play guitar from beginner to advanced", result.getDescription());
        assertEquals(2, result.getDurationHours());
        assertEquals("Istanbul", result.getProvince());
        assertEquals("Besiktas", result.getDistrict());
        assertEquals("sxk3uq9", result.getGeohash());
        assertEquals(ItemStatus.ACTIVE, result.getStatus());
        assertEquals("Test User", result.getProvider().getName());
        assertEquals(2, result.getTags().size());
        assertTrue(result.getTags().contains("Music"));
        assertTrue(result.getTags().contains("Teaching"));

        verify(userRepository, times(1)).findById(1);
        verify(semanticTagRepository, times(1)).findByName("Music");
        verify(semanticTagRepository, times(1)).findByName("Teaching");
        verify(offerRepository, times(1)).save(any(Offer.class));
    }

    @Test
    void createOffer_ShouldCreateNewTagsWhenNotExist() {
        // Arrange
        CreateOfferRequest request = new CreateOfferRequest();
        request.setTitle("Photography Sessions");
        request.setDescription("Professional photography service");
        request.setDurationHours(3);
        request.setStartDate(LocalDate.now());
        request.setEndDate(LocalDate.now().plusWeeks(2));
        request.setProvince("Izmir");
        request.setDistrict("Konak");
        request.setGeohash("sxk2uq3");
        request.setTags(Arrays.asList("Photography"));

        SemanticTag newTag = new SemanticTag();
        newTag.setId(20);
        newTag.setName("Photography");

        Offer savedOffer = new Offer();
        savedOffer.setId(11);
        savedOffer.setProvider(testUser);
        savedOffer.setTitle(request.getTitle());
        savedOffer.setDescription(request.getDescription());
        savedOffer.setDurationHours(request.getDurationHours());
        savedOffer.setStartDate(request.getStartDate());
        savedOffer.setEndDate(request.getEndDate());
        savedOffer.setProvince(request.getProvince());
        savedOffer.setDistrict(request.getDistrict());
        savedOffer.setGeohash(request.getGeohash());
        savedOffer.setStatus(ItemStatus.ACTIVE);
        savedOffer.setCreatedAt(LocalDateTime.now());
        savedOffer.setUpdatedAt(LocalDateTime.now());
        savedOffer.getTags().add(newTag);

        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(semanticTagRepository.findByName("Photography")).thenReturn(Optional.empty());
        when(semanticTagRepository.save(any(SemanticTag.class))).thenReturn(newTag);
        when(offerRepository.save(any(Offer.class))).thenReturn(savedOffer);

        // Act
        OfferDTO result = marketplaceService.createOffer(request, 1);

        // Assert
        assertNotNull(result);
        assertEquals("Photography Sessions", result.getTitle());
        assertEquals(1, result.getTags().size());
        assertTrue(result.getTags().contains("Photography"));

        verify(semanticTagRepository, times(1)).findByName("Photography");
        verify(semanticTagRepository, times(1)).save(any(SemanticTag.class));
        verify(offerRepository, times(1)).save(any(Offer.class));
    }

    @Test
    void createOffer_ShouldWorkWithoutTags() {
        // Arrange
        CreateOfferRequest request = new CreateOfferRequest();
        request.setTitle("Garden Help");
        request.setDescription("Help with gardening tasks");
        request.setDurationHours(4);
        request.setStartDate(LocalDate.now());
        request.setEndDate(LocalDate.now().plusDays(7));
        request.setProvince("Ankara");
        request.setDistrict("Cankaya");
        request.setGeohash("syethxk");
        request.setTags(null);

        Offer savedOffer = new Offer();
        savedOffer.setId(12);
        savedOffer.setProvider(testUser);
        savedOffer.setTitle(request.getTitle());
        savedOffer.setDescription(request.getDescription());
        savedOffer.setDurationHours(request.getDurationHours());
        savedOffer.setStartDate(request.getStartDate());
        savedOffer.setEndDate(request.getEndDate());
        savedOffer.setProvince(request.getProvince());
        savedOffer.setDistrict(request.getDistrict());
        savedOffer.setGeohash(request.getGeohash());
        savedOffer.setStatus(ItemStatus.ACTIVE);
        savedOffer.setCreatedAt(LocalDateTime.now());
        savedOffer.setUpdatedAt(LocalDateTime.now());

        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(offerRepository.save(any(Offer.class))).thenReturn(savedOffer);

        // Act
        OfferDTO result = marketplaceService.createOffer(request, 1);

        // Assert
        assertNotNull(result);
        assertEquals("Garden Help", result.getTitle());
        assertEquals(0, result.getTags().size());

        verify(userRepository, times(1)).findById(1);
        verify(semanticTagRepository, never()).findByName(anyString());
        verify(offerRepository, times(1)).save(any(Offer.class));
    }

    @Test
    void createOffer_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        CreateOfferRequest request = new CreateOfferRequest();
        request.setTitle("Test Offer");
        request.setDescription("Test Description");
        request.setDurationHours(2);
        request.setStartDate(LocalDate.now());
        request.setEndDate(LocalDate.now().plusDays(7));
        request.setProvince("Istanbul");
        request.setDistrict("Kadikoy");
        request.setGeohash("sxk3uq9");
        request.setTags(Arrays.asList("Test"));

        when(userRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> marketplaceService.createOffer(request, 999));

        assertEquals("User not found with id: 999", exception.getMessage());
        verify(userRepository, times(1)).findById(999);
        verify(offerRepository, never()).save(any(Offer.class));
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

    // ==================== CREATE REQUEST TESTS ====================

    @Test
    void createRequest_ShouldSuccessfullyCreateRequestWithTags() {
        // Arrange
        CreateRequestRequest request = new CreateRequestRequest();
        request.setTitle("Need Plumbing Help");
        request.setDescription("Looking for someone to fix a leaky pipe");
        request.setDurationHours(3);
        request.setStartDate(LocalDate.now());
        request.setEndDate(LocalDate.now().plusWeeks(1));
        request.setProvince("Istanbul");
        request.setDistrict("Besiktas");
        request.setGeohash("sxk3uq9");
        request.setTags(Arrays.asList("Plumbing", "Home Repair"));

        Request savedRequest = new Request();
        savedRequest.setId(10);
        savedRequest.setSeeker(testUser);
        savedRequest.setTitle(request.getTitle());
        savedRequest.setDescription(request.getDescription());
        savedRequest.setDurationHours(request.getDurationHours());
        savedRequest.setStartDate(request.getStartDate());
        savedRequest.setEndDate(request.getEndDate());
        savedRequest.setProvince(request.getProvince());
        savedRequest.setDistrict(request.getDistrict());
        savedRequest.setGeohash(request.getGeohash());
        savedRequest.setStatus(ItemStatus.ACTIVE);
        savedRequest.setCreatedAt(LocalDateTime.now());
        savedRequest.setUpdatedAt(LocalDateTime.now());

        SemanticTag plumbingTag = new SemanticTag();
        plumbingTag.setId(15);
        plumbingTag.setName("Plumbing");

        SemanticTag repairTag = new SemanticTag();
        repairTag.setId(16);
        repairTag.setName("Home Repair");

        savedRequest.getTags().add(plumbingTag);
        savedRequest.getTags().add(repairTag);

        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(semanticTagRepository.findByName("Plumbing")).thenReturn(Optional.of(plumbingTag));
        when(semanticTagRepository.findByName("Home Repair")).thenReturn(Optional.of(repairTag));
        when(requestRepository.save(any(Request.class))).thenReturn(savedRequest);

        // Act
        RequestDTO result = marketplaceService.createRequest(request, 1);

        // Assert
        assertNotNull(result);
        assertEquals(10, result.getId());
        assertEquals("Need Plumbing Help", result.getTitle());
        assertEquals("Looking for someone to fix a leaky pipe", result.getDescription());
        assertEquals(3, result.getDurationHours());
        assertEquals("Istanbul", result.getProvince());
        assertEquals("Besiktas", result.getDistrict());
        assertEquals("sxk3uq9", result.getGeohash());
        assertEquals(ItemStatus.ACTIVE, result.getStatus());
        assertEquals("Test User", result.getSeeker().getName());
        assertEquals(2, result.getTags().size());
        assertTrue(result.getTags().contains("Plumbing"));
        assertTrue(result.getTags().contains("Home Repair"));

        verify(userRepository, times(1)).findById(1);
        verify(semanticTagRepository, times(1)).findByName("Plumbing");
        verify(semanticTagRepository, times(1)).findByName("Home Repair");
        verify(requestRepository, times(1)).save(any(Request.class));
    }

    @Test
    void createRequest_ShouldCreateNewTagsWhenNotExist() {
        // Arrange
        CreateRequestRequest request = new CreateRequestRequest();
        request.setTitle("Need Babysitter");
        request.setDescription("Looking for childcare help");
        request.setDurationHours(4);
        request.setStartDate(LocalDate.now());
        request.setEndDate(LocalDate.now().plusDays(5));
        request.setProvince("Ankara");
        request.setDistrict("Cankaya");
        request.setGeohash("syethxk");
        request.setTags(Arrays.asList("Childcare"));

        SemanticTag newTag = new SemanticTag();
        newTag.setId(25);
        newTag.setName("Childcare");

        Request savedRequest = new Request();
        savedRequest.setId(11);
        savedRequest.setSeeker(testUser);
        savedRequest.setTitle(request.getTitle());
        savedRequest.setDescription(request.getDescription());
        savedRequest.setDurationHours(request.getDurationHours());
        savedRequest.setStartDate(request.getStartDate());
        savedRequest.setEndDate(request.getEndDate());
        savedRequest.setProvince(request.getProvince());
        savedRequest.setDistrict(request.getDistrict());
        savedRequest.setGeohash(request.getGeohash());
        savedRequest.setStatus(ItemStatus.ACTIVE);
        savedRequest.setCreatedAt(LocalDateTime.now());
        savedRequest.setUpdatedAt(LocalDateTime.now());
        savedRequest.getTags().add(newTag);

        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(semanticTagRepository.findByName("Childcare")).thenReturn(Optional.empty());
        when(semanticTagRepository.save(any(SemanticTag.class))).thenReturn(newTag);
        when(requestRepository.save(any(Request.class))).thenReturn(savedRequest);

        // Act
        RequestDTO result = marketplaceService.createRequest(request, 1);

        // Assert
        assertNotNull(result);
        assertEquals("Need Babysitter", result.getTitle());
        assertEquals(1, result.getTags().size());
        assertTrue(result.getTags().contains("Childcare"));

        verify(semanticTagRepository, times(1)).findByName("Childcare");
        verify(semanticTagRepository, times(1)).save(any(SemanticTag.class));
        verify(requestRepository, times(1)).save(any(Request.class));
    }

    @Test
    void createRequest_ShouldWorkWithoutTags() {
        // Arrange
        CreateRequestRequest request = new CreateRequestRequest();
        request.setTitle("Moving Help");
        request.setDescription("Need help moving furniture");
        request.setDurationHours(5);
        request.setStartDate(LocalDate.now());
        request.setEndDate(LocalDate.now().plusDays(2));
        request.setProvince("Izmir");
        request.setDistrict("Konak");
        request.setGeohash("sxk2uq3");
        request.setTags(null);

        Request savedRequest = new Request();
        savedRequest.setId(12);
        savedRequest.setSeeker(testUser);
        savedRequest.setTitle(request.getTitle());
        savedRequest.setDescription(request.getDescription());
        savedRequest.setDurationHours(request.getDurationHours());
        savedRequest.setStartDate(request.getStartDate());
        savedRequest.setEndDate(request.getEndDate());
        savedRequest.setProvince(request.getProvince());
        savedRequest.setDistrict(request.getDistrict());
        savedRequest.setGeohash(request.getGeohash());
        savedRequest.setStatus(ItemStatus.ACTIVE);
        savedRequest.setCreatedAt(LocalDateTime.now());
        savedRequest.setUpdatedAt(LocalDateTime.now());

        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(requestRepository.save(any(Request.class))).thenReturn(savedRequest);

        // Act
        RequestDTO result = marketplaceService.createRequest(request, 1);

        // Assert
        assertNotNull(result);
        assertEquals("Moving Help", result.getTitle());
        assertEquals(0, result.getTags().size());

        verify(userRepository, times(1)).findById(1);
        verify(semanticTagRepository, never()).findByName(anyString());
        verify(requestRepository, times(1)).save(any(Request.class));
    }

    @Test
    void createRequest_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        CreateRequestRequest request = new CreateRequestRequest();
        request.setTitle("Test Request");
        request.setDescription("Test Description");
        request.setDurationHours(2);
        request.setStartDate(LocalDate.now());
        request.setEndDate(LocalDate.now().plusDays(7));
        request.setProvince("Istanbul");
        request.setDistrict("Kadikoy");
        request.setGeohash("sxk3uq9");
        request.setTags(Arrays.asList("Test"));

        when(userRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> marketplaceService.createRequest(request, 999));

        assertEquals("User not found with id: 999", exception.getMessage());
        verify(userRepository, times(1)).findById(999);
        verify(requestRepository, never()).save(any(Request.class));
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

    // ==================== GET SERVICE BY ID (UNIFIED) TESTS ====================

    @Test
    void getServiceById_ShouldReturnOfferAsServiceDTO_WhenOfferExists() {
        // Arrange
        when(offerRepository.findById(1)).thenReturn(Optional.of(testOffer));

        // Act
        ServiceDTO result = marketplaceService.getServiceById(1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("OFFER", result.getType());
        assertEquals("Cooking Lessons", result.getTitle());
        assertEquals("I can teach you how to cook Italian cuisine", result.getDescription());
        assertEquals(2, result.getTimebank());
        assertEquals("Kadikoy, Istanbul", result.getLocation());
        assertEquals("ACTIVE", result.getStatus());
        
        // Verify repositories were called correctly
        verify(offerRepository, times(1)).findById(1);
        verify(requestRepository, never()).findById(any());
    }

    @Test
    void getServiceById_ShouldReturnRequestAsServiceDTO_WhenOnlyRequestExists() {
        // Arrange
        when(offerRepository.findById(1)).thenReturn(Optional.empty());
        when(requestRepository.findById(1)).thenReturn(Optional.of(testRequest));

        // Act
        ServiceDTO result = marketplaceService.getServiceById(1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("REQUEST", result.getType());
        assertEquals("Math Tutoring Needed", result.getTitle());
        assertEquals("Looking for help with calculus", result.getDescription());
        assertEquals(3, result.getTimebank());
        assertEquals("Cankaya, Ankara", result.getLocation());
        assertEquals("ACTIVE", result.getStatus());
        
        // Verify both repositories were called in correct order
        verify(offerRepository, times(1)).findById(1);
        verify(requestRepository, times(1)).findById(1);
    }

    @Test
    void getServiceById_ShouldThrowResourceNotFoundException_WhenNeitherExists() {
        // Arrange
        when(offerRepository.findById(999)).thenReturn(Optional.empty());
        when(requestRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> marketplaceService.getServiceById(999));

        assertEquals("Service not found with id: 999", exception.getMessage());
        
        // Verify both repositories were checked
        verify(offerRepository, times(1)).findById(999);
        verify(requestRepository, times(1)).findById(999);
    }

    @Test
    void getServiceById_ShouldPrioritizeOffer_WhenBothOfferAndRequestExistWithSameId() {
        // This is an edge case - in practice IDs should be unique across offers and requests
        // But our implementation checks offers first, so let's verify that behavior
        
        // Arrange
        when(offerRepository.findById(1)).thenReturn(Optional.of(testOffer));
        // Note: Not stubbing requestRepository because it won't be called due to short-circuit

        // Act
        ServiceDTO result = marketplaceService.getServiceById(1);

        // Assert
        assertNotNull(result);
        assertEquals("OFFER", result.getType());
        assertEquals("Cooking Lessons", result.getTitle());
        
        // Verify only offer repository was called (short-circuit behavior)
        verify(offerRepository, times(1)).findById(1);
        verify(requestRepository, never()).findById(any());
    }

    @Test
    void getServiceById_ShouldConvertOfferWithAllFields() {
        // Arrange
        when(offerRepository.findById(1)).thenReturn(Optional.of(testOffer));

        // Act
        ServiceDTO result = marketplaceService.getServiceById(1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("OFFER", result.getType());
        assertEquals("Cooking Lessons", result.getTitle());
        assertEquals("I can teach you how to cook Italian cuisine", result.getDescription());
        assertEquals(2, result.getTimebank());
        assertEquals("Istanbul", result.getProvince());
        assertEquals("Kadikoy", result.getDistrict());
        assertEquals("sxk3", result.getGeohash());
        assertEquals("ACTIVE", result.getStatus());
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());
        
        // Check poster
        assertNotNull(result.getPoster());
        assertEquals(1, result.getPoster().getId());
        assertEquals("Test User", result.getPoster().getName());
        assertEquals("Newcomer", result.getPoster().getBadge());
        
        // Check tags
        assertEquals(1, result.getTags().size());
        assertTrue(result.getTags().contains("Cooking"));
    }

    @Test
    void getServiceById_ShouldConvertRequestWithAllFields() {
        // Arrange
        when(offerRepository.findById(1)).thenReturn(Optional.empty());
        when(requestRepository.findById(1)).thenReturn(Optional.of(testRequest));

        // Act
        ServiceDTO result = marketplaceService.getServiceById(1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("REQUEST", result.getType());
        assertEquals("Math Tutoring Needed", result.getTitle());
        assertEquals("Looking for help with calculus", result.getDescription());
        assertEquals(3, result.getTimebank());
        assertEquals("Ankara", result.getProvince());
        assertEquals("Cankaya", result.getDistrict());
        assertEquals("syet", result.getGeohash());
        assertEquals("ACTIVE", result.getStatus());
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());
        
        // Check poster (seeker for requests)
        assertNotNull(result.getPoster());
        assertEquals(2, result.getPoster().getId());
        assertEquals("Badged User", result.getPoster().getName());
        assertEquals("Top Contributor", result.getPoster().getBadge());
        
        // Check tags
        assertEquals(1, result.getTags().size());
        assertTrue(result.getTags().contains("Teaching"));
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
        offer2.setGeohash("sxnf");
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
        request2.setGeohash("sxng");
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

