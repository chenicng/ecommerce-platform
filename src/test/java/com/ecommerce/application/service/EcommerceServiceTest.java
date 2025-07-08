package com.ecommerce.application.service;

import com.ecommerce.application.dto.PurchaseRequest;
import com.ecommerce.application.dto.PurchaseResponse;
import com.ecommerce.domain.Money;
import com.ecommerce.domain.user.User;
import com.ecommerce.domain.product.Product;
import com.ecommerce.domain.merchant.Merchant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EcommerceServiceTest {

    @Mock
    private UserService userService;
    
    @Mock
    private ProductService productService;
    
    @Mock
    private MerchantService merchantService;
    
    @Mock
    private OrderService orderService;
    
    private EcommerceService ecommerceService;

    @BeforeEach
    void setUp() {
        ecommerceService = new EcommerceService(userService, merchantService, productService, orderService);
    }

    @Test
    void shouldSuccessfullyProcessPurchase() {
        // Arrange
        Long userId = 1L;
        String productSku = "PRODUCT-001";
        int quantity = 2;
        
        User user = new User("john", "john@example.com", "123-456-7890", "USD");
        user.setId(userId); // Set the user ID for testing
        user.recharge(Money.of("100.00", "USD"));
        
        Money productPrice = Money.of("20.00", "USD");
        Product product = new Product(productSku, "Test Product", "Description", productPrice, 1L, 10);
        
        Merchant merchant = new Merchant("Test Merchant", "BL123456", "merchant@test.com", "555-1234", "USD");
        merchant.setId(1L); // Set the merchant ID for testing
        
        when(userService.getUserById(userId)).thenReturn(user);
        when(productService.getProductBySku(productSku)).thenReturn(product);
        when(merchantService.getMerchantById(1L)).thenReturn(merchant);
        
        PurchaseRequest request = new PurchaseRequest(userId, productSku, quantity);
        
        // Act
        PurchaseResponse response = ecommerceService.processPurchase(request);
        
        // Assert
        assertEquals("SUCCESS", response.getStatus());
        assertNotNull(response.getOrderNumber());
        assertEquals(userId, response.getUserId());
        assertEquals(1L, response.getMerchantId());
        assertEquals(productSku, response.getSku());
        assertEquals(quantity, response.getQuantity());
        assertEquals(Money.of("40.00", "USD"), response.getTotalAmount()); // 20 * 2 = 40
        
        verify(userService).getUserById(userId);
        verify(productService).getProductBySku(productSku);
        verify(merchantService).getMerchantById(1L);
        verify(userService).saveUser(user);
        verify(productService).saveProduct(product);
        verify(merchantService).saveMerchant(merchant);
        verify(orderService).saveOrder(any());
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        // Arrange
        Long userId = 999L;
        String productSku = "PRODUCT-001";
        int quantity = 1;
        
        when(userService.getUserById(userId)).thenThrow(new RuntimeException("User not found with id: " + userId));
        
        PurchaseRequest request = new PurchaseRequest(userId, productSku, quantity);
        
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> ecommerceService.processPurchase(request));
        assertTrue(exception.getMessage().contains("User not found"));
    }

    @Test
    void shouldThrowExceptionWhenProductNotFound() {
        // Arrange
        Long userId = 1L;
        String productSku = "nonexistent";
        int quantity = 1;
        
        User user = new User("john", "john@example.com", "123-456-7890", "USD");
        when(userService.getUserById(userId)).thenReturn(user);
        when(productService.getProductBySku(productSku)).thenThrow(new RuntimeException("Product not found with SKU: " + productSku));
        
        PurchaseRequest request = new PurchaseRequest(userId, productSku, quantity);
        
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> ecommerceService.processPurchase(request));
        assertTrue(exception.getMessage().contains("Product not found"));
    }

    @Test
    void shouldThrowExceptionWhenInsufficientBalance() {
        // Arrange
        Long userId = 1L;
        String productSku = "EXPENSIVE-PRODUCT";
        int quantity = 1;
        
        User user = new User("john", "john@example.com", "123-456-7890", "USD");
        user.recharge(Money.of("10.00", "USD")); // Only $10
        
        Money productPrice = Money.of("50.00", "USD"); // Product costs $50
        Product product = new Product(productSku, "Expensive Product", "Description", productPrice, 1L, 10);
        
        Merchant merchant = new Merchant("Test Merchant", "BL123456", "merchant@test.com", "555-1234", "USD");
        
        when(userService.getUserById(userId)).thenReturn(user);
        when(productService.getProductBySku(productSku)).thenReturn(product);
        when(merchantService.getMerchantById(1L)).thenReturn(merchant);
        
        PurchaseRequest request = new PurchaseRequest(userId, productSku, quantity);
        
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> ecommerceService.processPurchase(request));
        assertTrue(exception.getMessage().contains("Insufficient balance"));
    }

    @Test
    void shouldThrowExceptionWhenInsufficientInventory() {
        // Arrange
        Long userId = 1L;
        String productSku = "LOW-STOCK-PRODUCT";
        int quantity = 10; // Requesting 10 items
        
        User user = new User("john", "john@example.com", "123-456-7890", "USD");
        user.recharge(Money.of("1000.00", "USD")); // Enough money
        
        Money productPrice = Money.of("20.00", "USD");
        Product product = new Product(productSku, "Low Stock Product", "Description", productPrice, 1L, 5); // Only 5 in stock
        
        Merchant merchant = new Merchant("Test Merchant", "BL123456", "merchant@test.com", "555-1234", "USD");
        
        when(userService.getUserById(userId)).thenReturn(user);
        when(productService.getProductBySku(productSku)).thenReturn(product);
        when(merchantService.getMerchantById(1L)).thenReturn(merchant);
        
        PurchaseRequest request = new PurchaseRequest(userId, productSku, quantity);
        
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> ecommerceService.processPurchase(request));
        assertTrue(exception.getMessage().contains("Insufficient inventory"));
    }

    @Test
    void shouldThrowExceptionWhenUserIsInactive() {
        // Arrange
        Long userId = 1L;
        String productSku = "PRODUCT-001";
        int quantity = 1;
        
        User user = new User("john", "john@example.com", "123-456-7890", "USD");
        user.deactivate(); // Deactivate user
        
        Money productPrice = Money.of("20.00", "USD");
        Product product = new Product(productSku, "Test Product", "Description", productPrice, 1L, 10);
        
        Merchant merchant = new Merchant("Test Merchant", "BL123456", "merchant@test.com", "555-1234", "USD");
        
        when(userService.getUserById(userId)).thenReturn(user);
        when(productService.getProductBySku(productSku)).thenReturn(product);
        when(merchantService.getMerchantById(1L)).thenReturn(merchant);
        
        PurchaseRequest request = new PurchaseRequest(userId, productSku, quantity);
        
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> ecommerceService.processPurchase(request));
        assertTrue(exception.getMessage().contains("User is not active"));
    }
}