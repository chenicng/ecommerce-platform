package com.ecommerce.api.aspect;

import com.ecommerce.api.dto.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Unified Logging Aspect for API Requests
 *
 * Automatically logs all API requests including URL, parameters, response data, and execution time.
 * Provides comprehensive request tracing and monitoring capabilities.
 */
@Aspect
@Component
public class RequestLoggingAspect {
    private static final Logger logger = LoggerFactory.getLogger(RequestLoggingAspect.class);
    private static final ObjectMapper objectMapper = new ObjectMapper().configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    // Configurable sensitive fields for automatic masking
    private static final Set<String> SENSITIVE_FIELDS = new HashSet<>(Arrays.asList("password", "token", "secret", "key", "authorization"));

    @Around("execution(* com.ecommerce.api.controller..*.*(..))")
    public Object logApiRequest(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attrs != null ? attrs.getRequest() : null;
        String requestId = request != null ? request.getHeader("X-Request-ID") : "N/A";
        String url = request != null ? request.getRequestURI() : "N/A";
        String method = request != null ? request.getMethod() : "N/A";
        String query = request != null ? request.getQueryString() : null;

        // Log request parameters
        String params = "";
        try {
            Object[] args = joinPoint.getArgs();
            if (args != null && args.length > 0) {
                params = objectMapper.writeValueAsString(args.length == 1 ? args[0] : args);
                params = maskSensitive(params);
            }
        } catch (Exception e) {
            params = "[Parameter serialization failed]";
        }

        logger.info("[REQUEST] {} {}{} - RequestID: {} - Params: {}", method, url, query != null ? ("?" + query) : "", requestId, params);

        Object result = null;
        Throwable ex = null;
        try {
            result = joinPoint.proceed();
            return result;
        } catch (Throwable e) {
            ex = e;
            throw e;
        } finally {
            long cost = System.currentTimeMillis() - start;
            String respSummary = "";
            try {
                respSummary = extractResultSummary(result);
            } catch (Exception e) {
                respSummary = "[Response summary failed]";
            }
            if (ex == null) {
                logger.info("[RESPONSE] {} {} - RequestID: {} - Cost: {}ms - Result: {}", method, url, requestId, cost, respSummary);
            } else {
                logger.error("[RESPONSE-ERROR] {} {} - RequestID: {} - Cost: {}ms - Exception: {}", method, url, requestId, cost, ex.getMessage(), ex);
            }
        }
    }

    /**
     * Only print Result's core fields: code, message, data type (if present)
     */
    private String extractResultSummary(Object result) {
        if (result == null) return "null";
        Object body = result;
        if (result instanceof ResponseEntity<?>) {
            body = ((ResponseEntity<?>) result).getBody();
        }
        if (body instanceof Result<?>) {
            Result<?> res = (Result<?>) body;
            String dataType = res.getData() != null ? res.getData().getClass().getSimpleName() : "null";
            return String.format("{code: %s, message: %s, dataType: %s}", res.getCode(), res.getMessage(), dataType);
        }
        // fallback: try toString or JSON
        try {
            return maskSensitive(objectMapper.writeValueAsString(body));
        } catch (Exception e) {
            return String.valueOf(body);
        }
    }

    /**
     * Simple sensitive data masking implementation
     * Replaces sensitive field values with "***" in JSON strings
     */
    private String maskSensitive(String json) {
        String masked = json;
        for (String field : SENSITIVE_FIELDS) {
            masked = masked.replaceAll("\\\"" + field + "\\\"\\s*:\\s*\\\"[^\\\"]*\\\"", "\\\"" + field + "\\\":\\\"***\\\"");
        }
        return masked;
    }
}