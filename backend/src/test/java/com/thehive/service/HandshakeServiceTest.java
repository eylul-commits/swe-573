package com.thehive.service;

import com.thehive.exception.ResourceNotFoundException;
import com.thehive.model.dto.ConfirmHandshakeRequest;
import com.thehive.model.dto.CreateHandshakeRequest;
import com.thehive.model.dto.CreateRatingRequest;
import com.thehive.model.dto.HandshakeDTO;
import com.thehive.model.dto.ServiceRatingDTO;
import com.thehive.model.entity.Handshake;
import com.thehive.model.entity.Offer;
import com.thehive.model.entity.Rating;
import com.thehive.model.entity.Request;
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

    @Mock
    private TimebankTransactionService timebankTransactionService;

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
        handshake.setDurationHours(5);
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
    void createHandshake_InsufficientBalance_ThrowsExceptionForOffer() {
        // Arrange
        seeker.setBalanceHours(2); // Not enough for 5-hour service
        
        CreateHandshakeRequest request = new CreateHandshakeRequest();
        request.setOfferId(1);
        request.setProviderId(2);

        when(offerRepository.findById(1)).thenReturn(Optional.of(offer));
        when(userRepository.findById(1)).thenReturn(Optional.of(seeker));
        when(handshakeRepository.findByOfferIdAndSeekerId(1, 1)).thenReturn(Optional.empty());

        // Act & Assert - Should fail because seeker needs balance to accept an offer
        IllegalStateException exception = assertThrows(
            IllegalStateException.class, 
            () -> handshakeService.createHandshake(request, 1)
        );

        assertTrue(exception.getMessage().contains("Insufficient balance"));
        verify(handshakeRepository, never()).save(any(Handshake.class));
    }

    @Test
    void createHandshake_Request_NoBalanceCheckRequired() {
        // Arrange
        seeker.setBalanceHours(0); // Zero balance is OK for accepting requests
        
        Request serviceRequest = new Request();
        serviceRequest.setId(1);
        serviceRequest.setTitle("Need Help with Math");
        serviceRequest.setDescription("Looking for math tutor");
        serviceRequest.setDurationHours(5);
        serviceRequest.setSeeker(seeker);
        serviceRequest.setStatus(ItemStatus.ACTIVE);
        serviceRequest.setTags(new HashSet<>());
        
        Handshake requestHandshake = new Handshake();
        requestHandshake.setId(2);
        requestHandshake.setRequest(serviceRequest);
        requestHandshake.setSeeker(seeker);
        requestHandshake.setProvider(provider);
        requestHandshake.setStatus(HandshakeStatus.PENDING);
        requestHandshake.setDurationHours(5);
        
        CreateHandshakeRequest request = new CreateHandshakeRequest();
        request.setRequestId(1);
        request.setProviderId(2);

        when(requestRepository.findById(1)).thenReturn(Optional.of(serviceRequest));
        when(userRepository.findById(1)).thenReturn(Optional.of(seeker));
        when(userRepository.findById(2)).thenReturn(Optional.of(provider));
        when(handshakeRepository.findByRequestIdAndSeekerId(1, 1)).thenReturn(Optional.empty());
        when(handshakeRepository.save(any(Handshake.class))).thenReturn(requestHandshake);

        // Act - Should succeed even with zero balance because seeker will earn hours
        HandshakeDTO result = handshakeService.createHandshake(request, 1);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getId());
        assertEquals(HandshakeStatus.PENDING, result.getStatus());
        verify(handshakeRepository).save(any(Handshake.class));
    }

    @Test
    void confirmHandshake_SeekerConfirms() {
        // Arrange
        ConfirmHandshakeRequest request = new ConfirmHandshakeRequest();
        request.setAgreedDate(LocalDateTime.now().plusDays(7));

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
        LocalDateTime agreedDate = LocalDateTime.now().plusDays(7);
        request.setAgreedDate(agreedDate);

        when(handshakeRepository.findById(1)).thenReturn(Optional.of(handshake));
        when(handshakeRepository.save(any(Handshake.class))).thenAnswer(i -> {
            Handshake h = i.getArgument(0);
            h.setStatus(HandshakeStatus.CONFIRMED);
            h.setAgreedDate(agreedDate);
            return h;
        });

        // Act - Provider confirms
        HandshakeDTO result = handshakeService.confirmHandshake(1, 2, request);

        // Assert
        assertNotNull(result);
        assertTrue(result.getSeekerConfirmed());
        assertTrue(result.getProviderConfirmed());
        assertEquals(HandshakeStatus.CONFIRMED, result.getStatus());
        assertNotNull(result.getAgreedDate());
        
        verify(handshakeRepository).save(any(Handshake.class));
    }

    @Test
    void confirmHandshake_HandshakeNotFound() {
        // Arrange
        ConfirmHandshakeRequest request = new ConfirmHandshakeRequest();
        request.setAgreedDate(LocalDateTime.now().plusDays(7));

        when(handshakeRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, 
            () -> handshakeService.confirmHandshake(999, 1, request));
    }

    @Test
    void confirmHandshake_UserNotPartOfHandshake() {
        // Arrange
        ConfirmHandshakeRequest request = new ConfirmHandshakeRequest();
        request.setAgreedDate(LocalDateTime.now().plusDays(7));

        when(handshakeRepository.findById(1)).thenReturn(Optional.of(handshake));

        // Act & Assert - User ID 999 is not part of this handshake
        assertThrows(IllegalStateException.class, 
            () -> handshakeService.confirmHandshake(1, 999, request));
    }

    @Test
    void confirmHandshake_PastDateRejected() {
        // Arrange
        ConfirmHandshakeRequest request = new ConfirmHandshakeRequest();
        request.setAgreedDate(LocalDateTime.now().minusDays(1)); // Past date

        when(handshakeRepository.findById(1)).thenReturn(Optional.of(handshake));

        // Act & Assert - Past dates should be rejected
        assertThrows(IllegalArgumentException.class, 
            () -> handshakeService.confirmHandshake(1, 1, request));
    }

    @Test
    void confirmHandshake_SeekerCannotSetDate() {
        // Arrange
        ConfirmHandshakeRequest request = new ConfirmHandshakeRequest();
        LocalDateTime providedDate = LocalDateTime.now().plusDays(7);
        request.setAgreedDate(providedDate);

        when(handshakeRepository.findById(1)).thenReturn(Optional.of(handshake));
        when(handshakeRepository.save(any(Handshake.class))).thenAnswer(i -> i.getArgument(0));

        // Act - Seeker (userId=1) tries to confirm with a date
        HandshakeDTO result = handshakeService.confirmHandshake(1, 1, request);

        // Assert - Seeker confirmed but date should NOT be set (only provider can set date)
        assertNotNull(result);
        assertTrue(result.getSeekerConfirmed());
        assertFalse(result.getProviderConfirmed());
        assertEquals(HandshakeStatus.PENDING, result.getStatus());
        assertNull(result.getAgreedDate()); // Date should be null, not set by seeker
        
        verify(handshakeRepository).save(any(Handshake.class));
    }

    @Test
    void confirmHandshake_ProviderCanChangeDateBeforeSeekerConfirms() {
        // Arrange - Provider has already confirmed with a date
        handshake.setProviderConfirmed(true);
        LocalDateTime firstDate = LocalDateTime.now().plusDays(5);
        handshake.setAgreedDate(firstDate);
        
        ConfirmHandshakeRequest request = new ConfirmHandshakeRequest();
        LocalDateTime newDate = LocalDateTime.now().plusDays(10);
        request.setAgreedDate(newDate);

        when(handshakeRepository.findById(1)).thenReturn(Optional.of(handshake));
        when(handshakeRepository.save(any(Handshake.class))).thenAnswer(i -> i.getArgument(0));

        // Act - Provider (userId=2) changes the date before seeker confirms
        HandshakeDTO result = handshakeService.confirmHandshake(1, 2, request);

        // Assert - Date should be updated since seeker hasn't confirmed yet
        assertNotNull(result);
        assertTrue(result.getProviderConfirmed());
        assertFalse(result.getSeekerConfirmed());
        assertEquals(HandshakeStatus.PENDING, result.getStatus());
        assertEquals(newDate, result.getAgreedDate()); // Date should be updated
        
        verify(handshakeRepository).save(any(Handshake.class));
    }

    @Test
    void confirmHandshake_ProviderCannotChangeDateAfterSeekerConfirms() {
        // Arrange - Both have confirmed with a date
        handshake.setProviderConfirmed(true);
        handshake.setSeekerConfirmed(true);
        LocalDateTime originalDate = LocalDateTime.now().plusDays(5);
        handshake.setAgreedDate(originalDate);
        
        ConfirmHandshakeRequest request = new ConfirmHandshakeRequest();
        LocalDateTime newDate = LocalDateTime.now().plusDays(10);
        request.setAgreedDate(newDate);

        when(handshakeRepository.findById(1)).thenReturn(Optional.of(handshake));

        // Act & Assert - Provider cannot change date after seeker has confirmed
        assertThrows(IllegalStateException.class, 
            () -> handshakeService.confirmHandshake(1, 2, request));
    }

    @Test
    void createRating_Success() {
        // Arrange
        handshake.setStatus(HandshakeStatus.CONFIRMED);
        handshake.setAgreedDate(LocalDateTime.now().minusDays(1)); // Date passed

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
        handshake.setAgreedDate(LocalDateTime.now().plusDays(1)); // Future date

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
        handshake.setAgreedDate(LocalDateTime.now().minusDays(1));

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
    void createRating_BothUsersRated_StatusChangesToCompleted_ForOffer() {
        // Arrange
        handshake.setStatus(HandshakeStatus.CONFIRMED);
        handshake.setAgreedDate(LocalDateTime.now().minusDays(1));

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

        // Assert - For offers, seeker pays provider
        verify(ratingRepository).save(any(Rating.class));
        verify(handshakeRepository).save(any(Handshake.class));
        verify(timebankTransactionService).createTransaction(
            seeker.getId(),   // seeker pays (receives service)
            provider.getId(), // provider receives payment
            handshake.getId(), 
            handshake.getDurationHours()
        );
    }

    @Test
    void createRating_BothUsersRated_StatusChangesToCompleted_ForRequest() {
        // Arrange
        Request serviceRequest = new Request();
        serviceRequest.setId(1);
        serviceRequest.setTitle("Need Help with Math");
        serviceRequest.setDescription("Looking for math tutor");
        serviceRequest.setDurationHours(5);
        serviceRequest.setSeeker(seeker);
        serviceRequest.setStatus(ItemStatus.ACTIVE);
        serviceRequest.setTags(new HashSet<>());
        
        Handshake requestHandshake = new Handshake();
        requestHandshake.setId(2);
        requestHandshake.setRequest(serviceRequest);
        requestHandshake.setSeeker(seeker);
        requestHandshake.setProvider(provider);
        requestHandshake.setStatus(HandshakeStatus.CONFIRMED);
        requestHandshake.setDurationHours(5);
        requestHandshake.setAgreedDate(LocalDateTime.now().minusDays(1));
        
        CreateRatingRequest request = new CreateRatingRequest();
        request.setHandshakeId(2);
        request.setRateeId(2);
        request.setPunctuality(5);
        request.setFriendliness(5);
        request.setCommunicative(4);
        request.setPreparedness(5);

        Rating rating1 = new Rating();
        Rating rating2 = new Rating();

        when(handshakeRepository.findById(2)).thenReturn(Optional.of(requestHandshake));
        when(userRepository.findById(2)).thenReturn(Optional.of(provider));
        when(userRepository.findById(1)).thenReturn(Optional.of(seeker));
        when(ratingRepository.save(any(Rating.class))).thenReturn(rating1);
        when(ratingRepository.findByHandshakeId(2)).thenReturn(Arrays.asList(rating1, rating2));
        when(handshakeRepository.save(any(Handshake.class))).thenAnswer(i -> {
            Handshake h = i.getArgument(0);
            h.setStatus(HandshakeStatus.COMPLETED);
            return h;
        });

        // Act
        HandshakeDTO result = handshakeService.createRating(request, 1);

        // Assert - For requests, provider pays seeker (seeker provides service)
        verify(ratingRepository).save(any(Rating.class));
        verify(handshakeRepository).save(any(Handshake.class));
        verify(timebankTransactionService).createTransaction(
            provider.getId(), // provider pays (requested the service)
            seeker.getId(),   // seeker receives payment (provided the service)
            requestHandshake.getId(), 
            requestHandshake.getDurationHours()
        );
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
        handshake.setAgreedDate(LocalDateTime.now().minusDays(1));
        
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
        handshake.setAgreedDate(LocalDateTime.now().plusDays(1));
        
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
        handshake.setAgreedDate(LocalDateTime.now().minusDays(1));
        
        when(handshakeRepository.findById(1)).thenReturn(Optional.of(handshake));
        when(ratingRepository.existsByHandshakeIdAndRaterId(1, 1)).thenReturn(true);

        // Act
        HandshakeDTO result = handshakeService.getHandshakeById(1, 1);

        // Assert
        assertFalse(result.getCanRate());
    }

    @Test
    void cancelHandshake_SeekerCancels_Success() {
        // Arrange - Pending handshake, neither confirmed
        handshake.setStatus(HandshakeStatus.PENDING);
        handshake.setSeekerConfirmed(false);
        handshake.setProviderConfirmed(false);

        when(handshakeRepository.findById(1)).thenReturn(Optional.of(handshake));
        when(handshakeRepository.save(any(Handshake.class))).thenAnswer(i -> {
            Handshake h = i.getArgument(0);
            h.setStatus(HandshakeStatus.CANCELLED);
            return h;
        });

        // Act - Seeker (userId=1) cancels
        HandshakeDTO result = handshakeService.cancelHandshake(1, 1);

        // Assert
        assertNotNull(result);
        assertEquals(HandshakeStatus.CANCELLED, result.getStatus());
        verify(handshakeRepository).save(any(Handshake.class));
    }

    @Test
    void cancelHandshake_ProviderCancels_Success() {
        // Arrange - Pending handshake, neither confirmed
        handshake.setStatus(HandshakeStatus.PENDING);
        handshake.setSeekerConfirmed(false);
        handshake.setProviderConfirmed(false);

        when(handshakeRepository.findById(1)).thenReturn(Optional.of(handshake));
        when(handshakeRepository.save(any(Handshake.class))).thenAnswer(i -> {
            Handshake h = i.getArgument(0);
            h.setStatus(HandshakeStatus.CANCELLED);
            return h;
        });

        // Act - Provider (userId=2) cancels
        HandshakeDTO result = handshakeService.cancelHandshake(1, 2);

        // Assert
        assertNotNull(result);
        assertEquals(HandshakeStatus.CANCELLED, result.getStatus());
        verify(handshakeRepository).save(any(Handshake.class));
    }

    @Test
    void cancelHandshake_SeekerCancelsAfterProviderConfirmed_Success() {
        // Arrange - Pending handshake, only provider confirmed
        handshake.setStatus(HandshakeStatus.PENDING);
        handshake.setSeekerConfirmed(false);
        handshake.setProviderConfirmed(true);

        when(handshakeRepository.findById(1)).thenReturn(Optional.of(handshake));
        when(handshakeRepository.save(any(Handshake.class))).thenAnswer(i -> {
            Handshake h = i.getArgument(0);
            h.setStatus(HandshakeStatus.CANCELLED);
            return h;
        });

        // Act - Seeker cancels even though provider confirmed
        HandshakeDTO result = handshakeService.cancelHandshake(1, 1);

        // Assert
        assertNotNull(result);
        assertEquals(HandshakeStatus.CANCELLED, result.getStatus());
        verify(handshakeRepository).save(any(Handshake.class));
    }

    @Test
    void cancelHandshake_ProviderCancelsAfterSeekerConfirmed_Success() {
        // Arrange - Pending handshake, only seeker confirmed
        handshake.setStatus(HandshakeStatus.PENDING);
        handshake.setSeekerConfirmed(true);
        handshake.setProviderConfirmed(false);

        when(handshakeRepository.findById(1)).thenReturn(Optional.of(handshake));
        when(handshakeRepository.save(any(Handshake.class))).thenAnswer(i -> {
            Handshake h = i.getArgument(0);
            h.setStatus(HandshakeStatus.CANCELLED);
            return h;
        });

        // Act - Provider cancels even though seeker confirmed
        HandshakeDTO result = handshakeService.cancelHandshake(1, 2);

        // Assert
        assertNotNull(result);
        assertEquals(HandshakeStatus.CANCELLED, result.getStatus());
        verify(handshakeRepository).save(any(Handshake.class));
    }

    @Test
    void cancelHandshake_HandshakeNotFound() {
        // Arrange
        when(handshakeRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, 
            () -> handshakeService.cancelHandshake(999, 1));
    }

    @Test
    void cancelHandshake_UserNotPartOfHandshake() {
        // Arrange
        when(handshakeRepository.findById(1)).thenReturn(Optional.of(handshake));

        // Act & Assert - User ID 999 is not part of this handshake
        assertThrows(IllegalStateException.class, 
            () -> handshakeService.cancelHandshake(1, 999));
    }

    @Test
    void cancelHandshake_CannotCancelIfNotPending() {
        // Arrange - Handshake is already CONFIRMED
        handshake.setStatus(HandshakeStatus.CONFIRMED);
        handshake.setSeekerConfirmed(true);
        handshake.setProviderConfirmed(true);

        when(handshakeRepository.findById(1)).thenReturn(Optional.of(handshake));

        // Act & Assert - Cannot cancel non-pending handshake
        assertThrows(IllegalStateException.class, 
            () -> handshakeService.cancelHandshake(1, 1));
        
        verify(handshakeRepository, never()).save(any(Handshake.class));
    }

    @Test
    void cancelHandshake_CannotCancelIfBothConfirmed() {
        // Arrange - PENDING but both parties confirmed
        handshake.setStatus(HandshakeStatus.PENDING);
        handshake.setSeekerConfirmed(true);
        handshake.setProviderConfirmed(true);

        when(handshakeRepository.findById(1)).thenReturn(Optional.of(handshake));

        // Act & Assert - Cannot cancel if both parties confirmed
        assertThrows(IllegalStateException.class, 
            () -> handshakeService.cancelHandshake(1, 1));
        
        verify(handshakeRepository, never()).save(any(Handshake.class));
    }

    @Test
    void cancelHandshake_CannotCancelIfCompleted() {
        // Arrange - Handshake is COMPLETED
        handshake.setStatus(HandshakeStatus.COMPLETED);
        handshake.setSeekerConfirmed(true);
        handshake.setProviderConfirmed(true);

        when(handshakeRepository.findById(1)).thenReturn(Optional.of(handshake));

        // Act & Assert - Cannot cancel completed handshake
        assertThrows(IllegalStateException.class, 
            () -> handshakeService.cancelHandshake(1, 1));
        
        verify(handshakeRepository, never()).save(any(Handshake.class));
    }

    @Test
    void cancelHandshake_CannotCancelIfAlreadyCancelled() {
        // Arrange - Handshake is already CANCELLED
        handshake.setStatus(HandshakeStatus.CANCELLED);
        handshake.setSeekerConfirmed(false);
        handshake.setProviderConfirmed(false);

        when(handshakeRepository.findById(1)).thenReturn(Optional.of(handshake));

        // Act & Assert - Cannot cancel already cancelled handshake
        assertThrows(IllegalStateException.class, 
            () -> handshakeService.cancelHandshake(1, 1));
        
        verify(handshakeRepository, never()).save(any(Handshake.class));
    }

    @Test
    void getUserRatings_Success_ReturnsListOfRatings() {
        // Arrange
        Rating rating1 = new Rating();
        rating1.setId(1);
        rating1.setRater(provider);
        rating1.setRatee(seeker);
        rating1.setPunctuality(5);
        rating1.setFriendliness(4);
        rating1.setCommunicative(5);
        rating1.setPreparedness(4);
        rating1.setComment("Great service!");
        rating1.setCreatedAt(LocalDateTime.now().minusDays(5));

        Rating rating2 = new Rating();
        rating2.setId(2);
        rating2.setRater(provider);
        rating2.setRatee(seeker);
        rating2.setPunctuality(4);
        rating2.setFriendliness(5);
        rating2.setCommunicative(4);
        rating2.setPreparedness(5);
        rating2.setComment("Excellent work!");
        rating2.setCreatedAt(LocalDateTime.now().minusDays(2));

        List<Rating> ratings = Arrays.asList(rating1, rating2);
        when(ratingRepository.findByRateeId(1)).thenReturn(ratings);

        // Act
        List<ServiceRatingDTO> result = handshakeService.getUserRatings(1);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        
        ServiceRatingDTO dto1 = result.get(0);
        assertEquals(1, dto1.getId());
        assertNotNull(dto1.getRater());
        assertEquals(provider.getId(), dto1.getRater().getId());
        assertEquals(5, dto1.getPunctuality());
        assertEquals(4, dto1.getFriendliness());
        assertEquals(5, dto1.getCommunicative());
        assertEquals(4, dto1.getPreparedness());
        assertEquals("Great service!", dto1.getComment());
        assertNotNull(dto1.getCreatedAt());
        
        ServiceRatingDTO dto2 = result.get(1);
        assertEquals(2, dto2.getId());
        assertEquals(4, dto2.getPunctuality());
        assertEquals(5, dto2.getFriendliness());
        assertEquals(4, dto2.getCommunicative());
        assertEquals(5, dto2.getPreparedness());
        assertEquals("Excellent work!", dto2.getComment());
        
        verify(ratingRepository).findByRateeId(1);
    }

    @Test
    void getUserRatings_EmptyList_WhenUserHasNoRatings() {
        // Arrange
        when(ratingRepository.findByRateeId(1)).thenReturn(Collections.emptyList());

        // Act
        List<ServiceRatingDTO> result = handshakeService.getUserRatings(1);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(ratingRepository).findByRateeId(1);
    }

    @Test
    void getUserRatings_ConvertsRatingToDTO_WithAllFields() {
        // Arrange
        User rater = new User();
        rater.setId(3);
        rater.setEmail("rater@test.com");
        rater.setName("Rater User");
        rater.setBalanceHours(20);
        rater.setUserBadges(new HashSet<>());
        rater.setBio("Test bio");
        rater.setProvince("Test Province");
        rater.setDistrict("Test District");
        rater.setAvatarUrl("http://example.com/avatar.jpg");

        Rating rating = new Rating();
        rating.setId(10);
        rating.setRater(rater);
        rating.setRatee(seeker);
        rating.setPunctuality(5);
        rating.setFriendliness(5);
        rating.setCommunicative(5);
        rating.setPreparedness(5);
        rating.setComment("Perfect in every way!");
        LocalDateTime createdAt = LocalDateTime.now().minusDays(10);
        rating.setCreatedAt(createdAt);

        when(ratingRepository.findByRateeId(1)).thenReturn(Collections.singletonList(rating));

        // Act
        List<ServiceRatingDTO> result = handshakeService.getUserRatings(1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        
        ServiceRatingDTO dto = result.get(0);
        assertEquals(10, dto.getId());
        assertNotNull(dto.getRater());
        assertEquals(3, dto.getRater().getId());
        assertEquals("Rater User", dto.getRater().getName());
        assertEquals("http://example.com/avatar.jpg", dto.getRater().getAvatar());
        assertEquals("Test bio", dto.getRater().getBio());
        assertEquals("Test Province", dto.getRater().getProvince());
        assertEquals("Test District", dto.getRater().getDistrict());
        assertEquals(20, dto.getRater().getBalanceHours());
        assertEquals(5, dto.getPunctuality());
        assertEquals(5, dto.getFriendliness());
        assertEquals(5, dto.getCommunicative());
        assertEquals(5, dto.getPreparedness());
        assertEquals("Perfect in every way!", dto.getComment());
        assertEquals(createdAt, dto.getCreatedAt());
    }

    @Test
    void getUserRatings_HandlesNullComment() {
        // Arrange
        Rating rating = new Rating();
        rating.setId(1);
        rating.setRater(provider);
        rating.setRatee(seeker);
        rating.setPunctuality(3);
        rating.setFriendliness(3);
        rating.setCommunicative(3);
        rating.setPreparedness(3);
        rating.setComment(null);
        rating.setCreatedAt(LocalDateTime.now());

        when(ratingRepository.findByRateeId(1)).thenReturn(Collections.singletonList(rating));

        // Act
        List<ServiceRatingDTO> result = handshakeService.getUserRatings(1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        ServiceRatingDTO dto = result.get(0);
        assertNull(dto.getComment());
        assertEquals(3, dto.getPunctuality());
    }

    @Test
    void getUserRatings_IncludesServiceInfo_WhenHandshakeHasOffer() {
        // Arrange
        Handshake handshakeWithOffer = new Handshake();
        handshakeWithOffer.setId(1);
        handshakeWithOffer.setOffer(offer);
        handshakeWithOffer.setSeeker(seeker);
        handshakeWithOffer.setProvider(provider);
        handshakeWithOffer.setStatus(HandshakeStatus.COMPLETED);

        Rating rating = new Rating();
        rating.setId(1);
        rating.setRater(provider);
        rating.setRatee(seeker);
        rating.setHandshake(handshakeWithOffer);
        rating.setPunctuality(5);
        rating.setFriendliness(4);
        rating.setCommunicative(5);
        rating.setPreparedness(4);
        rating.setComment("Great service!");
        rating.setCreatedAt(LocalDateTime.now());

        when(ratingRepository.findByRateeId(1)).thenReturn(Collections.singletonList(rating));

        // Act
        List<ServiceRatingDTO> result = handshakeService.getUserRatings(1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        ServiceRatingDTO dto = result.get(0);
        assertNotNull(dto.getServiceId());
        assertEquals(offer.getId(), dto.getServiceId());
        assertNotNull(dto.getServiceTitle());
        assertEquals("Math Tutoring", dto.getServiceTitle());
    }

    @Test
    void getUserRatings_IncludesServiceInfo_WhenHandshakeHasRequest() {
        // Arrange
        Request request = new Request();
        request.setId(2);
        request.setTitle("Need Help with Physics");
        request.setSeeker(seeker);
        request.setStatus(ItemStatus.ACTIVE);
        request.setTags(new HashSet<>());

        Handshake handshakeWithRequest = new Handshake();
        handshakeWithRequest.setId(2);
        handshakeWithRequest.setRequest(request);
        handshakeWithRequest.setSeeker(seeker);
        handshakeWithRequest.setProvider(provider);
        handshakeWithRequest.setStatus(HandshakeStatus.COMPLETED);

        Rating rating = new Rating();
        rating.setId(2);
        rating.setRater(provider);
        rating.setRatee(seeker);
        rating.setHandshake(handshakeWithRequest);
        rating.setPunctuality(4);
        rating.setFriendliness(5);
        rating.setCommunicative(4);
        rating.setPreparedness(5);
        rating.setComment("Excellent work!");
        rating.setCreatedAt(LocalDateTime.now());

        when(ratingRepository.findByRateeId(1)).thenReturn(Collections.singletonList(rating));

        // Act
        List<ServiceRatingDTO> result = handshakeService.getUserRatings(1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        ServiceRatingDTO dto = result.get(0);
        assertNotNull(dto.getServiceId());
        assertEquals(request.getId(), dto.getServiceId());
        assertNotNull(dto.getServiceTitle());
        assertEquals("Need Help with Physics", dto.getServiceTitle());
    }

    @Test
    void getUserRatings_SetsRateeRoleAsProvider_WhenRateeIsHandshakeProvider() {
        // Arrange
        Handshake handshakeWithOffer = new Handshake();
        handshakeWithOffer.setId(1);
        handshakeWithOffer.setOffer(offer);
        handshakeWithOffer.setSeeker(seeker);
        handshakeWithOffer.setProvider(provider);
        handshakeWithOffer.setStatus(HandshakeStatus.COMPLETED);

        Rating rating = new Rating();
        rating.setId(1);
        rating.setRater(seeker);
        rating.setRatee(provider); // Provider is being rated
        rating.setHandshake(handshakeWithOffer);
        rating.setPunctuality(5);
        rating.setFriendliness(5);
        rating.setCommunicative(5);
        rating.setPreparedness(5);
        rating.setComment("Great provider!");
        rating.setCreatedAt(LocalDateTime.now());

        when(ratingRepository.findByRateeId(2)).thenReturn(Collections.singletonList(rating));

        // Act
        List<ServiceRatingDTO> result = handshakeService.getUserRatings(2);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        ServiceRatingDTO dto = result.get(0);
        assertNotNull(dto.getRateeRole());
        assertEquals("PROVIDER", dto.getRateeRole());
    }

    @Test
    void getUserRatings_SetsRateeRoleAsSeeker_WhenRateeIsHandshakeSeeker() {
        // Arrange
        Handshake handshakeWithOffer = new Handshake();
        handshakeWithOffer.setId(1);
        handshakeWithOffer.setOffer(offer);
        handshakeWithOffer.setSeeker(seeker);
        handshakeWithOffer.setProvider(provider);
        handshakeWithOffer.setStatus(HandshakeStatus.COMPLETED);

        Rating rating = new Rating();
        rating.setId(1);
        rating.setRater(provider);
        rating.setRatee(seeker); // Seeker is being rated
        rating.setHandshake(handshakeWithOffer);
        rating.setPunctuality(4);
        rating.setFriendliness(5);
        rating.setCommunicative(4);
        rating.setPreparedness(5);
        rating.setComment("Great seeker!");
        rating.setCreatedAt(LocalDateTime.now());

        when(ratingRepository.findByRateeId(1)).thenReturn(Collections.singletonList(rating));

        // Act
        List<ServiceRatingDTO> result = handshakeService.getUserRatings(1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        ServiceRatingDTO dto = result.get(0);
        assertNotNull(dto.getRateeRole());
        assertEquals("SEEKER", dto.getRateeRole());
    }

    @Test
    void getUserRatings_SetsRateeRoleAsProvider_ForRequestHandshake() {
        // Arrange - In a request, the provider accepts the request
        Request request = new Request();
        request.setId(2);
        request.setTitle("Need Help with Physics");
        request.setSeeker(seeker);
        request.setStatus(ItemStatus.ACTIVE);
        request.setTags(new HashSet<>());

        Handshake handshakeWithRequest = new Handshake();
        handshakeWithRequest.setId(2);
        handshakeWithRequest.setRequest(request);
        handshakeWithRequest.setSeeker(seeker);
        handshakeWithRequest.setProvider(provider);
        handshakeWithRequest.setStatus(HandshakeStatus.COMPLETED);

        Rating rating = new Rating();
        rating.setId(2);
        rating.setRater(seeker);
        rating.setRatee(provider); // Provider is being rated
        rating.setHandshake(handshakeWithRequest);
        rating.setPunctuality(5);
        rating.setFriendliness(5);
        rating.setCommunicative(5);
        rating.setPreparedness(5);
        rating.setComment("Excellent provider!");
        rating.setCreatedAt(LocalDateTime.now());

        when(ratingRepository.findByRateeId(2)).thenReturn(Collections.singletonList(rating));

        // Act
        List<ServiceRatingDTO> result = handshakeService.getUserRatings(2);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        ServiceRatingDTO dto = result.get(0);
        assertNotNull(dto.getRateeRole());
        assertEquals("PROVIDER", dto.getRateeRole());
        assertEquals(request.getId(), dto.getServiceId());
        assertEquals("Need Help with Physics", dto.getServiceTitle());
    }

    @Test
    void getUserRatings_SetsRateeRoleAsSeeker_ForRequestHandshake() {
        // Arrange - In a request, the seeker posted the request
        Request request = new Request();
        request.setId(2);
        request.setTitle("Need Help with Math");
        request.setSeeker(seeker);
        request.setStatus(ItemStatus.ACTIVE);
        request.setTags(new HashSet<>());

        Handshake handshakeWithRequest = new Handshake();
        handshakeWithRequest.setId(2);
        handshakeWithRequest.setRequest(request);
        handshakeWithRequest.setSeeker(seeker);
        handshakeWithRequest.setProvider(provider);
        handshakeWithRequest.setStatus(HandshakeStatus.COMPLETED);

        Rating rating = new Rating();
        rating.setId(2);
        rating.setRater(provider);
        rating.setRatee(seeker); // Seeker is being rated
        rating.setHandshake(handshakeWithRequest);
        rating.setPunctuality(4);
        rating.setFriendliness(5);
        rating.setCommunicative(4);
        rating.setPreparedness(5);
        rating.setComment("Excellent seeker!");
        rating.setCreatedAt(LocalDateTime.now());

        when(ratingRepository.findByRateeId(1)).thenReturn(Collections.singletonList(rating));

        // Act
        List<ServiceRatingDTO> result = handshakeService.getUserRatings(1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        ServiceRatingDTO dto = result.get(0);
        assertNotNull(dto.getRateeRole());
        assertEquals("SEEKER", dto.getRateeRole());
        assertEquals(request.getId(), dto.getServiceId());
        assertEquals("Need Help with Math", dto.getServiceTitle());
    }

    @Test
    void getHandshakesByOfferId_EmptyList_WhenNoHandshakesExist() {
        // Arrange
        when(handshakeRepository.findByOfferId(1)).thenReturn(Collections.emptyList());

        // Act
        List<HandshakeDTO> result = handshakeService.getHandshakesByOfferId(1, seeker.getId());

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(handshakeRepository).findByOfferId(1);
    }

    @Test
    void getHandshakesByOfferId_ConvertsToDTO_WithCorrectUserContext() {
        // Arrange
        List<Handshake> handshakes = Collections.singletonList(handshake);
        when(handshakeRepository.findByOfferId(1)).thenReturn(handshakes);

        // Act - Request from seeker's perspective
        List<HandshakeDTO> result = handshakeService.getHandshakesByOfferId(1, seeker.getId());

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        HandshakeDTO dto = result.get(0);
        assertEquals(seeker.getId(), dto.getSeeker().getId());
        assertEquals(provider.getId(), dto.getProvider().getId());
        
        verify(handshakeRepository).findByOfferId(1);
    }

    @Test
    void getHandshakesByRequestId_EmptyList_WhenNoHandshakesExist() {
        // Arrange
        when(handshakeRepository.findByRequestId(1)).thenReturn(Collections.emptyList());

        // Act
        List<HandshakeDTO> result = handshakeService.getHandshakesByRequestId(1, seeker.getId());

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(handshakeRepository).findByRequestId(1);
    }

    @Test
    void getHandshakesByRequestId_ConvertsToDTO_WithCorrectUserContext() {
        // Arrange
        Request serviceRequest = new Request();
        serviceRequest.setId(1);
        serviceRequest.setTitle("Need Help with Math");
        serviceRequest.setDescription("Looking for math tutor");
        serviceRequest.setDurationHours(5);
        serviceRequest.setSeeker(seeker);
        serviceRequest.setStatus(ItemStatus.ACTIVE);
        serviceRequest.setTags(new HashSet<>());

        Handshake requestHandshake = new Handshake();
        requestHandshake.setId(1);
        requestHandshake.setRequest(serviceRequest);
        requestHandshake.setSeeker(seeker);
        requestHandshake.setProvider(provider);
        requestHandshake.setStatus(HandshakeStatus.PENDING);
        requestHandshake.setDurationHours(5);
        requestHandshake.setCreatedAt(LocalDateTime.now());

        List<Handshake> handshakes = Collections.singletonList(requestHandshake);
        when(handshakeRepository.findByRequestId(1)).thenReturn(handshakes);

        // Act - Request from provider's perspective
        List<HandshakeDTO> result = handshakeService.getHandshakesByRequestId(1, provider.getId());

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        HandshakeDTO dto = result.get(0);
        assertEquals(seeker.getId(), dto.getSeeker().getId());
        assertEquals(provider.getId(), dto.getProvider().getId());
        
        verify(handshakeRepository).findByRequestId(1);
    }
}

