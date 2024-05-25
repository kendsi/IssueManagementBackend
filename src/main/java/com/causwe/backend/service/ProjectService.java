package com.causwe.backend.service;

import com.causwe.backend.model.Project;

import java.util.List;

public interface ProjectService {
    Project createProject(Project project, Long memberId);
    List<Project> getAllProjects();
    Project getProjectById(Long id);
    boolean deleteProject(Long id, Long memberId);
}