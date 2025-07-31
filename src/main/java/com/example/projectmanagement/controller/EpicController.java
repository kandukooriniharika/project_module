package com.example.projectmanagement.controller;

import com.example.projectmanagement.dto.EpicDto;
import com.example.projectmanagement.entity.User;
import com.example.projectmanagement.security.annotation.RequireProjectAccess;
import com.example.projectmanagement.security.annotation.RequireProjectEntityAccess;
import com.example.projectmanagement.security.annotation.RequireRole;
import com.example.projectmanagement.service.EpicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/epics")
@CrossOrigin(origins = "*")
public class EpicController {

    @Autowired
    private EpicService epicService;

    // Only Product Owners, Scrum Masters, and Admins can create epics
    @PostMapping
    @RequireRole({User.UserRole.PRODUCT_OWNER, User.UserRole.SCRUM_MASTER, User.UserRole.ADMIN})
    public ResponseEntity<EpicDto> createEpic(@RequestBody EpicDto epicDto) {
        EpicDto createdEpic = epicService.createEpic(epicDto);
        return ResponseEntity.ok(createdEpic);
    }

    // Only admins can view all epics (filtered by accessible projects in service layer)
    @GetMapping
    @RequireRole({User.UserRole.ADMIN})
    public ResponseEntity<List<EpicDto>> getAllEpics() {
        return ResponseEntity.ok(epicService.getAllEpics());
    }

    // Project members and admins can view epic details
    @GetMapping("/{id}")
    @RequireProjectEntityAccess(entityType = RequireProjectEntityAccess.EntityType.EPIC, value = {RequireProjectEntityAccess.AccessType.MEMBER, RequireProjectEntityAccess.AccessType.ADMIN})
    public ResponseEntity<EpicDto> getEpicById(@PathVariable Long id) {
        EpicDto epicDto = epicService.getEpicById(id);
        if (epicDto != null) {
            return ResponseEntity.ok(epicDto);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Project owners and admins can update epics
    @PutMapping("/{id}")
    @RequireProjectEntityAccess(entityType = RequireProjectEntityAccess.EntityType.EPIC, value = {RequireProjectEntityAccess.AccessType.OWNER, RequireProjectEntityAccess.AccessType.ADMIN})
    public ResponseEntity<EpicDto> updateEpic(@PathVariable Long id, @RequestBody EpicDto epicDto) {
        EpicDto updatedEpic = epicService.updateEpic(id, epicDto);
        if (updatedEpic != null) {
            return ResponseEntity.ok(updatedEpic);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Only project owners and admins can delete epics
    @DeleteMapping("/{id}")
    @RequireProjectEntityAccess(entityType = RequireProjectEntityAccess.EntityType.EPIC, value = {RequireProjectEntityAccess.AccessType.OWNER, RequireProjectEntityAccess.AccessType.ADMIN})
    public ResponseEntity<Void> deleteEpic(@PathVariable Long id) {
        boolean deleted = epicService.deleteEpic(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Project members and admins can view epics by project
    @GetMapping("/project/{projectId}")
    @RequireProjectAccess(value = {RequireProjectAccess.AccessType.MEMBER, RequireProjectAccess.AccessType.ADMIN}, projectIdParam = "projectId")
    public ResponseEntity<List<EpicDto>> getEpicsByProjectId(@PathVariable Long projectId) {
        return ResponseEntity.ok(epicService.getEpicsByProjectId(projectId));
    }
}
