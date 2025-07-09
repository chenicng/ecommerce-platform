package com.ecommerce.api.dto;

import java.time.LocalDateTime;

/**
 * Unified API Response Format
 * Provides consistent response structure for all API endpoints
 */
public class Result<T> {
    
    private String code;
    private String message;
    private T data;
    private LocalDateTime timestamp;
    
    // Private constructor to enforce factory methods
    private Result(String code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }
    
    /**
     * Create success response with data
     */
    public static <T> Result<T> success(T data) {
        return new Result<>("SUCCESS", "Operation completed successfully", data);
    }
    
    /**
     * Create success response with custom message
     */
    public static <T> Result<T> success(String message, T data) {
        return new Result<>("SUCCESS", message, data);
    }
    
    /**
     * Create success response without data
     */
    public static <Void> Result<Void> success() {
        return new Result<>("SUCCESS", "Operation completed successfully", null);
    }
    
    /**
     * Create success response with custom message and no data
     */
    public static <Void> Result<Void> success(String message) {
        return new Result<>("SUCCESS", message, null);
    }
    
    /**
     * Create error response with error code and message
     */
    public static <T> Result<T> error(String code, String message) {
        return new Result<>(code, message, null);
    }
    
    /**
     * Create error response with data (for detailed error information)
     */
    public static <T> Result<T> error(String code, String message, T data) {
        return new Result<>(code, message, data);
    }
    
    /**
     * Create generic error response
     */
    public static <T> Result<T> error(String message) {
        return new Result<>("ERROR", message, null);
    }
    
    // Getters
    public String getCode() {
        return code;
    }
    
    public String getMessage() {
        return message;
    }
    
    public T getData() {
        return data;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    // Utility methods
    public boolean isSuccess() {
        return "SUCCESS".equals(code);
    }
    
    public boolean isError() {
        return !isSuccess();
    }
    
    @Override
    public String toString() {
        return String.format("Result{code='%s', message='%s', data=%s, timestamp=%s}", 
                           code, message, data, timestamp);
    }
} 