package com.causwe.backend.service;

import com.causwe.backend.exceptions.UnauthorizedException;
import com.causwe.backend.model.Issue;
import com.causwe.backend.model.Project;
import com.causwe.backend.model.User;
import com.causwe.backend.repository.IssueRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        admin = new User("admin", "admin", User.Role.ADMIN);
        admin.setId(1L);

        dev = new User("dev", "dev", User.Role.DEV);
        dev.setId(2L);

        tester = new User("tester", "tester", User.Role.TESTER);
        dev.setId(3L);

        project = new Project();
        project.setId(1L);
        project.setName("Test Project");

        issue1 = new Issue("Test Issue1", "Issue Description", tester);
        issue1.setId(1L);
        issue1.setProject(project);

        issue2 = new Issue("Test Issue2", "Issue Description", tester);
        issue2.setId(2L);
        issue2.setProject(project);
    }

    @Test
    public void testGetAllIssues() {
        List<Issue> issues = new ArrayList<>();
        issues.add(issue1);
        issues.add(issue2);
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

        Issue foundIssue = issueService.getIssueById(3L);

        assertNull(foundIssue);
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
    public void testCreateIssue_Unauthorized() {
        when(userService.getUserById(4L)).thenReturn(null);

        assertThrows(UnauthorizedException.class, () -> {
            issueService.createIssue(1L, issue1, 4L);
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
    public void testUpdateIssue_Unauthorized() {
        when(userService.getUserById(4L)).thenReturn(null);

        Issue updatedIssue = new Issue("Updated Issue", "Updated Description", tester);

        assertThrows(UnauthorizedException.class, () -> {
            issueService.updateIssue(1L, updatedIssue, 1L);
        });
    }

    @Test
    public void testSearchIssues() {
        issue1.setAssignee(dev);
        issue2.setAssignee(dev);

        List<Issue> issues = new ArrayList<>();
        issues.add(issue1);
        issues.add(issue2);

        when(projectService.getProjectById(1L)).thenReturn(project);
        when(issueRepository.findByProjectAndAssigneeOrderByIdDesc(project, dev)).thenReturn(issues);

        List<Issue> result = issueService.searchIssues(1L, issue1, 1L);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(issue1.getTitle(), result.get(0).getTitle());
        assertEquals(dev, result.get(0).getAssignee());
    }

    // @Test
    // public void testSearchIssuesByNL() throws IOException {
    //     String jsonResponse = "{ \"choices\": [ { \"message\": { \"content\": \"```sql\nSELECT * FROM issues;\n```\" } } ] }";

    //     OkHttpClient mockHttpClient = mock(OkHttpClient.class);
    //     issueService = new IssueServiceImpl(issueRepository, projectService, userService);
    //     issueService.setHttpClient(mockHttpClient);

    //     Response mockResponse = mock(Response.class);
    //     ResponseBody mockResponseBody = mock(ResponseBody.class);

    //     when(mockResponse.isSuccessful()).thenReturn(true);
    //     when(mockResponse.body()).thenReturn(mockResponseBody);
    //     when(mockResponseBody.string()).thenReturn(jsonResponse);

    //     when(mockHttpClient.newCall(any(Request.class)).execute()).thenReturn(mockResponse);

    //     Query mockQuery = mock(Query.class);
    //     when(entityManager.createNativeQuery(anyString(), eq(Issue.class))).thenReturn(mockQuery);
    //     when(mockQuery.getResultList()).thenReturn(Arrays.asList(issue));

    //     List<Issue> result = issueService.searchIssuesByNL(1L, "test message", 1L);

    //     assertNotNull(result);
    //     assertEquals(1, result.size());
    //     assertEquals(issue.getTitle(), result.get(0).getTitle());
    // }

    @Test
    public void testGetRecommendedAssignees() {
        User dev2 = new User("dev2", "dev2", User.Role.DEV);
        dev2.setId(4L);
        User dev3 = new User("dev3", "dev3", User.Role.DEV);
        dev3.setId(5L);

        List<User> recommendedAssignees = new ArrayList<>();
        recommendedAssignees.add(dev);
        recommendedAssignees.add(dev2);
        recommendedAssignees.add(dev3);

        List<Long> assigneeIds = new ArrayList<>();
        assigneeIds.add(dev.getId());
        assigneeIds.add(dev2.getId());
        assigneeIds.add(dev3.getId());

        issue1.setAssignee(dev);
        issue2.setAssignee(dev2);
        Issue newIssue = new Issue("New Issue", "Issue Description", tester);
        newIssue.setId(3L);

        when(issueRepository.findById(3L)).thenReturn(Optional.of(newIssue));
        when(issueRepository.findRecommendedAssigneesByProjectId(1L, 3L)).thenReturn(assigneeIds);
        when(userService.getUserById(3L)).thenReturn(dev);
        when(userService.getUserById(4L)).thenReturn(dev2);
        when(userService.getUserById(5L)).thenReturn(dev3);

        List<User> result = issueService.getRecommendedAssignees(1L, 3L);

        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(dev.getId(), result.get(0).getId());
    }
}
