package com.ecommerce.api.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Performance Monitoring Aspect
 * 
 * Monitors and logs performance metrics for controller methods.
 * Provides execution time tracking and slow query detection.
 */
@Aspect
@Component
public class PerformanceMonitoringAspect {
    
    private static final Logger logger = LoggerFactory.getLogger(PerformanceMonitoringAspect.class);
    
    // Threshold for slow API warning (in milliseconds)
    private static final long SLOW_API_THRESHOLD = 2000; // 2 seconds
    
    /**
     * Monitor all controller methods
     */
    @Around("execution(* com.ecommerce.api.controller..*.*(..))")
    public Object monitorApiPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String methodName = joinPoint.getSignature().toShortString();
        
        try {
            logger.debug("API call started: {}", methodName);
            
            // Execute the method
            Object result = joinPoint.proceed();
            
            long executionTime = System.currentTimeMillis() - startTime;
            
            // Log performance metrics
            if (executionTime > SLOW_API_THRESHOLD) {
                logger.warn("Slow API detected: {} - Execution time: {}ms", methodName, executionTime);
            } else {
                logger.info("API completed: {} - Execution time: {}ms", methodName, executionTime);
            }
            
            return result;
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            logger.error("API failed: {} - Execution time: {}ms, Error: {}", 
                        methodName, executionTime, e.getMessage());
            throw e;
        }
    }
    
    /**
     * Monitor service layer methods for business logic performance
     */
    @Around("execution(* com.ecommerce.application.service..*.*(..))")
    public Object monitorServicePerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String methodName = joinPoint.getSignature().toShortString();
        
        try {
            Object result = joinPoint.proceed();
            
            long executionTime = System.currentTimeMillis() - startTime;
            
            // Only log slow service operations to avoid noise
            if (executionTime > 1000) { // 1 second threshold for services
                logger.warn("Slow service operation: {} - Execution time: {}ms", methodName, executionTime);
            } else {
                logger.debug("Service completed: {} - Execution time: {}ms", methodName, executionTime);
            }
            
            return result;
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            logger.error("Service failed: {} - Execution time: {}ms, Error: {}", 
                        methodName, executionTime, e.getMessage());
            throw e;
        }
    }
} 