package com.ecommerce.api.controller;

import com.ecommerce.application.service.EcommerceService;
import com.ecommerce.application.service.ProductService;
import com.ecommerce.application.dto.PurchaseRequest;
import com.ecommerce.application.dto.PurchaseResponse;
import com.ecommerce.api.dto.Result;
import com.ecommerce.domain.product.Product;
import com.ecommerce.api.annotation.ApiVersion;
import com.ecommerce.api.annotation.ApiTimeout;
import com.ecommerce.api.config.ApiVersionConfig;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import com.ecommerce.api.dto.ErrorResponse;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.concurrent.TimeUnit;

/**
 * Ecommerce Controller (API v1)
 * Handles product purchasing, order management, and product browsing functionality
 */
@RestController
@RequestMapping(ApiVersionConfig.API_V1 + "/ecommerce")
@ApiVersion(value = "v1", since = "2025-07-11")
@Tag(name = "Product & Purchase", description = "Product browsing, purchasing and order management")
public class EcommerceController {
    
    private static final Logger logger = LoggerFactory.getLogger(EcommerceController.class);
    
    private final EcommerceService ecommerceService;
    private final ProductService productService;
    
    public EcommerceController(EcommerceService ecommerceService, ProductService productService) {
        this.ecommerceService = ecommerceService;
        this.productService = productService;
    }
    
