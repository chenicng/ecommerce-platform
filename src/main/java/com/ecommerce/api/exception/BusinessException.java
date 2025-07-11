package com.ecommerce.api.exception;

import com.ecommerce.api.dto.ErrorCode;

/**
 * Business Exception Base Class
 * All business logic exceptions should extend this class and specify their error code
 */
public class BusinessException extends RuntimeException {
    
    private final ErrorCode errorCode;
    
    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public BusinessException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public ErrorCode getErrorCode() {
        return errorCode;
    }
    
    @Override
    public String toString() {
        return String.format("BusinessException{errorCode=%s, message='%s'}", 
                           errorCode, getMessage());
    }
} 