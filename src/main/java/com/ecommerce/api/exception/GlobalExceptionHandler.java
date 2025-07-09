package com.ecommerce.api.exception;

import com.ecommerce.api.dto.ErrorCode;
import com.ecommerce.api.dto.Result;
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
    public ResponseEntity<Result<Void>> handleValidationException(MethodArgumentNotValidException e) {
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
        
        Result<Void> result = Result.error(ErrorCode.VALIDATION_ERROR, errorMessage.toString());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
    }
    
    /**
     * Handle bind exceptions
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<Result<String>> handleBindException(BindException e) {
        logger.warn("Bind error: {}", e.getMessage());
        
        StringBuilder errors = new StringBuilder();
        e.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            if (errors.length() > 0) {
                errors.append(", ");
            }
            errors.append(fieldName).append(": ").append(errorMessage);
        });
        
        Result<String> result = Result.error(ErrorCode.BIND_ERROR, "Request binding failed: " + errors.toString());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
    }
    
    /**
     * Handle illegal argument exceptions (validation errors)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Result<Void>> handleIllegalArgumentException(IllegalArgumentException e) {
        logger.warn("Validation error: {}", e.getMessage());
        
        Result<Void> result = Result.error(ErrorCode.VALIDATION_ERROR, e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
    }
    
    /**
     * Handle business logic exceptions
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Result<Void>> handleBusinessException(RuntimeException e, WebRequest request) {
        logger.error("Business error: {}", e.getMessage(), e);
        
        String message = e.getMessage();
        ErrorCode errorCode = ErrorCode.fromMessage(message);
        Result<Void> result = Result.error(errorCode, message);
        
        // Use appropriate HTTP status codes
        HttpStatus status = determineHttpStatus(errorCode);
        return ResponseEntity.status(status).body(result);
    }
    
    /**
     * Determine appropriate HTTP status code based on error type
     */
    private HttpStatus determineHttpStatus(ErrorCode errorCode) {
        switch (errorCode) {
            case RESOURCE_NOT_FOUND:
                return HttpStatus.NOT_FOUND; // 404
            case VALIDATION_ERROR:
            case BIND_ERROR:
            case INSUFFICIENT_BALANCE:
            case INSUFFICIENT_INVENTORY:
            case INSUFFICIENT_FUNDS:
            case OPERATION_NOT_ALLOWED:
                return HttpStatus.BAD_REQUEST; // 400
            case RESOURCE_ALREADY_EXISTS:
                return HttpStatus.CONFLICT; // 409
            case RESOURCE_INACTIVE:
                return HttpStatus.FORBIDDEN; // 403
            case INTERNAL_ERROR:
                return HttpStatus.INTERNAL_SERVER_ERROR; // 500
            case UNSUPPORTED_API_VERSION:
                return HttpStatus.NOT_ACCEPTABLE; // 406
            default:
                return HttpStatus.BAD_REQUEST; // 400
        }
    }
    
    /**
     * Handle unexpected exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<String>> handleUnexpectedException(Exception e, WebRequest request) {
        if (e == null) {
            logger.error("Unexpected error: null exception");
            Result<String> result = Result.error(ErrorCode.INTERNAL_ERROR, "Internal server error occurred");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
        
        logger.error("Unexpected error: {}", e.getMessage(), e);
        
        Result<String> result = Result.error(
            ErrorCode.INTERNAL_ERROR, 
            "Internal server error occurred",
            request != null ? request.getDescription(false) : "Unknown request"
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
    }
} 