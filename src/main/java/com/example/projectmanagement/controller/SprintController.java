package com.example.projectmanagement.controller;

import com.example.projectmanagement.dto.SprintDto;
import com.example.projectmanagement.dto.TaskDto;
import com.example.projectmanagement.entity.Sprint;
import com.example.projectmanagement.entity.User;
import com.example.projectmanagement.repository.SprintRepository;
import com.example.projectmanagement.repository.UserRepository;
import com.example.projectmanagement.service.SprintService;
import com.example.projectmanagement.service.TaskService;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @Autowired
    private SprintRepository sprintRepository;

    // ✅ Create Sprint
    @PostMapping
    public ResponseEntity<SprintDto> createSprint(@Valid @RequestBody SprintDto sprintDto) {
        SprintDto createdSprint = sprintService.createSprint(sprintDto);
        return new ResponseEntity<>(createdSprint, HttpStatus.CREATED);
    }

    // ✅ Get Sprint by ID
    @GetMapping("/{id}")
    public ResponseEntity<SprintDto> getSprintById(@PathVariable Long id) {
        SprintDto sprint = sprintService.getSprintById(id);
        return ResponseEntity.ok(sprint);
    }

    // ✅ Get All Sprints with Pagination
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

    // ✅ Get Tasks for Sprint
    @GetMapping("/{sprintId}/tasks")
    public ResponseEntity<List<TaskDto>> getSprintTasks(@PathVariable Long sprintId) {
        List<TaskDto> tasks = taskService.getTasksBySprint(sprintId);
        return ResponseEntity.ok(tasks);
    }

    // ✅ Add Task to Sprint
    @PostMapping("/{sprintId}/tasks")
    public ResponseEntity<TaskDto> addTaskToSprint(@PathVariable Long sprintId, @Valid @RequestBody TaskDto taskDto) {
        taskDto.setSprintId(sprintId);
        TaskDto createdTask = taskService.createTask(taskDto);
        return new ResponseEntity<>(createdTask, HttpStatus.CREATED);
    }

    // ✅ Assign Task to Sprint
    @PostMapping("/{sprintId}/tasks/{taskId}")
    public ResponseEntity<TaskDto> assignTaskToSprint(@PathVariable Long sprintId, @PathVariable Long taskId) {
        TaskDto updatedTask = taskService.assignTaskToSprint(taskId, sprintId);
        return ResponseEntity.ok(updatedTask);
    }

    // ✅ Remove Task from Sprint
    @DeleteMapping("/{sprintId}/tasks/{taskId}")
    public ResponseEntity<TaskDto> removeTaskFromSprint(@PathVariable Long sprintId, @PathVariable Long taskId) {
        TaskDto updatedTask = taskService.removeTaskFromSprint(taskId);
        return ResponseEntity.ok(updatedTask);
    }

    // ✅ Get Sprints by Status (e.g., ACTIVE, COMPLETED)
    @GetMapping("/status/{status}")
    public ResponseEntity<List<SprintDto>> getSprintsByStatus(@PathVariable Sprint.SprintStatus status) {
        List<SprintDto> sprints = sprintService.getSprintsByStatus(status);
        return ResponseEntity.ok(sprints);
    }

  @GetMapping("/sprints/active")
public ResponseEntity<List<Sprint>> getActiveSprints() {
    List<Sprint> activeSprints = sprintRepository.findByStatus(Sprint.SprintStatus.ACTIVE);
    return ResponseEntity.ok(activeSprints); // even if empty, return empty list
}


    // ✅ Get Overdue Sprints
    @GetMapping("/overdue")
    public ResponseEntity<List<SprintDto>> getOverdueSprints() {
        List<SprintDto> sprints = sprintService.getOverdueSprints();
        return ResponseEntity.ok(sprints);
    }

    // ✅ Start Sprint — simulate user with ID 1
    @PutMapping("/{id}/start")
    public ResponseEntity<SprintDto> startSprint(@PathVariable Long id) {
        User testUser = userRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("Test user not found with ID: 1"));
        SprintDto startedSprint = sprintService.startSprint(id, testUser);
        return ResponseEntity.ok(startedSprint);
    }

 @PutMapping("/{id}/complete")
public ResponseEntity<Sprint> completeSprint(@PathVariable("id") Long sprintId) {
    Sprint updatedSprint = sprintService.completeSprint(sprintId);
    return ResponseEntity.ok(updatedSprint);
}



    // ✅ Update Sprint
    @PutMapping("/{id}")
    public ResponseEntity<SprintDto> updateSprint(@PathVariable Long id, @Valid @RequestBody SprintDto sprintDto) {
        SprintDto updatedSprint = sprintService.updateSprint(id, sprintDto);
        return ResponseEntity.ok(updatedSprint);
    }

    // ✅ Delete Sprint
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSprint(@PathVariable Long id) {
        sprintService.deleteSprint(id);
        return ResponseEntity.noContent().build();
    }
}
