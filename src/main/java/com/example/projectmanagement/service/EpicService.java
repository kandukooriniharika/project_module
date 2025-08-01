package com.example.projectmanagement.service;

import com.example.projectmanagement.dto.EpicDto;
import com.example.projectmanagement.dto.StoryDto;
import com.example.projectmanagement.entity.Epic;
import com.example.projectmanagement.entity.Project;
import com.example.projectmanagement.entity.Story;
import com.example.projectmanagement.entity.Epic.EpicStatus;
import com.example.projectmanagement.entity.Epic.Priority;
import com.example.projectmanagement.repository.EpicRepository;
import com.example.projectmanagement.repository.ProjectRepository;



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

            // ✅ Convert String to Enum
            if (epicDto.getStatus() != null) {
                existingEpic.setStatus(Epic.EpicStatus.valueOf(epicDto.getStatus()));
            }
            if (epicDto.getPriority() != null) {
                existingEpic.setPriority(Epic.Priority.valueOf(epicDto.getPriority()));
            }

            existingEpic.setProgressPercentage(epicDto.getProgressPercentage());

            // ✅ Convert LocalDateTime to LocalDate
            if (epicDto.getDueDate() != null) {
                existingEpic.setDueDate(epicDto.getDueDate().toLocalDate());
            }

            if (epicDto.getProjectId() != null) {
                Project project = projectRepository.findById(epicDto.getProjectId()).orElse(null);
                existingEpic.setProject(project);
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

        // Convert enum to String
        if (epic.getStatus() != null) {
            dto.setStatus(epic.getStatus().name()); // EpicStatus to String
        }

        if (epic.getPriority() != null) {
            dto.setPriority(epic.getPriority().name()); // Priority to String
        }

        dto.setProgressPercentage(epic.getProgressPercentage());

        // Convert LocalDate to LocalDateTime for DTO
        if (epic.getDueDate() != null) {
            dto.setDueDate(epic.getDueDate().atStartOfDay()); // LocalDate -> LocalDateTime
        }

        if (epic.getProject() != null) {
            dto.setProjectId(epic.getProject().getId());
        }

        

        return dto;
    }

    private Epic convertToEntity(EpicDto dto) {
        Epic epic = new Epic();
        epic.setName(dto.getName());
        epic.setDescription(dto.getDescription());

        // Convert String to Enum safely
        if (dto.getStatus() != null) {
            epic.setStatus(Epic.EpicStatus.valueOf(dto.getStatus().toUpperCase()));
        }

        if (dto.getPriority() != null) {
            epic.setPriority(Epic.Priority.valueOf(dto.getPriority().toUpperCase()));
        }

        epic.setProgressPercentage(dto.getProgressPercentage());

        // Convert LocalDateTime to LocalDate
        if (dto.getDueDate() != null) {
            epic.setDueDate(dto.getDueDate().toLocalDate());
        }

        if (dto.getProjectId() != null) {
            Project project = projectRepository.findById(dto.getProjectId()).orElse(null);
            epic.setProject(project);
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

//     public List<StoryDto> getStoriesByEpicId(Long epicId) {
//     Epic epic = epicRepository.findById(epicId)
//         .orElseThrow(() -> new RuntimeException("Epic not found with ID: " + epicId));

//     List<Story> stories = storyRepository.findByEpic(epic);  // or findByEpicId(epicId)

//     return stories.stream()
//         .map(story -> modelMapper.map(story, StoryDto.class))
//         .collect(Collectors.toList());
// }

}
