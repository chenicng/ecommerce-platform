package com.ecommerce.api.interceptor;

import com.ecommerce.api.annotation.ApiTimeout;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Timeout Control Interceptor
 * 
 * Monitors API request execution time and logs warnings for requests
 * that exceed the specified timeout threshold.
 */
@Component
public class TimeoutInterceptor implements HandlerInterceptor {
    
    private static final Logger logger = LoggerFactory.getLogger(TimeoutInterceptor.class);
    
    // Store request start times
    private static final ConcurrentHashMap<String, Long> requestStartTimes = new ConcurrentHashMap<>();
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod handlerMethod) {
            // Store the start time for this request
            String requestId = getRequestId(request);
            requestStartTimes.put(requestId, System.currentTimeMillis());
            
            // Get timeout configuration
            ApiTimeout timeout = getTimeoutAnnotation(handlerMethod);
            if (timeout != null) {
                // Store timeout info in request attributes for postHandle
                request.setAttribute("timeout_value", timeout.value());
                request.setAttribute("timeout_unit", timeout.unit());
                request.setAttribute("timeout_message", timeout.message());
            }
        }
        return true;
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        if (handler instanceof HandlerMethod) {
            String requestId = getRequestId(request);
            Long startTime = requestStartTimes.remove(requestId);
            
            if (startTime != null) {
                long executionTime = System.currentTimeMillis() - startTime;
                
                // Check if timeout configuration exists
                Long timeoutValue = (Long) request.getAttribute("timeout_value");
                TimeUnit timeoutUnit = (TimeUnit) request.getAttribute("timeout_unit");
                String timeoutMessage = (String) request.getAttribute("timeout_message");
                
                if (timeoutValue != null && timeoutUnit != null) {
                    long timeoutMillis = timeoutUnit.toMillis(timeoutValue);
                    
                    if (executionTime > timeoutMillis) {
                        logger.warn("Request timeout detected: {} - Execution time: {}ms, Timeout: {}ms, Message: {}", 
                                   request.getRequestURI(), executionTime, timeoutMillis, timeoutMessage);
                        
                        // Add timeout warning to response header
                        response.setHeader("X-Timeout-Warning", "true");
                        response.setHeader("X-Execution-Time", String.valueOf(executionTime));
                    } else {
                        logger.debug("Request completed within timeout: {} - Execution time: {}ms, Timeout: {}ms", 
                                    request.getRequestURI(), executionTime, timeoutMillis);
                    }
                } else {
                    // Log execution time even without timeout configuration
                    logger.debug("Request completed: {} - Execution time: {}ms", 
                                request.getRequestURI(), executionTime);
                }
                
                // Always add execution time header for monitoring
                response.setHeader("X-Response-Time", String.valueOf(executionTime));
            }
        }
    }
    
    /**
     * Get timeout annotation from method or class
     */
    private ApiTimeout getTimeoutAnnotation(HandlerMethod handlerMethod) {
        // First check method-level annotation
        ApiTimeout methodTimeout = handlerMethod.getMethodAnnotation(ApiTimeout.class);
        if (methodTimeout != null) {
            return methodTimeout;
        }
        
        // Then check class-level annotation
        return handlerMethod.getBeanType().getAnnotation(ApiTimeout.class);
    }
    
    /**
     * Get request ID from header or generate a simple one
     */
    private String getRequestId(HttpServletRequest request) {
        String requestId = request.getHeader("X-Request-ID");
        if (requestId == null || requestId.trim().isEmpty()) {
            // Fallback to thread name + timestamp
            requestId = Thread.currentThread().getName() + "_" + System.currentTimeMillis();
        }
        return requestId;
    }
} 