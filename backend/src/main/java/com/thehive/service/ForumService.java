package com.thehive.service;

import com.thehive.model.dto.*;
import com.thehive.model.entity.ForumPost;
import com.thehive.model.entity.ForumTopic;
import com.thehive.model.entity.User;
import com.thehive.repository.ForumPostRepository;
import com.thehive.repository.ForumTopicRepository;
import com.thehive.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ForumService {

    private final ForumTopicRepository topicRepository;
    private final ForumPostRepository postRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<ForumTopicDTO> getAllTopics() {
        List<ForumTopic> topics = topicRepository.findAll();
        return topics.stream()
                .map(this::convertToTopicDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ForumTopicDTO getTopicById(Integer id) {
        ForumTopic topic = topicRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Topic not found with id: " + id));
        return convertToTopicDTO(topic);
    }

    @Transactional
    public ForumTopicDTO createTopic(CreateForumTopicRequest request, Integer authorId) {
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + authorId));

        // Create topic
        ForumTopic topic = new ForumTopic();
        topic.setAuthor(author);
        topic.setTitle(request.getTitle());
        topic = topicRepository.save(topic);

        // Create initial post if content provided
        if (request.getInitialPostContent() != null && !request.getInitialPostContent().isEmpty()) {
            ForumPost initialPost = new ForumPost();
            initialPost.setTopic(topic);
            initialPost.setAuthor(author);
            initialPost.setContent(request.getInitialPostContent());
            postRepository.save(initialPost);
        }

        return convertToTopicDTO(topic);
    }

    @Transactional
    public void deleteTopic(Integer id) {
        if (!topicRepository.existsById(id)) {
            throw new RuntimeException("Topic not found with id: " + id);
        }
        topicRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<ForumPostDTO> getPostsByTopicId(Integer topicId) {
        List<ForumPost> posts = postRepository.findByTopicId(topicId);
        return posts.stream()
                .map(this::convertToPostDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ForumPostDTO createPost(Integer topicId, CreateForumPostRequest request, Integer authorId) {
        ForumTopic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new RuntimeException("Topic not found with id: " + topicId));
        
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + authorId));

        ForumPost post = new ForumPost();
        post.setTopic(topic);
        post.setAuthor(author);
        post.setContent(request.getContent());
        post = postRepository.save(post);

        // Update topic's updated_at timestamp
        topic.setUpdatedAt(LocalDateTime.now());
        topicRepository.save(topic);

        return convertToPostDTO(post);
    }

    @Transactional
    public void deletePost(Integer id) {
        if (!postRepository.existsById(id)) {
            throw new RuntimeException("Post not found with id: " + id);
        }
        postRepository.deleteById(id);
    }

    // Helper methods to convert entities to DTOs
    private ForumTopicDTO convertToTopicDTO(ForumTopic topic) {
        ForumTopicDTO dto = new ForumTopicDTO();
        dto.setId(topic.getId());
        dto.setTitle(topic.getTitle());
        dto.setAuthor(convertToAuthorDTO(topic.getAuthor()));
        dto.setCreatedAt(topic.getCreatedAt());
        dto.setUpdatedAt(topic.getUpdatedAt());
        
        // Get post count
        int postCount = postRepository.findByTopicId(topic.getId()).size();
        dto.setPostCount(postCount);
        
        // get first post as preview
        List<ForumPost> posts = postRepository.findByTopicId(topic.getId());
        if (!posts.isEmpty()) {
            String content = posts.get(0).getContent();
            dto.setExcerpt(content != null && content.length() > 150 
                ? content.substring(0, 150) + "..." 
                : content);
        }
        
        // Get last activity (last post creation time or topic creation)
        LocalDateTime lastActivity = posts.stream()
                .map(ForumPost::getCreatedAt)
                .max(LocalDateTime::compareTo)
                .orElse(topic.getCreatedAt());
        dto.setLastActivity(lastActivity);
        
        // defaults
        dto.setViews(0);
        dto.setLikes(0);
        dto.setPinned(false);
        
        return dto;
    }

    private ForumPostDTO convertToPostDTO(ForumPost post) {
        ForumPostDTO dto = new ForumPostDTO();
        dto.setId(post.getId());
        dto.setTopicId(post.getTopic().getId());
        dto.setAuthor(convertToAuthorDTO(post.getAuthor()));
        dto.setContent(post.getContent());
        dto.setCreatedAt(post.getCreatedAt());
        return dto;
    }

    private AuthorDTO convertToAuthorDTO(User user) {
        AuthorDTO dto = new AuthorDTO();
        dto.setId(user.getId());
        dto.setName(user.getName() != null ? user.getName() : user.getEmail());
        dto.setAvatar(null); // Could be added to User entity later
        
        if (user.getUserBadges() != null && !user.getUserBadges().isEmpty()) {
            // Get the most recent badge based on earned_at timestamp
            var latestBadge = user.getUserBadges().stream()
                .max((ub1, ub2) -> ub1.getEarnedAt().compareTo(ub2.getEarnedAt()))
                .map(userBadge -> userBadge.getBadge().getName())
                .orElse("Newcomer");
            dto.setBadge(latestBadge);
        } else {
            // Default badge for users with no badges
            dto.setBadge("Newcomer2");
        }
        
        return dto;
    }
}


