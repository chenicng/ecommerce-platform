package com.ecommerce.api.exception;

import com.ecommerce.api.dto.ErrorCode;
import com.ecommerce.api.dto.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import org.springframework.web.servlet.NoHandlerFoundException;

class GlobalExceptionHandlerTest {
    
    private GlobalExceptionHandler handler;
    private ObjectMapper objectMapper;
    private MethodParameter methodParameter;
    private WebRequest mockRequest;
    
    @BeforeEach
    void setUp() throws NoSuchMethodException {
        handler = new GlobalExceptionHandler();
        objectMapper = new ObjectMapper();
        
        // Create a mock MethodParameter
        Method method = TestController.class.getMethod("testMethod", String.class);
        methodParameter = new MethodParameter(method, 0);
        
        // Create a mock WebRequest
        mockRequest = new MockWebRequest();
    }
    
    // Helper class for creating MethodParameter
    private static class TestController {
        public void testMethod(String param) {}
    }
    
    // Mock WebRequest implementation
    private static class MockWebRequest implements WebRequest {
        @Override
        public String getDescription(boolean includeClientInfo) {
            return "Mock WebRequest";
        }
        
        // Implement other required methods with default implementations
        @Override
        public String getHeader(String headerName) { return null; }
        @Override
        public String[] getHeaderValues(String headerName) { return null; }
        @Override
        public java.util.Iterator<String> getHeaderNames() { return null; }
        @Override
        public String getParameter(String paramName) { return null; }
        @Override
        public String[] getParameterValues(String paramName) { return null; }
        @Override
        public java.util.Iterator<String> getParameterNames() { return null; }
        @Override
        public java.util.Map<String, String[]> getParameterMap() { return null; }
        @Override
        public java.util.Locale getLocale() { return null; }
        @Override
        public String getContextPath() { return null; }
        @Override
        public String getRemoteUser() { return null; }
        @Override
        public java.security.Principal getUserPrincipal() { return null; }
        @Override
        public boolean isUserInRole(String role) { return false; }
        @Override
        public boolean isSecure() { return false; }
        @Override
        public boolean checkNotModified(long lastModified) { return false; }
        @Override
        public boolean checkNotModified(String etag) { return false; }
        @Override
        public boolean checkNotModified(String etag, long lastModified) { return false; }
        @Override
        public String getSessionId() { return null; }
        @Override
        public Object getAttribute(String name, int scope) { return null; }
        @Override
        public void setAttribute(String name, Object value, int scope) {}
        @Override
        public void removeAttribute(String name, int scope) {}
        @Override
        public String[] getAttributeNames(int scope) { return null; }
        @Override
        public void registerDestructionCallback(String name, Runnable callback, int scope) {}
        @Override
        public Object resolveReference(String key) { return null; }
        @Override
        public String getSessionMutex() { return null; }
    }
    
