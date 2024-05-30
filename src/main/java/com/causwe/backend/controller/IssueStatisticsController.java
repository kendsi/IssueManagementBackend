package com.causwe.backend.controller;

import com.causwe.backend.model.Issue;
import com.causwe.backend.service.IssueStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/api/projects/{projectId}/statistics")
public class IssueStatisticsController {

    @Autowired
    private IssueStatisticsService issueStatisticsService;

    @GetMapping("/issuesPerStatus")
    public ResponseEntity<Map<String, Long>> getIssuesPerStatus(@PathVariable Long projectId) {
        Map<String, Long> issuesPerStatus = issueStatisticsService.getIssuesPerStatus(projectId);
        return new ResponseEntity<>(issuesPerStatus, HttpStatus.OK);
    }

    @GetMapping("/issuesPerFixer")
    public ResponseEntity<Map<String, Long>> getIssuesPerFixer(@PathVariable Long projectId) {
        Map<String, Long> issuesPerFixer = issueStatisticsService.getIssuesPerFixer(projectId);
        return new ResponseEntity<>(issuesPerFixer, HttpStatus.OK);
    }

    @GetMapping("/issuesPerDayAndStatusInWeek")
    public ResponseEntity<Map<String, Long>> getIssuesPerDayAndStatusInWeek(@PathVariable Long projectId, @RequestBody Issue.Status status) {
        if (status != null) {
            Map<String, Long> issuesPerDayAndStatusInWeek = issueStatisticsService.getIssuesPerDayAndStatusInWeek(projectId, status.toString());
            return new ResponseEntity<>(issuesPerDayAndStatusInWeek, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } 
    }

    @GetMapping("/issuesOrderByComments")
    public ResponseEntity<Map<String, Long>> getIssuesOrderByComments(@PathVariable Long projectId) {
        Map<String, Long> issuesOrderByComments = issueStatisticsService.getIssuesOrderByComments(projectId);
        return new ResponseEntity<>(issuesOrderByComments, HttpStatus.OK);
    }

    @GetMapping("/issuesPerDayInMonth")
    public ResponseEntity<Map<String, Long>> getIssuesPerDayInMonth(@PathVariable Long projectId) {
        Map<String, Long> issuesPerDayInMonth = issueStatisticsService.getIssuesPerDayInMonth(projectId);
        return new ResponseEntity<>(issuesPerDayInMonth, HttpStatus.OK);
    }

    @GetMapping("/issuesPerDayAndPriorityInWeek")
    public ResponseEntity<Map<String, Long>> getIssuesPerDayAndPriorityInWeek(@PathVariable Long projectId, @RequestBody Issue.Priority priority) {
        if (priority != null) {
            Map<String, Long> issuesPerDayAndPriorityInWeek = issueStatisticsService.getIssuesPerDayAndPriorityInWeek(projectId, priority.toString());
            return new ResponseEntity<>(issuesPerDayAndPriorityInWeek, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } 
    }

    @GetMapping("/issuesPerMonth")
    public ResponseEntity<Map<String, Long>> getIssuesPerMonth(@PathVariable Long projectId) {
        Map<String, Long> issuesPerMonth = issueStatisticsService.getIssuesPerMonth(projectId);
        return new ResponseEntity<>(issuesPerMonth, HttpStatus.OK);
    }

    @GetMapping("/issuesPerPriorityInMonth")
    public ResponseEntity<Map<String, Long>> getIssuesPerPriorityInMonth(@PathVariable Long projectId) {
        Map<String, Long> issuesPerPriorityInMonth = issueStatisticsService.getIssuesPerPriorityInMonth(projectId);
        return new ResponseEntity<>(issuesPerPriorityInMonth, HttpStatus.OK);
    }
}