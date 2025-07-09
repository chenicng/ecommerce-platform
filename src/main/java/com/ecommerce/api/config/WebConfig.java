package com.ecommerce.api.config;

import com.ecommerce.api.interceptor.ApiVersionInterceptor;
import org.springframework.context.annotation.Configuration;
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
    
    public WebConfig(ApiVersionInterceptor apiVersionInterceptor) {
        this.apiVersionInterceptor = apiVersionInterceptor;
    }
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Register API version control interceptor
        registry.addInterceptor(apiVersionInterceptor)
                .addPathPatterns("/api/**")  // Intercept all API requests
                .excludePathPatterns(
                    "/api/health",      // Health check doesn't need version control
                    "/api/healthz",     // Health check doesn't need version control
                    "/actuator/**"      // Actuator endpoints don't need version control
                );
    }
} 