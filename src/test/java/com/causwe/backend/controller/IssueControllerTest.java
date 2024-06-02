package com.causwe.backend.controller;

import com.causwe.backend.dto.IssueDTO;
import com.causwe.backend.dto.UserResponseDTO;
import com.causwe.backend.exceptions.GlobalExceptionHandler;
import com.causwe.backend.exceptions.IssueNotFoundException;
import com.causwe.backend.exceptions.UnauthorizedException;
import com.causwe.backend.model.Admin;
import com.causwe.backend.model.Developer;
import com.causwe.backend.model.Issue;
import com.causwe.backend.model.Project;
import com.causwe.backend.model.Tester;
import com.causwe.backend.model.User;
import com.causwe.backend.security.JwtTokenProvider;
import com.causwe.backend.service.IssueService;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
public class IssueControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Mock
    private IssueService issueService;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private IssueController issueController;

    private User admin;
    private User dev;
    private User tester;
    private Project project;
    private Issue issue1;
    private Issue issue2;
    private IssueDTO issueDTO1;
    private IssueDTO issueDTO2;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(issueController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();

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

        issue1 = new Issue("Test Issue1", "Issue Description", Issue.Priority.MAJOR, tester);
        issue1.setId(1L);
        issue1.setProject(project);

        issue2 = new Issue("Test Issue2", "Issue Description", Issue.Priority.MAJOR, tester);
        issue2.setId(2L);
        issue2.setProject(project);

        issueDTO1 = new IssueDTO();
        issueDTO1.setTitle("Test Issue1");
        issueDTO1.setDescription("Issue Description");
        issueDTO1.setReporterUsername("tester");

        issueDTO2 = new IssueDTO();
        issueDTO2.setTitle("Test Issue2");
        issueDTO2.setDescription("Issue Description");
        issueDTO2.setReporterUsername("tester");
    }

    @Test
    public void testGetAllIssues() throws Exception {
        List<Issue> issues = Arrays.asList(issue1, issue2);

        when(jwtTokenProvider.getUserIdFromToken("token")).thenReturn(3L);
        when(issueService.getAllIssues(1L, 3L)).thenReturn(issues);
        when(modelMapper.map(issue1, IssueDTO.class)).thenReturn(issueDTO1);
        when(modelMapper.map(issue2, IssueDTO.class)).thenReturn(issueDTO2);

        mockMvc.perform(get("/api/projects/1/issues")
                .cookie(new Cookie("jwt", "token")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value(issueDTO1.getTitle()));
    }

    @Test
    public void testGetIssueById_Success() throws Exception {
        when(issueService.getIssueById(1L)).thenReturn(issue1);
        when(modelMapper.map(issue1, IssueDTO.class)).thenReturn(issueDTO1);

        mockMvc.perform(get("/api/projects/1/issues/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(issueDTO1.getTitle()));
    }

    @Test
    public void testGetIssueById_NotFound() throws Exception {
        when(issueService.getIssueById(3L)).thenThrow(new IssueNotFoundException(3L));

        mockMvc.perform(get("/api/projects/1/issues/3"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testCreateIssue_Success() throws Exception {
        when(jwtTokenProvider.getUserIdFromToken("token")).thenReturn(3L);
        when(modelMapper.map(any(IssueDTO.class), eq(Issue.class))).thenReturn(issue2);
        when(issueService.createIssue(1L, issue2, 3L)).thenReturn(issue2);
        when(modelMapper.map(any(Issue.class), eq(IssueDTO.class))).thenReturn(issueDTO2);

        mockMvc.perform(post("/api/projects/1/issues")
                .cookie(new Cookie("jwt", "token"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(issueDTO2)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value(issue2.getTitle()));
    }

    @Test
    public void testCreateIssue_BadRequest() throws Exception {
        issueDTO2.setTitle("");

        mockMvc.perform(post("/api/projects/1/issues")
                .cookie(new Cookie("jwt", "token"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(issueDTO2)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testCreateIssue_Unauthorized_NotPermitted() throws Exception {
        when(jwtTokenProvider.getUserIdFromToken("token")).thenReturn(2L);
        when(modelMapper.map(any(IssueDTO.class), eq(Issue.class))).thenReturn(issue2);
        when(issueService.createIssue(1L, issue2, 2L)).thenThrow(new UnauthorizedException("User not authorized to create issue"));

        mockMvc.perform(post("/api/projects/1/issues")
                .cookie(new Cookie("jwt", "token"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(issueDTO2)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testCreateIssue_Unauthorized_NotLoggedIn() throws Exception {
        when(jwtTokenProvider.getUserIdFromToken("")).thenReturn(null);
        when(modelMapper.map(any(IssueDTO.class), eq(Issue.class))).thenReturn(issue2);
        when(issueService.createIssue(1L, issue2, null)).thenThrow(new UnauthorizedException("User not logged in"));

        mockMvc.perform(post("/api/projects/1/issues")
                .cookie(new Cookie("jwt", ""))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(issueDTO2)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testUpdateIssue_Success() throws Exception {
        
        issueDTO2.setAssigneeUsername("dev");
        issueDTO2.setDescription("Updated Description");
        issueDTO2.setStatus(IssueDTO.Status.ASSIGNED);
        issueDTO2.setPriority(IssueDTO.Priority.MINOR);

        issue2.setAssignee(dev);
        issue2.setDescription("Updated Description");
        issue2.setStatus(Issue.Status.ASSIGNED);
        issue2.setPriority(Issue.Priority.MINOR);

        issue1.setAssignee(dev);
        issue1.setDescription("Updated Description");
        issue1.setStatus(Issue.Status.ASSIGNED);
        issue1.setPriority(Issue.Priority.MINOR);

        issueDTO1.setAssigneeUsername("dev");
        issueDTO1.setDescription("Updated Description");
        issueDTO1.setStatus(IssueDTO.Status.ASSIGNED);
        issueDTO1.setPriority(IssueDTO.Priority.MINOR);

        when(jwtTokenProvider.getUserIdFromToken("token")).thenReturn(1L);
        when(modelMapper.map(any(IssueDTO.class), eq(Issue.class))).thenReturn(issue2);
        when(issueService.updateIssue(1L, issue2, 1L)).thenReturn(issue1);
        when(modelMapper.map(any(Issue.class), eq(IssueDTO.class))).thenReturn(issueDTO1);

        mockMvc.perform(put("/api/projects/1/issues/1")
                .cookie(new Cookie("jwt", "token"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(issueDTO2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(issue1.getTitle()))
                .andExpect(jsonPath("$.description").value(issueDTO2.getDescription()));
    }

    @Test
    public void testUpdateIssue_BadRequest() throws Exception {
        issueDTO2.setTitle("");

        mockMvc.perform(put("/api/projects/1/issues/3")
                .cookie(new Cookie("jwt", "token"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(issueDTO2)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testUpdateIssue_NotFound() throws Exception {
        
        issueDTO2.setAssigneeUsername("dev");
        issueDTO2.setDescription("Updated Description");
        issueDTO2.setStatus(IssueDTO.Status.ASSIGNED);
        issueDTO2.setPriority(IssueDTO.Priority.MINOR);

        issue2.setAssignee(dev);
        issue2.setDescription("Updated Description");
        issue2.setStatus(Issue.Status.ASSIGNED);
        issue2.setPriority(Issue.Priority.MINOR);

        when(jwtTokenProvider.getUserIdFromToken("token")).thenReturn(1L);
        when(modelMapper.map(any(IssueDTO.class), eq(Issue.class))).thenReturn(issue2);
        when(issueService.updateIssue(3L, issue2, 1L)).thenThrow(new IssueNotFoundException(3L));

        mockMvc.perform(put("/api/projects/1/issues/3")
                .cookie(new Cookie("jwt", "token"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(issueDTO2)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testUpdateIssue_Unauthorized_NotChanged() throws Exception {

        when(jwtTokenProvider.getUserIdFromToken("token")).thenReturn(1L);
        when(modelMapper.map(any(IssueDTO.class), eq(Issue.class))).thenReturn(issue2);
        when(issueService.updateIssue(1L, issue2, 1L)).thenThrow(new UnauthorizedException("Issue not changed"));

        mockMvc.perform(put("/api/projects/1/issues/1")
                .cookie(new Cookie("jwt", "token"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(issueDTO2)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testUpdateIssue_Unauthorized_NotLoggedIn() throws Exception {

        issueDTO2.setAssigneeUsername("dev");
        issueDTO2.setDescription("Updated Description");
        issueDTO2.setStatus(IssueDTO.Status.ASSIGNED);
        issueDTO2.setPriority(IssueDTO.Priority.MINOR);

        issue2.setAssignee(dev);
        issue2.setDescription("Updated Description");
        issue2.setStatus(Issue.Status.ASSIGNED);
        issue2.setPriority(Issue.Priority.MINOR);

        when(jwtTokenProvider.getUserIdFromToken("")).thenReturn(null);
        when(modelMapper.map(any(IssueDTO.class), eq(Issue.class))).thenReturn(issue2);
        when(issueService.updateIssue(1L, issue2, null)).thenThrow(new UnauthorizedException("User not logged in"));

        mockMvc.perform(put("/api/projects/1/issues/1")
                .cookie(new Cookie("jwt", ""))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(issueDTO2)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testSearchIssues() throws Exception {
        issue1.setReporter(tester);
        issue2.setReporter(tester);
        issue1.setAssignee(dev);
        issue2.setAssignee(dev);
        issue1.setStatus(Issue.Status.ASSIGNED);
        issue2.setStatus(Issue.Status.ASSIGNED);

        List<Issue> issues = Arrays.asList(issue1, issue2);

        when(jwtTokenProvider.getUserIdFromToken("token")).thenReturn(3L);
        when(issueService.searchIssues(1L, dev.getUsername(), tester.getUsername(), Issue.Status.ASSIGNED, 3L)).thenReturn(issues);
        when(modelMapper.map(any(Issue.class), eq(IssueDTO.class))).thenReturn(issueDTO1, issueDTO2);

        mockMvc.perform(get("/api/projects/1/issues/search")
                .cookie(new Cookie("jwt", "token"))
                .param("assigneeUsername", "dev")
                .param("reporterUsername", "tester")
                .param("status", "ASSIGNED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value(issueDTO1.getTitle()));
    }

    @Test
    public void testSearchIssuesbyNL_Success() throws Exception {
        List<Issue> issues = Arrays.asList(issue1);

        when(jwtTokenProvider.getUserIdFromToken("token")).thenReturn(1L);
        when(issueService.searchIssuesByNL(1L, "test message", 1L)).thenReturn(issues);
        when(modelMapper.map(any(Issue.class), eq(IssueDTO.class))).thenReturn(issueDTO1);

        mockMvc.perform(get("/api/projects/1/issues/searchbynl")
                    .param("userMessage", "test message")
                    .cookie(new Cookie("jwt", "token")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].title").value(issueDTO1.getTitle()));
    }

    @Test
    public void testSearchIssuesbyNL_Failure() throws Exception {

        when(jwtTokenProvider.getUserIdFromToken("token")).thenReturn(1L);
        when(issueService.searchIssuesByNL(1L, "test message", 1L)).thenThrow(new IOException("Unexpected code" + 401));
        mockMvc.perform(get("/api/projects/1/issues/searchbynl")
                    .param("userMessage", "test message")
                    .cookie(new Cookie("jwt", "token")))
                    .andExpect(status().isInternalServerError());
    }

    @Test
    public void testGetRecommendedAssignees() throws Exception {

        UserResponseDTO devDTO = new UserResponseDTO();
        devDTO.setUsername("dev");
        devDTO.setRole(UserResponseDTO.Role.DEV);

        List<User> recommendedAssignees = Arrays.asList(dev);
        List<UserResponseDTO> userResponseDTOs = Arrays.asList(devDTO);

        when(issueService.getRecommendedAssignees(3L)).thenReturn(recommendedAssignees);
        when(modelMapper.map(any(User.class), eq(UserResponseDTO.class))).thenReturn(userResponseDTOs.get(0));

        mockMvc.perform(get("/api/projects/1/issues/3/recommendedAssignees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value(userResponseDTOs.get(0).getUsername()));
    }
}