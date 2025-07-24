package com.example.projectmanagement.service;

import com.example.projectmanagement.dto.EpicDto;
import com.example.projectmanagement.entity.Epic;
import com.example.projectmanagement.entity.Project;
import com.example.projectmanagement.entity.Sprint;
import com.example.projectmanagement.entity.Epic.EpicStatus;
import com.example.projectmanagement.entity.Epic.Priority;
import com.example.projectmanagement.repository.EpicRepository;
import com.example.projectmanagement.repository.ProjectRepository;
import com.example.projectmanagement.repository.SprintRepository;
import com.example.projectmanagement.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EpicService {

    @Autowired
    private EpicRepository epicRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository UserRepository;

    @Autowired
    private SprintRepository sprintRepository;

    // ✅ Create Epic
    public EpicDto createEpic(EpicDto epicDto) {
        Epic epic = convertToEntity(epicDto);
        Epic savedEpic = epicRepository.save(epic);
        return convertToDto(savedEpic);
    }

    // ✅ Get All Epics
    public List<EpicDto> getAllEpics() {
        return epicRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // ✅ Get by ID
    public EpicDto getEpicById(Long id) {
        Optional<Epic> optionalEpic = epicRepository.findById(id);
        return optionalEpic.map(this::convertToDto).orElse(null);
    }

    // ✅ Update Epic
    public EpicDto updateEpic(Long id, EpicDto epicDto) {
        Optional<Epic> optionalEpic = epicRepository.findById(id);
        if (optionalEpic.isPresent()) {
            Epic existingEpic = optionalEpic.get();
            existingEpic.setName(epicDto.getName());
            existingEpic.setDescription(epicDto.getDescription());
            existingEpic.setStatus(epicDto.getStatus());
            existingEpic.setPriority(epicDto.getPriority());
            existingEpic.setProgressPercentage(epicDto.getProgressPercentage());
            existingEpic.setDueDate(epicDto.getDueDate());

            if (epicDto.getProjectId() != null) {
                Project project = projectRepository.findById(epicDto.getProjectId()).orElse(null);
                existingEpic.setProject(project);
            }

            if (epicDto.getSprintId() != null) {
                Sprint sprint = sprintRepository.findById(epicDto.getSprintId()).orElseThrow(
                        () -> new RuntimeException("Sprint not found with id: " + epicDto.getSprintId())
                );
                existingEpic.setSprint(sprint);
            } else {
                existingEpic.setSprint(null);
            }

            Epic updatedEpic = epicRepository.save(existingEpic);
            return convertToDto(updatedEpic);
        }
        return null;
    }

    // ✅ Delete Epic
    public boolean deleteEpic(Long id) {
        if (epicRepository.existsById(id)) {
            epicRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // ✅ DTO Conversion
    private EpicDto convertToDto(Epic epic) {
        EpicDto dto = new EpicDto();
        dto.setId(epic.getId());
        dto.setName(epic.getName());
        dto.setDescription(epic.getDescription());
        dto.setStatus(epic.getStatus());
        dto.setPriority(epic.getPriority());
        dto.setProgressPercentage(epic.getProgressPercentage());
        dto.setDueDate(epic.getDueDate());

        if (epic.getProject() != null) {
            dto.setProjectId(epic.getProject().getId());
        }

        if (epic.getSprint() != null) {
            dto.setSprintId(epic.getSprint().getId());
        }

        return dto;
    }

    private Epic convertToEntity(EpicDto dto) {
        Epic epic = new Epic();
        epic.setName(dto.getName());
        epic.setDescription(dto.getDescription());
        epic.setStatus(dto.getStatus());
        epic.setPriority(dto.getPriority());
        epic.setProgressPercentage(dto.getProgressPercentage());
        epic.setDueDate(dto.getDueDate());

        if (dto.getProjectId() != null) {
            Project project = projectRepository.findById(dto.getProjectId()).orElse(null);
            epic.setProject(project);
        }

        if (dto.getSprintId() != null) {
            Sprint sprint = sprintRepository.findById(dto.getSprintId()).orElseThrow(
                    () -> new RuntimeException("Sprint not found with id: " + dto.getSprintId())
            );
            epic.setSprint(sprint);
        }

        return epic;
    }

    // ✅ Optional methods to implement
    public List<EpicDto> getEpicsByProjectId(Long projectId) {
        return epicRepository.findByProjectId(projectId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<EpicDto> getEpicsByOrganizationId(Long organizationId) {
        // Assuming you have org info in project or epics
        throw new UnsupportedOperationException("Unimplemented method 'getEpicsByOrganizationId'");
    }

    public List<EpicDto> getEpicsByStatus(EpicStatus status) {
        return epicRepository.findByStatus(status).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public Page<EpicDto> searchEpics(String name, Priority priority, Long projectId, Pageable pageable) {
        throw new UnsupportedOperationException("Unimplemented method 'searchEpics'");
    }
}
