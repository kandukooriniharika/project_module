package com.example.projectmanagement.controller;

import com.example.projectmanagement.dto.SprintDto;
import com.example.projectmanagement.dto.TaskDto;
// import com.example.projectmanagement.dto.UserDto;
import com.example.projectmanagement.entity.Sprint;
// import com.example.projectmanagement.security.CurrentUser;
import com.example.projectmanagement.service.SprintService;
import com.example.projectmanagement.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.Jwt;
import com.example.projectmanagement.repository.UserRepository;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
// import com.example.projectmanagement.service.UserService;
import com.example.projectmanagement.entity.User;
import org.springframework.security.core.Authentication;


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

    
    @PostMapping
    public ResponseEntity<SprintDto> createSprint(@Valid @RequestBody SprintDto sprintDto) {
        SprintDto createdSprint = sprintService.createSprint(sprintDto);
        return new ResponseEntity<>(createdSprint, HttpStatus.CREATED);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<SprintDto> getSprintById(@PathVariable Long id) {
        SprintDto sprint = sprintService.getSprintById(id);
        return ResponseEntity.ok(sprint);
    }
    
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
    
    @GetMapping("/{sprintId}/tasks")
    public ResponseEntity<List<TaskDto>> getSprintTasks(@PathVariable Long sprintId) {
        List<TaskDto> tasks = taskService.getTasksBySprint(sprintId);
        return ResponseEntity.ok(tasks);
    }
    
    @PostMapping("/{sprintId}/tasks")
    public ResponseEntity<TaskDto> addTaskToSprint(@PathVariable Long sprintId, @Valid @RequestBody TaskDto taskDto) {
        taskDto.setSprintId(sprintId);
        TaskDto createdTask = taskService.createTask(taskDto);
        return new ResponseEntity<>(createdTask, HttpStatus.CREATED);
    }
    
    @PostMapping("/{sprintId}/tasks/{taskId}")
    public ResponseEntity<TaskDto> assignTaskToSprint(@PathVariable Long sprintId, @PathVariable Long taskId) {
        TaskDto updatedTask = taskService.assignTaskToSprint(taskId, sprintId);
        return ResponseEntity.ok(updatedTask);
    }
    
    @DeleteMapping("/{sprintId}/tasks/{taskId}")
    public ResponseEntity<TaskDto> removeTaskFromSprint(@PathVariable Long sprintId, @PathVariable Long taskId) {
        TaskDto updatedTask = taskService.removeTaskFromSprint(taskId);
        return ResponseEntity.ok(updatedTask);
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<List<SprintDto>> getSprintsByStatus(@PathVariable Sprint.SprintStatus status) {
        List<SprintDto> sprints = sprintService.getSprintsByStatus(status);
        return ResponseEntity.ok(sprints);
    }
    
    @GetMapping("/active")
    public ResponseEntity<List<SprintDto>> getActiveSprints() {
        List<SprintDto> sprints = sprintService.getActiveSprintsOnDate(LocalDateTime.now());
        return ResponseEntity.ok(sprints);
    }
    
    @GetMapping("/overdue")
    public ResponseEntity<List<SprintDto>> getOverdueSprints() {
        List<SprintDto> sprints = sprintService.getOverdueSprints();
        return ResponseEntity.ok(sprints);
    }
    
    @PutMapping("/{sprintId}/start")
public ResponseEntity<?> startSprint(
    @PathVariable("sprintId") Long sprintId,
    Authentication authentication
) {
    Jwt jwt = (Jwt) authentication.getPrincipal();  // ✅ Extract JWT token
    String email = jwt.getClaimAsString("email");   // ✅ Safely get the email claim

    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

    sprintService.startSprint(sprintId, user); // ✅ Use correct variable name
    return ResponseEntity.ok("Sprint started");
}


    
    @PostMapping("/{id}/complete")
    public ResponseEntity<SprintDto> completeSprint(@PathVariable Long id) {
        SprintDto updatedSprint = sprintService.completeSprint(id);
        return ResponseEntity.ok(updatedSprint);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<SprintDto> updateSprint(@PathVariable Long id, @Valid @RequestBody SprintDto sprintDto) {
        SprintDto updatedSprint = sprintService.updateSprint(id, sprintDto);
        return ResponseEntity.ok(updatedSprint);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSprint(@PathVariable Long id) {
        sprintService.deleteSprint(id);
        return ResponseEntity.noContent().build();
    }
}