package com.ecommerce.api.controller;

import com.ecommerce.application.service.EcommerceService;
import com.ecommerce.application.service.ProductService;
import com.ecommerce.application.dto.PurchaseRequest;
import com.ecommerce.application.dto.PurchaseResponse;
import com.ecommerce.api.dto.Result;
import com.ecommerce.domain.product.Product;
import com.ecommerce.domain.Money;
import com.ecommerce.api.config.ApiVersionConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for EcommerceController
 */
@WebMvcTest(EcommerceController.class)
class EcommerceControllerTest {

    private static final String API_BASE_PATH = ApiVersionConfig.API_V1 + "/ecommerce";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EcommerceService ecommerceService;

    @MockBean
    private ProductService productService;

    @Autowired
    private ObjectMapper objectMapper;

    private Product testProduct;
    private PurchaseRequest testPurchaseRequest;
    private PurchaseResponse testPurchaseResponse;

    @BeforeEach
    void setUp() {
        // Setup test product
        testProduct = new Product(
            "IPHONE15",
            "iPhone 15", 
            "Latest iPhone model",
            Money.of("999.99", "CNY"),
            1L,
            100
        );
        testProduct.setId(1L);

        // Setup test purchase request
        testPurchaseRequest = new PurchaseRequest(1L, "IPHONE15", 2);

        // Setup test purchase response
        testPurchaseResponse = new PurchaseResponse(
            "ORD123",
            1L,
            1L,
            "IPHONE15",
            "iPhone 15",
            2,
            Money.of("1999.98", "CNY")
        );
    }

    @Test
    void purchaseProduct_Success() throws Exception {
        // Given
        when(ecommerceService.processPurchase(any(PurchaseRequest.class)))
            .thenReturn(testPurchaseResponse);

        // When & Then
        mockMvc.perform(post(API_BASE_PATH + "/purchase")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testPurchaseRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("Purchase completed successfully"))
                .andExpect(jsonPath("$.data.orderNumber").value("ORD123"))
                .andExpect(jsonPath("$.data.userId").value(1))
                .andExpect(jsonPath("$.data.merchantId").value(1))
                .andExpect(jsonPath("$.data.sku").value("IPHONE15"))
                .andExpect(jsonPath("$.data.productName").value("iPhone 15"))
                .andExpect(jsonPath("$.data.quantity").value(2))
                .andExpect(jsonPath("$.data.totalAmount.amount").value(1999.98))
                .andExpect(jsonPath("$.data.totalAmount.currency").value("CNY"));

        verify(ecommerceService).processPurchase(any(PurchaseRequest.class));
    }

    @Test
    void purchaseProduct_ValidationError_NullUserId() throws Exception {
        // Given
        PurchaseRequest invalidRequest = new PurchaseRequest(null, "IPHONE15", 2);

        // When & Then
        mockMvc.perform(post(API_BASE_PATH + "/purchase")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("User ID is required"));

        verify(ecommerceService, never()).processPurchase(any());
    }

    @Test
    void purchaseProduct_ValidationError_EmptySku() throws Exception {
        // Given
        PurchaseRequest invalidRequest = new PurchaseRequest(1L, "", 2);

        // When & Then
        mockMvc.perform(post(API_BASE_PATH + "/purchase")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("SKU is required"));

        verify(ecommerceService, never()).processPurchase(any());
    }

