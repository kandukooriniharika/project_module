package com.example.projectmanagement.security.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireProjectAccess {
    String projectIdParam() default "id";
    AccessType[] value() default {AccessType.MEMBER};
    String message() default "Access denied: You don't have permission to access this project";
    
    enum AccessType {
        OWNER,    // Only project owner can access
        MEMBER,   // Project members can access
        ADMIN     // Only admins can access
    }
}