package com.ecommerce.api.interceptor;

import com.ecommerce.api.annotation.ApiVersion;
import com.ecommerce.api.config.ApiVersionConfig;
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
    @Mock PrintWriter writer;

    private StringWriter stringWriter;

    @BeforeEach
    void setUp() {
        interceptor = new ApiVersionInterceptor();
        stringWriter = new StringWriter();
        writer = new PrintWriter(stringWriter);
    }

    @Test
    void preHandle_nonHandlerMethod_shouldReturnTrue() throws Exception {
        // Given
        Object notHandler = new Object();
        
        // When
        boolean result = interceptor.preHandle(request, response, notHandler);
        
        // Then
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
        when(apiVersion.until()).thenReturn("2024-12-31");
        when(handlerMethod.getMethodAnnotation(ApiVersion.class)).thenReturn(apiVersion);
        when(request.getRequestURI()).thenReturn("/api/v1/users");
        
        // When
        boolean result = interceptor.preHandle(request, response, handlerMethod);
        
        // Then
        assertTrue(result);
        verify(response).setHeader("X-API-Deprecated", "true");
        verify(response).setHeader("X-API-Deprecation-Message", "This API will be removed in v3");
        verify(response).setHeader("X-API-Deprecation-Until", "2024-12-31");
    }

    static class TestController {
        @ApiVersion("v1")
        public void v1Method() {}
        @ApiVersion("v2")
        public void v2Method() {}
        public void noVersionMethod() {}
    }
} 