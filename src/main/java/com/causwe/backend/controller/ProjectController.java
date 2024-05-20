package com.causwe.backend.controller;

import com.causwe.backend.model.Project;
import com.causwe.backend.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    @PostMapping("")
    public ResponseEntity<Project> createProject(@RequestBody Project project, @CookieValue(value = "memberId", required = false) Long memberId) {
        Project newProject = projectService.createProject(project, memberId);
        return new ResponseEntity<>(newProject, HttpStatus.CREATED);
    }

    @GetMapping("")
    public ResponseEntity<List<Project>> getAllProjects() {
        List<Project> projects = projectService.getAllProjects();
        return new ResponseEntity<>(projects, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Project> getProjectById(@PathVariable Long id) {
        Project project = projectService.getProjectById(id);
        if (project != null) {
            return new ResponseEntity<>(project, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
