package com.trading.application.dto;

import com.trading.domain.Money;

/**
 * 购买响应DTO
 */
public class PurchaseResponse {
    
    private String orderNumber;
    private Long userId;
    private Long merchantId;
    private String sku;
    private String productName;
    private int quantity;
    private Money totalAmount;
    private String status;
    private String message;
    
    // 构造函数
    public PurchaseResponse() {}
    
    public PurchaseResponse(String orderNumber, Long userId, Long merchantId, String sku, 
                           String productName, int quantity, Money totalAmount, 
                           String status, String message) {
        this.orderNumber = orderNumber;
        this.userId = userId;
        this.merchantId = merchantId;
        this.sku = sku;
        this.productName = productName;
        this.quantity = quantity;
        this.totalAmount = totalAmount;
        this.status = status;
        this.message = message;
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
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    @Override
    public String toString() {
        return String.format("PurchaseResponse{orderNumber='%s', userId=%d, merchantId=%d, sku='%s', " +
                           "productName='%s', quantity=%d, totalAmount=%s, status='%s', message='%s'}",
                           orderNumber, userId, merchantId, sku, productName, quantity, 
                           totalAmount, status, message);
    }
} 