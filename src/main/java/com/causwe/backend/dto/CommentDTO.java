package com.causwe.backend.dto;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentDTO {

    private Long id;
    private String username;
    private String content;
    private LocalDateTime createdAt;

    public CommentDTO() {}

    public CommentDTO(Long id, String username, String content, LocalDateTime createdAt) {
        this.id = id;
        this.username = username;
        this.content = content;
        this.createdAt = createdAt;
    }
}