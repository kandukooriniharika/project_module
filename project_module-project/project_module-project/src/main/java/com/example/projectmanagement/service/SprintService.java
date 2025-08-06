// 📁 SprintService.java - Updated with 'updateSprint' method accepting only 2 parameters
package com.example.projectmanagement.service;

import com.example.projectmanagement.dto.SprintDto;
import com.example.projectmanagement.entity.Project;
import com.example.projectmanagement.entity.Sprint;
import com.example.projectmanagement.entity.User;
import com.example.projectmanagement.repository.ProjectRepository;
import com.example.projectmanagement.repository.SprintRepository;
import com.example.projectmanagement.entity.RolePermissionChecker;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class SprintService {

    @Autowired
    private SprintRepository sprintRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ModelMapper modelMapper;

    public SprintDto createSprint(SprintDto sprintDto, User currentUser) {
        if (!RolePermissionChecker.canCreateSprint(currentUser.getRole())) {
            throw new RuntimeException("Access denied: You are not allowed to create a sprint.");
        }

        Project project = projectRepository.findById(sprintDto.getProjectId())
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + sprintDto.getProjectId()));

        if (sprintDto.getStartDate().isAfter(sprintDto.getEndDate())) {
            throw new RuntimeException("Start date cannot be after end date");
        }

        validateNoSprintOverlap(sprintDto.getProjectId(), sprintDto.getStartDate(), sprintDto.getEndDate(), null);

        Sprint sprint = modelMapper.map(sprintDto, Sprint.class);
        sprint.setProject(project);

        Sprint savedSprint = sprintRepository.save(sprint);
        return convertToDto(savedSprint);
    }

    public SprintDto startSprint(Long id, User currentUser) {
        if (!RolePermissionChecker.canStartSprint(currentUser.getRole())) {
            throw new RuntimeException("Access denied: You are not allowed to start a sprint.");
        }

        Sprint sprint = sprintRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sprint not found with id: " + id));

        if (sprint.getStatus() != Sprint.SprintStatus.PLANNING) {
            throw new RuntimeException("Only planning sprints can be started");
        }

        boolean hasActiveSprint = sprintRepository.existsActiveSprintInProject(sprint.getProject().getId());
        if (hasActiveSprint) {
            throw new RuntimeException("Another active sprint already exists in this project.");
        }

        sprint.setStatus(Sprint.SprintStatus.ACTIVE);
        sprint.setStartedBy(currentUser);
        sprint.setStartedAt(LocalDateTime.now());

        Sprint updatedSprint = sprintRepository.save(sprint);
        return convertToDto(updatedSprint);
    }

    public SprintDto completeSprint(Long id) {
        Sprint sprint = sprintRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sprint not found with id: " + id));

        if (sprint.getStatus() != Sprint.SprintStatus.ACTIVE) {
            throw new RuntimeException("Only active sprints can be completed");
        }

        sprint.setStatus(Sprint.SprintStatus.COMPLETED);
        Sprint updatedSprint = sprintRepository.save(sprint);
        return convertToDto(updatedSprint);
    }

    public SprintDto updateSprint(Long id, SprintDto sprintDto) {
        Sprint sprint = sprintRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sprint not found with id: " + id));

        Project project = projectRepository.findById(sprintDto.getProjectId())
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + sprintDto.getProjectId()));

        if (sprintDto.getStartDate().isAfter(sprintDto.getEndDate())) {
            throw new RuntimeException("Start date cannot be after end date");
        }

        validateNoSprintOverlap(sprintDto.getProjectId(), sprintDto.getStartDate(), sprintDto.getEndDate(), id);

        sprint.setName(sprintDto.getName());
        sprint.setGoal(sprintDto.getGoal());
        sprint.setStartDate(sprintDto.getStartDate());
        sprint.setEndDate(sprintDto.getEndDate());
        sprint.setProject(project);

        Sprint updatedSprint = sprintRepository.save(sprint);
        return convertToDto(updatedSprint);
    }

    public void deleteSprint(Long id, User currentUser) {
        if (!RolePermissionChecker.canDeleteSprint(currentUser.getRole())) {
            throw new RuntimeException("Access denied: You are not allowed to delete sprints.");
        }

        if (!sprintRepository.existsById(id)) {
            throw new RuntimeException("Sprint not found with id: " + id);
        }
        sprintRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public SprintDto getSprintById(Long id) {
        Sprint sprint = sprintRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sprint not found with id: " + id));
        return convertToDto(sprint);
    }

    @Transactional(readOnly = true)
    public List<SprintDto> getAllSprints() {
        return sprintRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<SprintDto> getAllSprints(Pageable pageable) {
        return sprintRepository.findAll(pageable)
                .map(this::convertToDto);
    }

    @Transactional(readOnly = true)
    public List<SprintDto> getSprintsByProject(Long projectId) {
        return sprintRepository.findByProjectId(projectId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SprintDto> getSprintsByStatus(Sprint.SprintStatus status) {
        return sprintRepository.findByStatus(status).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SprintDto> getActiveSprintsOnDate(LocalDateTime date) {
        return sprintRepository.findActiveSprintsOnDate(date).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SprintDto> getOverdueSprints() {
        return sprintRepository.findOverdueSprints(LocalDateTime.now()).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private void validateNoSprintOverlap(Long projectId, LocalDateTime startDate, LocalDateTime endDate, Long excludeId) {
        List<Sprint> overlappingSprints = sprintRepository.findOverlappingSprints(projectId, startDate, endDate);

        if (excludeId != null) {
            overlappingSprints = overlappingSprints.stream()
                    .filter(s -> !s.getId().equals(excludeId))
                    .collect(Collectors.toList());
        }

        if (!overlappingSprints.isEmpty()) {
            throw new RuntimeException("A sprint already exists within this date range.");
        }
    }

    private SprintDto convertToDto(Sprint sprint) {
        SprintDto dto = modelMapper.map(sprint, SprintDto.class);
        dto.setProjectId(sprint.getProject().getId());
        return dto;
    }
}
