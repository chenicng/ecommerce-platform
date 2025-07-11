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
     * Handle 404 Not Found exceptions (NoHandlerFoundException)
     * This occurs when a request is made to a non-existent endpoint
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
        
        String message = "Invalid request format";
        Result<Void> result = Result.error(ErrorCode.VALIDATION_ERROR, message);
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
    }
    
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
     * Handle business logic exceptions with explicit error codes
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<Void>> handleBusinessException(BusinessException e, WebRequest request) {
        logger.error("Business error: {}", e.getMessage(), e);
        
        // Use the error code from the exception directly
        ErrorCode errorCode = e.getErrorCode();
        Result<Void> result = Result.error(errorCode, e.getMessage());
        
        // Use appropriate HTTP status codes
        HttpStatus status = determineHttpStatus(errorCode);
        return ResponseEntity.status(status).body(result);
    }
    
    /**
     * Handle runtime exceptions (fallback for legacy code)
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Result<Void>> handleRuntimeException(RuntimeException e, WebRequest request) {
        logger.error("Runtime error: {}", e.getMessage(), e);
        
        // Use fromMessage as fallback for legacy RuntimeExceptions
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
            case MERCHANT_NOT_FOUND:
                return HttpStatus.NOT_FOUND; // 404
            case VALIDATION_ERROR:
            case BIND_ERROR:
            case INSUFFICIENT_BALANCE:
            case INSUFFICIENT_INVENTORY:
            case INSUFFICIENT_FUNDS:
            case OPERATION_NOT_ALLOWED:
            case INVALID_SETTLEMENT_DATE:
            case BUSINESS_ERROR:
                return HttpStatus.BAD_REQUEST; // 400
            case RESOURCE_ALREADY_EXISTS:
                return HttpStatus.CONFLICT; // 409
            case RESOURCE_INACTIVE:
                return HttpStatus.FORBIDDEN; // 403
            case INTERNAL_ERROR:
            case SETTLEMENT_FAILED:
                return HttpStatus.INTERNAL_SERVER_ERROR; // 500
            case UNSUPPORTED_API_VERSION:
                return HttpStatus.NOT_ACCEPTABLE; // 406
            default:
                return HttpStatus.INTERNAL_SERVER_ERROR; // 500 for unknown errors
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