package com.causwe.backend.service;


import com.causwe.backend.repository.IssueRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) 
public class IssueStatisticsServiceTest {

    @Mock
    private IssueRepository issueRepository;

    @InjectMocks
    private IssueStatisticsServiceImpl issueStatisticsService;

    private List<Object[]> mockResults;
    private Map<String, Long> expected = new LinkedHashMap<>();

    @BeforeEach
    public void setUp() {

    }

    @Test
    public void testGetIssuesPerStatus() {
        mockResults = Arrays.asList(
                new Object[]{"NEW", 5L},
                new Object[]{"ASSINGED", 3L}
        );

        when(issueRepository.findByProjectPerStatus(1L)).thenReturn(mockResults);

        expected.put("NEW", 5L);
        expected.put("ASSINGED", 3L);

        Map<String, Long> result = issueStatisticsService.getIssuesPerStatus(1L);
        assertEquals(expected, result);
    }

    @Test
    public void testGetIssueStatusCounts() {
        expected.put("REMAINING", 10L);
        expected.put("RESOLVED", 5L);
        expected.put("ASSIGNED", 5L);
        expected.put("UNASSIGNED", 5L);
        expected.put("Registered Issues", 25L);

        when(issueRepository.countRemainingIssues(1L)).thenReturn(10L);
        when(issueRepository.countByProjectAndStatus(1L, "RESOLVED")).thenReturn(5L);
        when(issueRepository.countByProjectAndStatus(1L, "ASSIGNED")).thenReturn(5L);
        when(issueRepository.countByProjectAndAssigneeIsNull(1L)).thenReturn(5L);
        when(issueRepository.countByProjectId(1L)).thenReturn(25L);

        Map<String, Long> result = issueStatisticsService.getIssueStatusCounts(1L);
        assertEquals(expected, result);
    }

    @Test
    public void testGetIssuesPerFixer() {
        List<Object[]> mockFixerResults = Arrays.asList(
                new Object[]{"dev1", "RESOLVED", 5L},
                new Object[]{"dev1", "CLOSED", 10L},
                new Object[]{"dev2", "RESOLVED", 3L},
                new Object[]{"dev2", "CLOSED", 0L}
        );
        Map<String, Map<String, Long>> expectedFixerMap = new HashMap<>();

        Map<String, Long> dev1 = new HashMap<>();
        dev1.put("RESOLVED", 5L);
        dev1.put("CLOSED", 10L);
        expectedFixerMap.put("dev1", dev1);
        
        Map<String, Long> dev2 = new HashMap<>();
        dev2.put("RESOLVED", 3L);
        dev2.put("CLOSED", 0L);
        expectedFixerMap.put("dev2", dev2);

        when(issueRepository.findByProjectPerFixer(1L)).thenReturn(mockFixerResults);

        Map<String, Map<String, Long>> result = issueStatisticsService.getIssuesPerFixer(1L);
        assertEquals(expectedFixerMap, result);
    }

    @Test
    public void testGetIssuesPerDayAndStatusInWeek_PerStatus() {
        String status = "ASSIGNED";
        mockResults = Arrays.asList(
                new Object[]{"05-01", 2L},
                new Object[]{"05-02", 3L}
        );

        when(issueRepository.findIssuesPerDayAndStatusInWeek(1L, status)).thenReturn(mockResults);

        expected.put("05-01", 2L);
        expected.put("05-02", 3L);

        Map<String, Long> result = issueStatisticsService.getIssuesPerDayAndStatusInWeek(1L, status);
        assertEquals(expected, result);
    }

    @Test
    public void testGetIssuesPerDayAndStatusInWeek_All() {
        List<Object[]> mockDayResults = Arrays.asList(
                new Object[]{"05-01", 3L, 2L, 1L, 0L, 0L, 0L},
                new Object[]{"05-02", 0L, 0L, 0L, 1L, 2L, 1L}
        );
        Map<String, Map<String, Long>> expectedDayMap = new HashMap<>();

        Map<String, Long> day1 = new HashMap<>();
        day1.put("NEW", 3L);
        day1.put("ASSIGNED", 2L);
        day1.put("FIXED", 1L);
        day1.put("RESOLVED", 0L);
        day1.put("CLOSED", 0L);
        day1.put("REOPENED", 0L);
        expectedDayMap.put("05-01", day1);
        
        Map<String, Long> day2 = new HashMap<>();
        day2.put("NEW", 0L);
        day2.put("ASSIGNED", 0L);
        day2.put("FIXED", 0L);
        day2.put("RESOLVED", 1L);
        day2.put("CLOSED", 2L);
        day2.put("REOPENED", 1L);
        expectedDayMap.put("05-02", day2);

        when(issueRepository.findIssuesPerDayAndStatusInWeek(1L)).thenReturn(mockDayResults);

        Map<String, Map<String, Long>> result = issueStatisticsService.getIssuesPerDayAndStatusInWeek(1L);
        assertEquals(expectedDayMap, result);
    }

    @Test
    public void testGetIssuesOrderByComments() {
        mockResults = Arrays.asList(
                new Object[]{"Issue1", 10L},
                new Object[]{"Issue2", 7L}
        );

        when(issueRepository.findByProjectOrderByComments(1L)).thenReturn(mockResults);

        expected.put("Issue1", 10L);
        expected.put("Issue2", 7L);

        Map<String, Long> result = issueStatisticsService.getIssuesOrderByComments(1L);
        assertEquals(expected, result);
    }

    @Test
    public void testGetIssuesPerDayInMonth() {
        mockResults = Arrays.asList(
                new Object[]{"05-01", 8L},
                new Object[]{"05-02", 12L}
        );

        when(issueRepository.findByProjectPerDayInMonth(1L)).thenReturn(mockResults);

        expected.put("05-01", 8L);
        expected.put("05-02", 12L);

        Map<String, Long> result = issueStatisticsService.getIssuesPerDayInMonth(1L);
        assertEquals(expected, result);
    }

    @Test
    public void testGetIssuesPerDayAndPriorityInWeek() {
        String priority = "MAJOR";
        mockResults = Arrays.asList(
                new Object[]{"05-03", 5L},
                new Object[]{"05-04", 7L}
        );

        when(issueRepository.findIssuesPerDayAndPriorityInWeek(1L, priority)).thenReturn(mockResults);

        expected.put("05-03", 5L);
        expected.put("05-04", 7L);

        Map<String, Long> result = issueStatisticsService.getIssuesPerDayAndPriorityInWeek(1L, priority);
        assertEquals(expected, result);
    }

    @Test
    public void testGetIssuesPerMonth() {
        mockResults = Arrays.asList(
                new Object[]{"2024-04", 15L},
                new Object[]{"2024-05", 20L}
        );

        when(issueRepository.findByProjectPerMonth(1L)).thenReturn(mockResults);

        expected.put("2024-04", 15L);
        expected.put("2024-05", 20L);

        Map<String, Long> result = issueStatisticsService.getIssuesPerMonth(1L);
        assertEquals(expected, result);
    }

    @Test
    public void testGetIssuesPerPriorityInMonth() {
        mockResults = Arrays.asList(
                new Object[]{"MAJOR", 10L},
                new Object[]{"MINOR", 5L}
        );

        when(issueRepository.findByProjectPerPriorityInMonth(1L)).thenReturn(mockResults);

        expected.put("MAJOR", 10L);
        expected.put("MINOR", 5L);

        Map<String, Long> result = issueStatisticsService.getIssuesPerPriorityInMonth(1L);
        assertEquals(expected, result);
    }
}
