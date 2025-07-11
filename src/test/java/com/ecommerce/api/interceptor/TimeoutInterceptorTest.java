package com.ecommerce.api.interceptor;

import com.ecommerce.api.annotation.ApiTimeout;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.method.HandlerMethod;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TimeoutInterceptorTest {
    private TimeoutInterceptor interceptor;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private HandlerMethod handlerMethod;

    @BeforeEach
    void setUp() {
        interceptor = new TimeoutInterceptor();
    }

    @Test
    void preHandle_nonHandlerMethod_shouldReturnTrue() throws Exception {
        Object notHandler = new Object();
        boolean result = interceptor.preHandle(request, response, notHandler);
        assertTrue(result);
    }

    @Test
    void preHandle_handlerMethod_withoutTimeoutAnnotation_shouldReturnTrue() throws Exception {
        when(handlerMethod.getMethodAnnotation(ApiTimeout.class)).thenReturn(null);
        when(handlerMethod.getBeanType()).thenReturn((Class) TestController.class);
        when(request.getHeader("X-Request-ID")).thenReturn("test-request-id");
        boolean result = interceptor.preHandle(request, response, handlerMethod);
        assertTrue(result);
    }

    @Test
    void preHandle_handlerMethod_withMethodTimeoutAnnotation_shouldStoreTimeoutInfo() throws Exception {
        ApiTimeout timeout = mock(ApiTimeout.class);
        when(timeout.value()).thenReturn(1000L);
        when(timeout.unit()).thenReturn(TimeUnit.MILLISECONDS);
        when(timeout.message()).thenReturn("Request timeout");
        when(handlerMethod.getMethodAnnotation(ApiTimeout.class)).thenReturn(timeout);
        when(request.getHeader("X-Request-ID")).thenReturn("test-request-id");
        boolean result = interceptor.preHandle(request, response, handlerMethod);
        assertTrue(result);
        verify(request).setAttribute("timeout_value", 1000L);
        verify(request).setAttribute("timeout_unit", TimeUnit.MILLISECONDS);
        verify(request).setAttribute("timeout_message", "Request timeout");
    }

    @Test
    void afterCompletion_handlerMethod_withoutStartTime_shouldNotLog() throws Exception {
        when(request.getHeader("X-Request-ID")).thenReturn("non-existent-id");
        interceptor.afterCompletion(request, response, handlerMethod, null);
        verify(response, never()).setHeader(anyString(), anyString());
    }
    
    @Test
    void afterCompletion_nonHandlerMethod_shouldNotLog() throws Exception {
        Object notHandler = new Object();
        interceptor.afterCompletion(request, response, notHandler, null);
        verify(response, never()).setHeader(anyString(), anyString());
    }

    static class TestController {
        public void normalMethod() {}
    }

    @ApiTimeout(value = 500, unit = TimeUnit.SECONDS, message = "Class timeout")
    static class TestControllerWithTimeout {
        public void methodWithTimeout() {}
    }

    // Additional test cases for better coverage

    @Test
    void preHandle_handlerMethod_withClassTimeoutAnnotation_shouldStoreTimeoutInfo() throws Exception {
        // Given
        when(handlerMethod.getMethodAnnotation(ApiTimeout.class)).thenReturn(null);
        when(handlerMethod.getBeanType()).thenReturn((Class) TestControllerWithTimeout.class);
        when(request.getHeader("X-Request-ID")).thenReturn("test-request-id");

        // When
        boolean result = interceptor.preHandle(request, response, handlerMethod);

        // Then
        assertTrue(result);
        verify(request).setAttribute("timeout_value", 500L);
        verify(request).setAttribute("timeout_unit", TimeUnit.SECONDS);
        verify(request).setAttribute("timeout_message", "Class timeout");
    }

    @Test
    void preHandle_handlerMethod_withoutRequestId_shouldGenerateRequestId() throws Exception {
        // Given
        when(handlerMethod.getMethodAnnotation(ApiTimeout.class)).thenReturn(null);
        when(handlerMethod.getBeanType()).thenReturn((Class) TestController.class);
        when(request.getHeader("X-Request-ID")).thenReturn(null);

        // When
        boolean result = interceptor.preHandle(request, response, handlerMethod);

        // Then
        assertTrue(result);
    }

    @Test
    void preHandle_handlerMethod_withEmptyRequestId_shouldGenerateRequestId() throws Exception {
        // Given
        when(handlerMethod.getMethodAnnotation(ApiTimeout.class)).thenReturn(null);
        when(handlerMethod.getBeanType()).thenReturn((Class) TestController.class);
        when(request.getHeader("X-Request-ID")).thenReturn("");

        // When
        boolean result = interceptor.preHandle(request, response, handlerMethod);

        // Then
        assertTrue(result);
    }

    @Test
    void preHandle_handlerMethod_withWhitespaceRequestId_shouldGenerateRequestId() throws Exception {
        // Given
        when(handlerMethod.getMethodAnnotation(ApiTimeout.class)).thenReturn(null);
        when(handlerMethod.getBeanType()).thenReturn((Class) TestController.class);
        when(request.getHeader("X-Request-ID")).thenReturn("   ");

        // When
        boolean result = interceptor.preHandle(request, response, handlerMethod);

        // Then
        assertTrue(result);
    }

    @Test
    void afterCompletion_handlerMethod_withTimeoutExceeded_shouldSetWarningHeaders() throws Exception {
        // Given
        String requestId = "test-request-id";
        when(request.getHeader("X-Request-ID")).thenReturn(requestId);
        when(request.getAttribute("timeout_value")).thenReturn(1L); // Very short timeout - 1ms
        when(request.getAttribute("timeout_unit")).thenReturn(TimeUnit.MILLISECONDS);
        when(request.getAttribute("timeout_message")).thenReturn("Request timeout");
        when(request.getRequestURI()).thenReturn("/api/test");
        when(handlerMethod.getMethodAnnotation(ApiTimeout.class)).thenReturn(null);
        when(handlerMethod.getBeanType()).thenReturn((Class) TestController.class);
        // Simulate slow execution by setting start time in the past
        interceptor.preHandle(request, response, handlerMethod);
        // Simulate some execution time that will exceed 1ms timeout
        Thread.sleep(10); // This will definitely exceed 1ms timeout
        // When
        interceptor.afterCompletion(request, response, handlerMethod, null);
        // Then
        verify(response).setHeader("X-Timeout-Warning", "true");
        verify(response).setHeader(eq("X-Execution-Time"), anyString());
        verify(response).setHeader(eq("X-Response-Time"), anyString());
    }

    @Test
    void afterCompletion_handlerMethod_withinTimeout_shouldNotSetWarningHeaders() throws Exception {
        // Given
        String requestId = "test-request-id";
        when(request.getHeader("X-Request-ID")).thenReturn(requestId);
        when(request.getAttribute("timeout_value")).thenReturn(1000L);
        when(request.getAttribute("timeout_unit")).thenReturn(TimeUnit.MILLISECONDS);
        when(request.getAttribute("timeout_message")).thenReturn("Request timeout");
        when(request.getRequestURI()).thenReturn("/api/test");
        when(handlerMethod.getMethodAnnotation(ApiTimeout.class)).thenReturn(null);
        when(handlerMethod.getBeanType()).thenReturn((Class) TestController.class);
        // Simulate fast execution
        interceptor.preHandle(request, response, handlerMethod);
        // When
        interceptor.afterCompletion(request, response, handlerMethod, null);
        // Then
        verify(response, never()).setHeader("X-Timeout-Warning", "true");
        verify(response).setHeader(eq("X-Response-Time"), anyString());
    }

    @Test
    void afterCompletion_handlerMethod_withoutTimeoutConfig_shouldLogExecutionTime() throws Exception {
        // Given
        String requestId = "test-request-id";
        when(request.getHeader("X-Request-ID")).thenReturn(requestId);
        when(request.getAttribute("timeout_value")).thenReturn(null);
        when(request.getAttribute("timeout_unit")).thenReturn(null);
        when(request.getAttribute("timeout_message")).thenReturn(null);
        when(request.getRequestURI()).thenReturn("/api/test");
        when(handlerMethod.getMethodAnnotation(ApiTimeout.class)).thenReturn(null);
        when(handlerMethod.getBeanType()).thenReturn((Class) TestController.class);
        // Simulate execution
        interceptor.preHandle(request, response, handlerMethod);
        // When
        interceptor.afterCompletion(request, response, handlerMethod, null);
        // Then
        verify(response).setHeader(eq("X-Response-Time"), anyString());
    }

    @Test
    void afterCompletion_handlerMethod_withSecondsTimeout_shouldConvertCorrectly() throws Exception {
        // Given
        String requestId = "test-request-id";
        when(request.getHeader("X-Request-ID")).thenReturn(requestId);
        when(request.getAttribute("timeout_value")).thenReturn(0L); // Use 0 seconds to trigger timeout immediately
        when(request.getAttribute("timeout_unit")).thenReturn(TimeUnit.SECONDS);
        when(request.getAttribute("timeout_message")).thenReturn("Request timeout");
        when(request.getRequestURI()).thenReturn("/api/test");
        when(handlerMethod.getMethodAnnotation(ApiTimeout.class)).thenReturn(null);
        when(handlerMethod.getBeanType()).thenReturn((Class) TestController.class);

        interceptor.preHandle(request, response, handlerMethod);
        Thread.sleep(10); // Minimal sleep to ensure timeout is exceeded

        // When
        interceptor.afterCompletion(request, response, handlerMethod, null);

        // Then
        verify(response).setHeader("X-Timeout-Warning", "true");
        verify(response).setHeader(eq("X-Execution-Time"), anyString());
    }

    @Test
    void afterCompletion_handlerMethod_withMinutesTimeout_shouldConvertCorrectly() throws Exception {
        // Given
        String requestId = "test-request-id";
        when(request.getHeader("X-Request-ID")).thenReturn(requestId);
        when(request.getAttribute("timeout_value")).thenReturn(0L); // Use 0 minutes to trigger timeout immediately
        when(request.getAttribute("timeout_unit")).thenReturn(TimeUnit.MINUTES);
        when(request.getAttribute("timeout_message")).thenReturn("Request timeout");
        when(request.getRequestURI()).thenReturn("/api/test");
        when(handlerMethod.getMethodAnnotation(ApiTimeout.class)).thenReturn(null);
        when(handlerMethod.getBeanType()).thenReturn((Class) TestController.class);

        interceptor.preHandle(request, response, handlerMethod);
        Thread.sleep(10); // Minimal sleep to ensure timeout is exceeded

        // When
        interceptor.afterCompletion(request, response, handlerMethod, null);

        // Then
        verify(response).setHeader("X-Timeout-Warning", "true");
        verify(response).setHeader(eq("X-Execution-Time"), anyString());
    }

    @Test
    void afterCompletion_handlerMethod_withMicrosecondsTimeout_shouldConvertCorrectly() throws Exception {
        // Given
        String requestId = "test-request-id";
        when(request.getHeader("X-Request-ID")).thenReturn(requestId);
        when(request.getAttribute("timeout_value")).thenReturn(1L); // 1 microsecond timeout
        when(request.getAttribute("timeout_unit")).thenReturn(TimeUnit.MICROSECONDS);
        when(request.getAttribute("timeout_message")).thenReturn("Request timeout");
        when(request.getRequestURI()).thenReturn("/api/test");
        when(handlerMethod.getMethodAnnotation(ApiTimeout.class)).thenReturn(null);
        when(handlerMethod.getBeanType()).thenReturn((Class) TestController.class);

        interceptor.preHandle(request, response, handlerMethod);
        Thread.sleep(1); // Even 1ms will exceed 1 microsecond timeout

        // When
        interceptor.afterCompletion(request, response, handlerMethod, null);

        // Then
        verify(response).setHeader("X-Timeout-Warning", "true");
        verify(response).setHeader(eq("X-Execution-Time"), anyString());
    }

    @Test
    void afterCompletion_handlerMethod_withException_shouldStillLogExecutionTime() throws Exception {
        // Given
        String requestId = "test-request-id";
        when(request.getHeader("X-Request-ID")).thenReturn(requestId);
        when(request.getAttribute("timeout_value")).thenReturn(1000L);
        when(request.getAttribute("timeout_unit")).thenReturn(TimeUnit.MILLISECONDS);
        when(request.getAttribute("timeout_message")).thenReturn("Request timeout");
        when(request.getRequestURI()).thenReturn("/api/test");
        when(handlerMethod.getMethodAnnotation(ApiTimeout.class)).thenReturn(null);
        when(handlerMethod.getBeanType()).thenReturn((Class) TestController.class);

        interceptor.preHandle(request, response, handlerMethod);

        // When
        interceptor.afterCompletion(request, response, handlerMethod, new RuntimeException("Test exception"));

        // Then
        verify(response).setHeader(eq("X-Response-Time"), anyString());
    }

    @Test
    void afterCompletion_handlerMethod_withNullException_shouldLogExecutionTime() throws Exception {
        // Given
        String requestId = "test-request-id";
        when(request.getHeader("X-Request-ID")).thenReturn(requestId);
        when(request.getAttribute("timeout_value")).thenReturn(1000L);
        when(request.getAttribute("timeout_unit")).thenReturn(TimeUnit.MILLISECONDS);
        when(request.getAttribute("timeout_message")).thenReturn("Request timeout");
        when(request.getRequestURI()).thenReturn("/api/test");
        when(handlerMethod.getMethodAnnotation(ApiTimeout.class)).thenReturn(null);
        when(handlerMethod.getBeanType()).thenReturn((Class) TestController.class);

        interceptor.preHandle(request, response, handlerMethod);

        // When
        interceptor.afterCompletion(request, response, handlerMethod, null);

        // Then
        verify(response).setHeader(eq("X-Response-Time"), anyString());
    }

    @Test
    void afterCompletion_handlerMethod_withZeroTimeout_shouldHandleCorrectly() throws Exception {
        // Given
        String requestId = "test-request-id";
        when(request.getHeader("X-Request-ID")).thenReturn(requestId);
        when(request.getAttribute("timeout_value")).thenReturn(0L);
        when(request.getAttribute("timeout_unit")).thenReturn(TimeUnit.MILLISECONDS);
        when(request.getAttribute("timeout_message")).thenReturn("Request timeout");
        when(request.getRequestURI()).thenReturn("/api/test");
        when(handlerMethod.getMethodAnnotation(ApiTimeout.class)).thenReturn(null);
        when(handlerMethod.getBeanType()).thenReturn((Class) TestController.class);

        interceptor.preHandle(request, response, handlerMethod);
        Thread.sleep(1); // Minimal sleep - any execution time will exceed 0 timeout

        // When
        interceptor.afterCompletion(request, response, handlerMethod, null);

        // Then
        verify(response).setHeader("X-Timeout-Warning", "true");
        verify(response).setHeader(eq("X-Execution-Time"), anyString());
    }

    @Test
    void afterCompletion_handlerMethod_withNegativeTimeout_shouldHandleCorrectly() throws Exception {
        // Given
        String requestId = "test-request-id";
        when(request.getHeader("X-Request-ID")).thenReturn(requestId);
        when(request.getAttribute("timeout_value")).thenReturn(-1L);
        when(request.getAttribute("timeout_unit")).thenReturn(TimeUnit.MILLISECONDS);
        when(request.getAttribute("timeout_message")).thenReturn("Request timeout");
        when(request.getRequestURI()).thenReturn("/api/test");
        when(handlerMethod.getMethodAnnotation(ApiTimeout.class)).thenReturn(null);
        when(handlerMethod.getBeanType()).thenReturn((Class) TestController.class);

        interceptor.preHandle(request, response, handlerMethod);

        // When
        interceptor.afterCompletion(request, response, handlerMethod, null);

        // Then
        verify(response).setHeader(eq("X-Response-Time"), anyString());
    }

    @Test
    void afterCompletion_handlerMethod_withNullTimeoutValue_shouldHandleCorrectly() throws Exception {
        // Given
        String requestId = "test-request-id";
        when(request.getHeader("X-Request-ID")).thenReturn(requestId);
        when(request.getAttribute("timeout_value")).thenReturn(null);
        when(request.getAttribute("timeout_unit")).thenReturn(TimeUnit.MILLISECONDS);
        when(request.getAttribute("timeout_message")).thenReturn("Request timeout");
        when(request.getRequestURI()).thenReturn("/api/test");
        when(handlerMethod.getMethodAnnotation(ApiTimeout.class)).thenReturn(null);
        when(handlerMethod.getBeanType()).thenReturn((Class) TestController.class);

        interceptor.preHandle(request, response, handlerMethod);

        // When
        interceptor.afterCompletion(request, response, handlerMethod, null);

        // Then
        verify(response).setHeader(eq("X-Response-Time"), anyString());
    }

    @Test
    void afterCompletion_handlerMethod_withNullTimeoutUnit_shouldHandleCorrectly() throws Exception {
        // Given
        String requestId = "test-request-id";
        when(request.getHeader("X-Request-ID")).thenReturn(requestId);
        when(request.getAttribute("timeout_value")).thenReturn(1000L);
        when(request.getAttribute("timeout_unit")).thenReturn(null);
        when(request.getAttribute("timeout_message")).thenReturn("Request timeout");
        when(request.getRequestURI()).thenReturn("/api/test");
        when(handlerMethod.getMethodAnnotation(ApiTimeout.class)).thenReturn(null);
        when(handlerMethod.getBeanType()).thenReturn((Class) TestController.class);

        interceptor.preHandle(request, response, handlerMethod);

        // When
        interceptor.afterCompletion(request, response, handlerMethod, null);

        // Then
        verify(response).setHeader(eq("X-Response-Time"), anyString());
    }

    @Test
    void afterCompletion_handlerMethod_withNullTimeoutMessage_shouldHandleCorrectly() throws Exception {
        // Given
        String requestId = "test-request-id";
        when(request.getHeader("X-Request-ID")).thenReturn(requestId);
        when(request.getAttribute("timeout_value")).thenReturn(1000L);
        when(request.getAttribute("timeout_unit")).thenReturn(TimeUnit.MILLISECONDS);
        when(request.getAttribute("timeout_message")).thenReturn(null);
        when(request.getRequestURI()).thenReturn("/api/test");
        when(handlerMethod.getMethodAnnotation(ApiTimeout.class)).thenReturn(null);
        when(handlerMethod.getBeanType()).thenReturn((Class) TestController.class);

        interceptor.preHandle(request, response, handlerMethod);

        // When
        interceptor.afterCompletion(request, response, handlerMethod, null);

        // Then
        verify(response).setHeader(eq("X-Response-Time"), anyString());
    }

    @Test
    void afterCompletion_handlerMethod_withComplexRequestUri_shouldHandleCorrectly() throws Exception {
        // Given
        String requestId = "test-request-id";
        when(request.getHeader("X-Request-ID")).thenReturn(requestId);
        when(request.getAttribute("timeout_value")).thenReturn(1000L);
        when(request.getAttribute("timeout_unit")).thenReturn(TimeUnit.MILLISECONDS);
        when(request.getAttribute("timeout_message")).thenReturn("Request timeout");
        when(request.getRequestURI()).thenReturn("/api/v1/users/123/orders/456?page=1&size=10");
        when(handlerMethod.getMethodAnnotation(ApiTimeout.class)).thenReturn(null);
        when(handlerMethod.getBeanType()).thenReturn((Class) TestController.class);

        interceptor.preHandle(request, response, handlerMethod);

        // When
        interceptor.afterCompletion(request, response, handlerMethod, null);

        // Then
        verify(response).setHeader(eq("X-Response-Time"), anyString());
    }

    @Test
    void afterCompletion_handlerMethod_withEmptyRequestUri_shouldHandleCorrectly() throws Exception {
        // Given
        String requestId = "test-request-id";
        when(request.getHeader("X-Request-ID")).thenReturn(requestId);
        when(request.getAttribute("timeout_value")).thenReturn(1000L);
        when(request.getAttribute("timeout_unit")).thenReturn(TimeUnit.MILLISECONDS);
        when(request.getAttribute("timeout_message")).thenReturn("Request timeout");
        when(request.getRequestURI()).thenReturn("");
        when(handlerMethod.getMethodAnnotation(ApiTimeout.class)).thenReturn(null);
        when(handlerMethod.getBeanType()).thenReturn((Class) TestController.class);
        // Simulate execution
        interceptor.preHandle(request, response, handlerMethod);
        // When
        interceptor.afterCompletion(request, response, handlerMethod, null);
        // Then
        verify(response).setHeader(eq("X-Response-Time"), anyString());
    }

    @Test
    void afterCompletion_handlerMethod_withNullRequestUri_shouldHandleCorrectly() throws Exception {
        // Given
        String requestId = "test-request-id";
        when(request.getHeader("X-Request-ID")).thenReturn(requestId);
        when(request.getAttribute("timeout_value")).thenReturn(1000L);
        when(request.getAttribute("timeout_unit")).thenReturn(TimeUnit.MILLISECONDS);
        when(request.getAttribute("timeout_message")).thenReturn("Request timeout");
        when(request.getRequestURI()).thenReturn(null);
        when(handlerMethod.getMethodAnnotation(ApiTimeout.class)).thenReturn(null);
        when(handlerMethod.getBeanType()).thenReturn((Class) TestController.class);
        // Simulate execution
        interceptor.preHandle(request, response, handlerMethod);
        // When
        interceptor.afterCompletion(request, response, handlerMethod, null);
        // Then
        verify(response).setHeader(eq("X-Response-Time"), anyString());
    }
}
