package com.causwe.backend.controller;

import com.causwe.backend.dto.ProjectDTO;
import com.causwe.backend.exceptions.GlobalExceptionHandler;
import com.causwe.backend.exceptions.ProjectNotFoundException;
import com.causwe.backend.exceptions.UnauthorizedException;
import com.causwe.backend.model.Project;
import com.causwe.backend.security.JwtTokenProvider;
import com.causwe.backend.service.ProjectService;

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

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
public class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Mock
    private ProjectService projectService;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private ProjectController projectController;

    private Project project;
    private ProjectDTO projectDTO;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(projectController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();

        project = new Project();
        project.setId(1L);
        project.setName("Test Project");

        projectDTO = new ProjectDTO();
        projectDTO.setId(1L);
        projectDTO.setName("Test Project");
    }

    @Test
    public void testCreateProject_Success() throws Exception {
        when(jwtTokenProvider.getUserIdFromToken("token")).thenReturn(1L);
        when(modelMapper.map(any(ProjectDTO.class), eq(Project.class))).thenReturn(project);
        when(projectService.createProject(project, 1L)).thenReturn(project);
        when(modelMapper.map(any(Project.class), eq(ProjectDTO.class))).thenReturn(projectDTO);
        
        mockMvc.perform(post("/api/projects")
                .cookie(new Cookie("jwt", "token"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(projectDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Project"));
    }

    @Test
    public void testCreateProject_BadRequest() throws Exception {
        projectDTO.setName("");

        mockMvc.perform(post("/api/projects")
                .cookie(new Cookie("jwt", "token"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(projectDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testCreateProject_Unauthorized_NotPermitted() throws Exception {
        when(jwtTokenProvider.getUserIdFromToken("token")).thenReturn(1L);
        when(modelMapper.map(any(ProjectDTO.class), eq(Project.class))).thenReturn(project);
        when(projectService.createProject(project, 1L)).thenThrow(new UnauthorizedException("User not authorized to create a project."));

        mockMvc.perform(post("/api/projects")
                .cookie(new Cookie("jwt", "token"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(projectDTO)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testCreateProject_Unauthorized_NotLoggedIn() throws Exception {
        when(jwtTokenProvider.getUserIdFromToken("")).thenReturn(null);
        when(modelMapper.map(any(ProjectDTO.class), eq(Project.class))).thenReturn(project);
        when(projectService.createProject(project, null)).thenThrow(new UnauthorizedException("User not logged in"));

        mockMvc.perform(post("/api/projects")
                .cookie(new Cookie("jwt", ""))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(projectDTO)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testGetAllProjects() throws Exception {
        Project project2 = new Project();
        project2.setName("Test Project2");
        project2.setId(2L);

        ProjectDTO projectDTO2 = new ProjectDTO();
        projectDTO2.setName("Test Project2");
        projectDTO2.setId(2L);

        List<Project> projects = new ArrayList<>();
        projects.add(project);
        projects.add(project2);

        List<ProjectDTO> projectDTOs = new ArrayList<>();
        projectDTOs.add(projectDTO);
        projectDTOs.add(projectDTO2);

        when(projectService.getAllProjects()).thenReturn(projects);
        when(modelMapper.map(any(Project.class), eq(ProjectDTO.class))).thenReturn(projectDTOs.get(0), projectDTOs.get(1));

        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Test Project"));
    }

    @Test
    public void testGetProjectById_Success() throws Exception {
        when(projectService.getProjectById(1L)).thenReturn(project);
        when(modelMapper.map(any(Project.class), eq(ProjectDTO.class))).thenReturn(projectDTO);

        mockMvc.perform(get("/api/projects/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Project"));
    }

    @Test
    public void testGetProjectById_NotFound() throws Exception {
        when(projectService.getProjectById(1L)).thenThrow(new ProjectNotFoundException(1L));

        mockMvc.perform(get("/api/projects/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testDeleteProject_Success() throws Exception {
        when(jwtTokenProvider.getUserIdFromToken("token")).thenReturn(1L);
        when(projectService.deleteProject(1L, 1L)).thenReturn(true);

        mockMvc.perform(delete("/api/projects/1")
                .cookie(new Cookie("jwt", "token")))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testDeleteProject_NotFound() throws Exception {
        when(jwtTokenProvider.getUserIdFromToken("token")).thenReturn(1L);
        when(projectService.deleteProject(1L, 1L)).thenThrow(new ProjectNotFoundException(1L));

        mockMvc.perform(delete("/api/projects/1")
                .cookie(new Cookie("jwt", "token")))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testDeleteProject_Unauthorized_NotPermitted() throws Exception {
        when(jwtTokenProvider.getUserIdFromToken("token")).thenReturn(3L);
        when(projectService.deleteProject(1L, 3L)).thenThrow(new UnauthorizedException("You are not authorized to delete this project."));

        mockMvc.perform(delete("/api/projects/1")
                .cookie(new Cookie("jwt", "token")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testDeleteProject_Unauthorized_NotLoggedIn() throws Exception {
        when(jwtTokenProvider.getUserIdFromToken("")).thenReturn(null);
        when(projectService.deleteProject(1L, null)).thenThrow(new UnauthorizedException("User not logged in"));

        mockMvc.perform(delete("/api/projects/1")
                .cookie(new Cookie("jwt", "")))
                .andExpect(status().isUnauthorized());
    }
}
