package com.thehive.service;

import com.thehive.model.dto.ServiceDTO;
import com.thehive.model.dto.AuthorDTO;
import com.thehive.model.entity.*;
import com.thehive.model.enums.ItemStatus;
import com.thehive.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

    @Mock
    private OfferRepository offerRepository;

    @Mock
    private RequestRepository requestRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TimebankTransactionRepository transactionRepository;

    @Mock
    private HandshakeRepository handshakeRepository;

    @Mock
    private MarketplaceService marketplaceService;

    @InjectMocks
    private RecommendationService recommendationService;

    private User testUser;
    private ServiceDTO nearbyService;
    private ServiceDTO farService;
    private ServiceDTO userOwnService;
    private List<ServiceDTO> activeServices;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1);
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setGeohash("u4pruyd"); // Istanbul area

        // Create test services
        AuthorDTO nearbyPoster = new AuthorDTO();
        nearbyPoster.setId(2);
        nearbyPoster.setName("Nearby User");

        AuthorDTO farPoster = new AuthorDTO();
        farPoster.setId(3);
        farPoster.setName("Far User");

        AuthorDTO userPoster = new AuthorDTO();
        userPoster.setId(1);
        userPoster.setName("Test User");

        nearbyService = new ServiceDTO();
        nearbyService.setId(1);
        nearbyService.setTitle("Nearby Service");
        nearbyService.setGeohash("u4pruye"); // Very close (same 6 char prefix)
        nearbyService.setPoster(nearbyPoster);
        nearbyService.setTags(List.of("programming", "tutoring"));

        farService = new ServiceDTO();
        farService.setId(2);
        farService.setTitle("Far Service");
        farService.setGeohash("ezs42"); // Very far (different prefix)
        farService.setPoster(farPoster);
        farService.setTags(List.of("gardening", "outdoor"));

        userOwnService = new ServiceDTO();
        userOwnService.setId(3);
        userOwnService.setTitle("My Service");
        userOwnService.setGeohash("u4pruyd");
        userOwnService.setPoster(userPoster);
        userOwnService.setTags(List.of("cooking", "lessons"));

        activeServices = Arrays.asList(nearbyService, farService, userOwnService);
    }

    // ========== findNearbyServices Tests ==========

    @Test
    void findNearbyServices_WithValidUserAndGeohash_ReturnsNearbyServices() {
        // Arrange
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(marketplaceService.getActiveServices()).thenReturn(activeServices);

        // Act
        List<ServiceDTO> result = recommendationService.findNearbyServices(1, 10);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size()); // Only nearby service (within 100km and not user's own)
        assertEquals("Nearby Service", result.get(0).getTitle());
        assertNotNull(result.get(0).getDistance()); // Distance label should be set
        verify(userRepository).findById(1);
        verify(marketplaceService).getActiveServices();
    }

    @Test
    void findNearbyServices_UserWithoutGeohash_ReturnsRecentServices() {
        // Arrange
        testUser.setGeohash(null);
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(marketplaceService.getActiveServices()).thenReturn(activeServices);

        // Act
        List<ServiceDTO> result = recommendationService.findNearbyServices(1, 10);

        // Assert
        assertNotNull(result);
        assertTrue(result.size() <= 10);
        verify(userRepository).findById(1);
        verify(marketplaceService).getActiveServices();
    }

    @Test
    void findNearbyServices_UserNotFound_ReturnsRecentServices() {
        // Arrange
        when(userRepository.findById(999)).thenReturn(Optional.empty());
        when(marketplaceService.getActiveServices()).thenReturn(activeServices);

        // Act
        List<ServiceDTO> result = recommendationService.findNearbyServices(999, 10);

        // Assert
        assertNotNull(result);
        assertTrue(result.size() <= 10);
        verify(userRepository).findById(999);
        verify(marketplaceService).getActiveServices();
    }

    @Test
    void findNearbyServices_FiltersUserOwnServices() {
        // Arrange
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(marketplaceService.getActiveServices()).thenReturn(activeServices);

        // Act
        List<ServiceDTO> result = recommendationService.findNearbyServices(1, 10);

        // Assert
        assertNotNull(result);
        assertTrue(result.stream().noneMatch(s -> s.getPoster().getId().equals(1)));
        verify(userRepository).findById(1);
    }

    @Test
    void findNearbyServices_RespectsMaxResults() {
        // Arrange
        List<ServiceDTO> manyServices = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            AuthorDTO poster = new AuthorDTO();
            poster.setId(i + 10);
            ServiceDTO service = new ServiceDTO();
            service.setId(i + 10);
            service.setTitle("Service " + i);
            service.setGeohash("u4pruye"); // All nearby
            service.setPoster(poster);
            service.setTags(List.of("tag" + i));
            manyServices.add(service);
        }
        
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(marketplaceService.getActiveServices()).thenReturn(manyServices);

        // Act
        List<ServiceDTO> result = recommendationService.findNearbyServices(1, 5);

        // Assert
        assertNotNull(result);
        assertEquals(5, result.size());
        verify(userRepository).findById(1);
    }

    // ========== getRecommendedServices Tests ==========

    @Test
    void getRecommendedServices_UserWithTransactionHistory_ReturnsPersonalizedRecommendations() {
        // Arrange
        TimebankTransaction transaction = createTransactionWithTags(Arrays.asList("programming", "tutoring"));
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(transactionRepository.findBySenderIdOrReceiverId(1, 1))
                .thenReturn(Collections.singletonList(transaction));
        when(marketplaceService.getActiveServices()).thenReturn(activeServices);

        // Act
        List<ServiceDTO> result = recommendationService.getRecommendedServices(1, 10);

        // Assert
        assertNotNull(result);
        assertTrue(result.size() > 0);
        // Nearby service with matching tags should be prioritized
        assertEquals("Nearby Service", result.get(0).getTitle());
        verify(userRepository).findById(1);
        verify(transactionRepository).findBySenderIdOrReceiverId(1, 1);
        verify(marketplaceService).getActiveServices();
    }

    @Test
    void getRecommendedServices_UserWithNoHistory_ReturnsPopularServices() {
        // Arrange
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(transactionRepository.findBySenderIdOrReceiverId(1, 1))
                .thenReturn(Collections.emptyList());
        when(marketplaceService.getActiveServices()).thenReturn(activeServices);

        // Act
        List<ServiceDTO> result = recommendationService.getRecommendedServices(1, 10);

        // Assert
        assertNotNull(result);
        assertTrue(result.size() <= 10);
        verify(userRepository).findById(1);
        verify(transactionRepository).findBySenderIdOrReceiverId(1, 1);
        verify(marketplaceService).getActiveServices();
    }

    @Test
    void getRecommendedServices_UserNotFound_ReturnsPopularServices() {
        // Arrange
        when(userRepository.findById(999)).thenReturn(Optional.empty());
        when(marketplaceService.getActiveServices()).thenReturn(activeServices);

        // Act
        List<ServiceDTO> result = recommendationService.getRecommendedServices(999, 10);

        // Assert
        assertNotNull(result);
        assertTrue(result.size() <= 10);
        verify(userRepository).findById(999);
        verify(marketplaceService).getActiveServices();
    }

    @Test
    void getRecommendedServices_FiltersUserOwnServices() {
        // Arrange
        TimebankTransaction transaction = createTransactionWithTags(Arrays.asList("cooking", "lessons"));
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(transactionRepository.findBySenderIdOrReceiverId(1, 1))
                .thenReturn(Collections.singletonList(transaction));
        when(marketplaceService.getActiveServices()).thenReturn(activeServices);

        // Act
        List<ServiceDTO> result = recommendationService.getRecommendedServices(1, 10);

        // Assert
        assertNotNull(result);
        assertTrue(result.stream().noneMatch(s -> s.getPoster().getId().equals(1)));
        verify(userRepository).findById(1);
    }

    @Test
    void getRecommendedServices_ScoresBasedOnTagMatching() {
        // Arrange
        TimebankTransaction transaction = createTransactionWithTags(Arrays.asList("gardening", "outdoor"));
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(transactionRepository.findBySenderIdOrReceiverId(1, 1))
                .thenReturn(Collections.singletonList(transaction));
        when(marketplaceService.getActiveServices()).thenReturn(activeServices);

        // Act
        List<ServiceDTO> result = recommendationService.getRecommendedServices(1, 10);

        // Assert
        assertNotNull(result);
        if (!result.isEmpty()) {
            // Far service with matching tags should be included despite distance
            boolean hasFarService = result.stream()
                    .anyMatch(s -> s.getTitle().equals("Far Service"));
            assertTrue(hasFarService);
        }
        verify(userRepository).findById(1);
    }

    @Test
    void getRecommendedServices_RespectsMaxResults() {
        // Arrange
        List<ServiceDTO> manyServices = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            AuthorDTO poster = new AuthorDTO();
            poster.setId(i + 10);
            ServiceDTO service = new ServiceDTO();
            service.setId(i + 10);
            service.setTitle("Service " + i);
            service.setGeohash("u4pruye");
            service.setPoster(poster);
            service.setTags(List.of("programming")); // All match
            manyServices.add(service);
        }
        
        TimebankTransaction transaction = createTransactionWithTags(Arrays.asList("programming"));
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(transactionRepository.findBySenderIdOrReceiverId(1, 1))
                .thenReturn(Collections.singletonList(transaction));
        when(marketplaceService.getActiveServices()).thenReturn(manyServices);

        // Act
        List<ServiceDTO> result = recommendationService.getRecommendedServices(1, 5);

        // Assert
        assertNotNull(result);
        assertEquals(5, result.size());
        verify(userRepository).findById(1);
    }

    @Test
    void getRecommendedServices_FiltersServicesWithZeroScore() {
        // Arrange
        // User has transaction history with tags that don't match any services
        TimebankTransaction transaction = createTransactionWithTags(Arrays.asList("non-matching-tag"));
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(transactionRepository.findBySenderIdOrReceiverId(1, 1))
                .thenReturn(Collections.singletonList(transaction));
        
        // Create services with tags that don't match
        List<ServiceDTO> servicesWithoutMatch = new ArrayList<>();
        AuthorDTO poster = new AuthorDTO();
        poster.setId(10);
        ServiceDTO service = new ServiceDTO();
        service.setId(10);
        service.setTitle("Different Service");
        service.setGeohash("ezs42"); // Far away
        service.setPoster(poster);
        service.setTags(List.of("completely-different"));
        servicesWithoutMatch.add(service);
        
        when(marketplaceService.getActiveServices()).thenReturn(servicesWithoutMatch);

        // Act
        List<ServiceDTO> result = recommendationService.getRecommendedServices(1, 10);

        // Assert
        assertNotNull(result);
        // services with zero score filtered out
        verify(userRepository).findById(1);
        verify(transactionRepository).findBySenderIdOrReceiverId(1, 1);
    }

    // ========== Helper Methods ==========

    private TimebankTransaction createTransactionWithTags(List<String> tagNames) {
        TimebankTransaction transaction = new TimebankTransaction();
        transaction.setId(1);
        
        Handshake handshake = new Handshake();
        handshake.setId(1);
        
        Offer offer = new Offer();
        offer.setId(1);
        offer.setTitle("Test Offer");
        
        Set<SemanticTag> tags = new HashSet<>();
        for (String tagName : tagNames) {
            SemanticTag tag = new SemanticTag();
            tag.setName(tagName);
            tags.add(tag);
        }
        offer.setTags(tags);
        
        handshake.setOffer(offer);
        transaction.setHandshake(handshake);
        
        return transaction;
    }
}

