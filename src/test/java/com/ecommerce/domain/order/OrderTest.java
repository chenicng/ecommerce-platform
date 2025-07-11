package com.ecommerce.domain.order;

import com.ecommerce.domain.Money;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

class OrderTest {

    @Test
    void shouldCreateOrder() {
        Order order = new Order("ORD-001", 1L, 2L);
        
        assertEquals("ORD-001", order.getOrderNumber());
        assertEquals(1L, order.getUserId());
        assertEquals(2L, order.getMerchantId());
        assertEquals(OrderStatus.PENDING, order.getStatus());
        assertEquals(Money.zero("CNY"), order.getTotalAmount());
        assertTrue(order.getItems().isEmpty());
        assertNotNull(order.getOrderTime());
        assertNull(order.getCompletedTime());
    }

    @Test
    void shouldAddOrderItem() {
        Order order = new Order("ORD-002", 1L, 2L);
        Money unitPrice = Money.of("25.00", "CNY");
        
        order.addOrderItem("SKU001", "Product 1", unitPrice, 2);
        
        assertEquals(1, order.getItems().size());
        assertEquals(Money.of("50.00", "CNY"), order.getTotalAmount()); // 25 * 2
        assertEquals(2, order.getTotalQuantity());
        
        OrderItem item = order.getItems().get(0);
        assertEquals("SKU001", item.getSku());
        assertEquals("Product 1", item.getProductName());
        assertEquals(unitPrice, item.getUnitPrice());
        assertEquals(2, item.getQuantity());
    }

    @Test
    void shouldAddMultipleOrderItems() {
        Order order = new Order("ORD-003", 1L, 2L);
        
        order.addOrderItem("SKU001", "Product 1", Money.of("10.00", "CNY"), 2);
        order.addOrderItem("SKU002", "Product 2", Money.of("15.00", "CNY"), 1);
        
        assertEquals(2, order.getItems().size());
        assertEquals(Money.of("35.00", "CNY"), order.getTotalAmount()); // 20 + 15
        assertEquals(3, order.getTotalQuantity()); // 2 + 1
    }

    @Test
    void shouldThrowExceptionWhenAddingItemToNonPendingOrder() {
        Order order = new Order("ORD-004", 1L, 2L);
        order.addOrderItem("SKU001", "Product", Money.of("10.00", "CNY"), 1);
        order.confirm();
        
        assertThrows(com.ecommerce.domain.order.InvalidOrderStateException.class, 
            () -> order.addOrderItem("SKU002", "Product 2", Money.of("20.00", "CNY"), 1));
    }

    @Test
    void shouldConfirmOrder() {
        Order order = new Order("ORD-005", 1L, 2L);
        order.addOrderItem("SKU001", "Product", Money.of("10.00", "CNY"), 1);
        
        order.confirm();
        
        assertEquals(OrderStatus.CONFIRMED, order.getStatus());
    }

    @Test
    void shouldThrowExceptionWhenConfirmingEmptyOrder() {
        Order order = new Order("ORD-006", 1L, 2L);
        
        assertThrows(com.ecommerce.domain.order.InvalidOrderStateException.class, order::confirm);
    }

    @Test
    void shouldThrowExceptionWhenConfirmingNonPendingOrder() {
        Order order = new Order("ORD-007", 1L, 2L);
        order.addOrderItem("SKU001", "Product", Money.of("10.00", "CNY"), 1);
        order.confirm();
        
        assertThrows(com.ecommerce.domain.order.InvalidOrderStateException.class, order::confirm);
    }

    @Test
    void shouldProcessPayment() {
        Order order = new Order("ORD-008", 1L, 2L);
        order.addOrderItem("SKU001", "Product", Money.of("10.00", "CNY"), 1);
        order.confirm();
        
        order.processPayment();
        
        assertEquals(OrderStatus.PAID, order.getStatus());
    }

    @Test
    void shouldThrowExceptionWhenProcessingPaymentOnNonConfirmedOrder() {
        Order order = new Order("ORD-009", 1L, 2L);
        order.addOrderItem("SKU001", "Product", Money.of("10.00", "CNY"), 1);
        
        assertThrows(com.ecommerce.domain.order.InvalidOrderStateException.class, order::processPayment);
    }

