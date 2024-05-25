// IssueService.java
package com.causwe.backend.service;

import com.causwe.backend.model.Issue;
import com.causwe.backend.model.User;

import java.io.IOException;
import java.util.List;

public interface IssueService {
    List<Issue> getAllIssues(Long projectId, Long memberId);
    Issue getIssueById(Long id);
    Issue createIssue(Long projectId, Issue issueData, Long memberId);
    Issue updateIssue(Long id, Issue updatedIssue, Long memberId);
    List<Issue> searchIssues(Long projectId, String assigneeUsername, String reporterUsername, Issue.Status status, Long memberId);
    List<Issue> searchIssuesByNL(Long projectId, String userMessage, Long memberId) throws IOException;
    List<User> getRecommendedAssignees(Long projectId, Long id);
}