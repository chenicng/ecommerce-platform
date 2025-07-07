package com.trading.api.controller;

import com.trading.application.service.TradingService;
import com.trading.application.dto.PurchaseRequest;
import com.trading.application.dto.PurchaseResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 交易控制器
 * 处理商品购买相关的REST API
 */
@RestController
@RequestMapping("/api/trading")
public class TradingController {
    
    private static final Logger logger = LoggerFactory.getLogger(TradingController.class);
    
    private final TradingService tradingService;
    
    public TradingController(TradingService tradingService) {
        this.tradingService = tradingService;
    }
    
    /**
     * 购买商品
     * POST /api/trading/purchase
     */
    @PostMapping("/purchase")
    public ResponseEntity<PurchaseResponse> purchaseProduct(@RequestBody PurchaseRequest request) {
        try {
            logger.info("Processing purchase request: {}", request);
            
            // 验证请求参数
            validatePurchaseRequest(request);
            
            // 处理购买
            PurchaseResponse response = tradingService.processPurchase(request);
            
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