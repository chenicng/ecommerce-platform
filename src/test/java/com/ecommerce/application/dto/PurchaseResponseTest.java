package com.ecommerce.application.dto;

import com.ecommerce.domain.Money;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PurchaseResponseTest {

    @Test
    void testDefaultConstructor() {
        PurchaseResponse response = new PurchaseResponse();
        
        assertNull(response.getOrderNumber());
        assertNull(response.getUserId());
        assertNull(response.getMerchantId());
        assertNull(response.getSku());
        assertNull(response.getProductName());
        assertEquals(0, response.getQuantity());
        assertNull(response.getTotalAmount());
    }

    @Test
    void testParameterizedConstructor() {
        String orderNumber = "ORD123";
        Long userId = 1L;
        Long merchantId = 100L;
        String sku = "SKU123";
        String productName = "Test Product";
        int quantity = 5;
        Money totalAmount = Money.of("100.00", "CNY");
        
        PurchaseResponse response = new PurchaseResponse(
            orderNumber, userId, merchantId, sku, productName, 
            quantity, totalAmount
        );
        
        assertEquals(orderNumber, response.getOrderNumber());
        assertEquals(userId, response.getUserId());
        assertEquals(merchantId, response.getMerchantId());
        assertEquals(sku, response.getSku());
        assertEquals(productName, response.getProductName());
        assertEquals(quantity, response.getQuantity());
        assertEquals(totalAmount, response.getTotalAmount());
    }

    @Test
    void testOrderNumberGetterAndSetter() {
        PurchaseResponse response = new PurchaseResponse();
        String orderNumber = "ORD456";
        
        response.setOrderNumber(orderNumber);
        
        assertEquals(orderNumber, response.getOrderNumber());
    }

    @Test
    void testOrderNumberSetterWithNull() {
        PurchaseResponse response = new PurchaseResponse();
        
        response.setOrderNumber(null);
        
        assertNull(response.getOrderNumber());
    }

    @Test
    void testUserIdGetterAndSetter() {
        PurchaseResponse response = new PurchaseResponse();
        Long userId = 999L;
        
        response.setUserId(userId);
        
        assertEquals(userId, response.getUserId());
    }

    @Test
    void testUserIdSetterWithNull() {
        PurchaseResponse response = new PurchaseResponse();
        
        response.setUserId(null);
        
        assertNull(response.getUserId());
    }

    @Test
    void testMerchantIdGetterAndSetter() {
        PurchaseResponse response = new PurchaseResponse();
        Long merchantId = 888L;
        
        response.setMerchantId(merchantId);
        
        assertEquals(merchantId, response.getMerchantId());
    }

    @Test
    void testMerchantIdSetterWithNull() {
        PurchaseResponse response = new PurchaseResponse();
        
        response.setMerchantId(null);
        
        assertNull(response.getMerchantId());
    }

    @Test
    void testSkuGetterAndSetter() {
        PurchaseResponse response = new PurchaseResponse();
        String sku = "PRODUCT-789";
        
        response.setSku(sku);
        
        assertEquals(sku, response.getSku());
    }

    @Test
    void testSkuSetterWithNull() {
        PurchaseResponse response = new PurchaseResponse();
        
        response.setSku(null);
        
        assertNull(response.getSku());
    }

    @Test
    void testProductNameGetterAndSetter() {
        PurchaseResponse response = new PurchaseResponse();
        String productName = "Amazing Product";
        
        response.setProductName(productName);
        
        assertEquals(productName, response.getProductName());
    }

    @Test
    void testProductNameSetterWithNull() {
        PurchaseResponse response = new PurchaseResponse();
        
        response.setProductName(null);
        
        assertNull(response.getProductName());
    }

    @Test
    void testQuantityGetterAndSetter() {
        PurchaseResponse response = new PurchaseResponse();
        int quantity = 15;
        
        response.setQuantity(quantity);
        
        assertEquals(quantity, response.getQuantity());
    }

    @Test
    void testQuantitySetterWithZero() {
        PurchaseResponse response = new PurchaseResponse();
        
        response.setQuantity(0);
        
        assertEquals(0, response.getQuantity());
    }

    @Test
    void testQuantitySetterWithNegativeValue() {
        PurchaseResponse response = new PurchaseResponse();
        int negativeQuantity = -3;
        
        response.setQuantity(negativeQuantity);
        
        assertEquals(negativeQuantity, response.getQuantity());
    }

    @Test
    void testTotalAmountGetterAndSetter() {
        PurchaseResponse response = new PurchaseResponse();
        Money totalAmount = Money.of("250.75", "CNY");
        
        response.setTotalAmount(totalAmount);
        
        assertEquals(totalAmount, response.getTotalAmount());
    }

    @Test
    void testTotalAmountSetterWithNull() {
        PurchaseResponse response = new PurchaseResponse();
        
        response.setTotalAmount(null);
        
        assertNull(response.getTotalAmount());
    }

    @Test
    void testToStringWithAllFieldsSet() {
        Money totalAmount = Money.of("100.00", "CNY");
        PurchaseResponse response = new PurchaseResponse(
            "ORD123", 1L, 100L, "SKU123", "Test Product", 
            5, totalAmount
        );
        
        String result = response.toString();
        
        assertTrue(result.contains("PurchaseResponse"));
        assertTrue(result.contains("orderNumber='ORD123'"));
        assertTrue(result.contains("userId=1"));
        assertTrue(result.contains("merchantId=100"));
        assertTrue(result.contains("sku='SKU123'"));
        assertTrue(result.contains("productName='Test Product'"));
        assertTrue(result.contains("quantity=5"));
    }

    @Test
    void testToStringWithNullFields() {
        PurchaseResponse response = new PurchaseResponse();
        
        String result = response.toString();
        
        assertTrue(result.contains("PurchaseResponse"));
        assertTrue(result.contains("orderNumber='null'"));
        assertTrue(result.contains("userId=null"));
        assertTrue(result.contains("merchantId=null"));
        assertTrue(result.contains("sku='null'"));
        assertTrue(result.contains("productName='null'"));
        assertTrue(result.contains("quantity=0"));
        assertTrue(result.contains("totalAmount=null"));
    }

    @Test
    void testToStringWithPartiallyNullFields() {
        Money totalAmount = Money.of("50.00", "CNY");
        PurchaseResponse response = new PurchaseResponse(
            null, 2L, null, "SKU456", null, 
            3, totalAmount
        );
        
        String result = response.toString();
        
        assertTrue(result.contains("PurchaseResponse"));
        assertTrue(result.contains("orderNumber='null'"));
        assertTrue(result.contains("userId=2"));
        assertTrue(result.contains("merchantId=null"));
        assertTrue(result.contains("sku='SKU456'"));
        assertTrue(result.contains("productName='null'"));
        assertTrue(result.contains("quantity=3"));
    }

    @Test
    void testAllSettersChaining() {
        PurchaseResponse response = new PurchaseResponse();
        Money totalAmount = Money.of("300.00", "CNY");
        
        response.setOrderNumber("ORD999");
        response.setUserId(777L);
        response.setMerchantId(555L);
        response.setSku("CHAIN-TEST");
        response.setProductName("Chain Test Product");
        response.setQuantity(7);
        response.setTotalAmount(totalAmount);
        
        assertEquals("ORD999", response.getOrderNumber());
        assertEquals(777L, response.getUserId());
        assertEquals(555L, response.getMerchantId());
        assertEquals("CHAIN-TEST", response.getSku());
        assertEquals("Chain Test Product", response.getProductName());
        assertEquals(7, response.getQuantity());
        assertEquals(totalAmount, response.getTotalAmount());
    }

    @Test
    void testWithDifferentMoneyAmounts() {
        PurchaseResponse response = new PurchaseResponse();
        
        // Test with zero money
        Money zeroAmount = Money.zero("CNY");
        response.setTotalAmount(zeroAmount);
        assertEquals(zeroAmount, response.getTotalAmount());
        
        // Test with large amount
        Money largeAmount = Money.of("9999.99", "CNY");
        response.setTotalAmount(largeAmount);
        assertEquals(largeAmount, response.getTotalAmount());
    }
}