package com.ecommerce.infrastructure.repository.mock;

import com.ecommerce.domain.product.Product;
import com.ecommerce.domain.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class MockProductRepositoryTest {

    private MockProductRepository repository;

    @BeforeEach
    void setUp() {
        repository = new MockProductRepository();
    }

    @Test
    void constructor_ShouldInitializeDemoData() {
        // Verify that demo data is initialized
        List<Product> allProducts = repository.findAll();
        assertEquals(6, allProducts.size());
        
        // Verify specific products exist
        assertTrue(repository.existsBySku("PHONE-001"));
        assertTrue(repository.existsBySku("BOOK-001"));
        assertTrue(repository.existsBySku("LAPTOP-001"));
    }

    @Test
    void save_WithNewProduct_ShouldAssignIdAndSave() {
        // Given
        Product newProduct = new Product("TEST-001", "Test Product", "Test Description", 
                                       Money.of("100.00", "CNY"), 1L, 10);
        
        // When
        Product savedProduct = repository.save(newProduct);
        
        // Then
        assertNotNull(savedProduct.getId());
        assertEquals(7L, savedProduct.getId());
        assertEquals("TEST-001", savedProduct.getSku());
        assertEquals(7, repository.findAll().size());
    }

    @Test
    void save_WithExistingProduct_ShouldUpdateProduct() {
        // Given
        Product existingProduct = repository.findById(1L).orElse(null);
        assertNotNull(existingProduct);
        
        // When
        Product savedProduct = repository.save(existingProduct);
        
        // Then
        assertEquals(1L, savedProduct.getId());
        assertEquals(6, repository.findAll().size()); // Count should remain the same
    }

    @Test
    void findById_WithExistingId_ShouldReturnProduct() {
        // When
        Optional<Product> product = repository.findById(1L);
        
        // Then
        assertTrue(product.isPresent());
        assertEquals("PHONE-001", product.get().getSku());
        assertEquals("iPhone 16 Pro", product.get().getName());
    }

    @Test
    void findById_WithNonExistingId_ShouldReturnEmpty() {
        // When
        Optional<Product> product = repository.findById(999L);
        
        // Then
        assertFalse(product.isPresent());
    }

    @Test
    void findBySku_WithExistingSku_ShouldReturnProduct() {
        // When
        Optional<Product> product = repository.findBySku("PHONE-001");
        
        // Then
        assertTrue(product.isPresent());
        assertEquals("iPhone 16 Pro", product.get().getName());
        assertEquals(1L, product.get().getMerchantId());
    }

    @Test
    void findBySku_WithNonExistingSku_ShouldReturnEmpty() {
        // When
        Optional<Product> product = repository.findBySku("NONEXISTENT");
        
        // Then
        assertFalse(product.isPresent());
    }

    @Test
    void findByMerchantId_WithExistingMerchant_ShouldReturnProducts() {
        // When
        List<Product> products = repository.findByMerchantId(1L);
        
        // Then
        assertEquals(3, products.size());
        assertTrue(products.stream().allMatch(p -> p.getMerchantId().equals(1L)));
    }

    @Test
    void findByMerchantId_WithNonExistingMerchant_ShouldReturnEmptyList() {
        // When
        List<Product> products = repository.findByMerchantId(999L);
        
        // Then
        assertTrue(products.isEmpty());
    }

    @Test
    void findAll_ShouldReturnAllProducts() {
        // When
        List<Product> products = repository.findAll();
        
        // Then
        assertEquals(6, products.size());
    }

    @Test
    void existsBySku_WithExistingSku_ShouldReturnTrue() {
        // When & Then
        assertTrue(repository.existsBySku("PHONE-001"));
        assertTrue(repository.existsBySku("BOOK-001"));
        assertTrue(repository.existsBySku("LAPTOP-001"));
    }

    @Test
    void existsBySku_WithNonExistingSku_ShouldReturnFalse() {
        // When & Then
        assertFalse(repository.existsBySku("NONEXISTENT"));
    }

    @Test
    void deleteById_WithExistingId_ShouldRemoveProduct() {
        // Given
        assertTrue(repository.findById(1L).isPresent());
        String sku = repository.findById(1L).get().getSku();
        
        // When
        repository.deleteById(1L);
        
        // Then
        assertFalse(repository.findById(1L).isPresent());
        assertFalse(repository.existsBySku(sku));
        assertEquals(5, repository.findAll().size());
    }

    @Test
    void deleteById_WithNonExistingId_ShouldNotThrowException() {
        // When & Then
        assertDoesNotThrow(() -> repository.deleteById(999L));
        assertEquals(6, repository.findAll().size());
    }

    @Test
    void demoData_ShouldHaveCorrectPrices() {
        // Check that demo products have the expected prices
        Product phone = repository.findBySku("PHONE-001").orElse(null);
        Product book = repository.findBySku("BOOK-001").orElse(null);
        
        assertNotNull(phone);
        assertNotNull(book);
        
        assertEquals(Money.of("7999.00", "CNY"), phone.getPrice());
        assertEquals(Money.of("89.00", "CNY"), book.getPrice());
    }

    @Test
    void class_ShouldHaveCorrectAnnotations() {
        // Test that the class has the expected annotations
        assertTrue(MockProductRepository.class.isAnnotationPresent(org.springframework.stereotype.Repository.class));
        assertTrue(MockProductRepository.class.isAnnotationPresent(org.springframework.context.annotation.Profile.class));
    }
} 