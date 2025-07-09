package com.ecommerce.api.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * API Timeout Annotation
 * 
 * Used to mark API timeout settings for specific endpoints.
 * This annotation can be applied to controller methods to specify timeout values.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiTimeout {
    
    /**
     * Timeout value
     * @return timeout value (default: 30)
     */
    long value() default 30;
    
    /**
     * Time unit
     * @return time unit (default: SECONDS)
     */
    TimeUnit unit() default TimeUnit.SECONDS;
    
    /**
     * Timeout message
     * @return custom timeout message
     */
    String message() default "Request timeout";
} 