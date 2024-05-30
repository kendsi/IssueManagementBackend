package com.causwe.backend.controller;

import com.causwe.backend.dto.ProjectDTO;
import com.causwe.backend.exceptions.ProjectNotFoundException;
import com.causwe.backend.exceptions.UnauthorizedException;
import com.causwe.backend.model.Project;
import com.causwe.backend.security.JwtTokenProvider;
import com.causwe.backend.service.ProjectService;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @PostMapping("")
    @CacheEvict(value = {"projects", "issues", "issuesBySearch", "issuesByNLSearch"}, allEntries = true)
    public ResponseEntity<ProjectDTO> createProject(@RequestBody ProjectDTO projectData, @CookieValue(name = "jwt", required = false) String token) {
        if (Objects.equals(projectData.getName(), "")) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        try {
            Long memberId = jwtTokenProvider.getUserIdFromToken(token);
            Project newProject = projectService.createProject(modelMapper.map(projectData, Project.class), memberId);
            ProjectDTO newProjectDTO = modelMapper.map(newProject, ProjectDTO.class);
            return new ResponseEntity<>(newProjectDTO, HttpStatus.CREATED);
        } catch (UnauthorizedException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    @GetMapping("")
    @Cacheable("projects")
    public ResponseEntity<List<ProjectDTO>> getAllProjects() {
        List<Project> projects = projectService.getAllProjects();
        
        List<ProjectDTO> projectDTOs = projects
        .stream()
        .map(project -> modelMapper.map(project, ProjectDTO.class))
        .collect(Collectors.toList());

        return new ResponseEntity<>(projectDTOs, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @Cacheable(value = "projects", key = "#id")
    public ResponseEntity<ProjectDTO> getProjectById(@PathVariable Long id) {
        try {
            Project project = projectService.getProjectById(id);
            ProjectDTO projectDTO = modelMapper.map(project, ProjectDTO.class);
            return new ResponseEntity<>(projectDTO, HttpStatus.OK);
        } catch (ProjectNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("{id}")
    @CacheEvict(value = {"projects", "issues", "issuesBySearch", "issuesByNLSearch"}, allEntries = true)
    public ResponseEntity<Void> deleteProject(@PathVariable Long id, @CookieValue(name = "jwt", required = false) String token) {
        try {
            Long memberId = jwtTokenProvider.getUserIdFromToken(token);
            projectService.deleteProject(id, memberId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (ProjectNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (UnauthorizedException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }
}
