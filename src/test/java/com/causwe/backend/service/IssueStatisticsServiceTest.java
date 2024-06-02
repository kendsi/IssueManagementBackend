package com.causwe.backend.service;


import com.causwe.backend.repository.IssueRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
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

    @BeforeEach
    public void setUp() {

    }

    @Test
    public void testGetIssuesPerStatus() {
        Long projectId = 1L;
        List<Object[]> mockResults = Arrays.asList(
                new Object[]{"NEW", 5L},
                new Object[]{"ASSINGED", 3L}
        );

        when(issueRepository.findByProjectPerStatus(projectId)).thenReturn(mockResults);

        Map<String, Long> expected = new LinkedHashMap<>();
        expected.put("NEW", 5L);
        expected.put("ASSINGED", 3L);

        Map<String, Long> actual = issueStatisticsService.getIssuesPerStatus(projectId);
        assertEquals(expected, actual);
    }

    @Test
    public void testGetIssuesPerDayAndStatusInWeek() {
        Long projectId = 1L;
        String status = "Open";
        List<Object[]> mockResults = Arrays.asList(
                new Object[]{"05-01", 2L},
                new Object[]{"05-02", 3L}
        );

        when(issueRepository.findIssuesPerDayAndStatusInWeek(projectId, status)).thenReturn(mockResults);

        Map<String, Long> expected = new LinkedHashMap<>();
        expected.put("05-01", 2L);
        expected.put("05-02", 3L);

        Map<String, Long> actual = issueStatisticsService.getIssuesPerDayAndStatusInWeek(projectId, status);
        assertEquals(expected, actual);
    }

    @Test
    public void testGetIssuesPerFixer() {
        Long projectId = 1L;
        List<Object[]> mockResults = Arrays.asList(
                new Object[]{"dev1", 4L},
                new Object[]{"dev2", 6L}
        );

        when(issueRepository.findByProjectPerFixer(projectId)).thenReturn(mockResults);

        Map<String, Long> expected = new LinkedHashMap<>();
        expected.put("dev1", 4L);
        expected.put("dev2", 6L);

        Map<String, Long> actual = issueStatisticsService.getIssuesPerFixer(projectId);
        assertEquals(expected, actual);
    }

    @Test
    public void testGetIssuesOrderByComments() {
        Long projectId = 1L;
        List<Object[]> mockResults = Arrays.asList(
                new Object[]{"Issue1", 10L},
                new Object[]{"Issue2", 7L}
        );

        when(issueRepository.findByProjectOrderByComments(projectId)).thenReturn(mockResults);

        Map<String, Long> expected = new LinkedHashMap<>();
        expected.put("Issue1", 10L);
        expected.put("Issue2", 7L);

        Map<String, Long> actual = issueStatisticsService.getIssuesOrderByComments(projectId);
        assertEquals(expected, actual);
    }

    @Test
    public void testGetIssuesPerDayInMonth() {
        Long projectId = 1L;
        List<Object[]> mockResults = Arrays.asList(
                new Object[]{"05-01", 8L},
                new Object[]{"05-02", 12L}
        );

        when(issueRepository.findByProjectPerDayInMonth(projectId)).thenReturn(mockResults);

        Map<String, Long> expected = new LinkedHashMap<>();
        expected.put("05-01", 8L);
        expected.put("05-02", 12L);

        Map<String, Long> actual = issueStatisticsService.getIssuesPerDayInMonth(projectId);
        assertEquals(expected, actual);
    }

    @Test
    public void testGetIssuesPerDayAndPriorityInWeek() {
        Long projectId = 1L;
        String priority = "MAJOR";
        List<Object[]> mockResults = Arrays.asList(
                new Object[]{"05-03", 5L},
                new Object[]{"05-04", 7L}
        );

        when(issueRepository.findIssuesPerDayAndPriorityInWeek(projectId, priority)).thenReturn(mockResults);

        Map<String, Long> expected = new LinkedHashMap<>();
        expected.put("05-03", 5L);
        expected.put("05-04", 7L);

        Map<String, Long> actual = issueStatisticsService.getIssuesPerDayAndPriorityInWeek(projectId, priority);
        assertEquals(expected, actual);
    }

    @Test
    public void testGetIssuesPerMonth() {
        Long projectId = 1L;
        List<Object[]> mockResults = Arrays.asList(
                new Object[]{"2024-04", 15L},
                new Object[]{"2024-05", 20L}
        );

        when(issueRepository.findByProjectPerMonth(projectId)).thenReturn(mockResults);

        Map<String, Long> expected = new LinkedHashMap<>();
        expected.put("2024-04", 15L);
        expected.put("2024-05", 20L);

        Map<String, Long> actual = issueStatisticsService.getIssuesPerMonth(projectId);
        assertEquals(expected, actual);
    }

    @Test
    public void testGetIssuesPerPriorityInMonth() {
        Long projectId = 1L;
        List<Object[]> mockResults = Arrays.asList(
                new Object[]{"MAJOR", 10L},
                new Object[]{"MINOR", 5L}
        );

        when(issueRepository.findByProjectPerPriorityInMonth(projectId)).thenReturn(mockResults);

        Map<String, Long> expected = new LinkedHashMap<>();
        expected.put("MAJOR", 10L);
        expected.put("MINOR", 5L);

        Map<String, Long> actual = issueStatisticsService.getIssuesPerPriorityInMonth(projectId);
        assertEquals(expected, actual);
    }
}
