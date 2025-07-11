package com.ecommerce.api.interceptor;

import com.ecommerce.api.annotation.ApiVersion;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.method.HandlerMethod;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApiVersionInterceptorTest {
    
    private ApiVersionInterceptor interceptor;
    @Mock HttpServletRequest request;
    @Mock HttpServletResponse response;
    @Mock HandlerMethod handlerMethod;
    
    @BeforeEach
    void setUp() {
        interceptor = new ApiVersionInterceptor();
    }
    
    @Test
    void preHandle_nonHandlerMethod_shouldReturnTrue() throws Exception {
        Object notHandler = new Object();
        boolean result = interceptor.preHandle(request, response, notHandler);
        assertTrue(result);
    }
    
    @Test
    void preHandle_methodVersionAnnotation_compatibleVersion_shouldReturnTrue() throws Exception {
        // Given
        ApiVersion apiVersion = mock(ApiVersion.class);
        when(apiVersion.value()).thenReturn("v1");
        when(apiVersion.deprecated()).thenReturn(false);
        when(handlerMethod.getMethodAnnotation(ApiVersion.class)).thenReturn(apiVersion);
        when(request.getRequestURI()).thenReturn("/api/v1/users");
        
        // When
        boolean result = interceptor.preHandle(request, response, handlerMethod);
        
        // Then
        assertTrue(result);
        verify(response).setHeader("X-API-Version", "v1");
    }
    
    @Test
    void preHandle_deprecatedVersion_shouldSetDeprecationHeaders() throws Exception {
        // Given
        ApiVersion apiVersion = mock(ApiVersion.class);
        when(apiVersion.value()).thenReturn("v1");
        when(apiVersion.deprecated()).thenReturn(true);
        when(apiVersion.deprecationMessage()).thenReturn("This API will be removed in v3");
        when(apiVersion.until()).thenReturn("2025-07-10");
        when(handlerMethod.getMethodAnnotation(ApiVersion.class)).thenReturn(apiVersion);
        when(request.getRequestURI()).thenReturn("/api/v1/users");
        
        // When
        boolean result = interceptor.preHandle(request, response, handlerMethod);
        
        // Then
        assertTrue(result);
        verify(response).setHeader("X-API-Deprecated", "true");
        verify(response).setHeader("X-API-Deprecation-Message", "This API will be removed in v3");
        verify(response).setHeader("X-API-Deprecation-Until", "2025-07-10");
        verify(response).setHeader("X-API-Version", "v1");
    }
    
    static class TestController {
        @ApiVersion("v1")
        public void v1Method() {}
        
        @ApiVersion("v2")
        public void v2Method() {}
        
        public void noVersionMethod() {}
    }
    
    @Test
    void preHandle_incompatibleVersion_shouldReturnFalse() throws Exception {
        // Given
        ApiVersion apiVersion = mock(ApiVersion.class);
        when(apiVersion.value()).thenReturn("v1");
        when(handlerMethod.getMethodAnnotation(ApiVersion.class)).thenReturn(apiVersion);
        when(request.getRequestURI()).thenReturn("/api/users");
        when(request.getHeader("API-Version")).thenReturn("v3"); // v3 is not in SUPPORTED_VERSIONS
        when(response.getWriter()).thenReturn(new PrintWriter(new StringWriter()));
        
        // When
        boolean result = interceptor.preHandle(request, response, handlerMethod);
        
        // Then
        assertFalse(result);
        verify(response).setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
        verify(response).setContentType("application/json;charset=UTF-8");
    }
    
    @Test
    void preHandle_versionFromHeader_shouldReturnTrue() throws Exception {
        // Given
        ApiVersion apiVersion = mock(ApiVersion.class);
        when(apiVersion.value()).thenReturn("v2");
        when(apiVersion.deprecated()).thenReturn(false);
        when(handlerMethod.getMethodAnnotation(ApiVersion.class)).thenReturn(apiVersion);
        when(request.getRequestURI()).thenReturn("/api/users");
        when(request.getHeader("API-Version")).thenReturn("v2");
        
        // When
        boolean result = interceptor.preHandle(request, response, handlerMethod);
        
        // Then
        assertTrue(result);
        verify(response).setHeader("X-API-Version", "v2");
    }
    
    @Test
    void preHandle_versionFromQueryParam_shouldReturnTrue() throws Exception {
        // Given
        ApiVersion apiVersion = mock(ApiVersion.class);
        when(apiVersion.value()).thenReturn("v2");
        when(apiVersion.deprecated()).thenReturn(false);
        when(handlerMethod.getMethodAnnotation(ApiVersion.class)).thenReturn(apiVersion);
        when(request.getRequestURI()).thenReturn("/api/users");
        when(request.getParameter("version")).thenReturn("v2");
        
        // When
        boolean result = interceptor.preHandle(request, response, handlerMethod);
        
        // Then
        assertTrue(result);
        verify(response).setHeader("X-API-Version", "v2");
    }
    
    @Test
    void preHandle_versionFromQueryParamWithoutV_shouldReturnTrue() throws Exception {
        // Given
        ApiVersion apiVersion = mock(ApiVersion.class);
        when(apiVersion.value()).thenReturn("v2");
        when(apiVersion.deprecated()).thenReturn(false);
        when(handlerMethod.getMethodAnnotation(ApiVersion.class)).thenReturn(apiVersion);
        when(request.getRequestURI()).thenReturn("/api/users");
        when(request.getParameter("version")).thenReturn("v2");
        
        // When
        boolean result = interceptor.preHandle(request, response, handlerMethod);
        
        // Then
        assertTrue(result);
        verify(response).setHeader("X-API-Version", "v2");
    }
    
    @Test
    void preHandle_versionFromHeaderWithoutV_shouldReturnTrue() throws Exception {
        // Given
        ApiVersion apiVersion = mock(ApiVersion.class);
        when(apiVersion.value()).thenReturn("v2");
        when(apiVersion.deprecated()).thenReturn(false);
        when(handlerMethod.getMethodAnnotation(ApiVersion.class)).thenReturn(apiVersion);
        when(request.getRequestURI()).thenReturn("/api/users");
        when(request.getHeader("API-Version")).thenReturn("v2");
        
        // When
        boolean result = interceptor.preHandle(request, response, handlerMethod);
        
        // Then
        assertTrue(result);
        verify(response).setHeader("X-API-Version", "v2");
    }
    
    @Test
    void preHandle_defaultVersion_shouldReturnTrue() throws Exception {
        // Given
        ApiVersion apiVersion = mock(ApiVersion.class);
        when(apiVersion.value()).thenReturn("v1");
        when(handlerMethod.getMethodAnnotation(ApiVersion.class)).thenReturn(apiVersion);
        when(request.getRequestURI()).thenReturn("/api/users");
        
        // When
        boolean result = interceptor.preHandle(request, response, handlerMethod);
        
        // Then
        assertTrue(result);
        verify(response).setHeader("X-API-Version", "v1");
    }
    
    @Test
    void preHandle_deprecatedVersionWithUntilDate_shouldSetAllHeaders() throws Exception {
        // Given
        ApiVersion apiVersion = mock(ApiVersion.class);
        when(apiVersion.value()).thenReturn("v1");
        when(apiVersion.deprecated()).thenReturn(true);
        when(apiVersion.deprecationMessage()).thenReturn("This API will be removed in v3");
        when(apiVersion.until()).thenReturn("2025-07-10");
        when(handlerMethod.getMethodAnnotation(ApiVersion.class)).thenReturn(apiVersion);
        when(request.getRequestURI()).thenReturn("/api/v1/users");
        
        // When
        boolean result = interceptor.preHandle(request, response, handlerMethod);
        
        // Then
        assertTrue(result);
        verify(response).setHeader("X-API-Deprecated", "true");
        verify(response).setHeader("X-API-Deprecation-Message", "This API will be removed in v3");
        verify(response).setHeader("X-API-Deprecation-Until", "2025-07-10");
        verify(response).setHeader("X-API-Version", "v1");
    }
    
    @Test
    void preHandle_deprecatedVersionWithoutUntilDate_shouldSetDeprecationHeaders() throws Exception {
        // Given
        ApiVersion apiVersion = mock(ApiVersion.class);
        when(apiVersion.value()).thenReturn("v1");
        when(apiVersion.deprecated()).thenReturn(true);
        when(apiVersion.deprecationMessage()).thenReturn("This API will be removed in v3");
        when(apiVersion.until()).thenReturn("");
        when(handlerMethod.getMethodAnnotation(ApiVersion.class)).thenReturn(apiVersion);
        when(request.getRequestURI()).thenReturn("/api/v1/users");
        
        // When
        boolean result = interceptor.preHandle(request, response, handlerMethod);
        
        // Then
        assertTrue(result);
        verify(response).setHeader("X-API-Deprecated", "true");
        verify(response).setHeader("X-API-Deprecation-Message", "This API will be removed in v3");
        verify(response, never()).setHeader(eq("X-API-Deprecation-Until"), anyString());
        verify(response).setHeader("X-API-Version", "v1");
    }
    
    @Test
    void preHandle_versionInPathWithComplexUrl_shouldExtractCorrectly() throws Exception {
        // Given
        ApiVersion apiVersion = mock(ApiVersion.class);
        when(apiVersion.value()).thenReturn("v2");
        when(apiVersion.deprecated()).thenReturn(false);
        when(handlerMethod.getMethodAnnotation(ApiVersion.class)).thenReturn(apiVersion);
        when(request.getRequestURI()).thenReturn("/api/v2/users/123/orders/456");
        
        // When
        boolean result = interceptor.preHandle(request, response, handlerMethod);
        
        // Then
        assertTrue(result);
        verify(response).setHeader("X-API-Version", "v2");
    }
    
    @Test
    void preHandle_versionInPathWithQueryParams_shouldExtractCorrectly() throws Exception {
        // Given
        ApiVersion apiVersion = mock(ApiVersion.class);
        when(apiVersion.value()).thenReturn("v1");
        when(apiVersion.deprecated()).thenReturn(false);
        when(handlerMethod.getMethodAnnotation(ApiVersion.class)).thenReturn(apiVersion);
        when(request.getRequestURI()).thenReturn("/api/v1/users?page=1&size=10");
        
        // When
        boolean result = interceptor.preHandle(request, response, handlerMethod);
        
        // Then
        assertTrue(result);
        verify(response).setHeader("X-API-Version", "v1");
    }
    
    @Test
    void preHandle_versionInPathWithTrailingSlash_shouldExtractCorrectly() throws Exception {
        // Given
        ApiVersion apiVersion = mock(ApiVersion.class);
        when(apiVersion.value()).thenReturn("v1");
        when(apiVersion.deprecated()).thenReturn(false);
        when(handlerMethod.getMethodAnnotation(ApiVersion.class)).thenReturn(apiVersion);
        when(request.getRequestURI()).thenReturn("/api/v1/users/");
        
        // When
        boolean result = interceptor.preHandle(request, response, handlerMethod);
        
        // Then
        assertTrue(result);
        verify(response).setHeader("X-API-Version", "v1");
    }
    
    @Test
    void preHandle_versionInPathWithMultipleV_shouldExtractFirstV() throws Exception {
        // Given
        ApiVersion apiVersion = mock(ApiVersion.class);
        when(apiVersion.value()).thenReturn("v1");
        when(apiVersion.deprecated()).thenReturn(false);
        when(handlerMethod.getMethodAnnotation(ApiVersion.class)).thenReturn(apiVersion);
        when(request.getRequestURI()).thenReturn("/api/v1/users/v2/orders");
        
        // When
        boolean result = interceptor.preHandle(request, response, handlerMethod);
        
        // Then
        assertTrue(result);
        verify(response).setHeader("X-API-Version", "v1");
    }
    
    @Test
    void preHandle_versionInPathWithInvalidFormat_shouldUseDefault() throws Exception {
        // Given
        ApiVersion apiVersion = mock(ApiVersion.class);
        when(apiVersion.value()).thenReturn("v1");
        when(handlerMethod.getMethodAnnotation(ApiVersion.class)).thenReturn(apiVersion);
        when(request.getRequestURI()).thenReturn("/api/vinvalid/users");
        when(response.getWriter()).thenReturn(new PrintWriter(new StringWriter()));
        
        // When
        boolean result = interceptor.preHandle(request, response, handlerMethod);
        
        // Then
        assertFalse(result);
        verify(response).setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
        verify(response).setContentType("application/json;charset=UTF-8");
    }
    
    @Test
    void preHandle_versionInPathWithSingleCharacter_shouldUseDefault() throws Exception {
        // Given
        ApiVersion apiVersion = mock(ApiVersion.class);
        when(apiVersion.value()).thenReturn("v1");
        when(handlerMethod.getMethodAnnotation(ApiVersion.class)).thenReturn(apiVersion);
        when(request.getRequestURI()).thenReturn("/api/vx/users");
        when(response.getWriter()).thenReturn(new PrintWriter(new StringWriter()));
        
        // When
        boolean result = interceptor.preHandle(request, response, handlerMethod);
        
        // Then
        assertFalse(result);
        verify(response).setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
        verify(response).setContentType("application/json;charset=UTF-8");
    }
    
    @Test
    void preHandle_versionInPathWithLongVersion_shouldExtractCorrectly() throws Exception {
        // Given
        ApiVersion apiVersion = mock(ApiVersion.class);
        when(apiVersion.value()).thenReturn("v1");
        when(handlerMethod.getMethodAnnotation(ApiVersion.class)).thenReturn(apiVersion);
        when(request.getRequestURI()).thenReturn("/api/v123456789/users");
        when(response.getWriter()).thenReturn(new PrintWriter(new StringWriter()));
        
        // When
        boolean result = interceptor.preHandle(request, response, handlerMethod);
        
        // Then
        assertFalse(result);
        verify(response).setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
        verify(response).setContentType("application/json;charset=UTF-8");
    }
    
    @Test
    void preHandle_versionInPathWithSpecialCharacters_shouldExtractCorrectly() throws Exception {
        // Given
        ApiVersion apiVersion = mock(ApiVersion.class);
        when(apiVersion.value()).thenReturn("v1");
        when(handlerMethod.getMethodAnnotation(ApiVersion.class)).thenReturn(apiVersion);
        when(request.getRequestURI()).thenReturn("/api/v1@2/users");
        when(response.getWriter()).thenReturn(new PrintWriter(new StringWriter()));
        
        // When
        boolean result = interceptor.preHandle(request, response, handlerMethod);
        
        // Then
        assertFalse(result);
        verify(response).setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
        verify(response).setContentType("application/json;charset=UTF-8");
    }
    
    @Test
    void preHandle_versionInPathWithUpperCase_shouldExtractCorrectly() throws Exception {
        // Given
        ApiVersion apiVersion = mock(ApiVersion.class);
        when(apiVersion.value()).thenReturn("v1");
        when(handlerMethod.getMethodAnnotation(ApiVersion.class)).thenReturn(apiVersion);
        when(request.getRequestURI()).thenReturn("/api/V1/users");
        
        // When
        boolean result = interceptor.preHandle(request, response, handlerMethod);
        
        // Then
        assertTrue(result);
        verify(response).setHeader("X-API-Version", "v1");
    }
    
    @Test
    void preHandle_versionInPathWithMixedCase_shouldExtractCorrectly() throws Exception {
        // Given
        ApiVersion apiVersion = mock(ApiVersion.class);
        when(apiVersion.value()).thenReturn("v1");
        when(handlerMethod.getMethodAnnotation(ApiVersion.class)).thenReturn(apiVersion);
        when(request.getRequestURI()).thenReturn("/api/V1/users");
        
        // When
        boolean result = interceptor.preHandle(request, response, handlerMethod);
        
        // Then
        assertTrue(result);
        verify(response).setHeader("X-API-Version", "v1");
    }
    
    @Test
    void preHandle_versionInPathWithNumbersOnly_shouldUseDefault() throws Exception {
        // Given
        ApiVersion apiVersion = mock(ApiVersion.class);
        when(apiVersion.value()).thenReturn("v1");
        when(handlerMethod.getMethodAnnotation(ApiVersion.class)).thenReturn(apiVersion);
        when(request.getRequestURI()).thenReturn("/api/123/users");
        
        // When
        boolean result = interceptor.preHandle(request, response, handlerMethod);
        
        // Then
        assertTrue(result);
        verify(response).setHeader("X-API-Version", "v1");
    }
    
    @Test
    void preHandle_versionInPathWithEmptyVersion_shouldUseDefault() throws Exception {
        // Given
        ApiVersion apiVersion = mock(ApiVersion.class);
        when(apiVersion.value()).thenReturn("v1");
        when(handlerMethod.getMethodAnnotation(ApiVersion.class)).thenReturn(apiVersion);
        when(request.getRequestURI()).thenReturn("/api/v/users");
        
        // When
        boolean result = interceptor.preHandle(request, response, handlerMethod);
        
        // Then
        assertTrue(result);
        verify(response).setHeader("X-API-Version", "v1");
    }
    
    @Test
    void preHandle_versionInPathWithDotSeparated_shouldExtractCorrectly() throws Exception {
        // Given
        ApiVersion apiVersion = mock(ApiVersion.class);
        when(apiVersion.value()).thenReturn("v1");
        when(handlerMethod.getMethodAnnotation(ApiVersion.class)).thenReturn(apiVersion);
        when(request.getRequestURI()).thenReturn("/api/v1.2/users");
        when(response.getWriter()).thenReturn(new PrintWriter(new StringWriter()));
        
        // When
        boolean result = interceptor.preHandle(request, response, handlerMethod);
        
        // Then
        assertFalse(result);
        verify(response).setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
        verify(response).setContentType("application/json;charset=UTF-8");
    }
    
    @Test
    void preHandle_noVersionAnnotation_shouldReturnTrue() throws Exception {
        // Given - no method-level or class-level annotation
        when(handlerMethod.getMethodAnnotation(ApiVersion.class)).thenReturn(null);
        when(handlerMethod.getBeanType()).thenReturn((Class) Object.class);
        
        // When
        boolean result = interceptor.preHandle(request, response, handlerMethod);
        
        // Then
        assertTrue(result);
        // No version header should be set
        verify(response, never()).setHeader(eq("X-API-Version"), anyString());
    }

    @Test
    void preHandle_versionFromHeaderWithEmptyString_shouldUseDefault() throws Exception {
        // Given
        ApiVersion apiVersion = mock(ApiVersion.class);
        when(apiVersion.value()).thenReturn("v1");
        when(apiVersion.deprecated()).thenReturn(false);
        when(handlerMethod.getMethodAnnotation(ApiVersion.class)).thenReturn(apiVersion);
        when(request.getRequestURI()).thenReturn("/api/users");
        when(request.getHeader("API-Version")).thenReturn(""); // Empty string
        
        // When
        boolean result = interceptor.preHandle(request, response, handlerMethod);
        
        // Then
        assertTrue(result);
        verify(response).setHeader("X-API-Version", "v1");
    }

    @Test
    void preHandle_versionFromQueryParamWithEmptyString_shouldUseDefault() throws Exception {
        // Given
        ApiVersion apiVersion = mock(ApiVersion.class);
        when(apiVersion.value()).thenReturn("v1");
        when(apiVersion.deprecated()).thenReturn(false);
        when(handlerMethod.getMethodAnnotation(ApiVersion.class)).thenReturn(apiVersion);
        when(request.getRequestURI()).thenReturn("/api/users");
        when(request.getParameter("version")).thenReturn(""); // Empty string
        
        // When
        boolean result = interceptor.preHandle(request, response, handlerMethod);
        
        // Then
        assertTrue(result);
        verify(response).setHeader("X-API-Version", "v1");
    }

    @Test
    void preHandle_versionFromHeaderWithNumberOnly_shouldAddVPrefix() throws Exception {
        // Given
        ApiVersion apiVersion = mock(ApiVersion.class);
        when(apiVersion.value()).thenReturn("v2");
        when(apiVersion.deprecated()).thenReturn(false);
        when(handlerMethod.getMethodAnnotation(ApiVersion.class)).thenReturn(apiVersion);
        when(request.getRequestURI()).thenReturn("/api/users");
        when(request.getHeader("API-Version")).thenReturn("2"); // Number only
        
        // When
        boolean result = interceptor.preHandle(request, response, handlerMethod);
        
        // Then
        assertTrue(result);
        verify(response).setHeader("X-API-Version", "v2");
    }

    @Test
    void preHandle_versionFromQueryParamWithNumberOnly_shouldAddVPrefix() throws Exception {
        // Given
        ApiVersion apiVersion = mock(ApiVersion.class);
        when(apiVersion.value()).thenReturn("v2");
        when(apiVersion.deprecated()).thenReturn(false);
        when(handlerMethod.getMethodAnnotation(ApiVersion.class)).thenReturn(apiVersion);
        when(request.getRequestURI()).thenReturn("/api/users");
        when(request.getParameter("version")).thenReturn("2"); // Number only
        
        // When
        boolean result = interceptor.preHandle(request, response, handlerMethod);
        
        // Then
        assertTrue(result);
        verify(response).setHeader("X-API-Version", "v2");
    }

    @Test
    void preHandle_backwardCompatible_shouldReturnTrue() throws Exception {
        // Given - backward compatibility is enabled in config, so v1 should work with v2 endpoint
        ApiVersion apiVersion = mock(ApiVersion.class);
        when(apiVersion.value()).thenReturn("v2");
        when(apiVersion.deprecated()).thenReturn(false);
        when(handlerMethod.getMethodAnnotation(ApiVersion.class)).thenReturn(apiVersion);
        when(request.getRequestURI()).thenReturn("/api/users");
        when(request.getHeader("API-Version")).thenReturn("v1"); // Different but supported version
        
        // When
        boolean result = interceptor.preHandle(request, response, handlerMethod);
        
        // Then
        assertTrue(result); // Should pass because v1 is in SUPPORTED_VERSIONS
        verify(response).setHeader("X-API-Version", "v2");
    }
} 