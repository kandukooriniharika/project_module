package com.example.projectmanagement.controller;

import com.example.projectmanagement.dto.TaskDto;
import com.example.projectmanagement.entity.Task;
import com.example.projectmanagement.entity.User;
import com.example.projectmanagement.security.annotation.RequireRole;
import com.example.projectmanagement.security.annotation.RequireTaskAccess;
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
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(origins = "*")
public class TaskController {
    
    @Autowired
    private TaskService taskService;
    
    // Project members and managers can create tasks
    @PostMapping
    @RequireRole({User.UserRole.PRODUCT_OWNER, User.UserRole.SCRUM_MASTER, User.UserRole.DEVELOPER, User.UserRole.TESTER, User.UserRole.ADMIN})
    public ResponseEntity<TaskDto> createTask(@Valid @RequestBody TaskDto taskDto) {
        TaskDto createdTask = taskService.createTask(taskDto);
        return new ResponseEntity<>(createdTask, HttpStatus.CREATED);
    }
    
    // Project members can view task details
    @GetMapping("/{id}")
    @RequireTaskAccess({RequireTaskAccess.AccessType.PROJECT_MEMBER, RequireTaskAccess.AccessType.ADMIN})
    public ResponseEntity<TaskDto> getTaskById(@PathVariable Long id) {
        TaskDto task = taskService.getTaskById(id);
        return ResponseEntity.ok(task);
    }
    
    // All authenticated users can search tasks (filtered by their accessible projects in service layer)
    @GetMapping
    public ResponseEntity<Page<TaskDto>> getAllTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Task.Priority priority,
            @RequestParam(required = false) Long assigneeId) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<TaskDto> tasks = taskService.searchTasks(title, priority, assigneeId, pageable);
        return ResponseEntity.ok(tasks);
    }
    
    // All authenticated users can view backlog (filtered by accessible projects)
    @GetMapping("/backlog")
    public ResponseEntity<List<TaskDto>> getBacklogTasks() {
        List<TaskDto> tasks = taskService.getBacklogTasks();
        return ResponseEntity.ok(tasks);
    }
    
    // All authenticated users can view tasks by status (filtered by accessible projects)
    @GetMapping("/status/{status}")
    public ResponseEntity<List<TaskDto>> getTasksByStatus(@PathVariable Task.TaskStatus status) {
        List<TaskDto> tasks = taskService.getTasksByStatus(status);
        return ResponseEntity.ok(tasks);
    }
    
    // Assignees, reporters, project owners and admins can update tasks
    @PutMapping("/{id}")
    @RequireTaskAccess({RequireTaskAccess.AccessType.ASSIGNEE_OR_REPORTER, RequireTaskAccess.AccessType.PROJECT_OWNER, RequireTaskAccess.AccessType.ADMIN})
    public ResponseEntity<TaskDto> updateTask(@PathVariable Long id, @Valid @RequestBody TaskDto taskDto) {
        TaskDto updatedTask = taskService.updateTask(id, taskDto);
        return ResponseEntity.ok(updatedTask);
    }
    
    // Only project owners and admins can delete tasks
    @DeleteMapping("/{id}")
    @RequireTaskAccess({RequireTaskAccess.AccessType.PROJECT_OWNER, RequireTaskAccess.AccessType.ADMIN})
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
   }

    // Project members and admins can view task counts for stories
    @GetMapping("/story/{storyId}/count")
    @RequireRole({User.UserRole.PRODUCT_OWNER, User.UserRole.SCRUM_MASTER, User.UserRole.DEVELOPER, User.UserRole.TESTER, User.UserRole.ADMIN})
    public ResponseEntity<?> getTaskCountByStory(@PathVariable Long storyId) {
        long count = taskService.countTasksByStoryId(storyId);
        return ResponseEntity.ok(Map.of("storyId", storyId, "taskCount", count));
    }
}
