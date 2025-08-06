# 📋 Manual Testing Checklist for Email Notifications

## Quick Verification Steps

### ✅ Step 1: Basic Email Test (ALREADY WORKING)
```bash
curl -X POST "http://localhost:8080/api/projects/test-email?email=kandukooriniharika9113@gmail.com"
```
**Status**: ✅ CONFIRMED WORKING - Email successfully sent!

### 🔍 Step 2: Check Email Inbox
- [ ] Open Gmail for `kandukooriniharika9113@gmail.com`
- [ ] Look for email with subject: "Test Email - Project Management System"
- [ ] Verify email arrived from: `kandukoori1919@gmail.com`
- [ ] Check spam/promotions folder if not in inbox

### 🧪 Step 3: Test Real Project Assignment

#### Option A: Using API (if database works)
```bash
# Run the automated test script
./test-email-workflow.sh
```

#### Option B: Manual API Testing
1. **Create a user with your email**:
```bash
curl -X POST "http://localhost:8080/api/users" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test User",
    "email": "kandukooriniharika9113@gmail.com",
    "role": "DEVELOPER"
  }'
```

2. **Create a project owner**:
```bash
curl -X POST "http://localhost:8080/api/users" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Project Manager",
    "email": "pm@example.com",
    "role": "PRODUCT_OWNER"
  }'
```

3. **Create a project**:
```bash
curl -X POST "http://localhost:8080/api/projects" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "My Test Project",
    "projectKey": "MTP001",
    "description": "Testing email notifications",
    "ownerId": 2,
    "startDate": "2025-01-01",
    "endDate": "2025-12-31"
  }'
```

4. **Assign user to project** (this triggers the email!):
```bash
curl -X POST "http://localhost:8080/api/projects/1/members/1"
```

### 📧 Step 4: Verify Project Assignment Email
After running the assignment command, check your email for:
- [ ] **Subject**: "You've been assigned to project: My Test Project"
- [ ] **From**: kandukoori1919@gmail.com
- [ ] **Content includes**:
  - [ ] Your name: "Test User"
  - [ ] Project name: "My Test Project"
  - [ ] Project key: "MTP001"
  - [ ] Project description
  - [ ] Project owner name
  - [ ] Your role: "DEVELOPER"
  - [ ] Professional closing message

### 🔍 Step 5: Monitor Application Logs
Watch the application logs for confirmation:
```bash
# Look for these log messages:
# INFO: Successfully sent project assignment email to kandukooriniharika9113@gmail.com for project My Test Project
```

## 🎯 Success Criteria

- [x] Test email endpoint works
- [ ] Project assignment triggers automatic email
- [ ] Email contains all project details
- [ ] Email formatting is professional
- [ ] Logs confirm successful email delivery
- [ ] Email arrives within 2 minutes

## 🚨 Troubleshooting Guide

### If Test Email Works But Assignment Email Doesn't:
1. **Check database connection** - API might be failing
2. **Check application logs** for errors during project assignment
3. **Try manual database insertion** (see testing guide)

### If No Emails Are Received:
1. **Check spam folder**
2. **Verify Gmail app password** is correct
3. **Check application logs** for SMTP errors
4. **Verify network connectivity** to smtp.gmail.com:587

### If Emails Go to Spam:
1. **Add sender to contacts**: kandukoori1919@gmail.com
2. **Mark as "Not Spam"** in Gmail
3. **Consider SPF records** for production use

## 🎉 What You Should See

### Test Email Content:
```
Subject: Test Email - Project Management System

This is a test email to verify that the SMTP configuration is working correctly.

If you receive this email, the email notification system is properly configured.

Best regards,
Project Management System Team
```

### Project Assignment Email Content:
```
Subject: You've been assigned to project: My Test Project

Dear Test User,

You have been assigned to the following project:

Project Name: My Test Project
Project Key: MTP001
Description: Testing email notifications
Project Owner: Project Manager
Your Role: DEVELOPER

You can now access this project and start collaborating with your team.

Best regards,
Project Management System Team
```

## 📈 Next Steps After Successful Testing

1. **Test with multiple users**
2. **Test project creation with initial members**
3. **Test removing users from projects**
4. **Consider HTML email templates**
5. **Add email preferences for users**
6. **Set up monitoring for email delivery**