    @Test
    void shouldCompleteOrder() {
        Order order = new Order("ORD-010", 1L, 2L);
        order.addOrderItem("SKU001", "Product", Money.of("10.00", "CNY"), 1);
        order.confirm();
        order.processPayment();
        
        order.complete();
        
        assertEquals(OrderStatus.COMPLETED, order.getStatus());
        assertNotNull(order.getCompletedTime());
        assertTrue(order.isCompleted());
    }

    @Test
    void shouldThrowExceptionWhenCompletingNonPaidOrder() {
        Order order = new Order("ORD-011", 1L, 2L);
        order.addOrderItem("SKU001", "Product", Money.of("10.00", "CNY"), 1);
        order.confirm();
        
        assertThrows(com.ecommerce.domain.order.InvalidOrderStateException.class, order::complete);
    }

    @Test
    void shouldCancelPendingOrder() {
        Order order = new Order("ORD-012", 1L, 2L);
        order.addOrderItem("SKU001", "Product", Money.of("10.00", "CNY"), 1);
        
        order.cancel("Customer request");
        
        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        assertTrue(order.isCancelled());
    }

    @Test
    void shouldCancelConfirmedOrder() {
        Order order = new Order("ORD-013", 1L, 2L);
        order.addOrderItem("SKU001", "Product", Money.of("10.00", "CNY"), 1);
        order.confirm();
        
        order.cancel("Inventory issue");
        
        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        assertTrue(order.isCancelled());
    }

    @Test
    void shouldCancelPaidOrder() {
        Order order = new Order("ORD-014", 1L, 2L);
        order.addOrderItem("SKU001", "Product", Money.of("10.00", "CNY"), 1);
        order.confirm();
        order.processPayment();
        
        order.cancel("System error");
        
        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        assertTrue(order.isCancelled());
    }

    @Test
    void shouldThrowExceptionWhenCancellingCompletedOrder() {
        Order order = new Order("ORD-015", 1L, 2L);
        order.addOrderItem("SKU001", "Product", Money.of("10.00", "CNY"), 1);
        order.confirm();
        order.processPayment();
        order.complete();
        
        assertThrows(com.ecommerce.domain.order.InvalidOrderStateException.class, () -> order.cancel("Too late"));
    }

    @Test
    void shouldDetermineRefundNecessity() {
        Order pendingOrder = new Order("ORD-016", 1L, 2L);
        pendingOrder.addOrderItem("SKU001", "Product", Money.of("10.00", "CNY"), 1);
        assertFalse(pendingOrder.needsRefund());
        
        pendingOrder.confirm();
        assertFalse(pendingOrder.needsRefund());
        
        pendingOrder.processPayment();
        assertTrue(pendingOrder.needsRefund());
        
        pendingOrder.complete();
        assertTrue(pendingOrder.needsRefund());
    }

    @Test
    void shouldDetermineInventoryRestoreNecessity() {
        Order pendingOrder = new Order("ORD-017", 1L, 2L);
        pendingOrder.addOrderItem("SKU001", "Product", Money.of("10.00", "CNY"), 1);
        assertFalse(pendingOrder.needsInventoryRestore());
        
        pendingOrder.confirm();
        assertTrue(pendingOrder.needsInventoryRestore());
        
        pendingOrder.processPayment();
        assertTrue(pendingOrder.needsInventoryRestore());
        
        pendingOrder.complete();
        assertTrue(pendingOrder.needsInventoryRestore());
    }

    @Test
    void shouldCheckIfOrderCanBePaid() {
        Order order = new Order("ORD-018", 1L, 2L);
        order.addOrderItem("SKU001", "Product", Money.of("10.00", "CNY"), 1);
        
        assertFalse(order.canBePaid()); // Pending
        
        order.confirm();
        assertTrue(order.canBePaid()); // Confirmed
        
        order.processPayment();
        assertFalse(order.canBePaid()); // Paid
    }

    @Test
    void shouldReturnCopyOfItems() {
        Order order = new Order("ORD-019", 1L, 2L);
        order.addOrderItem("SKU001", "Product", Money.of("10.00", "CNY"), 1);
        
        var items = order.getItems();
        assertEquals(1, items.size());
        
        // Modifying returned list should not affect order
        items.clear();
        assertEquals(1, order.getItems().size()); // Should still have 1 item
    }