    @Test
    void purchaseProduct_ValidationError_InvalidQuantity() throws Exception {
        // Given
        PurchaseRequest invalidRequest = new PurchaseRequest(1L, "IPHONE15", 0);

        // When & Then
        mockMvc.perform(post(API_BASE_PATH + "/purchase")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Quantity must be positive"));

        verify(ecommerceService, never()).processPurchase(any());
    }

    @Test
    void purchaseProduct_ServiceError() throws Exception {
        // Given
        when(ecommerceService.processPurchase(any(PurchaseRequest.class)))
            .thenThrow(new RuntimeException("Insufficient inventory"));

        // When & Then
        mockMvc.perform(post(API_BASE_PATH + "/purchase")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testPurchaseRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INSUFFICIENT_INVENTORY"))
                .andExpect(jsonPath("$.message").value("Insufficient inventory"));

        verify(ecommerceService).processPurchase(any(PurchaseRequest.class));
    }

    @Test
    void getProductBySku_Success() throws Exception {
        // Given
        when(productService.getProductBySku("IPHONE15")).thenReturn(testProduct);

        // When & Then
        mockMvc.perform(get(API_BASE_PATH + "/products/{sku}", "IPHONE15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.sku").value("IPHONE15"))
                .andExpect(jsonPath("$.data.name").value("iPhone 15"))
                .andExpect(jsonPath("$.data.description").value("Latest iPhone model"))
                .andExpect(jsonPath("$.data.price").value(999.99))
                .andExpect(jsonPath("$.data.currency").value("CNY"))
                .andExpect(jsonPath("$.data.merchantId").value(1))
                .andExpect(jsonPath("$.data.availableInventory").value(100))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andExpect(jsonPath("$.data.available").value(true));

        verify(productService).getProductBySku("IPHONE15");
    }

    @Test
    void getProductBySku_NotFound() throws Exception {
        // Given
        when(productService.getProductBySku("UNKNOWN")).thenThrow(new RuntimeException("Product not found"));

        // When & Then
        mockMvc.perform(get(API_BASE_PATH + "/products/{sku}", "UNKNOWN"))
                .andExpect(status().isNotFound());

        verify(productService).getProductBySku("UNKNOWN");
    }

    @Test
    void getAllProducts_Success() throws Exception {
        // Given
        Product secondProduct = new Product(
            "SAMSUNG_S24",
            "Samsung Galaxy S24", 
            "Latest Samsung phone",
            Money.of("899.99", "CNY"),
            2L,
            50
        );
        secondProduct.setId(2L);
        List<Product> products = Arrays.asList(testProduct, secondProduct);
        when(productService.getAllAvailableProducts()).thenReturn(products);

        // When & Then
        mockMvc.perform(get(API_BASE_PATH + "/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.products", hasSize(2)))
                .andExpect(jsonPath("$.data.totalCount").value(2))
                .andExpect(jsonPath("$.data.products[0].sku").value("IPHONE15"))
                .andExpect(jsonPath("$.data.products[1].sku").value("SAMSUNG_S24"));

        verify(productService).getAllAvailableProducts();
    }

    @Test
    void getAllProducts_WithSearch() throws Exception {
        // Given
        List<Product> products = Arrays.asList(testProduct);
        when(productService.searchAvailableProducts("iPhone")).thenReturn(products);

        // When & Then
        mockMvc.perform(get(API_BASE_PATH + "/products").param("search", "iPhone"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.products", hasSize(1)))
                .andExpect(jsonPath("$.data.totalCount").value(1))
                .andExpect(jsonPath("$.data.searchTerm").value("iPhone"))
                .andExpect(jsonPath("$.data.products[0].sku").value("IPHONE15"));

        verify(productService).searchAvailableProducts("iPhone");
    }

    @Test
    void getProductInventory_Success() throws Exception {
        // Given
        when(productService.getProductBySku("IPHONE15")).thenReturn(testProduct);

        // When & Then
        mockMvc.perform(get(API_BASE_PATH + "/products/{sku}/inventory", "IPHONE15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.sku").value("IPHONE15"))
                .andExpect(jsonPath("$.data.productName").value("iPhone 15"))
                .andExpect(jsonPath("$.data.availableInventory").value(100))
                .andExpect(jsonPath("$.data.available").value(true))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));

        verify(productService).getProductBySku("IPHONE15");
    }

    @Test
    void getProductInventory_NotFound() throws Exception {
        // Given
        when(productService.getProductBySku("UNKNOWN")).thenThrow(new RuntimeException("Product not found"));

        // When & Then
        mockMvc.perform(get(API_BASE_PATH + "/products/{sku}/inventory", "UNKNOWN"))
                .andExpect(status().isNotFound());

        verify(productService).getProductBySku("UNKNOWN");
    }

    @Test
    void cancelOrder_Success() throws Exception {
        // Given
        String orderNumber = "ORD123";
        String reason = "Customer request";

        // When & Then
        mockMvc.perform(post(API_BASE_PATH + "/orders/{orderNumber}/cancel", orderNumber)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\":\"" + reason + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("Order cancelled successfully"))
                .andExpect(jsonPath("$.data.orderNumber").value(orderNumber))
                .andExpect(jsonPath("$.data.reason").value(reason));

        verify(ecommerceService).cancelOrder(orderNumber, reason);
    }

    @Test
    void cancelOrder_ServiceError() throws Exception {
        // Given
        String orderNumber = "ORD123";
        doThrow(new RuntimeException("Cannot cancel this order"))
            .when(ecommerceService).cancelOrder(anyString(), anyString());

        // When & Then
        mockMvc.perform(post(API_BASE_PATH + "/orders/{orderNumber}/cancel", orderNumber)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\":\"Customer request\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("OPERATION_NOT_ALLOWED"))
                .andExpect(jsonPath("$.message").value("Cannot cancel this order"));

        verify(ecommerceService).cancelOrder(orderNumber, "Customer request");
    }

    @Test
    void getAllProducts_WithMerchantFilter() throws Exception {
        // Given
        List<Product> products = Arrays.asList(testProduct);
        when(productService.getProductsByMerchant(1L)).thenReturn(products);

        // When & Then
        mockMvc.perform(get(API_BASE_PATH + "/products")
                .param("merchantId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.products", hasSize(1)))
                .andExpect(jsonPath("$.data.totalCount").value(1))
                .andExpect(jsonPath("$.data.merchantId").value(1));

        verify(productService).getProductsByMerchant(1L);
    }

    @Test
    void getAllProducts_WithSearchAndMerchantFilter() throws Exception {
        // Given
        List<Product> products = Arrays.asList(testProduct);
        when(productService.getProductsByMerchant(1L)).thenReturn(products);

        // When & Then
        mockMvc.perform(get(API_BASE_PATH + "/products")
                .param("search", "iPhone")
                .param("merchantId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.searchTerm").value("iPhone"))
                .andExpect(jsonPath("$.data.merchantId").value(1));

        verify(productService).getProductsByMerchant(1L);
    }

    @Test
    void getAllProducts_WithSearchOnly() throws Exception {
        // Given
        List<Product> products = Arrays.asList(testProduct);
        when(productService.searchAvailableProducts("iPhone")).thenReturn(products);

        // When & Then
        mockMvc.perform(get(API_BASE_PATH + "/products")
                .param("search", "iPhone"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.searchTerm").value("iPhone"));

        verify(productService).searchAvailableProducts("iPhone");
    }

    @Test
    void getAllProducts_NoFilters() throws Exception {
        // Given
        List<Product> products = Arrays.asList(testProduct);
        when(productService.getAllAvailableProducts()).thenReturn(products);

        // When & Then
        mockMvc.perform(get(API_BASE_PATH + "/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.products", hasSize(1)));

        verify(productService).getAllAvailableProducts();
    }

    // Additional test cases for better coverage

    @Test
    void purchaseProduct_ServiceThrowsInsufficientBalanceException() throws Exception {
        // Given
        when(ecommerceService.processPurchase(any(PurchaseRequest.class)))
            .thenThrow(new RuntimeException("Insufficient balance"));

        // When & Then
        mockMvc.perform(post(API_BASE_PATH + "/purchase")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testPurchaseRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INSUFFICIENT_BALANCE"))
                .andExpect(jsonPath("$.message").value("Insufficient balance"));

        verify(ecommerceService).processPurchase(any(PurchaseRequest.class));
    }

    @Test
    void purchaseProduct_ServiceThrowsProductNotFound() throws Exception {
        // Given
        when(ecommerceService.processPurchase(any(PurchaseRequest.class)))
            .thenThrow(new RuntimeException("Product not found"));

        // When & Then
        mockMvc.perform(post(API_BASE_PATH + "/purchase")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testPurchaseRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Product not found"));

        verify(ecommerceService).processPurchase(any(PurchaseRequest.class));
    }

    @Test
    void purchaseProduct_ServiceThrowsUserNotFound() throws Exception {
        // Given
        when(ecommerceService.processPurchase(any(PurchaseRequest.class)))
            .thenThrow(new RuntimeException("User not found"));

        // When & Then
        mockMvc.perform(post(API_BASE_PATH + "/purchase")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testPurchaseRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("User not found"));

        verify(ecommerceService).processPurchase(any(PurchaseRequest.class));
    }

    @Test
    void getProductBySku_ServiceThrowsException() throws Exception {
        // Given
        when(productService.getProductBySku("INVALID"))
            .thenThrow(new RuntimeException("Product not found"));

        // When & Then
        mockMvc.perform(get(API_BASE_PATH + "/products/{sku}", "INVALID"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Product not found"));

        verify(productService).getProductBySku("INVALID");
    }

    @Test
    void cancelOrder_ValidationError_NullReason() throws Exception {
        // Given
        EcommerceController.CancelOrderRequest request = new EcommerceController.CancelOrderRequest();
        request.setReason(null);

        // When & Then
        mockMvc.perform(post(API_BASE_PATH + "/orders/{orderNumber}/cancel", "ORD123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(ecommerceService, never()).cancelOrder(anyString(), anyString());
    }

    @Test
    void cancelOrder_ValidationError_EmptyReason() throws Exception {
        // Given
        EcommerceController.CancelOrderRequest request = new EcommerceController.CancelOrderRequest();
        request.setReason("");

        // When & Then
        mockMvc.perform(post(API_BASE_PATH + "/orders/{orderNumber}/cancel", "ORD123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(ecommerceService, never()).cancelOrder(anyString(), anyString());
    }

    @Test
    void cancelOrder_ServiceThrowsOrderNotFound() throws Exception {
        // Given
        EcommerceController.CancelOrderRequest request = new EcommerceController.CancelOrderRequest("Test reason");
        doThrow(new RuntimeException("Order not found"))
            .when(ecommerceService).cancelOrder("INVALID", "Test reason");

        // When & Then
        mockMvc.perform(post(API_BASE_PATH + "/orders/{orderNumber}/cancel", "INVALID")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Order not found"));

        verify(ecommerceService).cancelOrder("INVALID", "Test reason");
    }

    @Test
    void cancelOrder_ServiceThrowsOrderCannotBeCancelled() throws Exception {
        // Given
        EcommerceController.CancelOrderRequest request = new EcommerceController.CancelOrderRequest("Test reason");
        doThrow(new RuntimeException("Cannot cancel this order"))
            .when(ecommerceService).cancelOrder("ORD123", "Test reason");

        // When & Then
        mockMvc.perform(post(API_BASE_PATH + "/orders/{orderNumber}/cancel", "ORD123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("OPERATION_NOT_ALLOWED"))
                .andExpect(jsonPath("$.message").value("Cannot cancel this order"));

        verify(ecommerceService).cancelOrder("ORD123", "Test reason");
    }

    @Test
    void getAllProducts_WithSearchAndMerchantFilter_EmptySearchTerm() throws Exception {
        // Given
        List<Product> products = Arrays.asList(testProduct);
        when(productService.getProductsByMerchant(1L)).thenReturn(products);

        // When & Then
        mockMvc.perform(get(API_BASE_PATH + "/products")
                .param("search", "   ") // Whitespace only
                .param("merchantId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.products", hasSize(1)))
                .andExpect(jsonPath("$.data.totalCount").value(1));

        verify(productService).getProductsByMerchant(1L);
    }

    @Test
    void getAllProducts_WithSearchAndMerchantFilter_ValidSearchTerm() throws Exception {
        // Given
        List<Product> products = Arrays.asList(testProduct);
        when(productService.getProductsByMerchant(1L)).thenReturn(products);

        // When & Then
        mockMvc.perform(get(API_BASE_PATH + "/products")
                .param("search", "iPhone")
                .param("merchantId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.products", hasSize(1)))
                .andExpect(jsonPath("$.data.totalCount").value(1))
                .andExpect(jsonPath("$.data.searchTerm").value("iPhone"))
                .andExpect(jsonPath("$.data.merchantId").value(1));

        verify(productService).getProductsByMerchant(1L);
    }

    @Test
    void getAllProducts_WithSearchAndMerchantFilter_CaseInsensitiveSearch() throws Exception {
        // Given
        List<Product> products = Arrays.asList(testProduct);
        when(productService.getProductsByMerchant(1L)).thenReturn(products);

        // When & Then
        mockMvc.perform(get(API_BASE_PATH + "/products")
                .param("search", "iphone") // Lowercase
                .param("merchantId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.products", hasSize(1)))
                .andExpect(jsonPath("$.data.totalCount").value(1));

        verify(productService).getProductsByMerchant(1L);
    }

    @Test
    void getAllProducts_WithSearchAndMerchantFilter_NoMatchingProducts() throws Exception {
        // Given
        when(productService.getProductsByMerchant(1L)).thenReturn(Arrays.asList());

        // When & Then
        mockMvc.perform(get(API_BASE_PATH + "/products")
                .param("search", "NonExistentProduct")
                .param("merchantId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.products", hasSize(0)))
                .andExpect(jsonPath("$.data.totalCount").value(0));

        verify(productService).getProductsByMerchant(1L);
    }

    @Test
    void getProductInventory_ServiceThrowsException() throws Exception {
        // Given
        when(productService.getProductBySku("INVALID"))
            .thenThrow(new RuntimeException("Product not found"));

        // When & Then
        mockMvc.perform(get(API_BASE_PATH + "/products/{sku}/inventory", "INVALID"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Product not found"));

        verify(productService).getProductBySku("INVALID");
    }

    @Test
    void purchaseProduct_ValidationError_NullRequest() throws Exception {
        // When & Then
        mockMvc.perform(post(API_BASE_PATH + "/purchase")
                .contentType(MediaType.APPLICATION_JSON)
                .content("null"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void purchaseProduct_ValidationError_InvalidJson() throws Exception {
        // When & Then
        mockMvc.perform(post(API_BASE_PATH + "/purchase")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cancelOrder_ValidationError_InvalidJson() throws Exception {
        // When & Then
        mockMvc.perform(post(API_BASE_PATH + "/orders/{orderNumber}/cancel", "ORD123")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getProductBySku_ProductWithZeroInventory() throws Exception {
        // Given
        Product productWithZeroInventory = new Product(
            "ZERO-STOCK",
            "Zero Stock Product", 
            "Product with no inventory",
            Money.of("50.00", "CNY"),
            1L,
            0
        );
        productWithZeroInventory.setId(2L);
        when(productService.getProductBySku("ZERO-STOCK")).thenReturn(productWithZeroInventory);

        // When & Then
        mockMvc.perform(get(API_BASE_PATH + "/products/{sku}", "ZERO-STOCK"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.id").value(2))
                .andExpect(jsonPath("$.data.sku").value("ZERO-STOCK"))
                .andExpect(jsonPath("$.data.name").value("Zero Stock Product"))
                .andExpect(jsonPath("$.data.availableInventory").value(0))
                .andExpect(jsonPath("$.data.available").value(false));

        verify(productService).getProductBySku("ZERO-STOCK");
    }

    @Test
    void getProductBySku_ProductWithLargeInventory() throws Exception {
        // Given
        Product productWithLargeInventory = new Product(
            "BULK-PRODUCT",
            "Bulk Product", 
            "Product with large inventory",
            Money.of("10.00", "CNY"),
            1L,
            999999
        );
        productWithLargeInventory.setId(3L);
        when(productService.getProductBySku("BULK-PRODUCT")).thenReturn(productWithLargeInventory);

        // When & Then
        mockMvc.perform(get(API_BASE_PATH + "/products/{sku}", "BULK-PRODUCT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.id").value(3))
                .andExpect(jsonPath("$.data.sku").value("BULK-PRODUCT"))
                .andExpect(jsonPath("$.data.name").value("Bulk Product"))
                .andExpect(jsonPath("$.data.availableInventory").value(999999))
                .andExpect(jsonPath("$.data.available").value(true));

        verify(productService).getProductBySku("BULK-PRODUCT");
    }

    @Test
    void getAllProducts_WithSearch_ServiceThrowsException() throws Exception {
        // Given
        when(productService.searchAvailableProducts("test"))
            .thenThrow(new RuntimeException("Search failed"));

        // When & Then
        mockMvc.perform(get(API_BASE_PATH + "/products")
                .param("search", "test"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.message").value("Search failed"));

        verify(productService).searchAvailableProducts("test");
    }

    @Test
    void getAllProducts_WithMerchantFilter_ServiceThrowsException() throws Exception {
        // Given
        when(productService.getProductsByMerchant(999L))
            .thenThrow(new RuntimeException("Merchant not found"));

        // When & Then
        mockMvc.perform(get(API_BASE_PATH + "/products")
                .param("merchantId", "999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Merchant not found"));

        verify(productService).getProductsByMerchant(999L);
    }
}
