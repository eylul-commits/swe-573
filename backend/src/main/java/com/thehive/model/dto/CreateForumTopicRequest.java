package com.thehive.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateForumTopicRequest {
    private String title;
    private String initialPostContent; // First post content
}


