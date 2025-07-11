package com.ecommerce.domain.product;

import com.ecommerce.domain.Money;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ProductTest {

    @Test
    void shouldCreateProduct() {
        Money price = Money.of("99.99", "USD");
        Product product = new Product("SKU001", "Test Product", "Description", price, 1L, 100);
        
        assertEquals("SKU001", product.getSku());
        assertEquals("Test Product", product.getName());
        assertEquals("Description", product.getDescription());
        assertEquals(price, product.getPrice());
        assertEquals(1L, product.getMerchantId());
        assertEquals(100, product.getAvailableInventory());
        assertEquals(ProductStatus.ACTIVE, product.getStatus());
        assertTrue(product.isActive());
    }

    @Test
    void shouldAddInventory() {
        Money price = Money.of("50.00", "USD");
        Product product = new Product("SKU002", "Product", "Desc", price, 1L, 50);
        
        product.addInventory(30);
        
        assertEquals(80, product.getAvailableInventory());
    }

    @Test
    void shouldThrowExceptionWhenAddingNonPositiveInventory() {
        Money price = Money.of("50.00", "USD");
        Product product = new Product("SKU003", "Product", "Desc", price, 1L, 50);
        
        assertThrows(IllegalArgumentException.class, () -> product.addInventory(0));
        assertThrows(IllegalArgumentException.class, () -> product.addInventory(-5));
    }

    @Test
    void shouldReduceInventory() {
        Money price = Money.of("50.00", "USD");
        Product product = new Product("SKU004", "Product", "Desc", price, 1L, 100);
        
        product.reduceInventory(30);
        
        assertEquals(70, product.getAvailableInventory());
    }

    @Test
    void shouldThrowExceptionWhenReducingMoreThanAvailable() {
        Money price = Money.of("50.00", "USD");
        Product product = new Product("SKU005", "Product", "Desc", price, 1L, 50);
        
        assertThrows(InsufficientInventoryException.class, () -> product.reduceInventory(60));
    }

    @Test
    void shouldThrowExceptionWhenReducingNonPositiveInventory() {
        Money price = Money.of("50.00", "USD");
        Product product = new Product("SKU006", "Product", "Desc", price, 1L, 50);
        
        assertThrows(IllegalArgumentException.class, () -> product.reduceInventory(0));
        assertThrows(IllegalArgumentException.class, () -> product.reduceInventory(-5));
    }

    @Test
    void shouldCheckIfHasEnoughInventory() {
        Money price = Money.of("50.00", "USD");
        Product product = new Product("SKU007", "Product", "Desc", price, 1L, 100);
        
        assertTrue(product.hasEnoughInventory(50));
        assertTrue(product.hasEnoughInventory(100));
        assertFalse(product.hasEnoughInventory(101));
    }

    @Test
    void shouldCalculateTotalPrice() {
        Money price = Money.of("25.99", "USD");
        Product product = new Product("SKU008", "Product", "Desc", price, 1L, 100);
        
        Money totalPrice = product.calculateTotalPrice(3);
        assertEquals(Money.of("77.97", "USD"), totalPrice);
    }

    @Test
    void shouldThrowExceptionWhenCalculatingPriceWithNonPositiveQuantity() {
        Money price = Money.of("25.99", "USD");
        Product product = new Product("SKU009", "Product", "Desc", price, 1L, 100);
        
        assertThrows(IllegalArgumentException.class, () -> product.calculateTotalPrice(0));
        assertThrows(IllegalArgumentException.class, () -> product.calculateTotalPrice(-1));
    }

    @Test
    void shouldUpdatePrice() {
        Money originalPrice = Money.of("50.00", "USD");
        Money newPrice = Money.of("75.00", "USD");
        Product product = new Product("SKU010", "Product", "Desc", originalPrice, 1L, 100);
        
        product.updatePrice(newPrice);
        
        assertEquals(newPrice, product.getPrice());
    }

    @Test
    void shouldThrowExceptionWhenUpdatingToNullOrZeroPrice() {
        Money price = Money.of("50.00", "USD");
        Product product = new Product("SKU011", "Product", "Desc", price, 1L, 100);
        
        assertThrows(IllegalArgumentException.class, () -> product.updatePrice(null));
        assertThrows(IllegalArgumentException.class, () -> product.updatePrice(Money.zero("USD")));
    }

    @Test
    void shouldActivateProduct() {
        Money price = Money.of("50.00", "USD");
        Product product = new Product("SKU012", "Product", "Desc", price, 1L, 100);
        
        product.deactivate();
        assertFalse(product.isActive());
        
        product.activate();
        assertTrue(product.isActive());
        assertEquals(ProductStatus.ACTIVE, product.getStatus());
    }

    @Test
    void shouldDeactivateProduct() {
        Money price = Money.of("50.00", "USD");
        Product product = new Product("SKU013", "Product", "Desc", price, 1L, 100);
        
        product.deactivate();
        
        assertFalse(product.isActive());
        assertEquals(ProductStatus.INACTIVE, product.getStatus());
    }

    @Test
    void shouldCheckIfProductIsAvailable() {
        Money price = Money.of("50.00", "USD");
        Product product = new Product("SKU014", "Product", "Desc", price, 1L, 10);
        
        assertTrue(product.isAvailable()); // Active and has inventory
        
        product.deactivate();
        assertFalse(product.isAvailable()); // Inactive
        
        product.activate();
        product.reduceInventory(10); // Remove all inventory
        assertFalse(product.isAvailable()); // No inventory
    }

    @Test
    void shouldThrowExceptionWhenOperatingOnInactiveProduct() {
        Money price = Money.of("50.00", "USD");
        Product product = new Product("SKU015", "Product", "Desc", price, 1L, 100);
        
        product.deactivate();
        
        assertThrows(com.ecommerce.domain.ResourceInactiveException.class, () -> product.addInventory(10));
        assertThrows(com.ecommerce.domain.ResourceInactiveException.class, () -> product.reduceInventory(10));
        assertThrows(com.ecommerce.domain.ResourceInactiveException.class, () -> product.updatePrice(Money.of("60.00", "USD")));
    }

    @Test
    void shouldGetCorrectProductDetails() {
        Money price = Money.of("29.99", "USD");
        Product product = new Product("SKU016", "Test Product", "Test Description", price, 5L, 75);
        
        assertEquals("SKU016", product.getSku());
        assertEquals("Test Product", product.getName());
        assertEquals("Test Description", product.getDescription());
        assertEquals(price, product.getPrice());
        assertEquals(5L, product.getMerchantId());
        assertEquals(75, product.getAvailableInventory());
        assertNotNull(product.getInventory());
        assertEquals(ProductStatus.ACTIVE, product.getStatus());
    }

    @Test
    void shouldCreateProductWithDefaultConstructor() {
        Product product = new Product();
        
        assertNotNull(product);
        assertNull(product.getSku());
        assertNull(product.getName());
        assertNull(product.getDescription());
        assertNull(product.getPrice());
        assertNull(product.getMerchantId());
        assertNull(product.getInventory());
        assertNull(product.getStatus());
    }

    @Test
    void shouldSetSkuUsingPackagePrivateMethod() {
        Product product = new Product();
        
        product.setSku("TEST-SKU");
        
        assertEquals("TEST-SKU", product.getSku());
    }

    @Test
    void shouldSetNameUsingPackagePrivateMethod() {
        Product product = new Product();
        
        product.setName("Test Product Name");
        
        assertEquals("Test Product Name", product.getName());
    }

    @Test
    void shouldSetDescriptionUsingPackagePrivateMethod() {
        Product product = new Product();
        
        product.setDescription("Test Description");
        
        assertEquals("Test Description", product.getDescription());
    }

    @Test
    void shouldSetPriceUsingPackagePrivateMethod() {
        Product product = new Product();
        Money price = Money.of("99.99", "USD");
        
        product.setPrice(price);
        
        assertEquals(price, product.getPrice());
    }

    @Test
    void shouldSetMerchantIdUsingPackagePrivateMethod() {
        Product product = new Product();
        
        product.setMerchantId(123L);
        
        assertEquals(123L, product.getMerchantId());
    }

    @Test
    void shouldSetInventoryUsingPackagePrivateMethod() {
        Product product = new Product();
        ProductInventory inventory = new ProductInventory(50);
        
        product.setInventory(inventory);
        
        assertEquals(inventory, product.getInventory());
        assertEquals(50, product.getAvailableInventory());
    }

    @Test
    void shouldSetStatusUsingPackagePrivateMethod() {
        Product product = new Product();
        
        product.setStatus(ProductStatus.INACTIVE);
        
        assertEquals(ProductStatus.INACTIVE, product.getStatus());
        assertFalse(product.isActive());
    }

    @Test
    void shouldVerifyInheritanceFromBaseEntity() {
        Product product = new Product();
        
        assertTrue(product instanceof com.ecommerce.domain.BaseEntity);
    }
}