package com.ecommerce.domain.product;

import com.ecommerce.domain.BaseEntity;
import com.ecommerce.domain.Money;

/**
 * 商品聚合根
 * 包含商品基本信息、价格和库存
 */
public class Product extends BaseEntity {
    
    private String sku;
    private String name;
    private String description;
    private Money price;
    private Long merchantId;
    private ProductInventory inventory;
    private ProductStatus status;
    
    // 构造函数
    protected Product() {
        super();
    }
    
    public Product(String sku, String name, String description, Money price, 
                  Long merchantId, int initialStock) {
        super();
        this.sku = sku;
        this.name = name;
        this.description = description;
        this.price = price;
        this.merchantId = merchantId;
        this.inventory = new ProductInventory(initialStock);
        this.status = ProductStatus.ACTIVE;
    }
    
    /**
     * 增加库存
     */
    public void addStock(int quantity) {
        validateActiveStatus();
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        this.inventory = this.inventory.add(quantity);
        this.markAsUpdated();
    }
    
    /**
     * 减少库存
     */
    public void reduceStock(int quantity) {
        validateActiveStatus();
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (!hasEnoughStock(quantity)) {
            throw new InsufficientStockException("Insufficient stock for product: " + sku);
        }
        this.inventory = this.inventory.reduce(quantity);
        this.markAsUpdated();
    }
    
    /**
     * 检查是否有足够库存
     */
    public boolean hasEnoughStock(int quantity) {
        return this.inventory.hasEnoughStock(quantity);
    }
    
    /**
     * 计算总价
     */
    public Money calculateTotalPrice(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        return this.price.multiply(quantity);
    }
    
    /**
     * 更新价格
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
     * 激活商品
     */
    public void activate() {
        this.status = ProductStatus.ACTIVE;
        this.markAsUpdated();
    }
    
    /**
     * 停用商品
     */
    public void deactivate() {
        this.status = ProductStatus.INACTIVE;
        this.markAsUpdated();
    }
    
    private void validateActiveStatus() {
        if (!isActive()) {
            throw new IllegalStateException("Product is not active: " + sku);
        }
    }
    
    public boolean isActive() {
        return ProductStatus.ACTIVE.equals(this.status);
    }
    
    public boolean isAvailable() {
        return isActive() && hasEnoughStock(1);
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
    
    public int getAvailableStock() {
        return inventory.getAvailableStock();
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