package com.example.projectmanagement.service;

import com.example.projectmanagement.dto.StoryDto;
import com.example.projectmanagement.entity.Epic;
import com.example.projectmanagement.entity.Story;
import com.example.projectmanagement.entity.User;
import com.example.projectmanagement.repository.EpicRepository;
import com.example.projectmanagement.repository.StoryRepository;
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
public class StoryService {
    
    @Autowired
    private StoryRepository storyRepository;
    
    @Autowired
    private EpicRepository epicRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ModelMapper modelMapper;
    
    public StoryDto createStory(StoryDto storyDto) {
        Epic epic = epicRepository.findById(storyDto.getEpicId())
                .orElseThrow(() -> new RuntimeException("Epic not found with id: " + storyDto.getEpicId()));
        
        User reporter = userRepository.findById(storyDto.getReporterId())
                .orElseThrow(() -> new RuntimeException("Reporter not found with id: " + storyDto.getReporterId()));
        
        Story story = modelMapper.map(storyDto, Story.class);
        story.setEpic(epic);
        story.setReporter(reporter);
        
        if (storyDto.getAssigneeId() != null) {
            User assignee = userRepository.findById(storyDto.getAssigneeId())
                    .orElseThrow(() -> new RuntimeException("Assignee not found with id: " + storyDto.getAssigneeId()));
            story.setAssignee(assignee);
        }
        
        Story savedStory = storyRepository.save(story);
        return convertToDto(savedStory);
    }
    
    @Transactional(readOnly = true)
    public StoryDto getStoryById(Long id) {
        Story story = storyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Story not found with id: " + id));
        return convertToDto(story);
    }
    
    @Transactional(readOnly = true)
    public List<StoryDto> getAllStories() {
        return storyRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public Page<StoryDto> getAllStories(Pageable pageable) {
        return storyRepository.findAll(pageable)
                .map(this::convertToDto);
    }
    
    @Transactional(readOnly = true)
    public List<StoryDto> getStoriesByEpic(Long epicId) {
        return storyRepository.findByEpicId(epicId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<StoryDto> getStoriesByStatus(Story.StoryStatus status) {
        return storyRepository.findByStatus(status).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<StoryDto> getStoriesByAssignee(Long assigneeId) {
        return storyRepository.findByAssigneeId(assigneeId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public StoryDto updateStory(Long id, StoryDto storyDto) {
        Story existingStory = storyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Story not found with id: " + id));
        
        existingStory.setTitle(storyDto.getTitle());
        existingStory.setDescription(storyDto.getDescription());
        existingStory.setStatus(storyDto.getStatus());
        existingStory.setPriority(storyDto.getPriority());
        existingStory.setStoryPoints(storyDto.getStoryPoints());
        existingStory.setAcceptanceCriteria(storyDto.getAcceptanceCriteria());
        
        if (storyDto.getEpicId() != null && !storyDto.getEpicId().equals(existingStory.getEpic().getId())) {
            Epic newEpic = epicRepository.findById(storyDto.getEpicId())
                    .orElseThrow(() -> new RuntimeException("Epic not found with id: " + storyDto.getEpicId()));
            existingStory.setEpic(newEpic);
        }
        
        if (storyDto.getAssigneeId() != null) {
            User assignee = userRepository.findById(storyDto.getAssigneeId())
                    .orElseThrow(() -> new RuntimeException("Assignee not found with id: " + storyDto.getAssigneeId()));
            existingStory.setAssignee(assignee);
        } else {
            existingStory.setAssignee(null);
        }
        
        Story updatedStory = storyRepository.save(existingStory);
        return convertToDto(updatedStory);
    }
    
    public void deleteStory(Long id) {
        if (!storyRepository.existsById(id)) {
            throw new RuntimeException("Story not found with id: " + id);
        }
        storyRepository.deleteById(id);
    }
    
    @Transactional(readOnly = true)
    public Page<StoryDto> searchStories(String title, Story.Priority priority, Long epicId, Pageable pageable) {
        if (epicId != null) {
            return storyRepository.findByEpicId(epicId, pageable)
                    .map(this::convertToDto);
        } else if (title != null) {
            return storyRepository.findByTitleContaining(title, pageable)
                    .map(this::convertToDto);
        } else if (priority != null) {
            return storyRepository.findByPriority(priority, pageable)
                    .map(this::convertToDto);
        } else {
            return storyRepository.findAll(pageable)
                    .map(this::convertToDto);
        }
    }
    
    private StoryDto convertToDto(Story story) {
        StoryDto dto = modelMapper.map(story, StoryDto.class);
        dto.setEpicId(story.getEpic().getId());
        dto.setReporterId(story.getReporter().getId());
        if (story.getAssignee() != null) {
            dto.setAssigneeId(story.getAssignee().getId());
        }
        return dto;
    }
}