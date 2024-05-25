package com.causwe.backend.dto;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IssueDTO {

    private Long id;
    private String title;
    private String description;
    private LocalDateTime reportedDate;
    private String reporterUsername;
    private String fixerUsername;
    private String assigneeUsername;
    private Priority priority;
    private Status status;

    public enum Priority {
        BLOCKER, CRITICAL, MAJOR, MINOR, TRIVIAL
    }

    public enum Status {
        NEW, ASSIGNED, FIXED, RESOLVED, CLOSED, REOPENED
    }

    public IssueDTO() {}
}