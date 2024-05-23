package com.causwe.backend.dto;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentDTO {

    private Long id;
    private Long issueId;
    private Long userId;
    private String content;
    private LocalDateTime createdAt;

    public CommentDTO() {}

    public CommentDTO(Long id, Long issueId, Long userId, String content, LocalDateTime createdAt) {
        this.id = id;
        this.issueId = issueId;
        this.userId = userId;
        this.content = content;
        this.createdAt = createdAt;
    }
}