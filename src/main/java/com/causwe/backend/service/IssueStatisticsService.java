package com.causwe.backend.service;

import java.util.Map;

public interface IssueStatisticsService {
    Map<String, Long> getIssuesPerStatus(Long projectId);
    Map<String, Long> getIssuesPerDayAndStatusInWeek(Long projectId, String status);
    Map<String, Long> getIssuesPerFixer(Long projectId);
    Map<String, Long> getIssuesOrderByComments(Long projectId);
    Map<String, Long> getIssuesPerDayInMonth(Long projectId);
    Map<String, Long> getIssuesPerMonth(Long projectId);
    Map<String, Long> getIssuesPerDayAndPriorityInWeek(Long projectId, String priority);
    Map<String, Long> getIssuesPerPriorityInMonth(Long projectId);
}