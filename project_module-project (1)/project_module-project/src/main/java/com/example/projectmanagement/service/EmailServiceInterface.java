package com.example.projectmanagement.service;

public interface EmailServiceInterface {
    String sendEmail(String toEmail, String subject, String body);
}
