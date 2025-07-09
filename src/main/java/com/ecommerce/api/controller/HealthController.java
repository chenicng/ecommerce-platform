package com.ecommerce.api.controller;

import com.ecommerce.api.dto.Result;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Health Check Controller
 * Provides system health monitoring endpoints
 */
@RestController
@RequestMapping("/api")
@Tag(name = "Health Check", description = "System health monitoring endpoints")
public class HealthController {
    
    /**
     * Simple health check endpoint
     * GET /api/health
     */
    @GetMapping("/health")
    @Operation(summary = "Health Check", description = "Returns system health status and basic information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "System is healthy")
    })
    public ResponseEntity<Result<Map<String, Object>>> health() {
        Map<String, Object> healthInfo = new HashMap<>();
        healthInfo.put("status", "UP");
        healthInfo.put("timestamp", LocalDateTime.now());
        healthInfo.put("service", "ecommerce-platform");
        healthInfo.put("version", "1.0.0");
        
        return ResponseEntity.ok(Result.successWithMessage("System is healthy", healthInfo));
    }
    
    /**
     * Alternative health check endpoint (common pattern)
     * GET /api/healthz
     */
    @GetMapping("/healthz")
    @Operation(summary = "Health Check (Alternative)", description = "Alternative health check endpoint following common patterns")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Service is ready")
    })
    public ResponseEntity<Result<Map<String, Object>>> healthz() {
        Map<String, Object> healthInfo = new HashMap<>();
        healthInfo.put("status", "OK");
        healthInfo.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.ok(Result.successWithMessage("Service is ready", healthInfo));
    }
} 