package com.ecommerce.infrastructure.repository.mock;

import com.ecommerce.domain.product.Product;
import com.ecommerce.domain.Money;
import com.ecommerce.infrastructure.repository.ProductRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Mock Product Repository Implementation
 * Uses in-memory storage with pre-loaded demo data
 */
@Repository
@Profile("mock")
public class MockProductRepository implements ProductRepository {
    
    private final Map<Long, Product> storage = new HashMap<>();
    private final Map<String, Product> skuIndex = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);
    
    public MockProductRepository() {
        initializeDemoData();
    }
    
    private void initializeDemoData() {
        // Create demo products for merchant 1
        createProduct("PHONE-001", "iPhone 16 Pro", "Latest iPhone with Pro camera system", 
                     Money.of("7999.00", "CNY"), 1L, 50);
        createProduct("PHONE-002", "Samsung Galaxy S24", "Latest Samsung flagship phone", 
                     Money.of("6999.00", "CNY"), 1L, 30);
        createProduct("LAPTOP-001", "MacBook Pro 16", "Apple MacBook Pro with M3 chip", 
                     Money.of("19999.00", "CNY"), 1L, 20);
        
        // Create demo products for merchant 2  
        createProduct("BOOK-001", "Clean Code", "A handbook of agile software craftsmanship", 
                     Money.of("89.00", "CNY"), 2L, 100);
        createProduct("BOOK-002", "Design Patterns", "Elements of reusable object-oriented software", 
                     Money.of("79.00", "CNY"), 2L, 80);
        createProduct("HEADPHONE-001", "AirPods Pro", "Active noise cancellation wireless earbuds", 
                     Money.of("1999.00", "CNY"), 2L, 60);
    }
    
    private void createProduct(String sku, String name, String description, 
                              Money price, Long merchantId, int inventory) {
        Product product = new Product(sku, name, description, price, merchantId, inventory);
        product.setId(idGenerator.getAndIncrement());
        storage.put(product.getId(), product);
        skuIndex.put(sku, product);
    }
    
    @Override
    public Product save(Product product) {
        if (product.getId() == null) {
            product.setId(idGenerator.getAndIncrement());
        }
        storage.put(product.getId(), product);
        skuIndex.put(product.getSku(), product);
        return product;
    }
    
    @Override
    public Optional<Product> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }
    
    @Override
    public Optional<Product> findBySku(String sku) {
        return Optional.ofNullable(skuIndex.get(sku));
    }
    
    @Override
    public List<Product> findByMerchantId(Long merchantId) {
        return storage.values().stream()
                .filter(product -> Objects.equals(product.getMerchantId(), merchantId))
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Product> findAll() {
        return new ArrayList<>(storage.values());
    }
    
    @Override
    public boolean existsBySku(String sku) {
        return skuIndex.containsKey(sku);
    }
    
    @Override
    public void deleteById(Long id) {
        Product product = storage.remove(id);
        if (product != null) {
            skuIndex.remove(product.getSku());
        }
    }
} 