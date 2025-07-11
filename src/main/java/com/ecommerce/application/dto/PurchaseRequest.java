package com.ecommerce.application.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Purchase Request DTO
 */
@Schema(description = "Product purchase request")
public class PurchaseRequest {
    
    @Schema(description = "User ID", example = "1", required = true)
    @NotNull(message = "User ID is required")
    private Long userId;
    
    @Schema(description = "Product SKU", example = "PHONE-001", required = true)
    @NotBlank(message = "SKU is required")
    private String sku;
    
    @Schema(description = "Purchase quantity", example = "2", required = true)
    @Min(value = 1, message = "Quantity must be positive")
    private int quantity;
    
    // Constructor
    public PurchaseRequest() {}
    
    public PurchaseRequest(Long userId, String sku, int quantity) {
        this.userId = userId;
        this.sku = sku;
        this.quantity = quantity;
    }
    
    // Getters and Setters
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getSku() {
        return sku;
    }
    
    public void setSku(String sku) {
        this.sku = sku;
    }
    
    public int getQuantity() {
        return quantity;
    }
    
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    
    @Override
    public String toString() {
        return String.format("PurchaseRequest{userId=%d, sku='%s', quantity=%d}", 
                           userId, sku, quantity);
    }
} 