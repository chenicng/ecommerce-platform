package com.ecommerce.api.controller;

import com.ecommerce.application.service.MerchantService;
import com.ecommerce.application.service.ProductService;
import com.ecommerce.domain.Money;
import com.ecommerce.domain.merchant.Merchant;
import com.ecommerce.domain.product.Product;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MerchantController.class)
class MerchantControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MerchantService merchantService;

    @MockBean
    private ProductService productService;

    @Autowired
    private ObjectMapper objectMapper;

    private Merchant testMerchant;
    private Product testProduct;
    private MerchantController.CreateMerchantRequest createMerchantRequest;

    @BeforeEach
    void setUp() {
        // Setup test merchant
        testMerchant = new Merchant(
            "Test Store",
            "BL12345678",
            "merchant@test.com",
            "13900139000",
            "CNY"
        );

        // Setup test product
        testProduct = new Product(
            "IPHONE15",
            "iPhone 15",
            "Latest iPhone model",
            Money.of("999.99", "CNY"),
            1L,
            100
        );

        // Setup create merchant request
        createMerchantRequest = new MerchantController.CreateMerchantRequest();
        createMerchantRequest.setMerchantName("Test Store");
        createMerchantRequest.setBusinessLicense("BL12345678");
        createMerchantRequest.setContactEmail("merchant@test.com");
        createMerchantRequest.setContactPhone("13900139000");
    }

    @Test
    void createMerchant_Success() throws Exception {
        // Given
        when(merchantService.createMerchant("Test Store", "BL12345678", "merchant@test.com", "13900139000"))
            .thenReturn(testMerchant);
        when(merchantService.getMerchantBalance(any())).thenReturn(Money.of("0.00", "CNY"));
        when(merchantService.getMerchantTotalIncome(any())).thenReturn(Money.of("0.00", "CNY"));

        // When & Then
        mockMvc.perform(post("/api/merchants")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createMerchantRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.merchantName").value("Test Store"))
                .andExpect(jsonPath("$.businessLicense").value("BL12345678"));

        verify(merchantService).createMerchant("Test Store", "BL12345678", "merchant@test.com", "13900139000");
    }

    @Test
    void createMerchant_ValidationError() throws Exception {
        // Given
        MerchantController.CreateMerchantRequest invalidRequest = new MerchantController.CreateMerchantRequest();
        invalidRequest.setBusinessLicense("BL12345678");
        invalidRequest.setContactEmail("merchant@test.com");
        invalidRequest.setContactPhone("13900139000");

        // When & Then
        mockMvc.perform(post("/api/merchants")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(merchantService, never()).createMerchant(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void createMerchant_ServiceError() throws Exception {
        // Given
        when(merchantService.createMerchant(anyString(), anyString(), anyString(), anyString()))
            .thenThrow(new RuntimeException("Service error"));

        // When & Then
        mockMvc.perform(post("/api/merchants")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createMerchantRequest)))
                .andExpect(status().isBadRequest());

        verify(merchantService).createMerchant("Test Store", "BL12345678", "merchant@test.com", "13900139000");
    }

    @Test
    void getMerchantProducts_Success() throws Exception {
        // Given
        when(merchantService.merchantExists(1L)).thenReturn(true);
        when(productService.getProductsByMerchant(1L)).thenReturn(Arrays.asList(testProduct));

        // When & Then
        mockMvc.perform(get("/api/merchants/{merchantId}/products", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.merchantId").value(1))
                .andExpect(jsonPath("$.totalCount").value(1));

        verify(merchantService).merchantExists(1L);
        verify(productService).getProductsByMerchant(1L);
    }

    @Test
    void getMerchantProducts_WithStatusFilter() throws Exception {
        // Given
        when(merchantService.merchantExists(1L)).thenReturn(true);
        when(productService.getProductsByMerchant(1L)).thenReturn(Arrays.asList(testProduct));

        // When & Then
        mockMvc.perform(get("/api/merchants/{merchantId}/products", 1L)
                .param("status", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.merchantId").value(1));

        verify(merchantService).merchantExists(1L);
        verify(productService).getProductsByMerchant(1L);
    }

    @Test
    void getMerchantProducts_WithSearchFilter() throws Exception {
        // Given
        when(merchantService.merchantExists(1L)).thenReturn(true);
        when(productService.getProductsByMerchant(1L)).thenReturn(Arrays.asList(testProduct));

        // When & Then
        mockMvc.perform(get("/api/merchants/{merchantId}/products", 1L)
                .param("search", "iPhone"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.merchantId").value(1));

        verify(merchantService).merchantExists(1L);
        verify(productService).getProductsByMerchant(1L);
    }

    @Test
    void getMerchantProducts_MerchantNotFound() throws Exception {
        // Given
        when(merchantService.merchantExists(999L)).thenReturn(false);

        // When & Then
        mockMvc.perform(get("/api/merchants/{merchantId}/products", 999L))
                .andExpect(status().isBadRequest());

        verify(merchantService).merchantExists(999L);
        verify(productService, never()).getProductsByMerchant(anyLong());
    }

    @Test
    void getMerchantProducts_EmptyList() throws Exception {
        // Given
        when(merchantService.merchantExists(1L)).thenReturn(true);
        when(productService.getProductsByMerchant(1L)).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/api/merchants/{merchantId}/products", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.merchantId").value(1))
                .andExpect(jsonPath("$.totalCount").value(0));

        verify(merchantService).merchantExists(1L);
        verify(productService).getProductsByMerchant(1L);
    }

    @Test
    void getMerchantIncome_Success() throws Exception {
        // Given
        Money currentBalance = Money.of("15000.00", "CNY");
        Money totalIncome = Money.of("25000.00", "CNY");
        when(merchantService.getMerchantBalance(1L)).thenReturn(currentBalance);
        when(merchantService.getMerchantTotalIncome(1L)).thenReturn(totalIncome);

        // When & Then
        mockMvc.perform(get("/api/merchants/{merchantId}/income", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.merchantId").value(1))
                .andExpect(jsonPath("$.currentBalance").value(15000.00))
                .andExpect(jsonPath("$.totalIncome").value(25000.00));

        verify(merchantService).getMerchantBalance(1L);
        verify(merchantService).getMerchantTotalIncome(1L);
    }

    @Test
    void getMerchantIncome_ServiceError() throws Exception {
        // Given
        when(merchantService.getMerchantBalance(1L)).thenThrow(new RuntimeException("Merchant not found"));

        // When & Then
        mockMvc.perform(get("/api/merchants/{merchantId}/income", 1L))
                .andExpect(status().isBadRequest());

        verify(merchantService).getMerchantBalance(1L);
    }

    @Test
    void addInventory_Success() throws Exception {
        // Given
        MerchantController.AddInventoryRequest addInventoryRequest = new MerchantController.AddInventoryRequest();
        addInventoryRequest.setQuantity(50);
        
        when(merchantService.merchantExists(1L)).thenReturn(true);
        when(productService.getProductBySku("IPHONE15")).thenReturn(testProduct);
        doNothing().when(productService).addProductInventory("IPHONE15", 50);

        // When & Then
        mockMvc.perform(post("/api/merchants/{merchantId}/products/{sku}/add-inventory", 1L, "IPHONE15")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(addInventoryRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("Inventory added successfully"));

        verify(merchantService).merchantExists(1L);
        verify(productService).addProductInventory("IPHONE15", 50);
    }

    @Test
    void addInventory_MerchantNotFound() throws Exception {
        // Given
        MerchantController.AddInventoryRequest addInventoryRequest = new MerchantController.AddInventoryRequest();
        addInventoryRequest.setQuantity(50);
        
        when(merchantService.merchantExists(999L)).thenReturn(false);

        // When & Then
        mockMvc.perform(post("/api/merchants/{merchantId}/products/{sku}/add-inventory", 999L, "IPHONE15")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(addInventoryRequest)))
                .andExpect(status().isBadRequest());

        verify(merchantService).merchantExists(999L);
        verify(productService, never()).addProductInventory(anyString(), anyInt());
    }

    @Test
    void addInventory_ProductNotFound() throws Exception {
        // Given
        MerchantController.AddInventoryRequest addInventoryRequest = new MerchantController.AddInventoryRequest();
        addInventoryRequest.setQuantity(50);
        
        when(merchantService.merchantExists(1L)).thenReturn(true);
        when(productService.getProductBySku("UNKNOWN")).thenThrow(new RuntimeException("Product not found"));

        // When & Then
        mockMvc.perform(post("/api/merchants/{merchantId}/products/{sku}/add-inventory", 1L, "UNKNOWN")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(addInventoryRequest)))
                .andExpect(status().isBadRequest());

        verify(merchantService).merchantExists(1L);
        verify(productService).getProductBySku("UNKNOWN");
    }

    @Test
    void addInventory_InvalidQuantity() throws Exception {
        // Given
        MerchantController.AddInventoryRequest addInventoryRequest = new MerchantController.AddInventoryRequest();
        addInventoryRequest.setQuantity(-10);
        
        when(merchantService.merchantExists(1L)).thenReturn(true);

        // When & Then
        mockMvc.perform(post("/api/merchants/{merchantId}/products/{sku}/add-inventory", 1L, "IPHONE15")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(addInventoryRequest)))
                .andExpect(status().isBadRequest());

        verify(merchantService).merchantExists(1L);
        verify(productService, never()).addProductInventory(anyString(), anyInt());
    }

    @Test
    void addInventory_ProductNotBelongToMerchant() throws Exception {
        // Given
        MerchantController.AddInventoryRequest addInventoryRequest = new MerchantController.AddInventoryRequest();
        addInventoryRequest.setQuantity(50);
        
        Product otherMerchantProduct = new Product("IPHONE15", "iPhone 15", "Latest iPhone model", 
                                                  Money.of("999.99", "CNY"), 2L, 100);
        
        when(merchantService.merchantExists(1L)).thenReturn(true);
        when(productService.getProductBySku("IPHONE15")).thenReturn(otherMerchantProduct);

        // When & Then
        mockMvc.perform(post("/api/merchants/{merchantId}/products/{sku}/add-inventory", 1L, "IPHONE15")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(addInventoryRequest)))
                .andExpect(status().isBadRequest());

        verify(merchantService).merchantExists(1L);
        verify(productService).getProductBySku("IPHONE15");
        verify(productService, never()).addProductInventory(anyString(), anyInt());
    }
}
