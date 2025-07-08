package com.ecommerce.domain.order;

import com.ecommerce.domain.Money;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class OrderItemTest {

    @Test
    void shouldCreateOrderItem() {
        Money unitPrice = Money.of("25.99", "USD");
        OrderItem item = new OrderItem("SKU001", "Product Name", unitPrice, 3);
        
        assertEquals("SKU001", item.getSku());
        assertEquals("Product Name", item.getProductName());
        assertEquals(unitPrice, item.getUnitPrice());
        assertEquals(3, item.getQuantity());
        assertEquals(Money.of("77.97", "USD"), item.getTotalPrice()); // 25.99 * 3
    }

    @Test
    void shouldThrowExceptionForNullOrEmptySku() {
        Money unitPrice = Money.of("10.00", "USD");
        
        assertThrows(IllegalArgumentException.class, 
            () -> new OrderItem(null, "Product", unitPrice, 1));
        assertThrows(IllegalArgumentException.class, 
            () -> new OrderItem("", "Product", unitPrice, 1));
        assertThrows(IllegalArgumentException.class, 
            () -> new OrderItem("   ", "Product", unitPrice, 1));
    }

    @Test
    void shouldThrowExceptionForNullOrEmptyProductName() {
        Money unitPrice = Money.of("10.00", "USD");
        
        assertThrows(IllegalArgumentException.class, 
            () -> new OrderItem("SKU001", null, unitPrice, 1));
        assertThrows(IllegalArgumentException.class, 
            () -> new OrderItem("SKU001", "", unitPrice, 1));
        assertThrows(IllegalArgumentException.class, 
            () -> new OrderItem("SKU001", "   ", unitPrice, 1));
    }

    @Test
    void shouldThrowExceptionForNullOrZeroUnitPrice() {
        assertThrows(IllegalArgumentException.class, 
            () -> new OrderItem("SKU001", "Product", null, 1));
        assertThrows(IllegalArgumentException.class, 
            () -> new OrderItem("SKU001", "Product", Money.zero("USD"), 1));
    }

    @Test
    void shouldThrowExceptionForNonPositiveQuantity() {
        Money unitPrice = Money.of("10.00", "USD");
        
        assertThrows(IllegalArgumentException.class, 
            () -> new OrderItem("SKU001", "Product", unitPrice, 0));
        assertThrows(IllegalArgumentException.class, 
            () -> new OrderItem("SKU001", "Product", unitPrice, -1));
    }

    @Test
    void shouldCalculateCorrectTotalPrice() {
        Money unitPrice = Money.of("12.50", "USD");
        OrderItem item = new OrderItem("SKU002", "Test Product", unitPrice, 4);
        
        assertEquals(Money.of("50.00", "USD"), item.getTotalPrice()); // 12.50 * 4
    }

    @Test
    void shouldTestEquality() {
        Money unitPrice = Money.of("15.00", "USD");
        OrderItem item1 = new OrderItem("SKU003", "Product", unitPrice, 2);
        OrderItem item2 = new OrderItem("SKU003", "Product", unitPrice, 2);
        OrderItem item3 = new OrderItem("SKU004", "Product", unitPrice, 2);
        OrderItem item4 = new OrderItem("SKU003", "Different Product", unitPrice, 2);
        OrderItem item5 = new OrderItem("SKU003", "Product", Money.of("20.00", "USD"), 2);
        OrderItem item6 = new OrderItem("SKU003", "Product", unitPrice, 3);
        
        assertEquals(item1, item2);
        assertNotEquals(item1, item3); // Different SKU
        assertNotEquals(item1, item4); // Different product name
        assertNotEquals(item1, item5); // Different unit price
        assertNotEquals(item1, item6); // Different quantity
        assertNotEquals(item1, null);
        assertNotEquals(item1, "not an order item");
    }

    @Test
    void shouldTestHashCode() {
        Money unitPrice = Money.of("15.00", "USD");
        OrderItem item1 = new OrderItem("SKU003", "Product", unitPrice, 2);
        OrderItem item2 = new OrderItem("SKU003", "Product", unitPrice, 2);
        
        assertEquals(item1.hashCode(), item2.hashCode());
    }

    @Test
    void shouldTestToString() {
        Money unitPrice = Money.of("10.99", "USD");
        OrderItem item = new OrderItem("SKU005", "Test Product", unitPrice, 1);
        String result = item.toString();
        
        assertTrue(result.contains("OrderItem"));
        assertTrue(result.contains("SKU005"));
        assertTrue(result.contains("Test Product"));
        assertTrue(result.contains("10.99"));
        assertTrue(result.contains("quantity=1"));
    }
}