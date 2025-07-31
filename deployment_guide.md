# Deployment Guide - RBAC Project Management System

## Prerequisites

### Software Requirements
- Java 17 or higher
- Maven 3.6 or higher
- MySQL 8.0 or higher (or H2 for development)
- Git

### Environment Setup
1. **Java Installation**
   ```bash
   # Verify Java installation
   java -version
   javac -version
   ```

2. **Maven Installation**
   ```bash
   # Verify Maven installation
   mvn -version
   ```

3. **Database Setup (MySQL)**
   ```sql
   CREATE DATABASE project_management;
   CREATE USER 'pm_user'@'localhost' IDENTIFIED BY 'pm_password';
   GRANT ALL PRIVILEGES ON project_management.* TO 'pm_user'@'localhost';
   FLUSH PRIVILEGES;
   ```

## Configuration

### 1. Environment Variables
Create a `.env` file in the project root:

```env
# Application Configuration
SPRING_APPLICATION_NAME=jira-backend

# Database Configuration
DB_URL=jdbc:mysql://localhost:3306/project_management
DB_USERNAME=pm_user
DB_PASSWORD=pm_password
DB_DRIVER=com.mysql.cj.jdbc.Driver

# JPA Configuration
JPA_DDL=update
JPA_SHOW_SQL=false
JPA_DIALECT=org.hibernate.dialect.MySQL8Dialect

# Server Configuration
SERVER_PORT=8080
```

### 2. For Development (H2 Database)
```env
# Application Configuration
SPRING_APPLICATION_NAME=jira-backend

# H2 Database Configuration
DB_URL=jdbc:h2:mem:testdb
DB_USERNAME=sa
DB_PASSWORD=
DB_DRIVER=org.h2.Driver

# JPA Configuration
JPA_DDL=create-drop
JPA_SHOW_SQL=true
JPA_DIALECT=org.hibernate.dialect.H2Dialect

# Server Configuration
SERVER_PORT=8080
```

## Build and Deployment

### 1. Build the Application
```bash
# Navigate to project directory
cd /workspace

# Build the application
mvn clean package -DskipTests

# Or build with tests
mvn clean package
```

### 2. Run the Application
```bash
# Method 1: Using Maven
mvn spring-boot:run

# Method 2: Using JAR file
java -jar target/jira-backend-0.0.1-SNAPSHOT.jar

# Method 3: With specific profile
java -jar target/jira-backend-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

### 3. Docker Deployment (Optional)
Create a `Dockerfile`:
```dockerfile
FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/jira-backend-0.0.1-SNAPSHOT.jar app.jar
COPY .env .env

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
```

Build and run:
```bash
docker build -t rbac-project-management .
docker run -p 8080:8080 rbac-project-management
```

## Initial Data Setup

### 1. Create Initial Admin User
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "name": "System Admin",
    "email": "admin@example.com",
    "role": "ADMIN"
  }'
```

### 2. Create Sample Project
```bash
# First, login to get JWT token
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "admin@example.com"}' \
  | jq -r .token

# Use the token to create a project
curl -X POST http://localhost:8080/api/projects \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "name": "Sample Project",
    "projectKey": "SAMPLE",
    "description": "A sample project for testing RBAC",
    "ownerId": 1
  }'
```

### 3. Create Sample Users
```bash
# Create Product Owner
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "name": "Product Owner",
    "email": "po@example.com",
    "role": "PRODUCT_OWNER"
  }'

# Create Developer
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "name": "Developer",
    "email": "dev@example.com",
    "role": "DEVELOPER"
  }'
```

## Testing the RBAC System

### 1. Test Authentication
```bash
# Login as admin
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "admin@example.com"}'

# Login as developer
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "dev@example.com"}'
```

### 2. Test Role-Based Access
```bash
# Admin accessing users (should work)
curl -X GET http://localhost:8080/api/users \
  -H "Authorization: Bearer ADMIN_JWT_TOKEN"

# Developer accessing users (should fail with 403)
curl -X GET http://localhost:8080/api/users \
  -H "Authorization: Bearer DEVELOPER_JWT_TOKEN"
```

### 3. Test Project Access
```bash
# Project member accessing project (should work)
curl -X GET http://localhost:8080/api/projects/1 \
  -H "Authorization: Bearer MEMBER_JWT_TOKEN"

# Non-member accessing project (should fail with 403)
curl -X GET http://localhost:8080/api/projects/1 \
  -H "Authorization: Bearer NON_MEMBER_JWT_TOKEN"
```

## Health Checks and Monitoring

### 1. Application Health
```bash
# Basic health check
curl http://localhost:8080/actuator/health

# Detailed health information
curl http://localhost:8080/actuator/info
```

### 2. Database Connection
```bash
# Test database connectivity
curl http://localhost:8080/actuator/health/db
```

### 3. Security Test
```bash
# Test authentication endpoint
curl http://localhost:8080/api/auth/test
```

## Troubleshooting

### Common Issues

1. **Application Won't Start**
   - Check Java version: `java -version`
   - Verify environment variables in `.env` file
   - Check database connectivity
   - Review application logs

2. **Database Connection Failed**
   - Verify database is running
   - Check database credentials in `.env`
   - Ensure database exists and user has permissions
   - Test connection manually

3. **Authentication Issues**
   - Verify JWT secret configuration
   - Check user exists in database
   - Confirm token format and expiration

4. **Access Denied Errors**
   - Check user role assignment
   - Verify project membership
   - Review RBAC annotations on endpoints
   - Check audit logs

### Logging Configuration
Add to `application.properties`:
```properties
# Enable security debug logging
logging.level.com.example.projectmanagement.security=DEBUG
logging.level.org.springframework.security=DEBUG

# Database query logging
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
```

## Performance Optimization

### 1. Database Optimization
- Add appropriate indexes on frequently queried columns
- Configure connection pooling
- Enable query caching for static data

### 2. Security Optimization
- Implement JWT token caching
- Use Redis for session storage in multi-instance deployments
- Enable GZIP compression for API responses

### 3. Monitoring
- Set up application performance monitoring (APM)
- Configure log aggregation (ELK stack)
- Implement custom metrics for RBAC operations

## Production Deployment

### 1. Environment-Specific Configuration
- Use Spring profiles for different environments
- Externalize all configuration
- Use secrets management for sensitive data

### 2. Security Hardening
- Enable HTTPS with proper SSL certificates
- Configure firewall rules
- Set up intrusion detection
- Regular security updates

### 3. Backup and Recovery
- Regular database backups
- Application state backup
- Disaster recovery procedures
- Data retention policies

## Support and Maintenance

### Regular Tasks
1. Monitor application logs for security events
2. Review and update user roles and permissions
3. Database maintenance and optimization
4. Security patches and updates
5. Performance monitoring and tuning

### Scaling Considerations
- Horizontal scaling with load balancers
- Database read replicas
- Caching strategies
- Microservices migration path

## Contact Information

For deployment issues or questions:
- Development Team: dev-team@example.com
- System Administration: sysadmin@example.com
- Security Team: security@example.com

---

**Note**: This deployment guide covers the basic setup. For production environments, additional security measures and monitoring should be implemented based on your organization's requirements.