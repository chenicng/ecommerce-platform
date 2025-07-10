package com.ecommerce.api.controller;

import com.ecommerce.api.config.ApiVersionConfig;
import com.ecommerce.api.dto.Result;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * API Version Controller
 * Provides API version information, compatibility checking, and usage examples
 */
@RestController
@RequestMapping("/api/version")
@Tag(name = "API Version", description = "API version information and compatibility")
public class ApiVersionController {
    
    /**
     * Get API Version Information
     * GET /api/version/info
     */
    @GetMapping("/info")
    @Operation(summary = "Get API Version Information", 
               description = "Returns comprehensive API version information including supported versions, strategies, and compatibility settings")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Version information retrieved successfully")
    })
    public ResponseEntity<Result<Map<String, Object>>> getVersionInfo() {
        Map<String, Object> versionInfo = new HashMap<>();
        
        // Basic version information
        versionInfo.put("currentVersion", ApiVersionConfig.DEFAULT_VERSION);
        versionInfo.put("supportedVersions", Arrays.asList(ApiVersionConfig.SUPPORTED_VERSIONS));
        versionInfo.put("timestamp", LocalDateTime.now());
        
        // Version control strategy
        Map<String, Object> strategy = new HashMap<>();
        strategy.put("urlPathVersioning", ApiVersionConfig.VersionStrategy.URL_PATH_VERSIONING);
        strategy.put("headerVersioning", ApiVersionConfig.VersionStrategy.HEADER_VERSIONING);
        strategy.put("queryParamVersioning", ApiVersionConfig.VersionStrategy.QUERY_PARAM_VERSIONING);
        if (ApiVersionConfig.VersionStrategy.HEADER_VERSIONING) {
            strategy.put("versionHeader", ApiVersionConfig.VersionStrategy.VERSION_HEADER);
        }
        if (ApiVersionConfig.VersionStrategy.QUERY_PARAM_VERSIONING) {
            strategy.put("versionParam", ApiVersionConfig.VersionStrategy.VERSION_PARAM);
        }
        versionInfo.put("strategy", strategy);
        
        // Compatibility configuration
        Map<String, Object> compatibility = new HashMap<>();
        compatibility.put("backwardCompatible", ApiVersionConfig.VersionCompatibility.BACKWARD_COMPATIBLE);
        compatibility.put("deprecationWarning", ApiVersionConfig.VersionCompatibility.DEPRECATION_WARNING);
        compatibility.put("versionLifecycleMonths", ApiVersionConfig.VersionCompatibility.VERSION_LIFECYCLE_MONTHS);
        versionInfo.put("compatibility", compatibility);
        
        return ResponseEntity.ok(Result.success(versionInfo));
    }
    
    /**
     * Get Version Examples
     * GET /api/version/examples
     */
    @GetMapping("/examples")
    public ResponseEntity<Result<Map<String, Object>>> getVersionExamples() {
        Map<String, Object> examples = new HashMap<>();
        
        // URL path version control examples
        Map<String, Object> urlExamples = new HashMap<>();
        urlExamples.put("v1_user_create", "POST /api/v1/users");
        urlExamples.put("v2_user_create", "POST /api/v2/users");
        urlExamples.put("v1_product_list", "GET /api/v1/ecommerce/products");
        examples.put("urlPathVersioning", urlExamples);
        
        // Request header version control examples
        if (ApiVersionConfig.VersionStrategy.HEADER_VERSIONING) {
            Map<String, Object> headerExamples = new HashMap<>();
            headerExamples.put("header", ApiVersionConfig.VersionStrategy.VERSION_HEADER);
            headerExamples.put("v1_example", "API-Version: v1");
            headerExamples.put("v2_example", "API-Version: v2");
            examples.put("headerVersioning", headerExamples);
        }
        
        // Query parameter version control examples
        if (ApiVersionConfig.VersionStrategy.QUERY_PARAM_VERSIONING) {
            Map<String, Object> paramExamples = new HashMap<>();
            paramExamples.put("parameter", ApiVersionConfig.VersionStrategy.VERSION_PARAM);
            paramExamples.put("v1_example", "GET /api/users?version=v1");
            paramExamples.put("v2_example", "GET /api/users?version=v2");
            examples.put("queryParamVersioning", paramExamples);
        }
        
        return ResponseEntity.ok(Result.success(examples));
    }
    
    /**
     * Check Version Compatibility
     * GET /api/version/compatibility/{version}
     */
    @GetMapping("/compatibility/{version}")
    @Operation(summary = "Check Version Compatibility", 
               description = "Validates if a specific API version is supported and provides compatibility information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Version compatibility information retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid version format")
    })
    public ResponseEntity<Result<Map<String, Object>>> checkVersionCompatibility(
            @Parameter(description = "API version to check (e.g., 'v1', '1', 'v2')", required = true, example = "v1")
            @PathVariable String version) {
        
        // Validate version format
        if (version == null || version.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Result.error(
                com.ecommerce.api.dto.ErrorCode.VALIDATION_ERROR,
                "Version parameter cannot be null or empty"
            ));
        }
        
        // Normalize version number
        String normalizedVersion = version.startsWith("v") ? version : "v" + version;
        
        // Extract version number (remove 'v' prefix)
        String versionNumber = normalizedVersion.substring(1);
        
        // Validate version number format (should be a positive integer)
        if (!versionNumber.matches("^\\d+$")) {
            return ResponseEntity.badRequest().body(Result.error(
                com.ecommerce.api.dto.ErrorCode.VALIDATION_ERROR,
                "Invalid version format. Version must be a positive integer (e.g., '1', '2', 'v1', 'v2')"
            ));
        }
        
        // Check if supported
        boolean isSupported = Arrays.asList(ApiVersionConfig.SUPPORTED_VERSIONS)
            .contains(versionNumber);
        
        Map<String, Object> result = new HashMap<>();
        result.put("requestedVersion", normalizedVersion);
        result.put("isSupported", isSupported);
        result.put("currentVersion", ApiVersionConfig.DEFAULT_VERSION);
        result.put("supportedVersions", Arrays.asList(ApiVersionConfig.SUPPORTED_VERSIONS));
        
        if (!isSupported) {
            result.put("recommendation", "Please use one of the supported versions: " + 
                Arrays.toString(ApiVersionConfig.SUPPORTED_VERSIONS));
        }
        
        result.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.ok(Result.success(result));
    }
} 