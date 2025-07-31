# Role-Based Access Control (RBAC) Implementation

## Overview

This project implements a comprehensive Role-Based Access Control (RBAC) system for a Jira-like project management application. The RBAC system ensures that users can only access resources and perform actions that are appropriate for their role and project membership.

## User Roles

The system supports the following user roles:

- **ADMIN**: Full system access, can manage all projects and users
- **PRODUCT_OWNER**: Can create and manage projects, epics, and stories
- **SCRUM_MASTER**: Can manage sprints, facilitate team activities
- **DEVELOPER**: Can work on tasks, update stories and tasks assigned to them
- **TESTER**: Can work on testing tasks, update task status

## Access Control Levels

### 1. Role-Based Access Control
Uses `@RequireRole` annotation to restrict access based on user roles.

**Example:**
```java
@PostMapping
@RequireRole({User.UserRole.ADMIN})
public ResponseEntity<UserDto> createUser(@RequestBody UserDto userDto) {
    // Only admins can create users
}
```

### 2. Project-Level Access Control
Uses `@RequireProjectAccess` annotation to control access based on project ownership/membership.

**Access Types:**
- `OWNER`: Only project owner can access
- `MEMBER`: Project members (including owner) can access
- `ADMIN`: Only system admins can access

**Example:**
```java
@GetMapping("/{id}")
@RequireProjectAccess(value = {RequireProjectAccess.AccessType.MEMBER, RequireProjectAccess.AccessType.ADMIN})
public ResponseEntity<ProjectDto> getProjectById(@PathVariable Long id) {
    // Only project members and admins can view project details
}
```

### 3. Task-Level Access Control
Uses `@RequireTaskAccess` annotation for fine-grained task access control.

**Access Types:**
- `ASSIGNEE`: Only task assignee can access
- `REPORTER`: Only task reporter can access
- `ASSIGNEE_OR_REPORTER`: Either assignee or reporter can access
- `PROJECT_MEMBER`: Any project member can access
- `PROJECT_OWNER`: Only project owner can access
- `ADMIN`: Only system admins can access

**Example:**
```java
@PutMapping("/{id}")
@RequireTaskAccess({RequireTaskAccess.AccessType.ASSIGNEE_OR_REPORTER, RequireTaskAccess.AccessType.PROJECT_OWNER, RequireTaskAccess.AccessType.ADMIN})
public ResponseEntity<TaskDto> updateTask(@PathVariable Long id, @RequestBody TaskDto taskDto) {
    // Task assignee, reporter, project owner, or admin can update
}
```

### 4. Project Entity Access Control
Uses `@RequireProjectEntityAccess` annotation for entities related to projects (Epic, Sprint, Story).

**Entity Types:**
- `EPIC`: Access control for epics
- `SPRINT`: Access control for sprints  
- `STORY`: Access control for stories

**Example:**
```java
@GetMapping("/{id}")
@RequireProjectEntityAccess(entityType = RequireProjectEntityAccess.EntityType.EPIC, value = {RequireProjectEntityAccess.AccessType.MEMBER})
public ResponseEntity<EpicDto> getEpicById(@PathVariable Long id) {
    // Only project members can view epic details
}
```

## Access Control Matrix

| Operation | ADMIN | PRODUCT_OWNER | SCRUM_MASTER | DEVELOPER | TESTER |
|-----------|-------|---------------|--------------|-----------|--------|
| **Users** |
| Create User | ✅ | ❌ | ❌ | ❌ | ❌ |
| View All Users | ✅ | ❌ | ❌ | ❌ | ❌ |
| Update User | ✅ | ❌ | ❌ | ❌ | ❌ |
| Delete User | ✅ | ❌ | ❌ | ❌ | ❌ |
| **Projects** |
| Create Project | ✅ | ✅ | ✅ | ❌ | ❌ |
| View Project (Member) | ✅ | ✅ | ✅ | ✅ | ✅ |
| Update Project (Owner) | ✅ | ✅* | ✅* | ❌ | ❌ |
| Delete Project (Owner) | ✅ | ✅* | ✅* | ❌ | ❌ |
| Add/Remove Members | ✅ | ✅* | ✅* | ❌ | ❌ |
| **Epics** |
| Create Epic | ✅ | ✅ | ✅ | ❌ | ❌ |
| View Epic (Project Member) | ✅ | ✅ | ✅ | ✅ | ✅ |
| Update Epic (Project Owner) | ✅ | ✅* | ✅* | ❌ | ❌ |
| Delete Epic (Project Owner) | ✅ | ✅* | ✅* | ❌ | ❌ |
| **Sprints** |
| Create Sprint | ✅ | ✅ | ✅ | ❌ | ❌ |
| View Sprint (Project Member) | ✅ | ✅ | ✅ | ✅ | ✅ |
| Update Sprint (Project Owner) | ✅ | ✅* | ✅* | ❌ | ❌ |
| Delete Sprint (Project Owner) | ✅ | ✅* | ✅* | ❌ | ❌ |
| Manage Sprint Tasks | ✅ | ✅ | ✅ | ✅ | ✅ |
| **Stories** |
| Create Story | ✅ | ✅ | ✅ | ✅ | ❌ |
| View Story (Project Member) | ✅ | ✅ | ✅ | ✅ | ✅ |
| Update Story (Project Member) | ✅ | ✅ | ✅ | ✅ | ✅ |
| Delete Story (Project Owner) | ✅ | ✅* | ✅* | ❌ | ❌ |
| **Tasks** |
| Create Task | ✅ | ✅ | ✅ | ✅ | ✅ |
| View Task (Project Member) | ✅ | ✅ | ✅ | ✅ | ✅ |
| Update Task (Assignee/Reporter) | ✅ | ✅* | ✅* | ✅* | ✅* |
| Delete Task (Project Owner) | ✅ | ✅* | ✅* | ❌ | ❌ |

