package com.example.projectmanagement.controller;

import com.example.projectmanagement.dto.StoryDto;
import com.example.projectmanagement.dto.TaskDto;
import com.example.projectmanagement.entity.Story;
import com.example.projectmanagement.entity.User;
import com.example.projectmanagement.security.annotation.RequireProjectEntityAccess;
import com.example.projectmanagement.security.annotation.RequireRole;
import com.example.projectmanagement.service.StoryService;
import com.example.projectmanagement.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
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

    // Product owners, scrum masters, developers, and admins can create stories
    @PostMapping
    @RequireRole({User.UserRole.PRODUCT_OWNER, User.UserRole.SCRUM_MASTER, User.UserRole.DEVELOPER, User.UserRole.ADMIN})
    public ResponseEntity<StoryDto> createStory(@Valid @RequestBody StoryDto storyDto) {
        StoryDto createdStory = storyService.createStory(storyDto);
        return new ResponseEntity<>(createdStory, HttpStatus.CREATED);
    }

    // Project members and admins can view story details
    @GetMapping("/{id}")
    @RequireProjectEntityAccess(entityType = RequireProjectEntityAccess.EntityType.STORY, value = {RequireProjectEntityAccess.AccessType.MEMBER, RequireProjectEntityAccess.AccessType.ADMIN})
    public ResponseEntity<StoryDto> getStoryById(@PathVariable Long id) {
        StoryDto story = storyService.getStoryById(id);
        return ResponseEntity.ok(story);
    }

    // All authenticated users can search stories (filtered by accessible projects in service layer)
    @GetMapping
    public ResponseEntity<Page<StoryDto>> getAllStories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Story.Priority priority,
            @RequestParam(required = false) Long epicId,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Long sprintId
    ) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<StoryDto> stories = storyService.searchStories(
                title, priority, epicId, projectId, sprintId, pageable
        );
        return ResponseEntity.ok(stories);
    }

    // Project members and admins can view story tasks
    @GetMapping("/{id}/tasks")
    @RequireProjectEntityAccess(entityType = RequireProjectEntityAccess.EntityType.STORY, value = {RequireProjectEntityAccess.AccessType.MEMBER, RequireProjectEntityAccess.AccessType.ADMIN})
    public ResponseEntity<List<TaskDto>> getStoryTasks(@PathVariable Long id) {
        List<TaskDto> tasks = taskService.getTasksByStory(id);
        return ResponseEntity.ok(tasks);
    }

    // All authenticated users can view stories by status (filtered by accessible projects)
    @GetMapping("/status/{status}")
    public ResponseEntity<List<StoryDto>> getStoriesByStatus(@PathVariable Story.StoryStatus status) {
        List<StoryDto> stories = storyService.getStoriesByStatus(status);
        return ResponseEntity.ok(stories);
    }

    // Managers and admins can view stories by assignee
    @GetMapping("/assignee/{assigneeId}")
    @RequireRole({User.UserRole.PRODUCT_OWNER, User.UserRole.SCRUM_MASTER, User.UserRole.ADMIN})
    public ResponseEntity<List<StoryDto>> getStoriesByAssignee(@PathVariable Long assigneeId) {
        List<StoryDto> stories = storyService.getStoriesByAssignee(assigneeId);
        return ResponseEntity.ok(stories);
    }

    // Project members and admins can view stories by epic
    @GetMapping("/epic/{epicId}")
    @RequireProjectEntityAccess(entityType = RequireProjectEntityAccess.EntityType.EPIC, entityIdParam = "epicId", value = {RequireProjectEntityAccess.AccessType.MEMBER, RequireProjectEntityAccess.AccessType.ADMIN})
    public ResponseEntity<List<StoryDto>> getStoriesByEpic(@PathVariable Long epicId) {
        List<StoryDto> stories = storyService.getStoriesByEpic(epicId);
        return ResponseEntity.ok(stories);
    }

    // Project members and admins can view stories by sprint
    @GetMapping("/sprint/{sprintId}")
    @RequireProjectEntityAccess(entityType = RequireProjectEntityAccess.EntityType.SPRINT, entityIdParam = "sprintId", value = {RequireProjectEntityAccess.AccessType.MEMBER, RequireProjectEntityAccess.AccessType.ADMIN})
    public ResponseEntity<List<StoryDto>> getStoriesBySprint(@PathVariable Long sprintId) {
        return ResponseEntity.ok(storyService.getStoriesBySprint(sprintId));
    }

    // Story assignees, project owners, and admins can update stories
    @PutMapping("/{id}")
    @RequireProjectEntityAccess(entityType = RequireProjectEntityAccess.EntityType.STORY, value = {RequireProjectEntityAccess.AccessType.MEMBER, RequireProjectEntityAccess.AccessType.ADMIN})
    public ResponseEntity<StoryDto> updateStory(@PathVariable Long id, @Valid @RequestBody StoryDto storyDto) {
        StoryDto updatedStory = storyService.updateStory(id, storyDto);
        return ResponseEntity.ok(updatedStory);
    }

    // Only project owners and admins can delete stories
    @DeleteMapping("/{id}")
    @RequireProjectEntityAccess(entityType = RequireProjectEntityAccess.EntityType.STORY, value = {RequireProjectEntityAccess.AccessType.OWNER, RequireProjectEntityAccess.AccessType.ADMIN})
    public ResponseEntity<Void> deleteStory(@PathVariable Long id) {
        storyService.deleteStory(id);
        return ResponseEntity.noContent().build();
    }
}

 