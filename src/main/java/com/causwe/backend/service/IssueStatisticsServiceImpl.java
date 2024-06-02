package com.causwe.backend.service;
import com.causwe.backend.repository.IssueRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class IssueStatisticsServiceImpl implements IssueStatisticsService {

    private final IssueRepository issueRepository;

    @Autowired
    public IssueStatisticsServiceImpl(IssueRepository issueRepository) {
        this.issueRepository = issueRepository;
    }

    @Override
    public Map<String, Long> getIssuesPerStatus(Long projectId) {
        List<Object[]> results = issueRepository.findByProjectPerStatus(projectId);
        Map<String, Long> issuesPerStatus = new LinkedHashMap<>();

        for (Object[] result : results) {
            String statusKey = (String) result[0];
            Long count = (long) result[1];

            issuesPerStatus.put(statusKey, count);
        }
        return issuesPerStatus;
    }


    @Override
    public Map<String, Long> getIssueStatusCounts(Long projectId) {
        Map<String, Long> issueCounts = new LinkedHashMap<>();
        issueCounts.put("REMAINING", issueRepository.countRemainingIssues(projectId));
        issueCounts.put("RESOLVED", issueRepository.countByProjectAndStatus(projectId, "RESOLVED"));
        issueCounts.put("ASSIGNED", issueRepository.countByProjectAndStatus(projectId, "ASSIGNED"));
        issueCounts.put("UNASSIGNED", issueRepository.countByProjectAndAssigneeIsNull(projectId));
        issueCounts.put("Registered Issues", issueRepository.countByProjectId(projectId));

        return issueCounts;
    }

    @Override
    public Map<String, Long> getIssuesPerDayAndStatusInWeek(Long projectId, String status) {
        List<Object[]> results = issueRepository.findIssuesPerDayAndStatusInWeek(projectId, status);
        Map<String, Long> issuesPerDayAndStatusInWeek = new LinkedHashMap<>();

        for (Object[] result : results) {
            String dayKey = (String) result[0];
            Long count = (long) result[1];

            issuesPerDayAndStatusInWeek.put(dayKey, count);
        }

        return issuesPerDayAndStatusInWeek;
    }

    @Override
    public Map<String, Map<String, Long>> getIssuesPerFixer(Long projectId) {
        List<Object[]> results = issueRepository.findByProjectPerFixer(projectId);
        Map<String, Map<String, Long>> issuesPerFixer = new LinkedHashMap<>();

        for (Object[] result : results) {
            String fixerKey = (String) result[0];
            String statusKey = (String) result[1];
            Long count = (Long) result[2];

            issuesPerFixer.computeIfAbsent(fixerKey, k -> new LinkedHashMap<>())
                    .put(statusKey, count);
        }

        return issuesPerFixer.entrySet().stream()
                .sorted((e1, e2) -> {
                    long count1 = e1.getValue().values().stream().reduce(0L, Long::sum);
                    long count2 = e2.getValue().values().stream().reduce(0L, Long::sum);
                    return Long.compare(count2, count1);
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (e1, e2) -> e1, LinkedHashMap::new));
    }

    @Override
    public Map<String, Long> getIssuesOrderByComments(Long projectId) {
        List<Object[]> results = issueRepository.findByProjectOrderByComments(projectId);
        Map<String, Long> issuesOrderedByComments = new LinkedHashMap<>();

        for (Object[] result : results) {
            String issueKey = (String) result[0];
            Long count = (long) result[1];

            issuesOrderedByComments.put(issueKey, count);
        }

        return issuesOrderedByComments;
    }

    @Override
    public Map<String, Long> getIssuesPerDayInMonth(Long projectId) {
        List<Object[]> results = issueRepository.findByProjectPerDayInMonth(projectId);
        Map<String, Long> issuesPerDayInMonth = new LinkedHashMap<>();

        for (Object[] result : results) {
            String dayKey = (String) result[0];
            Long count = (long) result[1];

            issuesPerDayInMonth.put(dayKey, count);
        }

        return issuesPerDayInMonth;
    }

    @Override
    public Map<String, Long> getIssuesPerDayAndPriorityInWeek(Long projectId, String priority) {
        List<Object[]> results = issueRepository.findIssuesPerDayAndPriorityInWeek(projectId, priority);
        Map<String, Long> issuesPerDayAndPriorityInWeek = new LinkedHashMap<>();

        for (Object[] result : results) {
            String dayKey = (String) result[0];
            Long count = (long) result[1];

            issuesPerDayAndPriorityInWeek.put(dayKey, count);
        }

        return issuesPerDayAndPriorityInWeek;
    }
  
    @Cacheable("issuesPerMonth")
    @Override
    public Map<String, Long> getIssuesPerMonth(Long projectId) {
        List<Object[]> results = issueRepository.findByProjectPerMonth(projectId);
        Map<String, Long> issuesPerMonth = new LinkedHashMap<>();

        for (Object[] result : results) {
            String monthKey = (String) result[0];
            Long count = (long) result[1];

            issuesPerMonth.put(monthKey, count);
        }

        return issuesPerMonth;
    }

    @Override
    public Map<String, Long> getIssuesPerPriorityInMonth(Long projectId) {
        List<Object[]> results = issueRepository.findByProjectPerPriorityInMonth(projectId);
        Map<String, Long> issuesPerPriorityInMonth = new LinkedHashMap<>();

        for (Object[] result : results) {
            String priorityKey = (String) result[0];
            Long count = (long) result[1];

            issuesPerPriorityInMonth.put(priorityKey, count);
        }

        return issuesPerPriorityInMonth;
    }

    @Override
    public Map<String, Map<String, Long>> getIssuesPerDayAndStatusInWeek(Long projectId) {
        List<Object[]> results = issueRepository.findIssuesPerDayAndStatusInWeek(projectId);
        Map<String, Map<String, Long>> issuesPerDayAndStatus = new LinkedHashMap<>();

        for (Object[] result : results) {
            String dayKey = (String) result[0];
            issuesPerDayAndStatus.put(dayKey, new HashMap<>());

            issuesPerDayAndStatus.get(dayKey).put("NEW", (Long) result[1]);
            issuesPerDayAndStatus.get(dayKey).put("ASSIGNED", (Long) result[2]);
            issuesPerDayAndStatus.get(dayKey).put("FIXED", (Long) result[3]);
            issuesPerDayAndStatus.get(dayKey).put("RESOLVED", (Long) result[4]);
            issuesPerDayAndStatus.get(dayKey).put("CLOSED", (Long) result[5]);
            issuesPerDayAndStatus.get(dayKey).put("REOPENED", (Long) result[6]);
        }

        return issuesPerDayAndStatus;
    }
}