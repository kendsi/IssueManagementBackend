package com.causwe.backend.service;

import com.causwe.backend.exceptions.IssueNotFoundException;
import com.causwe.backend.exceptions.UnauthorizedException;
import com.causwe.backend.model.Admin;
import com.causwe.backend.model.Developer;
import com.causwe.backend.model.Issue;
import com.causwe.backend.model.Project;
import com.causwe.backend.model.Tester;
import com.causwe.backend.model.User;
import com.causwe.backend.repository.IssueRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) 
public class IssueServiceTest {

    @Mock
    private IssueRepository issueRepository;

    @Mock
    private ProjectService projectService;

    @Mock
    private UserService userService;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private IssueServiceImpl issueService;

    private User admin;
    private User dev;
    private User tester;
    private Project project;
    private Issue issue1;
    private Issue issue2;

    private MockWebServer mockWebServer;
    private CacheManager cacheManager;
    private Cache cache;

    @BeforeEach
    public void setUp() throws IOException {
        // Initialize CacheManager and Cache
        cacheManager = new ConcurrentMapCacheManager("sqlQueries");
        cache = cacheManager.getCache("sqlQueries");
        issueService.setCacheManager(cacheManager);

        // Initialize the EntityManager
        issueService.setEntityManager(entityManager);

        admin = new Admin();
        admin.setUsername("admin");
        admin.setPassword("admin");
        admin.setId(1L);

        dev = new Developer();
        dev.setUsername("dev");
        dev.setPassword("dev");
        dev.setId(2L);

        tester = new Tester();
        tester.setUsername("tester");
        tester.setPassword("tester");
        tester.setId(3L);

        project = new Project();
        project.setId(1L);
        project.setName("Test Project");

        issue1 = new Issue("Test Issue1", "Issue Description", tester);
        issue1.setId(1L);
        issue1.setProject(project);

        issue2 = new Issue("Test Issue2", "Issue Description", tester);
        issue2.setId(2L);
        issue2.setProject(project);

        // Initialize the MockWebServer
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @Test
    public void testGetAllIssues() {
        List<Issue> issues = Arrays.asList(issue1, issue2);

        when(issueRepository.IssuesByProjectAndUser(1L, 3L)).thenReturn(issues);

        List<Issue> foundIssue = issueService.getAllIssues(1L, 3L);

        assertNotNull(foundIssue);
        assertEquals(2, foundIssue.size());
        assertEquals(issues, foundIssue);
    }

    @Test
    public void testGetIssueById_Success() {
        when(issueRepository.findById(1L)).thenReturn(Optional.of(issue1));

        Issue foundIssue = issueService.getIssueById(1L);

        assertNotNull(foundIssue);
        assertEquals(issue1.getTitle(), foundIssue.getTitle());
    }

    @Test
    public void testGetIssueById_NotFound() {
        when(issueRepository.findById(3L)).thenReturn(Optional.empty());

        assertThrows(IssueNotFoundException.class, () -> {
            issueService.getIssueById(3L);
        });
    }

    @Test
    public void testCreateIssue_Success() {
        when(userService.getUserById(3L)).thenReturn(tester);
        when(projectService.getProjectById(1L)).thenReturn(project);
        when(issueRepository.save(any(Issue.class))).thenReturn(issue1);

        Issue result = issueService.createIssue(1L, issue1, 3L);

        assertNotNull(result);
        assertEquals(issue1.getTitle(), result.getTitle());
    }

    @Test
    public void testCreateIssue_Unauthorized_NotLoggedIn() {
        when(userService.getUserById(null)).thenReturn(null);

        assertThrows(UnauthorizedException.class, () -> {
            issueService.createIssue(1L, issue1, null);
        });
    }

    @Test
    public void testCreateIssue_Unauthorized_NotPermitted() {
        when(userService.getUserById(2L)).thenReturn(dev);

        assertThrows(UnauthorizedException.class, () -> {
            issueService.createIssue(1L, issue1, 2L);
        });
    }

    @Test
    public void testUpdateIssue_Success() {
        Issue updatedIssue = new Issue("Updated Issue", "Updated Description", tester);
        updatedIssue.setStatus(Issue.Status.ASSIGNED);
        updatedIssue.setAssignee(dev);

        when(userService.getUserById(1L)).thenReturn(admin);
        when(issueRepository.findById(1L)).thenReturn(Optional.of(issue1));
        when(issueRepository.save(any(Issue.class))).thenReturn(updatedIssue);

        Issue result = issueService.updateIssue(1L, updatedIssue, 1L);

        assertNotNull(result);
        assertEquals(updatedIssue.getTitle(), result.getTitle());
        assertEquals(updatedIssue.getStatus(), result.getStatus());
        assertEquals(dev, result.getAssignee());
    }

    @Test
    public void testUpdateIssue_Unauthorized_NotLoggedIn() {
        Issue updatedIssue = new Issue("Updated Issue", "Updated Description", tester);

        when(userService.getUserById(null)).thenReturn(null);

        assertThrows(UnauthorizedException.class, () -> {
            issueService.updateIssue(1L, updatedIssue, null);
        });
    }

    @Test
    public void testUpdateIssue_Unauthorized_NotPermitted() {
        Issue updatedIssue = new Issue("Updated Issue", "Updated Description", tester);

        when(userService.getUserById(2L)).thenReturn(dev);

        assertThrows(IssueNotFoundException.class, () -> {
            issueService.updateIssue(1L, updatedIssue, 2L);
        });
    }

    @Test
    public void testUpdateIssue_NoChanges() {
        Issue updatedIssue = new Issue("Test Issue1", "Issue Description", tester);

        when(userService.getUserById(1L)).thenReturn(admin);
        when(issueRepository.findById(1L)).thenReturn(Optional.of(issue1));

        assertThrows(UnauthorizedException.class, () -> {
            issueService.updateIssue(1L, updatedIssue, 1L);
        });
    }

    @Test
    public void testUpdateIssue_NotFound() {
        Issue updatedIssue = new Issue("Test Issue1", "Issue Description", tester);

        when(userService.getUserById(1L)).thenReturn(admin);
        when(issueRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IssueNotFoundException.class, () -> {
            issueService.updateIssue(1L, updatedIssue, 1L);
        });
    }

    @Test
    public void testSearchIssues_ByAssignee() {
        issue1.setAssignee(dev);
        issue2.setAssignee(dev);
        List<Issue> issues = Arrays.asList(issue1, issue2);

        when(projectService.getProjectById(1L)).thenReturn(project);
        when(userService.getUserByUsername("dev")).thenReturn(dev);
        when(issueRepository.findByProjectAndAssigneeOrderByIdDesc(project, dev)).thenReturn(issues);

        List<Issue> result = issueService.searchIssues(1L, "dev", null, null, 1L);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(issue1.getTitle(), result.get(0).getTitle());
    }

    @Test
    public void testSearchIssues_ByReporter() {
        issue1.setReporter(tester);
        issue2.setReporter(tester);
        List<Issue> issues = Arrays.asList(issue1, issue2);

        when(projectService.getProjectById(1L)).thenReturn(project);
        when(userService.getUserByUsername("tester")).thenReturn(tester);
        when(issueRepository.findByProjectAndReporterOrderByIdDesc(project, tester)).thenReturn(issues);

        List<Issue> result = issueService.searchIssues(1L, null, "tester", null, 1L);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(issue1.getTitle(), result.get(0).getTitle());
    }

    @Test
    public void testSearchIssues_ByStatus() {
        issue1.setStatus(Issue.Status.ASSIGNED);
        issue2.setStatus(Issue.Status.ASSIGNED);
        List<Issue> issues = Arrays.asList(issue1, issue2);

        when(projectService.getProjectById(1L)).thenReturn(project);
        when(issueRepository.findByProjectAndStatusOrderByIdDesc(project, Issue.Status.ASSIGNED)).thenReturn(issues);

        List<Issue> result = issueService.searchIssues(1L, null, null, Issue.Status.ASSIGNED, 1L);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(issue1.getTitle(), result.get(0).getTitle());
    }

    @Test
    public void testSearchIssues_Default() {
        List<Issue> issues = Arrays.asList(issue1, issue2);

        when(projectService.getProjectById(1L)).thenReturn(project);
        when(issueRepository.IssuesByProjectAndUser(1L, 1L)).thenReturn(issues);

        List<Issue> result = issueService.searchIssues(1L, null, null, null, 1L);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(issue1.getTitle(), result.get(0).getTitle());
    }

    @Test
    public void testSearchIssuesByNL_CachedHit() throws IOException {
        // Setup cached SQL query
        String cacheKey = "sqlQuery::1::find all issues::3";
        String cachedSqlQuery = "SELECT * FROM issues WHERE project_id = 1 ORDER BY reported_date DESC";
        cache.put(cacheKey, cachedSqlQuery);

        // Setup EntityManager mock
        Query query = mock(Query.class);
        when(entityManager.createNativeQuery(cachedSqlQuery, Issue.class)).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of(issue1, issue2));

        // Call the method
        List<Issue> issues = issueService.searchIssuesByNL(1L, "find all issues", 3L);

        // Assertions
        assertEquals(2, issues.size());
        verify(entityManager, times(1)).createNativeQuery(cachedSqlQuery, Issue.class);
        verify(query, times(1)).getResultList();
    }

