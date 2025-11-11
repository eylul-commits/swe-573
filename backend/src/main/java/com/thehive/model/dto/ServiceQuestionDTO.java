package com.thehive.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceQuestionDTO {
    private Integer id;
    private AuthorDTO author;
    private String content;
    private LocalDateTime createdAt;
    private ServiceAnswerDTO answer;
}


