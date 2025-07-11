package com.ecommerce.domain.order;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class InvalidOrderStateExceptionTest {

    @Test
    void constructor_WithMessage_ShouldCreateExceptionWithMessage() {
        String message = "Invalid order state transition";
        
        InvalidOrderStateException exception = new InvalidOrderStateException(message);
        
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void constructor_WithMessageAndCause_ShouldCreateExceptionWithMessageAndCause() {
        String message = "Invalid order state";
        RuntimeException cause = new RuntimeException("Root cause");
        
        InvalidOrderStateException exception = new InvalidOrderStateException(message, cause);
        
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void constructor_WithNullMessage_ShouldCreateExceptionWithNullMessage() {
        InvalidOrderStateException exception = new InvalidOrderStateException(null);
        
        assertNull(exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void constructor_WithEmptyMessage_ShouldCreateExceptionWithEmptyMessage() {
        String message = "";
        
        InvalidOrderStateException exception = new InvalidOrderStateException(message);
        
        assertEquals(message, exception.getMessage());
    }

    @Test
    void constructor_WithNullCause_ShouldCreateExceptionWithNullCause() {
        String message = "Test message";
        
        InvalidOrderStateException exception = new InvalidOrderStateException(message, null);
        
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void exceptionShouldBeInstanceOfRuntimeException() {
        InvalidOrderStateException exception = new InvalidOrderStateException("Test");
        
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void exceptionShouldAllowDetailedMessages() {
        String detailedMessage = "Cannot cancel completed order. Current status: COMPLETED";
        
        InvalidOrderStateException exception = new InvalidOrderStateException(detailedMessage);
        
        assertEquals(detailedMessage, exception.getMessage());
        assertTrue(exception.getMessage().contains("COMPLETED"));
    }
} 