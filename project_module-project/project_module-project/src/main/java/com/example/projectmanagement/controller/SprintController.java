package com.example.projectmanagement.controller;

import com.example.projectmanagement.dto.SprintDto;
import com.example.projectmanagement.dto.TaskDto;
import com.example.projectmanagement.entity.Sprint;
import com.example.projectmanagement.entity.User;
import com.example.projectmanagement.repository.UserRepository;
import com.example.projectmanagement.service.SprintService;
import com.example.projectmanagement.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

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

    // Create Sprint with User context
    @PostMapping
    public ResponseEntity<SprintDto> createSprint(@Valid @RequestBody SprintDto sprintDto) {
        User currentUser = userRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("User not found"));
        SprintDto createdSprint = sprintService.createSprint(sprintDto, currentUser);
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

    // Complete sprint (no User context needed)
    @PostMapping("/{id}/complete")
    public ResponseEntity<SprintDto> completeSprint(@PathVariable Long id) {
        SprintDto updatedSprint = sprintService.completeSprint(id);
        return ResponseEntity.ok(updatedSprint);
    }

    // Update sprint with User context
    @PutMapping("/{id}")
public ResponseEntity<SprintDto> updateSprint(@PathVariable Long id, @Valid @RequestBody SprintDto sprintDto) {
    SprintDto updatedSprint = sprintService.updateSprint(id, sprintDto); // ✅ Only 2 parameters
    return ResponseEntity.ok(updatedSprint);
}


    // Delete sprint with User context
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSprint(@PathVariable Long id) {
        User currentUser = userRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("User not found"));
        sprintService.deleteSprint(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}
