package com.ecommerce.domain.product;

import java.util.Objects;

/**
 * Product Inventory Value Object
 * Manages product stock quantity
 */
public final class ProductInventory {
    
    private final int quantity;
    
    public ProductInventory(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Inventory quantity cannot be negative");
        }
        this.quantity = quantity;
    }
    
    /**
     * Add inventory
     */
    public ProductInventory add(int additionalQuantity) {
        if (additionalQuantity <= 0) {
            throw new IllegalArgumentException("Additional quantity must be positive");
        }
        return new ProductInventory(this.quantity + additionalQuantity);
    }
    
    /**
     * Reduce inventory
     */
    public ProductInventory reduce(int reduceQuantity) {
        if (reduceQuantity <= 0) {
            throw new IllegalArgumentException("Reduce quantity must be positive");
        }
        if (reduceQuantity > this.quantity) {
            throw new InsufficientStockException("Cannot reduce more than available stock");
        }
        return new ProductInventory(this.quantity - reduceQuantity);
    }
    
    /**
     * Check if has enough stock
     */
    public boolean hasEnoughStock(int requiredQuantity) {
        return this.quantity >= requiredQuantity;
    }
    
    /**
     * Check if has stock
     */
    public boolean hasStock() {
        return this.quantity > 0;
    }
    
    /**
     * Check if inventory is empty
     */
    public boolean isEmpty() {
        return this.quantity == 0;
    }
    
    public int getQuantity() {
        return quantity;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductInventory that = (ProductInventory) o;
        return quantity == that.quantity;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(quantity);
    }
    
    @Override
    public String toString() {
        return "ProductInventory{quantity=" + quantity + '}';
    }
} 