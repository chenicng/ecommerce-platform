package com.ecommerce.domain.user;

import com.ecommerce.api.dto.ErrorCode;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class InsufficientBalanceExceptionTest {

    @Test
    void constructor_WithMessage_ShouldCreateExceptionWithCorrectDetails() {
        String message = "Test message";
        
        InsufficientBalanceException exception = new InsufficientBalanceException(message);
        
        assertEquals(ErrorCode.INSUFFICIENT_BALANCE, exception.getErrorCode());
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void constructor_WithMessageAndCause_ShouldCreateExceptionWithCorrectDetails() {
        String message = "Test message";
        RuntimeException cause = new RuntimeException("Root cause");
        
        InsufficientBalanceException exception = new InsufficientBalanceException(message, cause);
        
        assertEquals(ErrorCode.INSUFFICIENT_BALANCE, exception.getErrorCode());
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void constructor_WithNullMessage_ShouldCreateExceptionWithNullMessage() {
        InsufficientBalanceException exception = new InsufficientBalanceException(null);
        
        assertEquals(ErrorCode.INSUFFICIENT_BALANCE, exception.getErrorCode());
        assertNull(exception.getMessage());
    }

    @Test
    void constructor_WithEmptyMessage_ShouldCreateExceptionWithEmptyMessage() {
        String message = "";
        
        InsufficientBalanceException exception = new InsufficientBalanceException(message);
        
        assertEquals(ErrorCode.INSUFFICIENT_BALANCE, exception.getErrorCode());
        assertEquals(message, exception.getMessage());
    }

    @Test
    void constructor_WithNullCause_ShouldCreateExceptionWithNullCause() {
        String message = "Test message";
        
        InsufficientBalanceException exception = new InsufficientBalanceException(message, null);
        
        assertEquals(ErrorCode.INSUFFICIENT_BALANCE, exception.getErrorCode());
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void exceptionShouldBeInstanceOfBusinessException() {
        InsufficientBalanceException exception = new InsufficientBalanceException("Test");
        
        assertTrue(exception instanceof com.ecommerce.api.exception.BusinessException);
    }
} 