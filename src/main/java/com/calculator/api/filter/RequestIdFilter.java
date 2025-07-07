package com.calculator.api.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * 请求ID过滤器
 * 为每个HTTP请求生成唯一的追踪ID，并添加到响应头和日志上下文中
 */
@Component
@Order(1)
public class RequestIdFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(RequestIdFilter.class);
    private static final String REQUEST_ID_HEADER = "X-Request-ID";
    private static final String REQUEST_ID_MDC_KEY = "requestId";

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        // 从请求头获取或生成新的请求ID
        String requestId = request.getHeader(REQUEST_ID_HEADER);
        if (requestId == null || requestId.trim().isEmpty()) {
            requestId = generateRequestId();
        }

        // 将请求ID添加到MDC中，用于日志追踪
        MDC.put(REQUEST_ID_MDC_KEY, requestId);
        
        // 将请求ID添加到响应头
        response.setHeader(REQUEST_ID_HEADER, requestId);

        try {
            logger.info("Processing {} request for URI: {} with Request-ID: {}", 
                       request.getMethod(), request.getRequestURI(), requestId);
            
            long startTime = System.currentTimeMillis();
            filterChain.doFilter(request, response);
            long endTime = System.currentTimeMillis();
            
            logger.info("Completed {} request for URI: {} with status: {} in {}ms", 
                       request.getMethod(), request.getRequestURI(), response.getStatus(), (endTime - startTime));
        } finally {
            // 清理MDC，避免内存泄漏
            MDC.remove(REQUEST_ID_MDC_KEY);
        }
    }

    /**
     * 生成唯一的请求ID
     * 
     * @return 请求ID字符串
     */
    private String generateRequestId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }
} 