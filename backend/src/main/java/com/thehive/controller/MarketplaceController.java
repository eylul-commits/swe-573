package com.thehive.controller;

import com.thehive.model.dto.CreateOfferRequest;
import com.thehive.model.dto.CreateRequestRequest;
import com.thehive.model.dto.OfferDTO;
import com.thehive.model.dto.RequestDTO;
import com.thehive.model.dto.ServiceDTO;
import com.thehive.model.dto.ServiceQuestionDTO;
import com.thehive.model.dto.ServiceRatingsResponseDTO;
import com.thehive.service.MarketplaceService;
import com.thehive.service.RecommendationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/marketplace")
@RequiredArgsConstructor
public class MarketplaceController {

    private final MarketplaceService marketplaceService;
    private final RecommendationService recommendationService;

    @GetMapping("/offers")
    public ResponseEntity<List<OfferDTO>> getAllOffers() {
        List<OfferDTO> offers = marketplaceService.getAllOffers();
        return ResponseEntity.ok(offers);
    }

    @GetMapping("/offers/active")
    public ResponseEntity<List<OfferDTO>> getActiveOffers() {
        List<OfferDTO> offers = marketplaceService.getActiveOffers();
        return ResponseEntity.ok(offers);
    }

    @GetMapping("/offers/{id}")
    public ResponseEntity<OfferDTO> getOfferById(@PathVariable Integer id) {
        OfferDTO offer = marketplaceService.getOfferById(id);
        return ResponseEntity.ok(offer);
    }

    @PostMapping("/offers")
    public ResponseEntity<OfferDTO> createOffer(@Valid @RequestBody CreateOfferRequest request) {
        Integer userId = (Integer) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        OfferDTO offer = marketplaceService.createOffer(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(offer);
    }

    @GetMapping("/requests")
    public ResponseEntity<List<RequestDTO>> getAllRequests() {
        List<RequestDTO> requests = marketplaceService.getAllRequests();
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/requests/active")
    public ResponseEntity<List<RequestDTO>> getActiveRequests() {
        List<RequestDTO> requests = marketplaceService.getActiveRequests();
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/requests/{id}")
    public ResponseEntity<RequestDTO> getRequestById(@PathVariable Integer id) {
        RequestDTO request = marketplaceService.getRequestById(id);
        return ResponseEntity.ok(request);
    }

    @PostMapping("/requests")
    public ResponseEntity<RequestDTO> createRequest(@Valid @RequestBody CreateRequestRequest request) {
        Integer userId = (Integer) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        RequestDTO createdRequest = marketplaceService.createRequest(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRequest);
    }

    @GetMapping("/services")
    public ResponseEntity<List<ServiceDTO>> getAllServices() {
        List<ServiceDTO> services = marketplaceService.getAllServices();
        return ResponseEntity.ok(services);
    }

    @GetMapping("/services/active")
    public ResponseEntity<List<ServiceDTO>> getActiveServices() {
        List<ServiceDTO> services = marketplaceService.getActiveServices();
        return ResponseEntity.ok(services);
    }

    @GetMapping("/services/{id}")
    public ResponseEntity<ServiceDTO> getServiceById(@PathVariable Integer id) {
        ServiceDTO service = marketplaceService.getServiceById(id);
        return ResponseEntity.ok(service);
    }

    @GetMapping("/services/{id}/questions")
    public ResponseEntity<List<ServiceQuestionDTO>> getServiceQuestions(@PathVariable Integer id) {
        List<ServiceQuestionDTO> questions = marketplaceService.getQuestionsForService(id);
        return ResponseEntity.ok(questions);
    }

    @GetMapping("/services/{id}/ratings")
    public ResponseEntity<ServiceRatingsResponseDTO> getServiceRatings(@PathVariable Integer id) {
        ServiceRatingsResponseDTO ratings = marketplaceService.getRatingsForService(id);
        return ResponseEntity.ok(ratings);
    }

    @GetMapping("/services/nearby")
    public ResponseEntity<List<ServiceDTO>> getNearbyServices(
            @RequestParam(value = "limit", defaultValue = "6") int limit) {
        Integer userId = (Integer) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<ServiceDTO> services = recommendationService.findNearbyServices(userId, limit);
        return ResponseEntity.ok(services);
    }

    @GetMapping("/services/recommended")
    public ResponseEntity<List<ServiceDTO>> getRecommendedServices(
            @RequestParam(value = "limit", defaultValue = "3") int limit) {
        Integer userId = (Integer) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<ServiceDTO> services = recommendationService.getRecommendedServices(userId, limit);
        return ResponseEntity.ok(services);
    }

    @GetMapping("/offers/user/{userId}")
    public ResponseEntity<List<OfferDTO>> getUserOffers(@PathVariable Integer userId) {
        List<OfferDTO> offers = marketplaceService.getOffersByProvider(userId);
        return ResponseEntity.ok(offers);
    }

    @GetMapping("/requests/user/{userId}")
    public ResponseEntity<List<RequestDTO>> getUserRequests(@PathVariable Integer userId) {
        List<RequestDTO> requests = marketplaceService.getRequestsBySeeker(userId);
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/services/user/{userId}")
    public ResponseEntity<List<ServiceDTO>> getUserServices(@PathVariable Integer userId) {
        List<ServiceDTO> services = marketplaceService.getUserServices(userId);
        return ResponseEntity.ok(services);
    }
}

