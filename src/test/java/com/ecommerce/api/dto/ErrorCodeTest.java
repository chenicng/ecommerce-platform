package com.ecommerce.api.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ErrorCodeTest {
    
    @Test
    void getCode_ShouldReturnCorrectCode() {
        // Test all error codes
        assertEquals("SUCCESS", ErrorCode.SUCCESS.getCode());
        assertEquals("VALIDATION_ERROR", ErrorCode.VALIDATION_ERROR.getCode());
        assertEquals("BIND_ERROR", ErrorCode.BIND_ERROR.getCode());
        assertEquals("RESOURCE_NOT_FOUND", ErrorCode.RESOURCE_NOT_FOUND.getCode());
        assertEquals("INSUFFICIENT_BALANCE", ErrorCode.INSUFFICIENT_BALANCE.getCode());
        assertEquals("INSUFFICIENT_INVENTORY", ErrorCode.INSUFFICIENT_INVENTORY.getCode());
        assertEquals("INSUFFICIENT_FUNDS", ErrorCode.INSUFFICIENT_FUNDS.getCode());
        assertEquals("RESOURCE_ALREADY_EXISTS", ErrorCode.RESOURCE_ALREADY_EXISTS.getCode());
        assertEquals("RESOURCE_INACTIVE", ErrorCode.RESOURCE_INACTIVE.getCode());
        assertEquals("OPERATION_NOT_ALLOWED", ErrorCode.OPERATION_NOT_ALLOWED.getCode());
        assertEquals("BUSINESS_ERROR", ErrorCode.BUSINESS_ERROR.getCode());
        assertEquals("INTERNAL_ERROR", ErrorCode.INTERNAL_ERROR.getCode());
        assertEquals("UNSUPPORTED_API_VERSION", ErrorCode.UNSUPPORTED_API_VERSION.getCode());
    }
    
    @Test
    void getDefaultMessage_ShouldReturnCorrectMessage() {
        // Test all default messages
        assertEquals("Operation completed successfully", ErrorCode.SUCCESS.getDefaultMessage());
        assertEquals("Request validation failed", ErrorCode.VALIDATION_ERROR.getDefaultMessage());
        assertEquals("Request binding failed", ErrorCode.BIND_ERROR.getDefaultMessage());
        assertEquals("Requested resource not found", ErrorCode.RESOURCE_NOT_FOUND.getDefaultMessage());
        assertEquals("Insufficient user balance", ErrorCode.INSUFFICIENT_BALANCE.getDefaultMessage());
        assertEquals("Insufficient product inventory", ErrorCode.INSUFFICIENT_INVENTORY.getDefaultMessage());
        assertEquals("Insufficient merchant funds", ErrorCode.INSUFFICIENT_FUNDS.getDefaultMessage());
        assertEquals("Resource already exists", ErrorCode.RESOURCE_ALREADY_EXISTS.getDefaultMessage());
        assertEquals("Resource is not active", ErrorCode.RESOURCE_INACTIVE.getDefaultMessage());
        assertEquals("Operation not allowed", ErrorCode.OPERATION_NOT_ALLOWED.getDefaultMessage());
        assertEquals("Business logic error", ErrorCode.BUSINESS_ERROR.getDefaultMessage());
        assertEquals("Internal server error", ErrorCode.INTERNAL_ERROR.getDefaultMessage());
        assertEquals("API version not supported", ErrorCode.UNSUPPORTED_API_VERSION.getDefaultMessage());
    }
    
    @Test
    void fromMessage_WithNullMessage_ShouldReturnInternalError() {
        // Act
        ErrorCode result = ErrorCode.fromMessage(null);
        
        // Assert
        assertEquals(ErrorCode.INTERNAL_ERROR, result);
    }
    
    @Test
    void fromMessage_WithEmptyMessage_ShouldReturnInternalError() {
        // Act
        ErrorCode result = ErrorCode.fromMessage("");
        
        // Assert
        assertEquals(ErrorCode.INTERNAL_ERROR, result);
    }
    
    @Test
    void fromMessage_WithNotFoundMessage_ShouldReturnResourceNotFound() {
        // Test various "not found" patterns
        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ErrorCode.fromMessage("User not found"));
        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ErrorCode.fromMessage("Product not found"));
        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ErrorCode.fromMessage("Order not found"));
        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ErrorCode.fromMessage("NOT FOUND"));
        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ErrorCode.fromMessage("not found"));
    }
    
    @Test
    void fromMessage_WithInsufficientBalanceMessage_ShouldReturnInsufficientBalance() {
        // Test various "insufficient balance" patterns
        assertEquals(ErrorCode.INSUFFICIENT_BALANCE, ErrorCode.fromMessage("Insufficient balance"));
        assertEquals(ErrorCode.INSUFFICIENT_BALANCE, ErrorCode.fromMessage("insufficient balance"));
        assertEquals(ErrorCode.INSUFFICIENT_BALANCE, ErrorCode.fromMessage("INSUFFICIENT BALANCE"));
        assertEquals(ErrorCode.INSUFFICIENT_BALANCE, ErrorCode.fromMessage("User has insufficient balance"));
    }
    
    @Test
    void fromMessage_WithInsufficientInventoryMessage_ShouldReturnInsufficientInventory() {
        // Test various "insufficient inventory" patterns
        assertEquals(ErrorCode.INSUFFICIENT_INVENTORY, ErrorCode.fromMessage("Insufficient inventory"));
        assertEquals(ErrorCode.INSUFFICIENT_INVENTORY, ErrorCode.fromMessage("insufficient inventory"));
        assertEquals(ErrorCode.INSUFFICIENT_INVENTORY, ErrorCode.fromMessage("INSUFFICIENT INVENTORY"));
        assertEquals(ErrorCode.INSUFFICIENT_INVENTORY, ErrorCode.fromMessage("Product has insufficient inventory"));
    }
    
    @Test
    void fromMessage_WithInsufficientFundsMessage_ShouldReturnInsufficientFunds() {
        // Test various "insufficient funds" patterns
        assertEquals(ErrorCode.INSUFFICIENT_FUNDS, ErrorCode.fromMessage("Insufficient funds"));
        assertEquals(ErrorCode.INSUFFICIENT_FUNDS, ErrorCode.fromMessage("insufficient funds"));
        assertEquals(ErrorCode.INSUFFICIENT_FUNDS, ErrorCode.fromMessage("INSUFFICIENT FUNDS"));
        assertEquals(ErrorCode.INSUFFICIENT_FUNDS, ErrorCode.fromMessage("Merchant has insufficient funds"));
    }
    
    @Test
    void fromMessage_WithAlreadyExistsMessage_ShouldReturnResourceAlreadyExists() {
        // Test various "already exists" patterns
        assertEquals(ErrorCode.RESOURCE_ALREADY_EXISTS, ErrorCode.fromMessage("User already exists"));
        assertEquals(ErrorCode.RESOURCE_ALREADY_EXISTS, ErrorCode.fromMessage("already exists"));
        assertEquals(ErrorCode.RESOURCE_ALREADY_EXISTS, ErrorCode.fromMessage("ALREADY EXISTS"));
        assertEquals(ErrorCode.RESOURCE_ALREADY_EXISTS, ErrorCode.fromMessage("Product already exists"));
    }
    
    @Test
    void fromMessage_WithNotActiveMessage_ShouldReturnResourceInactive() {
        // Test various "not active" patterns
        assertEquals(ErrorCode.RESOURCE_INACTIVE, ErrorCode.fromMessage("User is not active"));
        assertEquals(ErrorCode.RESOURCE_INACTIVE, ErrorCode.fromMessage("not active"));
        assertEquals(ErrorCode.RESOURCE_INACTIVE, ErrorCode.fromMessage("NOT ACTIVE"));
        assertEquals(ErrorCode.RESOURCE_INACTIVE, ErrorCode.fromMessage("Account is not active"));
    }
    
    @Test
    void fromMessage_WithCannotCancelMessage_ShouldReturnOperationNotAllowed() {
        // Test various "cannot cancel" patterns
        assertEquals(ErrorCode.OPERATION_NOT_ALLOWED, ErrorCode.fromMessage("Cannot cancel this order"));
        assertEquals(ErrorCode.OPERATION_NOT_ALLOWED, ErrorCode.fromMessage("cannot cancel"));
        assertEquals(ErrorCode.OPERATION_NOT_ALLOWED, ErrorCode.fromMessage("CANNOT CANCEL"));
    }
    
    @Test
    void fromMessage_WithGenericMessage_ShouldReturnInternalError() {
        // Test generic messages that don't match any specific pattern
        assertEquals(ErrorCode.INTERNAL_ERROR, ErrorCode.fromMessage("Some random error"));
        assertEquals(ErrorCode.INTERNAL_ERROR, ErrorCode.fromMessage("Validation failed"));
        assertEquals(ErrorCode.INTERNAL_ERROR, ErrorCode.fromMessage("Database connection failed"));
        assertEquals(ErrorCode.INTERNAL_ERROR, ErrorCode.fromMessage("Unknown error occurred"));
        assertEquals(ErrorCode.INTERNAL_ERROR, ErrorCode.fromMessage(""));
        assertEquals(ErrorCode.INTERNAL_ERROR, ErrorCode.fromMessage("   "));
    }
    
    @Test
    void fromMessage_WithCaseInsensitiveMatching_ShouldWorkCorrectly() {
        // Test case insensitive matching
        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ErrorCode.fromMessage("USER NOT FOUND"));
        assertEquals(ErrorCode.INSUFFICIENT_BALANCE, ErrorCode.fromMessage("INSUFFICIENT BALANCE"));
        assertEquals(ErrorCode.INSUFFICIENT_INVENTORY, ErrorCode.fromMessage("insufficient inventory"));
        assertEquals(ErrorCode.INSUFFICIENT_FUNDS, ErrorCode.fromMessage("InSuFfIcIeNt FuNdS"));
        assertEquals(ErrorCode.RESOURCE_ALREADY_EXISTS, ErrorCode.fromMessage("Already Exists"));
        assertEquals(ErrorCode.RESOURCE_INACTIVE, ErrorCode.fromMessage("Not Active"));
        assertEquals(ErrorCode.OPERATION_NOT_ALLOWED, ErrorCode.fromMessage("Cannot Cancel"));
    }
    
    @Test
    void fromMessage_WithPartialMatches_ShouldWorkCorrectly() {
        // Test partial matches within messages
        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ErrorCode.fromMessage("The requested user was not found in the system"));
        assertEquals(ErrorCode.INSUFFICIENT_BALANCE, ErrorCode.fromMessage("Transaction failed due to insufficient balance"));
        assertEquals(ErrorCode.INSUFFICIENT_INVENTORY, ErrorCode.fromMessage("Cannot process order - insufficient inventory available"));
        assertEquals(ErrorCode.INSUFFICIENT_FUNDS, ErrorCode.fromMessage("Withdrawal failed: insufficient funds in account"));
        assertEquals(ErrorCode.RESOURCE_ALREADY_EXISTS, ErrorCode.fromMessage("Registration failed: user already exists in database"));
        assertEquals(ErrorCode.RESOURCE_INACTIVE, ErrorCode.fromMessage("Login failed: account is not active"));
        assertEquals(ErrorCode.OPERATION_NOT_ALLOWED, ErrorCode.fromMessage("Action denied: cannot cancel this order"));
    }
    
    @Test
    void enumValues_ShouldBeAccessible() {
        // Test that all enum values are accessible
        ErrorCode[] values = ErrorCode.values();
        assertNotNull(values);
        assertTrue(values.length > 0);
        
        // Verify specific values exist
        assertNotNull(ErrorCode.valueOf("SUCCESS"));
        assertNotNull(ErrorCode.valueOf("VALIDATION_ERROR"));
        assertNotNull(ErrorCode.valueOf("BUSINESS_ERROR"));
        assertNotNull(ErrorCode.valueOf("INTERNAL_ERROR"));
    }
    
    @Test
    void enumEquality_ShouldWorkCorrectly() {
        // Test enum equality
        ErrorCode success1 = ErrorCode.SUCCESS;
        ErrorCode success2 = ErrorCode.SUCCESS;
        ErrorCode business = ErrorCode.BUSINESS_ERROR;
        
        assertEquals(success1, success2);
        assertNotEquals(success1, business);
        assertSame(success1, success2);
        assertNotSame(success1, business);
    }
    
    @Test
    void debug_FromMessage_WithCannotCancelMessage() {
        // Debug test to see what's happening
        String message = "Cannot cancel this order";
        ErrorCode result = ErrorCode.fromMessage(message);
        System.out.println("Message: '" + message + "'");
        System.out.println("Lower message: '" + message.toLowerCase() + "'");
        System.out.println("Contains 'cannot cancel': " + message.toLowerCase().contains("cannot cancel"));
        System.out.println("Result: " + result);
        System.out.println("Expected: " + ErrorCode.OPERATION_NOT_ALLOWED);
    }
} 