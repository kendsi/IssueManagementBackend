package com.causwe.backend.controller;

import com.causwe.backend.exceptions.UnauthorizedException;
import com.causwe.backend.model.Comment;
import com.causwe.backend.model.Issue;
import com.causwe.backend.model.Project;
import com.causwe.backend.model.User;
import com.causwe.backend.repository.IssueRepository;
import com.causwe.backend.repository.ProjectRepository;
import com.causwe.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/projects/{projectId}/issues")
public class IssueController {

    @Autowired
    private IssueRepository issueRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    // Project ID로 프로젝트를 찾는 메소드
    private Project getProject(Long projectId) {
        Optional<Project> project = projectRepository.findById(projectId);
        if (project.isPresent()) {
            return project.get();
        } else {
            throw new IllegalArgumentException("Project not found with ID: " + projectId);
        }
    }

    @PostMapping("")
    public ResponseEntity<Issue> createIssue(@PathVariable Long projectId, @RequestBody Issue issueData, @CookieValue(value = "memberId", required = false) Long memberId) {
        User currentUser = userRepository.findById(memberId).orElse(null);
        if (currentUser == null || memberId == null) {
            throw new UnauthorizedException("User not logged in");
        }

        Project project = getProject(projectId);

        Issue issue = new Issue(issueData.getTitle(), issueData.getDescription(), currentUser);
        issue.setProject(project);
        Issue newIssue = issueRepository.save(issue);
        return new ResponseEntity<>(newIssue, HttpStatus.CREATED);
    }

    @GetMapping("")
    public ResponseEntity<List<Issue>> getAllIssues(@PathVariable Long projectId) {
        Project project = getProject(projectId);
        List<Issue> issues = issueRepository.findByProject(project);
        return new ResponseEntity<>(issues, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Issue> getIssueById(@PathVariable Long projectId, @PathVariable Long id) {
        Project project = getProject(projectId);
        Optional<Issue> issue = issueRepository.findByIdAndProject(id, project);
        if (issue.isPresent()) {
            return new ResponseEntity<>(issue.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<Comment> addComment(@PathVariable Long projectId, @PathVariable Long id, @RequestBody Comment commentData, @CookieValue(value = "memberId", required = false) Long memberId) {
        User currentUser = userRepository.findById(memberId).orElse(null);
        if (currentUser == null || memberId == null) {
            throw new UnauthorizedException("User not logged in");
        }

        Project project = getProject(projectId);
        Optional<Issue> issue = issueRepository.findByIdAndProject(id, project);

        if (issue.isPresent()) {
            Comment comment = new Comment(issue.get(), currentUser, commentData.getContent());
            issue.get().addComment(comment);
            issueRepository.save(issue.get());
            return new ResponseEntity<>(comment, HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Issue> updateIssue(@PathVariable Long projectId, @PathVariable Long id, @RequestBody Issue updatedIssue, @CookieValue(value = "memberId", required = false) Long memberId) {
        User currentUser = userRepository.findById(memberId).orElse(null);
        if (currentUser == null || memberId == null) {
            throw new UnauthorizedException("User not logged in");
        }

        Project project = getProject(projectId);
        Optional<Issue> existingIssue = issueRepository.findByIdAndProject(id, project);

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
                        issue.setAssignee(userRepository.findById(updatedIssue.getAssignee().getId()).orElse(null));
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
                        issue.setAssignee(userRepository.findById(updatedIssue.getAssignee().getId()).orElse(null));
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

            Issue updated = issueRepository.save(issue);
            return new ResponseEntity<>(updated, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<Issue>> searchIssues(@PathVariable Long projectId,
                                                    @RequestParam(value = "assignee", required = false) Long assigneeId,
                                                    @RequestParam(value = "reporter", required = false) Long reporterId,
                                                    @RequestParam(value = "status", required = false) Issue.Status status) {
        Project project = getProject(projectId);
        if (assigneeId != null) {
            User assignee = userRepository.findById(assigneeId).orElse(null);
            return new ResponseEntity<>(issueRepository.findByProjectAndAssignee(project, assignee), HttpStatus.OK);
        } else if (reporterId != null) {
            User reporter = userRepository.findById(reporterId).orElse(null);
            return new ResponseEntity<>(issueRepository.findByProjectAndReporter(project, reporter), HttpStatus.OK);
        } else if (status != null) {
            return new ResponseEntity<>(issueRepository.findByProjectAndStatus(project, status), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(issueRepository.findByProject(project), HttpStatus.OK);
        }
    }

    @GetMapping("/{id}/recommendedAssignees")
    public ResponseEntity<List<Long>> getRecommendedAssignees(@PathVariable Long projectId, @PathVariable Long id) {
        Project project = getProject(projectId);
        Optional<Issue> issue = issueRepository.findByIdAndProject(id, project);

        if (issue.isPresent()) {
            List<Long> assigneeIds = issueRepository.findRecommendedAssignees(issue.get().getDescription()); // Update to filter by project
            return new ResponseEntity<>(assigneeIds, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}