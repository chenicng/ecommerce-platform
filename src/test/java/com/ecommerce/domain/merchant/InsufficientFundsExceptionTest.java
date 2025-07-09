package com.ecommerce.domain.merchant;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class InsufficientFundsExceptionTest {

    @Test
    void shouldCreateExceptionWithMessage() {
        String message = "Insufficient funds for withdrawal";
        InsufficientFundsException exception = new InsufficientFundsException(message);
        
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void shouldCreateExceptionWithMessageAndCause() {
        String message = "Insufficient funds for withdrawal";
        Throwable cause = new RuntimeException("Account verification failed");
        InsufficientFundsException exception = new InsufficientFundsException(message, cause);
        
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void shouldBeRuntimeException() {
        InsufficientFundsException exception = new InsufficientFundsException("Test message");
        
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void shouldHandleNullMessage() {
        InsufficientFundsException exception = new InsufficientFundsException(null);
        
        assertNull(exception.getMessage());
    }

    @Test
    void shouldHandleNullCause() {
        String message = "Test message";
        InsufficientFundsException exception = new InsufficientFundsException(message, null);
        
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void shouldHandleEmptyMessage() {
        String message = "";
        InsufficientFundsException exception = new InsufficientFundsException(message);
        
        assertEquals(message, exception.getMessage());
    }

    @Test
    void shouldHandleDetailedMessage() {
        String message = "Cannot withdraw 500.00 USD. Available balance: 300.00 USD";
        InsufficientFundsException exception = new InsufficientFundsException(message);
        
        assertEquals(message, exception.getMessage());
        assertTrue(exception.getMessage().contains("500.00"));
        assertTrue(exception.getMessage().contains("300.00"));
        assertTrue(exception.getMessage().contains("USD"));
    }

    @Test
    void shouldHandleRefundScenario() {
        String message = "Merchant has insufficient funds for refund. Required: 100.00 USD, Available: 50.00 USD";
        InsufficientFundsException exception = new InsufficientFundsException(message);
        
        assertEquals(message, exception.getMessage());
        assertTrue(exception.getMessage().contains("refund"));
        assertTrue(exception.getMessage().contains("100.00"));
        assertTrue(exception.getMessage().contains("50.00"));
    }
} 