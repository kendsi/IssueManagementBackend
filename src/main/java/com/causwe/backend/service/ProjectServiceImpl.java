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
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final UserService userService;

    @Autowired
    public ProjectServiceImpl(ProjectRepository projectRepository, UserService userService) {
        this.projectRepository = projectRepository;
        this.userService = userService;
    }

    @Override
    public Project createProject(Project project, Long memberId) {
        User currentUser = userService.getUserById(memberId);
        if (currentUser == null) {
            throw new UnauthorizedException("User not logged in");
        }
        return projectRepository.save(project);
    }

    @Override
    public List<Project> getAllProjects() {
        return projectRepository.findAllByOrderByIdAsc();
    }

    @Override
    public Project getProjectById(Long id) {
        return projectRepository.findById(id).orElseThrow(() -> new ProjectNotFoundException(id));
    }

    @Override
    public boolean deleteProject(Long id, Long memberId) {
        User currentUser = userService.getUserById(memberId);
        if (currentUser == null) {
            throw new UnauthorizedException("User not logged in");
        }

        if (currentUser.getRole() == User.Role.ADMIN) {
            if (projectRepository.existsById(id)) {
                projectRepository.deleteById(id);
                return true;
            } else {
                throw new ProjectNotFoundException(id);
            }
        } else {
            throw new UnauthorizedException("You are not authorized to delete this comment.");
        }
    }
}
