package com.example.projectmanagement.controller;

import com.example.projectmanagement.dto.TaskDto;
import com.example.projectmanagement.dto.UserDto;
import com.example.projectmanagement.entity.User;
import com.example.projectmanagement.security.annotation.RequireProjectAccess;
import com.example.projectmanagement.security.annotation.RequireRole;
// import com.example.projectmanagement.security.CurrentUser;
import com.example.projectmanagement.service.TaskService;
import com.example.projectmanagement.service.UserService;
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
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private TaskService taskService;
    
    // Only admins can create new users
    @PostMapping
    @RequireRole({User.UserRole.ADMIN})
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody UserDto userDto) {
        UserDto createdUser = userService.createUser(userDto);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }
    
    // All authenticated users can view user profiles (for collaboration)
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        UserDto user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }
    
    // Only admins can view all users with search/pagination
    @GetMapping
    @RequireRole({User.UserRole.ADMIN})
    public ResponseEntity<Page<UserDto>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) User.UserRole role) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<UserDto> users = userService.searchUsers(name, role, pageable);
        return ResponseEntity.ok(users);
    }
    
    // Only admins and managers can search users by role
    @GetMapping("/role/{role}")
    @RequireRole({User.UserRole.ADMIN, User.UserRole.PRODUCT_OWNER, User.UserRole.SCRUM_MASTER})
    public ResponseEntity<List<UserDto>> getUsersByRole(@PathVariable User.UserRole role) {
        List<UserDto> users = userService.getUsersByRole(role);
        return ResponseEntity.ok(users);
    }
    
    // Project members and admins can see who's working on their project
    @GetMapping("/project/{projectId}")
    @RequireProjectAccess(value = {RequireProjectAccess.AccessType.MEMBER, RequireProjectAccess.AccessType.ADMIN}, projectIdParam = "projectId")
    public ResponseEntity<List<UserDto>> getUsersByProject(@PathVariable Long projectId) {
        List<UserDto> users = userService.getUsersByProject(projectId);
        return ResponseEntity.ok(users);
    }
    
    // Users can view their own tasks, admins and managers can view anyone's tasks
    @GetMapping("/{userId}/tasks")
    @RequireRole({User.UserRole.ADMIN, User.UserRole.PRODUCT_OWNER, User.UserRole.SCRUM_MASTER})
    public ResponseEntity<List<TaskDto>> getUserTasks(@PathVariable Long userId) {
        List<TaskDto> tasks = taskService.getTasksByAssignee(userId);
        return ResponseEntity.ok(tasks);
    }
    
    // Users can update their own profile, admins can update anyone's profile
    @PutMapping("/{id}")
    @RequireRole({User.UserRole.ADMIN})
    public ResponseEntity<UserDto> updateUser(@PathVariable Long id, @Valid @RequestBody UserDto userDto) {
        UserDto updatedUser = userService.updateUser(id, userDto);
        return ResponseEntity.ok(updatedUser);
    }
    
    // Only admins can delete users
    @DeleteMapping("/{id}")
    @RequireRole({User.UserRole.ADMIN})
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}