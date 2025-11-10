package com.thehive.controller;

import com.thehive.model.dto.OfferDTO;
import com.thehive.model.dto.RequestDTO;
import com.thehive.model.dto.ServiceDTO;
import com.thehive.service.MarketplaceService;
import com.thehive.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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

    @GetMapping("/services/nearby")
    public ResponseEntity<List<ServiceDTO>> getNearbyServices(
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Integer userId,
            @RequestParam(value = "limit", defaultValue = "6") int limit) {
        List<ServiceDTO> services = recommendationService.findNearbyServices(userId, limit);
        return ResponseEntity.ok(services);
    }

    @GetMapping("/services/recommended")
    public ResponseEntity<List<ServiceDTO>> getRecommendedServices(
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Integer userId,
            @RequestParam(value = "limit", defaultValue = "3") int limit) {
        List<ServiceDTO> services = recommendationService.getRecommendedServices(userId, limit);
        return ResponseEntity.ok(services);
    }
}

