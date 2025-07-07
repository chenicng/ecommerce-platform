package com.ecommerce.domain.product;

import java.util.Objects;

/**
 * 商品库存值对象
 * 管理商品的库存数量
 */
public final class ProductInventory {
    
    private final int availableStock;
    
    public ProductInventory(int availableStock) {
        if (availableStock < 0) {
            throw new IllegalArgumentException("Available stock cannot be negative");
        }
        this.availableStock = availableStock;
    }
    
    /**
     * 增加库存
     */
    public ProductInventory add(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        return new ProductInventory(this.availableStock + quantity);
    }
    
    /**
     * 减少库存
     */
    public ProductInventory reduce(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (quantity > this.availableStock) {
            throw new IllegalArgumentException("Cannot reduce more than available stock");
        }
        return new ProductInventory(this.availableStock - quantity);
    }
    
    /**
     * 检查是否有足够库存
     */
    public boolean hasEnoughStock(int quantity) {
        return this.availableStock >= quantity;
    }
    
    /**
     * 检查是否有库存
     */
    public boolean hasStock() {
        return this.availableStock > 0;
    }
    
    /**
     * 检查是否库存为空
     */
    public boolean isEmpty() {
        return this.availableStock == 0;
    }
    
    public int getAvailableStock() {
        return availableStock;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductInventory that = (ProductInventory) o;
        return availableStock == that.availableStock;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(availableStock);
    }
    
    @Override
    public String toString() {
        return "ProductInventory{availableStock=" + availableStock + '}';
    }
} 