package com.trading.application.service;

import com.trading.domain.product.Product;
import com.trading.domain.Money;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 商品服务
 * 管理商品相关的业务操作
 */
@Service
@Transactional
public class ProductService {
    
    // 简单的内存存储，生产环境应该使用数据库
    private final Map<Long, Product> productStorage = new HashMap<>();
    private final Map<String, Product> skuIndex = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);
    
    /**
     * 创建商品
     */
    public Product createProduct(String sku, String name, String description, 
                               Money price, Long merchantId, int initialStock) {
        if (skuIndex.containsKey(sku)) {
            throw new RuntimeException("Product with SKU " + sku + " already exists");
        }
        
        Product product = new Product(sku, name, description, price, merchantId, initialStock);
        Long id = idGenerator.getAndIncrement();
        product.setId(id);
        productStorage.put(id, product);
        skuIndex.put(sku, product);
        return product;
    }
    
    /**
     * 根据SKU获取商品
     */
    @Transactional(readOnly = true)
    public Product getProductBySku(String sku) {
        Product product = skuIndex.get(sku);
        if (product == null) {
            throw new RuntimeException("Product not found with SKU: " + sku);
        }
        return product;
    }
    
    /**
     * 根据ID获取商品
     */
    @Transactional(readOnly = true)
    public Product getProductById(Long productId) {
        Product product = productStorage.get(productId);
        if (product == null) {
            throw new RuntimeException("Product not found with id: " + productId);
        }
        return product;
    }
    
    /**
     * 添加商品库存
     */
    public void addProductStock(String sku, int quantity) {
        Product product = getProductBySku(sku);
        product.addStock(quantity);
        saveProduct(product);
    }
    
    /**
     * 保存商品
     */
    public void saveProduct(Product product) {
        productStorage.put(product.getId(), product);
        skuIndex.put(product.getSku(), product);
    }
    
    /**
     * 检查商品是否存在
     */
    @Transactional(readOnly = true)
    public boolean productExists(String sku) {
        return skuIndex.containsKey(sku);
    }
    
    /**
     * 获取商品库存
     */
    @Transactional(readOnly = true)
    public int getProductStock(String sku) {
        return getProductBySku(sku).getAvailableStock();
    }
} 