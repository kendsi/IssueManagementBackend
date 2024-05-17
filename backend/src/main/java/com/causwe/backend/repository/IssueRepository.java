package com.causwe.backend.repository;

import com.causwe.backend.model.Issue;
import com.causwe.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface IssueRepository extends JpaRepository<Issue, Long> {
    List<Issue> findByAssignee(User assignee);
    List<Issue> findByReporter(User reporter);
    List<Issue> findByStatus(Issue.Status status);

    @Query(value = "SELECT i.assignee_id " +
            "FROM issue_embeddings e " +
            "INNER JOIN issues i ON i.id = e.issue_id " +
            "WHERE i.status = 'RESOLVED' " +
            "ORDER BY e.issue_embedding <#> azure_openai.create_embeddings('text-embedding-3-small', :newIssueDescription)::vector " +
            "LIMIT 3", nativeQuery = true)
    List<Long> findRecommendedAssignees(@Param("newIssueDescription") String newIssueDescription);
}