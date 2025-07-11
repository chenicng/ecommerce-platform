package com.ecommerce.application.dto;

import com.ecommerce.domain.Money;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Purchase Response DTO
 */
@Schema(description = "Product purchase response")
public class PurchaseResponse {
    
    @Schema(description = "Order number", example = "ORD20250101001")
    private String orderNumber;
    
    @Schema(description = "User ID", example = "1")
    private Long userId;
    
    @Schema(description = "Merchant ID", example = "1")
    private Long merchantId;
    
    @Schema(description = "Product SKU", example = "PHONE-001")
    private String sku;
    
    @Schema(description = "Product name", example = "iPhone 15 Pro")
    private String productName;
    
    @Schema(description = "Purchase quantity", example = "2")
    private int quantity;
    
    @Schema(description = "Total amount", example = "2000.00")
    private Money totalAmount;
    
    // Constructor
    public PurchaseResponse() {}
    
    public PurchaseResponse(String orderNumber, Long userId, Long merchantId, String sku, 
                           String productName, int quantity, Money totalAmount) {
        this.orderNumber = orderNumber;
        this.userId = userId;
        this.merchantId = merchantId;
        this.sku = sku;
        this.productName = productName;
        this.quantity = quantity;
        this.totalAmount = totalAmount;
    }
    
    // Getters and Setters
    public String getOrderNumber() {
        return orderNumber;
    }
    
    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public Long getMerchantId() {
        return merchantId;
    }
    
    public void setMerchantId(Long merchantId) {
        this.merchantId = merchantId;
    }
    
    public String getSku() {
        return sku;
    }
    
    public void setSku(String sku) {
        this.sku = sku;
    }
    
    public String getProductName() {
        return productName;
    }
    
    public void setProductName(String productName) {
        this.productName = productName;
    }
    
    public int getQuantity() {
        return quantity;
    }
    
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    
    public Money getTotalAmount() {
        return totalAmount;
    }
    
    public void setTotalAmount(Money totalAmount) {
        this.totalAmount = totalAmount;
    }
    
    @Override
    public String toString() {
        return String.format("PurchaseResponse{orderNumber='%s', userId=%d, merchantId=%d, sku='%s', " +
                           "productName='%s', quantity=%d, totalAmount=%s}",
                           orderNumber, userId, merchantId, sku, productName, quantity, totalAmount);
    }
} 