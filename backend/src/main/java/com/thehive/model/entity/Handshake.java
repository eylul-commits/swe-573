package com.thehive.model.entity;

import com.thehive.model.enums.HandshakeStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "handshakes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Handshake {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "offer_id", nullable = false)
    private Offer offer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seeker_id", nullable = false)
    private User seeker;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id", nullable = false)
    private User provider;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private HandshakeStatus status = HandshakeStatus.PENDING;

    @Column(name = "agreed_hours")
    private Integer agreedHours;

    @Column(name = "seeker_confirmed")
    private Boolean seekerConfirmed = false;

    @Column(name = "provider_confirmed")
    private Boolean providerConfirmed = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    // Relationships
    @OneToMany(mappedBy = "handshake", cascade = CascadeType.ALL)
    private Set<TimebankTransaction> transactions = new HashSet<>();

    @OneToMany(mappedBy = "handshake", cascade = CascadeType.ALL)
    private Set<Rating> ratings = new HashSet<>();
}

