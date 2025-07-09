package com.ecommerce.api.config;

import org.springframework.context.annotation.Configuration;

/**
 * API version control configuration
 * 
 * Define API version constants and version control strategies
 */
@Configuration
public class ApiVersionConfig {
    
    /**
     * API version constants
     */
    public static final String API_V1 = "/api/v1";
    public static final String API_V2 = "/api/v2";
    
    /**
     * Currently supported versions list
     */
    public static final String[] SUPPORTED_VERSIONS = {"1", "2"};
    
    /**
     * Default version (used when version is not specified in request header)
     */
    public static final String DEFAULT_VERSION = "1";
    
    /**
     * Version control strategy configuration
     */
    public static class VersionStrategy {
        // URL path version control (recommended)
        public static final boolean URL_PATH_VERSIONING = true;
        
        // Request header version control
        public static final boolean HEADER_VERSIONING = false;
        public static final String VERSION_HEADER = "API-Version";
        
        // Query parameter version control
        public static final boolean QUERY_PARAM_VERSIONING = false;
        public static final String VERSION_PARAM = "version";
    }
    
    /**
     * Version compatibility configuration
     */
    public static class VersionCompatibility {
        // Whether to enable backward compatibility
        public static final boolean BACKWARD_COMPATIBLE = true;
        
        // Deprecation warning
        public static final boolean DEPRECATION_WARNING = true;
        
        // Version lifecycle (months)
        public static final int VERSION_LIFECYCLE_MONTHS = 12;
    }
} 