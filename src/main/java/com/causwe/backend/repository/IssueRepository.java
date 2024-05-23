package com.causwe.backend.repository;

import com.causwe.backend.model.Issue;
import com.causwe.backend.model.User;
import com.causwe.backend.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


public interface IssueRepository extends JpaRepository<Issue, Long> {
    List<Issue> findByProject(Project project);
    List<Issue> findByProjectAndAssignee(Project project, User assignee);
    List<Issue> findByProjectAndReporter(Project project, User reporter);
    List<Issue> findByProjectAndStatus(Project project, Issue.Status status);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO issue_embeddings (issue_id, issue_embedding) VALUES (:issueId, (azure_openai.create_embeddings('text-embedding-3-small', :issueTitle)))", nativeQuery = true)
    void embedIssueTitle(@Param("issueId") Long issueId, @Param("issueTitle") String issueTitle);

    @Query(value = "WITH ranked_fixers AS (" +
            "    SELECT i.fixer_id, " +
            "           e.issue_embedding <=> (SELECT e2.issue_embedding FROM issue_embeddings e2 WHERE e2.issue_id = :issueId) AS similarity, " +
            "           ROW_NUMBER() OVER (PARTITION BY i.fixer_id ORDER BY e.issue_embedding <=> (SELECT e2.issue_embedding FROM issue_embeddings e2 WHERE e2.issue_id = :issueId)) AS row_num " +
            "    FROM issue_embeddings e " +
            "    INNER JOIN issues i ON i.id = e.issue_id " +
            "    WHERE i.project_id = :projectId AND (i.status = 'RESOLVED' OR i.status = 'CLOSED') " +
            "), " +
            "unique_fixers AS (" +
            "    SELECT fixer_id, similarity " +
            "    FROM ranked_fixers " +
            "    WHERE row_num = 1 " +
            ") " +
            "SELECT fixer_id " +
            "FROM unique_fixers " +
            "ORDER BY similarity " +
            "LIMIT 3", nativeQuery = true)
    List<Long> findRecommendedAssigneesByProjectId(@Param("projectId") Long projectId, @Param("issueId") Long issueId);

}