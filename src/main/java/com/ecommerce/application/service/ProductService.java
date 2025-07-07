package com.ecommerce.application.service;

import com.ecommerce.domain.product.Product;
import com.ecommerce.domain.product.ProductStatus;
import com.ecommerce.domain.Money;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Product Service
 * Manages product-related business operations
 */
@Service
@Transactional
public class ProductService {
    
    // Simple in-memory storage, production should use database
    private final Map<Long, Product> productStorage = new HashMap<>();
    private final Map<String, Product> skuIndex = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);
    
    /**
     * Create product
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
     * Get product by SKU
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
     * Get product by ID
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
     * Add product stock
     */
    public void addProductStock(String sku, int quantity) {
        Product product = getProductBySku(sku);
        product.addStock(quantity);
        saveProduct(product);
    }
    
    /**
     * Save product
     */
    public void saveProduct(Product product) {
        productStorage.put(product.getId(), product);
        skuIndex.put(product.getSku(), product);
    }
    
    /**
     * Check if product exists
     */
    @Transactional(readOnly = true)
    public boolean productExists(String sku) {
        return skuIndex.containsKey(sku);
    }
    
    /**
     * Get product stock
     */
    @Transactional(readOnly = true)
    public int getProductStock(String sku) {
        return getProductBySku(sku).getAvailableStock();
    }
    
    /**
     * Get all products
     */
    @Transactional(readOnly = true)
    public List<Product> getAllProducts() {
        return productStorage.values().stream()
                .collect(Collectors.toList());
    }
    
    /**
     * Get products by merchant
     */
    @Transactional(readOnly = true)
    public List<Product> getProductsByMerchant(Long merchantId) {
        return productStorage.values().stream()
                .filter(product -> product.getMerchantId().equals(merchantId))
                .collect(Collectors.toList());
    }
    
    /**
     * Get active products
     */
    @Transactional(readOnly = true)
    public List<Product> getActiveProducts() {
        return productStorage.values().stream()
                .filter(Product::isActive)
                .collect(Collectors.toList());
    }
    
    /**
     * Get available products (active and in stock)
     */
    @Transactional(readOnly = true)
    public List<Product> getAvailableProducts() {
        return productStorage.values().stream()
                .filter(Product::isAvailable)
                .collect(Collectors.toList());
    }
    
    /**
     * Get products by merchant and status
     */
    @Transactional(readOnly = true)
    public List<Product> getProductsByMerchantAndStatus(Long merchantId, ProductStatus status) {
        return productStorage.values().stream()
                .filter(product -> product.getMerchantId().equals(merchantId))
                .filter(product -> product.getStatus().equals(status))
                .collect(Collectors.toList());
    }
    
    /**
     * Search products by name (case-insensitive)
     */
    @Transactional(readOnly = true)
    public List<Product> searchProductsByName(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllProducts();
        }
        
        String lowerSearchTerm = searchTerm.toLowerCase();
        return productStorage.values().stream()
                .filter(product -> product.getName().toLowerCase().contains(lowerSearchTerm) ||
                                 product.getDescription().toLowerCase().contains(lowerSearchTerm))
                .collect(Collectors.toList());
    }
} 