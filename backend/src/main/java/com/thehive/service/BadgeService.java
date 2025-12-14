package com.thehive.service;

import com.thehive.model.entity.Badge;
import com.thehive.model.entity.User;
import com.thehive.model.entity.UserBadge;
import com.thehive.model.entity.UserBadgeId;
import com.thehive.repository.BadgeRepository;
import com.thehive.repository.HandshakeRepository;
import com.thehive.repository.UserBadgeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BadgeService {

    private final BadgeRepository badgeRepository;
    private final UserBadgeRepository userBadgeRepository;
    private final HandshakeRepository handshakeRepository;

    /**
     * Check and award badges to a user based on their activity
     * Only the highest earned badge is kept, previous badges are removed
     */
    @Transactional
    public void checkAndAwardBadges(User user) {
        // Count completed handshakes for this user
        long completedCount = handshakeRepository.countCompletedHandshakesByUserId(user.getId());
        
        // Determine the highest badge earned
        String highestBadge = determineHighestBadge(completedCount);
        
        // Award the highest badge (removes all previous badges)
        awardSingleBadge(user, highestBadge);
    }
    
    /**
     * Determine the highest badge based on completed exchanges
     */
    private String determineHighestBadge(long completedCount) {
        if (completedCount >= 50) {
            return "Champion";
        } else if (completedCount >= 25) {
            return "Veteran";
        } else if (completedCount >= 10) {
            return "Active Member";
        } else if (completedCount >= 3) {
            return "Community Helper";
        } else {
            return "Newcomer";
        }
    }

    /**
     * Award a single badge to a user, removing all previous badges
     */
    @Transactional
    public void awardSingleBadge(User user, String badgeName) {
        Optional<Badge> badgeOpt = badgeRepository.findByName(badgeName);
        
        if (badgeOpt.isEmpty()) {
            log.warn("Badge '{}' not found in database", badgeName);
            return;
        }
        
        Badge badge = badgeOpt.get();
        UserBadgeId userBadgeId = new UserBadgeId(user.getId(), badge.getId());
        
        // Check if user already has this exact badge
        if (userBadgeRepository.existsById(userBadgeId)) {
            log.debug("User {} already has badge '{}'", user.getId(), badgeName);
            return; // Already has this badge, no need to update
        }
        
        // Remove all existing badges for this user
        userBadgeRepository.findByIdUserId(user.getId()).forEach(userBadgeRepository::delete);
        
        // Award the new badge
        UserBadge userBadge = new UserBadge();
        userBadge.setId(userBadgeId);
        userBadge.setUser(user);
        userBadge.setBadge(badge);
        
        userBadgeRepository.save(userBadge);
        log.info("Awarded badge '{}' to user {} (removed previous badges)", badgeName, user.getId());
    }

    /**
     * Award newcomer badge to new users
     */
    @Transactional
    public void awardNewcomerBadge(User user) {
        awardSingleBadge(user, "Newcomer");
    }
}

