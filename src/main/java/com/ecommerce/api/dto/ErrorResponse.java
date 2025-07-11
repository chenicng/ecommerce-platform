package com.ecommerce.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

/**
 * Error Response DTO
 * Used specifically for OpenAPI documentation to provide proper error response examples
 */
@Schema(description = "Error response structure")
public class ErrorResponse {
    
    @Schema(description = "Error code", example = "RESOURCE_NOT_FOUND")
    private String code;
    
    @Schema(description = "Error message", example = "User not found")
    private String message;
    
    @Schema(description = "Error data (usually null for errors)", nullable = true)
    private Object data;
    
    @Schema(description = "Error timestamp", example = "2025-07-11T12:00:00")
    private LocalDateTime timestamp;
    
    public ErrorResponse() {}
    
    public ErrorResponse(String code, String message, Object data, LocalDateTime timestamp) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = timestamp;
    }
    
    // Getters and setters
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public Object getData() {
        return data;
    }
    
    public void setData(Object data) {
        this.data = data;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}