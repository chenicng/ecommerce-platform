package com.ecommerce.api.dto;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ResultTest {

    @Test
    void debugTest() {
        // Debug test to see what's happening
        String data = "test data";
        Result<String> result = Result.success(data);
        
        System.out.println("Expected message: " + ErrorCode.SUCCESS.getDefaultMessage());
        System.out.println("Actual message: " + result.getMessage());
        System.out.println("Expected data: " + data);
        System.out.println("Actual data: " + result.getData());
        System.out.println("Code: " + result.getCode());
    }

    @Test
    void testSuccessWithData() {
        // Given
        String data = "test data";
        
        // When
        Result<String> result = Result.success(data);
        
        // Then
        assertEquals(ErrorCode.SUCCESS.getCode(), result.getCode());
        assertEquals(ErrorCode.SUCCESS.getDefaultMessage(), result.getMessage());
        assertEquals(data, result.getData());
        assertNotNull(result.getTimestamp());
        assertTrue(result.isSuccess());
        assertFalse(result.isError());
    }

    @Test
    void testSuccessWithMessage() {
        // Given
        String message = "Custom success message";
        String data = "test data";
        
        // When
        Result<String> result = Result.successWithMessage(message, data);
        
        // Then
        assertEquals(ErrorCode.SUCCESS.getCode(), result.getCode());
        assertEquals(message, result.getMessage());
        assertEquals(data, result.getData());
        assertNotNull(result.getTimestamp());
        assertTrue(result.isSuccess());
        assertFalse(result.isError());
    }

    @Test
    void testSuccessWithoutData() {
        // When
        Result<Void> result = Result.success();
        
        // Then
        assertEquals(ErrorCode.SUCCESS.getCode(), result.getCode());
        assertEquals(ErrorCode.SUCCESS.getDefaultMessage(), result.getMessage());
        assertNull(result.getData());
        assertNotNull(result.getTimestamp());
        assertTrue(result.isSuccess());
        assertFalse(result.isError());
    }

    @Test
    void testSuccessWithMessageOnly() {
        // Given
        String message = "Custom success message";
        
        // When
        Result<Void> result = Result.successWithMessage(message);
        
        // Then
        assertEquals(ErrorCode.SUCCESS.getCode(), result.getCode());
        assertEquals(message, result.getMessage());
        assertNull(result.getData());
        assertNotNull(result.getTimestamp());
        assertTrue(result.isSuccess());
        assertFalse(result.isError());
    }

    @Test
    void testErrorWithErrorCodeAndMessage() {
        // Given
        ErrorCode errorCode = ErrorCode.VALIDATION_ERROR;
        String message = "Validation failed";
        
        // When
        Result<String> result = Result.error(errorCode, message);
        
        // Then
        assertEquals(errorCode.getCode(), result.getCode());
        assertEquals(message, result.getMessage());
        assertNull(result.getData());
        assertNotNull(result.getTimestamp());
        assertFalse(result.isSuccess());
        assertTrue(result.isError());
    }

    @Test
    void testErrorWithErrorCodeAndMessageAndData() {
        // Given
        ErrorCode errorCode = ErrorCode.BUSINESS_ERROR;
        String message = "Business logic error";
        String data = "error details";
        
        // When
        Result<String> result = Result.error(errorCode, message, data);
        
        // Then
        assertEquals(errorCode.getCode(), result.getCode());
        assertEquals(message, result.getMessage());
        assertEquals(data, result.getData());
        assertNotNull(result.getTimestamp());
        assertFalse(result.isSuccess());
        assertTrue(result.isError());
    }

    @Test
    void testErrorWithErrorCodeOnly() {
        // Given
        ErrorCode errorCode = ErrorCode.INTERNAL_ERROR;
        
        // When
        Result<String> result = Result.error(errorCode);
        
        // Then
        assertEquals(errorCode.getCode(), result.getCode());
        assertEquals(errorCode.getDefaultMessage(), result.getMessage());
        assertNull(result.getData());
        assertNotNull(result.getTimestamp());
        assertFalse(result.isSuccess());
        assertTrue(result.isError());
    }

    @Test
    void testErrorWithMessage() {
        // Given
        String message = "Custom error message";
        
        // When
        Result<String> result = Result.error(message);
        
        // Then
        assertEquals(ErrorCode.INTERNAL_ERROR.getCode(), result.getCode());
        assertEquals(message, result.getMessage());
        assertNull(result.getData());
        assertFalse(result.isSuccess());
        assertTrue(result.isError());
    }

    @Test
    void testErrorWithInsufficientBalanceMessage() {
        // Given
        String message = "Insufficient balance";
        
        // When
        Result<String> result = Result.error(message);
        
        // Then
        assertEquals(ErrorCode.INSUFFICIENT_BALANCE.getCode(), result.getCode());
        assertEquals(message, result.getMessage());
        assertNull(result.getData());
        assertFalse(result.isSuccess());
        assertTrue(result.isError());
    }

    @Test
    void testErrorWithInsufficientInventoryMessage() {
        // Given
        String message = "Insufficient inventory";
        
        // When
        Result<String> result = Result.error(message);
        
        // Then
        assertEquals(ErrorCode.INSUFFICIENT_INVENTORY.getCode(), result.getCode());
        assertEquals(message, result.getMessage());
        assertNull(result.getData());
        assertFalse(result.isSuccess());
        assertTrue(result.isError());
    }

    @Test
    void testErrorWithInsufficientFundsMessage() {
        // Given
        String message = "Insufficient funds";
        
        // When
        Result<String> result = Result.error(message);
        
        // Then
        assertEquals(ErrorCode.INSUFFICIENT_FUNDS.getCode(), result.getCode());
        assertEquals(message, result.getMessage());
        assertNull(result.getData());
        assertFalse(result.isSuccess());
        assertTrue(result.isError());
    }

    @Test
    void testErrorWithUnknownMessage() {
        // Given
        String message = "Unknown error occurred";
        
        // When
        Result<String> result = Result.error(message);
        
        // Then
        assertEquals(ErrorCode.INTERNAL_ERROR.getCode(), result.getCode());
        assertEquals(message, result.getMessage());
        assertNull(result.getData());
        assertFalse(result.isSuccess());
        assertTrue(result.isError());
    }

    @Test
    void testErrorWithNullMessage() {
        // Given
        String message = null;
        
        // When
        Result<String> result = Result.error(message);
        
        // Then
        assertEquals(ErrorCode.INTERNAL_ERROR.getCode(), result.getCode());
        assertNull(result.getMessage());
        assertNull(result.getData());
        assertFalse(result.isSuccess());
        assertTrue(result.isError());
    }

    @Test
    void testErrorWithEmptyMessage() {
        // Given
        String message = "";
        
        // When
        Result<String> result = Result.error(message);
        
        // Then
        assertEquals(ErrorCode.INTERNAL_ERROR.getCode(), result.getCode());
        assertEquals(message, result.getMessage());
        assertNull(result.getData());
        assertFalse(result.isSuccess());
        assertTrue(result.isError());
    }

    @Test
    void testSuccessWithNullData() {
        // When
        Result<String> result = Result.success((String) null);
        
        // Then
        assertEquals(ErrorCode.SUCCESS.getCode(), result.getCode());
        assertEquals(ErrorCode.SUCCESS.getDefaultMessage(), result.getMessage());
        assertNull(result.getData());
        assertNotNull(result.getTimestamp());
        assertTrue(result.isSuccess());
        assertFalse(result.isError());
    }

    @Test
    void testSuccessWithComplexData() {
        // Given
        Map<String, Object> complexData = new HashMap<>();
        complexData.put("id", 1L);
        complexData.put("name", "Test User");
        complexData.put("active", true);
        complexData.put("roles", new String[]{"USER", "ADMIN"});
        
        // When
        Result<Map<String, Object>> result = Result.success(complexData);
        
        // Then
        assertEquals(ErrorCode.SUCCESS.getCode(), result.getCode());
        assertEquals(ErrorCode.SUCCESS.getDefaultMessage(), result.getMessage());
        assertEquals(complexData, result.getData());
        assertNotNull(result.getTimestamp());
        assertTrue(result.isSuccess());
        assertFalse(result.isError());
    }

    @Test
    void testErrorWithAllErrorCodes() {
        // Test all error codes
        ErrorCode[] errorCodes = ErrorCode.values();
        
        for (ErrorCode errorCode : errorCodes) {
            if (errorCode != ErrorCode.SUCCESS) {
                Result<String> result = Result.error(errorCode, "Test message for " + errorCode.name());
                
                assertEquals(errorCode.getCode(), result.getCode());
                assertEquals("Test message for " + errorCode.name(), result.getMessage());
                assertNull(result.getData());
                assertNotNull(result.getTimestamp());
                assertFalse(result.isSuccess());
                assertTrue(result.isError());
            }
        }
    }

    @Test
    void testTimestampConsistency() {
        // Given
        LocalDateTime before = LocalDateTime.now();
        
        // When
        Result<String> result = Result.success("test");
        LocalDateTime after = LocalDateTime.now();
        
        // Then
        assertTrue(result.getTimestamp().isAfter(before) || result.getTimestamp().equals(before));
        assertTrue(result.getTimestamp().isBefore(after) || result.getTimestamp().equals(after));
    }

    @Test
    void testToString() {
        // Given
        String data = "test data";
        Result<String> result = Result.success(data);
        
        // When
        String toString = result.toString();
        
        // Then
        assertTrue(toString.contains("Result"));
        assertTrue(toString.contains("code='SUCCESS'"));
        assertTrue(toString.contains("message='Operation completed successfully'"));
        assertTrue(toString.contains("data=test data"));
        assertTrue(toString.contains("timestamp="));
    }

    @Test
    void testToStringWithNullData() {
        // Given
        Result<String> result = Result.success((String) null);
        
        // When
        String toString = result.toString();
        
        // Then
        assertTrue(toString.contains("Result"));
        assertTrue(toString.contains("code='SUCCESS'"));
        assertTrue(toString.contains("data=null"));
    }

    @Test
    void testToStringWithError() {
        // Given
        Result<String> result = Result.error(ErrorCode.VALIDATION_ERROR, "Invalid input");
        
        // When
        String toString = result.toString();
        
        // Then
        assertTrue(toString.contains("Result"));
        assertTrue(toString.contains("code='VALIDATION_ERROR'"));
        assertTrue(toString.contains("message='Invalid input'"));
        assertTrue(toString.contains("data=null"));
    }

    @Test
    void testMultipleSuccessCalls() {
        // Test that multiple success calls work correctly
        Result<String> result1 = Result.success("data1");
        Result<Integer> result2 = Result.success(42);
        Result<Void> result3 = Result.success();
        
        assertTrue(result1.isSuccess());
        assertTrue(result2.isSuccess());
        assertTrue(result3.isSuccess());
        
        assertEquals("data1", result1.getData());
        assertEquals(42, result2.getData());
        assertNull(result3.getData());
    }

    @Test
    void testMultipleErrorCalls() {
        // Test that multiple error calls work correctly
        Result<String> result1 = Result.error(ErrorCode.VALIDATION_ERROR, "Error 1");
        Result<String> result2 = Result.error(ErrorCode.BUSINESS_ERROR, "Error 2");
        Result<String> result3 = Result.error("Custom error message");
        
        assertTrue(result1.isError());
        assertTrue(result2.isError());
        assertTrue(result3.isError());
        
        assertEquals(ErrorCode.VALIDATION_ERROR.getCode(), result1.getCode());
        assertEquals(ErrorCode.BUSINESS_ERROR.getCode(), result2.getCode());
        assertEquals(ErrorCode.INTERNAL_ERROR.getCode(), result3.getCode());
    }
} 