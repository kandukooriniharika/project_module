package com.example.projectmanagement.controller;

import com.example.projectmanagement.dto.SprintDto;
import com.example.projectmanagement.dto.TaskDto;
import com.example.projectmanagement.entity.Sprint;
import com.example.projectmanagement.entity.User;
import com.example.projectmanagement.security.annotation.RequireProjectEntityAccess;
import com.example.projectmanagement.security.annotation.RequireRole;
import com.example.projectmanagement.security.annotation.RequireTaskAccess;
import com.example.projectmanagement.service.SprintService;
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
import com.example.projectmanagement.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/sprints")
@CrossOrigin(origins = "*")
public class SprintController {
    
    @Autowired
    private SprintService sprintService;
    
    @Autowired
    private TaskService taskService;

    @Autowired
    private UserRepository userRepository;

    // Only Product Owners, Scrum Masters, and Admins can create sprints
    @PostMapping
    @RequireRole({User.UserRole.PRODUCT_OWNER, User.UserRole.SCRUM_MASTER, User.UserRole.ADMIN})
    public ResponseEntity<SprintDto> createSprint(@Valid @RequestBody SprintDto sprintDto) {
        SprintDto createdSprint = sprintService.createSprint(sprintDto);
        return new ResponseEntity<>(createdSprint, HttpStatus.CREATED);
    }
    
    // Project members and admins can view sprint details
    @GetMapping("/{id}")
    @RequireProjectEntityAccess(entityType = RequireProjectEntityAccess.EntityType.SPRINT, value = {RequireProjectEntityAccess.AccessType.MEMBER, RequireProjectEntityAccess.AccessType.ADMIN})
    public ResponseEntity<SprintDto> getSprintById(@PathVariable Long id) {
        SprintDto sprint = sprintService.getSprintById(id);
        return ResponseEntity.ok(sprint);
    }
    
    // All authenticated users can view sprints (filtered by accessible projects in service layer)
    @GetMapping
    public ResponseEntity<Page<SprintDto>> getAllSprints(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<SprintDto> sprints = sprintService.getAllSprints(pageable);
        return ResponseEntity.ok(sprints);
    }
    
    // Project members and admins can view sprint tasks
    @GetMapping("/{sprintId}/tasks")
    @RequireProjectEntityAccess(entityType = RequireProjectEntityAccess.EntityType.SPRINT, entityIdParam = "sprintId", value = {RequireProjectEntityAccess.AccessType.MEMBER, RequireProjectEntityAccess.AccessType.ADMIN})
    public ResponseEntity<List<TaskDto>> getSprintTasks(@PathVariable Long sprintId) {
        List<TaskDto> tasks = taskService.getTasksBySprint(sprintId);
        return ResponseEntity.ok(tasks);
    }
    
    // Project members and managers can add tasks to sprints
    @PostMapping("/{sprintId}/tasks")
    @RequireProjectEntityAccess(entityType = RequireProjectEntityAccess.EntityType.SPRINT, entityIdParam = "sprintId", value = {RequireProjectEntityAccess.AccessType.MEMBER, RequireProjectEntityAccess.AccessType.ADMIN})
    public ResponseEntity<TaskDto> addTaskToSprint(@PathVariable Long sprintId, @Valid @RequestBody TaskDto taskDto) {
        taskDto.setSprintId(sprintId);
        TaskDto createdTask = taskService.createTask(taskDto);
        return new ResponseEntity<>(createdTask, HttpStatus.CREATED);
    }
    
    // Project members and managers can assign tasks to sprints
    @PostMapping("/{sprintId}/tasks/{taskId}")
    @RequireProjectEntityAccess(entityType = RequireProjectEntityAccess.EntityType.SPRINT, entityIdParam = "sprintId", value = {RequireProjectEntityAccess.AccessType.MEMBER, RequireProjectEntityAccess.AccessType.ADMIN})
    public ResponseEntity<TaskDto> assignTaskToSprint(@PathVariable Long sprintId, @PathVariable Long taskId) {
        TaskDto updatedTask = taskService.assignTaskToSprint(taskId, sprintId);
        return ResponseEntity.ok(updatedTask);
    }
    
    // Project members and managers can remove tasks from sprints
    @DeleteMapping("/{sprintId}/tasks/{taskId}")
    @RequireProjectEntityAccess(entityType = RequireProjectEntityAccess.EntityType.SPRINT, entityIdParam = "sprintId", value = {RequireProjectEntityAccess.AccessType.MEMBER, RequireProjectEntityAccess.AccessType.ADMIN})
    public ResponseEntity<TaskDto> removeTaskFromSprint(@PathVariable Long sprintId, @PathVariable Long taskId) {
        TaskDto updatedTask = taskService.removeTaskFromSprint(taskId);
        return ResponseEntity.ok(updatedTask);
    }
    
    // All authenticated users can view sprints by status (filtered by accessible projects)
    @GetMapping("/status/{status}")
    public ResponseEntity<List<SprintDto>> getSprintsByStatus(@PathVariable Sprint.SprintStatus status) {
        List<SprintDto> sprints = sprintService.getSprintsByStatus(status);
        return ResponseEntity.ok(sprints);
    }
    
    // All authenticated users can view active sprints (filtered by accessible projects)
    @GetMapping("/active")
    public ResponseEntity<List<SprintDto>> getActiveSprints() {
        List<SprintDto> sprints = sprintService.getActiveSprintsOnDate(LocalDateTime.now());
        return ResponseEntity.ok(sprints);
    }
    
    // All authenticated users can view overdue sprints (filtered by accessible projects)
    @GetMapping("/overdue")
    public ResponseEntity<List<SprintDto>> getOverdueSprints() {
        List<SprintDto> sprints = sprintService.getOverdueSprints();
        return ResponseEntity.ok(sprints);
    }
    
    // Only Scrum Masters and admins can complete sprints
    @PostMapping("/{id}/complete")
    @RequireProjectEntityAccess(entityType = RequireProjectEntityAccess.EntityType.SPRINT, value = {RequireProjectEntityAccess.AccessType.OWNER, RequireProjectEntityAccess.AccessType.ADMIN})
    public ResponseEntity<SprintDto> completeSprint(@PathVariable Long id) {
        SprintDto updatedSprint = sprintService.completeSprint(id);
        return ResponseEntity.ok(updatedSprint);
    }
    
    // Project owners and admins can update sprints
    @PutMapping("/{id}")
    @RequireProjectEntityAccess(entityType = RequireProjectEntityAccess.EntityType.SPRINT, value = {RequireProjectEntityAccess.AccessType.OWNER, RequireProjectEntityAccess.AccessType.ADMIN})
    public ResponseEntity<SprintDto> updateSprint(@PathVariable Long id, @Valid @RequestBody SprintDto sprintDto) {
        SprintDto updatedSprint = sprintService.updateSprint(id, sprintDto);
        return ResponseEntity.ok(updatedSprint);
    }
    
    // Only project owners and admins can delete sprints
    @DeleteMapping("/{id}")
    @RequireProjectEntityAccess(entityType = RequireProjectEntityAccess.EntityType.SPRINT, value = {RequireProjectEntityAccess.AccessType.OWNER, RequireProjectEntityAccess.AccessType.ADMIN})
    public ResponseEntity<Void> deleteSprint(@PathVariable Long id) {
        sprintService.deleteSprint(id);
        return ResponseEntity.noContent().build();
    }
}