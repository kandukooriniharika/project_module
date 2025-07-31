package com.example.projectmanagement.security.annotation;

import com.example.projectmanagement.entity.User;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRole {
    User.UserRole[] value();
    String message() default "Access denied: Insufficient privileges";
}