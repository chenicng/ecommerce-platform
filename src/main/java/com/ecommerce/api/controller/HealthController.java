package com.ecommerce.api.controller;

import com.ecommerce.api.dto.Result;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Health Check Controller
 * Provides system health monitoring and status endpoints
 */
@RestController
@RequestMapping("/api")
@Tag(name = "Health Check", description = "System health monitoring endpoints")
public class HealthController {
    
    /**
     * Health Check
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
        
        // Add proper headers including Date
        HttpHeaders headers = new HttpHeaders();
        headers.set("Date", LocalDateTime.now().format(DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss")));
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(Result.successWithMessage("System is healthy", healthInfo));
    }
} 