*\* Only if user is project owner/member*

## Security Implementation

### 1. Spring Security Configuration
- JWT-based authentication
- Stateless session management
- CSRF disabled for API endpoints
- Method-level security enabled

### 2. Custom Annotations
- `@RequireRole`: Role-based access control
- `@RequireProjectAccess`: Project-level access control  
- `@RequireTaskAccess`: Task-level access control
- `@RequireProjectEntityAccess`: Entity-level access control

### 3. AOP Security Aspects
- `AuthorizationAspect`: Intercepts annotated methods and enforces access control
- Automatic parameter extraction for entity IDs
- Comprehensive error handling with meaningful messages

### 4. Exception Handling
- Custom `AccessDeniedException` for authorization failures
- Global exception handler for consistent error responses
- HTTP 403 Forbidden for access denied scenarios

## Testing the RBAC System

### 1. Authentication
Use the test authentication endpoint:
```bash
POST /api/auth/login
{
    "email": "user@example.com"
}
```

### 2. Testing Access Control
1. Create users with different roles
2. Create projects and assign members
3. Try accessing resources with different user tokens
4. Verify that access is granted/denied based on roles and permissions

### 3. Test Scenarios
- Admin accessing any resource ✅
- Project owner accessing their project ✅
- Project member accessing project resources ✅
- Non-member trying to access private project ❌
- Developer trying to delete a project ❌
- Non-assignee trying to update task ❌

## Configuration

### Environment Variables
```properties
# JWT Configuration
JWT_SECRET=your-secret-key
JWT_EXPIRATION=86400

# Security Configuration  
SPRING_SECURITY_ENABLED=true
```

### Database Permissions
Ensure proper database schema supports:
- User roles enumeration
- Project ownership relationships
- Project membership many-to-many relationships
- Task assignee/reporter relationships

## Best Practices

1. **Principle of Least Privilege**: Users get minimum permissions needed
2. **Defense in Depth**: Multiple layers of security (authentication + authorization)
3. **Explicit Access Control**: Access must be explicitly granted, not assumed
4. **Audit Trail**: All access control decisions are logged
5. **Consistent Error Handling**: Same error response for unauthorized access

## Future Enhancements

1. **Dynamic Roles**: Runtime role assignment and permission modification
2. **Resource-Level Permissions**: Fine-grained permissions per resource
3. **Time-Based Access**: Temporary access grants with expiration
4. **API Rate Limiting**: Prevent abuse of authenticated endpoints
5. **Advanced Auditing**: Comprehensive access logs and monitoring

## Troubleshooting

### Common Issues

1. **403 Forbidden Errors**
   - Check user role and project membership
   - Verify JWT token is valid and contains correct claims
   - Ensure annotation parameters match endpoint parameters

2. **Authentication Failed**
   - Verify JWT token format and signature
   - Check token expiration
   - Confirm user exists in database

3. **Access Denied for Valid Users**
   - Check project membership tables
   - Verify entity relationships (Epic->Project, Story->Epic->Project)
   - Review annotation configuration

### Debug Mode
Enable debug logging for security:
```properties
logging.level.com.example.projectmanagement.security=DEBUG
logging.level.org.springframework.security=DEBUG
```

## Conclusion

This RBAC implementation provides a robust, scalable security foundation for the project management system. It ensures proper access control while maintaining flexibility for future enhancements and integrations.