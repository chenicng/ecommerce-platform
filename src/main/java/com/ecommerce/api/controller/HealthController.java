package com.ecommerce.api.controller;

import com.ecommerce.api.dto.Result;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Health Check Controller
 * Provides system health monitoring endpoints
 */
@RestController
@RequestMapping("/api")
public class HealthController {
    
    /**
     * Simple health check endpoint
     * GET /api/health
     */
    @GetMapping("/health")
    public ResponseEntity<Result<Map<String, Object>>> health() {
        Map<String, Object> healthInfo = new HashMap<>();
        healthInfo.put("status", "UP");
        healthInfo.put("timestamp", LocalDateTime.now());
        healthInfo.put("service", "ecommerce-platform");
        healthInfo.put("version", "1.0.0");
        
        return ResponseEntity.ok(Result.success("System is healthy", healthInfo));
    }
    
    /**
     * Alternative health check endpoint (common pattern)
     * GET /api/healthz
     */
    @GetMapping("/healthz")
    public ResponseEntity<Result<Map<String, Object>>> healthz() {
        Map<String, Object> healthInfo = new HashMap<>();
        healthInfo.put("status", "OK");
        healthInfo.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.ok(Result.success("Service is ready", healthInfo));
    }
} 