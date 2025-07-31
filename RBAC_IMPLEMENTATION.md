# Role-Based Access Control (RBAC) Implementation

## 🎯 Overview
This project has been enhanced with a comprehensive Role-Based Access Control (RBAC) system using Spring Security. The implementation provides secure authentication and authorization for a Project Management System.

## 🔐 Supported Roles
- **ADMIN**: Full system access, user management
- **DEVELOPER**: Development-related features  
- **PRODUCT_OWNER**: Product management features
- **SCRUM_MASTER**: Project and team management

## 🏗️ Architecture Changes

### 1. Enhanced User Entity (`entity/User.java`)
- Added `username` field (unique, required)
- Added `password` field (BCrypt encrypted)
- Replaced single role with `Set<Role> roles` using `@ElementCollection`
- Support for multiple roles per user

### 2. New Role Enum (`entity/Role.java`)
```java
public enum Role {
    ADMIN,
    DEVELOPER, 
    PRODUCT_OWNER,
    SCRUM_MASTER
}
```

### 3. Updated UserRepository (`repository/UserRepository.java`)
- Added `findByUsername(String username)`
- Added `existsByUsername(String username)` 
- Updated role queries to work with new Role enum

### 4. Custom User Details Service (`service/CustomUserDetailsService.java`)
- Implements Spring Security `UserDetailsService`
- Loads users by username with their roles
- Maps roles to Spring Security authorities

### 5. Security Configuration (`config/SecurityConfig.java`)
- **Authentication**: Form-based login with BCrypt password encoding
- **Authorization**: URL-based and method-level security
- **CSRF**: Disabled for API development
- **Method Security**: Enabled with `@EnableMethodSecurity`

## 🛡️ Security Features

### URL-Based Security
```java
/admin/**         → ADMIN role required
/developer/**     → DEVELOPER role required  
/product-owner/** → PRODUCT_OWNER role required
/scrum-master/**  → SCRUM_MASTER role required
/public/**        → Public access
```

### Method-Level Security Examples
```java
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<UserDto> createUser(@RequestBody UserDto userDto)

@PreAuthorize("hasRole('ADMIN') or hasRole('SCRUM_MASTER')")  
public ResponseEntity<Page<UserDto>> getAllUsers(...)

@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<List<UserDto>> getUsersByRole(Role role)
```

## 🚀 Getting Started

### 1. Database Setup
The enhanced User entity includes new fields. Run the application and Spring Boot will auto-update the database schema.

### 2. Create Test Users
Use the `/api/users` endpoint to create users with roles:

```json
{
  "name": "Admin User",
  "email": "admin@company.com", 
  "username": "admin",
  "password": "password123",
  "roles": ["ADMIN"]
}
```

### 3. Authentication
- **Login URL**: `/login`
- **Default Success**: `/dashboard`
- **Logout URL**: `/logout`

### 4. Testing RBAC
1. Create users with different roles
2. Login with different users
3. Try accessing role-restricted endpoints
4. Verify proper access control

## 📋 API Endpoints

### Public Endpoints
- `POST /login` - User authentication
- `GET /public/**` - Public resources

### Admin Only
- `POST /api/users` - Create new users
- `GET /api/users/role/{role}` - Get users by role
- `GET /admin/**` - Admin dashboard

### ADMIN or SCRUM_MASTER
- `GET /api/users` - List all users (paginated)

### Role-Specific Endpoints  
- `/developer/**` - Developer features
- `/product-owner/**` - Product owner features
- `/scrum-master/**` - Scrum master features

## 🔧 Configuration

### Password Encoding
- Uses `BCryptPasswordEncoder` 
- Automatically encodes passwords on user creation/update

### Session Management
- Form-based authentication
- Session invalidation on logout
- JSESSIONID cookie management

## 📦 Dependencies Added
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

## 🎯 Key Features
✅ Multi-role support per user  
✅ BCrypt password encryption  
✅ URL-based access control  
✅ Method-level security  
✅ Form-based authentication  
✅ Secure session management  
✅ Role hierarchy support  

## 🔍 Testing Security

### 1. Role Verification
```bash
# Test admin endpoint (should work for ADMIN role)
curl -X GET http://localhost:8080/api/users/role/DEVELOPER \
  -H "Cookie: JSESSIONID=your-session-id"

# Test developer endpoint (should work for DEVELOPER role)  
curl -X GET http://localhost:8080/developer/dashboard \
  -H "Cookie: JSESSIONID=your-session-id"
```

### 2. Method Security Testing
The application will automatically enforce `@PreAuthorize` annotations. Unauthorized access attempts will return `403 Forbidden`.

## 🛠️ Development Notes

### Creating New Protected Endpoints
```java
@RestController
@RequestMapping("/api/projects")
public class ProjectController {
    
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('SCRUM_MASTER')")
    public ResponseEntity<List<Project>> getProjects() {
        // Implementation
    }
    
    @PostMapping  
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Project> createProject(@RequestBody Project project) {
        // Implementation
    }
}
```

### Role Checking in Service Layer
```java
@Service
public class ProjectService {
    
    @PreAuthorize("hasRole('DEVELOPER') or hasRole('SCRUM_MASTER')")
    public List<Task> getAssignedTasks(String username) {
        // Implementation
    }
}
```

## 🔒 Security Best Practices Implemented
- Passwords are never stored in plain text
- Role-based authorization at multiple layers
- Session management with secure cookies
- Method-level security for fine-grained control
- Comprehensive error handling for unauthorized access

## 📚 Additional Resources
- [Spring Security Documentation](https://spring.io/projects/spring-security)
- [Method Security Guide](https://docs.spring.io/spring-security/reference/servlet/authorization/method-security.html)
- [BCrypt Password Encoder](https://docs.spring.io/spring-security/site/docs/current/api/org/springframework/security/crypto/bcrypt/BCryptPasswordEncoder.html)