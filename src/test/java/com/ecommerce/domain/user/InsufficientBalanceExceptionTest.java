package com.ecommerce.domain.user;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class InsufficientBalanceExceptionTest {

    @Test
    void shouldCreateExceptionWithMessage() {
        String message = "Insufficient balance for transaction";
        InsufficientBalanceException exception = new InsufficientBalanceException(message);
        
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void shouldCreateExceptionWithMessageAndCause() {
        String message = "Insufficient balance for transaction";
        Throwable cause = new RuntimeException("Root cause");
        InsufficientBalanceException exception = new InsufficientBalanceException(message, cause);
        
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void shouldBeRuntimeException() {
        InsufficientBalanceException exception = new InsufficientBalanceException("Test message");
        
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void shouldHandleNullMessage() {
        InsufficientBalanceException exception = new InsufficientBalanceException(null);
        
        assertNull(exception.getMessage());
    }

    @Test
    void shouldHandleNullCause() {
        String message = "Test message";
        InsufficientBalanceException exception = new InsufficientBalanceException(message, null);
        
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void shouldHandleEmptyMessage() {
        String message = "";
        InsufficientBalanceException exception = new InsufficientBalanceException(message);
        
        assertEquals(message, exception.getMessage());
    }
} 