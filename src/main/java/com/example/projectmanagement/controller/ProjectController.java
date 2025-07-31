package com.example.projectmanagement.controller;

import com.example.projectmanagement.dto.EpicDto;
import com.example.projectmanagement.dto.ProjectDto;
import com.example.projectmanagement.dto.SprintDto;
import com.example.projectmanagement.dto.TaskDto;
import com.example.projectmanagement.entity.Project;
import com.example.projectmanagement.entity.User;
import com.example.projectmanagement.security.annotation.RequireProjectAccess;
import com.example.projectmanagement.security.annotation.RequireRole;
import com.example.projectmanagement.service.EpicService;
import com.example.projectmanagement.service.ProjectService;
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
@RequestMapping("/api/projects")
@CrossOrigin(origins = "*")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private EpicService epicService;

    @Autowired
    private SprintService sprintService;

    @Autowired
    private TaskService taskService;

    // ✅ CREATE a new project - Only Product Owners, Scrum Masters, and Admins can create projects
    @PostMapping
    @RequireRole({User.UserRole.PRODUCT_OWNER, User.UserRole.SCRUM_MASTER, User.UserRole.ADMIN})
    public ResponseEntity<ProjectDto> createProject(@Valid @RequestBody ProjectDto projectDto) {
        ProjectDto createdProject = projectService.createProject(projectDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProject);
    }

    // ✅ GET project by ID - Project members and admins can view
    @GetMapping("/{id}")
    @RequireProjectAccess(value = {RequireProjectAccess.AccessType.MEMBER, RequireProjectAccess.AccessType.ADMIN})
    public ResponseEntity<ProjectDto> getProjectById(@PathVariable Long id) {
        ProjectDto project = projectService.getProjectById(id);
        return ResponseEntity.ok(project);
    }

    // ✅ UPDATE project - Only project owners and admins can update
    @PutMapping("/{id}")
    @RequireProjectAccess(value = {RequireProjectAccess.AccessType.OWNER, RequireProjectAccess.AccessType.ADMIN})
    public ResponseEntity<ProjectDto> updateProject(@PathVariable Long id,
                                                    @RequestBody ProjectDto updatedProjectDto) {
        ProjectDto updated = projectService.updateProject(id, updatedProjectDto);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/api/projects/{projectId}/unarchive")
    @RequireProjectAccess(value = {RequireProjectAccess.AccessType.OWNER, RequireProjectAccess.AccessType.ADMIN}, projectIdParam = "projectId")
    public ResponseEntity<ProjectDto> unarchiveProject(@PathVariable Long projectId) {
        ProjectDto dto = projectService.unarchiveProject(projectId);
        return ResponseEntity.ok(dto);
    }

    // ✅ DELETE project - Only project owners and admins can delete
    @DeleteMapping("/{id}")
    @RequireProjectAccess(value = {RequireProjectAccess.AccessType.OWNER, RequireProjectAccess.AccessType.ADMIN})
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }

    // ✅ GET all projects with pagination, filters - All authenticated users can search projects
    @GetMapping
    public ResponseEntity<Page<ProjectDto>> getAllProjects(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Project.ProjectStatus status) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<ProjectDto> projects = projectService.searchProjects(name, status, pageable);
        return ResponseEntity.ok(projects);
    }

    // ✅ GET Epics by project ID - Project members and admins can view
    @GetMapping("/{id}/epics")
    @RequireProjectAccess(value = {RequireProjectAccess.AccessType.MEMBER, RequireProjectAccess.AccessType.ADMIN})
    public ResponseEntity<List<EpicDto>> getProjectEpics(@PathVariable Long id) {
        List<EpicDto> epics = epicService.getEpicsByProjectId(id);
        return ResponseEntity.ok(epics);
    }

    // ✅ GET Sprints by project ID - Project members and admins can view
    @GetMapping("/{id}/sprints")
    @RequireProjectAccess(value = {RequireProjectAccess.AccessType.MEMBER, RequireProjectAccess.AccessType.ADMIN})
    public ResponseEntity<List<SprintDto>> getProjectSprints(@PathVariable Long id) {
        List<SprintDto> sprints = sprintService.getSprintsByProject(id);
        return ResponseEntity.ok(sprints);
    }

    // ✅ GET Tasks by project ID - Project members and admins can view
    @GetMapping("/{id}/tasks")
    @RequireProjectAccess(value = {RequireProjectAccess.AccessType.MEMBER, RequireProjectAccess.AccessType.ADMIN})
    public ResponseEntity<List<TaskDto>> getProjectTasks(@PathVariable Long id) {
        List<TaskDto> tasks = taskService.getTasksByProject(id);
        return ResponseEntity.ok(tasks);
    }

    // ✅ GET Projects by Owner - Admins and the owner themselves can access
    @GetMapping("/owner/{ownerId}")
    @RequireRole({User.UserRole.ADMIN})
    public ResponseEntity<List<ProjectDto>> getProjectsByOwner(@PathVariable Long ownerId) {
        List<ProjectDto> projects = projectService.getProjectsByOwner(ownerId);
        return ResponseEntity.ok(projects);
    }

    // ✅ GET Projects by Member - Admins and the member themselves can access
    @GetMapping("/member/{userId}")
    @RequireRole({User.UserRole.ADMIN})
    public ResponseEntity<List<ProjectDto>> getProjectsByMember(@PathVariable Long userId) {
        List<ProjectDto> projects = projectService.getProjectsByMember(userId);
        return ResponseEntity.ok(projects);
    }

    // ✅ Add member to a project - Only project owners and admins can add members
    @PostMapping("/{projectId}/members/{userId}")
    @RequireProjectAccess(value = {RequireProjectAccess.AccessType.OWNER, RequireProjectAccess.AccessType.ADMIN}, projectIdParam = "projectId")
    public ResponseEntity<ProjectDto> addMemberToProject(@PathVariable Long projectId,
            @PathVariable Long userId) {
        ProjectDto updatedProject = projectService.addMemberToProject(projectId, userId);
        return ResponseEntity.ok(updatedProject);
    }

    // ✅ Remove member from project - Only project owners and admins can remove members
    @DeleteMapping("/{projectId}/members/{userId}")
    @RequireProjectAccess(value = {RequireProjectAccess.AccessType.OWNER, RequireProjectAccess.AccessType.ADMIN}, projectIdParam = "projectId")
    public ResponseEntity<ProjectDto> removeMemberFromProject(@PathVariable Long projectId,
            @PathVariable Long userId) {
        ProjectDto updatedProject = projectService.removeMemberFromProject(projectId, userId);
        return ResponseEntity.ok(updatedProject);
    }
}
