package com.thehive.controller;

import com.thehive.model.dto.OfferDTO;
import com.thehive.model.dto.RequestDTO;
import com.thehive.model.dto.ServiceDTO;
import com.thehive.service.MarketplaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/marketplace")
@RequiredArgsConstructor
public class MarketplaceController {

    private final MarketplaceService marketplaceService;

    /**
     * Get all offers
     */
    @GetMapping("/offers")
    public ResponseEntity<List<OfferDTO>> getAllOffers() {
        List<OfferDTO> offers = marketplaceService.getAllOffers();
        return ResponseEntity.ok(offers);
    }

    /**
     * Get active offers only
     */
    @GetMapping("/offers/active")
    public ResponseEntity<List<OfferDTO>> getActiveOffers() {
        List<OfferDTO> offers = marketplaceService.getActiveOffers();
        return ResponseEntity.ok(offers);
    }

    /**
     * Get a specific offer by ID
     */
    @GetMapping("/offers/{id}")
    public ResponseEntity<OfferDTO> getOfferById(@PathVariable Integer id) {
        OfferDTO offer = marketplaceService.getOfferById(id);
        return ResponseEntity.ok(offer);
    }

    /**
     * Get all requests
     */
    @GetMapping("/requests")
    public ResponseEntity<List<RequestDTO>> getAllRequests() {
        List<RequestDTO> requests = marketplaceService.getAllRequests();
        return ResponseEntity.ok(requests);
    }

    /**
     * Get active requests only
     */
    @GetMapping("/requests/active")
    public ResponseEntity<List<RequestDTO>> getActiveRequests() {
        List<RequestDTO> requests = marketplaceService.getActiveRequests();
        return ResponseEntity.ok(requests);
    }

    /**
     * Get a specific request by ID
     */
    @GetMapping("/requests/{id}")
    public ResponseEntity<RequestDTO> getRequestById(@PathVariable Integer id) {
        RequestDTO request = marketplaceService.getRequestById(id);
        return ResponseEntity.ok(request);
    }

    /**
     * Get all services (combined offers and requests)
     */
    @GetMapping("/services")
    public ResponseEntity<List<ServiceDTO>> getAllServices() {
        List<ServiceDTO> services = marketplaceService.getAllServices();
        return ResponseEntity.ok(services);
    }

    /**
     * Get active services only (combined offers and requests)
     */
    @GetMapping("/services/active")
    public ResponseEntity<List<ServiceDTO>> getActiveServices() {
        List<ServiceDTO> services = marketplaceService.getActiveServices();
        return ResponseEntity.ok(services);
    }
}

