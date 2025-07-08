package com.ecommerce.infrastructure.repository;

import com.ecommerce.domain.product.Product;
import java.util.List;
import java.util.Optional;

/**
 * Product Repository Interface
 * Defines data access contract for Product aggregate
 */
public interface ProductRepository {
    
    /**
     * Save product
     */
    Product save(Product product);
    
    /**
     * Find product by ID
     */
    Optional<Product> findById(Long id);
    
    /**
     * Find product by SKU
     */
    Optional<Product> findBySku(String sku);
    
    /**
     * Find products by merchant ID
     */
    List<Product> findByMerchantId(Long merchantId);
    
    /**
     * Find all products
     */
    List<Product> findAll();
    
    /**
     * Check if product exists by SKU
     */
    boolean existsBySku(String sku);
    
    /**
     * Delete product by ID
     */
    void deleteById(Long id);
} 