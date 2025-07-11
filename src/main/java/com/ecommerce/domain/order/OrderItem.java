package com.ecommerce.domain.order;

import com.ecommerce.domain.BaseEntity;
import com.ecommerce.domain.Money;
import jakarta.persistence.*;
import java.util.Objects;

/**
 * Order Item Entity
 * Represents a single product item in the order
 */
@Entity
@Table(name = "order_items", indexes = {
    @Index(name = "idx_order_item_order", columnList = "order_id"),
    @Index(name = "idx_order_item_sku", columnList = "sku")
})
public class OrderItem extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
    
    @Column(name = "sku", nullable = false, length = 50)
    private String sku;
    
    @Column(name = "product_name", nullable = false, length = 200)
    private String productName;
    
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "unit_price_amount", precision = 19, scale = 2)),
        @AttributeOverride(name = "currency", column = @Column(name = "unit_price_currency", length = 3))
    })
    private Money unitPrice;
    
    @Column(name = "quantity", nullable = false)
    private int quantity;
    
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "total_price_amount", precision = 19, scale = 2)),
        @AttributeOverride(name = "currency", column = @Column(name = "total_price_currency", length = 3))
    })
    private Money totalPrice;
    
    // Default constructor for JPA
    protected OrderItem() {
        super();
    }
    
    public OrderItem(String sku, String productName, Money unitPrice, int quantity) {
        super();
        if (sku == null || sku.trim().isEmpty()) {
            throw new IllegalArgumentException("SKU cannot be null or empty");
        }
        if (productName == null || productName.trim().isEmpty()) {
            throw new IllegalArgumentException("Product name cannot be null or empty");
        }
        if (unitPrice == null || unitPrice.isZero()) {
            throw new IllegalArgumentException("Unit price must be positive");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        
        this.sku = sku;
        this.productName = productName;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.totalPrice = unitPrice.multiply(quantity);
    }
    
    public Order getOrder() {
        return order;
    }
    
    public void setOrder(Order order) {
        this.order = order;
    }
    
    public String getSku() {
        return sku;
    }
    
    public String getProductName() {
        return productName;
    }
    
    public Money getUnitPrice() {
        return unitPrice;
    }
    
    public int getQuantity() {
        return quantity;
    }
    
    public Money getTotalPrice() {
        return totalPrice;
    }
    
    // Package private setters for JPA
    void setSku(String sku) {
        this.sku = sku;
    }
    
    void setProductName(String productName) {
        this.productName = productName;
    }
    
    void setUnitPrice(Money unitPrice) {
        this.unitPrice = unitPrice;
    }
    
    void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    
    void setTotalPrice(Money totalPrice) {
        this.totalPrice = totalPrice;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderItem orderItem = (OrderItem) o;
        return quantity == orderItem.quantity &&
               Objects.equals(sku, orderItem.sku) &&
               Objects.equals(productName, orderItem.productName) &&
               Objects.equals(unitPrice, orderItem.unitPrice);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(sku, productName, unitPrice, quantity);
    }
    
    @Override
    public String toString() {
        return String.format("OrderItem{sku='%s', productName='%s', unitPrice=%s, quantity=%d, totalPrice=%s}",
                sku, productName, unitPrice, quantity, totalPrice);
    }
} 