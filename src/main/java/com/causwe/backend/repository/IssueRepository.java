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
    @Query(value = "SELECT * FROM issues i " +
            "WHERE i.project_id = :projectId " +
            "ORDER BY CASE " +
            "  WHEN (SELECT role FROM users WHERE id = :memberId) = 'PL' AND i.status = 'NEW' THEN 0 " +
            "  WHEN (SELECT role FROM users WHERE id = :memberId) = 'PL' AND i.status = 'RESOLVED' THEN 1 " +
            "  WHEN (SELECT role FROM users WHERE id = :memberId) = 'DEV' AND i.assignee_id = :memberId THEN 0 " +
            "  WHEN (SELECT role FROM users WHERE id = :memberId) = 'TESTER' AND i.reporter_id = :memberId AND i.status = 'FIXED' THEN 0 " +
            "  WHEN (SELECT role FROM users WHERE id = :memberId) = 'TESTER' AND i.reporter_id = :memberId THEN 1 " +
            "  ELSE 2 " +
            "END, i.id DESC", nativeQuery = true)
    List<Issue> IssuesByProjectAndUser(@Param("projectId") Long projectId, @Param("memberId") Long memberId);
    List<Issue> findByProjectAndAssigneeOrderByIdDesc(Project project, User assignee);
    List<Issue> findByProjectAndReporterOrderByIdDesc(Project project, User reporter);
    List<Issue> findByProjectAndStatusOrderByIdDesc(Project project, Issue.Status status);

    @Query(value = "SELECT TO_CHAR(DATE_TRUNC('month', reported_date), 'YYYY-MM') AS month, COUNT(*) AS issue_count " +
            "FROM issues " +
            "WHERE project_id = :projectId " +
            "GROUP BY month " +
            "ORDER BY month ASC", nativeQuery = true)
    List<Object[]> findByProjectPerMonth(@Param("projectId") Long projectId);

    @Query(value = "SELECT status, COUNT(*) AS issue_count " +
            "FROM issues " +
            "WHERE project_id = :projectId " +
            "GROUP BY status " +
            "ORDER BY status ASC", nativeQuery = true)
    List<Object[]> findByProjectPerStatus(@Param("projectId") Long projectId);

    @Query(value = "SELECT COUNT(*) FROM issues WHERE project_id = :projectId AND status NOT IN ('RESOLVED', 'CLOSED')", nativeQuery = true)
    Long countRemainingIssues(@Param("projectId") Long projectId);

    @Query(value = "SELECT COUNT(*) FROM issues WHERE project_id = :projectId AND status = :status", nativeQuery = true)
    Long countByProjectAndStatus(@Param("projectId") Long projectId, @Param("status") String status);

    @Query(value = "SELECT COUNT(*) FROM issues WHERE project_id = :projectId AND assignee_id IS NULL", nativeQuery = true)
    Long countByProjectAndAssigneeIsNull(@Param("projectId") Long projectId);

    Long countByProjectId(Long projectId);

    @Query(value = "WITH dates AS (" +
            "    SELECT generate_series(" +
            "        current_date - interval '6 days', " +
            "        current_date, " +
            "        interval '1 day' " +
            "    ) AS day" +
            ") " +
            "SELECT " +
            "    TO_CHAR(d.day, 'MM-DD') AS day, " +
            "    COALESCE(COUNT(i.reported_date), 0) AS issue_count " +
            "FROM " +
            "    dates d " +
            "LEFT JOIN " +
            "    issues i " +
            "ON " +
            "    DATE_TRUNC('day', i.reported_date) = d.day " +
            "    AND i.project_id = :projectId " +
            "    AND i.status = :status " +
            "GROUP BY " +
            "    d.day " +
            "ORDER BY " +
            "    d.day ASC", nativeQuery = true)
    List<Object[]> findIssuesPerDayAndStatusInWeek(@Param("projectId") Long projectId, @Param("status") String status);

    @Query(value = "SELECT u.username as fixer, " +
            "CASE " +
            "    WHEN i.status = 'RESOLVED' THEN 'RESOLVED'" +
            "    WHEN i.status = 'CLOSED' THEN 'CLOSED'" +
            "    ELSE 'OTHER' " +
            "END AS fixer_status, " +
            "COUNT(*) AS issue_count " +
            "FROM issues i " +
            "JOIN users u ON i.fixer_id = u.id " +
            "WHERE i.project_id = :projectId " +
            "AND i.status in ('RESOLVED', 'CLOSED') " +
            "GROUP BY fixer, fixer_status " +
            "ORDER BY fixer, fixer_status", nativeQuery = true)
    List<Object[]> findByProjectPerFixer(@Param("projectId") Long projectId);

    @Query(value = "SELECT i.title , COUNT(*) AS comment_count " +
            "FROM issues i " +
            "JOIN comments c ON i.id = c.issue_id " +
            "WHERE i.project_id = :projectId " +
            "GROUP BY i.title " +
            "ORDER BY comment_count DESC " +
            "LIMIT 3", nativeQuery = true)
    List<Object[]> findByProjectOrderByComments(@Param("projectId") Long projectId);

    @Query(value = "WITH dates AS (" +
            "    SELECT generate_series(" +
            "        current_date - interval '6 days', " +
            "        current_date, " +
            "        CAST('1 day' AS interval) " +
            "    ) AS day" +
            ") " +
            "SELECT " +
            "    TO_CHAR(d.day, 'MM-DD') AS day, " +
            "    COALESCE(COUNT(i.reported_date), 0) AS issue_count " +
            "FROM " +
            "    dates d " +
            "LEFT JOIN " +
            "    issues i " +
            "ON " +
            "    DATE_TRUNC('day', i.reported_date) = DATE_TRUNC('day', d.day) " +
            "    AND i.project_id = :projectId " +
            "    AND i.priority = :priority " +
            "GROUP BY " +
            "    day " +
            "ORDER BY " +
            "    day ASC", nativeQuery = true)
    List<Object[]> findIssuesPerDayAndPriorityInWeek(@Param("projectId") Long projectId, @Param("priority") String priority);

    @Query(value = "WITH dates AS (" +
            "    SELECT generate_series(" +
            "        current_date - interval '29 days', " +
            "        current_date, " +
            "        interval '1 day'" +
            "    ) AS day" +
            ") " +
            "SELECT " +
            "    TO_CHAR(d.day, 'MM-DD') AS day, " +
            "    COALESCE(COUNT(i.reported_date), 0) AS issue_count " +
            "FROM " +
            "    dates d " +
            "LEFT JOIN " +
            "    issues i " +
            "ON " +
            "    DATE_TRUNC('day', i.reported_date) = d.day " +
            "    AND i.project_id = :projectId " +
            "GROUP BY " +
            "    d.day " +
            "ORDER BY " +
            "    d.day ASC", nativeQuery = true)
    List<Object[]> findByProjectPerDayInMonth(@Param("projectId") Long projectId);

    @Query(value = "SELECT priority, COUNT(*) AS issue_count " +
            "FROM issues " +
            "WHERE project_id = :projectId " +
            "AND reported_date >= current_date - interval '30 days' " +
            "GROUP BY priority " +
            "ORDER BY priority ASC", nativeQuery = true)
    List<Object[]> findByProjectPerPriorityInMonth(@Param("projectId") Long projectId);

    @Query(value = "WITH dates AS (" +
            "    SELECT generate_series(" +
            "        current_date - interval '6 days', " +
            "        current_date, " +
            "        interval '1 day' " +
            "    ) AS day" +
            ") " +
            "SELECT " +
            "    TO_CHAR(d.day, 'MM-DD') AS day, " +
            "    COALESCE(COUNT(CASE WHEN i.status = 'NEW' THEN 1 END), 0) AS \"NEW\", " +
            "    COALESCE(COUNT(CASE WHEN i.status = 'ASSIGNED' THEN 1 END), 0) AS \"ASSIGNED\", " +
            "    COALESCE(COUNT(CASE WHEN i.status = 'FIXED' THEN 1 END), 0) AS \"FIXED\", " +
            "    COALESCE(COUNT(CASE WHEN i.status = 'RESOLVED' THEN 1 END), 0) AS \"RESOLVED\", " +
            "    COALESCE(COUNT(CASE WHEN i.status = 'CLOSED' THEN 1 END), 0) AS \"CLOSED\", " +
            "    COALESCE(COUNT(CASE WHEN i.status = 'REOPENED' THEN 1 END), 0) AS \"REOPENED\" " +
            "FROM " +
            "    dates d " +
            "LEFT JOIN " +
            "    issues i " +
            "ON " +
            "    DATE_TRUNC('day', i.reported_date) = d.day " +
            "    AND i.project_id = :projectId " +
            "GROUP BY " +
            "    d.day " +
            "ORDER BY " +
            "    d.day ASC", nativeQuery = true)
    List<Object[]> findIssuesPerDayAndStatusInWeek(@Param("projectId") Long projectId);



    @Modifying
    @Transactional
    @Query(value = "INSERT INTO issue_embeddings (issue_id, issue_embedding) " +
            "VALUES (:issueId, azure_openai.create_embeddings('text-embedding-3-small', :issueTitle)) " +
            "ON CONFLICT (issue_id) " +
            "DO UPDATE SET issue_embedding = EXCLUDED.issue_embedding", nativeQuery = true)
    void embedIssueTitle(@Param("issueId") Long issueId, @Param("issueTitle") String issueTitle);

    @Query(value = "WITH ranked_fixers AS (" +
            "    SELECT i.fixer_id, " +
            "           e.issue_embedding <=> (SELECT e2.issue_embedding FROM issue_embeddings e2 WHERE e2.issue_id = :issueId) AS similarity, " +
            "           ROW_NUMBER() OVER (PARTITION BY i.fixer_id ORDER BY e.issue_embedding <=> (SELECT e2.issue_embedding FROM issue_embeddings e2 WHERE e2.issue_id = :issueId)) AS row_num " +
            "    FROM issue_embeddings e " +
            "    INNER JOIN issues i ON i.id = e.issue_id " +
            "    WHERE (i.status = 'RESOLVED' OR i.status = 'CLOSED') " +
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
    List<Long> findRecommendedAssigneesByIssueId(@Param("issueId") Long issueId);


}