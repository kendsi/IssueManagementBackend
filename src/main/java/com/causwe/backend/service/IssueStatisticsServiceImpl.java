package com.causwe.backend.service;

import com.causwe.backend.model.Issue;
import com.causwe.backend.repository.IssueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class IssueStatisticsServiceImpl implements IssueStatisticsService {

    private final IssueRepository issueRepository;

    @Autowired
    public IssueStatisticsServiceImpl(IssueRepository issueRepository) {
        this.issueRepository = issueRepository;
    }

    @Override
    @Cacheable("issuesPerMonth")
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