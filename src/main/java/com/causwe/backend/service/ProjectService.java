package com.causwe.backend.service;

import com.causwe.backend.exceptions.UnauthorizedException;
import com.causwe.backend.exceptions.ProjectNotFoundException;
import com.causwe.backend.model.Project;
import com.causwe.backend.model.User;
import com.causwe.backend.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserService userService;

    public Project createProject(Project project, Long memberId) {
        User currentUser = userService.getUserById(memberId);
        if (currentUser == null) {
            throw new UnauthorizedException("User not logged in");
        }
        return projectRepository.save(project);
    }

    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    public Project getProjectById(Long id) {
        Optional<Project> project = projectRepository.findById(id);
        return project.orElseThrow(() -> new ProjectNotFoundException(id));
    }

    // TODO 권한에 따라 삭제
    public boolean deleteProject(Long id, Long memberId) {
        User currentUser = userService.getUserById(memberId);
        if (currentUser == null) {
            throw new UnauthorizedException("User not logged in");
        }

        Optional<Project> project = projectRepository.findById(id);

        if (project.isPresent()) {
            projectRepository.deleteById(id);
            return true;
        }
        else {
            return false;
        }
    }
}
