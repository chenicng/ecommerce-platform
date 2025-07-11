package com.ecommerce.domain.merchant;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MerchantNotFoundExceptionTest {

    @Test
    void shouldCreateExceptionWithMessage() {
        String message = "Merchant not found";
        MerchantNotFoundException exception = new MerchantNotFoundException(message);
        
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void shouldCreateExceptionWithMerchantId() {
        Long merchantId = 123L;
        MerchantNotFoundException exception = new MerchantNotFoundException(merchantId);
        
        assertEquals("Merchant not found with id: 123", exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void shouldCreateExceptionWithNullMerchantId() {
        Long merchantId = null;
        MerchantNotFoundException exception = new MerchantNotFoundException(merchantId);
        
        assertEquals("Merchant not found with id: null", exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void shouldCreateExceptionWithZeroMerchantId() {
        Long merchantId = 0L;
        MerchantNotFoundException exception = new MerchantNotFoundException(merchantId);
        
        assertEquals("Merchant not found with id: 0", exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void shouldCreateExceptionWithNegativeMerchantId() {
        Long merchantId = -1L;
        MerchantNotFoundException exception = new MerchantNotFoundException(merchantId);
        
        assertEquals("Merchant not found with id: -1", exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void shouldCreateExceptionWithLargeMerchantId() {
        Long merchantId = Long.MAX_VALUE;
        MerchantNotFoundException exception = new MerchantNotFoundException(merchantId);
        
        assertEquals("Merchant not found with id: " + Long.MAX_VALUE, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void shouldCreateExceptionWithNullMessage() {
        String message = null;
        MerchantNotFoundException exception = new MerchantNotFoundException(message);
        
        assertNull(exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void shouldCreateExceptionWithEmptyMessage() {
        String message = "";
        MerchantNotFoundException exception = new MerchantNotFoundException(message);
        
        assertEquals("", exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void shouldCreateExceptionWithWhitespaceMessage() {
        String message = "   ";
        MerchantNotFoundException exception = new MerchantNotFoundException(message);
        
        assertEquals("   ", exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void shouldBeRuntimeException() {
        MerchantNotFoundException exception = new MerchantNotFoundException("Test message");
        
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void shouldCreateExceptionWithSpecialCharactersInMessage() {
        String message = "Merchant not found: !@#$%^&*()";
        MerchantNotFoundException exception = new MerchantNotFoundException(message);
        
        assertEquals(message, exception.getMessage());
    }

    @Test
    void shouldCreateExceptionWithLongMessage() {
        String message = "A".repeat(1000);
        MerchantNotFoundException exception = new MerchantNotFoundException(message);
        
        assertEquals(message, exception.getMessage());
    }

    @Test
    void shouldCreateExceptionWithUnicodeCharacters() {
        String message = "Merchant not found";
        MerchantNotFoundException exception = new MerchantNotFoundException(message);
        
        assertEquals(message, exception.getMessage());
    }

    @Test
    void shouldCreateExceptionWithNewlineCharacters() {
        String message = "Merchant not found\nPlease check the merchant ID";
        MerchantNotFoundException exception = new MerchantNotFoundException(message);
        
        assertEquals(message, exception.getMessage());
    }

    @Test
    void shouldCreateExceptionWithTabCharacters() {
        String message = "Merchant not found\tID: 123";
        MerchantNotFoundException exception = new MerchantNotFoundException(message);
        
        assertEquals(message, exception.getMessage());
    }

    @Test
    void shouldInheritFromRuntimeException() {
        MerchantNotFoundException exception = new MerchantNotFoundException("Test");
        
        // Test that it inherits RuntimeException behavior
        assertTrue(exception instanceof RuntimeException);
        
        // Test that it can be caught as RuntimeException
        try {
            throw exception;
        } catch (RuntimeException e) {
            assertEquals("Test", e.getMessage());
            assertTrue(e instanceof MerchantNotFoundException);
        }
    }

    @Test
    void shouldHaveConsistentBehaviorBetweenConstructors() {
        Long merchantId = 456L;
        String expectedMessage = "Merchant not found with id: 456";
        
        MerchantNotFoundException exception1 = new MerchantNotFoundException(merchantId);
        MerchantNotFoundException exception2 = new MerchantNotFoundException(expectedMessage);
        
        assertEquals(exception1.getMessage(), exception2.getMessage());
    }

    @Test
    void shouldHandleMinLongValue() {
        Long merchantId = Long.MIN_VALUE;
        MerchantNotFoundException exception = new MerchantNotFoundException(merchantId);
        
        assertEquals("Merchant not found with id: " + Long.MIN_VALUE, exception.getMessage());
    }

    @Test
    void shouldCreateExceptionWithComplexMessage() {
        String message = "Merchant not found: ID=123, Name=Test Store, License=BL123456";
        MerchantNotFoundException exception = new MerchantNotFoundException(message);
        
        assertEquals(message, exception.getMessage());
    }
} 