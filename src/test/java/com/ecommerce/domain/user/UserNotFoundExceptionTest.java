package com.ecommerce.domain.user;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UserNotFoundExceptionTest {

    @Test
    void shouldCreateExceptionWithMessage() {
        String message = "User not found";
        UserNotFoundException exception = new UserNotFoundException(message);
        
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void shouldCreateExceptionWithUserId() {
        Long userId = 123L;
        UserNotFoundException exception = new UserNotFoundException(userId);
        
        assertEquals("User not found with id: 123", exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void shouldCreateExceptionWithNullUserId() {
        Long userId = null;
        UserNotFoundException exception = new UserNotFoundException(userId);
        
        assertEquals("User not found with id: null", exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void shouldCreateExceptionWithZeroUserId() {
        Long userId = 0L;
        UserNotFoundException exception = new UserNotFoundException(userId);
        
        assertEquals("User not found with id: 0", exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void shouldCreateExceptionWithNegativeUserId() {
        Long userId = -1L;
        UserNotFoundException exception = new UserNotFoundException(userId);
        
        assertEquals("User not found with id: -1", exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void shouldCreateExceptionWithLargeUserId() {
        Long userId = Long.MAX_VALUE;
        UserNotFoundException exception = new UserNotFoundException(userId);
        
        assertEquals("User not found with id: " + Long.MAX_VALUE, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void shouldCreateExceptionWithNullMessage() {
        String message = null;
        UserNotFoundException exception = new UserNotFoundException(message);
        
        assertNull(exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void shouldCreateExceptionWithEmptyMessage() {
        String message = "";
        UserNotFoundException exception = new UserNotFoundException(message);
        
        assertEquals("", exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void shouldCreateExceptionWithWhitespaceMessage() {
        String message = "   ";
        UserNotFoundException exception = new UserNotFoundException(message);
        
        assertEquals("   ", exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void shouldBeRuntimeException() {
        UserNotFoundException exception = new UserNotFoundException("Test message");
        
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void shouldCreateExceptionWithSpecialCharactersInMessage() {
        String message = "User not found: !@#$%^&*()";
        UserNotFoundException exception = new UserNotFoundException(message);
        
        assertEquals(message, exception.getMessage());
    }

    @Test
    void shouldCreateExceptionWithLongMessage() {
        String message = "A".repeat(1000);
        UserNotFoundException exception = new UserNotFoundException(message);
        
        assertEquals(message, exception.getMessage());
    }

    @Test
    void shouldCreateExceptionWithUnicodeCharacters() {
        String message = "用户未找到";
        UserNotFoundException exception = new UserNotFoundException(message);
        
        assertEquals(message, exception.getMessage());
    }

    @Test
    void shouldCreateExceptionWithNewlineCharacters() {
        String message = "User not found\nPlease check the user ID";
        UserNotFoundException exception = new UserNotFoundException(message);
        
        assertEquals(message, exception.getMessage());
    }

    @Test
    void shouldCreateExceptionWithTabCharacters() {
        String message = "User not found\tID: 123";
        UserNotFoundException exception = new UserNotFoundException(message);
        
        assertEquals(message, exception.getMessage());
    }

    @Test
    void shouldInheritFromRuntimeException() {
        UserNotFoundException exception = new UserNotFoundException("Test");
        
        // Test that it inherits RuntimeException behavior
        assertTrue(exception instanceof RuntimeException);
        
        // Test that it can be caught as RuntimeException
        try {
            throw exception;
        } catch (RuntimeException e) {
            assertEquals("Test", e.getMessage());
            assertTrue(e instanceof UserNotFoundException);
        }
    }

    @Test
    void shouldHaveConsistentBehaviorBetweenConstructors() {
        Long userId = 456L;
        String expectedMessage = "User not found with id: 456";
        
        UserNotFoundException exception1 = new UserNotFoundException(userId);
        UserNotFoundException exception2 = new UserNotFoundException(expectedMessage);
        
        assertEquals(exception1.getMessage(), exception2.getMessage());
    }

    @Test
    void shouldHandleMinLongValue() {
        Long userId = Long.MIN_VALUE;
        UserNotFoundException exception = new UserNotFoundException(userId);
        
        assertEquals("User not found with id: " + Long.MIN_VALUE, exception.getMessage());
    }

    @Test
    void shouldCreateExceptionWithComplexMessage() {
        String message = "User not found: ID=123, Username=john_doe, Email=john@example.com";
        UserNotFoundException exception = new UserNotFoundException(message);
        
        assertEquals(message, exception.getMessage());
    }
} 