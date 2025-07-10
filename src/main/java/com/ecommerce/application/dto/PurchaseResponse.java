package com.ecommerce.application.dto;

import com.ecommerce.domain.Money;

/**
 * Purchase Response DTO
 */
public class PurchaseResponse {
    
    private String orderNumber;
    private Long userId;
    private Long merchantId;
    private String sku;
    private String productName;
    private int quantity;
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