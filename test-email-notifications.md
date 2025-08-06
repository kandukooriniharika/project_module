# Email Notification Testing Guide

## ✅ What We've Implemented

1. **SMTP Configuration**: Gmail SMTP with your credentials
2. **EmailService**: Complete email service with project assignment notifications
3. **ProjectService Integration**: Automatic emails when users are assigned to projects
4. **Test Endpoint**: `/api/projects/test-email` for verification

## 🧪 Testing Steps

### Step 1: Test Basic Email Functionality ✅ COMPLETED
```bash
curl -X POST "http://localhost:8080/api/projects/test-email?email=kandukooriniharika9113@gmail.com"
```
**Expected Result**: Email sent to kandukooriniharika9113@gmail.com with subject "Test Email - Project Management System"

### Step 2: Create Test Users
```bash
# Create a user with the target email
curl -X POST "http://localhost:8080/api/users" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test User",
    "email": "kandukooriniharika9113@gmail.com",
    "role": "DEVELOPER"
  }'

# Create project owner
curl -X POST "http://localhost:8080/api/users" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Project Owner",
    "email": "owner@example.com",
    "role": "PRODUCT_OWNER"
  }'
```

### Step 3: Create a Test Project
```bash
curl -X POST "http://localhost:8080/api/projects" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Email Test Project",
    "projectKey": "ETP001",
    "description": "Testing email notifications for project assignments",
    "ownerId": 2,
    "startDate": "2025-01-01",
    "endDate": "2025-12-31"
  }'
```

### Step 4: Add User to Project (This should trigger email!)
```bash
# Replace {projectId} and {userId} with actual IDs from previous steps
curl -X POST "http://localhost:8080/api/projects/1/members/1"
```

### Step 5: Verify Email Content
Check the email inbox for kandukooriniharika9113@gmail.com. You should receive an email with:

- **Subject**: "You've been assigned to project: Email Test Project"
- **Content**:
  ```
  Dear Test User,

  You have been assigned to the following project:

  Project Name: Email Test Project
  Project Key: ETP001
  Description: Testing email notifications for project assignments
  Project Owner: Project Owner
  Your Role: DEVELOPER

  You can now access this project and start collaborating with your team.

  Best regards,
  Project Management System Team
  ```

## 🔍 Alternative Testing Methods

### Option 1: Direct Database Testing
If the API has database connection issues, you can test by:

1. **Direct Database Insert**:
   ```sql
   INSERT INTO users (name, email, role, created_at, updated_at) 
   VALUES ('Test User', 'kandukooriniharika9113@gmail.com', 'DEVELOPER', NOW(), NOW());
   
   INSERT INTO projects (name, project_key, description, owner_id, status, start_date, end_date, created_at, updated_at)
   VALUES ('Test Project', 'TP001', 'Email test', 1, 'ACTIVE', '2025-01-01', '2025-12-31', NOW(), NOW());
   
   INSERT INTO project_members (project_id, user_id)
   VALUES (1, 1);
   ```

### Option 2: Unit Testing
Create a unit test for the EmailService:

```java
@Test
public void testProjectAssignmentNotification() {
    User user = new User("Test User", "kandukooriniharika9113@gmail.com", UserRole.DEVELOPER);
    Project project = new Project();
    project.setName("Test Project");
    project.setProjectKey("TP001");
    project.setDescription("Test description");
    
    emailService.sendProjectAssignmentNotification(user, project);
    // Verify email was sent
}
```

## 📧 What to Look For in Email

1. **Email Delivery**: Check inbox, spam, and promotion folders
2. **Email Format**: Professional formatting with all project details
3. **Timing**: Email should arrive within 1-2 minutes of assignment
4. **Content Accuracy**: All project and user details should be correct

## 🛠️ Troubleshooting

### If emails aren't being sent:
1. **Check Application Logs**: Look for EmailService success/error messages
2. **Verify SMTP Settings**: Ensure Gmail app password is correct
3. **Check Gmail Settings**: Verify "Less secure app access" or use App Password
4. **Network Issues**: Ensure port 587 is accessible

### If emails go to spam:
1. **SPF Records**: Configure proper SPF records for your domain
2. **Content**: Avoid spam trigger words
3. **Sender Reputation**: Use consistent "From" address

## ✅ Success Criteria

- [ ] Test email endpoint works
- [ ] Project assignment triggers email automatically
- [ ] Email contains correct project and user information
- [ ] Email formatting is professional and readable
- [ ] Logs show successful email delivery

## 🚀 Next Steps After Testing

1. **Production Setup**: 
   - Use environment variables for email credentials
   - Set up proper domain and SPF records
   - Configure email templates

2. **Enhanced Features**:
   - HTML email templates
   - Email preferences per user
   - Batch notifications
   - Email history tracking

3. **Monitoring**:
   - Email delivery metrics
   - Failed email retry logic
   - Email bouncing handling