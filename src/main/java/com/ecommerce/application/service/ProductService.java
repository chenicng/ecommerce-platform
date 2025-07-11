package com.ecommerce.application.service;

import com.ecommerce.api.dto.ErrorCode;
import com.ecommerce.api.exception.BusinessException;
import com.ecommerce.domain.product.Product;
import com.ecommerce.domain.product.ProductStatus;
import com.ecommerce.domain.Money;
import com.ecommerce.infrastructure.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Product Service
 * Manages product-related business operations
 */
@Service
public class ProductService {
    
    private final ProductRepository productRepository;
    
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }
    
    /**
     * Create product
     * Requires transaction due to validation + save operations
     */
    @Transactional
    public Product createProduct(String sku, String name, String description, 
                               Money price, Long merchantId, int initialInventory) {
        if (productRepository.existsBySku(sku)) {
            throw new BusinessException(ErrorCode.RESOURCE_ALREADY_EXISTS, 
                "Product with SKU " + sku + " already exists");
        }
        
        Product product = new Product(sku, name, description, price, merchantId, initialInventory);
        return productRepository.save(product);
    }
    
    /**
     * Get product by SKU
     */
    @Transactional(readOnly = true)
    public Product getProductBySku(String sku) {
        return productRepository.findBySku(sku)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, 
                    "Product not found with SKU: " + sku));
    }
    
    /**
     * Get product by ID
     */
    @Transactional(readOnly = true)
    public Product getProductById(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, 
                    "Product not found with id: " + productId));
    }
    
    /**
     * Save product
     */
    public void saveProduct(Product product) {
        productRepository.save(product);
    }
    
    /**
     * Add product inventory
     * Requires transaction due to read + modify + save operations
     */
    @Transactional
    public void addInventory(String sku, int quantity) {
        Product product = getProductBySku(sku);
        product.addInventory(quantity);
        productRepository.save(product);
    }
    
    /**
     * Add product inventory (alias for backward compatibility)
     */
    public void addProductInventory(String sku, int quantity) {
        addInventory(sku, quantity);
    }
    
    /**
     * Reduce product inventory
     * Requires transaction due to read + modify + save operations
     */
    @Transactional
    public void reduceInventory(String sku, int quantity) {
        Product product = getProductBySku(sku);
        product.reduceInventory(quantity);
        productRepository.save(product);
    }
    
    /**
     * Set product inventory to absolute value
     * Requires transaction due to read + modify + save operations
     */
    @Transactional
    public void setInventory(String sku, int quantity) {
        Product product = getProductBySku(sku);
        // Calculate the difference and adjust accordingly
        int currentInventory = product.getAvailableInventory();
        if (quantity > currentInventory) {
            // Need to add inventory
            product.addInventory(quantity - currentInventory);
        } else if (quantity < currentInventory) {
            // Need to reduce inventory
            product.reduceInventory(currentInventory - quantity);
        }
        // If quantity == currentInventory, no change needed
        productRepository.save(product);
    }
    
    /**
     * Get products by merchant
     */
    @Transactional(readOnly = true)
    public List<Product> getProductsByMerchant(Long merchantId) {
        return productRepository.findByMerchantId(merchantId);
    }
    
    /**
     * Get all products
     */
    @Transactional(readOnly = true)
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }
    
    /**
     * Get available products (active and have inventory)
     */
    @Transactional(readOnly = true)
    public List<Product> getAvailableProducts() {
        return productRepository.findAll().stream()
                .filter(Product::isAvailable)
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
        return productRepository.findAll().stream()
                .filter(product -> product.getName().toLowerCase().contains(lowerSearchTerm) ||
                                 product.getDescription().toLowerCase().contains(lowerSearchTerm))
                .collect(Collectors.toList());
    }
    
    /**
     * Check if product exists by SKU
     */
    @Transactional(readOnly = true)
    public boolean productExists(String sku) {
        return productRepository.existsBySku(sku);
    }
    
    /**
     * Get all available products (alias for getAvailableProducts)
     */
    @Transactional(readOnly = true)
    public List<Product> getAllAvailableProducts() {
        return getAvailableProducts();
    }
    
    /**
     * Search available products by name (case-insensitive)
     * Only returns products that are available (active and have inventory)
     */
    @Transactional(readOnly = true)
    public List<Product> searchAvailableProducts(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAvailableProducts();
        }
        
        String lowerSearchTerm = searchTerm.toLowerCase();
        return productRepository.findAll().stream()
                .filter(Product::isAvailable)
                .filter(product -> product.getName().toLowerCase().contains(lowerSearchTerm) ||
                                 product.getDescription().toLowerCase().contains(lowerSearchTerm))
                .collect(Collectors.toList());
    }
} 