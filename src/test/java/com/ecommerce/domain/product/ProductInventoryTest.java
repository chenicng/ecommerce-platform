package com.ecommerce.domain.product;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ProductInventoryTest {

    @Test
    void shouldCreateInventory() {
        ProductInventory inventory = new ProductInventory(100);
        assertEquals(100, inventory.getQuantity());
    }

    @Test
    void shouldThrowExceptionForNegativeQuantity() {
        assertThrows(IllegalArgumentException.class, () -> new ProductInventory(-1));
    }

    @Test
    void shouldAddInventory() {
        ProductInventory inventory = new ProductInventory(50);
        ProductInventory newInventory = inventory.add(30);
        
        assertEquals(50, inventory.getQuantity()); // Original unchanged
        assertEquals(80, newInventory.getQuantity()); // New instance
    }

    @Test
    void shouldThrowExceptionWhenAddingNonPositiveQuantity() {
        ProductInventory inventory = new ProductInventory(50);
        
        assertThrows(IllegalArgumentException.class, () -> inventory.add(0));
        assertThrows(IllegalArgumentException.class, () -> inventory.add(-5));
    }

    @Test
    void shouldReduceInventory() {
        ProductInventory inventory = new ProductInventory(100);
        ProductInventory newInventory = inventory.reduce(30);
        
        assertEquals(100, inventory.getQuantity()); // Original unchanged
        assertEquals(70, newInventory.getQuantity()); // New instance
    }

    @Test
    void shouldThrowExceptionWhenReducingMoreThanAvailable() {
        ProductInventory inventory = new ProductInventory(50);
        assertThrows(InsufficientInventoryException.class, () -> inventory.reduce(60));
    }

    @Test
    void shouldThrowExceptionWhenReducingNonPositiveQuantity() {
        ProductInventory inventory = new ProductInventory(50);
        
        assertThrows(IllegalArgumentException.class, () -> inventory.reduce(0));
        assertThrows(IllegalArgumentException.class, () -> inventory.reduce(-5));
    }

    @Test
    void shouldCheckIfHasEnoughInventory() {
        ProductInventory inventory = new ProductInventory(100);
        
        assertTrue(inventory.hasEnoughInventory(50));
        assertTrue(inventory.hasEnoughInventory(100));
        assertFalse(inventory.hasEnoughInventory(101));
    }

    @Test
    void shouldCheckIfHasInventory() {
        ProductInventory inventory = new ProductInventory(10);
        assertTrue(inventory.hasInventory());
        
        ProductInventory emptyInventory = new ProductInventory(0);
        assertFalse(emptyInventory.hasInventory());
    }

    @Test
    void shouldCheckIfEmpty() {
        ProductInventory inventory = new ProductInventory(0);
        assertTrue(inventory.isEmpty());
        
        ProductInventory nonEmptyInventory = new ProductInventory(10);
        assertFalse(nonEmptyInventory.isEmpty());
    }

    @Test
    void shouldTestEquality() {
        ProductInventory inventory1 = new ProductInventory(100);
        ProductInventory inventory2 = new ProductInventory(100);
        ProductInventory inventory3 = new ProductInventory(50);
        
        assertEquals(inventory1, inventory2);
        assertNotEquals(inventory1, inventory3);
        assertNotEquals(inventory1, null);
        assertNotEquals(inventory1, "not an inventory");
    }

    @Test
    void shouldTestHashCode() {
        ProductInventory inventory1 = new ProductInventory(100);
        ProductInventory inventory2 = new ProductInventory(100);
        
        assertEquals(inventory1.hashCode(), inventory2.hashCode());
    }

    @Test
    void shouldTestToString() {
        ProductInventory inventory = new ProductInventory(100);
        String result = inventory.toString();
        
        assertTrue(result.contains("ProductInventory"));
        assertTrue(result.contains("100"));
    }
}