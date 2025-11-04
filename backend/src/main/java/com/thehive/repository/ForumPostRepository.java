package com.thehive.repository;

import com.thehive.model.entity.ForumPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ForumPostRepository extends JpaRepository<ForumPost, Integer> {
    
    List<ForumPost> findByTopicId(Integer topicId);
    
    List<ForumPost> findByAuthorId(Integer authorId);
}

