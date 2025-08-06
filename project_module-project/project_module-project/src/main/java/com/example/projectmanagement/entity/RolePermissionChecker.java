package com.example.projectmanagement.entity;

import com.example.projectmanagement.entity.User.UserRole;

public class RolePermissionChecker {

    public static boolean canUpdateTask(UserRole role) {
        return role == UserRole.DEVELOPER || role == UserRole.ADMIN;
    }

    public static boolean canStartSprint(UserRole role) {
        return role == UserRole.PRODUCT_OWNER || role == UserRole.SCRUM_MASTER || role == UserRole.ADMIN;
    }

    public static boolean canDeleteSprint(UserRole role) {
        return role == UserRole.ADMIN;
    }

    public static boolean canCreateSprint(UserRole role) {
        return role == UserRole.PRODUCT_OWNER || role == UserRole.SCRUM_MASTER || role == UserRole.ADMIN;
    }

    // Add more methods as needed
}