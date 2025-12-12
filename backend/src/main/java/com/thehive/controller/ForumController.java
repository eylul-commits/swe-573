package com.thehive.controller;

import com.thehive.model.dto.*;
import com.thehive.service.ForumService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/forum")
@RequiredArgsConstructor
public class ForumController {

    private final ForumService forumService;

    @GetMapping("/topics")
    public ResponseEntity<List<ForumTopicDTO>> getAllTopics() {
        List<ForumTopicDTO> topics = forumService.getAllTopics();
        return ResponseEntity.ok(topics);
    }

    @GetMapping("/topics/{id}")
    public ResponseEntity<ForumTopicDTO> getTopicById(@PathVariable Integer id) {
        ForumTopicDTO topic = forumService.getTopicById(id);
        return ResponseEntity.ok(topic);
    }

    @PostMapping("/topics")
    public ResponseEntity<ForumTopicDTO> createTopic(@RequestBody CreateForumTopicRequest request) {
        Integer authorId = (Integer) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        ForumTopicDTO topic = forumService.createTopic(request, authorId);
        return ResponseEntity.status(HttpStatus.CREATED).body(topic);
    }

    @DeleteMapping("/topics/{id}")
    public ResponseEntity<Void> deleteTopic(@PathVariable Integer id) {
        forumService.deleteTopic(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/topics/{topicId}/posts")
    public ResponseEntity<List<ForumPostDTO>> getPostsByTopicId(@PathVariable Integer topicId) {
        List<ForumPostDTO> posts = forumService.getPostsByTopicId(topicId);
        return ResponseEntity.ok(posts);
    }

    @PostMapping("/topics/{topicId}/posts")
    public ResponseEntity<ForumPostDTO> createPost(
            @PathVariable Integer topicId,
            @RequestBody CreateForumPostRequest request) {
        Integer authorId = (Integer) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        ForumPostDTO post = forumService.createPost(topicId, request, authorId);
        return ResponseEntity.status(HttpStatus.CREATED).body(post);
    }

    @DeleteMapping("/posts/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Integer id) {
        forumService.deletePost(id);
        return ResponseEntity.noContent().build();
    }
}


