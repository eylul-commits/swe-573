package com.thehive.service;

import com.thehive.exception.ResourceNotFoundException;
import com.thehive.model.dto.ConfirmHandshakeRequest;
import com.thehive.model.dto.CreateHandshakeRequest;
import com.thehive.model.dto.CreateRatingRequest;
import com.thehive.model.dto.HandshakeDTO;
import com.thehive.model.entity.Handshake;
import com.thehive.model.entity.Offer;
import com.thehive.model.entity.Rating;
import com.thehive.model.entity.User;
import com.thehive.model.enums.HandshakeStatus;
import com.thehive.model.enums.ItemStatus;
import com.thehive.repository.HandshakeRepository;
import com.thehive.repository.OfferRepository;
import com.thehive.repository.RatingRepository;
import com.thehive.repository.RequestRepository;
import com.thehive.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HandshakeServiceTest {

    @Mock
    private HandshakeRepository handshakeRepository;

    @Mock
    private OfferRepository offerRepository;

    @Mock
    private RequestRepository requestRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RatingRepository ratingRepository;

    @InjectMocks
    private HandshakeService handshakeService;

    private User seeker;
    private User provider;
    private Offer offer;
    private Handshake handshake;

    @BeforeEach
    void setUp() {
        // Setup seeker
        seeker = new User();
        seeker.setId(1);
        seeker.setEmail("seeker@test.com");
        seeker.setName("Seeker User");
        seeker.setBalanceHours(10);
        seeker.setUserBadges(new HashSet<>());

        // Setup provider
        provider = new User();
        provider.setId(2);
        provider.setEmail("provider@test.com");
        provider.setName("Provider User");
        provider.setBalanceHours(15);
        provider.setUserBadges(new HashSet<>());

        // Setup offer
        offer = new Offer();
        offer.setId(1);
        offer.setTitle("Math Tutoring");
        offer.setDescription("Help with calculus");
        offer.setDurationHours(5);
        offer.setProvider(provider);
        offer.setStatus(ItemStatus.ACTIVE);
        offer.setTags(new HashSet<>());

        // Setup handshake
        handshake = new Handshake();
        handshake.setId(1);
        handshake.setOffer(offer);
        handshake.setSeeker(seeker);
        handshake.setProvider(provider);
        handshake.setStatus(HandshakeStatus.PENDING);
        handshake.setAgreedHours(5);
        handshake.setSeekerConfirmed(false);
        handshake.setProviderConfirmed(false);
        handshake.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void createHandshake_Success() {
        // Arrange
        CreateHandshakeRequest request = new CreateHandshakeRequest();
        request.setOfferId(1);
        request.setProviderId(2);
        request.setAgreedHours(5);

        when(offerRepository.findById(1)).thenReturn(Optional.of(offer));
        when(userRepository.findById(1)).thenReturn(Optional.of(seeker));
        when(userRepository.findById(2)).thenReturn(Optional.of(provider));
        when(handshakeRepository.findByOfferIdAndSeekerId(1, 1)).thenReturn(Optional.empty());
        when(handshakeRepository.save(any(Handshake.class))).thenReturn(handshake);

        // Act
        HandshakeDTO result = handshakeService.createHandshake(request, 1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals(HandshakeStatus.PENDING, result.getStatus());
        assertFalse(result.getSeekerConfirmed());
        assertFalse(result.getProviderConfirmed());
        assertEquals("Math Tutoring", result.getOfferTitle());
        
        verify(handshakeRepository).save(any(Handshake.class));
    }

    @Test
    void createHandshake_OfferNotFound() {
        // Arrange
        CreateHandshakeRequest request = new CreateHandshakeRequest();
        request.setOfferId(999);
        request.setProviderId(2);

        when(offerRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, 
            () -> handshakeService.createHandshake(request, 1));
    }

    @Test
    void createHandshake_HandshakeAlreadyExists() {
        // Arrange
        CreateHandshakeRequest request = new CreateHandshakeRequest();
        request.setOfferId(1);
        request.setProviderId(2);

        when(offerRepository.findById(1)).thenReturn(Optional.of(offer));
        when(handshakeRepository.findByOfferIdAndSeekerId(1, 1))
            .thenReturn(Optional.of(handshake));

        // Act & Assert
        assertThrows(IllegalStateException.class, 
            () -> handshakeService.createHandshake(request, 1));
    }

    @Test
    void confirmHandshake_SeekerConfirms() {
        // Arrange
        ConfirmHandshakeRequest request = new ConfirmHandshakeRequest();
        request.setCompletedAt(LocalDateTime.now().plusDays(7));

        when(handshakeRepository.findById(1)).thenReturn(Optional.of(handshake));
        when(handshakeRepository.save(any(Handshake.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        HandshakeDTO result = handshakeService.confirmHandshake(1, 1, request);

        // Assert
        assertNotNull(result);
        assertTrue(result.getSeekerConfirmed());
        assertFalse(result.getProviderConfirmed());
        assertEquals(HandshakeStatus.PENDING, result.getStatus());
        
        verify(handshakeRepository).save(any(Handshake.class));
    }

    @Test
    void confirmHandshake_BothConfirm() {
        // Arrange
        handshake.setSeekerConfirmed(true); // Seeker already confirmed
        
        ConfirmHandshakeRequest request = new ConfirmHandshakeRequest();
        LocalDateTime completionDate = LocalDateTime.now().plusDays(7);
        request.setCompletedAt(completionDate);

        when(handshakeRepository.findById(1)).thenReturn(Optional.of(handshake));
        when(handshakeRepository.save(any(Handshake.class))).thenAnswer(i -> {
            Handshake h = i.getArgument(0);
            h.setStatus(HandshakeStatus.CONFIRMED);
            h.setCompletedAt(completionDate);
            return h;
        });

        // Act - Provider confirms
        HandshakeDTO result = handshakeService.confirmHandshake(1, 2, request);

        // Assert
        assertNotNull(result);
        assertTrue(result.getSeekerConfirmed());
        assertTrue(result.getProviderConfirmed());
        assertEquals(HandshakeStatus.CONFIRMED, result.getStatus());
        assertNotNull(result.getCompletedAt());
        
        verify(handshakeRepository).save(any(Handshake.class));
    }

    @Test
    void confirmHandshake_HandshakeNotFound() {
        // Arrange
        ConfirmHandshakeRequest request = new ConfirmHandshakeRequest();
        request.setCompletedAt(LocalDateTime.now().plusDays(7));

        when(handshakeRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, 
            () -> handshakeService.confirmHandshake(999, 1, request));
    }

    @Test
    void confirmHandshake_UserNotPartOfHandshake() {
        // Arrange
        ConfirmHandshakeRequest request = new ConfirmHandshakeRequest();
        request.setCompletedAt(LocalDateTime.now().plusDays(7));

        when(handshakeRepository.findById(1)).thenReturn(Optional.of(handshake));

        // Act & Assert - User ID 999 is not part of this handshake
        assertThrows(IllegalStateException.class, 
            () -> handshakeService.confirmHandshake(1, 999, request));
    }

    @Test
    void createRating_Success() {
        // Arrange
        handshake.setStatus(HandshakeStatus.CONFIRMED);
        handshake.setCompletedAt(LocalDateTime.now().minusDays(1)); // Date passed

        CreateRatingRequest request = new CreateRatingRequest();
        request.setHandshakeId(1);
        request.setRateeId(2);
        request.setPunctuality(5);
        request.setFriendliness(5);
        request.setCommunicative(4);
        request.setPreparedness(5);
        request.setComment("Great experience!");

        Rating rating = new Rating();
        rating.setId(1);

        when(handshakeRepository.findById(1)).thenReturn(Optional.of(handshake));
        when(userRepository.findById(2)).thenReturn(Optional.of(provider));
        when(userRepository.findById(1)).thenReturn(Optional.of(seeker));
        when(ratingRepository.save(any(Rating.class))).thenReturn(rating);
        when(ratingRepository.findByHandshakeId(1)).thenReturn(Collections.singletonList(rating));

        // Act
        HandshakeDTO result = handshakeService.createRating(request, 1);

        // Assert
        assertNotNull(result);
        verify(ratingRepository).save(any(Rating.class));
    }

    @Test
    void createRating_BeforeCompletionDate() {
        // Arrange
        handshake.setStatus(HandshakeStatus.CONFIRMED);
        handshake.setCompletedAt(LocalDateTime.now().plusDays(1)); // Future date

        CreateRatingRequest request = new CreateRatingRequest();
        request.setHandshakeId(1);
        request.setRateeId(2);
        request.setPunctuality(5);
        request.setFriendliness(5);
        request.setCommunicative(4);
        request.setPreparedness(5);

        when(handshakeRepository.findById(1)).thenReturn(Optional.of(handshake));

        // Act & Assert
        assertThrows(IllegalStateException.class, 
            () -> handshakeService.createRating(request, 1));
    }

    @Test
    void createRating_AlreadyRated() {
        // Arrange
        handshake.setStatus(HandshakeStatus.CONFIRMED);
        handshake.setCompletedAt(LocalDateTime.now().minusDays(1));

        CreateRatingRequest request = new CreateRatingRequest();
        request.setHandshakeId(1);
        request.setRateeId(2);
        request.setPunctuality(5);
        request.setFriendliness(5);
        request.setCommunicative(4);
        request.setPreparedness(5);

        when(handshakeRepository.findById(1)).thenReturn(Optional.of(handshake));
        when(ratingRepository.existsByHandshakeIdAndRaterId(1, 1)).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalStateException.class, 
            () -> handshakeService.createRating(request, 1));
    }

    @Test
    void createRating_BothUsersRated_StatusChangesToCompleted() {
        // Arrange
        handshake.setStatus(HandshakeStatus.CONFIRMED);
        handshake.setCompletedAt(LocalDateTime.now().minusDays(1));

        CreateRatingRequest request = new CreateRatingRequest();
        request.setHandshakeId(1);
        request.setRateeId(2);
        request.setPunctuality(5);
        request.setFriendliness(5);
        request.setCommunicative(4);
        request.setPreparedness(5);

        Rating rating1 = new Rating();
        Rating rating2 = new Rating();

        when(handshakeRepository.findById(1)).thenReturn(Optional.of(handshake));
        when(userRepository.findById(2)).thenReturn(Optional.of(provider));
        when(userRepository.findById(1)).thenReturn(Optional.of(seeker));
        when(ratingRepository.save(any(Rating.class))).thenReturn(rating1);
        when(ratingRepository.findByHandshakeId(1)).thenReturn(Arrays.asList(rating1, rating2));
        when(handshakeRepository.save(any(Handshake.class))).thenAnswer(i -> {
            Handshake h = i.getArgument(0);
            h.setStatus(HandshakeStatus.COMPLETED);
            return h;
        });

        // Act
        HandshakeDTO result = handshakeService.createRating(request, 1);

        // Assert
        verify(ratingRepository).save(any(Rating.class));
        verify(handshakeRepository).save(any(Handshake.class));
    }

    @Test
    void getUserHandshakes_Success() {
        // Arrange
        List<Handshake> handshakes = Arrays.asList(handshake);
        when(handshakeRepository.findBySeekerIdOrProviderId(1, 1)).thenReturn(handshakes);

        // Act
        List<HandshakeDTO> result = handshakeService.getUserHandshakes(1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getId());
    }

    @Test
    void getUserPendingHandshakes_Success() {
        // Arrange
        List<Handshake> handshakes = Arrays.asList(handshake);
        when(handshakeRepository.findByUserIdAndStatus(1, HandshakeStatus.PENDING))
            .thenReturn(handshakes);

        // Act
        List<HandshakeDTO> result = handshakeService.getUserPendingHandshakes(1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(HandshakeStatus.PENDING, result.get(0).getStatus());
    }

    @Test
    void getUserConfirmedHandshakes_Success() {
        // Arrange
        handshake.setStatus(HandshakeStatus.CONFIRMED);
        List<Handshake> handshakes = Arrays.asList(handshake);
        when(handshakeRepository.findByUserIdAndStatus(1, HandshakeStatus.CONFIRMED))
            .thenReturn(handshakes);

        // Act
        List<HandshakeDTO> result = handshakeService.getUserConfirmedHandshakes(1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(HandshakeStatus.CONFIRMED, result.get(0).getStatus());
    }

    @Test
    void getHandshakeById_Success() {
        // Arrange
        when(handshakeRepository.findById(1)).thenReturn(Optional.of(handshake));

        // Act
        HandshakeDTO result = handshakeService.getHandshakeById(1, 1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("Math Tutoring", result.getOfferTitle());
    }

    @Test
    void getHandshakeById_NotFound() {
        // Arrange
        when(handshakeRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, 
            () -> handshakeService.getHandshakeById(999, 1));
    }

    @Test
    void getHandshakeById_UserNotAuthorized() {
        // Arrange
        when(handshakeRepository.findById(1)).thenReturn(Optional.of(handshake));

        // Act & Assert - User 999 is not part of this handshake
        assertThrows(IllegalStateException.class, 
            () -> handshakeService.getHandshakeById(1, 999));
    }

    @Test
    void canRate_ReturnsTrueAfterCompletionDate() {
        // Arrange
        handshake.setStatus(HandshakeStatus.CONFIRMED);
        handshake.setCompletedAt(LocalDateTime.now().minusDays(1));
        
        when(handshakeRepository.findById(1)).thenReturn(Optional.of(handshake));

        // Act
        HandshakeDTO result = handshakeService.getHandshakeById(1, 1);

        // Assert
        assertTrue(result.getCanRate());
    }

    @Test
    void canRate_ReturnsFalseBeforeCompletionDate() {
        // Arrange
        handshake.setStatus(HandshakeStatus.CONFIRMED);
        handshake.setCompletedAt(LocalDateTime.now().plusDays(1));
        
        when(handshakeRepository.findById(1)).thenReturn(Optional.of(handshake));

        // Act
        HandshakeDTO result = handshakeService.getHandshakeById(1, 1);

        // Assert
        assertFalse(result.getCanRate());
    }

    @Test
    void canRate_ReturnsFalseIfAlreadyRated() {
        // Arrange
        handshake.setStatus(HandshakeStatus.CONFIRMED);
        handshake.setCompletedAt(LocalDateTime.now().minusDays(1));
        
        when(handshakeRepository.findById(1)).thenReturn(Optional.of(handshake));
        when(ratingRepository.existsByHandshakeIdAndRaterId(1, 1)).thenReturn(true);

        // Act
        HandshakeDTO result = handshakeService.getHandshakeById(1, 1);

        // Assert
        assertFalse(result.getCanRate());
    }
}

