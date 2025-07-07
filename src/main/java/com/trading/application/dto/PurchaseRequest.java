package com.trading.application.dto;

/**
 * 购买请求DTO
 */
public class PurchaseRequest {
    
    private Long userId;
    private String sku;
    private int quantity;
    
    // 构造函数
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