    @Test
    public void testSearchIssuesByNL_CacheMiss() throws IOException {
        // Setup no cached SQL query
        String cacheKey = "sqlQuery::1::find all issues::3";
        assertNull(cache.get(cacheKey, String.class));

        // Setup mock web server response
        String jsonResponse = "{\n" +
                "  \"choices\": [\n" +
                "    {\n" +
                "      \"message\": {\n" +
                "        \"content\": \"```sql\\nSELECT * FROM issues WHERE project_id = 1 ORDER BY reported_date DESC\\n```\"\n" +
                "      }\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        mockWebServer.enqueue(new MockResponse()
                .setBody(jsonResponse)
                .addHeader("Content-Type", "application/json"));

        // Setup EntityManager mock
        String expectedSqlQuery = "SELECT * FROM issues WHERE project_id = 1 ORDER BY reported_date DESC";
        Query query = mock(Query.class);
        when(entityManager.createNativeQuery(expectedSqlQuery, Issue.class)).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of(issue1, issue2));

        // Call the method
        List<Issue> issues = issueService.searchIssuesByNL(1L, "find all issues", 3L);

        // Assertions
        assertEquals(2, issues.size());
        assertNotNull(cache.get(cacheKey, String.class));
        verify(entityManager, times(1)).createNativeQuery(expectedSqlQuery, Issue.class);
        verify(query, times(1)).getResultList();
    }

    @Test
    public void testGetRecommendedAssignees() {
        User dev2 = new Developer();
        dev2.setUsername("dev2");
        dev2.setPassword("dev2");
        dev2.setId(4L);

        User dev3 = new Developer();
        dev3.setUsername("dev3");
        dev3.setPassword("dev3");
        dev3.setId(5L);

        List<Long> assigneeIds = Arrays.asList(dev.getId(), dev2.getId(), dev3.getId());

        issue1.setAssignee(dev);
        issue2.setAssignee(dev2);
        
        Issue newIssue = new Issue("New Issue", "Issue Description", tester);
        newIssue.setId(3L);

        when(issueRepository.existsById(3L)).thenReturn(true);
        when(issueRepository.findRecommendedAssigneesByIssueId(3L)).thenReturn(assigneeIds);
        when(userService.getUserById(2L)).thenReturn(dev);
        when(userService.getUserById(4L)).thenReturn(dev2);
        when(userService.getUserById(5L)).thenReturn(dev3);

        List<User> result = issueService.getRecommendedAssignees(3L);

        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(dev.getId(), result.get(0).getId());
    }
}