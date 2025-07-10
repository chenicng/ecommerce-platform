package com.ecommerce.api.interceptor;

import com.ecommerce.api.annotation.ApiVersion;
import com.ecommerce.api.config.ApiVersionConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * API version control interceptor
 * 
 * Handle API version control logic, including version validation, deprecation warnings, etc.
 */
@Component
public class ApiVersionInterceptor implements HandlerInterceptor {
    
    private static final Logger logger = LoggerFactory.getLogger(ApiVersionInterceptor.class);
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }
        
        // Get version annotation on method
        ApiVersion methodVersion = handlerMethod.getMethodAnnotation(ApiVersion.class);
        
        // If no version annotation on method, check class-level version annotation
        if (methodVersion == null) {
            methodVersion = handlerMethod.getBeanType().getAnnotation(ApiVersion.class);
        }
        
        // If no version annotation, use default version
        if (methodVersion == null) {
            return true;
        }
        
        // Get requested version information
        String requestedVersion = extractVersionFromRequest(request);
        
        // Validate version compatibility
        if (!isVersionCompatible(requestedVersion, methodVersion.value())) {
            response.setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(String.format(
                "{\"code\":\"UNSUPPORTED_API_VERSION\",\"message\":\"API version '%s' is not supported. Current version: '%s'\",\"data\":null,\"timestamp\":\"%s\"}",
                requestedVersion, methodVersion.value(), java.time.LocalDateTime.now()
            ));
            return false;
        }
        
        // Handle deprecation warnings
        if (methodVersion.deprecated() && ApiVersionConfig.VersionCompatibility.DEPRECATION_WARNING) {
            response.setHeader("X-API-Deprecated", "true");
            response.setHeader("X-API-Deprecation-Message", methodVersion.deprecationMessage());
            if (!methodVersion.until().isEmpty()) {
                response.setHeader("X-API-Deprecation-Until", methodVersion.until());
            }
            
            logger.warn("Using deprecated API version: {} - {}", methodVersion.value(), methodVersion.deprecationMessage());
        }
        
        // Set version information in response header
        response.setHeader("X-API-Version", methodVersion.value());
        
        return true;
    }
    
    /**
     * Extract version information from request
     */
    private String extractVersionFromRequest(HttpServletRequest request) {
        // 1. Extract version from URL path
        String path = request.getRequestURI();
        if (path.contains("/api/v")) {
            String[] parts = path.split("/");
            for (String part : parts) {
                if (part.startsWith("v") && part.length() > 1) {
                    return part;
                }
            }
        }
        
        // 2. Extract version from request header
        String headerVersion = request.getHeader(ApiVersionConfig.VersionStrategy.VERSION_HEADER);
        if (headerVersion != null && !headerVersion.isEmpty()) {
            return headerVersion.startsWith("v") ? headerVersion : "v" + headerVersion;
        }
        
        // 3. Extract version from query parameter
        String paramVersion = request.getParameter(ApiVersionConfig.VersionStrategy.VERSION_PARAM);
        if (paramVersion != null && !paramVersion.isEmpty()) {
            return paramVersion.startsWith("v") ? paramVersion : "v" + paramVersion;
        }
        
        // 4. Return default version
        return ApiVersionConfig.DEFAULT_VERSION;
    }
    
    /**
     * Check version compatibility
     */
    private boolean isVersionCompatible(String requestedVersion, String supportedVersion) {
        // Exact match
        if (requestedVersion.equals(supportedVersion)) {
            return true;
        }
        
        // If backward compatibility is enabled, check if requested version is supported
        if (ApiVersionConfig.VersionCompatibility.BACKWARD_COMPATIBLE) {
            return isVersionSupported(requestedVersion);
        }
        
        return false;
    }
    
    /**
     * Check if version is supported
     */
    private boolean isVersionSupported(String version) {
        // Ensure version has 'v' prefix for comparison
        String normalizedVersion = version.startsWith("v") ? version : "v" + version;
        for (String supportedVersion : ApiVersionConfig.SUPPORTED_VERSIONS) {
            if (supportedVersion.equals(normalizedVersion)) {
                return true;
            }
        }
        return false;
    }
} 