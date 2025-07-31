package com.example.projectmanagement.controller;

import com.example.projectmanagement.dto.UserDto;
import com.example.projectmanagement.entity.User;
import com.example.projectmanagement.service.UserService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Authentication Controller for testing RBAC system
 * In production, this would be replaced with proper OAuth2/OIDC integration
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UserService userService;

    private final Key jwtKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    /**
     * Simple login endpoint that generates a JWT token for testing
     * In production, this would integrate with OAuth2/OIDC provider
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> credentials) {
        String email = credentials.get("email");
        
        try {
            // Find user by email
            User user = userService.findByEmail(email);
            
            // Generate JWT token
            String token = Jwts.builder()
                    .setSubject(user.getEmail())
                    .claim("email", user.getEmail())
                    .claim("name", user.getName())
                    .claim("role", user.getRole().toString())
                    .claim("userId", user.getId())
                    .setIssuedAt(Date.from(Instant.now()))
                    .setExpiration(Date.from(Instant.now().plus(24, ChronoUnit.HOURS)))
                    .signWith(jwtKey)
                    .compact();

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("user", convertToDto(user));
            response.put("message", "Login successful");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "User not found or invalid credentials");
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Get current user information
     */
    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser() {
        // This would be populated by the JWT authentication converter in production
        return ResponseEntity.ok(new UserDto());
    }

    /**
     * Test endpoint to check if authentication is working
     */
    @GetMapping("/test")
    public ResponseEntity<Map<String, String>> testAuth() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Authentication is working!");
        response.put("timestamp", Instant.now().toString());
        return ResponseEntity.ok(response);
    }

    private UserDto convertToDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        return dto;
    }
}