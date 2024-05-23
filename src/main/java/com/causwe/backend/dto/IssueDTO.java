package com.causwe.backend.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IssueDTO {

    private Long id;
    private String title;
    private String description;
    private LocalDateTime reportedDate;
    private Long reporterId;
    private Long fixerId;
    private Long assigneeId;
    private Priority priority;
    private Status status;
    private List<Long> commentIds;
    private Long projectId;

    public enum Priority {
        BLOCKER, CRITICAL, MAJOR, MINOR, TRIVIAL
    }

    public enum Status {
        NEW, ASSIGNED, RESOLVED, CLOSED, REOPENED
    }

    public IssueDTO() {}
}