    @Test
    void shouldCalculateTotalQuantityCorrectly() {
        Order order = new Order("ORD-020", 1L, 2L);
        
        assertEquals(0, order.getTotalQuantity()); // Empty order
        
        order.addOrderItem("SKU001", "Product 1", Money.of("10.00", "CNY"), 3);
        assertEquals(3, order.getTotalQuantity());
        
        order.addOrderItem("SKU002", "Product 2", Money.of("20.00", "CNY"), 2);
        assertEquals(5, order.getTotalQuantity()); // 3 + 2
    }

    @Test
    void shouldMaintainOrderWorkflow() {
        Order order = new Order("ORD-021", 1L, 2L);
        
        // Start as pending
        assertEquals(OrderStatus.PENDING, order.getStatus());
        
        // Add items
        order.addOrderItem("SKU001", "Product", Money.of("50.00", "CNY"), 1);
        
        // Confirm
        order.confirm();
        assertEquals(OrderStatus.CONFIRMED, order.getStatus());
        
        // Process payment
        order.processPayment();
        assertEquals(OrderStatus.PAID, order.getStatus());
        
        // Complete
        order.complete();
        assertEquals(OrderStatus.COMPLETED, order.getStatus());
        assertTrue(order.isCompleted());
        assertNotNull(order.getCompletedTime());
    }

    @Test
    void shouldCreateOrderWithDefaultConstructor() {
        Order order = new Order();
        
        assertNotNull(order);
        assertNull(order.getOrderNumber());
        assertNull(order.getUserId());
        assertNull(order.getMerchantId());
        assertTrue(order.getItems().isEmpty());
        assertEquals(Money.zero("CNY"), order.getTotalAmount());
        assertNull(order.getStatus());
        assertNull(order.getOrderTime());
        assertNull(order.getCompletedTime());
    }

    @Test
    void shouldSetOrderNumberUsingPackagePrivateMethod() {
        Order order = new Order();
        
        order.setOrderNumber("TEST-ORDER-001");
        
        assertEquals("TEST-ORDER-001", order.getOrderNumber());
    }

    @Test
    void shouldSetUserIdUsingPackagePrivateMethod() {
        Order order = new Order();
        
        order.setUserId(999L);
        
        assertEquals(999L, order.getUserId());
    }

    @Test
    void shouldSetMerchantIdUsingPackagePrivateMethod() {
        Order order = new Order();
        
        order.setMerchantId(888L);
        
        assertEquals(888L, order.getMerchantId());
    }

    @Test
    void shouldSetItemsUsingPackagePrivateMethod() {
        Order order = new Order();
        List<OrderItem> items = new ArrayList<>();
        items.add(new OrderItem("SKU001", "Product", Money.of("10.00", "CNY"), 1));
        
        order.setItems(items);
        
        assertEquals(1, order.getItems().size());
        assertEquals("SKU001", order.getItems().get(0).getSku());
    }

    @Test
    void shouldSetTotalAmountUsingPackagePrivateMethod() {
        Order order = new Order();
        Money totalAmount = Money.of("100.00", "CNY");
        
        order.setTotalAmount(totalAmount);
        
        assertEquals(totalAmount, order.getTotalAmount());
    }

    @Test
    void shouldSetStatusUsingPackagePrivateMethod() {
        Order order = new Order();
        
        order.setStatus(OrderStatus.CONFIRMED);
        
        assertEquals(OrderStatus.CONFIRMED, order.getStatus());
    }

    @Test
    void shouldSetOrderTimeUsingPackagePrivateMethod() {
        Order order = new Order();
        LocalDateTime orderTime = LocalDateTime.of(2023, 1, 1, 10, 0);
        
        order.setOrderTime(orderTime);
        
        assertEquals(orderTime, order.getOrderTime());
    }

    @Test
    void shouldSetCompletedTimeUsingPackagePrivateMethod() {
        Order order = new Order();
        LocalDateTime completedTime = LocalDateTime.of(2023, 1, 1, 11, 0);
        
        order.setCompletedTime(completedTime);
        
        assertEquals(completedTime, order.getCompletedTime());
    }

    @Test
    void shouldVerifyInheritanceFromBaseEntity() {
        Order order = new Order();
        
        assertTrue(order instanceof com.ecommerce.domain.BaseEntity);
    }
}