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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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

    private Map<String, Long> issueStatistics1;
    private Map<String, Map<String, Long>> issueStatistics2;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(issueStatisticsController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        issueStatistics1 = new LinkedHashMap<>();
        issueStatistics2 = new LinkedHashMap<>();
    }

    @Test
    public void testGetIssuesPerStatus() throws Exception {
        issueStatistics1.put("NEW", 5L);
        issueStatistics1.put("ASSINGED", 3L);
        
        when(issueStatisticsService.getIssuesPerStatus(1L)).thenReturn(issueStatistics1);

        mockMvc.perform(get("/api/projects/1/statistics/issuesPerStatus"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("{\"NEW\":5,\"ASSINGED\":3}"));
    }

    @Test
    public void getIssueStatusCounts() throws Exception {
        issueStatistics1.put("REMAINING", 10L);
        issueStatistics1.put("RESOLVED", 5L);
        issueStatistics1.put("ASSIGNED", 5L);
        issueStatistics1.put("UNASSIGNED", 5L);
        issueStatistics1.put("Registered Issues", 25L);
        
        when(issueStatisticsService.getIssueStatusCounts(1L)).thenReturn(issueStatistics1);

        mockMvc.perform(get("/api/projects/1/statistics/issueStatusCounts"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("{\"REMAINING\":10,\"RESOLVED\":5,\"ASSIGNED\":5,\"UNASSIGNED\":5,\"Registered Issues\":25}"));
    }

    @Test
    public void testGetIssuesPerFixer() throws Exception {
        issueStatistics1.put("RESOLVED", 3L);
        issueStatistics1.put("CLOSED", 2L);
        issueStatistics2.put("dev1", issueStatistics1);

        when(issueStatisticsService.getIssuesPerFixer(1L)).thenReturn(issueStatistics2);

        mockMvc.perform(get("/api/projects/1/statistics/issuesPerFixer"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("{\"dev1\":{\"RESOLVED\":3,\"CLOSED\":2}}"));
    }

    @Test
    public void testGetIssuesPerDayAndStatusInWeek_PerStatus() throws Exception {
        String status = Issue.Status.RESOLVED.toString();
        issueStatistics1.put("05-10", 1L);
        issueStatistics1.put("05-11", 3L);
        issueStatistics1.put("05-12", 0L);

        when(issueStatisticsService.getIssuesPerDayAndStatusInWeek(1L, status)).thenReturn(issueStatistics1);

        mockMvc.perform(get("/api/projects/1/statistics/issuesPerDayAndStatusInWeek/{status}", status))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("{\"05-10\":1,\"05-11\":3,\"05-12\":0}"));
    }

    @Test
    public void testGetIssuesPerDayAndStatusInWeek_All() throws Exception {
        issueStatistics1.put("NEW", 3L);
        issueStatistics1.put("ASSIGNED", 2L);
        issueStatistics1.put("FIXED", 1L);
        issueStatistics1.put("RESOLVED", 0L);
        issueStatistics1.put("CLOSED", 0L);
        issueStatistics1.put("REOPENED", 0L);
        issueStatistics2.put("05-11", issueStatistics1);

        when(issueStatisticsService.getIssuesPerDayAndStatusInWeek(1L)).thenReturn(issueStatistics2);

        mockMvc.perform(get("/api/projects/1/statistics/issuesPerDayAndStatusInWeek"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("{\"05-11\":{\"NEW\":3,\"ASSIGNED\":2,\"FIXED\":1,\"RESOLVED\":0,\"CLOSED\":0,\"REOPENED\":0}}"));
    }

    @Test
    public void testGetIssuesOrderByComments() throws Exception {
        issueStatistics1.put("Issue1", 5L);
        issueStatistics1.put("Issue2", 10L);

        when(issueStatisticsService.getIssuesOrderByComments(1L)).thenReturn(issueStatistics1);

        mockMvc.perform(get("/api/projects/1/statistics/issuesOrderByComments"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("{\"Issue1\":5,\"Issue2\":10}"));
    }

    @Test
    public void testGetIssuesPerDayInMonth() throws Exception {
        issueStatistics1.put("05-12", 5L);
        issueStatistics1.put("05-13", 4L);

        when(issueStatisticsService.getIssuesPerDayInMonth(1L)).thenReturn(issueStatistics1);

        mockMvc.perform(get("/api/projects/1/statistics/issuesPerDayInMonth"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("{\"05-12\":5,\"05-13\":4}"));
    }

    @Test
    public void testGetIssuesPerDayAndPriorityInWeek() throws Exception {
        String priority = Issue.Priority.MAJOR.toString();
        issueStatistics1.put("05-10", 0L);
        issueStatistics1.put("05-11", 1L);
        issueStatistics1.put("05-12", 2L);

        when(issueStatisticsService.getIssuesPerDayAndPriorityInWeek(1L, priority)).thenReturn(issueStatistics1);

        mockMvc.perform(get("/api/projects/1/statistics/issuesPerDayAndPriorityInWeek/{priority}", priority))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("{\"05-10\":0,\"05-11\":1,\"05-12\":2}"));
    }

    @Test
    public void testGetIssuesPerMonth() throws Exception {
        issueStatistics1.put("2024-04", 12L);
        issueStatistics1.put("2024-05", 16L);

        when(issueStatisticsService.getIssuesPerMonth(1L)).thenReturn(issueStatistics1);

        mockMvc.perform(get("/api/projects/1/statistics/issuesPerMonth"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("{\"2024-04\":12,\"2024-05\":16}"));
    }

    @Test
    public void testGetIssuesPerPriorityInMonth() throws Exception {
        issueStatistics1.put("MAJOR", 4L);
        issueStatistics1.put("MINOR", 3L);

        when(issueStatisticsService.getIssuesPerPriorityInMonth(1L)).thenReturn(issueStatistics1);

        mockMvc.perform(get("/api/projects/1/statistics/issuesPerPriorityInMonth"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("{\"MAJOR\":4,\"MINOR\":3}"));
    }
}
