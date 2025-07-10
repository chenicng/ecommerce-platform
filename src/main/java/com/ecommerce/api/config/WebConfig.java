package com.ecommerce.api.config;

import com.ecommerce.api.interceptor.ApiVersionInterceptor;
import com.ecommerce.api.interceptor.TimeoutInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web configuration class
 * 
 * Configure interceptors, CORS and other web-related settings
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    private final ApiVersionInterceptor apiVersionInterceptor;
    private final TimeoutInterceptor timeoutInterceptor;
    
    public WebConfig(ApiVersionInterceptor apiVersionInterceptor, TimeoutInterceptor timeoutInterceptor) {
        this.apiVersionInterceptor = apiVersionInterceptor;
        this.timeoutInterceptor = timeoutInterceptor;
    }
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Register timeout interceptor first (lower order = higher priority)
        registry.addInterceptor(timeoutInterceptor)
                .addPathPatterns("/api/**")  // Monitor all API requests
                .excludePathPatterns(
                    "/actuator/**"  // Exclude actuator endpoints
                );
        
        // Register API version control interceptor
        registry.addInterceptor(apiVersionInterceptor)
                .addPathPatterns("/api/**")  // Intercept all API requests
                .excludePathPatterns(
                    "/api/health",      // Health check doesn't need version control
                    "/actuator/**"      // Actuator endpoints don't need version control
                );
    }
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // Configure CORS for API endpoints
        registry.addMapping("/api/**")
                .allowedOriginPatterns("*")  // Allow all origins in development
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600); // Cache preflight response for 1 hour
        
        // Configure CORS for Swagger UI
        registry.addMapping("/swagger-ui/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET")
                .allowedHeaders("*")
                .maxAge(3600);
    }
} 