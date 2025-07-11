package com.ecommerce.api.exception;

import com.ecommerce.api.dto.ErrorCode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BusinessExceptionTest {

    @Test
    void constructor_WithErrorCodeAndMessage_ShouldCreateException() {
        // Given
        ErrorCode errorCode = ErrorCode.VALIDATION_ERROR;
        String message = "Validation failed";
        
        // When
        BusinessException exception = new BusinessException(errorCode, message);
        
        // Then
        assertEquals(errorCode, exception.getErrorCode());
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void constructor_WithErrorCodeMessageAndCause_ShouldCreateException() {
        // Given
        ErrorCode errorCode = ErrorCode.INTERNAL_ERROR;
        String message = "Internal error occurred";
        Throwable cause = new RuntimeException("Root cause");
        
        // When
        BusinessException exception = new BusinessException(errorCode, message, cause);
        
        // Then
        assertEquals(errorCode, exception.getErrorCode());
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void toString_ShouldReturnFormattedString() {
        // Given
        ErrorCode errorCode = ErrorCode.BUSINESS_ERROR;
        String message = "Business logic failed";
        BusinessException exception = new BusinessException(errorCode, message);
        
        // When
        String result = exception.toString();
        
        // Then
        assertTrue(result.contains("BusinessException"));
        assertTrue(result.contains(errorCode.toString()));
        assertTrue(result.contains(message));
    }

    @Test
    void getErrorCode_ShouldReturnCorrectErrorCode() {
        // Test with different error codes
        ErrorCode[] errorCodes = {
            ErrorCode.VALIDATION_ERROR,
            ErrorCode.INSUFFICIENT_BALANCE,
            ErrorCode.RESOURCE_NOT_FOUND,
            ErrorCode.INTERNAL_ERROR
        };
        
        for (ErrorCode errorCode : errorCodes) {
            BusinessException exception = new BusinessException(errorCode, "Test message");
            assertEquals(errorCode, exception.getErrorCode());
        }
    }
} 