package com.causwe.backend.service;

import com.causwe.backend.exceptions.UnauthorizedException;
import com.causwe.backend.exceptions.ProjectNotFoundException;
import com.causwe.backend.model.Project;
import com.causwe.backend.model.User;
import com.causwe.backend.repository.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private ProjectServiceImpl projectService;

    private User admin;
    private User pl;
    private User dev;
    private Project project;

    @BeforeEach
    public void setUp() {
        admin = new User("admin", "admin", User.Role.ADMIN);
        admin.setId(1L);
        
        pl = new User("pl", "pl", User.Role.PL);
        pl.setId(2L);

        dev = new User("dev", "dev", User.Role.DEV);
        dev.setId(3L);

        project = new Project();
        project.setId(1L);
        project.setName("Test Project");
    }

    @Test
    public void testCreateProject_Success() {
        when(userService.getUserById(2L)).thenReturn(pl);
        when(projectRepository.save(project)).thenReturn(project);

        Project createdProject = projectService.createProject(project, 2L);
        assertNotNull(createdProject);
        assertEquals("Test Project", createdProject.getName());
    }

    @Test
    public void testCreateProject_Unauthorized() {
        when(userService.getUserById(4L)).thenReturn(null);

        assertThrows(UnauthorizedException.class, () -> {
            projectService.createProject(project, 4L);
        });
    }

    @Test
    public void testGetAllProjects() {
        Project project2 = new Project("Test Project2");
        project2.setId(2L);

        List<Project> projects = new ArrayList<>();
        projects.add(project);
        projects.add(project2);
        when(projectRepository.findAllByOrderByIdAsc()).thenReturn(projects);

        List<Project> result = projectService.getAllProjects();
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    public void testGetProjectById_Success() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        Project foundProject = projectService.getProjectById(1L);
        assertNotNull(foundProject);
        assertEquals("Test Project", foundProject.getName());
    }

    @Test
    public void testGetProjectById_NotFound() {
        when(projectRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ProjectNotFoundException.class, () -> {
            projectService.getProjectById(1L);
        });
    }

    @Test
    public void testDeleteProject_Success() {
        when(userService.getUserById(1L)).thenReturn(admin);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        boolean result = projectService.deleteProject(1L, 1L);
        assertTrue(result);
        verify(projectRepository, times(1)).deleteById(1L);
    }

    @Test
    public void testDeleteProject_Unauthorized() {
        when(userService.getUserById(2L)).thenReturn(dev);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        boolean result = projectService.deleteProject(1L, 2L);
        assertFalse(result);
        verify(projectRepository, never()).deleteById(1L);
    }

    @Test
    public void testDeleteProject_NotFound() {
        when(userService.getUserById(1L)).thenReturn(admin);
        when(projectRepository.findById(1L)).thenReturn(Optional.empty());

        boolean result = projectService.deleteProject(1L, 1L);
        assertFalse(result);
        verify(projectRepository, never()).deleteById(1L);
    }

    @Test
    public void testDeleteProject_UserNotLoggedIn() {
        when(userService.getUserById(4L)).thenReturn(null);

        assertThrows(UnauthorizedException.class, () -> {
            projectService.deleteProject(1L, 4L);
        });
        verify(projectRepository, never()).deleteById(1L);
    }
}
