package com.ecommerce.domain.product;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class InsufficientInventoryExceptionTest {

    @Test
    void shouldCreateExceptionWithMessage() {
        String message = "Insufficient inventory for product SKU001";
        InsufficientInventoryException exception = new InsufficientInventoryException(message);
        
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void shouldCreateExceptionWithMessageAndCause() {
        String message = "Insufficient inventory for product SKU001";
        Throwable cause = new RuntimeException("Database connection lost");
        InsufficientInventoryException exception = new InsufficientInventoryException(message, cause);
        
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void shouldBeRuntimeException() {
        InsufficientInventoryException exception = new InsufficientInventoryException("Test message");
        
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void shouldHandleNullMessage() {
        InsufficientInventoryException exception = new InsufficientInventoryException(null);
        
        assertNull(exception.getMessage());
    }

    @Test
    void shouldHandleNullCause() {
        String message = "Test message";
        InsufficientInventoryException exception = new InsufficientInventoryException(message, null);
        
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void shouldHandleEmptyMessage() {
        String message = "";
        InsufficientInventoryException exception = new InsufficientInventoryException(message);
        
        assertEquals(message, exception.getMessage());
    }

    @Test
    void shouldHandleDetailedMessage() {
        String message = "Cannot reduce inventory for product SKU001. Required: 10, Available: 5";
        InsufficientInventoryException exception = new InsufficientInventoryException(message);
        
        assertEquals(message, exception.getMessage());
        assertTrue(exception.getMessage().contains("SKU001"));
        assertTrue(exception.getMessage().contains("Required: 10"));
        assertTrue(exception.getMessage().contains("Available: 5"));
    }
} 