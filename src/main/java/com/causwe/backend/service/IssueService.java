package com.causwe.backend.service;

import com.causwe.backend.exceptions.UnauthorizedException;
import com.causwe.backend.model.Issue;
import com.causwe.backend.model.Project;
import com.causwe.backend.model.User;
import com.causwe.backend.repository.IssueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class IssueService {

    @Autowired
    private IssueRepository issueRepository;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private UserService userService;


    public List<Issue> getAllIssues(Long projectId) {
        Project project = projectService.getProjectById(projectId);
        return issueRepository.findByProject(project);
    }

    public Issue getIssueById(Long id) {
        Optional<Issue> issue = issueRepository.findById(id);
        return issue.orElse(null);
    }

    public Issue createIssue(Long projectId, Issue issueData, Long memberId) {
        User currentUser = userService.getUserById(memberId);
        if (currentUser == null) {
            throw new UnauthorizedException("User not logged in");
        }

        Project project = projectService.getProjectById(projectId);

        Issue issue = new Issue(issueData.getTitle(), issueData.getDescription(), currentUser);
        issue.setProject(project);
        Issue newIssue = issueRepository.save(issue);

        // Embed the issue title using Azure OpenAI and store
        issueRepository.embedIssueTitle(newIssue.getId(), newIssue.getTitle());

        return newIssue;
    }

    public Issue updateIssue(Long id, Issue updatedIssue, Long memberId) {
        User currentUser = userService.getUserById(memberId);
        if (currentUser == null) {
            throw new UnauthorizedException("User not logged in");
        }

        Optional<Issue> existingIssue = issueRepository.findById(id);

        if (existingIssue.isPresent()) {
            Issue issue = existingIssue.get();

            // 사용자 역할 기반해서 필드 업데이트 권한 확인
            switch (currentUser.getRole()) {
                case ADMIN:
                    // 관리자는 모든 필드를 업데이트 할 수 있다
                    if (updatedIssue.getTitle() != null) {
                        issue.setTitle(updatedIssue.getTitle());
                    }
                    if (updatedIssue.getDescription() != null) {
                        issue.setDescription(updatedIssue.getDescription());
                    }
                    if (updatedIssue.getPriority() != null) {
                        issue.setPriority(updatedIssue.getPriority());
                    }
                    if (updatedIssue.getStatus() != null) {
                        issue.setStatus(updatedIssue.getStatus());
                    }
                    if (updatedIssue.getAssignee() != null) {
                        issue.setAssignee(userService.getUserById(updatedIssue.getAssignee().getId()));
                        issue.setStatus(Issue.Status.ASSIGNED);
                    }
                    break;
                case PL:
                    // PL은 priority, status, assignee을 업데이트 할 수 있다.
                    if (updatedIssue.getPriority() != null) {
                        issue.setPriority(updatedIssue.getPriority());
                    }
                    if (updatedIssue.getStatus() != null) {
                        issue.setStatus(updatedIssue.getStatus());
                    }
                    if (updatedIssue.getAssignee() != null) {
                        issue.setAssignee(userService.getUserById(updatedIssue.getAssignee().getId()));
                        issue.setStatus(Issue.Status.ASSIGNED);
                    }
                    break;
                case DEV:
                    // Dev는 status to RESOLVED 그리고 set fixer를 할 수 있다.
                    if (updatedIssue.getStatus() == Issue.Status.RESOLVED) {
                        issue.setStatus(updatedIssue.getStatus());
                        issue.setFixer(currentUser);
                    }
                    break;
                case TESTER:
                    // Tester는 status to REOPENED 할 수 있다.
                    if (updatedIssue.getStatus() == Issue.Status.REOPENED) {
                        issue.setStatus(updatedIssue.getStatus());
                    }
                    break;
            }

            return issueRepository.save(issue);
        } else {
            return null;
        }
    }

    public List<Issue> searchIssues(Long projectId, Long assigneeId, Long reporterId, Issue.Status status) {
        Project project = projectService.getProjectById(projectId);
        if (assigneeId != null) {
            User assignee = userService.getUserById(assigneeId);
            return issueRepository.findByProjectAndAssignee(project, assignee);
        } else if (reporterId != null) {
            User reporter = userService.getUserById(reporterId);
            return issueRepository.findByProjectAndReporter(project, reporter);
        } else if (status != null) {
            return issueRepository.findByProjectAndStatus(project, status);
        } else {
            return issueRepository.findByProject(project);
        }
    }

    public List<User> getRecommendedAssignees(Long projectId, Long id) {
        Optional<Issue> issue = issueRepository.findById(id);

        if (issue.isPresent()) {
            List<Long> assigneeIds = issueRepository.findRecommendedAssigneesByProjectId(projectId, id);
            List<User> unorderedAssignees = new ArrayList<>();

            for (int i = 0; i < assigneeIds.size(); i++) {
                unorderedAssignees.add(userService.getUserById(assigneeIds.get(i)));
            }

            // ID 목록 순서대로 사용자를 정렬
            return assigneeIds.stream()
                    .map(assigneeId -> unorderedAssignees.stream()
                            .filter(user -> user.getId().equals(assigneeId))
                            .findFirst()
                            .orElse(null))
                    .collect(Collectors.toList());
        } else {
            return null;
        }
    }
}
