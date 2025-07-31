package com.example.projectmanagement.security.aspect;

import com.example.projectmanagement.entity.Epic;
import com.example.projectmanagement.entity.Project;
import com.example.projectmanagement.entity.Sprint;
import com.example.projectmanagement.entity.Story;
import com.example.projectmanagement.entity.Task;
import com.example.projectmanagement.entity.User;
import com.example.projectmanagement.exception.AccessDeniedException;
import com.example.projectmanagement.repository.EpicRepository;
import com.example.projectmanagement.repository.ProjectRepository;
import com.example.projectmanagement.repository.SprintRepository;
import com.example.projectmanagement.repository.StoryRepository;
import com.example.projectmanagement.repository.TaskRepository;
import com.example.projectmanagement.repository.UserRepository;
import com.example.projectmanagement.security.annotation.RequireProjectAccess;
import com.example.projectmanagement.security.annotation.RequireProjectEntityAccess;
import com.example.projectmanagement.security.annotation.RequireRole;
import com.example.projectmanagement.security.annotation.RequireTaskAccess;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Optional;

@Aspect
@Component
public class AuthorizationAspect {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private EpicRepository epicRepository;

    @Autowired
    private SprintRepository sprintRepository;

    @Autowired
    private StoryRepository storyRepository;

    @Before("@annotation(requireRole)")
    public void checkRoleAccess(JoinPoint joinPoint, RequireRole requireRole) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new AccessDeniedException("Authentication required");
        }

        boolean hasRequiredRole = Arrays.stream(requireRole.value())
                .anyMatch(role -> role == currentUser.getRole());

        if (!hasRequiredRole) {
            throw new AccessDeniedException(requireRole.message());
        }
    }

    @Before("@annotation(requireProjectAccess)")
    public void checkProjectAccess(JoinPoint joinPoint, RequireProjectAccess requireProjectAccess) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new AccessDeniedException("Authentication required");
        }

        // Get the project ID from method parameters
        Long projectId = extractProjectId(joinPoint, requireProjectAccess.projectIdParam());
        if (projectId == null) {
            throw new AccessDeniedException("Project ID not found in request");
        }

        // Get the project
        Optional<Project> projectOpt = projectRepository.findById(projectId);
        if (projectOpt.isEmpty()) {
            throw new AccessDeniedException("Project not found");
        }

        Project project = projectOpt.get();

        // Check access based on access types
        boolean hasAccess = Arrays.stream(requireProjectAccess.value())
                .anyMatch(accessType -> checkAccessType(currentUser, project, accessType));

        if (!hasAccess) {
            throw new AccessDeniedException(requireProjectAccess.message());
        }
    }

    @Before("@annotation(requireTaskAccess)")
    public void checkTaskAccess(JoinPoint joinPoint, RequireTaskAccess requireTaskAccess) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new AccessDeniedException("Authentication required");
        }

        // Get the task ID from method parameters
        Long taskId = extractTaskId(joinPoint, requireTaskAccess.taskIdParam());
        if (taskId == null) {
            throw new AccessDeniedException("Task ID not found in request");
        }

        // Get the task
        Optional<Task> taskOpt = taskRepository.findById(taskId);
        if (taskOpt.isEmpty()) {
            throw new AccessDeniedException("Task not found");
        }

        Task task = taskOpt.get();

        // Check access based on access types
        boolean hasAccess = Arrays.stream(requireTaskAccess.value())
                .anyMatch(accessType -> checkTaskAccessType(currentUser, task, accessType));

        if (!hasAccess) {
            throw new AccessDeniedException(requireTaskAccess.message());
        }
    }

    private boolean checkTaskAccessType(User user, Task task, RequireTaskAccess.AccessType accessType) {
        switch (accessType) {
            case ASSIGNEE:
                return task.getAssignee() != null && task.getAssignee().getId().equals(user.getId());
            case REPORTER:
                return task.getReporter().getId().equals(user.getId());
            case ASSIGNEE_OR_REPORTER:
                return (task.getAssignee() != null && task.getAssignee().getId().equals(user.getId())) ||
                       task.getReporter().getId().equals(user.getId());
            case PROJECT_MEMBER:
                Project project = task.getProject();
                return project.getOwner().getId().equals(user.getId()) ||
                       (project.getMembers() != null &&
                        project.getMembers().stream().anyMatch(member -> member.getId().equals(user.getId())));
            case PROJECT_OWNER:
                return task.getProject().getOwner().getId().equals(user.getId());
            case ADMIN:
                return user.getRole() == User.UserRole.ADMIN;
            default:
                return false;
        }
    }

    private Long extractTaskId(JoinPoint joinPoint, String paramName) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Parameter[] parameters = method.getParameters();
        Object[] args = joinPoint.getArgs();

        for (int i = 0; i < parameters.length; i++) {
            Parameter param = parameters[i];
            
            // Check if parameter is annotated with @PathVariable and matches our param name
            PathVariable pathVariable = param.getAnnotation(PathVariable.class);
            if (pathVariable != null) {
                String pathVarName = pathVariable.value().isEmpty() ? param.getName() : pathVariable.value();
                if (pathVarName.equals(paramName) && args[i] instanceof Long) {
                    return (Long) args[i];
                }
            }
            
            // Also check parameter name directly
            if (param.getName().equals(paramName) && args[i] instanceof Long) {
                return (Long) args[i];
            }
        }
        return null;
    }

    @Before("@annotation(requireProjectEntityAccess)")
    public void checkProjectEntityAccess(JoinPoint joinPoint, RequireProjectEntityAccess requireProjectEntityAccess) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new AccessDeniedException("Authentication required");
        }

        // Get the entity ID from method parameters
        Long entityId = extractProjectEntityId(joinPoint, requireProjectEntityAccess.entityIdParam());
        if (entityId == null) {
            throw new AccessDeniedException("Entity ID not found in request");
        }

        // Get the project based on entity type
        Project project = getProjectFromEntity(entityId, requireProjectEntityAccess.entityType());
        if (project == null) {
            throw new AccessDeniedException("Entity or related project not found");
        }

        // Check access based on access types
        boolean hasAccess = Arrays.stream(requireProjectEntityAccess.value())
                .anyMatch(accessType -> checkProjectEntityAccessType(currentUser, project, accessType));

        if (!hasAccess) {
            throw new AccessDeniedException(requireProjectEntityAccess.message());
        }
    }

    private Project getProjectFromEntity(Long entityId, RequireProjectEntityAccess.EntityType entityType) {
        switch (entityType) {
            case EPIC:
                Optional<Epic> epicOpt = epicRepository.findById(entityId);
                return epicOpt.map(Epic::getProject).orElse(null);
            case SPRINT:
                Optional<Sprint> sprintOpt = sprintRepository.findById(entityId);
                return sprintOpt.map(Sprint::getProject).orElse(null);
            case STORY:
                Optional<Story> storyOpt = storyRepository.findById(entityId);
                return storyOpt.map(story -> story.getEpic().getProject()).orElse(null);
            default:
                return null;
        }
    }

    private boolean checkProjectEntityAccessType(User user, Project project, RequireProjectEntityAccess.AccessType accessType) {
        switch (accessType) {
            case OWNER:
                return project.getOwner().getId().equals(user.getId());
            case MEMBER:
                return project.getOwner().getId().equals(user.getId()) ||
                       (project.getMembers() != null &&
                        project.getMembers().stream().anyMatch(member -> member.getId().equals(user.getId())));
            case ADMIN:
                return user.getRole() == User.UserRole.ADMIN;
            default:
                return false;
        }
    }

    private Long extractProjectEntityId(JoinPoint joinPoint, String paramName) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Parameter[] parameters = method.getParameters();
        Object[] args = joinPoint.getArgs();

        for (int i = 0; i < parameters.length; i++) {
            Parameter param = parameters[i];
            
            // Check if parameter is annotated with @PathVariable and matches our param name
            PathVariable pathVariable = param.getAnnotation(PathVariable.class);
            if (pathVariable != null) {
                String pathVarName = pathVariable.value().isEmpty() ? param.getName() : pathVariable.value();
                if (pathVarName.equals(paramName) && args[i] instanceof Long) {
                    return (Long) args[i];
                }
            }
            
            // Also check parameter name directly
            if (param.getName().equals(paramName) && args[i] instanceof Long) {
                return (Long) args[i];
            }
        }
        return null;
    }

    private boolean checkAccessType(User user, Project project, RequireProjectAccess.AccessType accessType) {
        switch (accessType) {
            case OWNER:
                return project.getOwner().getId().equals(user.getId());
            case MEMBER:
                return project.getOwner().getId().equals(user.getId()) || 
                       (project.getMembers() != null && 
                        project.getMembers().stream().anyMatch(member -> member.getId().equals(user.getId())));
            case ADMIN:
                return user.getRole() == User.UserRole.ADMIN;
            default:
                return false;
        }
    }

    private Long extractProjectId(JoinPoint joinPoint, String paramName) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Parameter[] parameters = method.getParameters();
        Object[] args = joinPoint.getArgs();

        for (int i = 0; i < parameters.length; i++) {
            Parameter param = parameters[i];
            
            // Check if parameter is annotated with @PathVariable and matches our param name
            PathVariable pathVariable = param.getAnnotation(PathVariable.class);
            if (pathVariable != null) {
                String pathVarName = pathVariable.value().isEmpty() ? param.getName() : pathVariable.value();
                if (pathVarName.equals(paramName) && args[i] instanceof Long) {
                    return (Long) args[i];
                }
            }
            
            // Also check parameter name directly
            if (param.getName().equals(paramName) && args[i] instanceof Long) {
                return (Long) args[i];
            }
        }
        return null;
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        String email = authentication.getName();
        return userRepository.findByEmail(email).orElse(null);
    }
}