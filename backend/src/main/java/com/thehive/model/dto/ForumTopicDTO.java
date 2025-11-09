package com.thehive.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ForumTopicDTO {
    private Integer id;
    private String title;
    private AuthorDTO author;
    private Integer postCount;
    private Integer views;
    private Integer likes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastActivity;
    private String excerpt; // First post content preview
    private boolean isPinned;
}


