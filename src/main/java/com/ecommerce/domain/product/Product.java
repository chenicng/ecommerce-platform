package com.ecommerce.domain.product;

import com.ecommerce.domain.BaseEntity;
import com.ecommerce.domain.Money;
import com.ecommerce.domain.ResourceInactiveException;
import jakarta.persistence.*;

/**
 * Product Aggregate Root
 * Contains product basic information, price and inventory
 */
@Entity
@Table(name = "products", indexes = {
    @Index(name = "idx_product_sku", columnList = "sku", unique = true),
    @Index(name = "idx_product_merchant", columnList = "merchant_id"),
    @Index(name = "idx_product_name", columnList = "name")
})
public class Product extends BaseEntity {
    
    @Column(name = "sku", nullable = false, length = 50, unique = true)
    private String sku;
    
    @Column(name = "name", nullable = false, length = 200)
    private String name;
    
    @Column(name = "description", length = 1000)
    private String description;
    
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "price_amount", precision = 19, scale = 2)),
        @AttributeOverride(name = "currency", column = @Column(name = "price_currency", length = 3))
    })
    private Money price;
    
    @Column(name = "merchant_id", nullable = false)
    private Long merchantId;
    
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "quantity", column = @Column(name = "inventory_quantity"))
    })
    private ProductInventory inventory;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ProductStatus status;
    
    // Constructor
    protected Product() {
        super();
    }
    
    public Product(String sku, String name, String description, Money price, 
                  Long merchantId, int initialInventory) {
        super();
        this.sku = sku;
        this.name = name;
        this.description = description;
        this.price = price;
        this.merchantId = merchantId;
        this.inventory = new ProductInventory(initialInventory);
        this.status = ProductStatus.ACTIVE;
    }
    
    /**
     * Add inventory
     */
    public void addInventory(int quantity) {
        validateActiveStatus();
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        this.inventory = this.inventory.add(quantity);
        this.markAsUpdated();
    }
    
    /**
     * Reduce inventory
     */
    public void reduceInventory(int quantity) {
        validateActiveStatus();
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (!hasEnoughInventory(quantity)) {
            throw new InsufficientInventoryException("Insufficient inventory for product: " + sku + ". Required: " + quantity + ", Available: " + getAvailableInventory());
        }
        this.inventory = this.inventory.reduce(quantity);
        this.markAsUpdated();
    }
    
    /**
     * Check if has enough inventory
     */
    public boolean hasEnoughInventory(int quantity) {
        return this.inventory.hasEnoughInventory(quantity);
    }
    
    /**
     * Calculate total price
     */
    public Money calculateTotalPrice(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        return this.price.multiply(quantity);
    }
    
    /**
     * Update price
     */
    public void updatePrice(Money newPrice) {
        validateActiveStatus();
        if (newPrice == null || newPrice.isZero()) {
            throw new IllegalArgumentException("Price must be positive");
        }
        this.price = newPrice;
        this.markAsUpdated();
    }
    
    /**
     * Activate product
     */
    public void activate() {
        this.status = ProductStatus.ACTIVE;
        this.markAsUpdated();
    }
    
    /**
     * Deactivate product
     */
    public void deactivate() {
        this.status = ProductStatus.INACTIVE;
        this.markAsUpdated();
    }
    
    private void validateActiveStatus() {
        if (!isActive()) {
            throw new ResourceInactiveException("Product is not active: " + sku);
        }
    }
    
    public boolean isActive() {
        return ProductStatus.ACTIVE.equals(this.status);
    }
    
    public boolean isAvailable() {
        return isActive() && hasEnoughInventory(1);
    }
    
    // Getters
    public String getSku() {
        return sku;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public Money getPrice() {
        return price;
    }
    
    public Long getMerchantId() {
        return merchantId;
    }
    
    public ProductInventory getInventory() {
        return inventory;
    }
    
    public int getAvailableInventory() {
        return inventory.getQuantity();
    }
    
    public ProductStatus getStatus() {
        return status;
    }
    
    // Package private setters for JPA
    void setSku(String sku) {
        this.sku = sku;
    }
    
    void setName(String name) {
        this.name = name;
    }
    
    void setDescription(String description) {
        this.description = description;
    }
    
    void setPrice(Money price) {
        this.price = price;
    }
    
    void setMerchantId(Long merchantId) {
        this.merchantId = merchantId;
    }
    
    void setInventory(ProductInventory inventory) {
        this.inventory = inventory;
    }
    
    void setStatus(ProductStatus status) {
        this.status = status;
    }
} 