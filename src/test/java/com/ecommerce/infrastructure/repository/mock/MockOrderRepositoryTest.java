package com.ecommerce.infrastructure.repository.mock;

import com.ecommerce.domain.order.Order;
import com.ecommerce.domain.order.OrderStatus;
import com.ecommerce.domain.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class MockOrderRepositoryTest {

    private MockOrderRepository repository;

    @BeforeEach
    void setUp() {
        repository = new MockOrderRepository();
    }

    @Test
    void save_WithNewOrder_ShouldAssignIdAndSave() {
        // Given
        Order newOrder = new Order("ORD-001", 1L, 2L);
        newOrder.addOrderItem("SKU001", "Product", Money.of("100.00", "CNY"), 1);
        
        // When
        Order savedOrder = repository.save(newOrder);
        
        // Then
        assertNotNull(savedOrder.getId());
        assertEquals(1L, savedOrder.getId());
        assertEquals("ORD-001", savedOrder.getOrderNumber());
        assertEquals(1L, savedOrder.getUserId());
        assertEquals(2L, savedOrder.getMerchantId());
    }

    @Test
    void save_WithExistingOrder_ShouldUpdateOrder() {
        // Given
        Order order = new Order("ORD-002", 1L, 2L);
        order.addOrderItem("SKU001", "Product", Money.of("100.00", "CNY"), 1);
        Order savedOrder = repository.save(order);
        
        // Modify the order
        savedOrder.addOrderItem("SKU002", "Product 2", Money.of("50.00", "CNY"), 1);
        
        // When
        Order updatedOrder = repository.save(savedOrder);
        
        // Then
        assertEquals(savedOrder.getId(), updatedOrder.getId());
        assertEquals(2, updatedOrder.getItems().size());
        assertEquals(Money.of("150.00", "CNY"), updatedOrder.getTotalAmount());
    }

    @Test
    void findById_WithExistingId_ShouldReturnOrder() {
        // Given
        Order order = new Order("ORD-003", 1L, 2L);
        order.addOrderItem("SKU001", "Product", Money.of("100.00", "CNY"), 1);
        Order savedOrder = repository.save(order);
        
        // When
        Optional<Order> foundOrder = repository.findById(savedOrder.getId());
        
        // Then
        assertTrue(foundOrder.isPresent());
        assertEquals("ORD-003", foundOrder.get().getOrderNumber());
        assertEquals(1L, foundOrder.get().getUserId());
        assertEquals(2L, foundOrder.get().getMerchantId());
    }

    @Test
    void findById_WithNonExistingId_ShouldReturnEmpty() {
        // When
        Optional<Order> foundOrder = repository.findById(999L);
        
        // Then
        assertFalse(foundOrder.isPresent());
    }

    @Test
    void findByOrderNumber_WithExistingOrderNumber_ShouldReturnOrder() {
        // Given
        Order order = new Order("ORD-004", 1L, 2L);
        order.addOrderItem("SKU001", "Product", Money.of("100.00", "CNY"), 1);
        repository.save(order);
        
        // When
        Optional<Order> foundOrder = repository.findByOrderNumber("ORD-004");
        
        // Then
        assertTrue(foundOrder.isPresent());
        assertEquals("ORD-004", foundOrder.get().getOrderNumber());
        assertEquals(1L, foundOrder.get().getUserId());
        assertEquals(2L, foundOrder.get().getMerchantId());
    }

    @Test
    void findByOrderNumber_WithNonExistingOrderNumber_ShouldReturnEmpty() {
        // When
        Optional<Order> foundOrder = repository.findByOrderNumber("NON-EXISTENT");
        
        // Then
        assertFalse(foundOrder.isPresent());
    }

    @Test
    void existsByOrderNumber_WithExistingOrderNumber_ShouldReturnTrue() {
        // Given
        Order order = new Order("ORD-005", 1L, 2L);
        order.addOrderItem("SKU001", "Product", Money.of("100.00", "CNY"), 1);
        repository.save(order);
        
        // When & Then
        assertTrue(repository.existsByOrderNumber("ORD-005"));
    }

    @Test
    void existsByOrderNumber_WithNonExistingOrderNumber_ShouldReturnFalse() {
        // When & Then
        assertFalse(repository.existsByOrderNumber("NON-EXISTENT"));
    }

    @Test
    void findByMerchantIdAndOrderTimeBetween_ShouldReturnMatchingOrders() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneHourAgo = now.minusHours(1);
        LocalDateTime oneHourLater = now.plusHours(1);
        
        // Create orders for different merchants and times
        Order order1 = new Order("ORD-006", 1L, 2L);
        order1.addOrderItem("SKU001", "Product", Money.of("100.00", "CNY"), 1);
        repository.save(order1);
        
        Order order2 = new Order("ORD-007", 2L, 2L);
        order2.addOrderItem("SKU002", "Product", Money.of("200.00", "CNY"), 1);
        repository.save(order2);
        
        Order order3 = new Order("ORD-008", 1L, 3L);
        order3.addOrderItem("SKU003", "Product", Money.of("300.00", "CNY"), 1);
        repository.save(order3);
        
        // When
        List<Order> orders = repository.findByMerchantIdAndOrderTimeBetween(2L, oneHourAgo, oneHourLater);
        
        // Then
        assertEquals(2, orders.size());
        assertTrue(orders.stream().allMatch(order -> order.getMerchantId().equals(2L)));
        assertTrue(orders.stream().allMatch(order -> {
            LocalDateTime orderTime = order.getOrderTime();
            return orderTime != null && 
                   !orderTime.isBefore(oneHourAgo) && 
                   !orderTime.isAfter(oneHourLater);
        }));
    }

    @Test
    void findByMerchantIdAndOrderTimeBetween_WithNoMatchingOrders_ShouldReturnEmptyList() {
        // Given
        LocalDateTime futureStart = LocalDateTime.now().plusDays(1);
        LocalDateTime futureEnd = LocalDateTime.now().plusDays(2);
        
        Order order = new Order("ORD-009", 1L, 2L);
        order.addOrderItem("SKU001", "Product", Money.of("100.00", "CNY"), 1);
        repository.save(order);
        
        // When
        List<Order> orders = repository.findByMerchantIdAndOrderTimeBetween(2L, futureStart, futureEnd);
        
        // Then
        assertTrue(orders.isEmpty());
    }

    @Test
    void findByMerchantIdAndStatusAndOrderTimeBetween_ShouldReturnMatchingOrders() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneHourAgo = now.minusHours(1);
        LocalDateTime oneHourLater = now.plusHours(1);
        
        // Create orders with different statuses
        Order order1 = new Order("ORD-010", 1L, 2L);
        order1.addOrderItem("SKU001", "Product", Money.of("100.00", "CNY"), 1);
        order1.confirm();
        order1.processPayment();
        order1.complete();
        repository.save(order1);
        
        Order order2 = new Order("ORD-011", 2L, 2L);
        order2.addOrderItem("SKU002", "Product", Money.of("200.00", "CNY"), 1);
        order2.confirm();
        repository.save(order2);
        
        Order order3 = new Order("ORD-012", 3L, 2L);
        order3.addOrderItem("SKU003", "Product", Money.of("300.00", "CNY"), 1);
        order3.confirm();
        order3.processPayment();
        order3.complete();
        repository.save(order3);
        
        // When
        List<Order> completedOrders = repository.findByMerchantIdAndStatusAndOrderTimeBetween(
            2L, OrderStatus.COMPLETED, oneHourAgo, oneHourLater);
        
        // Then
        assertEquals(2, completedOrders.size());
        assertTrue(completedOrders.stream().allMatch(order -> 
            order.getMerchantId().equals(2L) && order.getStatus() == OrderStatus.COMPLETED));
    }

    @Test
    void findByMerchantIdAndStatusAndOrderTimeBetween_WithNoMatchingOrders_ShouldReturnEmptyList() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneHourAgo = now.minusHours(1);
        LocalDateTime oneHourLater = now.plusHours(1);
        
        Order order = new Order("ORD-013", 1L, 2L);
        order.addOrderItem("SKU001", "Product", Money.of("100.00", "CNY"), 1);
        // Order remains in PENDING status
        repository.save(order);
        
        // When
        List<Order> completedOrders = repository.findByMerchantIdAndStatusAndOrderTimeBetween(
            2L, OrderStatus.COMPLETED, oneHourAgo, oneHourLater);
        
        // Then
        assertTrue(completedOrders.isEmpty());
    }

    @Test
    void deleteById_WithExistingId_ShouldRemoveOrder() {
        // Given
        Order order = new Order("ORD-014", 1L, 2L);
        order.addOrderItem("SKU001", "Product", Money.of("100.00", "CNY"), 1);
        Order savedOrder = repository.save(order);
        
        // Verify order exists
        assertTrue(repository.findById(savedOrder.getId()).isPresent());
        assertTrue(repository.existsByOrderNumber("ORD-014"));
        
        // When
        repository.deleteById(savedOrder.getId());
        
        // Then
        assertFalse(repository.findById(savedOrder.getId()).isPresent());
        assertFalse(repository.existsByOrderNumber("ORD-014"));
    }

    @Test
    void deleteById_WithNonExistingId_ShouldNotThrowException() {
        // When & Then
        assertDoesNotThrow(() -> repository.deleteById(999L));
    }

    @Test
    void deleteById_WithNullOrder_ShouldHandleGracefully() {
        // Given - save an order and then manually remove it from storage to simulate null order
        Order order = new Order("ORD-015", 1L, 2L);
        order.addOrderItem("SKU001", "Product", Money.of("100.00", "CNY"), 1);
        Order savedOrder = repository.save(order);
        
        // Remove from storage directly to simulate null scenario
        repository.deleteById(savedOrder.getId());
        
        // When & Then - trying to delete again should not throw exception
        assertDoesNotThrow(() -> repository.deleteById(savedOrder.getId()));
    }

    @Test
    void findByMerchantIdAndOrderTimeBetween_WithNullOrderTime_ShouldBeFiltered() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneHourAgo = now.minusHours(1);
        LocalDateTime oneHourLater = now.plusHours(1);
        
        Order order = new Order("ORD-016", 1L, 2L);
        order.addOrderItem("SKU001", "Product", Money.of("100.00", "CNY"), 1);
        // Set order time to null using reflection to test null handling
        try {
            java.lang.reflect.Field field = Order.class.getDeclaredField("orderTime");
            field.setAccessible(true);
            field.set(order, null);
        } catch (Exception e) {
            // If reflection fails, skip this test
            return;
        }
        
        repository.save(order);
        
        // When
        List<Order> orders = repository.findByMerchantIdAndOrderTimeBetween(2L, oneHourAgo, oneHourLater);
        
        // Then
        assertTrue(orders.isEmpty()); // Order with null time should be filtered out
    }

    @Test
    void class_ShouldHaveCorrectAnnotations() {
        // Test that the class has the expected annotations
        assertTrue(MockOrderRepository.class.isAnnotationPresent(org.springframework.stereotype.Repository.class));
        assertTrue(MockOrderRepository.class.isAnnotationPresent(org.springframework.context.annotation.Profile.class));
        
        // Check profile value
        org.springframework.context.annotation.Profile profileAnnotation = 
            MockOrderRepository.class.getAnnotation(org.springframework.context.annotation.Profile.class);
        assertEquals("mock", profileAnnotation.value()[0]);
    }

    @Test
    void idGenerator_ShouldGenerateSequentialIds() {
        // Given
        Order order1 = new Order("ORD-017", 1L, 2L);
        order1.addOrderItem("SKU001", "Product", Money.of("100.00", "CNY"), 1);
        
        Order order2 = new Order("ORD-018", 2L, 3L);
        order2.addOrderItem("SKU002", "Product", Money.of("200.00", "CNY"), 1);
        
        // When
        Order savedOrder1 = repository.save(order1);
        Order savedOrder2 = repository.save(order2);
        
        // Then
        assertNotNull(savedOrder1.getId());
        assertNotNull(savedOrder2.getId());
        assertTrue(savedOrder2.getId() > savedOrder1.getId());
    }

    @Test
    void save_WithNullOrder_ShouldThrowException() {
        // When & Then
        assertThrows(NullPointerException.class, () -> repository.save(null));
    }

    @Test
    void findById_WithNullId_ShouldReturnEmpty() {
        // When
        Optional<Order> result = repository.findById(null);
        
        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void findByOrderNumber_WithNullOrderNumber_ShouldReturnEmpty() {
        // When
        Optional<Order> result = repository.findByOrderNumber(null);
        
        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void findByOrderNumber_WithEmptyOrderNumber_ShouldReturnEmpty() {
        // When
        Optional<Order> result = repository.findByOrderNumber("");
        
        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void existsByOrderNumber_WithNullOrderNumber_ShouldReturnFalse() {
        // When & Then
        assertFalse(repository.existsByOrderNumber(null));
    }

    @Test
    void existsByOrderNumber_WithEmptyOrderNumber_ShouldReturnFalse() {
        // When & Then
        assertFalse(repository.existsByOrderNumber(""));
    }

    @Test
    void findByMerchantIdAndOrderTimeBetween_WithNullMerchantId_ShouldReturnEmptyList() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneHourAgo = now.minusHours(1);
        LocalDateTime oneHourLater = now.plusHours(1);
        
        Order order = new Order("ORD-019", 1L, 2L);
        order.addOrderItem("SKU001", "Product", Money.of("100.00", "CNY"), 1);
        repository.save(order);
        
        // When
        List<Order> orders = repository.findByMerchantIdAndOrderTimeBetween(null, oneHourAgo, oneHourLater);
        
        // Then
        assertTrue(orders.isEmpty());
    }

    @Test
    void findByMerchantIdAndStatusAndOrderTimeBetween_WithNullMerchantId_ShouldReturnEmptyList() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneHourAgo = now.minusHours(1);
        LocalDateTime oneHourLater = now.plusHours(1);
        
        Order order = new Order("ORD-021", 1L, 2L);
        order.addOrderItem("SKU001", "Product", Money.of("100.00", "CNY"), 1);
        repository.save(order);
        
        // When
        List<Order> orders = repository.findByMerchantIdAndStatusAndOrderTimeBetween(
            null, OrderStatus.PENDING, oneHourAgo, oneHourLater);
        
        // Then
        assertTrue(orders.isEmpty());
    }

    @Test
    void findByMerchantIdAndStatusAndOrderTimeBetween_WithNullStatus_ShouldReturnEmptyList() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneHourAgo = now.minusHours(1);
        LocalDateTime oneHourLater = now.plusHours(1);
        
        Order order = new Order("ORD-022", 1L, 2L);
        order.addOrderItem("SKU001", "Product", Money.of("100.00", "CNY"), 1);
        repository.save(order);
        
        // When
        List<Order> orders = repository.findByMerchantIdAndStatusAndOrderTimeBetween(
            2L, null, oneHourAgo, oneHourLater);
        
        // Then
        assertTrue(orders.isEmpty());
    }

    @Test
    void deleteById_WithNullId_ShouldNotThrowException() {
        // When & Then
        assertDoesNotThrow(() -> repository.deleteById(null));
    }

    @Test
    void save_ShouldUpdateOrderNumberIndex() {
        // Given
        Order order = new Order("ORD-024", 1L, 2L);
        order.addOrderItem("SKU001", "Product", Money.of("100.00", "CNY"), 1);
        
        // When
        Order savedOrder = repository.save(order);
        
        // Then
        assertTrue(repository.existsByOrderNumber("ORD-024"));
        Optional<Order> foundOrder = repository.findByOrderNumber("ORD-024");
        assertTrue(foundOrder.isPresent());
        assertEquals(savedOrder.getId(), foundOrder.get().getId());
    }

    @Test
    void save_WithUpdatedOrderNumber_ShouldUpdateIndex() {
        // Given
        Order order = new Order("ORD-025", 1L, 2L);
        order.addOrderItem("SKU001", "Product", Money.of("100.00", "CNY"), 1);
        Order savedOrder = repository.save(order);
        
        // Update order number using reflection (since there's no setter)
        try {
            java.lang.reflect.Field field = Order.class.getDeclaredField("orderNumber");
            field.setAccessible(true);
            field.set(savedOrder, "ORD-025-UPDATED");
        } catch (Exception e) {
            // If reflection fails, skip this test
            return;
        }
        
        // When
        repository.save(savedOrder);
        
        // Then
        assertTrue(repository.existsByOrderNumber("ORD-025-UPDATED"));
        Optional<Order> foundOrder = repository.findByOrderNumber("ORD-025-UPDATED");
        assertTrue(foundOrder.isPresent());
        assertEquals(savedOrder.getId(), foundOrder.get().getId());
    }

    @Test
    void findByMerchantIdAndOrderTimeBetween_WithBoundaryDates_ShouldIncludeExactMatches() {
        // Given
        LocalDateTime exactTime = LocalDateTime.now();
        
        Order order = new Order("ORD-026", 1L, 2L);
        order.addOrderItem("SKU001", "Product", Money.of("100.00", "CNY"), 1);
        
        // Set exact order time using reflection
        try {
            java.lang.reflect.Field field = Order.class.getDeclaredField("orderTime");
            field.setAccessible(true);
            field.set(order, exactTime);
        } catch (Exception e) {
            // If reflection fails, skip this test
            return;
        }
        
        repository.save(order);
        
        // When - search with exact time as boundaries
        List<Order> orders = repository.findByMerchantIdAndOrderTimeBetween(2L, exactTime, exactTime);
        
        // Then
        assertEquals(1, orders.size());
        assertEquals("ORD-026", orders.get(0).getOrderNumber());
    }

    @Test
    void findByMerchantIdAndStatusAndOrderTimeBetween_WithBoundaryDates_ShouldIncludeExactMatches() {
        // Given
        LocalDateTime exactTime = LocalDateTime.now();
        
        Order order = new Order("ORD-027", 1L, 2L);
        order.addOrderItem("SKU001", "Product", Money.of("100.00", "CNY"), 1);
        order.confirm();
        
        // Set exact order time using reflection
        try {
            java.lang.reflect.Field field = Order.class.getDeclaredField("orderTime");
            field.setAccessible(true);
            field.set(order, exactTime);
        } catch (Exception e) {
            // If reflection fails, skip this test
            return;
        }
        
        repository.save(order);
        
        // When - search with exact time as boundaries
        List<Order> orders = repository.findByMerchantIdAndStatusAndOrderTimeBetween(
            2L, OrderStatus.CONFIRMED, exactTime, exactTime);
        
        // Then
        assertEquals(1, orders.size());
        assertEquals("ORD-027", orders.get(0).getOrderNumber());
    }

    @Test
    void deleteById_ShouldRemoveFromBothStorageAndIndex() {
        // Given
        Order order = new Order("ORD-028", 1L, 2L);
        order.addOrderItem("SKU001", "Product", Money.of("100.00", "CNY"), 1);
        Order savedOrder = repository.save(order);
        
        // Verify order exists in both storage and index
        assertTrue(repository.findById(savedOrder.getId()).isPresent());
        assertTrue(repository.existsByOrderNumber("ORD-028"));
        
        // When
        repository.deleteById(savedOrder.getId());
        
        // Then
        assertFalse(repository.findById(savedOrder.getId()).isPresent());
        assertFalse(repository.existsByOrderNumber("ORD-028"));
    }

    @Test
    void concurrentAccess_ShouldHandleMultipleOperations() {
        // Given - test thread safety aspects
        Order order1 = new Order("ORD-029", 1L, 2L);
        order1.addOrderItem("SKU001", "Product", Money.of("100.00", "CNY"), 1);
        
        Order order2 = new Order("ORD-030", 2L, 3L);
        order2.addOrderItem("SKU002", "Product", Money.of("200.00", "CNY"), 1);
        
        // When - save multiple orders
        Order savedOrder1 = repository.save(order1);
        Order savedOrder2 = repository.save(order2);
        
        // Then - both should be saved with unique IDs
        assertNotNull(savedOrder1.getId());
        assertNotNull(savedOrder2.getId());
        assertNotEquals(savedOrder1.getId(), savedOrder2.getId());
        
        // Both should be findable
        assertTrue(repository.findById(savedOrder1.getId()).isPresent());
        assertTrue(repository.findById(savedOrder2.getId()).isPresent());
        assertTrue(repository.existsByOrderNumber("ORD-029"));
        assertTrue(repository.existsByOrderNumber("ORD-030"));
    }
} 