package com.thehive.service;

import com.thehive.model.dto.AuthorDTO;
import com.thehive.model.dto.OfferDTO;
import com.thehive.model.dto.RequestDTO;
import com.thehive.model.dto.ServiceDTO;
import com.thehive.model.entity.Offer;
import com.thehive.model.entity.Request;
import com.thehive.model.entity.SemanticTag;
import com.thehive.model.entity.User;
import com.thehive.model.enums.ItemStatus;
import com.thehive.repository.OfferRepository;
import com.thehive.repository.RequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MarketplaceService {

    private final OfferRepository offerRepository;
    private final RequestRepository requestRepository;

    @Transactional(readOnly = true)
    public List<OfferDTO> getAllOffers() {
        List<Offer> offers = offerRepository.findAll();
        return offers.stream()
                .map(this::convertToOfferDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OfferDTO> getActiveOffers() {
        List<Offer> offers = offerRepository.findByStatus(ItemStatus.ACTIVE);
        return offers.stream()
                .map(this::convertToOfferDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OfferDTO getOfferById(Integer id) {
        Offer offer = offerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Offer not found with id: " + id));
        return convertToOfferDTO(offer);
    }

    @Transactional(readOnly = true)
    public List<RequestDTO> getAllRequests() {
        List<Request> requests = requestRepository.findAll();
        return requests.stream()
                .map(this::convertToRequestDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RequestDTO> getActiveRequests() {
        List<Request> requests = requestRepository.findByStatus(ItemStatus.ACTIVE);
        return requests.stream()
                .map(this::convertToRequestDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RequestDTO getRequestById(Integer id) {
        Request request = requestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Request not found with id: " + id));
        return convertToRequestDTO(request);
    }

    @Transactional(readOnly = true)
    public List<ServiceDTO> getAllServices() {
        List<ServiceDTO> services = new ArrayList<>();
        
        // Get all offers and convert to ServiceDTO
        List<Offer> offers = offerRepository.findAll();
        services.addAll(offers.stream()
                .map(this::convertOfferToServiceDTO)
                .collect(Collectors.toList()));
        
        // Get all requests and convert to ServiceDTO
        List<Request> requests = requestRepository.findAll();
        services.addAll(requests.stream()
                .map(this::convertRequestToServiceDTO)
                .collect(Collectors.toList()));
        
        return services;
    }

    @Transactional(readOnly = true)
    public List<ServiceDTO> getActiveServices() {
        List<ServiceDTO> services = new ArrayList<>();
        
        // Get active offers and convert to ServiceDTO
        List<Offer> offers = offerRepository.findByStatus(ItemStatus.ACTIVE);
        services.addAll(offers.stream()
                .map(this::convertOfferToServiceDTO)
                .collect(Collectors.toList()));
        
        // Get active requests and convert to ServiceDTO
        List<Request> requests = requestRepository.findByStatus(ItemStatus.ACTIVE);
        services.addAll(requests.stream()
                .map(this::convertRequestToServiceDTO)
                .collect(Collectors.toList()));
        
        return services;
    }

    // Helper methods to convert entities to DTOs
    private OfferDTO convertToOfferDTO(Offer offer) {
        OfferDTO dto = new OfferDTO();
        dto.setId(offer.getId());
        dto.setTitle(offer.getTitle());
        dto.setDescription(offer.getDescription());
        dto.setDurationHours(offer.getDurationHours());
        dto.setStartDate(offer.getStartDate());
        dto.setEndDate(offer.getEndDate());
        dto.setProvince(offer.getProvince());
        dto.setDistrict(offer.getDistrict());
        dto.setGeohash(offer.getGeohash());
        dto.setStatus(offer.getStatus());
        dto.setCreatedAt(offer.getCreatedAt());
        dto.setUpdatedAt(offer.getUpdatedAt());
        dto.setProvider(convertToAuthorDTO(offer.getProvider()));
        dto.setTags(offer.getTags().stream()
                .map(SemanticTag::getName)
                .collect(Collectors.toList()));
        return dto;
    }

    private RequestDTO convertToRequestDTO(Request request) {
        RequestDTO dto = new RequestDTO();
        dto.setId(request.getId());
        dto.setTitle(request.getTitle());
        dto.setDescription(request.getDescription());
        dto.setDurationHours(request.getDurationHours());
        dto.setStartDate(request.getStartDate());
        dto.setEndDate(request.getEndDate());
        dto.setProvince(request.getProvince());
        dto.setDistrict(request.getDistrict());
        dto.setGeohash(request.getGeohash());
        dto.setStatus(request.getStatus());
        dto.setCreatedAt(request.getCreatedAt());
        dto.setUpdatedAt(request.getUpdatedAt());
        dto.setSeeker(convertToAuthorDTO(request.getSeeker()));
        dto.setTags(request.getTags().stream()
                .map(SemanticTag::getName)
                .collect(Collectors.toList()));
        return dto;
    }

    private ServiceDTO convertOfferToServiceDTO(Offer offer) {
        ServiceDTO dto = new ServiceDTO();
        dto.setId(offer.getId());
        dto.setType("OFFER");
        dto.setTitle(offer.getTitle());
        dto.setDescription(offer.getDescription());
        dto.setTimebank(offer.getDurationHours());
        dto.setStartDate(offer.getStartDate());
        dto.setEndDate(offer.getEndDate());
        dto.setProvince(offer.getProvince());
        dto.setDistrict(offer.getDistrict());
        dto.setGeohash(offer.getGeohash());
        dto.setLocation(formatLocation(offer.getProvince(), offer.getDistrict()));
        dto.setStatus(offer.getStatus() != null ? offer.getStatus().toString() : "ACTIVE");
        dto.setCreatedAt(offer.getCreatedAt());
        dto.setUpdatedAt(offer.getUpdatedAt());
        dto.setPoster(convertToAuthorDTO(offer.getProvider()));
        dto.setTags(offer.getTags().stream()
                .map(SemanticTag::getName)
                .collect(Collectors.toList()));
        return dto;
    }

    private ServiceDTO convertRequestToServiceDTO(Request request) {
        ServiceDTO dto = new ServiceDTO();
        dto.setId(request.getId());
        dto.setType("REQUEST");
        dto.setTitle(request.getTitle());
        dto.setDescription(request.getDescription());
        dto.setTimebank(request.getDurationHours());
        dto.setStartDate(request.getStartDate());
        dto.setEndDate(request.getEndDate());
        dto.setProvince(request.getProvince());
        dto.setDistrict(request.getDistrict());
        dto.setGeohash(request.getGeohash());
        dto.setLocation(formatLocation(request.getProvince(), request.getDistrict()));
        dto.setStatus(request.getStatus() != null ? request.getStatus().toString() : "ACTIVE");
        dto.setCreatedAt(request.getCreatedAt());
        dto.setUpdatedAt(request.getUpdatedAt());
        dto.setPoster(convertToAuthorDTO(request.getSeeker()));
        dto.setTags(request.getTags().stream()
                .map(SemanticTag::getName)
                .collect(Collectors.toList()));
        return dto;
    }

    private AuthorDTO convertToAuthorDTO(User user) {
        AuthorDTO dto = new AuthorDTO();
        dto.setId(user.getId());
        dto.setName(user.getName() != null ? user.getName() : user.getEmail());
        dto.setAvatar(null); // değişecek
        
        if (user.getUserBadges() != null && !user.getUserBadges().isEmpty()) {
            // Get the most recent badge based on earned_at timestamp
            var latestBadge = user.getUserBadges().stream()
                .max((ub1, ub2) -> ub1.getEarnedAt().compareTo(ub2.getEarnedAt()))
                .map(userBadge -> userBadge.getBadge().getName())
                .orElse("Newcomer");
            dto.setBadge(latestBadge);
        } else {
            // Default badge for users with no badges
            dto.setBadge("Newcomer");
        }
        
        return dto;
    }

    private String formatLocation(String province, String district) {
        if (province == null && district == null) {
            return "Unknown";
        }
        if (province != null && district != null) {
            return district + ", " + province;
        }
        if (province != null) {
            return province;
        }
        return district;
    }
}

