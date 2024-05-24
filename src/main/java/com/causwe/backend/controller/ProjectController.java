package com.causwe.backend.controller;

import com.causwe.backend.dto.ProjectDTO;
import com.causwe.backend.model.Project;
import com.causwe.backend.service.ProjectService;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ModelMapper modelMapper;

    @PostMapping("")
    public ResponseEntity<ProjectDTO> createProject(@RequestBody ProjectDTO projectDTO, @CookieValue(value = "memberId", required = false) Long memberId) {
        Project newProject = projectService.createProject(modelMapper.map(projectDTO, Project.class), memberId);
        ProjectDTO newProjectDTO = modelMapper.map(newProject, ProjectDTO.class);

        return new ResponseEntity<>(newProjectDTO, HttpStatus.CREATED);
    }

    @GetMapping("")
    public ResponseEntity<List<ProjectDTO>> getAllProjects() {
        List<Project> projects = projectService.getAllProjects();
        
        List<ProjectDTO> projectDTOs = projects
        .stream()
        .map(project -> modelMapper.map(project, ProjectDTO.class))
        .collect(Collectors.toList());

        return new ResponseEntity<>(projectDTOs, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectDTO> getProjectById(@PathVariable Long id) {
        Project project = projectService.getProjectById(id);
        if (project != null) {
            ProjectDTO projectDTO = modelMapper.map(project, ProjectDTO.class);
            return new ResponseEntity<>(projectDTO, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id, @CookieValue(value = "memberId", required = false) Long memberId) {
        boolean isDeleted = projectService.deleteProject(id, memberId);
        if (isDeleted) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
