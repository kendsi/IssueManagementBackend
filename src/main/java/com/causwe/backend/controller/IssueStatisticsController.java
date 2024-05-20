package com.causwe.backend.controller;

import com.causwe.backend.service.IssueStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/statistics")
public class IssueStatisticsController {

    @Autowired
    private IssueStatisticsService issueStatisticsService;

    @GetMapping("/issuesPerMonth")
    public ResponseEntity<Map<String, Long>> getIssuesPerMonth() {
        Map<String, Long> issuesPerMonth = issueStatisticsService.getIssuesPerMonth();
        return new ResponseEntity<>(issuesPerMonth, HttpStatus.OK);
    }

    // TODO: 통계 추가
}