package com.ecommerce.domain.merchant;

import com.ecommerce.api.dto.ErrorCode;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class InsufficientFundsExceptionTest {

    @Test
    void constructor_WithMessage_ShouldCreateExceptionWithCorrectDetails() {
        String message = "Test message";
        
        InsufficientFundsException exception = new InsufficientFundsException(message);
        
        assertEquals(ErrorCode.INSUFFICIENT_FUNDS, exception.getErrorCode());
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void constructor_WithMessageAndCause_ShouldCreateExceptionWithCorrectDetails() {
        String message = "Test message";
        RuntimeException cause = new RuntimeException("Root cause");
        
        InsufficientFundsException exception = new InsufficientFundsException(message, cause);
        
        assertEquals(ErrorCode.INSUFFICIENT_FUNDS, exception.getErrorCode());
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void constructor_WithNullMessage_ShouldCreateExceptionWithNullMessage() {
        InsufficientFundsException exception = new InsufficientFundsException(null);
        
        assertEquals(ErrorCode.INSUFFICIENT_FUNDS, exception.getErrorCode());
        assertNull(exception.getMessage());
    }

    @Test
    void constructor_WithEmptyMessage_ShouldCreateExceptionWithEmptyMessage() {
        String message = "";
        
        InsufficientFundsException exception = new InsufficientFundsException(message);
        
        assertEquals(ErrorCode.INSUFFICIENT_FUNDS, exception.getErrorCode());
        assertEquals(message, exception.getMessage());
    }

    @Test
    void constructor_WithNullCause_ShouldCreateExceptionWithNullCause() {
        String message = "Test message";
        
        InsufficientFundsException exception = new InsufficientFundsException(message, null);
        
        assertEquals(ErrorCode.INSUFFICIENT_FUNDS, exception.getErrorCode());
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void exceptionShouldBeInstanceOfBusinessException() {
        InsufficientFundsException exception = new InsufficientFundsException("Test");
        
        assertTrue(exception instanceof com.ecommerce.api.exception.BusinessException);
    }

    @Test
    void toString_ShouldContainErrorCodeAndMessage() {
        String message = "Insufficient funds for transaction";
        InsufficientFundsException exception = new InsufficientFundsException(message);
        
        String result = exception.toString();
        assertTrue(result.contains("INSUFFICIENT_FUNDS"));
        assertTrue(result.contains(message));
    }

    @Test
    void getErrorCode_ShouldReturnCorrectErrorCode() {
        InsufficientFundsException exception = new InsufficientFundsException("Test");
        
        assertEquals(ErrorCode.INSUFFICIENT_FUNDS, exception.getErrorCode());
    }
} 