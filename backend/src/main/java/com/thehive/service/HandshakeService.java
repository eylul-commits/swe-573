package com.thehive.service;

import com.thehive.exception.ResourceNotFoundException;
import com.thehive.model.dto.AuthorDTO;
import com.thehive.model.dto.ConfirmHandshakeRequest;
import com.thehive.model.dto.CreateHandshakeRequest;
import com.thehive.model.dto.CreateRatingRequest;
import com.thehive.model.dto.HandshakeDTO;
import com.thehive.model.entity.Handshake;
import com.thehive.model.entity.Offer;
import com.thehive.model.entity.Rating;
import com.thehive.model.entity.Request;
import com.thehive.model.entity.User;
import com.thehive.model.enums.HandshakeStatus;
import com.thehive.repository.HandshakeRepository;
import com.thehive.repository.OfferRepository;
import com.thehive.repository.RatingRepository;
import com.thehive.repository.RequestRepository;
import com.thehive.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HandshakeService {

    private final HandshakeRepository handshakeRepository;
    private final OfferRepository offerRepository;
    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final RatingRepository ratingRepository;

    @Transactional
    public HandshakeDTO createHandshake(CreateHandshakeRequest request, Integer seekerId) {
        // Validate that either offerId or requestId is provided (but not both)
        if ((request.getOfferId() == null && request.getRequestId() == null) ||
            (request.getOfferId() != null && request.getRequestId() != null)) {
            throw new IllegalArgumentException("Either offerId or requestId must be provided (but not both)");
        }

        Offer offer = null;
        Request serviceRequest = null;
        Integer defaultHours;

        // Validate offer or request exists
        if (request.getOfferId() != null) {
            offer = offerRepository.findById(request.getOfferId())
                    .orElseThrow(() -> new ResourceNotFoundException("Offer not found with id: " + request.getOfferId()));
            defaultHours = offer.getDurationHours();
            
            // Check if handshake already exists for this offer
            if (handshakeRepository.findByOfferIdAndSeekerId(request.getOfferId(), seekerId).isPresent()) {
                throw new IllegalStateException("Handshake already exists for this offer and seeker");
            }
        } else {
            serviceRequest = requestRepository.findById(request.getRequestId())
                    .orElseThrow(() -> new ResourceNotFoundException("Request not found with id: " + request.getRequestId()));
            defaultHours = serviceRequest.getDurationHours();
            
            // Check if handshake already exists for this request
            if (handshakeRepository.findByRequestIdAndSeekerId(request.getRequestId(), seekerId).isPresent()) {
                throw new IllegalStateException("Handshake already exists for this request and seeker");
            }
        }

        // Validate seeker
        User seeker = userRepository.findById(seekerId)
                .orElseThrow(() -> new ResourceNotFoundException("Seeker not found with id: " + seekerId));

        // Validate provider
        User provider = userRepository.findById(request.getProviderId())
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found with id: " + request.getProviderId()));

        // Create handshake
        Handshake handshake = new Handshake();
        handshake.setOffer(offer);
        handshake.setRequest(serviceRequest);
        handshake.setSeeker(seeker);
        handshake.setProvider(provider);
        handshake.setAgreedHours(request.getAgreedHours() != null ? request.getAgreedHours() : defaultHours);
        handshake.setStatus(HandshakeStatus.PENDING);
        handshake.setSeekerConfirmed(false);
        handshake.setProviderConfirmed(false);

        Handshake savedHandshake = handshakeRepository.save(handshake);
        return convertToDTO(savedHandshake, seekerId);
    }

    @Transactional
    public HandshakeDTO confirmHandshake(Integer handshakeId, Integer userId, ConfirmHandshakeRequest request) {
        Handshake handshake = handshakeRepository.findById(handshakeId)
                .orElseThrow(() -> new ResourceNotFoundException("Handshake not found with id: " + handshakeId));

        // Check if user is part of this handshake
        boolean isSeeker = handshake.getSeeker().getId().equals(userId);
        boolean isProvider = handshake.getProvider().getId().equals(userId);

        if (!isSeeker && !isProvider) {
            throw new IllegalStateException("User is not part of this handshake");
        }

        // Check if handshake is already completed
        if (handshake.getStatus() == HandshakeStatus.COMPLETED || handshake.getStatus() == HandshakeStatus.CANCELLED) {
            throw new IllegalStateException("Handshake is already " + handshake.getStatus());
        }

        // Update confirmation status
        if (isSeeker) {
            handshake.setSeekerConfirmed(true);
        } else {
            handshake.setProviderConfirmed(true);
        }

        // If both confirmed, update status and set completion date
        if (handshake.getSeekerConfirmed() && handshake.getProviderConfirmed()) {
            handshake.setStatus(HandshakeStatus.CONFIRMED);
            handshake.setCompletedAt(request.getCompletedAt());
        }

        Handshake savedHandshake = handshakeRepository.save(handshake);
        return convertToDTO(savedHandshake, userId);
    }

    @Transactional
    public HandshakeDTO createRating(CreateRatingRequest request, Integer raterId) {
        Handshake handshake = handshakeRepository.findById(request.getHandshakeId())
                .orElseThrow(() -> new ResourceNotFoundException("Handshake not found with id: " + request.getHandshakeId()));

        // Check if user is part of this handshake
        boolean isSeeker = handshake.getSeeker().getId().equals(raterId);
        boolean isProvider = handshake.getProvider().getId().equals(raterId);

        if (!isSeeker && !isProvider) {
            throw new IllegalStateException("User is not part of this handshake");
        }

        // Check if handshake is confirmed
        if (handshake.getStatus() != HandshakeStatus.CONFIRMED) {
            throw new IllegalStateException("Handshake must be confirmed before rating");
        }

        // Check if completion date has passed
        if (handshake.getCompletedAt() == null || handshake.getCompletedAt().isAfter(LocalDateTime.now())) {
            throw new IllegalStateException("Cannot rate before the completion date");
        }

        // Check if user has already rated
        if (ratingRepository.existsByHandshakeIdAndRaterId(request.getHandshakeId(), raterId)) {
            throw new IllegalStateException("User has already rated this handshake");
        }

        // Validate ratee
        User ratee = userRepository.findById(request.getRateeId())
                .orElseThrow(() -> new ResourceNotFoundException("Ratee not found with id: " + request.getRateeId()));

        User rater = userRepository.findById(raterId)
                .orElseThrow(() -> new ResourceNotFoundException("Rater not found with id: " + raterId));

        // Create rating
        Rating rating = new Rating();
        rating.setHandshake(handshake);
        rating.setRater(rater);
        rating.setRatee(ratee);
        rating.setPunctuality(request.getPunctuality());
        rating.setFriendliness(request.getFriendliness());
        rating.setCommunicative(request.getCommunicative());
        rating.setPreparedness(request.getPreparedness());
        rating.setComment(request.getComment());

        ratingRepository.save(rating);

        // Check if both users have rated, if so update status to COMPLETED
        List<Rating> allRatings = ratingRepository.findByHandshakeId(request.getHandshakeId());
        if (allRatings.size() == 2) {
            handshake.setStatus(HandshakeStatus.COMPLETED);
            handshakeRepository.save(handshake);
        }

        return convertToDTO(handshake, raterId);
    }

    @Transactional(readOnly = true)
    public List<HandshakeDTO> getUserHandshakes(Integer userId) {
        List<Handshake> handshakes = handshakeRepository.findBySeekerIdOrProviderId(userId, userId);
        return handshakes.stream()
                .map(h -> convertToDTO(h, userId))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<HandshakeDTO> getUserPendingHandshakes(Integer userId) {
        List<Handshake> handshakes = handshakeRepository.findByUserIdAndStatus(userId, HandshakeStatus.PENDING);
        return handshakes.stream()
                .map(h -> convertToDTO(h, userId))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<HandshakeDTO> getUserConfirmedHandshakes(Integer userId) {
        List<Handshake> handshakes = handshakeRepository.findByUserIdAndStatus(userId, HandshakeStatus.CONFIRMED);
        return handshakes.stream()
                .map(h -> convertToDTO(h, userId))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public HandshakeDTO getHandshakeById(Integer handshakeId, Integer userId) {
        Handshake handshake = handshakeRepository.findById(handshakeId)
                .orElseThrow(() -> new ResourceNotFoundException("Handshake not found with id: " + handshakeId));

        // Check if user is part of this handshake
        if (!handshake.getSeeker().getId().equals(userId) && !handshake.getProvider().getId().equals(userId)) {
            throw new IllegalStateException("User is not part of this handshake");
        }

        return convertToDTO(handshake, userId);
    }

    private HandshakeDTO convertToDTO(Handshake handshake, Integer currentUserId) {
        HandshakeDTO dto = new HandshakeDTO();
        dto.setId(handshake.getId());
        
        // Set either offerId or requestId and title based on which one exists
        if (handshake.getOffer() != null) {
            dto.setOfferId(handshake.getOffer().getId());
            dto.setOfferTitle(handshake.getOffer().getTitle());
            dto.setRequestId(null);
        } else if (handshake.getRequest() != null) {
            dto.setRequestId(handshake.getRequest().getId());
            dto.setOfferTitle(handshake.getRequest().getTitle());
            dto.setOfferId(null);
        }
        
        dto.setSeeker(convertToAuthorDTO(handshake.getSeeker()));
        dto.setProvider(convertToAuthorDTO(handshake.getProvider()));
        dto.setStatus(handshake.getStatus());
        dto.setAgreedHours(handshake.getAgreedHours());
        dto.setSeekerConfirmed(handshake.getSeekerConfirmed());
        dto.setProviderConfirmed(handshake.getProviderConfirmed());
        dto.setCreatedAt(handshake.getCreatedAt());
        dto.setCompletedAt(handshake.getCompletedAt());

        // Check if user can rate
        boolean canRate = false;
        if (handshake.getStatus() == HandshakeStatus.CONFIRMED &&
                handshake.getCompletedAt() != null &&
                handshake.getCompletedAt().isBefore(LocalDateTime.now())) {
            // Check if user hasn't rated yet
            canRate = !ratingRepository.existsByHandshakeIdAndRaterId(handshake.getId(), currentUserId);
        }
        dto.setCanRate(canRate);

        return dto;
    }

    private AuthorDTO convertToAuthorDTO(User user) {
        AuthorDTO dto = new AuthorDTO();
        dto.setId(user.getId());
        dto.setName(user.getName() != null ? user.getName() : user.getEmail());
        dto.setAvatar(null);
        dto.setBio(user.getBio());
        dto.setProvince(user.getProvince());
        dto.setDistrict(user.getDistrict());
        dto.setBalanceHours(user.getBalanceHours());

        if (user.getUserBadges() != null && !user.getUserBadges().isEmpty()) {
            var latestBadge = user.getUserBadges().stream()
                    .max((ub1, ub2) -> ub1.getEarnedAt().compareTo(ub2.getEarnedAt()))
                    .map(userBadge -> userBadge.getBadge().getName())
                    .orElse("Newcomer");
            dto.setBadge(latestBadge);
        } else {
            dto.setBadge("Newcomer");
        }

        return dto;
    }
}

