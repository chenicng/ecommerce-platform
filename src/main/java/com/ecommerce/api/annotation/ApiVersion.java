package com.ecommerce.api.annotation;

import java.lang.annotation.*;

/**
 * API version annotation
 * 
 * Used to mark API version information
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiVersion {
    
    /**
     * API version number
     * @return version number, such as "v1", "v2"
     */
    String value() default "v1";
    
    /**
     * Whether it is deprecated
     * @return true indicates deprecated
     */
    boolean deprecated() default false;
    
    /**
     * Deprecation message
     * @return deprecation reason and alternative solution
     */
    String deprecationMessage() default "";
    
    /**
     * Version introduction time
     * @return version introduction time, format: yyyy-MM-dd
     */
    String since() default "";
    
    /**
     * Planned removal time
     * @return planned removal time, format: yyyy-MM-dd
     */
    String until() default "";
} 