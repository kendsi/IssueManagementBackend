package com.causwe.backend.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "issues")
public class Issue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @ManyToOne
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    @Column(nullable = false)
    private LocalDateTime reportedDate;

    @ManyToOne
    @JoinColumn(name = "fixer_id")
    private User fixer;

    @ManyToOne
    @JoinColumn(name = "assignee_id")
    private User assignee;

    @Enumerated(EnumType.STRING)
    private Priority priority;

    @Enumerated(EnumType.STRING)
    private Status status;

    @OneToMany(mappedBy = "issue", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    public enum Priority {
        BLOCKER, CRITICAL, MAJOR, MINOR, TRIVIAL
    }

    public enum Status {
        NEW, ASSIGNED, FIXED, RESOLVED, CLOSED, REOPENED
    }

    public Issue() {}

    public Issue(String title, String description, Priority priority, User reporter) {
        this.title = title;
        this.description = description;
        this.reporter = reporter;
        this.reportedDate = LocalDateTime.now();
        this.priority = priority;
        this.status = Status.NEW;
    }

    // 복사 생성자 추가
    public Issue(Issue issue) {
        this.id = issue.id;
        this.title = issue.title;
        this.description = issue.description;
        this.reporter = issue.reporter;
        this.reportedDate = issue.reportedDate;
        this.fixer = issue.fixer;
        this.assignee = issue.assignee;
        this.priority = issue.priority;
        this.status = issue.status;
        this.comments = new ArrayList<>(issue.comments);
        this.project = issue.project;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public void addComment(Comment comment) {
        comments.add(comment);
        comment.setIssue(this);
    }

    public void deleteComment(Long id) {
        int length = comments.size();
        for (int i = 0; i < length; i++) {
            if (comments.get(i).getId() == id) {
                comments.remove(i);
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Issue issue = (Issue) o;
        return id.equals(issue.id) &&
                title.equals(issue.title) &&
                description.equals(issue.description) &&
                reporter.equals(issue.reporter) &&
                reportedDate.equals(issue.reportedDate) &&
                Objects.equals(fixer, issue.fixer) &&
                Objects.equals(assignee, issue.assignee) &&
                priority == issue.priority &&
                status == issue.status &&
                comments.equals(issue.comments) && // List 비교
                project.equals(issue.project);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, description, reporter, reportedDate, fixer, assignee, priority, status, comments, project);
    }
}