package com.calculator.api.controller;

import com.calculator.api.model.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查控制器
 * 提供应用程序健康状态检查端点
 */
@RestController
@RequestMapping("/api/v1")
public class HealthController {

    private static final Logger logger = LoggerFactory.getLogger(HealthController.class);

    /**
     * 简单健康检查端点
     * 
     * @return 健康状态响应
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> health() {
        logger.info("Health check request received");
        
        Map<String, Object> healthData = new HashMap<>();
        healthData.put("status", "UP");
        healthData.put("service", "Calculator API");
        healthData.put("version", "1.0.0");
        healthData.put("timestamp", LocalDateTime.now());
        
        ApiResponse<Map<String, Object>> response = ApiResponse.success("Service is healthy", healthData);
        logger.info("Health check completed successfully: status={}", healthData.get("status"));
        
        return ResponseEntity.ok(response);
    }

    /**
     * 服务信息端点
     * 
     * @return 服务信息响应
     */
    @GetMapping("/system/info")
    public ResponseEntity<ApiResponse<Map<String, Object>>> info() {
        logger.info("Service info request received");
        
        Map<String, Object> infoData = new HashMap<>();
        infoData.put("name", "Calculator API");
        infoData.put("description", "Calculator API Service with Spring Boot");
        infoData.put("version", "1.0.0");
        infoData.put("java.version", System.getProperty("java.version"));
        infoData.put("java.vendor", System.getProperty("java.vendor"));
        infoData.put("os.name", System.getProperty("os.name"));
        infoData.put("os.version", System.getProperty("os.version"));
        
        ApiResponse<Map<String, Object>> response = ApiResponse.success("Service information", infoData);
        logger.info("Service info request completed successfully");
        
        return ResponseEntity.ok(response);
    }
} 