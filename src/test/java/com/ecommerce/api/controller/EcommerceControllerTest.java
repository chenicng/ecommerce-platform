package com.ecommerce.api.controller;

import com.ecommerce.application.service.EcommerceService;
import com.ecommerce.application.service.ProductService;
import com.ecommerce.application.dto.PurchaseRequest;
import com.ecommerce.application.dto.PurchaseResponse;
import com.ecommerce.domain.Money;
import com.ecommerce.domain.product.Product;
import com.ecommerce.domain.product.ProductInventory;
import com.ecommerce.domain.product.ProductStatus;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.hasSize;

@WebMvcTest(EcommerceController.class)
class EcommerceControllerTest {

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
            Money.of("1999.98", "CNY"),
            "SUCCESS",
            "Purchase completed successfully"
        );
    }

    @Test
    void purchaseProduct_Success() throws Exception {
        // Given
        when(ecommerceService.processPurchase(any(PurchaseRequest.class)))
            .thenReturn(testPurchaseResponse);

        // When & Then
        mockMvc.perform(post("/api/ecommerce/purchase")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testPurchaseRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderNumber").value("ORD123"))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.merchantId").value(1))
                .andExpect(jsonPath("$.sku").value("IPHONE15"))
                .andExpect(jsonPath("$.productName").value("iPhone 15"))
                .andExpect(jsonPath("$.quantity").value(2))
                .andExpect(jsonPath("$.totalAmount.amount").value(1999.98))
                .andExpect(jsonPath("$.totalAmount.currency").value("CNY"))
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("Purchase completed successfully"));

        verify(ecommerceService).processPurchase(any(PurchaseRequest.class));
    }

    @Test
    void purchaseProduct_ValidationError_NullUserId() throws Exception {
        // Given
        PurchaseRequest invalidRequest = new PurchaseRequest(null, "IPHONE15", 2);

        // When & Then
        mockMvc.perform(post("/api/ecommerce/purchase")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("FAILED"))
                .andExpect(jsonPath("$.message").value("User ID is required"));

        verify(ecommerceService, never()).processPurchase(any());
    }

    @Test
    void purchaseProduct_ValidationError_EmptySku() throws Exception {
        // Given
        PurchaseRequest invalidRequest = new PurchaseRequest(1L, "", 2);

        // When & Then
        mockMvc.perform(post("/api/ecommerce/purchase")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("FAILED"))
                .andExpect(jsonPath("$.message").value("SKU is required"));

        verify(ecommerceService, never()).processPurchase(any());
    }

    @Test
    void purchaseProduct_ValidationError_InvalidQuantity() throws Exception {
        // Given
        PurchaseRequest invalidRequest = new PurchaseRequest(1L, "IPHONE15", 0);

        // When & Then
        mockMvc.perform(post("/api/ecommerce/purchase")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("FAILED"))
                .andExpect(jsonPath("$.message").value("Quantity must be positive"));

        verify(ecommerceService, never()).processPurchase(any());
    }

    @Test
    void purchaseProduct_ServiceError() throws Exception {
        // Given
        when(ecommerceService.processPurchase(any(PurchaseRequest.class)))
            .thenThrow(new RuntimeException("Insufficient inventory"));

        // When & Then
        mockMvc.perform(post("/api/ecommerce/purchase")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testPurchaseRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("FAILED"))
                .andExpect(jsonPath("$.message").value("Insufficient inventory"));

        verify(ecommerceService).processPurchase(any(PurchaseRequest.class));
    }

    @Test
    void getProductBySku_Success() throws Exception {
        // Given
        when(productService.getProductBySku("IPHONE15")).thenReturn(testProduct);

        // When & Then
        mockMvc.perform(get("/api/ecommerce/products/{sku}", "IPHONE15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.sku").value("IPHONE15"))
                .andExpect(jsonPath("$.name").value("iPhone 15"))
                .andExpect(jsonPath("$.description").value("Latest iPhone model"))
                .andExpect(jsonPath("$.price").value(999.99))
                .andExpect(jsonPath("$.currency").value("CNY"))
                .andExpect(jsonPath("$.merchantId").value(1))
                .andExpect(jsonPath("$.availableInventory").value(100))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.available").value(true));

        verify(productService).getProductBySku("IPHONE15");
    }

    @Test
    void getProductBySku_NotFound() throws Exception {
        // Given
        when(productService.getProductBySku("UNKNOWN")).thenThrow(new RuntimeException("Product not found"));

        // When & Then
        mockMvc.perform(get("/api/ecommerce/products/{sku}", "UNKNOWN"))
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
        mockMvc.perform(get("/api/ecommerce/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products", hasSize(2)))
                .andExpect(jsonPath("$.totalCount").value(2))
                .andExpect(jsonPath("$.products[0].sku").value("IPHONE15"))
                .andExpect(jsonPath("$.products[1].sku").value("SAMSUNG_S24"));

        verify(productService).getAllAvailableProducts();
    }

    @Test
    void getAllProducts_WithSearch() throws Exception {
        // Given
        List<Product> products = Arrays.asList(testProduct);
        when(productService.searchAvailableProducts("iPhone")).thenReturn(products);

        // When & Then
        mockMvc.perform(get("/api/ecommerce/products").param("search", "iPhone"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products", hasSize(1)))
                .andExpect(jsonPath("$.totalCount").value(1))
                .andExpect(jsonPath("$.searchTerm").value("iPhone"))
                .andExpect(jsonPath("$.products[0].sku").value("IPHONE15"));

        verify(productService).searchAvailableProducts("iPhone");
    }

    @Test
    void getProductInventory_Success() throws Exception {
        // Given
        when(productService.getProductBySku("IPHONE15")).thenReturn(testProduct);

        // When & Then
        mockMvc.perform(get("/api/ecommerce/products/{sku}/inventory", "IPHONE15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sku").value("IPHONE15"))
                .andExpect(jsonPath("$.productName").value("iPhone 15"))
                .andExpect(jsonPath("$.availableInventory").value(100))
                .andExpect(jsonPath("$.available").value(true))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        verify(productService).getProductBySku("IPHONE15");
    }

    @Test
    void getProductInventory_NotFound() throws Exception {
        // Given
        when(productService.getProductBySku("UNKNOWN")).thenThrow(new RuntimeException("Product not found"));

        // When & Then
        mockMvc.perform(get("/api/ecommerce/products/{sku}/inventory", "UNKNOWN"))
                .andExpect(status().isNotFound());

        verify(productService).getProductBySku("UNKNOWN");
    }
}
