package com.causwe.backend.repository;

import com.causwe.backend.model.Issue;
import com.causwe.backend.model.User;
import com.causwe.backend.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface IssueRepository extends JpaRepository<Issue, Long> {
    List<Issue> findByProject(Project project);
    List<Issue> findByProjectAndAssignee(Project project, User assignee);
    List<Issue> findByProjectAndReporter(Project project, User reporter);
    List<Issue> findByProjectAndStatus(Project project, Issue.Status status);
    Optional<Issue> findByIdAndProject(Long id, Project project);

    @Query(value = "SELECT i.assignee_id " +
            "FROM issue_embeddings e " +
            "INNER JOIN issues i ON i.id = e.issue_id " +
            "WHERE i.status = 'RESOLVED' " +
            "ORDER BY e.issue_embedding <#> azure_openai.create_embeddings('text-embedding-3-small', :newIssueDescription)::vector " +
            "LIMIT 3", nativeQuery = true)
    List<Long> findRecommendedAssignees(@Param("newIssueDescription") String newIssueDescription);
}