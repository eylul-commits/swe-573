package com.thehive.controller;

import com.thehive.model.dto.ConfirmHandshakeRequest;
import com.thehive.model.dto.CreateHandshakeRequest;
import com.thehive.model.dto.CreateRatingRequest;
import com.thehive.model.dto.HandshakeDTO;
import com.thehive.service.HandshakeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/handshakes")
@RequiredArgsConstructor
public class HandshakeController {

    private final HandshakeService handshakeService;

    /**
     * Create a new handshake when accepting an offer
     */
    @PostMapping
    public ResponseEntity<HandshakeDTO> createHandshake(@Valid @RequestBody CreateHandshakeRequest request) {
        Integer userId = (Integer) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        HandshakeDTO handshake = handshakeService.createHandshake(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(handshake);
    }

    /**
     * Confirm handshake and set completion date
     */
    @PostMapping("/{handshakeId}/confirm")
    public ResponseEntity<HandshakeDTO> confirmHandshake(
            @PathVariable Integer handshakeId,
            @Valid @RequestBody ConfirmHandshakeRequest request) {
        Integer userId = (Integer) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        HandshakeDTO handshake = handshakeService.confirmHandshake(handshakeId, userId, request);
        return ResponseEntity.ok(handshake);
    }

    /**
     * Create a rating after completion date has passed
     */
    @PostMapping("/rate")
    public ResponseEntity<HandshakeDTO> createRating(@Valid @RequestBody CreateRatingRequest request) {
        Integer userId = (Integer) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        HandshakeDTO handshake = handshakeService.createRating(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(handshake);
    }

    /**
     * Get all handshakes for the current user
     */
    @GetMapping
    public ResponseEntity<List<HandshakeDTO>> getUserHandshakes() {
        Integer userId = (Integer) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<HandshakeDTO> handshakes = handshakeService.getUserHandshakes(userId);
        return ResponseEntity.ok(handshakes);
    }

    /**
     * Get pending handshakes (waiting for confirmation)
     */
    @GetMapping("/pending")
    public ResponseEntity<List<HandshakeDTO>> getUserPendingHandshakes() {
        Integer userId = (Integer) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<HandshakeDTO> handshakes = handshakeService.getUserPendingHandshakes(userId);
        return ResponseEntity.ok(handshakes);
    }

    /**
     * Get confirmed handshakes (both parties confirmed)
     */
    @GetMapping("/confirmed")
    public ResponseEntity<List<HandshakeDTO>> getUserConfirmedHandshakes() {
        Integer userId = (Integer) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<HandshakeDTO> handshakes = handshakeService.getUserConfirmedHandshakes(userId);
        return ResponseEntity.ok(handshakes);
    }

    /**
     * Cancel a handshake (only if pending and not both confirmed)
     */
    @PostMapping("/{handshakeId}/cancel")
    public ResponseEntity<HandshakeDTO> cancelHandshake(@PathVariable Integer handshakeId) {
        Integer userId = (Integer) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        HandshakeDTO handshake = handshakeService.cancelHandshake(handshakeId, userId);
        return ResponseEntity.ok(handshake);
    }

    /**
     * Get a specific handshake by ID
     */
    @GetMapping("/{handshakeId}")
    public ResponseEntity<HandshakeDTO> getHandshakeById(@PathVariable Integer handshakeId) {
        Integer userId = (Integer) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        HandshakeDTO handshake = handshakeService.getHandshakeById(handshakeId, userId);
        return ResponseEntity.ok(handshake);
    }
}

