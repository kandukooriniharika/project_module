package com.example.projectmanagement.controller;

// package com.yourpackage.controller;

import com.example.projectmanagement.dto.EmailDTO;
import com.example.projectmanagement.service.EmailServiceInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/email")
public class EmailController {

    @Autowired
    private EmailServiceInterface emailService;

    @PostMapping("/send")
    public ResponseEntity<String> sendEmail(@RequestBody EmailDTO request) {
        String result = emailService.sendEmail(request.getToEmail(), request.getSubject(), request.getBody());
        return ResponseEntity.ok(result);
    }
}
