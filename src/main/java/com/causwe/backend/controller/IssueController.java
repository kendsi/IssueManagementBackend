package com.causwe.backend.controller;

import com.causwe.backend.model.Comment;
import com.causwe.backend.model.Issue;
import com.causwe.backend.model.User;
import com.causwe.backend.service.IssueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/issues")
public class IssueController {

    @Autowired
    private IssueService issueService;

    @GetMapping("")
    public ResponseEntity<List<Issue>> getAllIssues(@PathVariable Long projectId) {
        List<Issue> issues = issueService.getAllIssues(projectId);
        return new ResponseEntity<>(issues, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Issue> getIssueById(@PathVariable Long projectId, @PathVariable Long id) {
        Issue issue = issueService.getIssueById(projectId, id);
        if (issue != null) {
            return new ResponseEntity<>(issue, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("")
    public ResponseEntity<Issue> createIssue(@PathVariable Long projectId, @RequestBody Issue issueData, @CookieValue(value = "memberId", required = false) Long memberId) {
        Issue newIssue = issueService.createIssue(projectId, issueData, memberId);
        return new ResponseEntity<>(newIssue, HttpStatus.CREATED);
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<Comment> addComment(@PathVariable Long projectId, @PathVariable Long id, @RequestBody Comment commentData, @CookieValue(value = "memberId", required = false) Long memberId) {
        Comment comment = issueService.addComment(projectId, id, commentData, memberId);
        if (comment != null) {
            return new ResponseEntity<>(comment, HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Issue> updateIssue(@PathVariable Long projectId, @PathVariable Long id, @RequestBody Issue updatedIssue, @CookieValue(value = "memberId", required = false) Long memberId) {
        Issue updated = issueService.updateIssue(projectId, id, updatedIssue, memberId);
        if (updated != null) {
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
        List<Issue> issues = issueService.searchIssues(projectId, assigneeId, reporterId, status);
        return new ResponseEntity<>(issues, HttpStatus.OK);
    }

    @GetMapping("/{id}/recommendedAssignees")
    public ResponseEntity<List<User>> getRecommendedAssignees(@PathVariable Long projectId, @PathVariable Long id) {
        List<User> recommendedAssignees = issueService.getRecommendedAssignees(projectId, id);
        if (recommendedAssignees != null) {
            return new ResponseEntity<>(recommendedAssignees, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
