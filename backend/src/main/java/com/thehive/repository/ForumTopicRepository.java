package com.thehive.repository;

import com.thehive.model.entity.ForumTopic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ForumTopicRepository extends JpaRepository<ForumTopic, Integer> {
    
    List<ForumTopic> findByAuthorId(Integer authorId);
}

