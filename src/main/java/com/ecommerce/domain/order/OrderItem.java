package com.ecommerce.domain.order;

import com.ecommerce.domain.Money;
import java.util.Objects;

/**
 * Order Item Value Object
 * Represents a single product item in the order
 */
public final class OrderItem {
    
    private final String sku;
    private final String productName;
    private final Money unitPrice;
    private final int quantity;
    private final Money totalPrice;
    
    public OrderItem(String sku, String productName, Money unitPrice, int quantity) {
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