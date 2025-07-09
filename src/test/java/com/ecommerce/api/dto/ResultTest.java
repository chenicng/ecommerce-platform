package com.ecommerce.api.dto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ResultTest {
    
    private Result<String> testResult;
    
    @BeforeEach
    void setUp() {
        testResult = Result.success("test data");
    }
    
    @Test
    void success_WithData_ShouldCreateSuccessResult() {
        // Arrange
        String data = "test data";
        
        // Act
        Result<Object> result = Result.success((Object) data);
        
        // Assert
        assertEquals(ErrorCode.SUCCESS.getCode(), result.getCode());
        assertEquals(ErrorCode.SUCCESS.getDefaultMessage(), result.getMessage());
        assertEquals(data, result.getData());
        assertNotNull(result.getTimestamp());
        assertTrue(result.isSuccess());
        assertFalse(result.isError());
    }
    
    @Test
    void success_WithMessageAndData_ShouldCreateSuccessResult() {
        // Arrange
        String message = "Custom success message";
        Map<String, Object> data = new HashMap<>();
        data.put("key", "value");
        
        // Act
        Result<Map<String, Object>> result = Result.successWithMessage(message, data);
        
        // Assert
        assertEquals(ErrorCode.SUCCESS.getCode(), result.getCode());
        assertEquals(message, result.getMessage());
        assertEquals(data, result.getData());
        assertNotNull(result.getTimestamp());
        assertTrue(result.isSuccess());
        assertFalse(result.isError());
    }
    
    @Test
    void success_WithoutData_ShouldCreateSuccessResult() {
        // Act
        Result<Void> result = Result.success();
        
        // Assert
        assertEquals(ErrorCode.SUCCESS.getCode(), result.getCode());
        assertEquals(ErrorCode.SUCCESS.getDefaultMessage(), result.getMessage());
        assertNull(result.getData());
        assertNotNull(result.getTimestamp());
        assertTrue(result.isSuccess());
        assertFalse(result.isError());
    }
    
    @Test
    void success_WithMessageOnly_ShouldCreateSuccessResult() {
        // Arrange
        String message = "Custom success message";
        
        // Act
        Result<Void> result = Result.success(message);
        
        // Assert
        assertEquals(ErrorCode.SUCCESS.getCode(), result.getCode());
        assertEquals(message, result.getMessage());
        assertNull(result.getData());
        assertNotNull(result.getTimestamp());
        assertTrue(result.isSuccess());
        assertFalse(result.isError());
    }
    
    @Test
    void error_WithErrorCodeAndMessage_ShouldCreateErrorResult() {
        // Arrange
        ErrorCode errorCode = ErrorCode.VALIDATION_ERROR;
        String message = "Validation failed";
        
        // Act
        Result<Void> result = Result.error(errorCode, message);
        
        // Assert
        assertEquals(errorCode.getCode(), result.getCode());
        assertEquals(message, result.getMessage());
        assertNull(result.getData());
        assertNotNull(result.getTimestamp());
        assertFalse(result.isSuccess());
        assertTrue(result.isError());
    }
    
    @Test
    void error_WithErrorCodeAndMessageAndData_ShouldCreateErrorResult() {
        // Arrange
        ErrorCode errorCode = ErrorCode.BUSINESS_ERROR;
        String message = "Business error occurred";
        String errorData = "Error details";
        
        // Act
        Result<String> result = Result.error(errorCode, message, errorData);
        
        // Assert
        assertEquals(errorCode.getCode(), result.getCode());
        assertEquals(message, result.getMessage());
        assertEquals(errorData, result.getData());
        assertNotNull(result.getTimestamp());
        assertFalse(result.isSuccess());
        assertTrue(result.isError());
    }
    
    @Test
    void error_WithErrorCodeOnly_ShouldCreateErrorResult() {
        // Arrange
        ErrorCode errorCode = ErrorCode.RESOURCE_NOT_FOUND;
        
        // Act
        Result<Void> result = Result.error(errorCode);
        
        // Assert
        assertEquals(errorCode.getCode(), result.getCode());
        assertEquals(errorCode.getDefaultMessage(), result.getMessage());
        assertNull(result.getData());
        assertNotNull(result.getTimestamp());
        assertFalse(result.isSuccess());
        assertTrue(result.isError());
    }
    
    @Test
    void error_WithMessageOnly_ShouldCreateErrorResult() {
        // Arrange
        String message = "User not found";
        
        // Act
        Result<Void> result = Result.error(message);
        
        // Assert
        assertEquals(ErrorCode.RESOURCE_NOT_FOUND.getCode(), result.getCode());
        assertEquals(message, result.getMessage());
        assertNull(result.getData());
        assertNotNull(result.getTimestamp());
        assertFalse(result.isSuccess());
        assertTrue(result.isError());
    }
    
    @Test
    void error_WithNullMessage_ShouldHandleGracefully() {
        // Act
        Result<Void> result = Result.error((String) null);
        
        // Assert
        assertEquals(ErrorCode.BUSINESS_ERROR.getCode(), result.getCode());
        assertNull(result.getMessage());
        assertNull(result.getData());
        assertNotNull(result.getTimestamp());
        assertFalse(result.isSuccess());
        assertTrue(result.isError());
    }
    
    @Test
    void getters_ShouldReturnCorrectValues() {
        // Arrange
        String message = "Test message";
        String data = "Test data";
        Result<String> result = Result.error(ErrorCode.BUSINESS_ERROR, message, data);
        
        // Act & Assert
        assertEquals(ErrorCode.BUSINESS_ERROR.getCode(), result.getCode());
        assertEquals(message, result.getMessage());
        assertEquals(data, result.getData());
        assertNotNull(result.getTimestamp());
    }
    
    @Test
    void isSuccess_WithSuccessCode_ShouldReturnTrue() {
        // Arrange
        Result<String> successResult = Result.success("data");
        
        // Act & Assert
        assertTrue(successResult.isSuccess());
        assertFalse(successResult.isError());
    }
    
    @Test
    void isSuccess_WithErrorCode_ShouldReturnFalse() {
        // Arrange
        Result<Void> errorResult = Result.error(ErrorCode.VALIDATION_ERROR, "Error");
        
        // Act & Assert
        assertFalse(errorResult.isSuccess());
        assertTrue(errorResult.isError());
    }
    
    @Test
    void isError_WithErrorCode_ShouldReturnTrue() {
        // Arrange
        Result<Void> errorResult = Result.error(ErrorCode.BUSINESS_ERROR, "Error");
        
        // Act & Assert
        assertTrue(errorResult.isError());
        assertFalse(errorResult.isSuccess());
    }
    
    @Test
    void isError_WithSuccessCode_ShouldReturnFalse() {
        // Arrange
        Result<String> successResult = Result.success("data");
        
        // Act & Assert
        assertFalse(successResult.isError());
        assertTrue(successResult.isSuccess());
    }
    
    @Test
    void toString_ShouldReturnFormattedString() {
        // Arrange
        Result<String> result = Result.success("test data");
        
        // Act
        String resultString = result.toString();
        
        // Assert
        assertTrue(resultString.contains("Result"));
        assertTrue(resultString.contains("SUCCESS"));
        assertTrue(resultString.contains("test data"));
        assertTrue(resultString.contains("timestamp"));
    }
    
    @Test
    void toString_WithNullData_ShouldHandleGracefully() {
        // Arrange
        Result<Void> result = Result.success();
        
        // Act
        String resultString = result.toString();
        
        // Assert
        assertTrue(resultString.contains("Result"));
        assertTrue(resultString.contains("SUCCESS"));
        assertTrue(resultString.contains("null"));
        assertTrue(resultString.contains("timestamp"));
    }
    
    @Test
    void timestamp_ShouldBeSetOnCreation() {
        // Arrange
        LocalDateTime beforeCreation = LocalDateTime.now();
        
        // Act
        Result<String> result = Result.success("data");
        LocalDateTime afterCreation = LocalDateTime.now();
        
        // Assert
        assertNotNull(result.getTimestamp());
        assertTrue(result.getTimestamp().isAfter(beforeCreation) || result.getTimestamp().isEqual(beforeCreation));
        assertTrue(result.getTimestamp().isBefore(afterCreation) || result.getTimestamp().isEqual(afterCreation));
    }
    
    @Test
    void error_WithDifferentErrorCodes_ShouldReturnCorrectCodes() {
        // Test different error codes
        Result<Void> validationError = Result.error(ErrorCode.VALIDATION_ERROR, "Validation failed");
        assertEquals(ErrorCode.VALIDATION_ERROR.getCode(), validationError.getCode());
        
        Result<Void> notFoundError = Result.error(ErrorCode.RESOURCE_NOT_FOUND, "Not found");
        assertEquals(ErrorCode.RESOURCE_NOT_FOUND.getCode(), notFoundError.getCode());
        
        Result<Void> businessError = Result.error(ErrorCode.BUSINESS_ERROR, "Business error");
        assertEquals(ErrorCode.BUSINESS_ERROR.getCode(), businessError.getCode());
        
        Result<Void> internalError = Result.error(ErrorCode.INTERNAL_ERROR, "Internal error");
        assertEquals(ErrorCode.INTERNAL_ERROR.getCode(), internalError.getCode());
    }
    
    @Test
    void error_WithMessageMatchingPatterns_ShouldReturnCorrectErrorCodes() {
        // Test error code detection from message patterns
        Result<Void> notFoundResult = Result.error("User not found");
        assertEquals(ErrorCode.RESOURCE_NOT_FOUND.getCode(), notFoundResult.getCode());
        
        Result<Void> insufficientBalanceResult = Result.error("Insufficient balance");
        assertEquals(ErrorCode.INSUFFICIENT_BALANCE.getCode(), insufficientBalanceResult.getCode());
        
        Result<Void> insufficientInventoryResult = Result.error("Insufficient inventory");
        assertEquals(ErrorCode.INSUFFICIENT_INVENTORY.getCode(), insufficientInventoryResult.getCode());
        
        Result<Void> insufficientFundsResult = Result.error("Insufficient funds");
        assertEquals(ErrorCode.INSUFFICIENT_FUNDS.getCode(), insufficientFundsResult.getCode());
        
        Result<Void> alreadyExistsResult = Result.error("User already exists");
        assertEquals(ErrorCode.RESOURCE_ALREADY_EXISTS.getCode(), alreadyExistsResult.getCode());
        
        Result<Void> inactiveResult = Result.error("User is not active");
        assertEquals(ErrorCode.RESOURCE_INACTIVE.getCode(), inactiveResult.getCode());
        
        Result<Void> notAllowedResult = Result.error("Cannot cancel this order");
        assertEquals(ErrorCode.OPERATION_NOT_ALLOWED.getCode(), notAllowedResult.getCode());
        
        Result<Void> genericResult = Result.error("Some other error");
        assertEquals(ErrorCode.BUSINESS_ERROR.getCode(), genericResult.getCode());
    }
    
    @Test
    void debug_SuccessDefaultMessage() {
        // Debug test to check the actual value
        System.out.println("ErrorCode.SUCCESS.getDefaultMessage(): '" + ErrorCode.SUCCESS.getDefaultMessage() + "'");
        System.out.println("ErrorCode.SUCCESS.getCode(): '" + ErrorCode.SUCCESS.getCode() + "'");
        
        Result<String> result = Result.success("test data");
        System.out.println("Result message: '" + result.getMessage() + "'");
        System.out.println("Result code: '" + result.getCode() + "'");
        System.out.println("Result data: '" + result.getData() + "'");
    }
    
    @Test
    void debug_ConstructorParameters() {
        // Debug test to check constructor parameters
        String code = ErrorCode.SUCCESS.getCode();
        String message = ErrorCode.SUCCESS.getDefaultMessage();
        String data = "test data";
        
        System.out.println("Constructor parameters:");
        System.out.println("  code: '" + code + "'");
        System.out.println("  message: '" + message + "'");
        System.out.println("  data: '" + data + "'");
        
        // Create Result using factory method
        Result<String> factoryResult = Result.success(data);
        System.out.println("Factory Result:");
        System.out.println("  code: '" + factoryResult.getCode() + "'");
        System.out.println("  message: '" + factoryResult.getMessage() + "'");
        System.out.println("  data: '" + factoryResult.getData() + "'");
    }
    
    @Test
    void debug_ConstructorOrder() {
        // Test constructor parameter order
        String code = "TEST_CODE";
        String message = "TEST_MESSAGE";
        String data = "TEST_DATA";
        
        // Use reflection to create Result with specific parameters
        try {
            java.lang.reflect.Constructor<Result> constructor = Result.class.getDeclaredConstructor(String.class, String.class, Object.class);
            constructor.setAccessible(true);
            Result<String> result = constructor.newInstance(code, message, data);
            
            System.out.println("Constructor test:");
            System.out.println("  Expected code: '" + code + "', Actual: '" + result.getCode() + "'");
            System.out.println("  Expected message: '" + message + "', Actual: '" + result.getMessage() + "'");
            System.out.println("  Expected data: '" + data + "', Actual: '" + result.getData() + "'");
        } catch (Exception e) {
            System.out.println("Reflection failed: " + e.getMessage());
        }
    }
} 