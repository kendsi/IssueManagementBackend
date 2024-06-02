package com.causwe.backend.controller;

import com.causwe.backend.exceptions.GlobalExceptionHandler;
import com.causwe.backend.model.Issue;
import com.causwe.backend.service.IssueStatisticsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
public class IssueStatisticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private IssueStatisticsService issueStatisticsService;

    @InjectMocks
    private IssueStatisticsController issueStatisticsController;

    private Map<String, Long> issueStatistics;
    private Long projectId = 1L;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(issueStatisticsController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        issueStatistics = new LinkedHashMap<>();
    }

    @Test
    public void testGetIssuesPerStatus() throws Exception {
        issueStatistics.put("NEW", 5L);
        issueStatistics.put("ASSINGED", 3L);
        
        when(issueStatisticsService.getIssuesPerStatus(projectId)).thenReturn(issueStatistics);

        mockMvc.perform(get("/api/projects/{projectId}/statistics/issuesPerStatus", projectId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("{\"NEW\":5,\"ASSINGED\":3}"));
    }

    @Test
    public void testGetIssuesPerFixer() throws Exception {
        issueStatistics.put("dev1", 2L);
        issueStatistics.put("dev2", 3L);

        when(issueStatisticsService.getIssuesPerFixer(projectId)).thenReturn(issueStatistics);

        mockMvc.perform(get("/api/projects/{projectId}/statistics/issuesPerFixer", projectId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("{\"dev1\":2,\"dev2\":3}"));
    }

    @Test
    public void testGetIssuesPerDayAndStatusInWeek() throws Exception {
        String status = Issue.Status.RESOLVED.toString();
        issueStatistics.put("05-10", 1L);
        issueStatistics.put("05-11", 3L);
        issueStatistics.put("05-12", 0L);

        when(issueStatisticsService.getIssuesPerDayAndStatusInWeek(projectId, status)).thenReturn(issueStatistics);

        mockMvc.perform(get("/api/projects/{projectId}/statistics/issuesPerDayAndStatusInWeek", projectId)
                .param("status", status))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("{\"05-10\":1,\"05-11\":3,\"05-12\":0}"));
    }

    @Test
    public void testGetIssuesOrderByComments() throws Exception {
        issueStatistics.put("Issue1", 5L);
        issueStatistics.put("Issue2", 10L);

        when(issueStatisticsService.getIssuesOrderByComments(projectId)).thenReturn(issueStatistics);

        mockMvc.perform(get("/api/projects/{projectId}/statistics/issuesOrderByComments", projectId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("{\"Issue1\":5,\"Issue2\":10}"));
    }

    @Test
    public void testGetIssuesPerDayInMonth() throws Exception {
        issueStatistics.put("05-12", 5L);
        issueStatistics.put("05-13", 4L);

        when(issueStatisticsService.getIssuesPerDayInMonth(projectId)).thenReturn(issueStatistics);

        mockMvc.perform(get("/api/projects/{projectId}/statistics/issuesPerDayInMonth", projectId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("{\"05-12\":5,\"05-13\":4}"));
    }

    @Test
    public void testGetIssuesPerDayAndPriorityInWeek() throws Exception {
        issueStatistics.put("05-10", 0L);
        issueStatistics.put("05-11", 1L);
        issueStatistics.put("05-12", 2L);

        String priority = Issue.Priority.MAJOR.toString();
        when(issueStatisticsService.getIssuesPerDayAndPriorityInWeek(projectId, priority)).thenReturn(issueStatistics);

        mockMvc.perform(get("/api/projects/{projectId}/statistics/issuesPerDayAndPriorityInWeek", projectId)
                .param("priority", priority))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("{\"05-10\":0,\"05-11\":1,\"05-12\":2}"));
    }

    @Test
    public void testGetIssuesPerMonth() throws Exception {
        issueStatistics.put("2024-04", 12L);
        issueStatistics.put("2024-05", 16L);

        when(issueStatisticsService.getIssuesPerMonth(projectId)).thenReturn(issueStatistics);

        mockMvc.perform(get("/api/projects/{projectId}/statistics/issuesPerMonth", projectId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("{\"2024-04\":12,\"2024-05\":16}"));
    }

    @Test
    public void testGetIssuesPerPriorityInMonth() throws Exception {
        issueStatistics.put("MAJOR", 4L);
        issueStatistics.put("MINOR", 3L);

        when(issueStatisticsService.getIssuesPerPriorityInMonth(projectId)).thenReturn(issueStatistics);

        mockMvc.perform(get("/api/projects/{projectId}/statistics/issuesPerPriorityInMonth", projectId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("{\"MAJOR\":4,\"MINOR\":3}"));
    }
}
