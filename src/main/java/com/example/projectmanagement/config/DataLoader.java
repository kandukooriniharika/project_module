package com.example.projectmanagement.config;

import com.example.projectmanagement.entity.User;
import com.example.projectmanagement.entity.Role;
import com.example.projectmanagement.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * DataLoader component to initialize test users for RBAC testing
 * This will create sample users with different roles on application startup
 * Only runs if no users exist in the database
 */
@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Only load data if no users exist
        if (userRepository.count() == 0) {
            loadTestUsers();
        }
    }

    private void loadTestUsers() {
        // Create Admin User
        User admin = new User();
        admin.setName("Admin User");
        admin.setEmail("admin@company.com");
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("password123"));
        admin.setRoles(Set.of(Role.ADMIN));
        userRepository.save(admin);

        // Create Developer User
        User developer = new User();
        developer.setName("John Developer");
        developer.setEmail("john.dev@company.com");
        developer.setUsername("johndev");
        developer.setPassword(passwordEncoder.encode("password123"));
        developer.setRoles(Set.of(Role.DEVELOPER));
        userRepository.save(developer);

        // Create Product Owner User
        User productOwner = new User();
        productOwner.setName("Jane ProductOwner");
        productOwner.setEmail("jane.po@company.com");
        productOwner.setUsername("janepo");
        productOwner.setPassword(passwordEncoder.encode("password123"));
        productOwner.setRoles(Set.of(Role.PRODUCT_OWNER));
        userRepository.save(productOwner);

        // Create Scrum Master User
        User scrumMaster = new User();
        scrumMaster.setName("Mike ScrumMaster");
        scrumMaster.setEmail("mike.sm@company.com");
        scrumMaster.setUsername("mikesm");
        scrumMaster.setPassword(passwordEncoder.encode("password123"));
        scrumMaster.setRoles(Set.of(Role.SCRUM_MASTER));
        userRepository.save(scrumMaster);

        // Create Multi-Role User (Developer + Scrum Master)
        User multiRole = new User();
        multiRole.setName("Sarah MultiRole");
        multiRole.setEmail("sarah@company.com");
        multiRole.setUsername("sarahmulti");
        multiRole.setPassword(passwordEncoder.encode("password123"));
        multiRole.setRoles(Set.of(Role.DEVELOPER, Role.SCRUM_MASTER));
        userRepository.save(multiRole);

        System.out.println("✅ Test users loaded successfully!");
        System.out.println("📋 Test Credentials:");
        System.out.println("   Admin: admin / password123");
        System.out.println("   Developer: johndev / password123");
        System.out.println("   Product Owner: janepo / password123");
        System.out.println("   Scrum Master: mikesm / password123");
        System.out.println("   Multi-Role: sarahmulti / password123");
    }
}