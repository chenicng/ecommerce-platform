package com.ecommerce.api.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global Exception Handler
 * Provides unified exception handling for all controllers
 */
@ControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    /**
     * Handle validation exceptions from @Valid annotations
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException e) {
        logger.warn("Validation error: {}", e.getMessage());
        
        StringBuilder errorMessage = new StringBuilder();
        e.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String message = error.getDefaultMessage();
            if (errorMessage.length() > 0) {
                errorMessage.append(", ");
            }
            if ("userId".equals(fieldName) && message.contains("null")) {
                errorMessage.append("User ID is required");
            } else if ("sku".equals(fieldName) && message.contains("blank")) {
                errorMessage.append("SKU is required");
            } else if ("quantity".equals(fieldName) && message.contains("positive")) {
                errorMessage.append("Quantity must be positive");
            } else {
                errorMessage.append(message);
            }
        });
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", "FAILED");
        errorResponse.put("message", errorMessage.toString());
        
        return ResponseEntity.badRequest().body(errorResponse);
    }
    
    /**
     * Handle bind exceptions
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBindException(BindException e) {
        logger.warn("Bind error: {}", e.getMessage());
        
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        ErrorResponse errorResponse = new ErrorResponse(
            "BIND_ERROR",
            "Request binding failed",
            errors.toString(),
            LocalDateTime.now()
        );
        
        return ResponseEntity.badRequest().body(errorResponse);
    }
    
    /**
     * Handle illegal argument exceptions (validation errors)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
        logger.warn("Validation error: {}", e.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
            "VALIDATION_ERROR",
            e.getMessage(),
            null,
            LocalDateTime.now()
        );
        
        return ResponseEntity.badRequest().body(errorResponse);
    }
    
    /**
     * Handle business logic exceptions
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleBusinessException(RuntimeException e, WebRequest request) {
        logger.error("Business error: {}", e.getMessage(), e);
        
        String message = e.getMessage();
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", "FAILED");
        errorResponse.put("message", message);
        
        // Return 404 for "not found" errors (resource lookup failures)
        // Return 400 for business validation errors (insufficient funds, invalid operations, etc.)
        if (message != null && message.toLowerCase().contains("not found")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } else {
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * Handle unexpected exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(Exception e, WebRequest request) {
        logger.error("Unexpected error: {}", e.getMessage(), e);
        
        ErrorResponse errorResponse = new ErrorResponse(
            "INTERNAL_ERROR",
            "Internal server error occurred",
            request.getDescription(false),
            LocalDateTime.now()
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
    
    /**
     * Determine error code based on exception message
     */
    private String determineErrorCode(String message) {
        if (message == null) {
            return "BUSINESS_ERROR";
        }
        
        String lowerMessage = message.toLowerCase();
        if (lowerMessage.contains("not found")) {
            return "RESOURCE_NOT_FOUND";
        } else if (lowerMessage.contains("insufficient balance")) {
            return "INSUFFICIENT_BALANCE";
        } else if (lowerMessage.contains("insufficient inventory")) {
            return "INSUFFICIENT_INVENTORY";
        } else if (lowerMessage.contains("insufficient funds")) {
            return "INSUFFICIENT_FUNDS";
        } else if (lowerMessage.contains("already exists")) {
            return "RESOURCE_ALREADY_EXISTS";
        } else if (lowerMessage.contains("not active")) {
            return "RESOURCE_INACTIVE";
        } else if (lowerMessage.contains("cannot cancel")) {
            return "OPERATION_NOT_ALLOWED";
        } else {
            return "BUSINESS_ERROR";
        }
    }
    
    /**
     * Error Response DTO
     */
    public static class ErrorResponse {
        private String errorCode;
        private String message;
        private String details;
        private LocalDateTime timestamp;
        
        public ErrorResponse(String errorCode, String message, String details, LocalDateTime timestamp) {
            this.errorCode = errorCode;
            this.message = message;
            this.details = details;
            this.timestamp = timestamp;
        }
        
        // Getters
        public String getErrorCode() { return errorCode; }
        public String getMessage() { return message; }
        public String getDetails() { return details; }
        public LocalDateTime getTimestamp() { return timestamp; }
    }
} 