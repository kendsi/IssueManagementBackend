package com.causwe.backend.controller;

import com.causwe.backend.model.Issue;
import com.causwe.backend.service.IssueStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;


@RestController
@RequestMapping("/api/projects/{projectId}/statistics")
public class IssueStatisticsController {

    @Autowired
    private IssueStatisticsService issueStatisticsService;

    @GetMapping("/issuesPerStatus")
    @Cacheable(value = "issuesPerStatus", key = "#projectId")
    public ResponseEntity<Map<String, Long>> getIssuesPerStatus(@PathVariable Long projectId) {
        Map<String, Long> issuesPerStatus = issueStatisticsService.getIssuesPerStatus(projectId);
        return new ResponseEntity<>(issuesPerStatus, HttpStatus.OK);
    }

    @GetMapping("/issueStatusCounts")
    @Cacheable(value = "issueStatusCounts", key = "#projectId")
    public ResponseEntity<Map<String, Long>> getIssueStatusCounts(@PathVariable Long projectId) {
        Map<String, Long> issueStatusCounts = issueStatisticsService.getIssueStatusCounts(projectId);
        return new ResponseEntity<>(issueStatusCounts, HttpStatus.OK);
    }

    @GetMapping("/issuesPerFixer")
    @Cacheable(value = "issuesPerFixer", key = "#projectId")
    public ResponseEntity<Map<String, Map<String, Long>>> getIssuesPerFixer(@PathVariable Long projectId) {
        Map<String, Map<String, Long>> issuesPerFixer = issueStatisticsService.getIssuesPerFixer(projectId);
        return new ResponseEntity<>(issuesPerFixer, HttpStatus.OK);
    }


    @GetMapping("/issuesPerDayAndStatusInWeek/{status}")
    @Cacheable(value = "issuesPerDayAndStatusInWeek", key = "{#projectId, #status}")
    public ResponseEntity<Map<String, Long>> getIssuesPerDayAndStatusInWeek(@PathVariable Long projectId, @PathVariable Issue.Status status) {
        Map<String, Long> issuesPerDayAndStatusInWeek = issueStatisticsService.getIssuesPerDayAndStatusInWeek(projectId, status.toString());
        return new ResponseEntity<>(issuesPerDayAndStatusInWeek, HttpStatus.OK);
    }

    @GetMapping("/issuesOrderByComments")
    @Cacheable(value = "issuesOrderByComments", key = "#projectId")
    public ResponseEntity<Map<String, Long>> getIssuesOrderByComments(@PathVariable Long projectId) {
        Map<String, Long> issuesOrderByComments = issueStatisticsService.getIssuesOrderByComments(projectId);
        return new ResponseEntity<>(issuesOrderByComments, HttpStatus.OK);
    }

    @GetMapping("/issuesPerDayInMonth")
    @Cacheable(value = "issuesPerDayInMonth", key = "#projectId")
    public ResponseEntity<Map<String, Long>> getIssuesPerDayInMonth(@PathVariable Long projectId) {
        Map<String, Long> issuesPerDayInMonth = issueStatisticsService.getIssuesPerDayInMonth(projectId);
        return new ResponseEntity<>(issuesPerDayInMonth, HttpStatus.OK);
    }

    @GetMapping("/issuesPerDayAndPriorityInWeek/{priority}")
    @Cacheable(value = "issuesPerDayAndPriorityInWeek", key = "{#projectId, #priority}")
    public ResponseEntity<Map<String, Long>> getIssuesPerDayAndPriorityInWeek(@PathVariable Long projectId, @PathVariable Issue.Priority priority) {
        Map<String, Long> issuesPerDayAndPriorityInWeek = issueStatisticsService.getIssuesPerDayAndPriorityInWeek(projectId, priority.toString());
        return new ResponseEntity<>(issuesPerDayAndPriorityInWeek, HttpStatus.OK);
    }

    @GetMapping("/issuesPerMonth")
    @Cacheable(value = "issuesPerMonth", key = "#projectId")
    public ResponseEntity<Map<String, Long>> getIssuesPerMonth(@PathVariable Long projectId) {
        Map<String, Long> issuesPerMonth = issueStatisticsService.getIssuesPerMonth(projectId);
        return new ResponseEntity<>(issuesPerMonth, HttpStatus.OK);
    }

    @GetMapping("/issuesPerPriorityInMonth")
    @Cacheable(value = "issuesPerPriorityInMonth", key = "#projectId")
    public ResponseEntity<Map<String, Long>> getIssuesPerPriorityInMonth(@PathVariable Long projectId) {
        Map<String, Long> issuesPerPriorityInMonth = issueStatisticsService.getIssuesPerPriorityInMonth(projectId);
        return new ResponseEntity<>(issuesPerPriorityInMonth, HttpStatus.OK);
    }

    @GetMapping("/issuesPerDayAndStatusInWeek")
    @Cacheable(value = "getIssuesPerDayAndStatusInWeek", key = "#projectId")
    public ResponseEntity<Map<String, Map<String, Long>>> getIssuesPerDayAndStatusInWeek(@PathVariable Long projectId) {
        Map<String, Map<String, Long>> issuesPerDayAndStatusInWeek = issueStatisticsService.getIssuesPerDayAndStatusInWeek(projectId);
        return new ResponseEntity<>(issuesPerDayAndStatusInWeek, HttpStatus.OK);
    }

}