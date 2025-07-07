package com.ecommerce.api.controller;

import com.ecommerce.application.service.CommerceService;
import com.ecommerce.application.dto.PurchaseRequest;
import com.ecommerce.application.dto.PurchaseResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 商务控制器
 * 处理商品购买相关的REST API
 */
@RestController
@RequestMapping("/api/commerce")
public class CommerceController {
    
    private static final Logger logger = LoggerFactory.getLogger(CommerceController.class);
    
    private final CommerceService commerceService;
    
    public CommerceController(CommerceService commerceService) {
        this.commerceService = commerceService;
    }
    
    /**
     * 购买商品
     * POST /api/commerce/purchase
     */
    @PostMapping("/purchase")
    public ResponseEntity<PurchaseResponse> purchaseProduct(@RequestBody PurchaseRequest request) {
        try {
            logger.info("Processing purchase request: {}", request);
            
            // 验证请求参数
            validatePurchaseRequest(request);
            
            // 处理购买
            PurchaseResponse response = commerceService.processPurchase(request);
            
            logger.info("Purchase completed successfully: {}", response.getOrderNumber());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Purchase failed: {}", e.getMessage(), e);
            
            // 返回错误响应
            PurchaseResponse errorResponse = new PurchaseResponse();
            errorResponse.setStatus("FAILED");
            errorResponse.setMessage(e.getMessage());
            
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    private void validatePurchaseRequest(PurchaseRequest request) {
        if (request.getUserId() == null) {
            throw new IllegalArgumentException("User ID is required");
        }
        if (request.getSku() == null || request.getSku().trim().isEmpty()) {
            throw new IllegalArgumentException("SKU is required");
        }
        if (request.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
    }
} 