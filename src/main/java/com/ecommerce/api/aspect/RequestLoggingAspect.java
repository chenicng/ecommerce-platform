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
import java.util.regex.Pattern;

/**
 * Enhanced Logging Aspect for API Requests
 *
 * Automatically logs all API requests including URL, parameters, response data, and execution time.
 * Provides comprehensive request tracing and monitoring capabilities with enhanced data masking.
 * 
 * Features:
 * - Truncates long messages to 1024 characters
 * - Masks sensitive information (passwords, tokens, etc.)
 * - Masks phone numbers and email addresses
 * - Configurable sensitive field patterns
 */
@Aspect
@Component
public class RequestLoggingAspect {
    private static final Logger logger = LoggerFactory.getLogger(RequestLoggingAspect.class);
    private static final ObjectMapper objectMapper = new ObjectMapper().configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    
    // Configuration constants
    private static final int MAX_LOG_LENGTH = 1024;
    private static final String TRUNCATE_SUFFIX = "...[TRUNCATED]";
    
    // Configurable sensitive fields for automatic masking
    private static final Set<String> SENSITIVE_FIELDS = new HashSet<>(Arrays.asList(
        "password", "token", "secret", "key", "authorization", "apiKey", "accessToken", 
        "refreshToken", "sessionId", "cookie", "auth", "credential"
    ));
    
    // Patterns for sensitive data masking
    private static final Pattern PHONE_PATTERN = Pattern.compile("(\\d{3})(\\d{4})(\\d{4})");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("([a-zA-Z0-9._%+-]+)@([a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})");
    private static final Pattern ID_CARD_PATTERN = Pattern.compile("(\\d{6})(\\d{8})(\\d{4})");
    private static final Pattern BANK_CARD_PATTERN = Pattern.compile("(\\d{4})(\\d{4})(\\d{4})(\\d{4})");

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
                params = maskSensitiveData(params);
                params = truncateIfNeeded(params);
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
                respSummary = maskSensitiveData(respSummary);
                respSummary = truncateIfNeeded(respSummary);
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
     * Print Result's core fields: code, message, and data content
     */
    private String extractResultSummary(Object result) {
        if (result == null) return "null";
        Object body = result;
        if (result instanceof ResponseEntity<?>) {
            body = ((ResponseEntity<?>) result).getBody();
        }
        if (body instanceof Result<?>) {
            Result<?> res = (Result<?>) body;
            String dataContent = "null";
            if (res.getData() != null) {
                dataContent = String.valueOf(res.getData());
            }
            return String.format("{code: %s, message: %s, data: %s}", res.getCode(), res.getMessage(), dataContent);
        }
        // fallback: try toString or JSON
        try {
            return objectMapper.writeValueAsString(body);
        } catch (Exception e) {
            return String.valueOf(body);
        }
    }

    /**
     * Enhanced sensitive data masking implementation
     * Masks passwords, tokens, phone numbers, email addresses, ID cards, and bank cards
     */
    private String maskSensitiveData(String content) {
        if (content == null || content.isEmpty()) {
            return content;
        }
        
        String masked = content;
        
        // Mask sensitive JSON fields
        for (String field : SENSITIVE_FIELDS) {
            masked = masked.replaceAll("\\\"" + field + "\\\"\\s*:\\s*\\\"[^\\\"]*\\\"", "\\\"" + field + "\\\":\\\"***\\\"");
        }
        
        // Mask phone numbers (11 digits)
        masked = PHONE_PATTERN.matcher(masked).replaceAll("$1****$3");
        
        // Mask email addresses
        masked = EMAIL_PATTERN.matcher(masked).replaceAll("$1***@$2");
        
        // Mask ID card numbers (18 digits)
        masked = ID_CARD_PATTERN.matcher(masked).replaceAll("$1********$3");
        
        // Mask bank card numbers (16 digits)
        masked = BANK_CARD_PATTERN.matcher(masked).replaceAll("$1****$3****$4");
        
        return masked;
    }
    
    /**
     * Truncates content if it exceeds the maximum log length
     */
    private String truncateIfNeeded(String content) {
        if (content == null) {
            return null;
        }
        
        if (content.length() <= MAX_LOG_LENGTH) {
            return content;
        }
        
        int truncateLength = MAX_LOG_LENGTH - TRUNCATE_SUFFIX.length();
        return content.substring(0, truncateLength) + TRUNCATE_SUFFIX;
    }
}