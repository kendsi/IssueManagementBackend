package com.causwe.backend.service;

import com.causwe.backend.model.Issue;
import com.causwe.backend.repository.IssueRepository;

import org.hibernate.annotations.DialectOverride.OverridesAnnotation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.LinkedHashMap;
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
    public Map<String, Long> getIssuesPerFixer(Long projectId) {
        List<Object[]> results = issueRepository.findByProjectPerFixer(projectId);
        Map<String, Long> issuesPerFixers = new LinkedHashMap<>();

        for (Object[] result : results) {
            String fixerKey = (String) result[0];
            Long count = (long) result[1];

            issuesPerFixers.put(fixerKey, count);
        }
        return issuesPerFixers;
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
}