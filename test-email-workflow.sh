#!/bin/bash

echo "🧪 Testing Email Notification Workflow"
echo "======================================"

BASE_URL="http://localhost:8080"
EMAIL="kandukooriniharika9113@gmail.com"

echo ""
echo "Step 1: Testing basic email functionality..."
RESPONSE=$(curl -s -X POST "$BASE_URL/api/projects/test-email?email=$EMAIL")
echo "Response: $RESPONSE"

if [[ $RESPONSE == *"successfully"* ]]; then
    echo "✅ Basic email test PASSED"
else
    echo "❌ Basic email test FAILED"
    exit 1
fi

echo ""
echo "Step 2: Creating test users..."

# Create user with target email
USER1_RESPONSE=$(curl -s -X POST "$BASE_URL/api/users" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test User",
    "email": "'$EMAIL'",
    "role": "DEVELOPER"
  }')

echo "User 1 Response: $USER1_RESPONSE"

# Create project owner
USER2_RESPONSE=$(curl -s -X POST "$BASE_URL/api/users" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Project Owner",
    "email": "owner@example.com",
    "role": "PRODUCT_OWNER"
  }')

echo "User 2 Response: $USER2_RESPONSE"

# Extract user IDs (basic parsing)
USER1_ID=$(echo $USER1_RESPONSE | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
USER2_ID=$(echo $USER2_RESPONSE | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)

echo "User 1 ID: $USER1_ID"
echo "User 2 ID: $USER2_ID"

if [[ -z "$USER1_ID" || -z "$USER2_ID" ]]; then
    echo "❌ Failed to create users or extract IDs"
    echo "Manual testing required - see test-email-notifications.md"
    exit 1
fi

echo ""
echo "Step 3: Creating test project..."

PROJECT_RESPONSE=$(curl -s -X POST "$BASE_URL/api/projects" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Email Test Project",
    "projectKey": "ETP001",
    "description": "Testing email notifications for project assignments",
    "ownerId": '$USER2_ID',
    "startDate": "2025-01-01",
    "endDate": "2025-12-31"
  }')

echo "Project Response: $PROJECT_RESPONSE"

PROJECT_ID=$(echo $PROJECT_RESPONSE | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
echo "Project ID: $PROJECT_ID"

if [[ -z "$PROJECT_ID" ]]; then
    echo "❌ Failed to create project"
    exit 1
fi

echo ""
echo "Step 4: Assigning user to project (this should trigger email notification)..."

ASSIGNMENT_RESPONSE=$(curl -s -X POST "$BASE_URL/api/projects/$PROJECT_ID/members/$USER1_ID")
echo "Assignment Response: $ASSIGNMENT_RESPONSE"

if [[ $ASSIGNMENT_RESPONSE == *"Email Test Project"* ]]; then
    echo "✅ User assignment SUCCESSFUL"
    echo ""
    echo "🎉 EMAIL NOTIFICATION SHOULD BE SENT!"
    echo "📧 Check the inbox for: $EMAIL"
    echo ""
    echo "Expected email details:"
    echo "  Subject: You've been assigned to project: Email Test Project"
    echo "  From: kandukoori1919@gmail.com"
    echo "  Content: Project assignment details with project info"
    echo ""
    echo "✅ All tests completed successfully!"
else
    echo "❌ User assignment failed"
    echo "Check application logs for errors"
fi

echo ""
echo "📋 Next steps:"
echo "1. Check email inbox for kandukooriniharika9113@gmail.com"
echo "2. Verify email content matches expected format"
echo "3. Check application logs for email sending confirmation"
echo "4. Test with different users and projects"