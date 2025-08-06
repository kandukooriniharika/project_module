package com.example.projectmanagement.service;

import com.example.projectmanagement.service.EmailServiceInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailServiceInterface {

    @Autowired
    private JavaMailSender mailSender;

    @Override
    public String sendEmail(String toEmail, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("yourgmail@gmail.com"); // must match your configured Gmail in application.properties
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);
            return "Email sent successfully to " + toEmail;
        } catch (Exception e) {
            return "Error while sending email: " + e.getMessage();
        }
    }
}
