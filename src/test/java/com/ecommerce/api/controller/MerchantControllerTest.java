package com.ecommerce.api.controller;

import com.ecommerce.application.service.MerchantService;
import com.ecommerce.application.service.ProductService;
import com.ecommerce.domain.Money;
import com.ecommerce.domain.merchant.Merchant;
import com.ecommerce.domain.product.Product;
import com.ecommerce.api.config.ApiVersionConfig;
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
import static org.junit.jupiter.api.Assertions.*;

@WebMvcTest(MerchantController.class)
class MerchantControllerTest {

    private static final String API_BASE_PATH = ApiVersionConfig.API_V1 + "/merchants";

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
        mockMvc.perform(post(API_BASE_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createMerchantRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.merchantName").value("Test Store"))
                .andExpect(jsonPath("$.data.businessLicense").value("BL12345678"));

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
        mockMvc.perform(post(API_BASE_PATH)
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
        mockMvc.perform(post(API_BASE_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createMerchantRequest)))
                .andExpect(status().isInternalServerError());

        verify(merchantService).createMerchant("Test Store", "BL12345678", "merchant@test.com", "13900139000");
    }

    @Test
    void getMerchantProducts_Success() throws Exception {
        // Given
        when(merchantService.merchantExists(1L)).thenReturn(true);
        when(productService.getProductsByMerchant(1L)).thenReturn(Arrays.asList(testProduct));

        // When & Then
        mockMvc.perform(get(API_BASE_PATH + "/{merchantId}/products", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.merchantId").value(1))
                .andExpect(jsonPath("$.data.totalCount").value(1));

        verify(merchantService).merchantExists(1L);
        verify(productService).getProductsByMerchant(1L);
    }

    @Test
    void getMerchantProducts_WithStatusFilter() throws Exception {
        // Given
        when(merchantService.merchantExists(1L)).thenReturn(true);
        when(productService.getProductsByMerchant(1L)).thenReturn(Arrays.asList(testProduct));

        // When & Then
        mockMvc.perform(get(API_BASE_PATH + "/{merchantId}/products", 1L)
                .param("status", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.merchantId").value(1));

        verify(merchantService).merchantExists(1L);
        verify(productService).getProductsByMerchant(1L);
    }

    @Test
    void getMerchantProducts_WithSearchFilter() throws Exception {
        // Given
        when(merchantService.merchantExists(1L)).thenReturn(true);
        when(productService.getProductsByMerchant(1L)).thenReturn(Arrays.asList(testProduct));

        // When & Then
        mockMvc.perform(get(API_BASE_PATH + "/{merchantId}/products", 1L)
                .param("search", "iPhone"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.merchantId").value(1));

        verify(merchantService).merchantExists(1L);
        verify(productService).getProductsByMerchant(1L);
    }

    @Test
    void getMerchantProducts_MerchantNotFound() throws Exception {
        // Given
        when(merchantService.merchantExists(999L)).thenReturn(false);

        // When & Then
        mockMvc.perform(get(API_BASE_PATH + "/{merchantId}/products", 999L))
                .andExpect(status().isNotFound());

        verify(merchantService).merchantExists(999L);
        verify(productService, never()).getProductsByMerchant(anyLong());
    }

    @Test
    void getMerchantProducts_EmptyList() throws Exception {
        // Given
        when(merchantService.merchantExists(1L)).thenReturn(true);
        when(productService.getProductsByMerchant(1L)).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get(API_BASE_PATH + "/{merchantId}/products", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.merchantId").value(1))
                .andExpect(jsonPath("$.data.totalCount").value(0));

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
        mockMvc.perform(get(API_BASE_PATH + "/{merchantId}/income", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.merchantId").value(1))
                .andExpect(jsonPath("$.data.currentBalance").value(15000.00))
                .andExpect(jsonPath("$.data.totalIncome").value(25000.00));

        verify(merchantService).getMerchantBalance(1L);
        verify(merchantService).getMerchantTotalIncome(1L);
    }

    @Test
    void getMerchantIncome_ServiceError() throws Exception {
        // Given
        when(merchantService.getMerchantBalance(1L)).thenThrow(new RuntimeException("Merchant not found"));

        // When & Then
        mockMvc.perform(get(API_BASE_PATH + "/{merchantId}/income", 1L))
                .andExpect(status().isNotFound());

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
        mockMvc.perform(post(API_BASE_PATH + "/{merchantId}/products/{sku}/inventory/add", 1L, "IPHONE15")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(addInventoryRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sku").value("IPHONE15"));

        verify(merchantService).merchantExists(1L);
        verify(productService).addProductInventory("IPHONE15", 50);
        verify(productService, times(2)).getProductBySku("IPHONE15"); // Called twice: validation + response
    }

    @Test
    void addInventory_MerchantNotFound() throws Exception {
        // Given
        MerchantController.AddInventoryRequest addInventoryRequest = new MerchantController.AddInventoryRequest();
        addInventoryRequest.setQuantity(50);
        
        when(merchantService.merchantExists(999L)).thenReturn(false);

        // When & Then
        mockMvc.perform(post(API_BASE_PATH + "/{merchantId}/products/{sku}/inventory/add", 999L, "IPHONE15")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(addInventoryRequest)))
                .andExpect(status().isNotFound());

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
        mockMvc.perform(post(API_BASE_PATH + "/{merchantId}/products/{sku}/inventory/add", 1L, "UNKNOWN")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(addInventoryRequest)))
                .andExpect(status().isNotFound());

        verify(merchantService).merchantExists(1L);
        verify(productService).getProductBySku("UNKNOWN");
    }

    @Test
    void addInventory_InvalidQuantity() throws Exception {
        // Given
        MerchantController.AddInventoryRequest addInventoryRequest = new MerchantController.AddInventoryRequest();
        addInventoryRequest.setQuantity(-10);

        // When & Then
        mockMvc.perform(post(API_BASE_PATH + "/{merchantId}/products/{sku}/inventory/add", 1L, "IPHONE15")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(addInventoryRequest)))
                .andExpect(status().isBadRequest());

        // Note: When Bean Validation fails, the controller method is never called,
        // so we don't verify merchantService.merchantExists() or productService interactions
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
        mockMvc.perform(post(API_BASE_PATH + "/{merchantId}/products/{sku}/inventory/add", 1L, "IPHONE15")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(addInventoryRequest)))
                .andExpect(status().isBadRequest());

        verify(merchantService).merchantExists(1L);
        verify(productService).getProductBySku("IPHONE15");
        verify(productService, never()).addProductInventory(anyString(), anyInt());
    }

    @Test
    void reduceInventory_Success() throws Exception {
        // Given
        MerchantController.ReduceInventoryRequest reduceInventoryRequest = new MerchantController.ReduceInventoryRequest();
        reduceInventoryRequest.setQuantity(20);
        
        when(merchantService.merchantExists(1L)).thenReturn(true);
        when(productService.getProductBySku("IPHONE15")).thenReturn(testProduct);
        doNothing().when(productService).reduceInventory("IPHONE15", 20);

        // When & Then
        mockMvc.perform(post(API_BASE_PATH + "/{merchantId}/products/{sku}/inventory/reduce", 1L, "IPHONE15")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reduceInventoryRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sku").value("IPHONE15"));

        verify(merchantService).merchantExists(1L);
        verify(productService).reduceInventory("IPHONE15", 20);
        verify(productService, times(2)).getProductBySku("IPHONE15"); // Called twice: validation + response
    }

    @Test
    void setInventory_Success() throws Exception {
        // Given
        when(merchantService.merchantExists(1L)).thenReturn(true);
        when(productService.getProductBySku("IPHONE15")).thenReturn(testProduct);
        when(productService.getProductBySku("IPHONE15")).thenReturn(testProduct);

        MerchantController.SetInventoryRequest request = new MerchantController.SetInventoryRequest();
        request.setQuantity(50);

        // When & Then
        mockMvc.perform(put(API_BASE_PATH + "/{merchantId}/products/{sku}/inventory", 1L, "IPHONE15")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sku").value("IPHONE15"));

        verify(productService).setInventory("IPHONE15", 50);
    }

    @Test
    void createProduct_Success() throws Exception {
        // Given
        when(merchantService.merchantExists(1L)).thenReturn(true);
        when(productService.createProduct(anyString(), anyString(), anyString(), any(), anyLong(), anyInt()))
            .thenReturn(testProduct);

        MerchantController.CreateProductRequest request = new MerchantController.CreateProductRequest();
        request.setSku("IPHONE15");
        request.setName("iPhone 15");
        request.setDescription("Latest iPhone model");
        request.setPrice(new BigDecimal("999.99"));
        request.setCurrency("CNY");
        request.setInitialInventory(100);

        // When & Then
        mockMvc.perform(post(API_BASE_PATH + "/{merchantId}/products", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sku").value("IPHONE15"));

        verify(productService).createProduct("IPHONE15", "iPhone 15", "Latest iPhone model", 
            Money.of("999.99", "CNY"), 1L, 100);
    }

    @Test
    void createProduct_MerchantNotFound() throws Exception {
        // Given
        when(merchantService.merchantExists(999L)).thenReturn(false);

        MerchantController.CreateProductRequest request = new MerchantController.CreateProductRequest();
        request.setSku("IPHONE15");
        request.setName("iPhone 15");
        request.setDescription("Latest iPhone model");
        request.setPrice(new BigDecimal("999.99"));
        request.setCurrency("CNY");
        request.setInitialInventory(100);

        // When & Then
        mockMvc.perform(post(API_BASE_PATH + "/{merchantId}/products", 999L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        verify(merchantService).merchantExists(999L);
        verify(productService, never()).createProduct(anyString(), anyString(), anyString(), any(), anyLong(), anyInt());
    }

    @Test
    void createProduct_ValidationError() throws Exception {
        // Given
        MerchantController.CreateProductRequest invalidRequest = new MerchantController.CreateProductRequest();
        // Missing required fields

        // When & Then
        mockMvc.perform(post(API_BASE_PATH + "/{merchantId}/products", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(merchantService, never()).merchantExists(anyLong());
        verify(productService, never()).createProduct(anyString(), anyString(), anyString(), any(), anyLong(), anyInt());
    }

    @Test
    void getMerchantProduct_Success() throws Exception {
        // Given
        when(merchantService.merchantExists(1L)).thenReturn(true);
        when(productService.getProductBySku("IPHONE15")).thenReturn(testProduct);

        // When & Then
        mockMvc.perform(get(API_BASE_PATH + "/{merchantId}/products/{sku}", 1L, "IPHONE15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sku").value("IPHONE15"));

        verify(merchantService).merchantExists(1L);
        verify(productService).getProductBySku("IPHONE15");
    }

    @Test
    void getMerchantProduct_MerchantNotFound() throws Exception {
        // Given
        when(merchantService.merchantExists(999L)).thenReturn(false);

        // When & Then
        mockMvc.perform(get(API_BASE_PATH + "/{merchantId}/products/{sku}", 999L, "IPHONE15"))
                .andExpect(status().isNotFound());

        verify(merchantService).merchantExists(999L);
        verify(productService, never()).getProductBySku(anyString());
    }

    @Test
    void getMerchantProduct_ProductNotFound() throws Exception {
        // Given
        when(merchantService.merchantExists(1L)).thenReturn(true);
        when(productService.getProductBySku("INVALID-SKU")).thenReturn(null);

        // When & Then
        mockMvc.perform(get(API_BASE_PATH + "/{merchantId}/products/{sku}", 1L, "INVALID-SKU"))
                .andExpect(status().isInternalServerError());

        verify(merchantService).merchantExists(1L);
        verify(productService).getProductBySku("INVALID-SKU");
    }

    @Test
    void getMerchantProduct_ProductNotBelongToMerchant() throws Exception {
        // Given
        Product productForDifferentMerchant = new Product(
            "IPHONE15",
            "iPhone 15",
            "Latest iPhone model",
            Money.of("999.99", "CNY"),
            2L, // Different merchant ID
            100
        );
        when(merchantService.merchantExists(1L)).thenReturn(true);
        when(productService.getProductBySku("IPHONE15")).thenReturn(productForDifferentMerchant);

        // When & Then
        mockMvc.perform(get(API_BASE_PATH + "/{merchantId}/products/{sku}", 1L, "IPHONE15"))
                .andExpect(status().isBadRequest());

        verify(merchantService).merchantExists(1L);
        verify(productService).getProductBySku("IPHONE15");
    }

    // Test internal classes getter/setter methods
    @Test
    void testCreateMerchantRequest_GettersAndSetters() {
        MerchantController.CreateMerchantRequest request = new MerchantController.CreateMerchantRequest();
        
        request.setMerchantName("Test Store");
        request.setBusinessLicense("BL12345678");
        request.setContactEmail("test@example.com");
        request.setContactPhone("13900139000");
        
        assertEquals("Test Store", request.getMerchantName());
        assertEquals("BL12345678", request.getBusinessLicense());
        assertEquals("test@example.com", request.getContactEmail());
        assertEquals("13900139000", request.getContactPhone());
    }

    @Test
    void testCreateProductRequest_GettersAndSetters() {
        MerchantController.CreateProductRequest request = new MerchantController.CreateProductRequest();
        
        request.setSku("TEST-SKU");
        request.setName("Test Product");
        request.setDescription("Test Description");
        request.setPrice(new BigDecimal("99.99"));
        request.setCurrency("USD");
        request.setInitialInventory(50);
        
        assertEquals("TEST-SKU", request.getSku());
        assertEquals("Test Product", request.getName());
        assertEquals("Test Description", request.getDescription());
        assertEquals(new BigDecimal("99.99"), request.getPrice());
        assertEquals("USD", request.getCurrency());
        assertEquals(50, request.getInitialInventory());
    }

    @Test
    void testAddInventoryRequest_GettersAndSetters() {
        MerchantController.AddInventoryRequest request = new MerchantController.AddInventoryRequest();
        
        request.setQuantity(25);
        assertEquals(25, request.getQuantity());
    }

    @Test
    void testReduceInventoryRequest_GettersAndSetters() {
        MerchantController.ReduceInventoryRequest request = new MerchantController.ReduceInventoryRequest();
        
        request.setQuantity(10);
        assertEquals(10, request.getQuantity());
    }

    @Test
    void testSetInventoryRequest_GettersAndSetters() {
        MerchantController.SetInventoryRequest request = new MerchantController.SetInventoryRequest();
        
        request.setQuantity(100);
        assertEquals(100, request.getQuantity());
    }

    @Test
    void testInventoryResponse_GettersAndSetters() {
        MerchantController.InventoryResponse response = new MerchantController.InventoryResponse(
            "TEST-SKU", "Test Product", 50, true
        );
        
        assertEquals("TEST-SKU", response.getSku());
        assertEquals("Test Product", response.getProductName());
        assertEquals(50, response.getAvailableInventory());
        assertTrue(response.isAvailable());
    }

    @Test
    void testIncomeResponse_GettersAndSetters() {
        MerchantController.IncomeResponse response = new MerchantController.IncomeResponse(
            1L, new BigDecimal("1000.00"), new BigDecimal("5000.00"), "CNY"
        );
        
        assertEquals(1L, response.getMerchantId());
        assertEquals(new BigDecimal("1000.00"), response.getCurrentBalance());
        assertEquals(new BigDecimal("5000.00"), response.getTotalIncome());
        assertEquals("CNY", response.getCurrency());
    }

    @Test
    void testMerchantProductListResponse_GettersAndSetters() {
        MerchantController.ProductResponse productResponse = new MerchantController.ProductResponse(
            1L, "TEST-SKU", "Test Product", "Description", 
            new BigDecimal("99.99"), "CNY", 1L, 50, "ACTIVE"
        );
        
        MerchantController.MerchantProductListResponse response = new MerchantController.MerchantProductListResponse(
            1L, Arrays.asList(productResponse), 1, "ACTIVE", "test"
        );
        
        assertEquals(1L, response.getMerchantId());
        assertEquals(1, response.getProducts().size());
        assertEquals(1, response.getTotalCount());
        assertEquals("ACTIVE", response.getStatusFilter());
        assertEquals("test", response.getSearchTerm());
    }

    @Test
    void testSettlementRequest_GettersAndSetters() {
        MerchantController.SettlementRequest request = new MerchantController.SettlementRequest();
        
        java.time.LocalDate date = java.time.LocalDate.of(2023, 10, 27);
        request.setSettlementDate(date);
        assertEquals(date, request.getSettlementDate());
    }

    @Test
    void testSettlementResponse_GettersAndSetters() {
        MerchantController.SettlementResponse response = new MerchantController.SettlementResponse(
            1L, java.time.LocalDate.of(2023, 10, 27), 
            new BigDecimal("1000.00"), new BigDecimal("950.00"), 
            new BigDecimal("50.00"), "MATCHED", "Settlement completed"
        );
        
        assertEquals(1L, response.getMerchantId());
        assertEquals(java.time.LocalDate.of(2023, 10, 27), response.getSettlementDate());
        assertEquals(new BigDecimal("1000.00"), response.getExpectedIncome());
        assertEquals(new BigDecimal("950.00"), response.getActualBalance());
        assertEquals(new BigDecimal("50.00"), response.getDifference());
        assertEquals("MATCHED", response.getStatus());
        assertEquals("Settlement completed", response.getNotes());
        
        // Test setters
        response.setMerchantId(2L);
        response.setSettlementDate(java.time.LocalDate.of(2023, 10, 28));
        response.setExpectedIncome(new BigDecimal("2000.00"));
        response.setActualBalance(new BigDecimal("1900.00"));
        response.setDifference(new BigDecimal("100.00"));
        response.setStatus("MISMATCH");
        response.setNotes("Updated notes");
        
        assertEquals(2L, response.getMerchantId());
        assertEquals(java.time.LocalDate.of(2023, 10, 28), response.getSettlementDate());
        assertEquals(new BigDecimal("2000.00"), response.getExpectedIncome());
        assertEquals(new BigDecimal("1900.00"), response.getActualBalance());
        assertEquals(new BigDecimal("100.00"), response.getDifference());
        assertEquals("MISMATCH", response.getStatus());
        assertEquals("Updated notes", response.getNotes());
    }

    @Test
    void testGlobalSettlementRequest_GettersAndSetters() {
        MerchantController.GlobalSettlementRequest request = new MerchantController.GlobalSettlementRequest();
        
        java.time.LocalDate date = java.time.LocalDate.of(2023, 10, 27);
        request.setSettlementDate(date);
        assertEquals(date, request.getSettlementDate());
    }

    @Test
    void testGlobalSettlementResponse_GettersAndSetters() {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        MerchantController.GlobalSettlementResponse response = new MerchantController.GlobalSettlementResponse(
            java.time.LocalDate.of(2023, 10, 27), now
        );
        
        assertEquals(java.time.LocalDate.of(2023, 10, 27), response.getSettlementDate());
        assertEquals(now, response.getSettlementTime());
        
        // Test setters
        java.time.LocalDate newDate = java.time.LocalDate.of(2023, 10, 28);
        java.time.LocalDateTime newTime = java.time.LocalDateTime.now().plusDays(1);
        response.setSettlementDate(newDate);
        response.setSettlementTime(newTime);
        
        assertEquals(newDate, response.getSettlementDate());
        assertEquals(newTime, response.getSettlementTime());
    }

    @Test
    void testMerchantResponse_GettersAndSetters() {
        MerchantController.MerchantResponse response = new MerchantController.MerchantResponse(
            1L, "Test Store", "BL12345678", "test@example.com", "13900139000",
            new BigDecimal("1000.00"), "CNY", new BigDecimal("5000.00"), "ACTIVE"
        );
        
        assertEquals(1L, response.getId());
        assertEquals("Test Store", response.getMerchantName());
        assertEquals("BL12345678", response.getBusinessLicense());
        assertEquals("test@example.com", response.getContactEmail());
        assertEquals("13900139000", response.getContactPhone());
        assertEquals(new BigDecimal("1000.00"), response.getBalance());
        assertEquals("CNY", response.getCurrency());
        assertEquals(new BigDecimal("5000.00"), response.getTotalIncome());
        assertEquals("ACTIVE", response.getStatus());
    }

    @Test
    void testProductResponse_GettersAndSetters() {
        MerchantController.ProductResponse response = new MerchantController.ProductResponse(
            1L, "TEST-SKU", "Test Product", "Description", 
            new BigDecimal("99.99"), "CNY", 1L, 50, "ACTIVE"
        );
        
        assertEquals(1L, response.getId());
        assertEquals("TEST-SKU", response.getSku());
        assertEquals("Test Product", response.getName());
        assertEquals("Description", response.getDescription());
        assertEquals(new BigDecimal("99.99"), response.getPrice());
        assertEquals("CNY", response.getCurrency());
        assertEquals(1L, response.getMerchantId());
        assertEquals(50, response.getAvailableInventory());
        assertEquals("ACTIVE", response.getStatus());
    }

    // Additional edge case tests for better coverage
    @Test
    void createProduct_ValidationError_NullSku() throws Exception {
        // Given
        MerchantController.CreateProductRequest invalidRequest = new MerchantController.CreateProductRequest();
        invalidRequest.setName("Test Product");
        invalidRequest.setDescription("Test Description");
        invalidRequest.setPrice(new BigDecimal("99.99"));
        invalidRequest.setCurrency("CNY");
        invalidRequest.setInitialInventory(100);

        // When & Then
        mockMvc.perform(post(API_BASE_PATH + "/{merchantId}/products", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(productService, never()).createProduct(anyString(), anyString(), anyString(), any(), anyLong(), anyInt());
    }

    @Test
    void createProduct_ValidationError_NullName() throws Exception {
        // Given
        MerchantController.CreateProductRequest invalidRequest = new MerchantController.CreateProductRequest();
        invalidRequest.setSku("TEST123");
        invalidRequest.setDescription("Test Description");
        invalidRequest.setPrice(new BigDecimal("99.99"));
        invalidRequest.setCurrency("CNY");
        invalidRequest.setInitialInventory(100);

        // When & Then
        mockMvc.perform(post(API_BASE_PATH + "/{merchantId}/products", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(productService, never()).createProduct(anyString(), anyString(), anyString(), any(), anyLong(), anyInt());
    }

    @Test
    void createProduct_ValidationError_NullPrice() throws Exception {
        // Given
        MerchantController.CreateProductRequest invalidRequest = new MerchantController.CreateProductRequest();
        invalidRequest.setSku("TEST123");
        invalidRequest.setName("Test Product");
        invalidRequest.setDescription("Test Description");
        invalidRequest.setCurrency("CNY");
        invalidRequest.setInitialInventory(100);

        // When & Then
        mockMvc.perform(post(API_BASE_PATH + "/{merchantId}/products", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isNotFound());

        verify(productService, never()).createProduct(anyString(), anyString(), anyString(), any(), anyLong(), anyInt());
    }

    @Test
    void createProduct_ValidationError_NullCurrency() throws Exception {
        // Given
        MerchantController.CreateProductRequest invalidRequest = new MerchantController.CreateProductRequest();
        invalidRequest.setSku("TEST123");
        invalidRequest.setName("Test Product");
        invalidRequest.setDescription("Test Description");
        invalidRequest.setPrice(new BigDecimal("99.99"));
        invalidRequest.setInitialInventory(100);

        // When & Then
        mockMvc.perform(post(API_BASE_PATH + "/{merchantId}/products", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isNotFound());

        verify(productService, never()).createProduct(anyString(), anyString(), anyString(), any(), anyLong(), anyInt());
    }

    @Test
    void createProduct_ValidationError_NegativePrice() throws Exception {
        // Given
        MerchantController.CreateProductRequest invalidRequest = new MerchantController.CreateProductRequest();
        invalidRequest.setSku("TEST123");
        invalidRequest.setName("Test Product");
        invalidRequest.setDescription("Test Description");
        invalidRequest.setPrice(new BigDecimal("-99.99"));
        invalidRequest.setCurrency("CNY");
        invalidRequest.setInitialInventory(100);

        // When & Then
        mockMvc.perform(post(API_BASE_PATH + "/{merchantId}/products", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(productService, never()).createProduct(anyString(), anyString(), anyString(), any(), anyLong(), anyInt());
    }

    @Test
    void createProduct_ValidationError_NegativeInventory() throws Exception {
        // Given
        MerchantController.CreateProductRequest invalidRequest = new MerchantController.CreateProductRequest();
        invalidRequest.setSku("TEST123");
        invalidRequest.setName("Test Product");
        invalidRequest.setDescription("Test Description");
        invalidRequest.setPrice(new BigDecimal("99.99"));
        invalidRequest.setCurrency("CNY");
        invalidRequest.setInitialInventory(-100);

        // When & Then
        mockMvc.perform(post(API_BASE_PATH + "/{merchantId}/products", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(productService, never()).createProduct(anyString(), anyString(), anyString(), any(), anyLong(), anyInt());
    }

    @Test
    void addInventory_ValidationError_NullQuantity() throws Exception {
        // Given
        MerchantController.AddInventoryRequest invalidRequest = new MerchantController.AddInventoryRequest();

        // When & Then
        mockMvc.perform(post(API_BASE_PATH + "/{merchantId}/products/{sku}/inventory/add", 1L, testProduct.getSku())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(productService, never()).addProductInventory(anyString(), anyInt());
    }

    @Test
    void addInventory_ValidationError_NegativeQuantity() throws Exception {
        // Given
        MerchantController.AddInventoryRequest invalidRequest = new MerchantController.AddInventoryRequest();
        invalidRequest.setQuantity(-10);

        // When & Then
        mockMvc.perform(post(API_BASE_PATH + "/{merchantId}/products/{sku}/inventory/add", 1L, testProduct.getSku())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(productService, never()).addProductInventory(anyString(), anyInt());
    }

    @Test
    void reduceInventory_ValidationError_NullQuantity() throws Exception {
        // Given
        MerchantController.ReduceInventoryRequest invalidRequest = new MerchantController.ReduceInventoryRequest();

        // When & Then
        mockMvc.perform(post(API_BASE_PATH + "/{merchantId}/products/{sku}/inventory/reduce", 1L, testProduct.getSku())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(productService, never()).reduceInventory(anyString(), anyInt());
    }

    @Test
    void reduceInventory_ValidationError_NegativeQuantity() throws Exception {
        // Given
        MerchantController.ReduceInventoryRequest invalidRequest = new MerchantController.ReduceInventoryRequest();
        invalidRequest.setQuantity(-10);

        // When & Then
        mockMvc.perform(post(API_BASE_PATH + "/{merchantId}/products/{sku}/inventory/reduce", 1L, testProduct.getSku())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(productService, never()).reduceInventory(anyString(), anyInt());
    }

    @Test
    void setInventory_ValidationError_NullQuantity() throws Exception {
        // Given
        MerchantController.SetInventoryRequest invalidRequest = new MerchantController.SetInventoryRequest();

        // When & Then
        mockMvc.perform(put(API_BASE_PATH + "/{merchantId}/products/{sku}/inventory", 1L, testProduct.getSku())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isNotFound());

        verify(productService, never()).setInventory(anyString(), anyInt());
    }

    @Test
    void setInventory_ValidationError_NegativeQuantity() throws Exception {
        // Given
        MerchantController.SetInventoryRequest invalidRequest = new MerchantController.SetInventoryRequest();
        invalidRequest.setQuantity(-10);

        // When & Then
        mockMvc.perform(put(API_BASE_PATH + "/{merchantId}/products/{sku}/inventory", 1L, testProduct.getSku())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(productService, never()).setInventory(anyString(), anyInt());
    }

    @Test
    void getMerchantProduct_ServiceException() throws Exception {
        // Given
        when(merchantService.merchantExists(1L)).thenReturn(true);
        when(productService.getProductBySku("INVALID-SKU")).thenThrow(new RuntimeException("Database error"));

        // When & Then
        mockMvc.perform(get(API_BASE_PATH + "/{merchantId}/products/{sku}", 1L, "INVALID-SKU"))
                .andExpect(status().isInternalServerError());

        verify(merchantService).merchantExists(1L);
        verify(productService).getProductBySku("INVALID-SKU");
    }

    @Test
    void addInventory_ServiceException() throws Exception {
        // Given
        when(merchantService.merchantExists(1L)).thenReturn(true);
        when(productService.getProductBySku(testProduct.getSku())).thenReturn(testProduct);
        doThrow(new RuntimeException("Database error")).when(productService).addProductInventory(eq(testProduct.getSku()), eq(10));

        MerchantController.AddInventoryRequest request = new MerchantController.AddInventoryRequest();
        request.setQuantity(10);

        // When & Then
        mockMvc.perform(post(API_BASE_PATH + "/{merchantId}/products/{sku}/inventory/add", 1L, testProduct.getSku())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());

        verify(merchantService).merchantExists(1L);
        verify(productService).getProductBySku(testProduct.getSku());
        verify(productService).addProductInventory(eq(testProduct.getSku()), eq(10));
    }

    @Test
    void reduceInventory_ServiceException() throws Exception {
        // Given
        when(merchantService.merchantExists(1L)).thenReturn(true);
        when(productService.getProductBySku(testProduct.getSku())).thenReturn(testProduct);
        doThrow(new RuntimeException("Database error")).when(productService).reduceInventory(eq(testProduct.getSku()), eq(10));

        MerchantController.ReduceInventoryRequest request = new MerchantController.ReduceInventoryRequest();
        request.setQuantity(10);

        // When & Then
        mockMvc.perform(post(API_BASE_PATH + "/{merchantId}/products/{sku}/inventory/reduce", 1L, testProduct.getSku())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());

        verify(merchantService).merchantExists(1L);
        verify(productService).getProductBySku(testProduct.getSku());
        verify(productService).reduceInventory(eq(testProduct.getSku()), eq(10));
    }

    @Test
    void setInventory_ServiceException() throws Exception {
        // Given
        when(merchantService.merchantExists(1L)).thenReturn(true);
        when(productService.getProductBySku(testProduct.getSku())).thenReturn(testProduct);
        doThrow(new RuntimeException("Database error")).when(productService).setInventory(eq(testProduct.getSku()), eq(50));

        MerchantController.SetInventoryRequest request = new MerchantController.SetInventoryRequest();
        request.setQuantity(50);

        // When & Then
        mockMvc.perform(put(API_BASE_PATH + "/{merchantId}/products/{sku}/inventory", 1L, testProduct.getSku())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());

        verify(merchantService).merchantExists(1L);
        verify(productService).getProductBySku(testProduct.getSku());
        verify(productService).setInventory(eq(testProduct.getSku()), eq(50));
    }

    @Test
    void createProduct_ServiceException() throws Exception {
        // Given
        when(merchantService.merchantExists(1L)).thenReturn(true);
        when(productService.createProduct(eq("TEST123"), eq("Test Product"), eq("Test Description"), 
                eq(Money.of("99.99", "CNY")), eq(1L), eq(100))).thenThrow(new RuntimeException("Database error"));

        MerchantController.CreateProductRequest request = new MerchantController.CreateProductRequest();
        request.setSku("TEST123");
        request.setName("Test Product");
        request.setDescription("Test Description");
        request.setPrice(new BigDecimal("99.99"));
        request.setCurrency("CNY");
        request.setInitialInventory(100);

        // When & Then
        mockMvc.perform(post(API_BASE_PATH + "/{merchantId}/products", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());

        verify(merchantService).merchantExists(1L);
        verify(productService).createProduct(eq("TEST123"), eq("Test Product"), eq("Test Description"), 
                eq(Money.of("99.99", "CNY")), eq(1L), eq(100));
    }

    @Test
    void getMerchantIncome_MerchantNotFound() throws Exception {
        // Given
        when(merchantService.getMerchantBalance(999L)).thenThrow(new RuntimeException("Merchant not found"));

        // When & Then
        mockMvc.perform(get(API_BASE_PATH + "/{merchantId}/income", 999L))
                .andExpect(status().isNotFound());

        verify(merchantService).getMerchantBalance(999L);
        verify(merchantService, never()).getMerchantTotalIncome(anyLong());
    }

    @Test
    void getMerchantProducts_ServiceException() throws Exception {
        // Given
        when(merchantService.merchantExists(1L)).thenReturn(true);
        when(productService.getProductsByMerchant(1L)).thenThrow(new RuntimeException("Database error"));

        // When & Then
        mockMvc.perform(get(API_BASE_PATH + "/{merchantId}/products", 1L))
                .andExpect(status().isInternalServerError());

        verify(merchantService).merchantExists(1L);
        verify(productService).getProductsByMerchant(1L);
    }

    @Test
    void createMerchant_InvalidJson() throws Exception {
        // When & Then
        mockMvc.perform(post(API_BASE_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content("invalid json"))
                .andExpect(status().isBadRequest());

        verify(merchantService, never()).createMerchant(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void createProduct_InvalidJson() throws Exception {
        // When & Then
        mockMvc.perform(post(API_BASE_PATH + "/{merchantId}/products", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content("invalid json"))
                .andExpect(status().isBadRequest());

        verify(productService, never()).createProduct(anyString(), anyString(), anyString(), any(), anyLong(), anyInt());
    }

    @Test
    void addInventory_InvalidJson() throws Exception {
        // When & Then
        mockMvc.perform(post(API_BASE_PATH + "/{merchantId}/products/{sku}/inventory/add", 1L, testProduct.getSku())
                .contentType(MediaType.APPLICATION_JSON)
                .content("invalid json"))
                .andExpect(status().isBadRequest());

        verify(productService, never()).addProductInventory(anyString(), anyInt());
    }

    @Test
    void reduceInventory_InvalidJson() throws Exception {
        // When & Then
        mockMvc.perform(post(API_BASE_PATH + "/{merchantId}/products/{sku}/inventory/reduce", 1L, testProduct.getSku())
                .contentType(MediaType.APPLICATION_JSON)
                .content("invalid json"))
                .andExpect(status().isBadRequest());

        verify(productService, never()).reduceInventory(anyString(), anyInt());
    }

    @Test
    void setInventory_InvalidJson() throws Exception {
        // When & Then
        mockMvc.perform(put(API_BASE_PATH + "/{merchantId}/products/{sku}/inventory", 1L, testProduct.getSku())
                .contentType(MediaType.APPLICATION_JSON)
                .content("invalid json"))
                .andExpect(status().isBadRequest());

        verify(productService, never()).setInventory(anyString(), anyInt());
    }
}
