package com.ecommerce.application.service;

import com.ecommerce.application.dto.PurchaseRequest;
import com.ecommerce.application.dto.PurchaseResponse;
import com.ecommerce.domain.Money;
import com.ecommerce.domain.user.User;
import com.ecommerce.domain.product.Product;
import com.ecommerce.domain.merchant.Merchant;
import com.ecommerce.domain.order.Order;
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
        
        Merchant merchant = new Merchant("Test Merchant", "BL123.6", "merchant@test.com", "555-1234", "USD");
        
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

    @Test
    void shouldThrowExceptionWhenProductIsInactive() {
        // Arrange
        Long userId = 1L;
        String productSku = "INACTIVE-PRODUCT";
        int quantity = 1;
        
        User user = new User("john", "john@example.com", "123-456-7890", "USD");
        user.recharge(Money.of("100.00", "USD"));
        
        Money productPrice = Money.of("20.00", "USD");
        Product product = new Product(productSku, "Inactive Product", "Description", productPrice, 1L, 10);
        product.deactivate(); // Deactivate product
        
        Merchant merchant = new Merchant("Test Merchant", "BL123456", "merchant@test.com", "555-1234", "USD");
        
        when(userService.getUserById(userId)).thenReturn(user);
        when(productService.getProductBySku(productSku)).thenReturn(product);
        when(merchantService.getMerchantById(1L)).thenReturn(merchant);
        
        PurchaseRequest request = new PurchaseRequest(userId, productSku, quantity);
        
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> ecommerceService.processPurchase(request));
        assertTrue(exception.getMessage().contains("Product is not active"));
    }

    @Test
    void shouldThrowExceptionWhenMerchantIsInactive() {
        // Arrange
        Long userId = 1L;
        String productSku = "PRODUCT-001";
        int quantity = 1;
        
        User user = new User("john", "john@example.com", "123-456-7890", "USD");
        user.recharge(Money.of("100.00", "USD"));
        
        Money productPrice = Money.of("20.00", "USD");
        Product product = new Product(productSku, "Test Product", "Description", productPrice, 1L, 10);
        
        Merchant merchant = new Merchant("Inactive Merchant", "BL123456", "merchant@test.com", "555-1234", "USD");
        merchant.deactivate(); // Deactivate merchant
        
        when(userService.getUserById(userId)).thenReturn(user);
        when(productService.getProductBySku(productSku)).thenReturn(product);
        when(merchantService.getMerchantById(1L)).thenReturn(merchant);
        
        PurchaseRequest request = new PurchaseRequest(userId, productSku, quantity);
        
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> ecommerceService.processPurchase(request));
        assertTrue(exception.getMessage().contains("Merchant is not active"));
    }

    @Test
    void shouldThrowExceptionWhenQuantityIsZero() {
        // Arrange
        Long userId = 1L;
        String productSku = "PRODUCT-001";
        int quantity = 0; // Invalid quantity
        
        User user = new User("john", "john@example.com", "123-456-7890", "USD");
        user.recharge(Money.of("100.00", "USD"));
        
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
        assertTrue(exception.getMessage().contains("Quantity must be positive"));
    }

    @Test
    void shouldSuccessfullyCancelOrder() {
        // Arrange
        String orderNumber = "ORDER-123";
        String reason = "Customer request";
        
        Order order = new Order(orderNumber, 1L, 1L);
        order.addOrderItem("PRODUCT-001", "Test Product", Money.of("20.00", "USD"), 1);
        order.confirm(); // Set status to CONFIRMED
        
        // Mock the product for inventory restoration
        Product product = new Product("PRODUCT-001", "Test Product", "Description", Money.of("20.00", "USD"), 1L, 10);
        when(productService.getProductBySku("PRODUCT-001")).thenReturn(product);
        
        when(orderService.getOrderByNumber(orderNumber)).thenReturn(order);
        
        // Act
        ecommerceService.cancelOrder(orderNumber, reason);
        
        // Assert
        verify(orderService).getOrderByNumber(orderNumber);
        verify(orderService).saveOrder(order);
        verify(productService).getProductBySku("PRODUCT-001");
        verify(productService).saveProduct(product);
    }

    @Test
    void shouldThrowExceptionWhenCancellingCompletedOrder() {
        // Arrange
        String orderNumber = "ORDER-123";
        String reason = "Customer request";
        
        Order order = new Order(orderNumber, 1L, 1L);
        order.addOrderItem("PRODUCT-001", "Test Product", Money.of("20.00", "USD"), 1);
        order.confirm();
        order.processPayment();
        order.complete(); // Set status to COMPLETED
        
        when(orderService.getOrderByNumber(orderNumber)).thenReturn(order);
        
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> ecommerceService.cancelOrder(orderNumber, reason));
        assertTrue(exception.getMessage().contains("Cannot cancel completed order"));
    }

    @Test
    void shouldThrowExceptionWhenCancellingAlreadyCancelledOrder() {
        // Arrange
        String orderNumber = "ORDER-123";
        String reason = "Customer request";
        
        Order order = new Order(orderNumber, 1L, 1L);
        order.addOrderItem("PRODUCT-001", "Test Product", Money.of("20.00", "USD"), 1);
        order.confirm();
        order.cancel("Previous cancellation"); // Set status to CANCELLED
        
        when(orderService.getOrderByNumber(orderNumber)).thenReturn(order);
        
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> ecommerceService.cancelOrder(orderNumber, reason));
        assertTrue(exception.getMessage().contains("Order is already cancelled"));
    }

    @Test
    void shouldSuccessfullyCancelPaidOrderWithRefund() {
        // Arrange
        String orderNumber = "ORDER-PAID-123";
        String reason = "System error";
        
        // Setup paid order
        Order order = new Order(orderNumber, 1L, 2L);
        order.addOrderItem("PRODUCT-001", "Test Product", Money.of("30.00", "USD"), 2);
        order.confirm();
        order.processPayment(); // Order is paid, needs refund
        
        // Setup user and merchant for refund
        User user = new User("john", "john@example.com", "123-456-7890", "USD");
        user.setId(1L);
        user.recharge(Money.of("100.00", "USD"));
        user.deduct(Money.of("60.00", "USD")); // Simulate user already paid
        
        Merchant merchant = new Merchant("Test Merchant", "BL123456", "merchant@test.com", "555-1234", "USD");
        merchant.setId(2L);
        merchant.receiveIncome(Money.of("60.00", "USD")); // Merchant received payment
        
        Product product = new Product("PRODUCT-001", "Test Product", "Description", Money.of("30.00", "USD"), 2L, 10);
        
        when(orderService.getOrderByNumber(orderNumber)).thenReturn(order);
        when(userService.getUserById(1L)).thenReturn(user);
        when(merchantService.getMerchantById(2L)).thenReturn(merchant);
        when(productService.getProductBySku("PRODUCT-001")).thenReturn(product);
        
        // Act
        ecommerceService.cancelOrder(orderNumber, reason);
        
        // Assert
        verify(orderService).getOrderByNumber(orderNumber);
        verify(userService).getUserById(1L);
        verify(merchantService).getMerchantById(2L);
        verify(productService).getProductBySku("PRODUCT-001");
        verify(userService).saveUser(user);
        verify(merchantService).saveMerchant(merchant);
        verify(productService).saveProduct(product);
        verify(orderService).saveOrder(order);
        
        // Verify user got refund and merchant's income was deducted
        assertEquals(Money.of("100.00", "USD"), user.getBalance()); // Back to original balance
        assertEquals(Money.zero("USD"), merchant.getBalance()); // Refund deducted
    }

    @Test
    void shouldThrowExceptionWhenMerchantHasInsufficientFundsForRefund() {
        // Arrange
        String orderNumber = "ORDER-INSUFFICIENT-123";
        String reason = "System error";
        
        Order order = new Order(orderNumber, 1L, 2L);
        order.addOrderItem("PRODUCT-001", "Test Product", Money.of("50.00", "USD"), 1);
        order.confirm();
        order.processPayment(); // Order is paid, needs refund
        
        User user = new User("john", "john@example.com", "123-456-7890", "USD");
        user.setId(1L);
        
        // Merchant has insufficient funds for refund
        Merchant merchant = new Merchant("Test Merchant", "BL123456", "merchant@test.com", "555-1234", "USD");
        merchant.setId(2L);
        merchant.receiveIncome(Money.of("20.00", "USD")); // Only $20, but needs to refund $50
        
        when(orderService.getOrderByNumber(orderNumber)).thenReturn(order);
        when(userService.getUserById(1L)).thenReturn(user);
        when(merchantService.getMerchantById(2L)).thenReturn(merchant);
        
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> ecommerceService.cancelOrder(orderNumber, reason));
        assertTrue(exception.getMessage().contains("Merchant has insufficient funds for refund"));
    }

    @Test
    void shouldCancelPendingOrderWithoutRefundOrInventoryRestore() {
        // Arrange
        String orderNumber = "ORDER-PENDING-123";
        String reason = "Customer request";
        
        Order order = new Order(orderNumber, 1L, 2L);
        order.addOrderItem("PRODUCT-001", "Test Product", Money.of("25.00", "USD"), 1);
        // Order is still PENDING - no inventory deducted, no payment made
        
        when(orderService.getOrderByNumber(orderNumber)).thenReturn(order);
        
        // Act
        ecommerceService.cancelOrder(orderNumber, reason);
        
        // Assert
        verify(orderService).getOrderByNumber(orderNumber);
        verify(orderService).saveOrder(order);
        // Should not call user, merchant, or product services for pending orders
        verify(userService, never()).getUserById(anyLong());
        verify(merchantService, never()).getMerchantById(anyLong());
        verify(productService, never()).getProductBySku(anyString());
        assertTrue(order.isCancelled());
    }

    @Test
    void shouldHandleComplexPurchaseWithMultipleCurrencies() {
        // Arrange
        Long userId = 1L;
        String productSku = "PRODUCT-EUR";
        int quantity = 3;
        
        User user = new User("john", "john@example.com", "123-456-7890", "EUR");
        user.setId(userId);
        user.recharge(Money.of("150.00", "EUR"));
        
        Money productPrice = Money.of("25.00", "EUR");
        Product product = new Product(productSku, "European Product", "Description", productPrice, 1L, 20);
        
        Merchant merchant = new Merchant("European Merchant", "BL123456", "merchant@test.com", "555-1234", "EUR");
        merchant.setId(1L);
        
        when(userService.getUserById(userId)).thenReturn(user);
        when(productService.getProductBySku(productSku)).thenReturn(product);
        when(merchantService.getMerchantById(1L)).thenReturn(merchant);
        
        PurchaseRequest request = new PurchaseRequest(userId, productSku, quantity);
        
        // Act
        PurchaseResponse response = ecommerceService.processPurchase(request);
        
        // Assert
        assertEquals(Money.of("75.00", "EUR"), response.getTotalAmount()); // 25 * 3 = 75
        assertEquals(Money.of("75.00", "EUR"), user.getBalance()); // 150 - 75 = 75
        assertEquals(Money.of("75.00", "EUR"), merchant.getBalance()); // Received payment
        assertEquals(17, product.getAvailableInventory()); // 20 - 3 = 17
    }

    @Test
    void shouldValidateOrderNumberGeneration() {
        // Arrange
        Long userId = 1L;
        String productSku = "PRODUCT-001";
        int quantity = 1;
        
        User user = new User("john", "john@example.com", "123-456-7890", "USD");
        user.setId(userId);
        user.recharge(Money.of("100.00", "USD"));
        
        Money productPrice = Money.of("20.00", "USD");
        Product product = new Product(productSku, "Test Product", "Description", productPrice, 1L, 10);
        
        Merchant merchant = new Merchant("Test Merchant", "BL123456", "merchant@test.com", "555-1234", "USD");
        merchant.setId(1L);
        
        when(userService.getUserById(userId)).thenReturn(user);
        when(productService.getProductBySku(productSku)).thenReturn(product);
        when(merchantService.getMerchantById(1L)).thenReturn(merchant);
        
        PurchaseRequest request = new PurchaseRequest(userId, productSku, quantity);
        
        // Act
        PurchaseResponse response1 = ecommerceService.processPurchase(request);
        
        // Reset mocks for second call
        user.recharge(Money.of("20.00", "USD")); // Add more balance for second purchase
        product.addInventory(1); // Add inventory back
        
        PurchaseResponse response2 = ecommerceService.processPurchase(request);
        
        // Assert
        assertNotNull(response1.getOrderNumber());
        assertNotNull(response2.getOrderNumber());
        assertNotEquals(response1.getOrderNumber(), response2.getOrderNumber());
        assertTrue(response1.getOrderNumber().startsWith("ORD"));
        assertTrue(response2.getOrderNumber().startsWith("ORD"));
    }

    @Test
    void shouldThrowExceptionWhenCancellingNonExistentOrder() {
        // Arrange
        String orderNumber = "NON-EXISTENT-ORDER";
        String reason = "Test";
        
        when(orderService.getOrderByNumber(orderNumber))
            .thenThrow(new RuntimeException("Order not found with number: " + orderNumber));
        
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> ecommerceService.cancelOrder(orderNumber, reason));
        assertTrue(exception.getMessage().contains("Order not found"));
    }

    @Test
    void shouldHandleLargeQuantityPurchase() {
        // Arrange
        Long userId = 1L;
        String productSku = "BULK-PRODUCT";
        int quantity = 1000;
        
        User user = new User("bulk-buyer", "bulk@example.com", "123-456-7890", "USD");
        user.setId(userId);
        user.recharge(Money.of("10000.00", "USD")); // $10,000 balance
        
        Money productPrice = Money.of("5.00", "USD");
        Product product = new Product(productSku, "Bulk Product", "Description", productPrice, 1L, 2000); // 2000 in stock
        
        Merchant merchant = new Merchant("Bulk Merchant", "BL123456", "merchant@test.com", "555-1234", "USD");
        merchant.setId(1L);
        
        when(userService.getUserById(userId)).thenReturn(user);
        when(productService.getProductBySku(productSku)).thenReturn(product);
        when(merchantService.getMerchantById(1L)).thenReturn(merchant);
        
        PurchaseRequest request = new PurchaseRequest(userId, productSku, quantity);
        
        // Act
        PurchaseResponse response = ecommerceService.processPurchase(request);
        
        // Assert
        assertEquals(Money.of("5000.00", "USD"), response.getTotalAmount()); // 5 * 1000 = 5000
        assertEquals(Money.of("5000.00", "USD"), user.getBalance()); // 10000 - 5000 = 5000
        assertEquals(Money.of("5000.00", "USD"), merchant.getBalance()); // Received payment
        assertEquals(1000, product.getAvailableInventory()); // 2000 - 1000 = 1000
    }

    // Additional comprehensive test cases for better coverage

    @Test
    void shouldHandlePurchaseWithExactBalance() {
        // Arrange - Test edge case where user has exact amount needed
        Long userId = 1L;
        String productSku = "EXACT-PRICE-PRODUCT";
        int quantity = 2;
        
        User user = new User("john", "john@example.com", "123-456-7890", "USD");
        user.setId(userId);
        user.recharge(Money.of("50.00", "USD")); // Exact amount needed
        
        Money productPrice = Money.of("25.00", "USD");
        Product product = new Product(productSku, "Exact Price Product", "Description", productPrice, 1L, 10);
        
        Merchant merchant = new Merchant("Test Merchant", "BL123456", "merchant@test.com", "555-1234", "USD");
        merchant.setId(1L);
        
        when(userService.getUserById(userId)).thenReturn(user);
        when(productService.getProductBySku(productSku)).thenReturn(product);
        when(merchantService.getMerchantById(1L)).thenReturn(merchant);
        
        PurchaseRequest request = new PurchaseRequest(userId, productSku, quantity);
        
        // Act
        PurchaseResponse response = ecommerceService.processPurchase(request);
        
        // Assert
        assertEquals(Money.of("50.00", "USD"), response.getTotalAmount()); // 25 * 2 = 50
        assertEquals(Money.zero("USD"), user.getBalance()); // Exactly 0 left
        assertEquals(Money.of("50.00", "USD"), merchant.getBalance()); // Received payment
    }

    @Test
    void shouldHandlePurchaseWithExactInventory() {
        // Arrange - Test edge case where purchasing exact inventory
        Long userId = 1L;
        String productSku = "LAST-ITEMS-PRODUCT";
        int quantity = 5; // Exact inventory amount
        
        User user = new User("john", "john@example.com", "123-456-7890", "USD");
        user.setId(userId);
        user.recharge(Money.of("1000.00", "USD"));
        
        Money productPrice = Money.of("20.00", "USD");
        Product product = new Product(productSku, "Last Items Product", "Description", productPrice, 1L, 5); // Exactly 5 in stock
        
        Merchant merchant = new Merchant("Test Merchant", "BL123456", "merchant@test.com", "555-1234", "USD");
        merchant.setId(1L);
        
        when(userService.getUserById(userId)).thenReturn(user);
        when(productService.getProductBySku(productSku)).thenReturn(product);
        when(merchantService.getMerchantById(1L)).thenReturn(merchant);
        
        PurchaseRequest request = new PurchaseRequest(userId, productSku, quantity);
        
        // Act
        PurchaseResponse response = ecommerceService.processPurchase(request);
        
        // Assert
        assertEquals(Money.of("100.00", "USD"), response.getTotalAmount()); // 20 * 5 = 100
        assertEquals(0, product.getAvailableInventory()); // Exactly 0 left
        assertEquals(Money.of("100.00", "USD"), merchant.getBalance()); // Received payment
    }

    @Test
    void shouldHandleCancelOrderWithMultipleItems() {
        // Arrange - Test cancellation with multiple order items
        String orderNumber = "ORDER-MULTI-123";
        String reason = "Bulk cancellation";
        
        Order order = new Order(orderNumber, 1L, 2L);
        order.addOrderItem("PRODUCT-001", "Product 1", Money.of("20.00", "USD"), 2);
        order.addOrderItem("PRODUCT-002", "Product 2", Money.of("30.00", "USD"), 1);
        order.confirm();
        order.processPayment(); // Order is paid, needs refund
        
        User user = new User("john", "john@example.com", "123-456-7890", "USD");
        user.setId(1L);
        user.recharge(Money.of("100.00", "USD"));
        user.deduct(Money.of("70.00", "USD")); // Total: 20*2 + 30*1 = 70
        
        Merchant merchant = new Merchant("Test Merchant", "BL123456", "merchant@test.com", "555-1234", "USD");
        merchant.setId(2L);
        merchant.receiveIncome(Money.of("70.00", "USD"));
        
        Product product1 = new Product("PRODUCT-001", "Product 1", "Description", Money.of("20.00", "USD"), 2L, 10);
        Product product2 = new Product("PRODUCT-002", "Product 2", "Description", Money.of("30.00", "USD"), 2L, 15);
        
        when(orderService.getOrderByNumber(orderNumber)).thenReturn(order);
        when(userService.getUserById(1L)).thenReturn(user);
        when(merchantService.getMerchantById(2L)).thenReturn(merchant);
        when(productService.getProductBySku("PRODUCT-001")).thenReturn(product1);
        when(productService.getProductBySku("PRODUCT-002")).thenReturn(product2);
        
        // Act
        ecommerceService.cancelOrder(orderNumber, reason);
        
        // Assert
        verify(orderService).getOrderByNumber(orderNumber);
        verify(userService).getUserById(1L);
        verify(merchantService).getMerchantById(2L);
        verify(productService).getProductBySku("PRODUCT-001");
        verify(productService).getProductBySku("PRODUCT-002");
        verify(userService).saveUser(user);
        verify(merchantService).saveMerchant(merchant);
        verify(productService, times(2)).saveProduct(any(Product.class));
        verify(orderService).saveOrder(order);
        
        // Verify refund and inventory restore
        assertEquals(Money.of("100.00", "USD"), user.getBalance()); // Back to original
        assertEquals(Money.zero("USD"), merchant.getBalance()); // Refund deducted
        assertEquals(12, product1.getAvailableInventory()); // 10 + 2 restored
        assertEquals(16, product2.getAvailableInventory()); // 15 + 1 restored
    }

    @Test
    void shouldThrowExceptionWhenQuantityIsNegative() {
        // Arrange
        Long userId = 1L;
        String productSku = "PRODUCT-001";
        int quantity = -5; // Negative quantity
        
        User user = new User("john", "john@example.com", "123-456-7890", "USD");
        user.recharge(Money.of("100.00", "USD"));
        
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
        assertTrue(exception.getMessage().contains("Quantity must be positive"));
    }

    @Test
    void shouldHandlePurchaseWithJPYCurrency() {
        // Arrange - Test with JPY currency (no decimal places)
        Long userId = 1L;
        String productSku = "PRODUCT-JPY";
        int quantity = 3;
        
        User user = new User("takeshi", "takeshi@example.com", "123-456-7890", "JPY");
        user.setId(userId);
        user.recharge(Money.of("5000.00", "JPY")); // 5000 yen
        
        Money productPrice = Money.of("1000.00", "JPY"); // 1000 yen per item
        Product product = new Product(productSku, "Japanese Product", "Description", productPrice, 1L, 10);
        
        Merchant merchant = new Merchant("Japanese Merchant", "BL123456", "merchant@test.com", "555-1234", "JPY");
        merchant.setId(1L);
        
        when(userService.getUserById(userId)).thenReturn(user);
        when(productService.getProductBySku(productSku)).thenReturn(product);
        when(merchantService.getMerchantById(1L)).thenReturn(merchant);
        
        PurchaseRequest request = new PurchaseRequest(userId, productSku, quantity);
        
        // Act
        PurchaseResponse response = ecommerceService.processPurchase(request);
        
        // Assert
        assertEquals(Money.of("3000.00", "JPY"), response.getTotalAmount()); // 1000 * 3 = 3000
        assertEquals(Money.of("2000.00", "JPY"), user.getBalance()); // 5000 - 3000 = 2000
        assertEquals(Money.of("3000.00", "JPY"), merchant.getBalance()); // Received payment
    }

    @Test
    void shouldCancelOrderWithDifferentCurrencies() {
        // Arrange - Test cancellation with EUR currency
        String orderNumber = "ORDER-EUR-123";
        String reason = "Customer request";
        
        Order order = new Order(orderNumber, 1L, 2L);
        order.addOrderItem("PRODUCT-EUR", "European Product", Money.of("50.00", "EUR"), 2);
        order.confirm();
        order.processPayment();
        
        User user = new User("pierre", "pierre@example.com", "123-456-7890", "EUR");
        user.setId(1L);
        user.recharge(Money.of("200.00", "EUR"));
        user.deduct(Money.of("100.00", "EUR")); // 50 * 2 = 100
        
        Merchant merchant = new Merchant("European Merchant", "BL123456", "merchant@test.com", "555-1234", "EUR");
        merchant.setId(2L);
        merchant.receiveIncome(Money.of("100.00", "EUR"));
        
        Product product = new Product("PRODUCT-EUR", "European Product", "Description", Money.of("50.00", "EUR"), 2L, 10);
        
        when(orderService.getOrderByNumber(orderNumber)).thenReturn(order);
        when(userService.getUserById(1L)).thenReturn(user);
        when(merchantService.getMerchantById(2L)).thenReturn(merchant);
        when(productService.getProductBySku("PRODUCT-EUR")).thenReturn(product);
        
        // Act
        ecommerceService.cancelOrder(orderNumber, reason);
        
        // Assert
        assertEquals(Money.of("200.00", "EUR"), user.getBalance()); // Back to original
        assertEquals(Money.zero("EUR"), merchant.getBalance()); // Refund deducted
        assertEquals(12, product.getAvailableInventory()); // 10 + 2 restored
    }

    @Test
    void shouldHandleCancelOrderWhenProductServiceFails() {
        // Arrange - Test exception handling during cancellation
        String orderNumber = "ORDER-SERVICE-FAIL";
        String reason = "Test exception handling";
        
        Order order = new Order(orderNumber, 1L, 2L);
        order.addOrderItem("PRODUCT-FAIL", "Failing Product", Money.of("25.00", "USD"), 1);
        order.confirm(); // Needs inventory restore
        
        when(orderService.getOrderByNumber(orderNumber)).thenReturn(order);
        when(productService.getProductBySku("PRODUCT-FAIL"))
            .thenThrow(new RuntimeException("Product service unavailable"));
        
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> ecommerceService.cancelOrder(orderNumber, reason));
        assertTrue(exception.getMessage().contains("Failed to cancel order"));
        
        verify(orderService).getOrderByNumber(orderNumber);
        verify(productService).getProductBySku("PRODUCT-FAIL");
        // Order should not be saved if exception occurs during processing
        verify(orderService, never()).saveOrder(order);
    }
}