package com.example.projectmanagement.service;

import com.example.projectmanagement.dto.ProjectDto;
import com.example.projectmanagement.entity.Project;
import com.example.projectmanagement.entity.User;
import com.example.projectmanagement.repository.ProjectRepository;
import com.example.projectmanagement.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ModelMapper modelMapper;

    public ProjectDto createProject(ProjectDto projectDto) {
    if (projectRepository.existsByProjectKey(projectDto.getProjectKey())) {
        throw new RuntimeException("Project with key " + projectDto.getProjectKey() + " already exists");
    }

    if (projectDto.getStartDate() != null && projectDto.getEndDate() != null &&
        projectDto.getStartDate().isAfter(projectDto.getEndDate())) {
        throw new RuntimeException("Start date cannot be after end date.");
    }

    User owner = userRepository.findById(projectDto.getOwnerId())
            .orElseThrow(() -> new RuntimeException("Owner not found with id: " + projectDto.getOwnerId()));

    Project project = modelMapper.map(projectDto, Project.class);
    project.setOwner(owner);

    Project savedProject = projectRepository.save(project);
    return convertToDto(savedProject);
}



    @Transactional(readOnly = true)
    public ProjectDto getProjectById(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + id));
        return convertToDto(project);
    }

    @Transactional(readOnly = true)
    public ProjectDto getProjectByKey(String projectKey) {
        Project project = projectRepository.findByProjectKey(projectKey)
                .orElseThrow(() -> new RuntimeException("Project not found with key: " + projectKey));
        return convertToDto(project);
    }

    @Transactional(readOnly = true)
    public List<ProjectDto> getAllProjects() {
        return projectRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<ProjectDto> getAllProjects(Pageable pageable) {
        return projectRepository.findAll(pageable)
                .map(this::convertToDto);
    }

    @Transactional(readOnly = true)
    public List<ProjectDto> getProjectsByOwner(Long ownerId) {
        return projectRepository.findByOwnerId(ownerId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProjectDto> getProjectsByMember(Long userId) {
        return projectRepository.findByMemberId(userId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProjectDto> getProjectsByStatus(Project.ProjectStatus status) {
        return projectRepository.findByStatus(status).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public ProjectDto updateProject(Long id, ProjectDto updatedProjectDto) {
    Project existingProject = projectRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Project not found with id: " + id));

    Project.ProjectStatus existingStatus = existingProject.getStatus();
    Project.ProjectStatus newStatus = updatedProjectDto.getStatus();

    // Prevent invalid update on archived projects
    if (existingStatus == Project.ProjectStatus.ARCHIVED && newStatus != Project.ProjectStatus.ACTIVE) {
        throw new RuntimeException("Cannot update an archived project except to change status to ACTIVE.");
    }

    // Allow only status change if transitioning from ARCHIVED → ACTIVE
    if (existingStatus == Project.ProjectStatus.ARCHIVED && newStatus == Project.ProjectStatus.ACTIVE) {
        existingProject.setStatus(Project.ProjectStatus.ACTIVE);
        return convertToDto(projectRepository.save(existingProject));
    }

    //  Validate startDate and endDate
    if (updatedProjectDto.getStartDate() != null && updatedProjectDto.getEndDate() != null &&
        updatedProjectDto.getStartDate().isAfter(updatedProjectDto.getEndDate())) {
        throw new RuntimeException("Start date cannot be after end date.");
    }

    // Proceed with update
    existingProject.setName(updatedProjectDto.getName());
    existingProject.setDescription(updatedProjectDto.getDescription());
    existingProject.setProjectKey(updatedProjectDto.getProjectKey());
    existingProject.setStartDate(updatedProjectDto.getStartDate());
    existingProject.setEndDate(updatedProjectDto.getEndDate());
    existingProject.setStatus(updatedProjectDto.getStatus());

    if (updatedProjectDto.getOwner() != null) {
        User owner = userRepository.findById(updatedProjectDto.getOwner().getId())
                .orElseThrow(() -> new RuntimeException("Owner not found with id: " + updatedProjectDto.getOwner().getId()));
        existingProject.setOwner(owner);
    }

    return convertToDto(projectRepository.save(existingProject));
}


    public void deleteProject(Long id) {
        if (!projectRepository.existsById(id)) {
            throw new RuntimeException("Project not found with id: " + id);
        }
        projectRepository.deleteById(id);
    }

    public ProjectDto addMemberToProject(Long projectId, Long userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + projectId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        if (!project.getMembers().contains(user)) {
            project.getMembers().add(user);
            projectRepository.save(project);
        }

        return convertToDto(project);
    }

    public ProjectDto removeMemberFromProject(Long projectId, Long userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + projectId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        project.getMembers().remove(user);
        projectRepository.save(project);

        return convertToDto(project);
    }

    @Transactional(readOnly = true)
    public Page<ProjectDto> searchProjects(String name, Project.ProjectStatus status, Pageable pageable) {
        if (name != null && status != null) {
            return projectRepository.findByNameContainingAndStatus(name, status, pageable)
                    .map(this::convertToDto);
        } else if (name != null) {
            return projectRepository.findByNameContaining(name, pageable)
                    .map(this::convertToDto);
        } else if (status != null) {
            return projectRepository.findByStatus(status, pageable)
                    .map(this::convertToDto);
        } else {
            return projectRepository.findAll(pageable)
                    .map(this::convertToDto);
        }
    }

    private ProjectDto convertToDto(Project project) {
        ProjectDto dto = modelMapper.map(project, ProjectDto.class);
        dto.setOwnerId(project.getOwner().getId());
        dto.setStartDate(project.getStartDate());
        dto.setEndDate(project.getEndDate());
        return dto;
    }
public ProjectDto unarchiveProject(Long projectId) {
    Project project = projectRepository.findById(projectId)
        .orElseThrow(() -> new RuntimeException("Project not found"));

    if (project.getStatus() != Project.ProjectStatus.ARCHIVED) {
        throw new RuntimeException("Only archived projects can be unarchived.");
    }

    project.setStatus(Project.ProjectStatus.ACTIVE); // or IN_PROGRESS
    Project updated = projectRepository.save(project);

    return convertToDto(updated); // or use manual conversion
}


   


}
