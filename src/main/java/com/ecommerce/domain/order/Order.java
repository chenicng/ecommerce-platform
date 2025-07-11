package com.ecommerce.domain.order;

import com.ecommerce.domain.BaseEntity;
import com.ecommerce.domain.Money;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Order Aggregate Root
 * Handles the complete process of user purchasing products
 */
@Entity
@Table(name = "orders", indexes = {
    @Index(name = "idx_order_number", columnList = "order_number", unique = true),
    @Index(name = "idx_order_user", columnList = "user_id"),
    @Index(name = "idx_order_merchant", columnList = "merchant_id"),
    @Index(name = "idx_order_status", columnList = "status"),
    @Index(name = "idx_order_time", columnList = "order_time")
})
public class Order extends BaseEntity {
    
    @Column(name = "order_number", nullable = false, length = 50, unique = true)
    private String orderNumber;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "merchant_id", nullable = false)
    private Long merchantId;
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<OrderItem> items;
    
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "total_amount", precision = 19, scale = 2)),
        @AttributeOverride(name = "currency", column = @Column(name = "total_currency", length = 3))
    })
    private Money totalAmount;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OrderStatus status;
    
    @Column(name = "order_time", nullable = false)
    private LocalDateTime orderTime;
    
    @Column(name = "completed_time")
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
            throw new InvalidOrderStateException("Cannot modify order in status: " + this.status);
        }
        
        OrderItem item = new OrderItem(sku, productName, unitPrice, quantity);
        item.setOrder(this); // Set bidirectional relationship
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
            throw new InvalidOrderStateException("Cannot confirm order without items");
        }
        this.status = OrderStatus.CONFIRMED;
        this.markAsUpdated();
    }
    
    /**
     * Process payment
     */
    public void processPayment() {
        if (this.status != OrderStatus.CONFIRMED) {
            throw new InvalidOrderStateException("Order must be confirmed before payment. Current status: " + this.status);
        }
        this.status = OrderStatus.PAID;
        this.markAsUpdated();
    }
    
    /**
     * Complete order
     */
    public void complete() {
        if (this.status != OrderStatus.PAID) {
            throw new InvalidOrderStateException("Order must be paid before completion. Current status: " + this.status);
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
            throw new InvalidOrderStateException("Cannot cancel completed order");
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
            throw new InvalidOrderStateException("Order is not in pending status. Current status: " + this.status);
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