    /**
     * Purchase Product (API v1)
     * POST /api/v1/ecommerce/purchase
     */
    @PostMapping("/purchase")
    @ApiTimeout(value = 10, unit = TimeUnit.SECONDS, message = "Purchase operation timeout")
    @Operation(summary = "Purchase Product", description = "Process product purchase with inventory and payment validation")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Purchase completed successfully",
                    content = @Content(mediaType = "application/json", 
                                     schema = @Schema(implementation = Result.class),
                                     examples = @ExampleObject(
                                         name = "Success Response",
                                         value = "{\"code\":\"SUCCESS\",\"message\":\"Purchase completed successfully\",\"data\":{\"orderNumber\":\"ORD-2025-001\",\"userId\":1,\"merchantId\":1,\"productSku\":\"PHONE-001\",\"quantity\":1,\"totalAmount\":999.00,\"currency\":\"CNY\",\"orderStatus\":\"COMPLETED\"},\"timestamp\":\"2025-07-11T12:00:00\"}"))),
        @ApiResponse(responseCode = "400", description = "Invalid request data or business validation failed",
                    content = @Content(mediaType = "application/json", 
                                     schema = @Schema(implementation = ErrorResponse.class),
                                     examples = @ExampleObject(
                                         name = "Insufficient Balance",
                                         value = "{\"code\":\"INSUFFICIENT_BALANCE\",\"message\":\"Insufficient user balance\",\"data\":null,\"timestamp\":\"2025-07-11T12:00:00\"}"))),
        @ApiResponse(responseCode = "404", description = "User, product, or merchant not found",
                    content = @Content(mediaType = "application/json", 
                                     schema = @Schema(implementation = ErrorResponse.class),
                                     examples = @ExampleObject(
                                         name = "Product Not Found",
                                         value = "{\"code\":\"RESOURCE_NOT_FOUND\",\"message\":\"Product not found\",\"data\":null,\"timestamp\":\"2025-07-11T12:00:00\"}")))
    })
    public ResponseEntity<Result<PurchaseResponse>> purchaseProduct(@Valid @RequestBody PurchaseRequest request) {
        logger.info("Processing purchase request: {}", request);
        
        // Process purchase - exceptions will be handled by GlobalExceptionHandler
        PurchaseResponse response = ecommerceService.processPurchase(request);
        
        logger.info("Purchase completed successfully: {}", response.getOrderNumber());
        return ResponseEntity.ok(Result.successWithMessage("Purchase completed successfully", response));
    }
    
    /**
     * Cancel Order (API v1)
     * POST /api/v1/ecommerce/orders/{orderNumber}/cancel
     */
    @PostMapping("/orders/{orderNumber}/cancel")
    @Operation(summary = "Cancel Order", description = "Cancel an existing order with reason")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Order cancelled successfully",
                    content = @Content(mediaType = "application/json", 
                                     schema = @Schema(implementation = Result.class),
                                     examples = @ExampleObject(
                                         name = "Success Response",
                                         value = "{\"code\":\"SUCCESS\",\"message\":\"Order cancelled successfully\",\"data\":{\"orderNumber\":\"ORD-2025-001\",\"reason\":\"Customer request\"},\"timestamp\":\"2025-07-11T12:00:00\"}"))),
        @ApiResponse(responseCode = "400", description = "Invalid request data",
                    content = @Content(mediaType = "application/json", 
                                     schema = @Schema(implementation = ErrorResponse.class),
                                     examples = @ExampleObject(
                                         name = "Validation Error",
                                         value = "{\"code\":\"VALIDATION_ERROR\",\"message\":\"Reason is required\",\"data\":null,\"timestamp\":\"2025-07-11T12:00:00\"}"))),
        @ApiResponse(responseCode = "404", description = "Order not found",
                    content = @Content(mediaType = "application/json", 
                                     schema = @Schema(implementation = ErrorResponse.class),
                                     examples = @ExampleObject(
                                         name = "Order Not Found",
                                         value = "{\"code\":\"RESOURCE_NOT_FOUND\",\"message\":\"Order not found\",\"data\":null,\"timestamp\":\"2025-07-11T12:00:00\"}")))
    })
    public ResponseEntity<Result<Map<String, Object>>> cancelOrder(
            @Parameter(description = "Order number", required = true, example = "ORD-2025-001")
            @PathVariable String orderNumber, 
            @Valid @RequestBody CancelOrderRequest request) {
        logger.info("Cancelling order: {} with reason: {}", orderNumber, request.getReason());
        
        ecommerceService.cancelOrder(orderNumber, request.getReason());
        
        logger.info("Order cancelled successfully: {}", orderNumber);
        
        Map<String, Object> response = new HashMap<>();
        response.put("orderNumber", orderNumber);
        response.put("reason", request.getReason());
        
        return ResponseEntity.ok(Result.successWithMessage("Order cancelled successfully", response));
    }

    /**
     * Get Product Details (API v1)
     * GET /api/v1/ecommerce/products/{sku}
     */
    @GetMapping("/products/{sku}")
    @Operation(summary = "Get Product Details", description = "Retrieve detailed product information by SKU")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Product details retrieved successfully",
                    content = @Content(mediaType = "application/json", 
                                     schema = @Schema(implementation = Result.class),
                                     examples = @ExampleObject(
                                         name = "Success Response",
                                         value = "{\"code\":\"SUCCESS\",\"message\":\"Operation completed successfully\",\"data\":{\"id\":1,\"sku\":\"PHONE-001\",\"name\":\"iPhone 15 Pro\",\"description\":\"Latest iPhone with advanced features\",\"price\":999.00,\"currency\":\"CNY\",\"merchantId\":1,\"availableInventory\":100,\"status\":\"ACTIVE\",\"available\":true},\"timestamp\":\"2025-07-11T12:00:00\"}"))),
        @ApiResponse(responseCode = "404", description = "Product not found",
                    content = @Content(mediaType = "application/json", 
                                     schema = @Schema(implementation = ErrorResponse.class),
                                     examples = @ExampleObject(
                                         name = "Product Not Found",
                                         value = "{\"code\":\"RESOURCE_NOT_FOUND\",\"message\":\"Product not found\",\"data\":null,\"timestamp\":\"2025-07-11T12:00:00\"}")))
    })
    public ResponseEntity<Result<ProductDetailResponse>> getProductBySku(
            @Parameter(description = "Product SKU", required = true, example = "PHONE-001")
            @PathVariable String sku) {
        logger.info("Getting product details for SKU: {}", sku);
        
        Product product = productService.getProductBySku(sku);
        
        ProductDetailResponse response = new ProductDetailResponse(
            product.getId(),
            product.getSku(),
            product.getName(),
            product.getDescription(),
            product.getPrice().getAmount(),
            product.getPrice().getCurrency(),
            product.getMerchantId(),
            product.getAvailableInventory(),
            product.getStatus().toString(),
            product.isAvailable()
        );
        
        return ResponseEntity.ok(Result.success(response));
    }
    
    /**
     * Get Available Products (API v1)
     * GET /api/v1/ecommerce/products
     * 
     * This endpoint is for public product browsing and purchasing.
     * It only returns AVAILABLE products (active and have inventory).
     * 
     * Supports:
     * - Global search: ?search=iPhone
     * - Merchant filtering: ?merchantId=1  
     * - Combined search: ?search=iPhone&merchantId=1
     * 
     * For merchant product management, use /api/v1/merchants/{merchantId}/products instead.
     */
    @GetMapping("/products")
    @Operation(summary = "Get Available Products", description = "Retrieve all available products with optional search and merchant filtering")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Products retrieved successfully")
    })
    public ResponseEntity<Result<ProductListResponse>> getAvailableProducts(
            @Parameter(description = "Search term for product name/description", required = false, example = "iPhone")
            @RequestParam(value = "search", required = false) String searchTerm,
            @Parameter(description = "Filter by merchant ID", required = false, example = "1")
            @RequestParam(value = "merchantId", required = false) Long merchantId) {
        logger.info("Getting available products with search: {}, merchantId: {}", searchTerm, merchantId);
        
        List<Product> products;
        
        // Apply search and merchant filtering logic
        if (searchTerm != null && !searchTerm.trim().isEmpty() && merchantId != null) {
            // Combined search: search within specific merchant's products
            products = productService.getProductsByMerchant(merchantId)
                .stream()
                .filter(Product::isAvailable)
                .filter(product -> product.getName().toLowerCase().contains(searchTerm.toLowerCase()) ||
                                 product.getDescription().toLowerCase().contains(searchTerm.toLowerCase()))
                .collect(Collectors.toList());
        } else if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            // Global search: search across all merchants
            products = productService.searchAvailableProducts(searchTerm);
        } else if (merchantId != null) {
            // Merchant filter: get all available products from specific merchant
            products = productService.getProductsByMerchant(merchantId)
                .stream()
                .filter(Product::isAvailable)
                .collect(Collectors.toList());
        } else {
            // No filters: get all available products
            products = productService.getAllAvailableProducts();
        }

        List<ProductSummaryResponse> productSummaries = products.stream()
            .map(product -> new ProductSummaryResponse(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getPrice().getAmount(),
                product.getPrice().getCurrency(),
                product.getMerchantId(),
                product.getAvailableInventory(),
                product.isAvailable()
            ))
            .collect(Collectors.toList());
        
        ProductListResponse response = new ProductListResponse(
            productSummaries,
            productSummaries.size(),
            searchTerm,
            merchantId
        );
        
        return ResponseEntity.ok(Result.success(response));
    }
    
    /**
     * Get Product Inventory (API v1)
     * GET /api/v1/ecommerce/products/{sku}/inventory
     */
    @GetMapping("/products/{sku}/inventory")
    @Operation(summary = "Get Product Inventory", description = "Check inventory status for a specific product")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Inventory information retrieved successfully",
                    content = @Content(mediaType = "application/json", 
                                     schema = @Schema(implementation = Result.class),
                                     examples = @ExampleObject(
                                         name = "Success Response",
                                         value = "{\"code\":\"SUCCESS\",\"message\":\"Operation completed successfully\",\"data\":{\"sku\":\"PHONE-001\",\"productName\":\"iPhone 15 Pro\",\"availableInventory\":100,\"available\":true,\"status\":\"ACTIVE\"},\"timestamp\":\"2025-07-11T12:00:00\"}"))),
        @ApiResponse(responseCode = "404", description = "Product not found",
                    content = @Content(mediaType = "application/json", 
                                     schema = @Schema(implementation = ErrorResponse.class),
                                     examples = @ExampleObject(
                                         name = "Product Not Found",
                                         value = "{\"code\":\"RESOURCE_NOT_FOUND\",\"message\":\"Product not found\",\"data\":null,\"timestamp\":\"2025-07-11T12:00:00\"}")))
    })
    public ResponseEntity<Result<InventoryResponse>> getProductInventory(
            @Parameter(description = "Product SKU", required = true, example = "PHONE-001")
            @PathVariable String sku) {
        logger.info("Checking inventory for product: {}", sku);
        
        Product product = productService.getProductBySku(sku);
        
        InventoryResponse response = new InventoryResponse(
            product.getSku(),
            product.getName(),
            product.getAvailableInventory(),
            product.isAvailable(),
            product.getStatus().toString()
        );
        
        return ResponseEntity.ok(Result.success(response));
    }

    
    // DTO classes for product queries
    @Schema(description = "Product detail response")
    public static class ProductDetailResponse {
        @Schema(description = "Product ID", example = "1")
        private Long id;
        
        @Schema(description = "Product SKU", example = "PHONE-001")
        private String sku;
        
        @Schema(description = "Product name", example = "iPhone 15 Pro")
        private String name;
        
        @Schema(description = "Product description", example = "Latest iPhone with advanced features")
        private String description;
        
        @Schema(description = "Product price", example = "999.00")
        private BigDecimal price;
        
        @Schema(description = "Currency code", example = "CNY")
        private String currency;
        
        @Schema(description = "Merchant ID", example = "1")
        private Long merchantId;
        
        @Schema(description = "Available inventory", example = "100")
        private int availableInventory;
        
        @Schema(description = "Product status", example = "ACTIVE")
        private String status;
        
        @Schema(description = "Product availability", example = "true")
        private boolean available;
        
        public ProductDetailResponse(Long id, String sku, String name, String description,
                                   BigDecimal price, String currency, Long merchantId,
                                   int availableInventory, String status, boolean available) {
            this.id = id;
            this.sku = sku;
            this.name = name;
            this.description = description;
            this.price = price;
            this.currency = currency;
            this.merchantId = merchantId;
            this.availableInventory = availableInventory;
            this.status = status;
            this.available = available;
        }
        
        // Getters
        public Long getId() { return id; }
        public String getSku() { return sku; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public BigDecimal getPrice() { return price; }
        public String getCurrency() { return currency; }
        public Long getMerchantId() { return merchantId; }
        public int getAvailableInventory() { return availableInventory; }
        public String getStatus() { return status; }
        public boolean isAvailable() { return available; }
    }
    
    @Schema(description = "Product summary response")
    public static class ProductSummaryResponse {
        @Schema(description = "Product ID", example = "1")
        private Long id;
        @Schema(description = "Product SKU", example = "PHONE-001")
        private String sku;
        @Schema(description = "Product name", example = "iPhone 15 Pro")
        private String name;
        @Schema(description = "Product price", example = "999.00")
        private BigDecimal price;
        @Schema(description = "Currency code", example = "CNY")
        private String currency;
        @Schema(description = "Merchant ID", example = "1")
        private Long merchantId;
        @Schema(description = "Available inventory", example = "100")
        private int availableInventory;
        @Schema(description = "Product availability", example = "true")
        private boolean available;
        
        public ProductSummaryResponse(Long id, String sku, String name, BigDecimal price,
                                    String currency, Long merchantId, int availableInventory, boolean available) {
            this.id = id;
            this.sku = sku;
            this.name = name;
            this.price = price;
            this.currency = currency;
            this.merchantId = merchantId;
            this.availableInventory = availableInventory;
            this.available = available;
        }
        
        // Getters
        public Long getId() { return id; }
        public String getSku() { return sku; }
        public String getName() { return name; }
        public BigDecimal getPrice() { return price; }
        public String getCurrency() { return currency; }
        public Long getMerchantId() { return merchantId; }
        public int getAvailableInventory() { return availableInventory; }
        public boolean isAvailable() { return available; }
    }
    
    @Schema(description = "Product list response")
    public static class ProductListResponse {
        @Schema(description = "List of products")
        private List<ProductSummaryResponse> products;
        @Schema(description = "Total product count", example = "10")
        private int totalCount;
        @Schema(description = "Search term applied", example = "iPhone")
        private String searchTerm;
        @Schema(description = "Merchant ID filter applied", example = "1")
        private Long merchantId;
        
        public ProductListResponse(List<ProductSummaryResponse> products, int totalCount,
                                 String searchTerm, Long merchantId) {
            this.products = products;
            this.totalCount = totalCount;
            this.searchTerm = searchTerm;
            this.merchantId = merchantId;
        }
        
        // Getters
        public List<ProductSummaryResponse> getProducts() { return products; }
        public int getTotalCount() { return totalCount; }
        public String getSearchTerm() { return searchTerm; }
        public Long getMerchantId() { return merchantId; }
    }
    
    @Schema(description = "Product inventory response")
    public static class InventoryResponse {
        @Schema(description = "Product SKU", example = "PHONE-001")
        private String sku;
        @Schema(description = "Product name", example = "iPhone 15 Pro")
        private String productName;
        @Schema(description = "Available inventory", example = "100")
        private int availableInventory;
        @Schema(description = "Product availability", example = "true")
        private boolean available;
        @Schema(description = "Product status", example = "ACTIVE")
        private String status;
        
        public InventoryResponse(String sku, String productName, int availableInventory,
                           boolean available, String status) {
            this.sku = sku;
            this.productName = productName;
            this.availableInventory = availableInventory;
            this.available = available;
            this.status = status;
        }
        
        // Getters
        public String getSku() { return sku; }
        public String getProductName() { return productName; }
        public int getAvailableInventory() { return availableInventory; }
        public boolean isAvailable() { return available; }
        public String getStatus() { return status; }
    }
    
    @Schema(description = "Cancel order request")
    public static class CancelOrderRequest {
        @Schema(description = "Cancellation reason", example = "Customer request", required = true)
        @NotBlank(message = "Reason is required")
        private String reason;
        
        public CancelOrderRequest() {}
        
        public CancelOrderRequest(String reason) {
            this.reason = reason;
        }
        
        // Getters and Setters
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }
} 