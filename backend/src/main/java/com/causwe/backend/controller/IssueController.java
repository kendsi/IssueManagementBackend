package com.causwe.backend.controller;

import com.causwe.backend.exceptions.UnauthorizedException;
import com.causwe.backend.model.Comment;
import com.causwe.backend.model.Issue;
import com.causwe.backend.model.User;
import com.causwe.backend.repository.IssueRepository;
import com.causwe.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/issues")
public class IssueController {

    @Autowired
    private IssueRepository issueRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("")
    public ResponseEntity<Issue> createIssue(@RequestBody Issue issueData, @CookieValue(value = "memberId", required = false) Long memberId) {
        System.out.println("Member ID: " + memberId);
        User currentUser = userRepository.findById(memberId).orElse(null);
        if (currentUser == null || memberId == null) {
            throw new UnauthorizedException("User not logged in");
        }

        // 새 Issue 객체 생성
        Issue issue = new Issue(issueData.getTitle(), issueData.getDescription(), currentUser);

        Issue newIssue = issueRepository.save(issue);

        // TODO: 임베딩을 생성해서 'issue_embeddings' 테이블에 저장
        // ...

        return new ResponseEntity<>(newIssue, HttpStatus.CREATED);
    }


    @GetMapping("")
    public ResponseEntity<List<Issue>> getAllIssues() {
        List<Issue> issues = issueRepository.findAll();
        return new ResponseEntity<>(issues, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Issue> getIssueById(@PathVariable Long id) {
        Optional<Issue> issue = issueRepository.findById(id);
        if (issue.isPresent()) {
            return new ResponseEntity<>(issue.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<Comment> addComment(@PathVariable Long id, @RequestBody Comment comment, @CookieValue(value = "memberId", required = false) Long memberId) {
        User currentUser = userRepository.findById(memberId).orElse(null);
        if (currentUser == null || memberId == null) {
            throw new UnauthorizedException("User not logged in");
        }
        Optional<Issue> issue = issueRepository.findById(id);
        if (issue.isPresent()) {
            comment.setUser(currentUser);
            issue.get().addComment(comment);
            issueRepository.save(issue.get());
            return new ResponseEntity<>(comment, HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Issue> updateIssue(@PathVariable Long id, @RequestBody Issue updatedIssue, @CookieValue(value = "memberId", required = false) Long memberId) {
        User currentUser = userRepository.findById(memberId).orElse(null);
        if (currentUser == null || memberId == null) {
            throw new UnauthorizedException("User not logged in");
        }

        Optional<Issue> existingIssue = issueRepository.findById(id);
        if (existingIssue.isPresent()) {
            Issue issue = existingIssue.get();

            // 사용자 역할 기반해서 필드 업데이트 권한 확인
            switch (currentUser.getRole()) {
                case ADMIN:
                    // 관리자는 모든 필드를 업데이트 할 수 있다
                    issue.setTitle(updatedIssue.getTitle());
                    issue.setDescription(updatedIssue.getDescription());
                    issue.setPriority(updatedIssue.getPriority());
                    issue.setStatus(updatedIssue.getStatus());
                    if (updatedIssue.getAssignee() != null) {
                        issue.setAssignee(userRepository.findById(updatedIssue.getAssignee().getId()).orElse(null));
                    }
                    break;
                case PL:
                    // PL은 priority, status, assignee을 업데이트 할 수 있다.
                    issue.setPriority(updatedIssue.getPriority());
                    issue.setStatus(updatedIssue.getStatus());
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
    public ResponseEntity<List<Issue>> searchIssues(
            @RequestParam(value = "assignee", required = false) Long assigneeId,
            @RequestParam(value = "reporter", required = false) Long reporterId,
            @RequestParam(value = "status", required = false) Issue.Status status) {

        if (assigneeId != null) {
            User assignee = userRepository.findById(assigneeId).orElse(null);
            return new ResponseEntity<>(issueRepository.findByAssignee(assignee), HttpStatus.OK);
        } else if (reporterId != null) {
            User reporter = userRepository.findById(reporterId).orElse(null);
            return new ResponseEntity<>(issueRepository.findByReporter(reporter), HttpStatus.OK);
        } else if (status != null) {
            return new ResponseEntity<>(issueRepository.findByStatus(status), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(issueRepository.findAll(), HttpStatus.OK);
        }
    }

    @GetMapping("/{id}/recommendedAssignees")
    public ResponseEntity<List<Long>> getRecommendedAssignees(@PathVariable Long id) {
        Optional<Issue> issue = issueRepository.findById(id);
        if (issue.isPresent()) {
            List<Long> assigneeIds = issueRepository.findRecommendedAssignees(issue.get().getDescription());
            return new ResponseEntity<>(assigneeIds, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}