    @Test
    void handleValidationException_WithUserIdError_ShouldReturnCustomMessage() {
        // Arrange
        FieldError fieldError = new FieldError("object", "userId", "must not be null");
        org.springframework.validation.BeanPropertyBindingResult bindingResult = 
            new org.springframework.validation.BeanPropertyBindingResult(new Object(), "object");
        bindingResult.addError(fieldError);
        
        // Create a proper MethodArgumentNotValidException
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(
            methodParameter, bindingResult
        );
        
        // Act
        ResponseEntity<Result<Void>> response = handler.handleValidationException(exception);
        
        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ErrorCode.VALIDATION_ERROR.getCode(), response.getBody().getCode());
        assertEquals("User ID is required", response.getBody().getMessage());
    }
    
    @Test
    void handleValidationException_WithSkuError_ShouldReturnCustomMessage() {
        // Arrange
        FieldError fieldError = new FieldError("object", "sku", "must not be blank");
        org.springframework.validation.BeanPropertyBindingResult bindingResult = 
            new org.springframework.validation.BeanPropertyBindingResult(new Object(), "object");
        bindingResult.addError(fieldError);
        
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(
            methodParameter, bindingResult
        );
        
        // Act
        ResponseEntity<Result<Void>> response = handler.handleValidationException(exception);
        
        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ErrorCode.VALIDATION_ERROR.getCode(), response.getBody().getCode());
        assertEquals("SKU is required", response.getBody().getMessage());
    }
    
    @Test
    void handleValidationException_WithQuantityError_ShouldReturnCustomMessage() {
        // Arrange
        FieldError fieldError = new FieldError("object", "quantity", "must be positive");
        org.springframework.validation.BeanPropertyBindingResult bindingResult = 
            new org.springframework.validation.BeanPropertyBindingResult(new Object(), "object");
        bindingResult.addError(fieldError);
        
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(
            methodParameter, bindingResult
        );
        
        // Act
        ResponseEntity<Result<Void>> response = handler.handleValidationException(exception);
        
        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ErrorCode.VALIDATION_ERROR.getCode(), response.getBody().getCode());
        assertEquals("Quantity must be positive", response.getBody().getMessage());
    }
    
    @Test
    void handleValidationException_WithMultipleErrors_ShouldConcatenateMessages() {
        // Arrange
        FieldError error1 = new FieldError("object", "userId", "must not be null");
        FieldError error2 = new FieldError("object", "sku", "must not be blank");
        org.springframework.validation.BeanPropertyBindingResult bindingResult = 
            new org.springframework.validation.BeanPropertyBindingResult(new Object(), "object");
        bindingResult.addError(error1);
        bindingResult.addError(error2);
        
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(
            methodParameter, bindingResult
        );
        
        // Act
        ResponseEntity<Result<Void>> response = handler.handleValidationException(exception);
        
        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ErrorCode.VALIDATION_ERROR.getCode(), response.getBody().getCode());
        assertTrue(response.getBody().getMessage().contains("User ID is required"));
        assertTrue(response.getBody().getMessage().contains("SKU is required"));
    }
    
    @Test
    void handleValidationException_WithGenericError_ShouldReturnOriginalMessage() {
        // Arrange
        FieldError fieldError = new FieldError("object", "email", "must be a valid email");
        org.springframework.validation.BeanPropertyBindingResult bindingResult = 
            new org.springframework.validation.BeanPropertyBindingResult(new Object(), "object");
        bindingResult.addError(fieldError);
        
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(
            methodParameter, bindingResult
        );
        
        // Act
        ResponseEntity<Result<Void>> response = handler.handleValidationException(exception);
        
        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ErrorCode.VALIDATION_ERROR.getCode(), response.getBody().getCode());
        assertEquals("must be a valid email", response.getBody().getMessage());
    }
    
    @Test
    void handleBindException_ShouldReturnFormattedError() {
        // Arrange
        FieldError fieldError = new FieldError("object", "name", "must not be null");
        BindException exception = new BindException(new Object(), "object");
        exception.addError(fieldError);
        
        // Act
        ResponseEntity<Result<String>> response = handler.handleBindException(exception);
        
        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ErrorCode.BIND_ERROR.getCode(), response.getBody().getCode());
        assertTrue(response.getBody().getMessage().contains("Request binding failed"));
        assertTrue(response.getBody().getMessage().contains("name: must not be null"));
    }
    
    @Test
    void handleBindException_WithMultipleErrors_ShouldConcatenateAll() {
        // Arrange
        FieldError error1 = new FieldError("object", "name", "must not be null");
        FieldError error2 = new FieldError("object", "age", "must be positive");
        BindException exception = new BindException(new Object(), "object");
        exception.addError(error1);
        exception.addError(error2);
        
        // Act
        ResponseEntity<Result<String>> response = handler.handleBindException(exception);
        
        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ErrorCode.BIND_ERROR.getCode(), response.getBody().getCode());
        assertTrue(response.getBody().getMessage().contains("name: must not be null"));
        assertTrue(response.getBody().getMessage().contains("age: must be positive"));
    }
    
    @Test
    void handleIllegalArgumentException_ShouldReturnValidationError() {
        // Arrange
        IllegalArgumentException exception = new IllegalArgumentException("Invalid input");
        
        // Act
        ResponseEntity<Result<Void>> response = handler.handleIllegalArgumentException(exception);
        
        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ErrorCode.VALIDATION_ERROR.getCode(), response.getBody().getCode());
        assertEquals("Invalid input", response.getBody().getMessage());
    }
    
    @Test
    void handleBusinessException_WithResourceNotFound_ShouldReturn404() {
        // Arrange
        RuntimeException exception = new RuntimeException("User not found");
        
        // Act
        ResponseEntity<Result<Void>> response = handler.handleBusinessException(exception, mockRequest);
        
        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ErrorCode.RESOURCE_NOT_FOUND.getCode(), response.getBody().getCode());
        assertEquals("User not found", response.getBody().getMessage());
    }
    
    @Test
    void handleBusinessException_WithInsufficientBalance_ShouldReturn400() {
        // Arrange
        RuntimeException exception = new RuntimeException("Insufficient balance");
        
        // Act
        ResponseEntity<Result<Void>> response = handler.handleBusinessException(exception, mockRequest);
        
        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ErrorCode.INSUFFICIENT_BALANCE.getCode(), response.getBody().getCode());
        assertEquals("Insufficient balance", response.getBody().getMessage());
    }
    
    @Test
    void handleBusinessException_WithInsufficientInventory_ShouldReturn400() {
        // Arrange
        RuntimeException exception = new RuntimeException("Insufficient inventory");
        
        // Act
        ResponseEntity<Result<Void>> response = handler.handleBusinessException(exception, mockRequest);
        
        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ErrorCode.INSUFFICIENT_INVENTORY.getCode(), response.getBody().getCode());
        assertEquals("Insufficient inventory", response.getBody().getMessage());
    }
    
    @Test
    void handleBusinessException_WithInsufficientFunds_ShouldReturn400() {
        // Arrange
        RuntimeException exception = new RuntimeException("Insufficient funds");
        
        // Act
        ResponseEntity<Result<Void>> response = handler.handleBusinessException(exception, mockRequest);
        
        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ErrorCode.INSUFFICIENT_FUNDS.getCode(), response.getBody().getCode());
        assertEquals("Insufficient funds", response.getBody().getMessage());
    }
    
    @Test
    void handleBusinessException_WithResourceAlreadyExists_ShouldReturn409() {
        // Arrange
        RuntimeException exception = new RuntimeException("User already exists");
        
        // Act
        ResponseEntity<Result<Void>> response = handler.handleBusinessException(exception, mockRequest);
        
        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ErrorCode.RESOURCE_ALREADY_EXISTS.getCode(), response.getBody().getCode());
        assertEquals("User already exists", response.getBody().getMessage());
    }
    
    @Test
    void handleBusinessException_WithResourceInactive_ShouldReturn403() {
        // Arrange
        RuntimeException exception = new RuntimeException("User is not active");
        
        // Act
        ResponseEntity<Result<Void>> response = handler.handleBusinessException(exception, mockRequest);
        
        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ErrorCode.RESOURCE_INACTIVE.getCode(), response.getBody().getCode());
        assertEquals("User is not active", response.getBody().getMessage());
    }
    
    @Test
    void handleBusinessException_WithOperationNotAllowed_ShouldReturn400() {
        // Arrange
        RuntimeException exception = new RuntimeException("Cannot cancel this order");
        
        // Act
        ResponseEntity<Result<Void>> response = handler.handleBusinessException(exception, mockRequest);
        
        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ErrorCode.OPERATION_NOT_ALLOWED.getCode(), response.getBody().getCode());
        assertEquals("Cannot cancel this order", response.getBody().getMessage());
    }
    
    @Test
    void handleBusinessException_WithGenericBusinessError_ShouldReturn400() {
        // Arrange
        RuntimeException exception = new RuntimeException("Some business error");
        
        // Act
        ResponseEntity<Result<Void>> response = handler.handleBusinessException(exception, mockRequest);
        
        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ErrorCode.BUSINESS_ERROR.getCode(), response.getBody().getCode());
        assertEquals("Some business error", response.getBody().getMessage());
    }
    
    @Test
    void handleUnexpectedException_ShouldReturn500() {
        // Arrange
        Exception exception = new Exception("Unexpected error");
        
        // Act
        ResponseEntity<Result<String>> response = handler.handleUnexpectedException(exception, mockRequest);
        
        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ErrorCode.INTERNAL_ERROR.getCode(), response.getBody().getCode());
        assertEquals("Internal server error occurred", response.getBody().getMessage());
    }
    
    @Test
    void handleUnexpectedException_WithNullException_ShouldHandleGracefully() {
        // Arrange
        Exception exception = null;
        
        // Act & Assert - should not throw exception
        assertDoesNotThrow(() -> {
            ResponseEntity<Result<String>> response = handler.handleUnexpectedException(exception, mockRequest);
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        });
    }
    
    @Test
    void handleNoHandlerFoundException_ShouldReturn404() {
        // Arrange
        NoHandlerFoundException exception = new NoHandlerFoundException(
            "GET", "/api/health1", new org.springframework.http.HttpHeaders()
        );
        
        // Act
        ResponseEntity<Result<Void>> response = handler.handleNoHandlerFoundException(exception, mockRequest);
        
        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ErrorCode.RESOURCE_NOT_FOUND.getCode(), response.getBody().getCode());
        assertEquals("Endpoint 'GET /api/health1' not found", response.getBody().getMessage());
    }
    
    @Test
    void handleNoHandlerFoundException_WithDifferentMethodAndPath_ShouldReturnCorrectMessage() {
        // Arrange
        NoHandlerFoundException exception = new NoHandlerFoundException(
            "POST", "/api/v1/users/999/orders", new org.springframework.http.HttpHeaders()
        );
        
        // Act
        ResponseEntity<Result<Void>> response = handler.handleNoHandlerFoundException(exception, mockRequest);
        
        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ErrorCode.RESOURCE_NOT_FOUND.getCode(), response.getBody().getCode());
        assertEquals("Endpoint 'POST /api/v1/users/999/orders' not found", response.getBody().getMessage());
    }

    @Test
    void handleHttpRequestMethodNotSupportedException_ShouldReturn405() {
        // Arrange
        org.springframework.web.HttpRequestMethodNotSupportedException exception = 
            new org.springframework.web.HttpRequestMethodNotSupportedException("DELETE");
        
        // Act
        ResponseEntity<Result<Void>> response = handler.handleHttpRequestMethodNotSupportedException(exception);
        
        // Assert
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ErrorCode.OPERATION_NOT_ALLOWED.getCode(), response.getBody().getCode());
        assertEquals("HTTP method 'DELETE' is not supported for this endpoint", response.getBody().getMessage());
    }

    @Test
    void handleHttpMediaTypeNotAcceptableException_ShouldReturn406() {
        // Arrange
        org.springframework.web.HttpMediaTypeNotAcceptableException exception = 
            new org.springframework.web.HttpMediaTypeNotAcceptableException("Media type not acceptable");
        
        // Act
        ResponseEntity<Result<Void>> response = handler.handleHttpMediaTypeNotAcceptableException(exception);
        
        // Assert
        assertEquals(HttpStatus.NOT_ACCEPTABLE, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ErrorCode.UNSUPPORTED_API_VERSION.getCode(), response.getBody().getCode());
        assertEquals("Requested media type is not supported", response.getBody().getMessage());
    }

    @Test
    void handleHttpMessageNotReadableException_ShouldReturn400() {
        // Arrange
        org.springframework.http.converter.HttpMessageNotReadableException exception = 
            new org.springframework.http.converter.HttpMessageNotReadableException("JSON parse error");
        
        // Act
        ResponseEntity<Result<Void>> response = handler.handleHttpMessageNotReadableException(exception);
        
        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ErrorCode.VALIDATION_ERROR.getCode(), response.getBody().getCode());
        assertEquals("Invalid request format", response.getBody().getMessage());
    }

    @Test
    void handleBusinessException_WithMerchantNotFound_ShouldReturn404() {
        // Arrange - "Merchant not found" contains "not found" so it maps to RESOURCE_NOT_FOUND
        RuntimeException exception = new RuntimeException("Merchant not found");
        
        // Act
        ResponseEntity<Result<Void>> response = handler.handleBusinessException(exception, mockRequest);
        
        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ErrorCode.RESOURCE_NOT_FOUND.getCode(), response.getBody().getCode());
        assertEquals("Merchant not found", response.getBody().getMessage());
    }

    @Test
    void handleBusinessException_WithInvalidSettlementDate_ShouldReturn500() {
        // Arrange - "Invalid settlement date" contains "settlement" so it maps to SETTLEMENT_FAILED
        RuntimeException exception = new RuntimeException("Invalid settlement date");
        
        // Act
        ResponseEntity<Result<Void>> response = handler.handleBusinessException(exception, mockRequest);
        
        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ErrorCode.SETTLEMENT_FAILED.getCode(), response.getBody().getCode());
        assertEquals("Invalid settlement date", response.getBody().getMessage());
    }

    @Test
    void handleBusinessException_WithInternalError_ShouldReturn500() {
        // Arrange
        RuntimeException exception = new RuntimeException("Internal error");
        
        // Act
        ResponseEntity<Result<Void>> response = handler.handleBusinessException(exception, mockRequest);
        
        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ErrorCode.INTERNAL_ERROR.getCode(), response.getBody().getCode());
        assertEquals("Internal error", response.getBody().getMessage());
    }

    @Test
    void handleBusinessException_WithSettlementFailed_ShouldReturn500() {
        // Arrange
        RuntimeException exception = new RuntimeException("Settlement failed");
        
        // Act
        ResponseEntity<Result<Void>> response = handler.handleBusinessException(exception, mockRequest);
        
        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ErrorCode.SETTLEMENT_FAILED.getCode(), response.getBody().getCode());
        assertEquals("Settlement failed", response.getBody().getMessage());
    }

    @Test
    void handleUnexpectedException_WithNullWebRequest_ShouldHandleGracefully() {
        // Arrange
        Exception exception = new Exception("Test exception");
        
        // Act
        ResponseEntity<Result<String>> response = handler.handleUnexpectedException(exception, null);
        
        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ErrorCode.INTERNAL_ERROR.getCode(), response.getBody().getCode());
        assertEquals("Internal server error occurred", response.getBody().getMessage());
        assertEquals("Unknown request", response.getBody().getData());
    }
} 