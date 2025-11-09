package com.thehive.service;

import com.thehive.model.dto.*;
import com.thehive.model.entity.*;
import com.thehive.repository.ForumPostRepository;
import com.thehive.repository.ForumTopicRepository;
import com.thehive.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ForumServiceTest {

    @Mock
    private ForumTopicRepository topicRepository;

    @Mock
    private ForumPostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ForumService forumService;

    private User testUser;
    private ForumTopic testTopic;
    private ForumPost testPost;

    @BeforeEach
    void setUp() {
        // Create test user
        testUser = new User();
        testUser.setId(1);
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setBalanceHours(15);
        testUser.setUserBadges(new java.util.HashSet<>()); // Empty badges by default

        // Create test topic
        testTopic = new ForumTopic();
        testTopic.setId(1);
        testTopic.setTitle("Test Topic");
        testTopic.setAuthor(testUser);
        testTopic.setCreatedAt(LocalDateTime.now());
        testTopic.setUpdatedAt(LocalDateTime.now());

        // Create test post
        testPost = new ForumPost();
        testPost.setId(1);
        testPost.setTopic(testTopic);
        testPost.setAuthor(testUser);
        testPost.setContent("Test content for the post");
        testPost.setCreatedAt(LocalDateTime.now());
    }

    // ==================== GET ALL TOPICS TESTS ====================

    @Test
    void getAllTopics_ShouldReturnListOfTopicDTOs() {
        // Arrange
        when(topicRepository.findAll()).thenReturn(Arrays.asList(testTopic));
        when(postRepository.findByTopicId(anyInt())).thenReturn(Arrays.asList(testPost));

        // Act
        List<ForumTopicDTO> result = forumService.getAllTopics();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Topic", result.get(0).getTitle());
        assertEquals(1, result.get(0).getPostCount());
        verify(topicRepository, times(1)).findAll();
    }

    @Test
    void getAllTopics_ShouldReturnEmptyListWhenNoTopics() {
        // Arrange
        when(topicRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<ForumTopicDTO> result = forumService.getAllTopics();

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(topicRepository, times(1)).findAll();
    }

    // ==================== GET TOPIC BY ID TESTS ====================

    @Test
    void getTopicById_ShouldReturnTopicDTO_WhenTopicExists() {
        // Arrange
        when(topicRepository.findById(1)).thenReturn(Optional.of(testTopic));
        when(postRepository.findByTopicId(1)).thenReturn(Arrays.asList(testPost));

        // Act
        ForumTopicDTO result = forumService.getTopicById(1);

        // Assert
        assertNotNull(result);
        assertEquals("Test Topic", result.getTitle());
        assertEquals(1, result.getPostCount());
        verify(topicRepository, times(1)).findById(1);
    }

    @Test
    void getTopicById_ShouldThrowException_WhenTopicNotFound() {
        // Arrange
        when(topicRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> forumService.getTopicById(999));
        
        assertEquals("Topic not found with id: 999", exception.getMessage());
        verify(topicRepository, times(1)).findById(999);
    }

    // ==================== CREATE TOPIC TESTS ====================

    @Test
    void createTopic_ShouldCreateTopicWithInitialPost_WhenContentProvided() {
        // Arrange
        CreateForumTopicRequest request = new CreateForumTopicRequest();
        request.setTitle("New Topic");
        request.setInitialPostContent("This is the first post content");

        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(topicRepository.save(any(ForumTopic.class))).thenReturn(testTopic);
        when(postRepository.save(any(ForumPost.class))).thenReturn(testPost);
        when(postRepository.findByTopicId(anyInt())).thenReturn(Arrays.asList(testPost));

        // Act
        ForumTopicDTO result = forumService.createTopic(request, 1);

        // Assert
        assertNotNull(result);
        verify(userRepository, times(1)).findById(1);
        verify(topicRepository, times(1)).save(any(ForumTopic.class));
        verify(postRepository, times(1)).save(any(ForumPost.class)); // Initial post created
    }

    @Test
    void createTopic_ShouldCreateTopicWithoutPost_WhenContentNotProvided() {
        // Arrange
        CreateForumTopicRequest request = new CreateForumTopicRequest();
        request.setTitle("New Topic");
        request.setInitialPostContent(null); // No content

        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(topicRepository.save(any(ForumTopic.class))).thenReturn(testTopic);
        when(postRepository.findByTopicId(anyInt())).thenReturn(Collections.emptyList());

        // Act
        ForumTopicDTO result = forumService.createTopic(request, 1);

        // Assert
        assertNotNull(result);
        verify(topicRepository, times(1)).save(any(ForumTopic.class));
        verify(postRepository, never()).save(any(ForumPost.class)); // postRepository'nin "save" methodu hiç çağırılmamış olmalı
    }

    @Test
    void createTopic_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        CreateForumTopicRequest request = new CreateForumTopicRequest();
        request.setTitle("New Topic");
        request.setInitialPostContent("Content");

        when(userRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> forumService.createTopic(request, 999));
        
        assertEquals("User not found with id: 999", exception.getMessage());
        verify(topicRepository, never()).save(any(ForumTopic.class));
    }

    // ==================== DELETE TOPIC TESTS ====================

    @Test
    void deleteTopic_ShouldDeleteTopic_WhenTopicExists() {
        // Arrange
        when(topicRepository.existsById(1)).thenReturn(true);
        doNothing().when(topicRepository).deleteById(1);

        // Act
        forumService.deleteTopic(1);

        // Assert
        verify(topicRepository, times(1)).existsById(1);
        verify(topicRepository, times(1)).deleteById(1);
    }

    @Test
    void deleteTopic_ShouldThrowException_WhenTopicNotFound() {
        // Arrange
        when(topicRepository.existsById(999)).thenReturn(false);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> forumService.deleteTopic(999));
        
        assertEquals("Topic not found with id: 999", exception.getMessage());
        verify(topicRepository, never()).deleteById(anyInt());
    }

    // ==================== GET POSTS BY TOPIC ID TESTS ====================

    @Test
    void getPostsByTopicId_ShouldReturnListOfPostDTOs() {
        // Arrange
        when(postRepository.findByTopicId(1)).thenReturn(Arrays.asList(testPost));

        // Act
        List<ForumPostDTO> result = forumService.getPostsByTopicId(1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test content for the post", result.get(0).getContent());
        verify(postRepository, times(1)).findByTopicId(1);
    }

    @Test
    void getPostsByTopicId_ShouldReturnEmptyList_WhenNoPosts() {
        // Arrange
        when(postRepository.findByTopicId(1)).thenReturn(Collections.emptyList());

        // Act
        List<ForumPostDTO> result = forumService.getPostsByTopicId(1);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    // ==================== CREATE POST TESTS ====================

    @Test
    void createPost_ShouldCreatePostAndUpdateTopicTimestamp() {
        // Arrange
        CreateForumPostRequest request = new CreateForumPostRequest();
        request.setContent("New post content");

        when(topicRepository.findById(1)).thenReturn(Optional.of(testTopic));
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(postRepository.save(any(ForumPost.class))).thenReturn(testPost);
        when(topicRepository.save(any(ForumTopic.class))).thenReturn(testTopic);

        LocalDateTime beforeUpdate = testTopic.getUpdatedAt();

        // Act
        ForumPostDTO result = forumService.createPost(1, request, 1);

        // Assert
        assertNotNull(result);
        verify(postRepository, times(1)).save(any(ForumPost.class));
        verify(topicRepository, times(1)).save(any(ForumTopic.class)); // Topic updated
    }

    @Test
    void createPost_ShouldThrowException_WhenTopicNotFound() {
        // Arrange
        CreateForumPostRequest request = new CreateForumPostRequest();
        request.setContent("New post content");

        when(topicRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> forumService.createPost(999, request, 1));
        
        assertEquals("Topic not found with id: 999", exception.getMessage());
        verify(postRepository, never()).save(any(ForumPost.class));
    }

    @Test
    void createPost_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        CreateForumPostRequest request = new CreateForumPostRequest();
        request.setContent("New post content");

        when(topicRepository.findById(1)).thenReturn(Optional.of(testTopic));
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> forumService.createPost(1, request, 999));
        
        assertEquals("User not found with id: 999", exception.getMessage());
        verify(postRepository, never()).save(any(ForumPost.class));
    }

    // ==================== DELETE POST TESTS ====================

    @Test
    void deletePost_ShouldDeletePost_WhenPostExists() {
        // Arrange
        when(postRepository.existsById(1)).thenReturn(true);
        doNothing().when(postRepository).deleteById(1);

        // Act
        forumService.deletePost(1);

        // Assert
        verify(postRepository, times(1)).existsById(1);
        verify(postRepository, times(1)).deleteById(1);
    }

    @Test
    void deletePost_ShouldThrowException_WhenPostNotFound() {
        // Arrange
        when(postRepository.existsById(999)).thenReturn(false);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> forumService.deletePost(999));
        
        assertEquals("Post not found with id: 999", exception.getMessage());
        verify(postRepository, never()).deleteById(anyInt());
    }

    // ==================== DTO CONVERSION TESTS ====================

    @Test
    void convertToTopicDTO_ShouldCalculatePostCountCorrectly() {
        // Arrange
        ForumPost post2 = new ForumPost();
        post2.setId(2);
        post2.setTopic(testTopic);
        post2.setAuthor(testUser);
        post2.setContent("Second post");
        post2.setCreatedAt(LocalDateTime.now());

        when(topicRepository.findById(1)).thenReturn(Optional.of(testTopic));
        when(postRepository.findByTopicId(1)).thenReturn(Arrays.asList(testPost, post2));

        // Act
        ForumTopicDTO result = forumService.getTopicById(1);

        // Assert
        assertEquals(2, result.getPostCount());
    }

    @Test
    void convertToTopicDTO_ShouldTruncatePreviewAt150Characters() {
        // Arrange
        String longContent = "A".repeat(200); // 200 characters
        testPost.setContent(longContent);

        when(topicRepository.findById(1)).thenReturn(Optional.of(testTopic));
        when(postRepository.findByTopicId(1)).thenReturn(Arrays.asList(testPost));

        // Act
        ForumTopicDTO result = forumService.getTopicById(1);

        // Assert
        assertNotNull(result.getExcerpt());
        assertEquals(153, result.getExcerpt().length()); // 150 chars + "..."
        assertTrue(result.getExcerpt().endsWith("..."));
    }

    @Test
    void convertToTopicDTO_ShouldNotTruncateShortPreview() {
        // Arrange
        String shortContent = "Short content";
        testPost.setContent(shortContent);

        when(topicRepository.findById(1)).thenReturn(Optional.of(testTopic));
        when(postRepository.findByTopicId(1)).thenReturn(Arrays.asList(testPost));

        // Act
        ForumTopicDTO result = forumService.getTopicById(1);

        // Assert
        assertEquals(shortContent, result.getExcerpt());
        assertFalse(result.getExcerpt().endsWith("..."));
    }

    @Test
    void convertToAuthorDTO_ShouldUseDefaultBadgeWhenNoBadgesInDatabase() {
        // Arrange - user with no badges
        testUser.setUserBadges(new java.util.HashSet<>());
        when(topicRepository.findById(1)).thenReturn(Optional.of(testTopic));
        when(postRepository.findByTopicId(1)).thenReturn(Arrays.asList(testPost));

        // Act
        ForumTopicDTO result = forumService.getTopicById(1);

        // Assert
        assertEquals("New comer", result.getAuthor().getBadge());
    }

    @Test
    void convertToAuthorDTO_ShouldUseDatabaseBadgeWhenAvailable() {
        // Arrange - user with a badge
        Badge badge = new Badge();
        badge.setId(1);
        badge.setName("Top Contributor");
        badge.setIconUrl("/images/badges/top-contributor.png");
        
        UserBadge userBadge = new UserBadge();
        userBadge.setId(new UserBadgeId(testUser.getId(), badge.getId()));
        userBadge.setUser(testUser);
        userBadge.setBadge(badge);
        userBadge.setEarnedAt(LocalDateTime.now());
        
        java.util.Set<UserBadge> userBadges = new java.util.HashSet<>();
        userBadges.add(userBadge);
        testUser.setUserBadges(userBadges);
        
        when(topicRepository.findById(1)).thenReturn(Optional.of(testTopic));
        when(postRepository.findByTopicId(1)).thenReturn(Arrays.asList(testPost));

        // Act
        ForumTopicDTO result = forumService.getTopicById(1);

        // Assert
        assertEquals("Top Contributor", result.getAuthor().getBadge());
    }

    @Test
    void convertToAuthorDTO_ShouldUseEmailWhenNameIsNull() {
        // Arrange
        testUser.setName(null);
        when(topicRepository.findById(1)).thenReturn(Optional.of(testTopic));
        when(postRepository.findByTopicId(1)).thenReturn(Arrays.asList(testPost));

        // Act
        ForumTopicDTO result = forumService.getTopicById(1);

        // Assert
        assertEquals("test@example.com", result.getAuthor().getName());
    }

    @Test
    void convertToTopicDTO_ShouldFindLastActivityFromLatestPost() {
        // Arrange
        LocalDateTime oldTime = LocalDateTime.now().minusDays(2);
        LocalDateTime newTime = LocalDateTime.now().minusHours(1);

        testPost.setCreatedAt(oldTime);
        
        ForumPost newerPost = new ForumPost();
        newerPost.setId(2);
        newerPost.setTopic(testTopic);
        newerPost.setAuthor(testUser);
        newerPost.setContent("Newer post");
        newerPost.setCreatedAt(newTime);

        when(topicRepository.findById(1)).thenReturn(Optional.of(testTopic));
        when(postRepository.findByTopicId(1)).thenReturn(Arrays.asList(testPost, newerPost));

        // Act
        ForumTopicDTO result = forumService.getTopicById(1);

        // Assert
        assertEquals(newTime, result.getLastActivity());
    }
}

