package com.ecommerce.api.dto;

/**
 * API Error Codes
 * Centralized management of all error codes used in the application
 */
public enum ErrorCode {
    
    // Success
    SUCCESS("SUCCESS", "Operation completed successfully"),
    
    // Validation Errors
    VALIDATION_ERROR("VALIDATION_ERROR", "Request validation failed"),
    BIND_ERROR("BIND_ERROR", "Request binding failed"),
    
    // Business Logic Errors
    RESOURCE_NOT_FOUND("RESOURCE_NOT_FOUND", "Requested resource not found"),
    INSUFFICIENT_BALANCE("INSUFFICIENT_BALANCE", "Insufficient user balance"),
    INSUFFICIENT_INVENTORY("INSUFFICIENT_INVENTORY", "Insufficient product inventory"),
    INSUFFICIENT_FUNDS("INSUFFICIENT_FUNDS", "Insufficient merchant funds"),
    RESOURCE_ALREADY_EXISTS("RESOURCE_ALREADY_EXISTS", "Resource already exists"),
    RESOURCE_INACTIVE("RESOURCE_INACTIVE", "Resource is not active"),
    OPERATION_NOT_ALLOWED("OPERATION_NOT_ALLOWED", "Operation not allowed"),
    BUSINESS_ERROR("BUSINESS_ERROR", "Business logic error"),
    
    // System Errors
    INTERNAL_ERROR("INTERNAL_ERROR", "Internal server error"),
    UNSUPPORTED_API_VERSION("UNSUPPORTED_API_VERSION", "API version not supported"),
    
    // Settlement related errors
    SETTLEMENT_FAILED("SETTLEMENT_FAILED", "Settlement execution failed"),
    MERCHANT_NOT_FOUND("MERCHANT_NOT_FOUND", "Merchant not found"),
    INVALID_SETTLEMENT_DATE("INVALID_SETTLEMENT_DATE", "Invalid settlement date"),
    
    // Security and Rate Limiting
    RATE_LIMIT_EXCEEDED("RATE_LIMIT_EXCEEDED", "Rate limit exceeded"),
    INPUT_TOO_LARGE("INPUT_TOO_LARGE", "Input data too large"),
    INVALID_CURRENCY("INVALID_CURRENCY", "Invalid currency format");
    
    private final String code;
    private final String defaultMessage;
    
    ErrorCode(String code, String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDefaultMessage() {
        return defaultMessage;
    }
    
    /**
     * Determine error code based on exception message
     */
    public static ErrorCode fromMessage(String message) {
        if (message == null || message.trim().isEmpty()) {
            return INTERNAL_ERROR;
        }
        
        String lowerMessage = message.toLowerCase();
        if (lowerMessage.contains("not found")) {
            return RESOURCE_NOT_FOUND;
        } else if (lowerMessage.contains("insufficient balance")) {
            return INSUFFICIENT_BALANCE;
        } else if (lowerMessage.contains("insufficient inventory")) {
            return INSUFFICIENT_INVENTORY;
        } else if (lowerMessage.contains("insufficient funds")) {
            return INSUFFICIENT_FUNDS;
        } else if (lowerMessage.contains("already exists")) {
            return RESOURCE_ALREADY_EXISTS;
        } else if (lowerMessage.contains("not active")) {
            return RESOURCE_INACTIVE;
        } else if (lowerMessage.contains("cannot cancel")) {
            return OPERATION_NOT_ALLOWED;
        } else if (lowerMessage.contains("not belong")) {
            return VALIDATION_ERROR;
        } else if (lowerMessage.contains("settlement")) {
            return SETTLEMENT_FAILED;
        } else if (lowerMessage.contains("merchant not found")) {
            return MERCHANT_NOT_FOUND;
        } else if (lowerMessage.contains("business error") || lowerMessage.contains("business logic error")) {
            return BUSINESS_ERROR;
        } else {
            return INTERNAL_ERROR;
        }
    }
} 