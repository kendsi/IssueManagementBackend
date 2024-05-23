package com.causwe.backend.repository;

import com.causwe.backend.model.Comment;
import com.causwe.backend.model.Issue;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByIssue(Issue issue);
}