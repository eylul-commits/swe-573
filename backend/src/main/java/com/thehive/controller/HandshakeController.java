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
    public ResponseEntity<HandshakeDTO> createHandshake(
            @RequestHeader(value = "X-User-Id") Integer userId,
            @Valid @RequestBody CreateHandshakeRequest request) {
        HandshakeDTO handshake = handshakeService.createHandshake(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(handshake);
    }

    /**
     * Confirm handshake and set completion date
     */
    @PostMapping("/{handshakeId}/confirm")
    public ResponseEntity<HandshakeDTO> confirmHandshake(
            @PathVariable Integer handshakeId,
            @RequestHeader(value = "X-User-Id") Integer userId,
            @Valid @RequestBody ConfirmHandshakeRequest request) {
        HandshakeDTO handshake = handshakeService.confirmHandshake(handshakeId, userId, request);
        return ResponseEntity.ok(handshake);
    }

    /**
     * Create a rating after completion date has passed
     */
    @PostMapping("/rate")
    public ResponseEntity<HandshakeDTO> createRating(
            @RequestHeader(value = "X-User-Id") Integer userId,
            @Valid @RequestBody CreateRatingRequest request) {
        HandshakeDTO handshake = handshakeService.createRating(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(handshake);
    }

    /**
     * Get all handshakes for the current user
     */
    @GetMapping
    public ResponseEntity<List<HandshakeDTO>> getUserHandshakes(
            @RequestHeader(value = "X-User-Id") Integer userId) {
        List<HandshakeDTO> handshakes = handshakeService.getUserHandshakes(userId);
        return ResponseEntity.ok(handshakes);
    }

    /**
     * Get pending handshakes (waiting for confirmation)
     */
    @GetMapping("/pending")
    public ResponseEntity<List<HandshakeDTO>> getUserPendingHandshakes(
            @RequestHeader(value = "X-User-Id") Integer userId) {
        List<HandshakeDTO> handshakes = handshakeService.getUserPendingHandshakes(userId);
        return ResponseEntity.ok(handshakes);
    }

    /**
     * Get confirmed handshakes (both parties confirmed)
     */
    @GetMapping("/confirmed")
    public ResponseEntity<List<HandshakeDTO>> getUserConfirmedHandshakes(
            @RequestHeader(value = "X-User-Id") Integer userId) {
        List<HandshakeDTO> handshakes = handshakeService.getUserConfirmedHandshakes(userId);
        return ResponseEntity.ok(handshakes);
    }

    /**
     * Get a specific handshake by ID
     */
    @GetMapping("/{handshakeId}")
    public ResponseEntity<HandshakeDTO> getHandshakeById(
            @PathVariable Integer handshakeId,
            @RequestHeader(value = "X-User-Id") Integer userId) {
        HandshakeDTO handshake = handshakeService.getHandshakeById(handshakeId, userId);
        return ResponseEntity.ok(handshake);
    }
}

