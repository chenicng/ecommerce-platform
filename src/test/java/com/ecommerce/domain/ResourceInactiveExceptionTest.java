package com.ecommerce.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ResourceInactiveExceptionTest {

    @Test
    void constructor_WithMessage_ShouldCreateExceptionWithMessage() {
        String message = "User is not active";
        
        ResourceInactiveException exception = new ResourceInactiveException(message);
        
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void constructor_WithMessageAndCause_ShouldCreateExceptionWithMessageAndCause() {
        String message = "Resource is inactive";
        RuntimeException cause = new RuntimeException("Root cause");
        
        ResourceInactiveException exception = new ResourceInactiveException(message, cause);
        
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void constructor_WithNullMessage_ShouldCreateExceptionWithNullMessage() {
        ResourceInactiveException exception = new ResourceInactiveException(null);
        
        assertNull(exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void constructor_WithEmptyMessage_ShouldCreateExceptionWithEmptyMessage() {
        String message = "";
        
        ResourceInactiveException exception = new ResourceInactiveException(message);
        
        assertEquals(message, exception.getMessage());
    }

    @Test
    void constructor_WithNullCause_ShouldCreateExceptionWithNullCause() {
        String message = "Test message";
        
        ResourceInactiveException exception = new ResourceInactiveException(message, null);
        
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void exceptionShouldBeInstanceOfRuntimeException() {
        ResourceInactiveException exception = new ResourceInactiveException("Test");
        
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void exceptionShouldAllowDetailedMessages() {
        String detailedMessage = "Product is not active: PHONE-001";
        
        ResourceInactiveException exception = new ResourceInactiveException(detailedMessage);
        
        assertEquals(detailedMessage, exception.getMessage());
        assertTrue(exception.getMessage().contains("PHONE-001"));
    }

    @Test
    void exceptionShouldSupportUserInactiveScenario() {
        String userMessage = "User is not active. User ID: 123";
        
        ResourceInactiveException exception = new ResourceInactiveException(userMessage);
        
        assertEquals(userMessage, exception.getMessage());
        assertTrue(exception.getMessage().contains("User ID: 123"));
    }
} 