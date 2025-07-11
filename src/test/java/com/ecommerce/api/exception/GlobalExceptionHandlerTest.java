package com.ecommerce.api.exception;

import com.ecommerce.api.dto.ErrorCode;
import com.ecommerce.api.dto.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {
    
    private GlobalExceptionHandler handler;
    private MethodParameter methodParameter;
    private WebRequest mockRequest;
    
    @BeforeEach
    void setUp() throws NoSuchMethodException {
        handler = new GlobalExceptionHandler();
        
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
    void handleBusinessException_WithValidBusinessException_ShouldReturnCorrectErrorCode() {
        // Arrange
        BusinessException exception = new BusinessException(ErrorCode.INSUFFICIENT_BALANCE, 
            "Insufficient balance for operation");
        
        // Act
        ResponseEntity<Result<Void>> response = handler.handleBusinessException(exception, mockRequest);
        
        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ErrorCode.INSUFFICIENT_BALANCE.getCode(), response.getBody().getCode());
        assertEquals("Insufficient balance for operation", response.getBody().getMessage());
    }
    
    @Test
    void handleBusinessException_WithResourceNotFound_ShouldReturn404() {
        // Arrange
        BusinessException exception = new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, 
            "User not found");
        
        // Act
        ResponseEntity<Result<Void>> response = handler.handleBusinessException(exception, mockRequest);
        
        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ErrorCode.RESOURCE_NOT_FOUND.getCode(), response.getBody().getCode());
        assertEquals("User not found", response.getBody().getMessage());
    }
    
    @Test
    void handleBusinessException_WithResourceAlreadyExists_ShouldReturn409() {
        // Arrange
        BusinessException exception = new BusinessException(ErrorCode.RESOURCE_ALREADY_EXISTS, 
            "Product already exists");
        
        // Act
        ResponseEntity<Result<Void>> response = handler.handleBusinessException(exception, mockRequest);
        
        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ErrorCode.RESOURCE_ALREADY_EXISTS.getCode(), response.getBody().getCode());
        assertEquals("Product already exists", response.getBody().getMessage());
    }
    
    @Test
    void handleRuntimeException_WithLegacyException_ShouldUseFromMessage() {
        // Arrange
        RuntimeException exception = new RuntimeException("Insufficient balance");
        
        // Act
        ResponseEntity<Result<Void>> response = handler.handleRuntimeException(exception, mockRequest);
        
        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ErrorCode.INSUFFICIENT_BALANCE.getCode(), response.getBody().getCode());
        assertEquals("Insufficient balance", response.getBody().getMessage());
    }
    
    @Test
    void handleValidationException_WithUserIdError_ShouldReturnCustomMessage() {
        // Arrange
        FieldError fieldError = new FieldError("object", "userId", "must not be null");
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
        assertEquals("User ID is required", response.getBody().getMessage());
    }
    
    @Test
    void handleIllegalArgumentException_ShouldReturnValidationError() {
        // Arrange
        IllegalArgumentException exception = new IllegalArgumentException("Invalid parameter value");
        
        // Act
        ResponseEntity<Result<Void>> response = handler.handleIllegalArgumentException(exception);
        
        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ErrorCode.VALIDATION_ERROR.getCode(), response.getBody().getCode());
        assertEquals("Invalid parameter value", response.getBody().getMessage());
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
    void handleUnexpectedException_WithNullRequest_ShouldReturn500() {
        // Arrange
        Exception exception = new Exception("Unexpected error");
        
        // Act
        ResponseEntity<Result<String>> response = handler.handleUnexpectedException(exception, null);
        
        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ErrorCode.INTERNAL_ERROR.getCode(), response.getBody().getCode());
        assertEquals("Internal server error occurred", response.getBody().getMessage());
    }

    @Test
    void determineHttpStatus_WithResourceInactive_ShouldReturnForbidden() {
        // This tests the switch case in determineHttpStatus method
        BusinessException exception = new BusinessException(ErrorCode.RESOURCE_INACTIVE, "Resource inactive");
        
        ResponseEntity<Result<Void>> response = handler.handleBusinessException(exception, mockRequest);
        
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void determineHttpStatus_WithUnsupportedApiVersion_ShouldReturnNotAcceptable() {
        BusinessException exception = new BusinessException(ErrorCode.UNSUPPORTED_API_VERSION, "API version not supported");
        
        ResponseEntity<Result<Void>> response = handler.handleBusinessException(exception, mockRequest);
        
        assertEquals(HttpStatus.NOT_ACCEPTABLE, response.getStatusCode());
    }

    @Test
    void determineHttpStatus_WithInternalError_ShouldReturnInternalServerError() {
        BusinessException exception = new BusinessException(ErrorCode.INTERNAL_ERROR, "Internal error");
        
        ResponseEntity<Result<Void>> response = handler.handleBusinessException(exception, mockRequest);
        
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void buildUserFriendlyValidationMessage_WithProductNameField_ShouldReturnCustomMessage() {
        // This tests different field mappings in buildUserFriendlyValidationMessage
        FieldError fieldError = new FieldError("object", "productName", "must not be blank");
        org.springframework.validation.BeanPropertyBindingResult bindingResult = 
            new org.springframework.validation.BeanPropertyBindingResult(new Object(), "object");
        bindingResult.addError(fieldError);
        
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(
            methodParameter, bindingResult
        );
        
        ResponseEntity<Result<Void>> response = handler.handleValidationException(exception);
        
        assertEquals("must not be blank", response.getBody().getMessage());
    }

    @Test
    void buildUserFriendlyValidationMessage_WithEmailField_ShouldReturnCustomMessage() {
        FieldError fieldError = new FieldError("object", "email", "must be a well-formed email address");
        org.springframework.validation.BeanPropertyBindingResult bindingResult = 
            new org.springframework.validation.BeanPropertyBindingResult(new Object(), "object");
        bindingResult.addError(fieldError);
        
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(
            methodParameter, bindingResult
        );
        
        ResponseEntity<Result<Void>> response = handler.handleValidationException(exception);
        
        assertEquals("Valid email address is required", response.getBody().getMessage());
    }

    @Test
    void buildUserFriendlyValidationMessage_WithPhoneField_ShouldReturnCustomMessage() {
        FieldError fieldError = new FieldError("object", "phone", "invalid format");
        org.springframework.validation.BeanPropertyBindingResult bindingResult = 
            new org.springframework.validation.BeanPropertyBindingResult(new Object(), "object");
        bindingResult.addError(fieldError);
        
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(
            methodParameter, bindingResult
        );
        
        ResponseEntity<Result<Void>> response = handler.handleValidationException(exception);
        
        assertEquals("Valid phone number is required", response.getBody().getMessage());
    }

    @Test
    void buildUserFriendlyValidationMessage_WithAmountField_ShouldReturnCustomMessage() {
        FieldError fieldError = new FieldError("object", "amount", "must be positive");
        org.springframework.validation.BeanPropertyBindingResult bindingResult = 
            new org.springframework.validation.BeanPropertyBindingResult(new Object(), "object");
        bindingResult.addError(fieldError);
        
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(
            methodParameter, bindingResult
        );
        
        ResponseEntity<Result<Void>> response = handler.handleValidationException(exception);
        
        assertEquals("Amount must be a positive number", response.getBody().getMessage());
    }
} 