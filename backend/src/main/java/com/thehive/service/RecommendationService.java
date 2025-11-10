package com.thehive.service;

import com.thehive.model.dto.ServiceDTO;
import com.thehive.model.entity.*;
import com.thehive.model.enums.ItemStatus;
import com.thehive.repository.*;
import com.thehive.util.GeohashUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final OfferRepository offerRepository;
    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final TimebankTransactionRepository transactionRepository;
    private final HandshakeRepository handshakeRepository;
    private final MarketplaceService marketplaceService;

    @Transactional(readOnly = true)
    public List<ServiceDTO> findNearbyServices(Integer userId, int maxResults) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null || user.getGeohash() == null) {
            // If user has no geohash, return recent services
            return getRecentServices(maxResults);
        }

        String userGeohash = user.getGeohash();
        List<ServiceDTO> allServices = marketplaceService.getActiveServices();
        
        // Calculate distances and filter out user's own services
        List<ServiceWithDistance> servicesWithDistance = allServices.stream()
                .filter(service -> !isUserOwnService(service, userId))
                .map(service -> {
                    String serviceGeohash = service.getGeohash();
                    double distance = GeohashUtil.calculateDistance(userGeohash, serviceGeohash);
                    return new ServiceWithDistance(service, distance);
                })
                .filter(swd -> swd.distance < 100.0) // Within 100km
                .sorted(Comparator.comparingDouble(swd -> swd.distance))
                .limit(maxResults)
                .collect(Collectors.toList());

        // Add distance label to services
        return servicesWithDistance.stream()
                .map(swd -> {
                    ServiceDTO service = swd.service;
                    service.setDistance(GeohashUtil.getDistanceLabel(swd.distance));
                    return service;
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ServiceDTO> getRecommendedServices(Integer userId, int maxResults) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return getPopularServices(maxResults);
        }

        // Get user's transaction history to understand preferences
        List<TimebankTransaction> transactions = transactionRepository
                .findBySenderIdOrReceiverId(userId, userId);
        
        if (transactions.isEmpty()) {
            // New user with no history - return popular services
            return getPopularServices(maxResults);
        }

        // Extract tags from services user has interacted with
        Set<String> userPreferredTags = extractTagsFromTransactions(transactions);
        
        // Get all active services
        List<ServiceDTO> allServices = marketplaceService.getActiveServices();
        
        // Score services based on tag matching
        List<ScoredService> scoredServices = allServices.stream()
                .filter(service -> !isUserOwnService(service, userId))
                .map(service -> {
                    int score = calculateRecommendationScore(service, userPreferredTags, user.getGeohash());
                    return new ScoredService(service, score);
                })
                .filter(ss -> ss.score > 0)
                .sorted((a, b) -> Integer.compare(b.score, a.score))
                .limit(maxResults)
                .collect(Collectors.toList());

        return scoredServices.stream()
                .map(ss -> ss.service)
                .collect(Collectors.toList());
    }

    // Helper methods

    private boolean isUserOwnService(ServiceDTO service, Integer userId) {
        return service.getPoster().getId().equals(userId);
    }

    private Set<String> extractTagsFromTransactions(List<TimebankTransaction> transactions) {
        Set<String> tags = new HashSet<>();
        
        for (TimebankTransaction transaction : transactions) {
            Handshake handshake = transaction.getHandshake();
            if (handshake != null && handshake.getOffer() != null) {
                Offer offer = handshake.getOffer();
                tags.addAll(offer.getTags().stream()
                        .map(SemanticTag::getName)
                        .collect(Collectors.toSet()));
            }
        }
        
        return tags;
    }

    private int calculateRecommendationScore(ServiceDTO service, Set<String> userPreferredTags, String userGeohash) {
        int score = 0;
        
        // Tag matching (highest weight)
        for (String tag : service.getTags()) {
            if (userPreferredTags.contains(tag)) {
                score += 10;
            }
        }
        
        // Proximity bonus (if geohash available)
        if (userGeohash != null && service.getGeohash() != null) {
            double distance = GeohashUtil.calculateDistance(userGeohash, service.getGeohash());
            if (distance < 5) {
                score += 5; // Very close
            } else if (distance < 20) {
                score += 3; // Nearby
            } else if (distance < 50) {
                score += 1; // In the area
            }
        }
        
        // Recency bonus (newer services get slight boost)
        score += 1;
        
        return score;
    }

    private List<ServiceDTO> getRecentServices(int maxResults) {
        return marketplaceService.getActiveServices()
                .stream()
                .limit(maxResults)
                .collect(Collectors.toList());
    }

    private List<ServiceDTO> getPopularServices(int maxResults) {
        // For new users, return services with most common tags or recent ones
        return getRecentServices(maxResults);
    }

    // Inner classes for sorting
    private static class ServiceWithDistance {
        ServiceDTO service;
        double distance;

        ServiceWithDistance(ServiceDTO service, double distance) {
            this.service = service;
            this.distance = distance;
        }
    }

    private static class ScoredService {
        ServiceDTO service;
        int score;

        ScoredService(ServiceDTO service, int score) {
            this.service = service;
            this.score = score;
        }
    }
}

