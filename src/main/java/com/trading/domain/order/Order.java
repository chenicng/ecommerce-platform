package com.trading.domain.order;

import com.trading.domain.BaseEntity;
import com.trading.domain.Money;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 订单聚合根
 * 处理用户购买商品的完整流程
 */
public class Order extends BaseEntity {
    
    private String orderNumber;
    private Long userId;
    private Long merchantId;
    private List<OrderItem> items;
    private Money totalAmount;
    private OrderStatus status;
    private LocalDateTime orderTime;
    private LocalDateTime completedTime;
    
    // 构造函数
    protected Order() {
        super();
        this.items = new ArrayList<>();
    }
    
    public Order(String orderNumber, Long userId, Long merchantId) {
        super();
        this.orderNumber = orderNumber;
        this.userId = userId;
        this.merchantId = merchantId;
        this.items = new ArrayList<>();
        this.totalAmount = Money.zero("CNY"); // 默认货币
        this.status = OrderStatus.PENDING;
        this.orderTime = LocalDateTime.now();
    }
    
    /**
     * 添加订单项
     */
    public void addOrderItem(String sku, String productName, Money unitPrice, int quantity) {
        if (this.status != OrderStatus.PENDING) {
            throw new IllegalStateException("Cannot modify order in status: " + this.status);
        }
        
        OrderItem item = new OrderItem(sku, productName, unitPrice, quantity);
        this.items.add(item);
        this.totalAmount = this.totalAmount.add(item.getTotalPrice());
        this.markAsUpdated();
    }
    
    /**
     * 确认订单
     */
    public void confirm() {
        validatePendingStatus();
        if (this.items.isEmpty()) {
            throw new IllegalStateException("Cannot confirm order without items");
        }
        this.status = OrderStatus.CONFIRMED;
        this.markAsUpdated();
    }
    
    /**
     * 处理支付
     */
    public void processPayment() {
        if (this.status != OrderStatus.CONFIRMED) {
            throw new IllegalStateException("Order must be confirmed before payment");
        }
        this.status = OrderStatus.PAID;
        this.markAsUpdated();
    }
    
    /**
     * 完成订单
     */
    public void complete() {
        if (this.status != OrderStatus.PAID) {
            throw new IllegalStateException("Order must be paid before completion");
        }
        this.status = OrderStatus.COMPLETED;
        this.completedTime = LocalDateTime.now();
        this.markAsUpdated();
    }
    
    /**
     * 取消订单
     */
    public void cancel(String reason) {
        if (this.status == OrderStatus.COMPLETED) {
            throw new IllegalStateException("Cannot cancel completed order");
        }
        this.status = OrderStatus.CANCELLED;
        this.markAsUpdated();
    }
    
    /**
     * 检查订单是否可以支付
     */
    public boolean canBePaid() {
        return OrderStatus.CONFIRMED.equals(this.status);
    }
    
    /**
     * 检查订单是否已完成
     */
    public boolean isCompleted() {
        return OrderStatus.COMPLETED.equals(this.status);
    }
    
    /**
     * 检查订单是否已取消
     */
    public boolean isCancelled() {
        return OrderStatus.CANCELLED.equals(this.status);
    }
    
    /**
     * 获取订单中的商品总数量
     */
    public int getTotalQuantity() {
        return this.items.stream()
                .mapToInt(OrderItem::getQuantity)
                .sum();
    }
    
    private void validatePendingStatus() {
        if (this.status != OrderStatus.PENDING) {
            throw new IllegalStateException("Order is not in pending status");
        }
    }
    
    // Getters
    public String getOrderNumber() {
        return orderNumber;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public Long getMerchantId() {
        return merchantId;
    }
    
    public List<OrderItem> getItems() {
        return new ArrayList<>(items); // 返回副本保护封装
    }
    
    public Money getTotalAmount() {
        return totalAmount;
    }
    
    public OrderStatus getStatus() {
        return status;
    }
    
    public LocalDateTime getOrderTime() {
        return orderTime;
    }
    
    public LocalDateTime getCompletedTime() {
        return completedTime;
    }
    
    // Package private setters for JPA
    void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }
    
    void setUserId(Long userId) {
        this.userId = userId;
    }
    
    void setMerchantId(Long merchantId) {
        this.merchantId = merchantId;
    }
    
    void setItems(List<OrderItem> items) {
        this.items = items;
    }
    
    void setTotalAmount(Money totalAmount) {
        this.totalAmount = totalAmount;
    }
    
    void setStatus(OrderStatus status) {
        this.status = status;
    }
    
    void setOrderTime(LocalDateTime orderTime) {
        this.orderTime = orderTime;
    }
    
    void setCompletedTime(LocalDateTime completedTime) {
        this.completedTime = completedTime;
    }
} 