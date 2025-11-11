package com.thehive.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceAnswerDTO {
    private Integer id;
    private AuthorDTO responder;
    private String content;
    private LocalDateTime createdAt;
}


