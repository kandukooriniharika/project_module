package com.example.projectmanagement.controller;
import com.example.projectmanagement.dto.StoryDto;
import com.example.projectmanagement.dto.TaskDto;
import com.example.projectmanagement.entity.Story;
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
 
    // ✅ Create a story

    @PostMapping
    public ResponseEntity<StoryDto> createStory(@Valid @RequestBody StoryDto storyDto) {
        StoryDto createdStory = storyService.createStory(storyDto);
        return new ResponseEntity<>(createdStory, HttpStatus.CREATED);

    }
 
    // ✅ Get story by ID

    @GetMapping("/{id}")

    public ResponseEntity<StoryDto> getStoryById(@PathVariable Long id) {

        StoryDto story = storyService.getStoryById(id);

        return ResponseEntity.ok(story);

    }
 
    // ✅ Get all stories with optional filters and pagination

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
 
    // ✅ Get all tasks under a story

    @GetMapping("/{id}/tasks")

    public ResponseEntity<List<TaskDto>> getStoryTasks(@PathVariable Long id) {

        List<TaskDto> tasks = taskService.getTasksByStory(id);

        return ResponseEntity.ok(tasks);

    }
 
    // ✅ Get stories by status

    @GetMapping("/status/{status}")

    public ResponseEntity<List<StoryDto>> getStoriesByStatus(@PathVariable Story.StoryStatus status) {

        List<StoryDto> stories = storyService.getStoriesByStatus(status);

        return ResponseEntity.ok(stories);

    }
 
    // ✅ Get stories by assignee ID

    @GetMapping("/assignee/{assigneeId}")

    public ResponseEntity<List<StoryDto>> getStoriesByAssignee(@PathVariable Long assigneeId) {

        List<StoryDto> stories = storyService.getStoriesByAssignee(assigneeId);

        return ResponseEntity.ok(stories);

    }
 
    // ✅ Update a story

    @PutMapping("/{id}")

    public ResponseEntity<StoryDto> updateStory(@PathVariable Long id, @Valid @RequestBody StoryDto storyDto) {

        StoryDto updatedStory = storyService.updateStory(id, storyDto);

        return ResponseEntity.ok(updatedStory);

    }
 
    // ✅ Delete a story

    @DeleteMapping("/{id}")

    public ResponseEntity<Void> deleteStory(@PathVariable Long id) {

        storyService.deleteStory(id);

        return ResponseEntity.noContent().build();

    }

}

 