package com.ecommerce.application.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PurchaseRequestTest {

    @Test
    void testDefaultConstructor() {
        PurchaseRequest request = new PurchaseRequest();
        
        assertNull(request.getUserId());
        assertNull(request.getSku());
        assertEquals(0, request.getQuantity());
    }

    @Test
    void testParameterizedConstructor() {
        Long userId = 1L;
        String sku = "SKU123";
        int quantity = 5;
        
        PurchaseRequest request = new PurchaseRequest(userId, sku, quantity);
        
        assertEquals(userId, request.getUserId());
        assertEquals(sku, request.getSku());
        assertEquals(quantity, request.getQuantity());
    }

    @Test
    void testUserIdGetterAndSetter() {
        PurchaseRequest request = new PurchaseRequest();
        Long userId = 100L;
        
        request.setUserId(userId);
        
        assertEquals(userId, request.getUserId());
    }

    @Test
    void testUserIdSetterWithNull() {
        PurchaseRequest request = new PurchaseRequest();
        
        request.setUserId(null);
        
        assertNull(request.getUserId());
    }

    @Test
    void testSkuGetterAndSetter() {
        PurchaseRequest request = new PurchaseRequest();
        String sku = "PRODUCT-001";
        
        request.setSku(sku);
        
        assertEquals(sku, request.getSku());
    }

    @Test
    void testSkuSetterWithNull() {
        PurchaseRequest request = new PurchaseRequest();
        
        request.setSku(null);
        
        assertNull(request.getSku());
    }

    @Test
    void testSkuSetterWithEmptyString() {
        PurchaseRequest request = new PurchaseRequest();
        String emptySku = "";
        
        request.setSku(emptySku);
        
        assertEquals(emptySku, request.getSku());
    }

    @Test
    void testQuantityGetterAndSetter() {
        PurchaseRequest request = new PurchaseRequest();
        int quantity = 10;
        
        request.setQuantity(quantity);
        
        assertEquals(quantity, request.getQuantity());
    }

    @Test
    void testQuantitySetterWithZero() {
        PurchaseRequest request = new PurchaseRequest();
        
        request.setQuantity(0);
        
        assertEquals(0, request.getQuantity());
    }

    @Test
    void testQuantitySetterWithNegativeValue() {
        PurchaseRequest request = new PurchaseRequest();
        int negativeQuantity = -5;
        
        request.setQuantity(negativeQuantity);
        
        assertEquals(negativeQuantity, request.getQuantity());
    }

    @Test
    void testToStringWithAllFieldsSet() {
        Long userId = 1L;
        String sku = "SKU123";
        int quantity = 5;
        PurchaseRequest request = new PurchaseRequest(userId, sku, quantity);
        
        String result = request.toString();
        
        assertTrue(result.contains("PurchaseRequest"));
        assertTrue(result.contains("userId=1"));
        assertTrue(result.contains("sku='SKU123'"));
        assertTrue(result.contains("quantity=5"));
    }

    @Test
    void testToStringWithNullUserId() {
        PurchaseRequest request = new PurchaseRequest(null, "SKU123", 5);
        
        String result = request.toString();
        
        assertTrue(result.contains("PurchaseRequest"));
        assertTrue(result.contains("userId=null"));
        assertTrue(result.contains("sku='SKU123'"));
        assertTrue(result.contains("quantity=5"));
    }

    @Test
    void testToStringWithNullSku() {
        PurchaseRequest request = new PurchaseRequest(1L, null, 5);
        
        String result = request.toString();
        
        assertTrue(result.contains("PurchaseRequest"));
        assertTrue(result.contains("userId=1"));
        assertTrue(result.contains("sku='null'"));
        assertTrue(result.contains("quantity=5"));
    }

    @Test
    void testToStringWithDefaultConstructor() {
        PurchaseRequest request = new PurchaseRequest();
        
        String result = request.toString();
        
        assertTrue(result.contains("PurchaseRequest"));
        assertTrue(result.contains("userId=null"));
        assertTrue(result.contains("sku='null'"));
        assertTrue(result.contains("quantity=0"));
    }

    @Test
    void testAllSettersChaining() {
        PurchaseRequest request = new PurchaseRequest();
        
        request.setUserId(99L);
        request.setSku("CHAIN-TEST");
        request.setQuantity(15);
        
        assertEquals(99L, request.getUserId());
        assertEquals("CHAIN-TEST", request.getSku());
        assertEquals(15, request.getQuantity());
    }
}