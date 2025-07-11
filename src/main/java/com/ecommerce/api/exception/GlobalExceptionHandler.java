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
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.http.converter.HttpMessageNotReadableException;

/**
 * Global Exception Handler
 * Provides unified exception handling for all controllers
 */
@ControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    /**
     * Handle 404 Not Found exceptions
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<Result<Void>> handleNoHandlerFoundException(NoHandlerFoundException e, WebRequest request) {
        logger.warn("Endpoint not found: {} {}", e.getHttpMethod(), e.getRequestURL());
        
        String message = String.format("Endpoint '%s %s' not found", e.getHttpMethod(), e.getRequestURL());
        Result<Void> result = Result.error(ErrorCode.RESOURCE_NOT_FOUND, message);
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
    }
    
    /**
     * Handle HTTP method not supported exceptions
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Result<Void>> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        logger.warn("HTTP method not supported: {}", e.getMethod());
        
        String message = String.format("HTTP method '%s' is not supported for this endpoint", e.getMethod());
        Result<Void> result = Result.error(ErrorCode.OPERATION_NOT_ALLOWED, message);
        
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(result);
    }
    
    /**
     * Handle media type not acceptable exceptions
     */
    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<Result<Void>> handleHttpMediaTypeNotAcceptableException(HttpMediaTypeNotAcceptableException e) {
        logger.warn("Media type not acceptable: {}", e.getMessage());
        
        String message = "Requested media type is not supported";
        Result<Void> result = Result.error(ErrorCode.UNSUPPORTED_API_VERSION, message);
        
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(result);
    }
    
    /**
     * Handle HTTP message not readable exceptions (JSON parsing errors)
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Result<Void>> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        logger.warn("Message not readable: {}", e.getMessage());
        
        String message = "Invalid request format or malformed JSON";
        Result<Void> result = Result.error(ErrorCode.VALIDATION_ERROR, message);
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
    }
    
    /**
     * Handle validation exceptions with improved error messages
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
            errorMessage.append(buildUserFriendlyValidationMessage(fieldName, message));
        });
        
        Result<Void> result = Result.error(ErrorCode.VALIDATION_ERROR, errorMessage.toString());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
    }
    
    /**
     * Build user-friendly validation messages
     */
    private String buildUserFriendlyValidationMessage(String fieldName, String originalMessage) {
        // Map common validation errors to user-friendly messages
        if ("userId".equals(fieldName) && originalMessage.contains("null")) {
            return "User ID is required";
        } else if ("sku".equals(fieldName) && originalMessage.contains("blank")) {
            return "Product SKU is required";
        } else if ("quantity".equals(fieldName) && originalMessage.contains("positive")) {
            return "Quantity must be positive";
        } else if ("amount".equals(fieldName)) {
            return "Amount must be a positive number";
        } else if ("email".equals(fieldName)) {
            return "Valid email address is required";
        } else if ("phone".equals(fieldName)) {
            return "Valid phone number is required";
        }
        return originalMessage;
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
     * Handle illegal argument exceptions
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Result<Void>> handleIllegalArgumentException(IllegalArgumentException e) {
        logger.warn("Validation error: {}", e.getMessage());
        
        Result<Void> result = Result.error(ErrorCode.VALIDATION_ERROR, e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
    }
    
    /**
     * Handle business logic exceptions with explicit error codes
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<Void>> handleBusinessException(BusinessException e, WebRequest request) {
        logger.error("Business error: {}", e.getMessage(), e);
        
        ErrorCode errorCode = e.getErrorCode();
        Result<Void> result = Result.error(errorCode, e.getMessage());
        
        HttpStatus status = determineHttpStatus(errorCode);
        return ResponseEntity.status(status).body(result);
    }
    
    /**
     * Handle runtime exceptions (fallback for legacy code)
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Result<Void>> handleRuntimeException(RuntimeException e, WebRequest request) {
        logger.error("Runtime error: {}", e.getMessage(), e);
        
        String message = e.getMessage();
        ErrorCode errorCode = ErrorCode.fromMessage(message);
        Result<Void> result = Result.error(errorCode, message);
        
        HttpStatus status = determineHttpStatus(errorCode);
        return ResponseEntity.status(status).body(result);
    }
    
    /**
     * Determine appropriate HTTP status code based on error type
     */
    private HttpStatus determineHttpStatus(ErrorCode errorCode) {
        return switch (errorCode) {
            case RESOURCE_NOT_FOUND, MERCHANT_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case VALIDATION_ERROR, BIND_ERROR, INSUFFICIENT_BALANCE, 
                 INSUFFICIENT_INVENTORY, INSUFFICIENT_FUNDS, OPERATION_NOT_ALLOWED,
                 INVALID_SETTLEMENT_DATE, BUSINESS_ERROR -> HttpStatus.BAD_REQUEST;
            case RESOURCE_ALREADY_EXISTS -> HttpStatus.CONFLICT;
            case RESOURCE_INACTIVE -> HttpStatus.FORBIDDEN;
            case UNSUPPORTED_API_VERSION -> HttpStatus.NOT_ACCEPTABLE;
            case INTERNAL_ERROR, SETTLEMENT_FAILED -> HttpStatus.INTERNAL_SERVER_ERROR;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
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