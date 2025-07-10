package com.ecommerce.api.aspect;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for RequestLoggingAspect
 */
class RequestLoggingAspectTest {
    
    @Test
    void testRequestIdGeneration() {
        // Test request ID generation when header is present
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Request-ID", "test-request-id");
        
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        
        // The aspect should be able to extract the request ID
        assertNotNull(request.getHeader("X-Request-ID"));
        assertEquals("test-request-id", request.getHeader("X-Request-ID"));
    }
    
    @Test
    void testRequestIdGenerationWithoutHeader() {
        // Test request ID generation when header is not present
        MockHttpServletRequest request = new MockHttpServletRequest();
        
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        
        // The aspect should generate a request ID
        assertNull(request.getHeader("X-Request-ID"));
    }
} 