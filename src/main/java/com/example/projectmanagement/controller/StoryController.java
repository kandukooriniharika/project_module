package com.example.projectmanagement.controller;

import com.example.projectmanagement.dto.StoryDto;
import com.example.projectmanagement.dto.TaskDto;
import com.example.projectmanagement.entity.Story;
import com.example.projectmanagement.service.StoryService;
import com.example.projectmanagement.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stories")
@CrossOrigin(origins = "*")
public class StoryController {
    
    @Autowired
    private StoryService storyService;
    
    @Autowired
    private TaskService taskService;
    
    @PostMapping
    public ResponseEntity<StoryDto> createStory(@Valid @RequestBody StoryDto storyDto) {
        StoryDto createdStory = storyService.createStory(storyDto);
        return new ResponseEntity<>(createdStory, HttpStatus.CREATED);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<StoryDto> getStoryById(@PathVariable Long id) {
        StoryDto story = storyService.getStoryById(id);
        return ResponseEntity.ok(story);
    }
    
    @GetMapping
    public ResponseEntity<Page<StoryDto>> getAllStories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Story.Priority priority,
            @RequestParam(required = false) Long epicId) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<StoryDto> stories = storyService.searchStories(title, priority, epicId, pageable);
        return ResponseEntity.ok(stories);
    }
    
    @GetMapping("/{id}/tasks")
    public ResponseEntity<List<TaskDto>> getStoryTasks(@PathVariable Long id) {
        List<TaskDto> tasks = taskService.getTasksByStory(id);
        return ResponseEntity.ok(tasks);
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<List<StoryDto>> getStoriesByStatus(@PathVariable Story.StoryStatus status) {
        List<StoryDto> stories = storyService.getStoriesByStatus(status);
        return ResponseEntity.ok(stories);
    }
    
    @GetMapping("/assignee/{assigneeId}")
    public ResponseEntity<List<StoryDto>> getStoriesByAssignee(@PathVariable Long assigneeId) {
        List<StoryDto> stories = storyService.getStoriesByAssignee(assigneeId);
        return ResponseEntity.ok(stories);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<StoryDto> updateStory(@PathVariable Long id, @Valid @RequestBody StoryDto storyDto) {
        StoryDto updatedStory = storyService.updateStory(id, storyDto);
        return ResponseEntity.ok(updatedStory);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStory(@PathVariable Long id) {
        storyService.deleteStory(id);
        return ResponseEntity.noContent().build();
    }
}