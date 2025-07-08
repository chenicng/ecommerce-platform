package com.ecommerce.domain.order;

import com.ecommerce.domain.BaseEntity;
import com.ecommerce.domain.Money;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Order Aggregate Root
 * Handles the complete process of user purchasing products
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
    
    // Constructor
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
        this.totalAmount = null; // Will be set when first item is added
        this.status = OrderStatus.PENDING;
        this.orderTime = LocalDateTime.now();
    }
    
    /**
     * Add order item
     */
    public void addOrderItem(String sku, String productName, Money unitPrice, int quantity) {
        if (this.status != OrderStatus.PENDING) {
            throw new IllegalStateException("Cannot modify order in status: " + this.status);
        }
        
        OrderItem item = new OrderItem(sku, productName, unitPrice, quantity);
        this.items.add(item);
        
        // Initialize totalAmount with first item's currency, or add to existing total
        if (this.totalAmount == null) {
            this.totalAmount = item.getTotalPrice();
        } else {
            this.totalAmount = this.totalAmount.add(item.getTotalPrice());
        }
        this.markAsUpdated();
    }
    
    /**
     * Confirm order
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
     * Process payment
     */
    public void processPayment() {
        if (this.status != OrderStatus.CONFIRMED) {
            throw new IllegalStateException("Order must be confirmed before payment");
        }
        this.status = OrderStatus.PAID;
        this.markAsUpdated();
    }
    
    /**
     * Complete order
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
     * Cancel order
     * Note: This method only changes the order status. 
     * Refund processing should be handled by the application service layer.
     */
    public void cancel(String reason) {
        if (this.status == OrderStatus.COMPLETED) {
            throw new IllegalStateException("Cannot cancel completed order");
        }
        this.status = OrderStatus.CANCELLED;
        this.markAsUpdated();
    }
    
    /**
     * Check if order needs refund when cancelled
     * Returns true if money has been deducted (status is PAID or higher)
     */
    public boolean needsRefund() {
        return this.status == OrderStatus.PAID || this.status == OrderStatus.COMPLETED;
    }
    
    /**
     * Check if inventory needs to be restored when cancelled
     * Returns true if inventory has been deducted (status is CONFIRMED or higher)
     */
    public boolean needsInventoryRestore() {
        return this.status == OrderStatus.CONFIRMED || 
               this.status == OrderStatus.PAID || 
               this.status == OrderStatus.COMPLETED;
    }
    
    /**
     * Check if order can be paid
     */
    public boolean canBePaid() {
        return OrderStatus.CONFIRMED.equals(this.status);
    }
    
    /**
     * Check if order is completed
     */
    public boolean isCompleted() {
        return OrderStatus.COMPLETED.equals(this.status);
    }
    
    /**
     * Check if order is cancelled
     */
    public boolean isCancelled() {
        return OrderStatus.CANCELLED.equals(this.status);
    }
    
    /**
     * Get total quantity of items in order
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
        return new ArrayList<>(items); // Return a copy to protect encapsulation
    }
    
    public Money getTotalAmount() {
        return totalAmount != null ? totalAmount : Money.zero("CNY"); // Default to CNY if no items added yet
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