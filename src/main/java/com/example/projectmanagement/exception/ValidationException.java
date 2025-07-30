package com.example.projectmanagement.exception;

import java.util.List;
import java.util.Map;

public class ValidationException extends RuntimeException {
    private final Map<String, String> errors;

    public ValidationException(List<String> errors2) {
        super("Validation failed");
        this.errors = (Map<String, String>) errors2; 
        
    }

    public Map<String, String> getErrors() {
        return errors;
    }
}
