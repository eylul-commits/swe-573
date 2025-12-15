package com.thehive.service;

import com.thehive.exception.ResourceNotFoundException;
import com.thehive.model.dto.AuthorDTO;
import com.thehive.model.dto.CreateOfferRequest;
import com.thehive.model.dto.CreateRequestRequest;
import com.thehive.model.dto.CreateQuestionRequest;
import com.thehive.model.dto.CreateAnswerRequest;
import com.thehive.model.dto.ServiceAnswerDTO;
import com.thehive.model.dto.OfferDTO;
import com.thehive.model.dto.RequestDTO;
import com.thehive.model.dto.ServiceDTO;
import com.thehive.model.dto.ServiceQuestionDTO;
import com.thehive.model.dto.ServiceRatingsResponseDTO;
import com.thehive.model.entity.*;
import com.thehive.model.enums.HandshakeStatus;
import com.thehive.model.enums.ItemStatus;
import com.thehive.repository.HandshakeRepository;
import com.thehive.repository.OfferRepository;
import com.thehive.repository.QuestionRepository;
import com.thehive.repository.AnswerRepository;
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
    private AnswerRepository answerRepository;

    @Mock
    private RatingRepository ratingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SemanticTagRepository semanticTagRepository;

    @Mock
    private HandshakeRepository handshakeRepository;

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

    @Test
    void createRequest_ShouldThrowException_WhenInsufficientBalance() {
        // Arrange
        User userWithLowBalance = new User();
        userWithLowBalance.setId(5);
        userWithLowBalance.setName("Low Balance User");
        userWithLowBalance.setEmail("lowbalance@example.com");
        userWithLowBalance.setBalanceHours(2); // User has only 2 hours
        userWithLowBalance.setUserBadges(new HashSet<>());

        CreateRequestRequest request = new CreateRequestRequest();
        request.setTitle("Need Expert Help");
        request.setDescription("Looking for extensive consultation");
        request.setDurationHours(5); // Request requires 5 hours
        request.setStartDate(LocalDate.now());
        request.setEndDate(LocalDate.now().plusDays(7));
        request.setProvince("Istanbul");
        request.setDistrict("Kadikoy");
        request.setGeohash("sxk3uq9");
        request.setTags(Arrays.asList("Consulting"));

        when(userRepository.findById(5)).thenReturn(Optional.of(userWithLowBalance));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> marketplaceService.createRequest(request, 5));

        assertEquals("Insufficient timebank balance. You have 2 hours but this request requires 5 hours.", 
                     exception.getMessage());
        verify(userRepository, times(1)).findById(5);
        verify(requestRepository, never()).save(any(Request.class));
    }

    @Test
    void createRequest_ShouldSucceed_WhenBalanceIsExactlyEnough() {
        // Arrange
        User userWithExactBalance = new User();
        userWithExactBalance.setId(6);
        userWithExactBalance.setName("Exact Balance User");
        userWithExactBalance.setEmail("exact@example.com");
        userWithExactBalance.setBalanceHours(5); // User has exactly 5 hours
        userWithExactBalance.setUserBadges(new HashSet<>());

        CreateRequestRequest request = new CreateRequestRequest();
        request.setTitle("Need Help");
        request.setDescription("Looking for assistance");
        request.setDurationHours(5); // Request requires exactly 5 hours
        request.setStartDate(LocalDate.now());
        request.setEndDate(LocalDate.now().plusDays(7));
        request.setProvince("Istanbul");
        request.setDistrict("Kadikoy");
        request.setGeohash("sxk3uq9");
        request.setTags(null);

        Request savedRequest = new Request();
        savedRequest.setId(20);
        savedRequest.setSeeker(userWithExactBalance);
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

        when(userRepository.findById(6)).thenReturn(Optional.of(userWithExactBalance));
        when(requestRepository.save(any(Request.class))).thenReturn(savedRequest);

        // Act
        RequestDTO result = marketplaceService.createRequest(request, 6);

        // Assert
        assertNotNull(result);
        assertEquals("Need Help", result.getTitle());
        assertEquals(5, result.getDurationHours());
        verify(userRepository, times(1)).findById(6);
        verify(requestRepository, times(1)).save(any(Request.class));
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

        // Called twice: once in expireOldServices(), once in getActiveServices()
        verify(offerRepository, times(2)).findByStatus(ItemStatus.ACTIVE);
        verify(requestRepository, times(2)).findByStatus(ItemStatus.ACTIVE);
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

    // ==================== GET QUESTIONS FOR SERVICE TESTS ====================

    @Test
    void getQuestionsForService_ShouldReturnQuestionsForOffer() {
        Question question = new Question();
        question.setContent("Test question");
        question.setAsker(testUser);

        when(offerRepository.existsById(1)).thenReturn(true);
        when(questionRepository.findByOfferId(1)).thenReturn(Arrays.asList(question));

        List<ServiceQuestionDTO> result = marketplaceService.getQuestionsForService(1);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getQuestionsForService_ShouldReturnQuestionsForRequest() {
        Question question = new Question();
        question.setContent("Test question");
        question.setAsker(testUser);

        when(offerRepository.existsById(1)).thenReturn(false);
        when(requestRepository.existsById(1)).thenReturn(true);
        when(questionRepository.findByRequestId(1)).thenReturn(Arrays.asList(question));

        List<ServiceQuestionDTO> result = marketplaceService.getQuestionsForService(1);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getQuestionsForService_ShouldThrowException_WhenServiceNotFound() {
        when(offerRepository.existsById(999)).thenReturn(false);
        when(requestRepository.existsById(999)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
            () -> marketplaceService.getQuestionsForService(999));
    }

    // ==================== GET RATINGS FOR SERVICE TESTS ====================

    @Test
    void getRatingsForService_ShouldReturnRatingsForOffer() {
        Rating rating = new Rating();
        rating.setId(1);
        rating.setRater(testUser);
        rating.setPunctuality(5);
        rating.setFriendliness(4);
        rating.setCommunicative(5);
        rating.setPreparedness(4);
        rating.setCreatedAt(LocalDateTime.now());

        when(offerRepository.existsById(1)).thenReturn(true);
        when(offerRepository.findById(1)).thenReturn(Optional.of(testOffer));
        when(ratingRepository.findByHandshakeOfferId(1)).thenReturn(Arrays.asList(rating));

        ServiceRatingsResponseDTO result = marketplaceService.getRatingsForService(1);

        assertNotNull(result);
        assertEquals(1, result.getRatings().size());
        assertEquals(5.0, result.getSummary().getPunctuality());
        
        // Verify service information is included in the rating DTO
        assertNotNull(result.getRatings().get(0).getServiceId());
        assertEquals(testOffer.getId(), result.getRatings().get(0).getServiceId());
        assertNotNull(result.getRatings().get(0).getServiceTitle());
        assertEquals("Cooking Lessons", result.getRatings().get(0).getServiceTitle());
    }

    @Test
    void getRatingsForService_ShouldReturnRatingsForRequest() {
        Rating rating = new Rating();
        rating.setId(2);
        rating.setRater(testUser);
        rating.setPunctuality(4);
        rating.setFriendliness(5);
        rating.setCommunicative(4);
        rating.setPreparedness(5);
        rating.setCreatedAt(LocalDateTime.now());

        when(offerRepository.existsById(1)).thenReturn(false);
        when(requestRepository.existsById(1)).thenReturn(true);
        when(requestRepository.findById(1)).thenReturn(Optional.of(testRequest));
        when(ratingRepository.findByHandshakeRequestId(1)).thenReturn(Arrays.asList(rating));

        ServiceRatingsResponseDTO result = marketplaceService.getRatingsForService(1);

        assertNotNull(result);
        assertEquals(1, result.getRatings().size());
        assertEquals(4.0, result.getSummary().getPunctuality());
        
        // Verify service information is included in the rating DTO
        assertNotNull(result.getRatings().get(0).getServiceId());
        assertEquals(testRequest.getId(), result.getRatings().get(0).getServiceId());
        assertNotNull(result.getRatings().get(0).getServiceTitle());
        assertEquals("Math Tutoring Needed", result.getRatings().get(0).getServiceTitle());
    }

    @Test
    void getRatingsForService_ShouldReturnEmptyForRequestWithNoRatings() {
        when(offerRepository.existsById(1)).thenReturn(false);
        when(requestRepository.existsById(1)).thenReturn(true);
        when(requestRepository.findById(1)).thenReturn(Optional.of(testRequest));
        when(ratingRepository.findByHandshakeRequestId(1)).thenReturn(Collections.emptyList());

        ServiceRatingsResponseDTO result = marketplaceService.getRatingsForService(1);

        assertNotNull(result);
        assertEquals(0, result.getRatings().size());
        assertEquals(0.0, result.getSummary().getPunctuality());
    }

    @Test
    void getRatingsForService_ShouldFilterOutRequestOwnerRatings() {
        // Create a rating from the request owner (testUserWithBadge)
        Rating ratingFromOwner = new Rating();
        ratingFromOwner.setId(1);
        ratingFromOwner.setRater(testUserWithBadge); // testUserWithBadge is the request owner
        ratingFromOwner.setPunctuality(5);
        ratingFromOwner.setFriendliness(5);
        ratingFromOwner.setCommunicative(5);
        ratingFromOwner.setPreparedness(5);
        ratingFromOwner.setCreatedAt(LocalDateTime.now());

        // Create a rating from a provider (not the owner)
        Rating ratingFromProvider = new Rating();
        ratingFromProvider.setId(2);
        ratingFromProvider.setRater(testUser); // testUser is not the owner
        ratingFromProvider.setPunctuality(4);
        ratingFromProvider.setFriendliness(4);
        ratingFromProvider.setCommunicative(4);
        ratingFromProvider.setPreparedness(4);
        ratingFromProvider.setCreatedAt(LocalDateTime.now());

        when(offerRepository.existsById(1)).thenReturn(false);
        when(requestRepository.existsById(1)).thenReturn(true);
        when(requestRepository.findById(1)).thenReturn(Optional.of(testRequest));
        when(ratingRepository.findByHandshakeRequestId(1)).thenReturn(Arrays.asList(ratingFromOwner, ratingFromProvider));

        ServiceRatingsResponseDTO result = marketplaceService.getRatingsForService(1);

        // Should only return the rating from the provider, not from the owner
        assertNotNull(result);
        assertEquals(1, result.getRatings().size());
        assertEquals(4.0, result.getSummary().getPunctuality());
        assertEquals(testUser.getId(), result.getRatings().get(0).getRater().getId());
    }

    @Test
    void getRatingsForService_ShouldThrowException_WhenServiceNotFound() {
        when(offerRepository.existsById(999)).thenReturn(false);
        when(requestRepository.existsById(999)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
            () -> marketplaceService.getRatingsForService(999));
    }

    // ==================== GET OFFERS BY PROVIDER TESTS ====================

    @Test
    void getOffersByProvider_ShouldReturnOffers() {
        when(offerRepository.findByProviderId(1)).thenReturn(Arrays.asList(testOffer));

        List<OfferDTO> result = marketplaceService.getOffersByProvider(1);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    // ==================== GET REQUESTS BY SEEKER TESTS ====================

    @Test
    void getRequestsBySeeker_ShouldReturnRequests() {
        when(requestRepository.findBySeekerId(2)).thenReturn(Arrays.asList(testRequest));

        List<RequestDTO> result = marketplaceService.getRequestsBySeeker(2);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    // ==================== GET USER SERVICES TESTS ====================

    @Test
    void getUserServices_ShouldReturnCombinedOffersAndRequests() {
        when(offerRepository.findByProviderId(1)).thenReturn(Arrays.asList(testOffer));
        when(requestRepository.findBySeekerId(1)).thenReturn(Arrays.asList(testRequest));

        List<ServiceDTO> result = marketplaceService.getUserServices(1);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1, result.stream().filter(s -> "OFFER".equals(s.getType())).count());
        assertEquals(1, result.stream().filter(s -> "REQUEST".equals(s.getType())).count());
    }

    @Test
    void getUserServices_ShouldReturnOnlyOffers() {
        when(offerRepository.findByProviderId(1)).thenReturn(Arrays.asList(testOffer));
        when(requestRepository.findBySeekerId(1)).thenReturn(Collections.emptyList());

        List<ServiceDTO> result = marketplaceService.getUserServices(1);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("OFFER", result.get(0).getType());
    }

    @Test
    void getUserServices_ShouldReturnEmptyList() {
        when(offerRepository.findByProviderId(1)).thenReturn(Collections.emptyList());
        when(requestRepository.findBySeekerId(1)).thenReturn(Collections.emptyList());

        List<ServiceDTO> result = marketplaceService.getUserServices(1);

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    // ==================== CREATE QUESTION FOR SERVICE TESTS ====================

    @Test
    void createQuestionForService_ShouldCreateQuestionForOffer() {
        // Arrange
        Integer serviceId = 1;
        Integer askerId = 2;
        CreateQuestionRequest request = new CreateQuestionRequest();
        request.setContent("Is this offer still available?");

        User asker = new User();
        asker.setId(askerId);
        asker.setName("Question Asker");
        asker.setEmail("asker@example.com");
        asker.setUserBadges(new HashSet<>());

        Question savedQuestion = new Question();
        savedQuestion.setId(1);
        savedQuestion.setAsker(asker);
        savedQuestion.setContent(request.getContent());
        savedQuestion.setOffer(testOffer);
        savedQuestion.setCreatedAt(LocalDateTime.now());

        when(userRepository.findById(askerId)).thenReturn(Optional.of(asker));
        when(offerRepository.findById(serviceId)).thenReturn(Optional.of(testOffer));
        when(questionRepository.save(any(Question.class))).thenReturn(savedQuestion);

        // Act
        ServiceQuestionDTO result = marketplaceService.createQuestionForService(serviceId, request, askerId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("Is this offer still available?", result.getContent());
        assertNotNull(result.getAuthor());
        assertEquals("Question Asker", result.getAuthor().getName());
        assertNull(result.getAnswer());

        verify(userRepository, times(1)).findById(askerId);
        verify(offerRepository, times(1)).findById(serviceId);
        verify(questionRepository, times(1)).save(any(Question.class));
    }

    @Test
    void createQuestionForService_ShouldCreateQuestionForRequest() {
        // Arrange
        Integer serviceId = 1;
        Integer askerId = 2;
        CreateQuestionRequest request = new CreateQuestionRequest();
        request.setContent("Can this be done online?");

        User asker = new User();
        asker.setId(askerId);
        asker.setName("Question Asker");
        asker.setEmail("asker@example.com");
        asker.setUserBadges(new HashSet<>());

        Question savedQuestion = new Question();
        savedQuestion.setId(1);
        savedQuestion.setAsker(asker);
        savedQuestion.setContent(request.getContent());
        savedQuestion.setRequest(testRequest);
        savedQuestion.setCreatedAt(LocalDateTime.now());

        when(userRepository.findById(askerId)).thenReturn(Optional.of(asker));
        when(offerRepository.findById(serviceId)).thenReturn(Optional.empty());
        when(requestRepository.findById(serviceId)).thenReturn(Optional.of(testRequest));
        when(questionRepository.save(any(Question.class))).thenReturn(savedQuestion);

        // Act
        ServiceQuestionDTO result = marketplaceService.createQuestionForService(serviceId, request, askerId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("Can this be done online?", result.getContent());
        assertNotNull(result.getAuthor());
        assertEquals("Question Asker", result.getAuthor().getName());

        verify(userRepository, times(1)).findById(askerId);
        verify(offerRepository, times(1)).findById(serviceId);
        verify(requestRepository, times(1)).findById(serviceId);
        verify(questionRepository, times(1)).save(any(Question.class));
    }

    @Test
    void createQuestionForService_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        Integer serviceId = 1;
        Integer askerId = 999;
        CreateQuestionRequest request = new CreateQuestionRequest();
        request.setContent("Test question");

        when(userRepository.findById(askerId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            marketplaceService.createQuestionForService(serviceId, request, askerId);
        });

        verify(userRepository, times(1)).findById(askerId);
        verify(offerRepository, never()).findById(any());
        verify(questionRepository, never()).save(any());
    }

    @Test
    void createQuestionForService_ShouldThrowException_WhenServiceNotFound() {
        // Arrange
        Integer serviceId = 999;
        Integer askerId = 2;
        CreateQuestionRequest request = new CreateQuestionRequest();
        request.setContent("Test question");

        User asker = new User();
        asker.setId(askerId);
        asker.setUserBadges(new HashSet<>());

        when(userRepository.findById(askerId)).thenReturn(Optional.of(asker));
        when(offerRepository.findById(serviceId)).thenReturn(Optional.empty());
        when(requestRepository.findById(serviceId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            marketplaceService.createQuestionForService(serviceId, request, askerId);
        });

        verify(userRepository, times(1)).findById(askerId);
        verify(offerRepository, times(1)).findById(serviceId);
        verify(requestRepository, times(1)).findById(serviceId);
        verify(questionRepository, never()).save(any());
    }

    // ==================== CREATE ANSWER FOR QUESTION TESTS ====================

    @Test
    void createAnswerForQuestion_ShouldCreateAnswer() {
        // Arrange
        Integer questionId = 1;
        Integer responderId = 2;
        CreateAnswerRequest request = new CreateAnswerRequest();
        request.setContent("Yes, it's still available!");

        User responder = new User();
        responder.setId(responderId);
        responder.setName("Service Provider");
        responder.setEmail("provider@example.com");
        responder.setUserBadges(new HashSet<>());

        Question question = new Question();
        question.setId(questionId);
        question.setAsker(testUser);
        question.setContent("Is this offer still available?");
        question.setOffer(testOffer);
        question.setAnswer(null); // No answer yet

        Answer savedAnswer = new Answer();
        savedAnswer.setId(1);
        savedAnswer.setQuestion(question);
        savedAnswer.setResponder(responder);
        savedAnswer.setContent(request.getContent());
        savedAnswer.setCreatedAt(LocalDateTime.now());

        when(questionRepository.findById(questionId)).thenReturn(Optional.of(question));
        when(userRepository.findById(responderId)).thenReturn(Optional.of(responder));
        when(answerRepository.save(any(Answer.class))).thenReturn(savedAnswer);

        // Act
        ServiceAnswerDTO result = marketplaceService.createAnswerForQuestion(questionId, request, responderId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("Yes, it's still available!", result.getContent());
        assertNotNull(result.getResponder());
        assertEquals("Service Provider", result.getResponder().getName());

        verify(questionRepository, times(1)).findById(questionId);
        verify(userRepository, times(1)).findById(responderId);
        verify(answerRepository, times(1)).save(any(Answer.class));
    }

    @Test
    void createAnswerForQuestion_ShouldThrowException_WhenQuestionNotFound() {
        // Arrange
        Integer questionId = 999;
        Integer responderId = 2;
        CreateAnswerRequest request = new CreateAnswerRequest();
        request.setContent("Test answer");

        when(questionRepository.findById(questionId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            marketplaceService.createAnswerForQuestion(questionId, request, responderId);
        });

        verify(questionRepository, times(1)).findById(questionId);
        verify(userRepository, never()).findById(any());
        verify(answerRepository, never()).save(any());
    }

    @Test
    void createAnswerForQuestion_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        Integer questionId = 1;
        Integer responderId = 999;
        CreateAnswerRequest request = new CreateAnswerRequest();
        request.setContent("Test answer");

        Question question = new Question();
        question.setId(questionId);
        question.setAnswer(null);

        when(questionRepository.findById(questionId)).thenReturn(Optional.of(question));
        when(userRepository.findById(responderId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            marketplaceService.createAnswerForQuestion(questionId, request, responderId);
        });

        verify(questionRepository, times(1)).findById(questionId);
        verify(userRepository, times(1)).findById(responderId);
        verify(answerRepository, never()).save(any());
    }

    @Test
    void createAnswerForQuestion_ShouldThrowException_WhenQuestionAlreadyAnswered() {
        // Arrange
        Integer questionId = 1;
        Integer responderId = 2;
        CreateAnswerRequest request = new CreateAnswerRequest();
        request.setContent("Another answer");

        User existingResponder = new User();
        existingResponder.setId(3);
        existingResponder.setName("Previous Responder");
        existingResponder.setUserBadges(new HashSet<>());

        Answer existingAnswer = new Answer();
        existingAnswer.setId(1);
        existingAnswer.setResponder(existingResponder);
        existingAnswer.setContent("Previous answer");

        Question question = new Question();
        question.setId(questionId);
        question.setAnswer(existingAnswer); // Already has an answer

        when(questionRepository.findById(questionId)).thenReturn(Optional.of(question));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            marketplaceService.createAnswerForQuestion(questionId, request, responderId);
        });

        assertEquals("Question already has an answer", exception.getMessage());

        verify(questionRepository, times(1)).findById(questionId);
        verify(userRepository, never()).findById(any());
        verify(answerRepository, never()).save(any());
    }

    @Test
    void createQuestionForService_ShouldThrowException_WhenContentIsNull() {
        // Arrange
        Integer serviceId = 1;
        Integer askerId = 2;
        CreateQuestionRequest request = new CreateQuestionRequest();
        request.setContent(null); // Null content

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            marketplaceService.createQuestionForService(serviceId, request, askerId);
        });

        assertEquals("Question content cannot be null or empty", exception.getMessage());

        verify(userRepository, never()).findById(any());
        verify(offerRepository, never()).findById(any());
        verify(questionRepository, never()).save(any());
    }

    @Test
    void createQuestionForService_ShouldThrowException_WhenContentIsEmpty() {
        // Arrange
        Integer serviceId = 1;
        Integer askerId = 2;
        CreateQuestionRequest request = new CreateQuestionRequest();
        request.setContent(""); // Empty content

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            marketplaceService.createQuestionForService(serviceId, request, askerId);
        });

        assertEquals("Question content cannot be null or empty", exception.getMessage());

        verify(userRepository, never()).findById(any());
        verify(offerRepository, never()).findById(any());
        verify(questionRepository, never()).save(any());
    }

    @Test
    void createQuestionForService_ShouldThrowException_WhenContentIsWhitespace() {
        // Arrange
        Integer serviceId = 1;
        Integer askerId = 2;
        CreateQuestionRequest request = new CreateQuestionRequest();
        request.setContent("   "); // Whitespace only

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            marketplaceService.createQuestionForService(serviceId, request, askerId);
        });

        assertEquals("Question content cannot be null or empty", exception.getMessage());

        verify(userRepository, never()).findById(any());
        verify(offerRepository, never()).findById(any());
        verify(questionRepository, never()).save(any());
    }

    @Test
    void createAnswerForQuestion_ShouldThrowException_WhenContentIsNull() {
        // Arrange
        Integer questionId = 1;
        Integer responderId = 2;
        CreateAnswerRequest request = new CreateAnswerRequest();
        request.setContent(null); // Null content

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            marketplaceService.createAnswerForQuestion(questionId, request, responderId);
        });

        assertEquals("Answer content cannot be null or empty", exception.getMessage());

        verify(questionRepository, never()).findById(any());
        verify(userRepository, never()).findById(any());
        verify(answerRepository, never()).save(any());
    }

    @Test
    void createAnswerForQuestion_ShouldThrowException_WhenContentIsEmpty() {
        // Arrange
        Integer questionId = 1;
        Integer responderId = 2;
        CreateAnswerRequest request = new CreateAnswerRequest();
        request.setContent(""); // Empty content

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            marketplaceService.createAnswerForQuestion(questionId, request, responderId);
        });

        assertEquals("Answer content cannot be null or empty", exception.getMessage());

        verify(questionRepository, never()).findById(any());
        verify(userRepository, never()).findById(any());
        verify(answerRepository, never()).save(any());
    }

    @Test
    void createAnswerForQuestion_ShouldThrowException_WhenContentIsWhitespace() {
        // Arrange
        Integer questionId = 1;
        Integer responderId = 2;
        CreateAnswerRequest request = new CreateAnswerRequest();
        request.setContent("   "); // Whitespace only

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            marketplaceService.createAnswerForQuestion(questionId, request, responderId);
        });

        assertEquals("Answer content cannot be null or empty", exception.getMessage());

        verify(questionRepository, never()).findById(any());
        verify(userRepository, never()).findById(any());
        verify(answerRepository, never()).save(any());
    }

    // ==================== Deactivate Service Tests ====================

    @Test
    void deactivateOffer_WithNoHandshakes_ShouldSucceed() {
        // Arrange
        Integer offerId = 1;
        Integer userId = 1;
        
        when(offerRepository.findById(offerId)).thenReturn(Optional.of(testOffer));
        when(handshakeRepository.findByOfferId(offerId)).thenReturn(Collections.emptyList());
        when(offerRepository.save(any(Offer.class))).thenReturn(testOffer);
        
        // Act
        OfferDTO result = marketplaceService.deactivateOffer(offerId, userId);
        
        // Assert
        assertNotNull(result);
        verify(offerRepository).findById(offerId);
        verify(handshakeRepository).findByOfferId(offerId);
        verify(offerRepository).save(any(Offer.class));
    }

    @Test
    void deactivateOffer_WithCompletedHandshakes_ShouldSucceed() {
        // Arrange
        Integer offerId = 1;
        Integer userId = 1;
        
        Handshake completedHandshake = new Handshake();
        completedHandshake.setId(1);
        completedHandshake.setStatus(HandshakeStatus.COMPLETED);
        
        when(offerRepository.findById(offerId)).thenReturn(Optional.of(testOffer));
        when(handshakeRepository.findByOfferId(offerId)).thenReturn(List.of(completedHandshake));
        when(offerRepository.save(any(Offer.class))).thenReturn(testOffer);
        
        // Act
        OfferDTO result = marketplaceService.deactivateOffer(offerId, userId);
        
        // Assert
        assertNotNull(result);
        verify(offerRepository).findById(offerId);
        verify(handshakeRepository).findByOfferId(offerId);
        verify(offerRepository).save(any(Offer.class));
    }

    @Test
    void deactivateOffer_WithCancelledHandshakes_ShouldSucceed() {
        // Arrange
        Integer offerId = 1;
        Integer userId = 1;
        
        Handshake cancelledHandshake = new Handshake();
        cancelledHandshake.setId(1);
        cancelledHandshake.setStatus(HandshakeStatus.CANCELLED);
        
        when(offerRepository.findById(offerId)).thenReturn(Optional.of(testOffer));
        when(handshakeRepository.findByOfferId(offerId)).thenReturn(List.of(cancelledHandshake));
        when(offerRepository.save(any(Offer.class))).thenReturn(testOffer);
        
        // Act
        OfferDTO result = marketplaceService.deactivateOffer(offerId, userId);
        
        // Assert
        assertNotNull(result);
        verify(offerRepository).findById(offerId);
        verify(handshakeRepository).findByOfferId(offerId);
        verify(offerRepository).save(any(Offer.class));
    }

    @Test
    void deactivateOffer_WithPendingHandshakes_ShouldThrowException() {
        // Arrange
        Integer offerId = 1;
        Integer userId = 1;
        
        Handshake pendingHandshake = new Handshake();
        pendingHandshake.setId(1);
        pendingHandshake.setStatus(HandshakeStatus.PENDING);
        
        when(offerRepository.findById(offerId)).thenReturn(Optional.of(testOffer));
        when(handshakeRepository.findByOfferId(offerId)).thenReturn(List.of(pendingHandshake));
        
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            marketplaceService.deactivateOffer(offerId, userId);
        });
        
        assertEquals("Cannot deactivate offer with pending or confirmed handshakes", exception.getMessage());
        verify(offerRepository).findById(offerId);
        verify(handshakeRepository).findByOfferId(offerId);
        verify(offerRepository, never()).save(any(Offer.class));
    }

    @Test
    void deactivateOffer_WithConfirmedHandshakes_ShouldThrowException() {
        // Arrange
        Integer offerId = 1;
        Integer userId = 1;
        
        Handshake confirmedHandshake = new Handshake();
        confirmedHandshake.setId(1);
        confirmedHandshake.setStatus(HandshakeStatus.CONFIRMED);
        
        when(offerRepository.findById(offerId)).thenReturn(Optional.of(testOffer));
        when(handshakeRepository.findByOfferId(offerId)).thenReturn(List.of(confirmedHandshake));
        
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            marketplaceService.deactivateOffer(offerId, userId);
        });
        
        assertEquals("Cannot deactivate offer with pending or confirmed handshakes", exception.getMessage());
        verify(offerRepository).findById(offerId);
        verify(handshakeRepository).findByOfferId(offerId);
        verify(offerRepository, never()).save(any(Offer.class));
    }

    @Test
    void deactivateOffer_WithUnauthorizedUser_ShouldThrowException() {
        // Arrange
        Integer offerId = 1;
        Integer unauthorizedUserId = 999;
        
        when(offerRepository.findById(offerId)).thenReturn(Optional.of(testOffer));
        
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            marketplaceService.deactivateOffer(offerId, unauthorizedUserId);
        });
        
        assertEquals("You are not authorized to deactivate this offer", exception.getMessage());
        verify(offerRepository).findById(offerId);
        verify(handshakeRepository, never()).findByOfferId(any());
        verify(offerRepository, never()).save(any(Offer.class));
    }

    @Test
    void deactivateOffer_WithNonExistentOffer_ShouldThrowException() {
        // Arrange
        Integer offerId = 999;
        Integer userId = 1;
        
        when(offerRepository.findById(offerId)).thenReturn(Optional.empty());
        
        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            marketplaceService.deactivateOffer(offerId, userId);
        });
        
        assertEquals("Offer not found with id: 999", exception.getMessage());
        verify(offerRepository).findById(offerId);
        verify(handshakeRepository, never()).findByOfferId(any());
        verify(offerRepository, never()).save(any(Offer.class));
    }

    @Test
    void deactivateRequest_WithNoHandshakes_ShouldSucceed() {
        // Arrange
        Integer requestId = 1;
        Integer userId = 2; // testUserWithBadge is the seeker
        
        when(requestRepository.findById(requestId)).thenReturn(Optional.of(testRequest));
        when(handshakeRepository.findByRequestId(requestId)).thenReturn(Collections.emptyList());
        when(requestRepository.save(any(Request.class))).thenReturn(testRequest);
        
        // Act
        RequestDTO result = marketplaceService.deactivateRequest(requestId, userId);
        
        // Assert
        assertNotNull(result);
        verify(requestRepository).findById(requestId);
        verify(handshakeRepository).findByRequestId(requestId);
        verify(requestRepository).save(any(Request.class));
    }

    @Test
    void deactivateRequest_WithPendingHandshakes_ShouldThrowException() {
        // Arrange
        Integer requestId = 1;
        Integer userId = 2; // testUserWithBadge is the seeker
        
        Handshake pendingHandshake = new Handshake();
        pendingHandshake.setId(1);
        pendingHandshake.setStatus(HandshakeStatus.PENDING);
        
        when(requestRepository.findById(requestId)).thenReturn(Optional.of(testRequest));
        when(handshakeRepository.findByRequestId(requestId)).thenReturn(List.of(pendingHandshake));
        
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            marketplaceService.deactivateRequest(requestId, userId);
        });
        
        assertEquals("Cannot deactivate request with pending or confirmed handshakes", exception.getMessage());
        verify(requestRepository).findById(requestId);
        verify(handshakeRepository).findByRequestId(requestId);
        verify(requestRepository, never()).save(any(Request.class));
    }

    @Test
    void deactivateRequest_WithUnauthorizedUser_ShouldThrowException() {
        // Arrange
        Integer requestId = 1;
        Integer unauthorizedUserId = 999;
        
        when(requestRepository.findById(requestId)).thenReturn(Optional.of(testRequest));
        
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            marketplaceService.deactivateRequest(requestId, unauthorizedUserId);
        });
        
        assertEquals("You are not authorized to deactivate this request", exception.getMessage());
        verify(requestRepository).findById(requestId);
        verify(handshakeRepository, never()).findByRequestId(any());
        verify(requestRepository, never()).save(any(Request.class));
    }

    // ==================== Service Expiration Tests ====================

    @Test
    void getActiveServices_ShouldExpireOffersWithPastEndDate() {
        // Arrange
        Offer expiredOffer = new Offer();
        expiredOffer.setId(1);
        expiredOffer.setProvider(testUser);
        expiredOffer.setTitle("Expired Offer");
        expiredOffer.setEndDate(LocalDate.now().minusDays(1)); // Yesterday
        expiredOffer.setStatus(ItemStatus.ACTIVE);
        expiredOffer.setProvince("Istanbul");
        expiredOffer.setDistrict("Kadikoy");
        
        when(offerRepository.findByStatus(ItemStatus.ACTIVE)).thenReturn(List.of(expiredOffer));
        when(requestRepository.findByStatus(ItemStatus.ACTIVE)).thenReturn(Collections.emptyList());
        
        // Act
        marketplaceService.getActiveServices();
        
        // Assert
        verify(offerRepository, times(2)).findByStatus(ItemStatus.ACTIVE); //called twice: expireOldServices(), getActiveServices() o yüzden patladı
        verify(offerRepository).save(expiredOffer);
        assertEquals(ItemStatus.EXPIRED, expiredOffer.getStatus());
    }

    @Test
    void getActiveServices_ShouldExpireRequestsWithPastEndDate() {
        // Arrange
        Request expiredRequest = new Request();
        expiredRequest.setId(1);
        expiredRequest.setSeeker(testUser);
        expiredRequest.setTitle("Expired Request");
        expiredRequest.setEndDate(LocalDate.now().minusDays(5)); // 5 days ago
        expiredRequest.setStatus(ItemStatus.ACTIVE);
        expiredRequest.setProvince("Ankara");
        expiredRequest.setDistrict("Cankaya");
        
        when(offerRepository.findByStatus(ItemStatus.ACTIVE)).thenReturn(Collections.emptyList());
        when(requestRepository.findByStatus(ItemStatus.ACTIVE)).thenReturn(List.of(expiredRequest));
        
        // Act
        marketplaceService.getActiveServices();
        
        // Assert - called twice: once in expireOldServices(), once in getActiveServices()
        verify(requestRepository, times(2)).findByStatus(ItemStatus.ACTIVE);
        verify(requestRepository).save(expiredRequest);
        assertEquals(ItemStatus.EXPIRED, expiredRequest.getStatus());
    }

    @Test
    void getActiveServices_ShouldNotExpireServicesWithFutureEndDate() {
        // Arrange
        Offer futureOffer = new Offer();
        futureOffer.setId(1);
        futureOffer.setProvider(testUser);
        futureOffer.setTitle("Future Offer");
        futureOffer.setEndDate(LocalDate.now().plusDays(10)); // 10 days from now
        futureOffer.setStatus(ItemStatus.ACTIVE);
        futureOffer.setProvince("Istanbul");
        futureOffer.setDistrict("Kadikoy");
        
        Request futureRequest = new Request();
        futureRequest.setId(1);
        futureRequest.setSeeker(testUser);
        futureRequest.setTitle("Future Request");
        futureRequest.setEndDate(LocalDate.now().plusDays(5)); // 5 days from now
        futureRequest.setStatus(ItemStatus.ACTIVE);
        futureRequest.setProvince("Ankara");
        futureRequest.setDistrict("Cankaya");
        
        when(offerRepository.findByStatus(ItemStatus.ACTIVE)).thenReturn(List.of(futureOffer));
        when(requestRepository.findByStatus(ItemStatus.ACTIVE)).thenReturn(List.of(futureRequest));
        
        // Act
        marketplaceService.getActiveServices();
        
        // Assert - called twice: once in expireOldServices(), once in getActiveServices()
        verify(offerRepository, times(2)).findByStatus(ItemStatus.ACTIVE);
        verify(requestRepository, times(2)).findByStatus(ItemStatus.ACTIVE);
        verify(offerRepository, never()).save(any(Offer.class));
        verify(requestRepository, never()).save(any(Request.class));
        assertEquals(ItemStatus.ACTIVE, futureOffer.getStatus());
        assertEquals(ItemStatus.ACTIVE, futureRequest.getStatus());
    }

    @Test
    void getActiveServices_ShouldNotExpireServicesWithNullEndDate() {
        // Arrange
        Offer offerWithoutEndDate = new Offer();
        offerWithoutEndDate.setId(1);
        offerWithoutEndDate.setProvider(testUser);
        offerWithoutEndDate.setTitle("No End Date Offer");
        offerWithoutEndDate.setEndDate(null);
        offerWithoutEndDate.setStatus(ItemStatus.ACTIVE);
        offerWithoutEndDate.setProvince("Istanbul");
        offerWithoutEndDate.setDistrict("Kadikoy");
        
        when(offerRepository.findByStatus(ItemStatus.ACTIVE)).thenReturn(List.of(offerWithoutEndDate));
        when(requestRepository.findByStatus(ItemStatus.ACTIVE)).thenReturn(Collections.emptyList());
        
        // Act
        marketplaceService.getActiveServices();
        
        // Assert - called twice: once in expireOldServices(), once in getActiveServices()
        verify(offerRepository, times(2)).findByStatus(ItemStatus.ACTIVE);
        verify(offerRepository, never()).save(any(Offer.class));
        assertEquals(ItemStatus.ACTIVE, offerWithoutEndDate.getStatus());
    }

    @Test
    void getActiveServices_ShouldExpireMultipleServicesAtOnce() {
        // Arrange
        Offer expiredOffer1 = new Offer();
        expiredOffer1.setId(1);
        expiredOffer1.setProvider(testUser);
        expiredOffer1.setEndDate(LocalDate.now().minusDays(1));
        expiredOffer1.setStatus(ItemStatus.ACTIVE);
        expiredOffer1.setProvince("Istanbul");
        expiredOffer1.setDistrict("Kadikoy");
        
        Offer expiredOffer2 = new Offer();
        expiredOffer2.setId(2);
        expiredOffer2.setProvider(testUser);
        expiredOffer2.setEndDate(LocalDate.now().minusDays(3));
        expiredOffer2.setStatus(ItemStatus.ACTIVE);
        expiredOffer2.setProvince("Ankara");
        expiredOffer2.setDistrict("Cankaya");
        
        Request expiredRequest = new Request();
        expiredRequest.setId(1);
        expiredRequest.setSeeker(testUser);
        expiredRequest.setEndDate(LocalDate.now().minusDays(2));
        expiredRequest.setStatus(ItemStatus.ACTIVE);
        expiredRequest.setProvince("Izmir");
        expiredRequest.setDistrict("Bornova");
        
        when(offerRepository.findByStatus(ItemStatus.ACTIVE))
            .thenReturn(List.of(expiredOffer1, expiredOffer2));
        when(requestRepository.findByStatus(ItemStatus.ACTIVE))
            .thenReturn(List.of(expiredRequest));
        
        // Act
        marketplaceService.getActiveServices();
        
        // Assert
        verify(offerRepository).save(expiredOffer1);
        verify(offerRepository).save(expiredOffer2);
        verify(requestRepository).save(expiredRequest);
        assertEquals(ItemStatus.EXPIRED, expiredOffer1.getStatus());
        assertEquals(ItemStatus.EXPIRED, expiredOffer2.getStatus());
        assertEquals(ItemStatus.EXPIRED, expiredRequest.getStatus());
    }
}

