package com.causwe.backend.controller;

import com.causwe.backend.exceptions.UnauthorizedException;
import com.causwe.backend.model.Issue;
import com.causwe.backend.model.Project;
import com.causwe.backend.model.User;
import com.causwe.backend.repository.ProjectRepository;
import com.causwe.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("")
    public ResponseEntity<Project> createProject(@RequestBody Project project, @CookieValue(value = "memberId", required = false) Long memberId) {
        User currentUser = userRepository.findById(memberId).orElse(null);
        if (currentUser == null || memberId == null || currentUser.getRole() != User.Role.ADMIN) {
            throw new UnauthorizedException("Only admins can create projects.");
        }

        Project newProject = projectRepository.save(project);
        return new ResponseEntity<>(newProject, HttpStatus.CREATED);
    }

    @GetMapping("")
    public ResponseEntity<List<Project>> getAllProjects() {
        List<Project> projects = projectRepository.findAll();
        return new ResponseEntity<>(projects, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Project> getProjectById(@PathVariable Long id) {
        Optional<Project> project = projectRepository.findById(id);
        if (project.isPresent()) {
            return new ResponseEntity<>(project.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}