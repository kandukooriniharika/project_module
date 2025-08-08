package com.example.projectmanagement.service;

import com.example.projectmanagement.dto.ProjectDto;
import com.example.projectmanagement.entity.Project;
import com.example.projectmanagement.entity.User;
import com.example.projectmanagement.exception.ValidationException;
import com.example.projectmanagement.repository.ProjectRepository;
import com.example.projectmanagement.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
    List<String> errors = new ArrayList<>();

    if (projectDto.getName() == null || projectDto.getName().trim().isEmpty()) {
        errors.add("Project name must not be empty.");
    }

    if (projectDto.getProjectKey() == null || projectDto.getProjectKey().trim().isEmpty()) {
        errors.add("Project key must be provided.");
    } else if (projectRepository.existsByProjectKey(projectDto.getProjectKey())) {
        errors.add("Project with key " + projectDto.getProjectKey() + " already exists.");
    }

    if (projectDto.getStartDate() == null) {
        errors.add("Start date is required.");
    }

    // endDate is optional, but if provided, must be after or equal to startDate
    if (projectDto.getStartDate() != null && projectDto.getEndDate() != null &&
        projectDto.getStartDate().isAfter(projectDto.getEndDate())) {
        errors.add("Start date cannot be after end date.");
    }

    if (projectDto.getOwnerId() == null || !userRepository.existsById(projectDto.getOwnerId())) {
        errors.add("Valid owner ID is required.");
    }

    if (!errors.isEmpty()) {
        throw new ValidationException(errors);
    }

    User owner = userRepository.findById(projectDto.getOwnerId()).get();
    Project project = modelMapper.map(projectDto, Project.class);
    project.setOwner(owner);

    // endDate is optional, so no need to set if null
    return convertToDto(projectRepository.save(project));
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

    public ProjectDto updateProject(Long id, ProjectDto updatedDto) {
        List<String> errors = new ArrayList<>();

        Project existing = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + id));

        Project.ProjectStatus existingStatus = existing.getStatus();
        Project.ProjectStatus newStatus = updatedDto.getStatus();

        if (existingStatus == Project.ProjectStatus.ARCHIVED && newStatus != Project.ProjectStatus.ACTIVE) {
            errors.add("Cannot update an archived project unless status is changed to ACTIVE.");
        }

        // Only validate if both dates are present
        if (updatedDto.getStartDate() != null && updatedDto.getEndDate() != null &&
            updatedDto.getStartDate().isAfter(updatedDto.getEndDate())) {
            errors.add("Start date cannot be after end date.");
        }

        if (updatedDto.getOwner() != null && !userRepository.existsById(updatedDto.getOwner().getId())) {
            errors.add("Owner not found with id: " + updatedDto.getOwner().getId());
        }

        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }

        // Apply changes
        if (existingStatus == Project.ProjectStatus.ARCHIVED && newStatus == Project.ProjectStatus.ACTIVE) {
            existing.setStatus(Project.ProjectStatus.ACTIVE);
        } else {
            existing.setName(updatedDto.getName());
            existing.setDescription(updatedDto.getDescription());
            existing.setProjectKey(updatedDto.getProjectKey());
            existing.setStartDate(updatedDto.getStartDate());
            existing.setEndDate(updatedDto.getEndDate()); // endDate can be null
            existing.setStatus(updatedDto.getStatus());

            if (updatedDto.getOwner() != null) {
                User owner = userRepository.findById(updatedDto.getOwner().getId()).get();
                existing.setOwner(owner);
            }
        }

        return convertToDto(projectRepository.save(existing));
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

    public List<ProjectDto> getOverdueProjects() {
        List<Project> all = projectRepository.findAll();
        LocalDateTime now = LocalDateTime.now();
        return all.stream()
            .filter(p -> p.getEndDate() != null && p.getEndDate().isBefore(now))
            .filter(p -> p.getStatus() != Project.ProjectStatus.COMPLETED && p.getStatus() != Project.ProjectStatus.ARCHIVED)
            .map(this::convertToDto)
            .collect(Collectors.toList());
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
