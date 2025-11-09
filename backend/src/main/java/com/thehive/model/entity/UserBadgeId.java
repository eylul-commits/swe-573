package com.thehive.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserBadgeId implements Serializable {

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "badge_id")
    private Integer badgeId;
}

