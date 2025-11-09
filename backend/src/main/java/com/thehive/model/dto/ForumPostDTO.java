package com.thehive.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ForumPostDTO {
    private Integer id;
    private Integer topicId;
    private AuthorDTO author;
    private String content;
    private LocalDateTime createdAt;
}


