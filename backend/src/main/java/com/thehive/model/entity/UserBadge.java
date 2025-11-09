package com.thehive.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_badges")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserBadge {

    @EmbeddedId
    private UserBadgeId id;

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @MapsId("badgeId")
    @JoinColumn(name = "badge_id")
    private Badge badge;

    @CreationTimestamp
    @Column(name = "earned_at")
    private LocalDateTime earnedAt;
}

