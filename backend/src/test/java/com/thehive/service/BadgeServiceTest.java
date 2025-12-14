package com.thehive.service;

import com.thehive.model.entity.Badge;
import com.thehive.model.entity.User;
import com.thehive.model.entity.UserBadge;
import com.thehive.model.entity.UserBadgeId;
import com.thehive.repository.BadgeRepository;
import com.thehive.repository.HandshakeRepository;
import com.thehive.repository.UserBadgeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BadgeServiceTest {

    @Mock
    private BadgeRepository badgeRepository;

    @Mock
    private UserBadgeRepository userBadgeRepository;

    @Mock
    private HandshakeRepository handshakeRepository;

    @InjectMocks
    private BadgeService badgeService;

    private User testUser;
    private Badge newcomerBadge;
    private Badge communityHelperBadge;
    private Badge activeMemberBadge;
    private Badge veteranBadge;
    private Badge championBadge;

    @BeforeEach
    void setUp() {
        // Setup test user
        testUser = new User();
        testUser.setId(1);
        testUser.setEmail("test@example.com");
        testUser.setName("Test User");

        // Setup badges
        newcomerBadge = new Badge();
        newcomerBadge.setId(1);
        newcomerBadge.setName("Newcomer");
        newcomerBadge.setDescription("Awarded for joining the community.");
        newcomerBadge.setIconUrl("🌱");

        communityHelperBadge = new Badge();
        communityHelperBadge.setId(2);
        communityHelperBadge.setName("Community Helper");
        communityHelperBadge.setDescription("Awarded after completing 3 exchanges.");
        communityHelperBadge.setIconUrl("🤝");

        activeMemberBadge = new Badge();
        activeMemberBadge.setId(3);
        activeMemberBadge.setName("Active Member");
        activeMemberBadge.setDescription("Awarded after completing 10 exchanges.");
        activeMemberBadge.setIconUrl("⭐");

        veteranBadge = new Badge();
        veteranBadge.setId(4);
        veteranBadge.setName("Veteran");
        veteranBadge.setDescription("Awarded after completing 25 exchanges.");
        veteranBadge.setIconUrl("🏅");

        championBadge = new Badge();
        championBadge.setId(5);
        championBadge.setName("Champion");
        championBadge.setDescription("Awarded after completing 50 exchanges.");
        championBadge.setIconUrl("🏆");
    }

    @Test
    void awardNewcomerBadge_ShouldAwardNewcomerBadge() {
        // Arrange
        when(badgeRepository.findByName("Newcomer")).thenReturn(Optional.of(newcomerBadge));
        when(userBadgeRepository.existsById(any(UserBadgeId.class))).thenReturn(false);
        when(userBadgeRepository.findByIdUserId(testUser.getId())).thenReturn(Collections.emptyList());

        // Act
        badgeService.awardNewcomerBadge(testUser);

        // Assert
        verify(badgeRepository).findByName("Newcomer");
        verify(userBadgeRepository).save(any(UserBadge.class));
    }

    @Test
    void checkAndAwardBadges_WithZeroCompletedHandshakes_ShouldAwardNewcomerBadge() {
        // Arrange
        when(handshakeRepository.countCompletedHandshakesByUserId(testUser.getId())).thenReturn(0L);
        when(badgeRepository.findByName("Newcomer")).thenReturn(Optional.of(newcomerBadge));
        when(userBadgeRepository.existsById(any(UserBadgeId.class))).thenReturn(false);
        when(userBadgeRepository.findByIdUserId(testUser.getId())).thenReturn(Collections.emptyList());

        // Act
        badgeService.checkAndAwardBadges(testUser);

        // Assert
        verify(handshakeRepository).countCompletedHandshakesByUserId(testUser.getId());
        verify(badgeRepository).findByName("Newcomer");
        verify(userBadgeRepository).save(any(UserBadge.class));
    }

    @Test
    void checkAndAwardBadges_With3CompletedHandshakes_ShouldAwardCommunityHelperBadge() {
        // Arrange
        when(handshakeRepository.countCompletedHandshakesByUserId(testUser.getId())).thenReturn(3L);
        when(badgeRepository.findByName("Community Helper")).thenReturn(Optional.of(communityHelperBadge));
        when(userBadgeRepository.existsById(any(UserBadgeId.class))).thenReturn(false);
        when(userBadgeRepository.findByIdUserId(testUser.getId())).thenReturn(Collections.emptyList());

        // Act
        badgeService.checkAndAwardBadges(testUser);

        // Assert
        verify(badgeRepository).findByName("Community Helper");
        verify(userBadgeRepository).save(any(UserBadge.class));
    }

    @Test
    void checkAndAwardBadges_With10CompletedHandshakes_ShouldAwardActiveMemberBadge() {
        // Arrange
        when(handshakeRepository.countCompletedHandshakesByUserId(testUser.getId())).thenReturn(10L);
        when(badgeRepository.findByName("Active Member")).thenReturn(Optional.of(activeMemberBadge));
        when(userBadgeRepository.existsById(any(UserBadgeId.class))).thenReturn(false);
        when(userBadgeRepository.findByIdUserId(testUser.getId())).thenReturn(Collections.emptyList());

        // Act
        badgeService.checkAndAwardBadges(testUser);

        // Assert
        verify(badgeRepository).findByName("Active Member");
        verify(userBadgeRepository).save(any(UserBadge.class));
    }

    @Test
    void checkAndAwardBadges_With25CompletedHandshakes_ShouldAwardVeteranBadge() {
        // Arrange
        when(handshakeRepository.countCompletedHandshakesByUserId(testUser.getId())).thenReturn(25L);
        when(badgeRepository.findByName("Veteran")).thenReturn(Optional.of(veteranBadge));
        when(userBadgeRepository.existsById(any(UserBadgeId.class))).thenReturn(false);
        when(userBadgeRepository.findByIdUserId(testUser.getId())).thenReturn(Collections.emptyList());

        // Act
        badgeService.checkAndAwardBadges(testUser);

        // Assert
        verify(badgeRepository).findByName("Veteran");
        verify(userBadgeRepository).save(any(UserBadge.class));
    }

    @Test
    void checkAndAwardBadges_With50CompletedHandshakes_ShouldAwardChampionBadge() {
        // Arrange
        when(handshakeRepository.countCompletedHandshakesByUserId(testUser.getId())).thenReturn(50L);
        when(badgeRepository.findByName("Champion")).thenReturn(Optional.of(championBadge));
        when(userBadgeRepository.existsById(any(UserBadgeId.class))).thenReturn(false);
        when(userBadgeRepository.findByIdUserId(testUser.getId())).thenReturn(Collections.emptyList());

        // Act
        badgeService.checkAndAwardBadges(testUser);

        // Assert
        verify(badgeRepository).findByName("Champion");
        verify(userBadgeRepository).save(any(UserBadge.class));
    }

    @Test
    void awardSingleBadge_WhenBadgeDoesNotExist_ShouldNotAwardAndLogWarning() {
        // Arrange
        when(badgeRepository.findByName("NonExistentBadge")).thenReturn(Optional.empty());

        // Act
        badgeService.awardSingleBadge(testUser, "NonExistentBadge");

        // Assert
        verify(badgeRepository).findByName("NonExistentBadge");
        verify(userBadgeRepository, never()).save(any(UserBadge.class));
    }

    @Test
    void awardSingleBadge_WhenUserAlreadyHasBadge_ShouldNotAwardAgain() {
        // Arrange
        when(badgeRepository.findByName("Newcomer")).thenReturn(Optional.of(newcomerBadge));
        when(userBadgeRepository.existsById(any(UserBadgeId.class))).thenReturn(true);

        // Act
        badgeService.awardSingleBadge(testUser, "Newcomer");

        // Assert
        verify(badgeRepository).findByName("Newcomer");
        verify(userBadgeRepository).existsById(any(UserBadgeId.class));
        verify(userBadgeRepository, never()).findByIdUserId(testUser.getId());
        verify(userBadgeRepository, never()).save(any(UserBadge.class));
    }

    @Test
    void awardSingleBadge_ShouldRemovePreviousBadgesBeforeAwardingNew() {
        // Arrange
        UserBadge oldBadge = new UserBadge();
        oldBadge.setId(new UserBadgeId(testUser.getId(), newcomerBadge.getId()));
        oldBadge.setUser(testUser);
        oldBadge.setBadge(newcomerBadge);

        List<UserBadge> existingBadges = new ArrayList<>();
        existingBadges.add(oldBadge);

        when(badgeRepository.findByName("Community Helper")).thenReturn(Optional.of(communityHelperBadge));
        when(userBadgeRepository.existsById(any(UserBadgeId.class))).thenReturn(false);
        when(userBadgeRepository.findByIdUserId(testUser.getId())).thenReturn(existingBadges);

        // Act
        badgeService.awardSingleBadge(testUser, "Community Helper");

        // Assert
        verify(userBadgeRepository).findByIdUserId(testUser.getId());
        verify(userBadgeRepository).delete(oldBadge); // Old badge should be deleted
        verify(userBadgeRepository).save(any(UserBadge.class)); // New badge should be saved
    }

    @Test
    void checkAndAwardBadges_EdgeCase_With2CompletedHandshakes_ShouldAwardNewcomerBadge() {
        // Arrange - Just below the threshold for Community Helper (3)
        when(handshakeRepository.countCompletedHandshakesByUserId(testUser.getId())).thenReturn(2L);
        when(badgeRepository.findByName("Newcomer")).thenReturn(Optional.of(newcomerBadge));
        when(userBadgeRepository.existsById(any(UserBadgeId.class))).thenReturn(false);
        when(userBadgeRepository.findByIdUserId(testUser.getId())).thenReturn(Collections.emptyList());

        // Act
        badgeService.checkAndAwardBadges(testUser);

        // Assert
        verify(badgeRepository).findByName("Newcomer");
        verify(badgeRepository, never()).findByName("Community Helper");
    }

    @Test
    void checkAndAwardBadges_EdgeCase_With9CompletedHandshakes_ShouldAwardCommunityHelperBadge() {
        // Arrange - Just below the threshold for Active Member (10)
        when(handshakeRepository.countCompletedHandshakesByUserId(testUser.getId())).thenReturn(9L);
        when(badgeRepository.findByName("Community Helper")).thenReturn(Optional.of(communityHelperBadge));
        when(userBadgeRepository.existsById(any(UserBadgeId.class))).thenReturn(false);
        when(userBadgeRepository.findByIdUserId(testUser.getId())).thenReturn(Collections.emptyList());

        // Act
        badgeService.checkAndAwardBadges(testUser);

        // Assert
        verify(badgeRepository).findByName("Community Helper");
        verify(badgeRepository, never()).findByName("Active Member");
    }

    @Test
    void checkAndAwardBadges_EdgeCase_With24CompletedHandshakes_ShouldAwardActiveMemberBadge() {
        // Arrange - Just below the threshold for Veteran (25)
        when(handshakeRepository.countCompletedHandshakesByUserId(testUser.getId())).thenReturn(24L);
        when(badgeRepository.findByName("Active Member")).thenReturn(Optional.of(activeMemberBadge));
        when(userBadgeRepository.existsById(any(UserBadgeId.class))).thenReturn(false);
        when(userBadgeRepository.findByIdUserId(testUser.getId())).thenReturn(Collections.emptyList());

        // Act
        badgeService.checkAndAwardBadges(testUser);

        // Assert
        verify(badgeRepository).findByName("Active Member");
        verify(badgeRepository, never()).findByName("Veteran");
    }

    @Test
    void checkAndAwardBadges_EdgeCase_With49CompletedHandshakes_ShouldAwardVeteranBadge() {
        // Arrange - Just below the threshold for Champion (50)
        when(handshakeRepository.countCompletedHandshakesByUserId(testUser.getId())).thenReturn(49L);
        when(badgeRepository.findByName("Veteran")).thenReturn(Optional.of(veteranBadge));
        when(userBadgeRepository.existsById(any(UserBadgeId.class))).thenReturn(false);
        when(userBadgeRepository.findByIdUserId(testUser.getId())).thenReturn(Collections.emptyList());

        // Act
        badgeService.checkAndAwardBadges(testUser);

        // Assert
        verify(badgeRepository).findByName("Veteran");
        verify(badgeRepository, never()).findByName("Champion");
    }

    @Test
    void checkAndAwardBadges_With100CompletedHandshakes_ShouldStillAwardChampionBadge() {
        // Arrange - Way above the highest threshold
        when(handshakeRepository.countCompletedHandshakesByUserId(testUser.getId())).thenReturn(100L);
        when(badgeRepository.findByName("Champion")).thenReturn(Optional.of(championBadge));
        when(userBadgeRepository.existsById(any(UserBadgeId.class))).thenReturn(false);
        when(userBadgeRepository.findByIdUserId(testUser.getId())).thenReturn(Collections.emptyList());

        // Act
        badgeService.checkAndAwardBadges(testUser);

        // Assert
        verify(badgeRepository).findByName("Champion");
        verify(userBadgeRepository).save(any(UserBadge.class));
    }

    @Test
    void checkAndAwardBadges_WhenUpgradingBadge_ShouldReplaceOldWithNew() {
        // Arrange - User has Newcomer, now eligible for Community Helper
        UserBadge oldBadge = new UserBadge();
        oldBadge.setId(new UserBadgeId(testUser.getId(), newcomerBadge.getId()));
        oldBadge.setUser(testUser);
        oldBadge.setBadge(newcomerBadge);

        List<UserBadge> existingBadges = new ArrayList<>();
        existingBadges.add(oldBadge);

        when(handshakeRepository.countCompletedHandshakesByUserId(testUser.getId())).thenReturn(5L);
        when(badgeRepository.findByName("Community Helper")).thenReturn(Optional.of(communityHelperBadge));
        when(userBadgeRepository.existsById(any(UserBadgeId.class))).thenReturn(false);
        when(userBadgeRepository.findByIdUserId(testUser.getId())).thenReturn(existingBadges);

        // Act
        badgeService.checkAndAwardBadges(testUser);

        // Assert
        verify(userBadgeRepository).delete(oldBadge); // Old badge deleted
        verify(userBadgeRepository).save(any(UserBadge.class)); // New badge saved
    }
}

