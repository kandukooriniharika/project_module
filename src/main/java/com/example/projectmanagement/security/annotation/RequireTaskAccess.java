package com.example.projectmanagement.security.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireTaskAccess {
    String taskIdParam() default "id";
    AccessType[] value() default {AccessType.PROJECT_MEMBER};
    String message() default "Access denied: You don't have permission to access this task";
    
    enum AccessType {
        ASSIGNEE,         // Only task assignee can access
        REPORTER,         // Only task reporter can access
        ASSIGNEE_OR_REPORTER, // Either assignee or reporter can access
        PROJECT_MEMBER,   // Project members can access
        PROJECT_OWNER,    // Only project owner can access
        ADMIN             // Only admins can access
    }
}