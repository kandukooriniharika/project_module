package com.example.projectmanagement.service;

import com.example.projectmanagement.entity.Project;
import com.example.projectmanagement.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    /**
     * Sends an email notification when a user is assigned to a project
     * @param user The user who was assigned to the project
     * @param project The project the user was assigned to
     */
    public void sendProjectAssignmentNotification(User user, Project project) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(user.getEmail());
            message.setSubject("You've been assigned to project: " + project.getName());
            
            String emailBody = buildProjectAssignmentEmailBody(user, project);
            message.setText(emailBody);
            
            message.setFrom("kandukoori1919@gmail.com");
            
            mailSender.send(message);
            logger.info("Successfully sent project assignment email to {} for project {}", user.getEmail(), project.getName());
            
        } catch (Exception e) {
            logger.error("Failed to send project assignment email to {} for project {}: {}", 
                user.getEmail(), project.getName(), e.getMessage(), e);
        }
    }

    /**
     * Sends a test email to verify SMTP configuration
     * @param toEmail The email address to send the test email to
     */
    public void sendTestEmail(String toEmail) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("Test Email - Project Management System");
            message.setText("This is a test email to verify that the SMTP configuration is working correctly.\n\n" +
                          "If you receive this email, the email notification system is properly configured.\n\n" +
                          "Best regards,\nProject Management System");
            message.setFrom("kandukoori1919@gmail.com");
            
            mailSender.send(message);
            logger.info("Successfully sent test email to {}", toEmail);
            
        } catch (Exception e) {
            logger.error("Failed to send test email to {}: {}", toEmail, e.getMessage(), e);
            throw new RuntimeException("Failed to send test email: " + e.getMessage(), e);
        }
    }

    /**
     * Builds the email body for project assignment notification
     * @param user The user who was assigned
     * @param project The project they were assigned to
     * @return The formatted email body
     */
    private String buildProjectAssignmentEmailBody(User user, Project project) {
        StringBuilder body = new StringBuilder();
        body.append("Dear ").append(user.getName()).append(",\n\n");
        body.append("You have been assigned to the following project:\n\n");
        body.append("Project Name: ").append(project.getName()).append("\n");
        body.append("Project Key: ").append(project.getProjectKey()).append("\n");
        
        if (project.getDescription() != null && !project.getDescription().trim().isEmpty()) {
            body.append("Description: ").append(project.getDescription()).append("\n");
        }
        
        body.append("Project Owner: ").append(project.getOwner().getName()).append("\n");
        body.append("Your Role: ").append(user.getRole()).append("\n\n");
        
        body.append("You can now access this project and start collaborating with your team.\n\n");
        body.append("Best regards,\n");
        body.append("Project Management System Team");
        
        return body.toString();
    }
}