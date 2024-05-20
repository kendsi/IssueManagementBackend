package com.causwe.backend.service;

import com.causwe.backend.model.Issue;
import com.causwe.backend.repository.IssueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class IssueStatisticsService {

    @Autowired
    private IssueRepository issueRepository;

    public Map<String, Long> getIssuesPerMonth() {
        List<Issue> allIssues = issueRepository.findAll();
        Map<String, Long> issuesPerMonth = new HashMap<>();

        for (Issue issue : allIssues) {
            YearMonth month = YearMonth.from(issue.getReportedDate());
            String monthKey = month.toString(); // E.g., "2024-01"

            issuesPerMonth.put(monthKey, issuesPerMonth.getOrDefault(monthKey, 0L) + 1);
        }

        return issuesPerMonth;
    }

    // TODO: IssueStaticticsService.java와 함께 통계 기능 추가
}