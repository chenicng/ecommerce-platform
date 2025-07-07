package com.ecommerce.api.controller;

import com.ecommerce.application.service.EcommerceService;
import com.ecommerce.application.dto.PurchaseRequest;
import com.ecommerce.application.dto.PurchaseResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Ecommerce Controller
 * Handles REST API related to product purchasing
 */
@RestController
@RequestMapping("/api/ecommerce")
public class EcommerceController {
    
    private static final Logger logger = LoggerFactory.getLogger(EcommerceController.class);
    
    private final EcommerceService ecommerceService;
    
    public EcommerceController(EcommerceService ecommerceService) {
        this.ecommerceService = ecommerceService;
    }
    
    /**
     * Purchase Product
     * POST /api/ecommerce/purchase
     */
    @PostMapping("/purchase")
    public ResponseEntity<PurchaseResponse> purchaseProduct(@RequestBody PurchaseRequest request) {
        try {
            logger.info("Processing purchase request: {}", request);
            
            // Validate request parameters
            validatePurchaseRequest(request);
            
            // Process purchase
            PurchaseResponse response = ecommerceService.processPurchase(request);
            
            logger.info("Purchase completed successfully: {}", response.getOrderNumber());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Purchase failed: {}", e.getMessage(), e);
            
            // Return error response
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