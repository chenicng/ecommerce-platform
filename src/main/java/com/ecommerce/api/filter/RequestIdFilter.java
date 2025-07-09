package com.ecommerce.api.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

/**
 * Request ID Filter
 * 
 * Generates a unique request ID for each HTTP request and adds it to MDC for logging.
 * This enables request tracing across all log statements within the same request context.
 */
@Component
@Order(1) // Execute early in the filter chain
public class RequestIdFilter implements Filter {
    
    private static final String REQUEST_ID_HEADER = "X-Request-ID";
    private static final String MDC_REQUEST_ID_KEY = "requestId";
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        try {
            // Generate or extract request ID
            String requestId = getOrGenerateRequestId(httpRequest);
            
            // Add request ID to MDC for logging
            MDC.put(MDC_REQUEST_ID_KEY, requestId);
            
            // Add request ID to response header
            httpResponse.setHeader(REQUEST_ID_HEADER, requestId);
            
            // Continue with the filter chain
            chain.doFilter(request, response);
            
        } finally {
            // Clean up MDC to prevent memory leaks
            MDC.clear();
        }
    }
    
    /**
     * Get request ID from header or generate a new one
     */
    private String getOrGenerateRequestId(HttpServletRequest request) {
        String requestId = request.getHeader(REQUEST_ID_HEADER);
        if (requestId == null || requestId.trim().isEmpty()) {
            requestId = generateRequestId();
        }
        return requestId;
    }
    
    /**
     * Generate a new unique request ID
     */
    private String generateRequestId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // No initialization needed
    }
    
    @Override
    public void destroy() {
        // No cleanup needed
    }
} 