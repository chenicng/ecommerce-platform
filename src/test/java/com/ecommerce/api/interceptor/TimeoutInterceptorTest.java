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
}
