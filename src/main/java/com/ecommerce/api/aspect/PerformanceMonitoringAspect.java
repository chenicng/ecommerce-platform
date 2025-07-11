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
 * Monitors execution time of critical business operations
 * and logs warnings for slow operations
 */
@Aspect
@Component
public class PerformanceMonitoringAspect {
    
    private static final Logger logger = LoggerFactory.getLogger(PerformanceMonitoringAspect.class);
    private static final long SLOW_OPERATION_THRESHOLD_MS = 1000; // 1 second
    
    @Around("execution(* com.ecommerce.application.service.*.*(..))")
    public Object monitorServicePerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        
        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;
            
            if (executionTime > SLOW_OPERATION_THRESHOLD_MS) {
                logger.warn("Slow operation detected: {}.{} took {}ms", 
                    className, methodName, executionTime);
            } else {
                logger.debug("Operation completed: {}.{} took {}ms", 
                    className, methodName, executionTime);
            }
            
            return result;
        } catch (Throwable e) {
            long executionTime = System.currentTimeMillis() - startTime;
            logger.error("Operation failed: {}.{} took {}ms with error: {}", 
                className, methodName, executionTime, e.getMessage());
            throw e;
        }
    }
} 