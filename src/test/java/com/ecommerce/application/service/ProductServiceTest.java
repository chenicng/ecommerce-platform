package com.ecommerce.application.service;

import com.ecommerce.domain.product.Product;
import com.ecommerce.domain.product.ProductStatus;
import com.ecommerce.domain.Money;
import com.ecommerce.infrastructure.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product testProduct;
    private Product testProduct2;
    private Money testPrice;

    @BeforeEach
    void setUp() {
        testPrice = Money.of(100.00, "CNY");
        testProduct = new Product("IPHONE15", "iPhone 15", "Latest iPhone", testPrice, 1L, 50);
        testProduct2 = new Product("LAPTOP", "MacBook Pro", "Professional laptop", Money.of(8000.00, "CNY"), 1L, 10);
    }

    @Test
    void createProduct_Success() {
        // Given
        when(productRepository.existsBySku("IPHONE15")).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // When
        Product result = productService.createProduct("IPHONE15", "iPhone 15", "Latest iPhone", testPrice, 1L, 50);

        // Then
        assertNotNull(result);
        assertEquals("IPHONE15", result.getSku());
        assertEquals("iPhone 15", result.getName());
        verify(productRepository).existsBySku("IPHONE15");
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void createProduct_SkuAlreadyExists() {
        // Given
        when(productRepository.existsBySku("IPHONE15")).thenReturn(true);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> productService.createProduct("IPHONE15", "iPhone 15", "Latest iPhone", testPrice, 1L, 50));
        assertEquals("Product with SKU IPHONE15 already exists", exception.getMessage());
        verify(productRepository).existsBySku("IPHONE15");
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void getProductBySku_Success() {
        // Given
        when(productRepository.findBySku("IPHONE15")).thenReturn(Optional.of(testProduct));

        // When
        Product result = productService.getProductBySku("IPHONE15");

        // Then
        assertNotNull(result);
        assertEquals(testProduct, result);
        verify(productRepository).findBySku("IPHONE15");
    }

    @Test
    void getProductBySku_ProductNotFound() {
        // Given
        when(productRepository.findBySku("UNKNOWN")).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> productService.getProductBySku("UNKNOWN"));
        assertEquals("Product not found with SKU: UNKNOWN", exception.getMessage());
        verify(productRepository).findBySku("UNKNOWN");
    }

    @Test
    void getProductById_Success() {
        // Given
        Long productId = 1L;
        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));

        // When
        Product result = productService.getProductById(productId);

        // Then
        assertNotNull(result);
        assertEquals(testProduct, result);
        verify(productRepository).findById(productId);
    }

    @Test
    void getProductById_ProductNotFound() {
        // Given
        Long productId = 999L;
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> productService.getProductById(productId));
        assertEquals("Product not found with id: 999", exception.getMessage());
        verify(productRepository).findById(productId);
    }

    @Test
    void saveProduct_Success() {
        // Given
        when(productRepository.save(testProduct)).thenReturn(testProduct);

        // When
        productService.saveProduct(testProduct);

        // Then
        verify(productRepository).save(testProduct);
    }

    @Test
    void addInventory_Success() {
        // Given
        when(productRepository.findBySku("IPHONE15")).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // When
        productService.addInventory("IPHONE15", 20);

        // Then
        verify(productRepository).findBySku("IPHONE15");
        verify(productRepository).save(testProduct);
    }

    @Test
    void addInventory_ProductNotFound() {
        // Given
        when(productRepository.findBySku("UNKNOWN")).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> productService.addInventory("UNKNOWN", 20));
        assertEquals("Product not found with SKU: UNKNOWN", exception.getMessage());
        verify(productRepository).findBySku("UNKNOWN");
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void addProductInventory_Success() {
        // Given
        when(productRepository.findBySku("IPHONE15")).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // When
        productService.addProductInventory("IPHONE15", 20);

        // Then
        verify(productRepository).findBySku("IPHONE15");
        verify(productRepository).save(testProduct);
    }

    @Test
    void reduceInventory_Success() {
        // Given
        when(productRepository.findBySku("IPHONE15")).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // When
        productService.reduceInventory("IPHONE15", 10);

        // Then
        verify(productRepository).findBySku("IPHONE15");
        verify(productRepository).save(testProduct);
    }

    @Test
    void reduceInventory_ProductNotFound() {
        // Given
        when(productRepository.findBySku("UNKNOWN")).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> productService.reduceInventory("UNKNOWN", 10));
        assertEquals("Product not found with SKU: UNKNOWN", exception.getMessage());
        verify(productRepository).findBySku("UNKNOWN");
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void setInventory_Success() {
        // Given
        when(productRepository.findBySku("IPHONE15")).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // When
        productService.setInventory("IPHONE15", 150);

        // Then
        verify(productRepository).findBySku("IPHONE15");
        verify(productRepository).save(testProduct);
    }

    @Test
    void setInventory_ProductNotFound() {
        // Given
        when(productRepository.findBySku("UNKNOWN")).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> productService.setInventory("UNKNOWN", 100));
        assertEquals("Product not found with SKU: UNKNOWN", exception.getMessage());
        verify(productRepository).findBySku("UNKNOWN");
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void getProductsByMerchant_Success() {
        // Given
        Long merchantId = 1L;
        List<Product> products = Arrays.asList(testProduct, testProduct2);
        when(productRepository.findByMerchantId(merchantId)).thenReturn(products);

        // When
        List<Product> result = productService.getProductsByMerchant(merchantId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(productRepository).findByMerchantId(merchantId);
    }

    @Test
    void getAllProducts_Success() {
        // Given
        List<Product> products = Arrays.asList(testProduct, testProduct2);
        when(productRepository.findAll()).thenReturn(products);

        // When
        List<Product> result = productService.getAllProducts();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(productRepository).findAll();
    }

    @Test
    void getAvailableProducts_Success() {
        // Given
        List<Product> products = Arrays.asList(testProduct, testProduct2);
        when(productRepository.findAll()).thenReturn(products);

        // When
        List<Product> result = productService.getAvailableProducts();

        // Then
        assertNotNull(result);
        verify(productRepository).findAll();
    }

    @Test
    void searchProductsByName_WithSearchTerm() {
        // Given
        List<Product> products = Arrays.asList(testProduct, testProduct2);
        when(productRepository.findAll()).thenReturn(products);

        // When
        List<Product> result = productService.searchProductsByName("iPhone");

        // Then
        assertNotNull(result);
        verify(productRepository).findAll();
    }

    @Test
    void searchProductsByName_EmptySearchTerm() {
        // Given
        List<Product> products = Arrays.asList(testProduct, testProduct2);
        when(productRepository.findAll()).thenReturn(products);

        // When
        List<Product> result = productService.searchProductsByName("");

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(productRepository).findAll();
    }

    @Test
    void searchProductsByName_NullSearchTerm() {
        // Given
        List<Product> products = Arrays.asList(testProduct, testProduct2);
        when(productRepository.findAll()).thenReturn(products);

        // When
        List<Product> result = productService.searchProductsByName(null);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(productRepository).findAll();
    }

    @Test
    void productExists_True() {
        // Given
        when(productRepository.existsBySku("IPHONE15")).thenReturn(true);

        // When
        boolean result = productService.productExists("IPHONE15");

        // Then
        assertTrue(result);
        verify(productRepository).existsBySku("IPHONE15");
    }

    @Test
    void productExists_False() {
        // Given
        when(productRepository.existsBySku("UNKNOWN")).thenReturn(false);

        // When
        boolean result = productService.productExists("UNKNOWN");

        // Then
        assertFalse(result);
        verify(productRepository).existsBySku("UNKNOWN");
    }

    @Test
    void getAllAvailableProducts_Success() {
        // Given
        List<Product> products = Arrays.asList(testProduct, testProduct2);
        when(productRepository.findAll()).thenReturn(products);

        // When
        List<Product> result = productService.getAllAvailableProducts();

        // Then
        assertNotNull(result);
        verify(productRepository).findAll();
    }

    @Test
    void searchAvailableProducts_WithSearchTerm() {
        // Given
        List<Product> products = Arrays.asList(testProduct, testProduct2);
        when(productRepository.findAll()).thenReturn(products);

        // When
        List<Product> result = productService.searchAvailableProducts("iPhone");

        // Then
        assertNotNull(result);
        verify(productRepository).findAll();
    }

    @Test
    void searchAvailableProducts_EmptySearchTerm() {
        // Given
        List<Product> products = Arrays.asList(testProduct, testProduct2);
        when(productRepository.findAll()).thenReturn(products);

        // When
        List<Product> result = productService.searchAvailableProducts("");

        // Then
        assertNotNull(result);
        verify(productRepository).findAll();
    }

    @Test
    void setInventory_ReduceInventory() {
        // Given - current inventory is 50, set to 30 (reduce by 20)
        when(productRepository.findBySku("IPHONE15")).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // When
        productService.setInventory("IPHONE15", 30);

        // Then
        verify(productRepository).findBySku("IPHONE15");
        verify(productRepository).save(testProduct);
    }

    @Test
    void setInventory_IncreaseInventory() {
        // Given - current inventory is 50, set to 100 (increase by 50)
        when(productRepository.findBySku("IPHONE15")).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // When
        productService.setInventory("IPHONE15", 100);

        // Then
        verify(productRepository).findBySku("IPHONE15");
        verify(productRepository).save(testProduct);
    }

    @Test
    void setInventory_SameInventory() {
        // Given - current inventory is 50, set to 50 (no change)
        when(productRepository.findBySku("IPHONE15")).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // When
        productService.setInventory("IPHONE15", 50);

        // Then
        verify(productRepository).findBySku("IPHONE15");
        verify(productRepository).save(testProduct);
    }

    @Test
    void searchProductsByName_WithDescriptionMatch() {
        // Given - create products with different descriptions
        Product productWithDescription = new Product("TABLET", "iPad Pro", "Professional tablet device", Money.of(3000.00, "CNY"), 1L, 20);
        List<Product> products = Arrays.asList(testProduct, testProduct2, productWithDescription);
        when(productRepository.findAll()).thenReturn(products);

        // When - search by description keyword
        List<Product> result = productService.searchProductsByName("tablet");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("TABLET", result.get(0).getSku());
        verify(productRepository).findAll();
    }

    @Test
    void searchProductsByName_WithWhitespaceSearchTerm() {
        // Given
        List<Product> products = Arrays.asList(testProduct, testProduct2);
        when(productRepository.findAll()).thenReturn(products);

        // When
        List<Product> result = productService.searchProductsByName("   ");

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(productRepository).findAll();
    }

    @Test
    void searchAvailableProducts_WithDescriptionMatch() {
        // Given - create products with different descriptions
        Product productWithDescription = new Product("TABLET", "iPad Pro", "Professional tablet device", Money.of(3000.00, "CNY"), 1L, 20);
        List<Product> products = Arrays.asList(testProduct, testProduct2, productWithDescription);
        when(productRepository.findAll()).thenReturn(products);

        // When - search by description keyword
        List<Product> result = productService.searchAvailableProducts("tablet");

        // Then
        assertNotNull(result);
        verify(productRepository).findAll();
    }

    @Test
    void searchAvailableProducts_WithWhitespaceSearchTerm() {
        // Given
        List<Product> products = Arrays.asList(testProduct, testProduct2);
        when(productRepository.findAll()).thenReturn(products);

        // When
        List<Product> result = productService.searchAvailableProducts("   ");

        // Then
        assertNotNull(result);
        verify(productRepository).findAll();
    }

    @Test
    void searchAvailableProducts_NullSearchTerm() {
        // Given
        List<Product> products = Arrays.asList(testProduct, testProduct2);
        when(productRepository.findAll()).thenReturn(products);

        // When
        List<Product> result = productService.searchAvailableProducts(null);

        // Then
        assertNotNull(result);
        verify(productRepository).findAll();
    }
} 