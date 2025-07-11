package com.ecommerce.api.controller;

import com.ecommerce.application.service.MerchantService;
import com.ecommerce.application.service.ProductService;
import com.ecommerce.application.service.SettlementService;
import com.ecommerce.domain.merchant.MerchantNotFoundException;
import com.ecommerce.domain.product.Product;
import com.ecommerce.domain.product.ProductStatus;
import com.ecommerce.domain.Money;
import com.ecommerce.api.dto.Result;
import com.ecommerce.api.annotation.ApiVersion;
import com.ecommerce.api.config.ApiVersionConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import com.ecommerce.api.dto.ErrorResponse;
import com.ecommerce.domain.settlement.Settlement;

/**
 * Merchant Controller (API v1)
 * Handles merchant registration, product management, and income tracking
 */
@RestController
@RequestMapping(ApiVersionConfig.API_V1 + "/merchants")
@ApiVersion(value = "v1", since = "2025-07-11")
@Tag(name = "Merchant Management", description = "Merchant registration, product management and income tracking")
public class MerchantController {
    
    private static final Logger logger = LoggerFactory.getLogger(MerchantController.class);
    
    private final MerchantService merchantService;
    private final ProductService productService;
    private final SettlementService settlementService;
    
    public MerchantController(MerchantService merchantService, ProductService productService, SettlementService settlementService) {
        this.merchantService = merchantService;
        this.productService = productService;
        this.settlementService = settlementService;
    }
    
    /**
     * Create Merchant (API v1)
     * POST /api/v1/merchants
     */
    @PostMapping
    @Operation(summary = "Create Merchant", description = "Register a new merchant with business information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Merchant created successfully",
                    content = @Content(mediaType = "application/json", 
                                     schema = @Schema(implementation = Result.class),
                                     examples = @ExampleObject(
                                         name = "Success Response",
                                         value = "{\"code\":\"SUCCESS\",\"message\":\"Merchant created successfully\",\"data\":{\"id\":1,\"merchantName\":\"Apple Store\",\"businessLicense\":\"BL123456789\",\"contactEmail\":\"contact@applestore.com\",\"contactPhone\":\"13800138000\",\"balance\":0.00,\"currency\":\"CNY\",\"totalIncome\":0.00,\"status\":\"ACTIVE\"},\"timestamp\":\"2025-07-11T12:00:00\"}"))),
        @ApiResponse(responseCode = "400", description = "Invalid request data",
                    content = @Content(mediaType = "application/json", 
                                     schema = @Schema(implementation = ErrorResponse.class),
                                     examples = @ExampleObject(
                                         name = "Validation Error",
                                         value = "{\"code\":\"VALIDATION_ERROR\",\"message\":\"Merchant name is required\",\"data\":null,\"timestamp\":\"2025-07-11T12:00:00\"}")))
    })
    public ResponseEntity<Result<MerchantResponse>> createMerchant(@Valid @RequestBody CreateMerchantRequest request) {
        logger.info("Creating merchant with name: {}", request.getMerchantName());
        
        var merchant = merchantService.createMerchant(
            request.getMerchantName(),
            request.getBusinessLicense(),
            request.getContactEmail(),
            request.getContactPhone()
        );
        
        Money balance = merchantService.getMerchantBalance(merchant.getId());
        Money totalIncome = merchantService.getMerchantTotalIncome(merchant.getId());
        
        MerchantResponse response = new MerchantResponse(
            merchant.getId(),
            merchant.getMerchantName(),
            merchant.getBusinessLicense(),
            merchant.getContactEmail(),
            merchant.getContactPhone(),
            balance.getAmount(),
            balance.getCurrency(),
            totalIncome.getAmount(),
            merchant.getStatus().toString()
        );
        
        logger.info("Merchant created successfully with id: {}", merchant.getId());
        return ResponseEntity.ok(Result.successWithMessage("Merchant created successfully", response));
    }
    
    /**
     * Create Product (API v1)
     * POST /api/v1/merchants/{merchantId}/products
     */
    @PostMapping("/{merchantId}/products")
    @Operation(summary = "Create Product", description = "Create a new product for a specific merchant")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Product created successfully",
                    content = @Content(mediaType = "application/json", 
                                     schema = @Schema(implementation = Result.class),
                                     examples = @ExampleObject(
                                         name = "Success Response",
                                         value = "{\"code\":\"SUCCESS\",\"message\":\"Product created successfully\",\"data\":{\"id\":1,\"sku\":\"PHONE-001\",\"name\":\"iPhone 15 Pro\",\"description\":\"Latest iPhone with advanced features\",\"price\":999.00,\"currency\":\"CNY\",\"merchantId\":1,\"availableInventory\":100,\"status\":\"ACTIVE\"},\"timestamp\":\"2025-07-11T12:00:00\"}"))),
        @ApiResponse(responseCode = "400", description = "Invalid request data",
                    content = @Content(mediaType = "application/json", 
                                     schema = @Schema(implementation = ErrorResponse.class),
                                     examples = @ExampleObject(
                                         name = "Validation Error",
                                         value = "{\"code\":\"VALIDATION_ERROR\",\"message\":\"SKU is required\",\"data\":null,\"timestamp\":\"2025-07-11T12:00:00\"}"))),
        @ApiResponse(responseCode = "404", description = "Merchant not found",
                    content = @Content(mediaType = "application/json", 
                                     schema = @Schema(implementation = ErrorResponse.class),
                                     examples = @ExampleObject(
                                         name = "Merchant Not Found",
                                         value = "{\"code\":\"MERCHANT_NOT_FOUND\",\"message\":\"Merchant not found\",\"data\":null,\"timestamp\":\"2025-07-11T12:00:00\"}")))
    })
    public ResponseEntity<Result<ProductResponse>> createProduct(
            @Parameter(description = "Merchant ID", required = true, example = "1")
            @PathVariable Long merchantId,
            @Valid @RequestBody CreateProductRequest request) {
        logger.info("Creating product for merchant {}: {}", merchantId, request.getSku());
        
        // Validate merchant exists
        if (!merchantService.merchantExists(merchantId)) {
            throw new MerchantNotFoundException(merchantId);
        }
        
        Product product = productService.createProduct(
            request.getSku(), 
            request.getName(), 
            request.getDescription(),
            Money.of(request.getPrice(), request.getCurrency()),
            merchantId,
            request.getInitialInventory()
        );
        
        ProductResponse response = new ProductResponse(
            product.getId(),
            product.getSku(),
            product.getName(),
            product.getDescription(),
            product.getPrice().getAmount(),
            product.getPrice().getCurrency(),
            product.getMerchantId(),
            product.getAvailableInventory(),
            product.getStatus().toString()
        );
        
        logger.info("Product created successfully: {}", product.getSku());
        return ResponseEntity.ok(Result.successWithMessage("Product created successfully", response));
    }
    
    /**
     * Add Product Inventory (API v1)
     * POST /api/v1/merchants/{merchantId}/products/{sku}/inventory/add
     */
    @PostMapping("/{merchantId}/products/{sku}/inventory/add")
    @Operation(summary = "Add Product Inventory", description = "Add inventory quantity to a specific product")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Inventory added successfully",
                    content = @Content(mediaType = "application/json", 
                                     schema = @Schema(implementation = Result.class),
                                     examples = @ExampleObject(
                                         name = "Success Response",
                                         value = "{\"code\":\"SUCCESS\",\"message\":\"Inventory added successfully\",\"data\":{\"sku\":\"PHONE-001\",\"productName\":\"iPhone 15 Pro\",\"availableInventory\":110,\"available\":true},\"timestamp\":\"2025-07-11T12:00:00\"}"))),
        @ApiResponse(responseCode = "400", description = "Invalid request data",
                    content = @Content(mediaType = "application/json", 
                                     schema = @Schema(implementation = ErrorResponse.class),
                                     examples = @ExampleObject(
                                         name = "Validation Error",
                                         value = "{\"code\":\"VALIDATION_ERROR\",\"message\":\"Quantity must be positive\",\"data\":null,\"timestamp\":\"2025-07-11T12:00:00\"}"))),
        @ApiResponse(responseCode = "404", description = "Merchant or product not found",
                    content = @Content(mediaType = "application/json", 
                                     schema = @Schema(implementation = ErrorResponse.class),
                                     examples = @ExampleObject(
                                         name = "Product Not Found",
                                         value = "{\"code\":\"RESOURCE_NOT_FOUND\",\"message\":\"Product not found\",\"data\":null,\"timestamp\":\"2025-07-11T12:00:00\"}")))
    })
    public ResponseEntity<Result<InventoryResponse>> addProductInventory(
            @Parameter(description = "Merchant ID", required = true, example = "1")
            @PathVariable Long merchantId,
            @Parameter(description = "Product SKU", required = true, example = "PHONE-001")
            @PathVariable String sku,
            @Valid @RequestBody AddInventoryRequest request) {
        logger.info("Adding inventory for merchant {}, product {}: quantity={}", merchantId, sku, request.getQuantity());
        
        validateMerchantAndProduct(merchantId, sku);
        
        productService.addProductInventory(sku, request.getQuantity());
        Product product = productService.getProductBySku(sku);
        
        InventoryResponse response = new InventoryResponse(
            product.getSku(),
            product.getName(),
            product.getAvailableInventory(),
            product.isAvailable()
        );
        
        logger.info("Inventory added successfully for product {}: quantity={}", sku, request.getQuantity());
        return ResponseEntity.ok(Result.successWithMessage("Inventory added successfully", response));
    }
    
    /**
     * Reduce Product Inventory (API v1)
     * POST /api/v1/merchants/{merchantId}/products/{sku}/inventory/reduce
     */
    @PostMapping("/{merchantId}/products/{sku}/inventory/reduce")
    @Operation(summary = "Reduce Product Inventory", description = "Reduce inventory quantity for a specific product")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Inventory reduced successfully",
                    content = @Content(mediaType = "application/json", 
                                     schema = @Schema(implementation = Result.class),
                                     examples = @ExampleObject(
                                         name = "Success Response",
                                         value = "{\"code\":\"SUCCESS\",\"message\":\"Operation completed successfully\",\"data\":{\"sku\":\"PHONE-001\",\"productName\":\"iPhone 15 Pro\",\"availableInventory\":95,\"available\":true},\"timestamp\":\"2025-07-11T12:00:00\"}"))),
        @ApiResponse(responseCode = "400", description = "Invalid request data or insufficient inventory",
                    content = @Content(mediaType = "application/json", 
                                     schema = @Schema(implementation = ErrorResponse.class),
                                     examples = @ExampleObject(
                                         name = "Insufficient Inventory",
                                         value = "{\"code\":\"INSUFFICIENT_INVENTORY\",\"message\":\"Insufficient product inventory\",\"data\":null,\"timestamp\":\"2025-07-11T12:00:00\"}"))),
        @ApiResponse(responseCode = "404", description = "Merchant or product not found",
                    content = @Content(mediaType = "application/json", 
                                     schema = @Schema(implementation = ErrorResponse.class),
                                     examples = @ExampleObject(
                                         name = "Product Not Found",
                                         value = "{\"code\":\"RESOURCE_NOT_FOUND\",\"message\":\"Product not found\",\"data\":null,\"timestamp\":\"2025-07-11T12:00:00\"}")))
    })
    public ResponseEntity<Result<InventoryResponse>> reduceProductInventory(
            @Parameter(description = "Merchant ID", required = true, example = "1")
            @PathVariable Long merchantId,
            @Parameter(description = "Product SKU", required = true, example = "PHONE-001")
            @PathVariable String sku,
            @Valid @RequestBody ReduceInventoryRequest request) {
        logger.info("Reducing inventory for merchant {}, product {}: quantity={}", merchantId, sku, request.getQuantity());
        
        validateMerchantAndProduct(merchantId, sku);
        
        productService.reduceInventory(sku, request.getQuantity());
        Product product = productService.getProductBySku(sku);
        
        InventoryResponse response = new InventoryResponse(
            product.getSku(),
            product.getName(),
            product.getAvailableInventory(),
            product.isAvailable()
        );
        
        logger.info("Inventory reduced successfully for product {}: quantity={}", sku, request.getQuantity());
        return ResponseEntity.ok(Result.success(response));
    }
    
    /**
     * Set Product Inventory (API v1)
     * PUT /api/v1/merchants/{merchantId}/products/{sku}/inventory
     */
    @PutMapping("/{merchantId}/products/{sku}/inventory")
    @Operation(summary = "Set Product Inventory", description = "Set inventory quantity to absolute value for a specific product")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Inventory set successfully",
                    content = @Content(mediaType = "application/json", 
                                     schema = @Schema(implementation = Result.class),
                                     examples = @ExampleObject(
                                         name = "Success Response",
                                         value = "{\"code\":\"SUCCESS\",\"message\":\"Operation completed successfully\",\"data\":{\"sku\":\"PHONE-001\",\"productName\":\"iPhone 15 Pro\",\"availableInventory\":50,\"available\":true},\"timestamp\":\"2025-07-11T12:00:00\"}"))),
        @ApiResponse(responseCode = "400", description = "Invalid request data",
                    content = @Content(mediaType = "application/json", 
                                     schema = @Schema(implementation = ErrorResponse.class),
                                     examples = @ExampleObject(
                                         name = "Validation Error",
                                         value = "{\"code\":\"VALIDATION_ERROR\",\"message\":\"Quantity cannot be negative\",\"data\":null,\"timestamp\":\"2025-07-11T12:00:00\"}"))),
        @ApiResponse(responseCode = "404", description = "Merchant or product not found",
                    content = @Content(mediaType = "application/json", 
                                     schema = @Schema(implementation = ErrorResponse.class),
                                     examples = @ExampleObject(
                                         name = "Product Not Found",
                                         value = "{\"code\":\"RESOURCE_NOT_FOUND\",\"message\":\"Product not found\",\"data\":null,\"timestamp\":\"2025-07-11T12:00:00\"}")))
    })
    public ResponseEntity<Result<InventoryResponse>> setProductInventory(
            @Parameter(description = "Merchant ID", required = true, example = "1")
            @PathVariable Long merchantId,
            @Parameter(description = "Product SKU", required = true, example = "PHONE-001")
            @PathVariable String sku,
            @Valid @RequestBody SetInventoryRequest request) {
        logger.info("Setting inventory for merchant {}, product {}: quantity={}", merchantId, sku, request.getQuantity());
        
        validateMerchantAndProduct(merchantId, sku);
        
        productService.setInventory(sku, request.getQuantity());
        Product product = productService.getProductBySku(sku);
        
        InventoryResponse response = new InventoryResponse(
            product.getSku(),
            product.getName(),
            product.getAvailableInventory(),
            product.isAvailable()
        );
        
        logger.info("Inventory set successfully for product {}: quantity={}", sku, request.getQuantity());
        return ResponseEntity.ok(Result.success(response));
    }
    
    /**
     * Validates that merchant exists and product belongs to the merchant
     */
    private void validateMerchantAndProduct(Long merchantId, String sku) {
        // Validate merchant exists
        if (!merchantService.merchantExists(merchantId)) {
            throw new MerchantNotFoundException(merchantId);
        }
        // Validate product exists and belongs to this merchant
        Product product = productService.getProductBySku(sku);
        if (!product.getMerchantId().equals(merchantId)) {
            throw new IllegalArgumentException("Product does not belong to this merchant");
        }
    }
    
    /**
     * Get Merchant Income (API v1)
     * GET /api/v1/merchants/{merchantId}/income
     */
    @GetMapping("/{merchantId}/income")
    @Operation(summary = "Get Merchant Income", description = "Retrieve merchant's current balance and total income")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Income information retrieved successfully",
                    content = @Content(mediaType = "application/json", 
                                     schema = @Schema(implementation = Result.class),
                                     examples = @ExampleObject(
                                         name = "Success Response",
                                         value = "{\"code\":\"SUCCESS\",\"message\":\"Operation completed successfully\",\"data\":{\"merchantId\":1,\"currentBalance\":1000.00,\"totalIncome\":5000.00,\"currency\":\"CNY\"},\"timestamp\":\"2025-07-11T12:00:00\"}"))),
        @ApiResponse(responseCode = "404", description = "Merchant not found",
                    content = @Content(mediaType = "application/json", 
                                     schema = @Schema(implementation = ErrorResponse.class),
                                     examples = @ExampleObject(
                                         name = "Merchant Not Found",
                                         value = "{\"code\":\"MERCHANT_NOT_FOUND\",\"message\":\"Merchant not found\",\"data\":null,\"timestamp\":\"2025-07-11T12:00:00\"}")))
    })
    public ResponseEntity<Result<IncomeResponse>> getMerchantIncome(
            @Parameter(description = "Merchant ID", required = true, example = "1")
            @PathVariable Long merchantId) {
        logger.info("Getting income for merchant: {}", merchantId);
        
        Money currentBalance = merchantService.getMerchantBalance(merchantId);
        Money totalIncome = merchantService.getMerchantTotalIncome(merchantId);
        
        IncomeResponse response = new IncomeResponse(
            merchantId,
            currentBalance.getAmount(),
            totalIncome.getAmount(),
            currentBalance.getCurrency()
        );
        
        return ResponseEntity.ok(Result.success(response));
    }
    
    /**
     * Execute Settlement for Merchant (API v1)
     * POST /api/v1/merchants/{merchantId}/settlement
     */
    @PostMapping("/{merchantId}/settlement")
    @Operation(summary = "Execute Settlement", description = "Manually trigger settlement for a specific merchant")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Settlement executed successfully",
                    content = @Content(mediaType = "application/json", 
                                     schema = @Schema(implementation = Result.class),
                                     examples = @ExampleObject(
                                         name = "Success Response",
                                         value = "{\"code\":\"SUCCESS\",\"message\":\"Settlement executed successfully\",\"data\":{\"merchantId\":1,\"settlementDate\":\"2025-07-11\",\"expectedIncome\":1000.00,\"actualBalance\":1000.00,\"difference\":0.00,\"status\":\"COMPLETED\",\"notes\":\"Settlement completed successfully\"},\"timestamp\":\"2025-07-11T12:00:00\"}"))),
        @ApiResponse(responseCode = "404", description = "Merchant not found",
                    content = @Content(mediaType = "application/json", 
                                     schema = @Schema(implementation = ErrorResponse.class),
                                     examples = @ExampleObject(
                                         name = "Merchant Not Found",
                                         value = "{\"code\":\"MERCHANT_NOT_FOUND\",\"message\":\"Merchant not found\",\"data\":null,\"timestamp\":\"2025-07-11T12:00:00\"}")))
    })
    public ResponseEntity<Result<SettlementResponse>> executeSettlement(
            @Parameter(description = "Merchant ID", required = true, example = "1")
            @PathVariable Long merchantId,
            @RequestBody(required = false) SettlementRequest request) {
        logger.info("Executing settlement for merchant: {}", merchantId);
        
        // Use provided date or default to today
        LocalDate settlementDate = (request != null && request.getSettlementDate() != null) 
            ? request.getSettlementDate() 
            : LocalDate.now();
        
        Settlement settlement = settlementService.executeMerchantSettlement(merchantId, settlementDate);
        
        SettlementResponse response = new SettlementResponse(
            settlement.getMerchantId(),
            settlement.getSettlementDate(),
            settlement.getExpectedIncome().getAmount(),
            settlement.getActualBalance().getAmount(),
            settlement.getDifference().getAmount(),
            settlement.getStatus().toString(),
            settlement.getNotes()
        );
        
        logger.info("Settlement completed for merchant {}: expected={}, actual={}, status={}", 
                  merchantId, settlement.getExpectedIncome(), settlement.getActualBalance(), 
                  settlement.getStatus());
        
        return ResponseEntity.ok(Result.successWithMessage("Settlement executed successfully", response));
    }
    
    /**
     * Execute Global Settlement (API v1)
     * POST /api/v1/merchants/settlement/global
     */
    @PostMapping("/settlement/global")
    @Operation(summary = "Execute Global Settlement", description = "Manually trigger settlement for all merchants")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Global settlement executed successfully",
                    content = @Content(mediaType = "application/json", 
                                     schema = @Schema(implementation = Result.class),
                                     examples = @ExampleObject(
                                         name = "Success Response",
                                         value = "{\"code\":\"SUCCESS\",\"message\":\"Operation completed successfully\",\"data\":{\"settlementDate\":\"2025-07-11\",\"settlementTime\":\"2025-07-11T02:00:00\"},\"timestamp\":\"2025-07-11T12:00:00\"}")))
    })
    public ResponseEntity<Result<GlobalSettlementResponse>> executeGlobalSettlement(
            @RequestBody(required = false) GlobalSettlementRequest request) {
        logger.info("Executing global settlement");
        
        // Use provided date or default to today
        LocalDate settlementDate = (request != null && request.getSettlementDate() != null) 
            ? request.getSettlementDate() 
            : LocalDate.now();
        
        // Execute global settlement
        settlementService.executeSettlement();
        
        GlobalSettlementResponse response = new GlobalSettlementResponse(
            settlementDate,
            LocalDateTime.now()
        );
        
        logger.info("Global settlement completed for date: {}", settlementDate);
        
        return ResponseEntity.ok(Result.successWithMessage("Global settlement executed successfully", response));
    }
    
    /**
     * Get Merchant Products (API v1)
     * GET /api/v1/merchants/{merchantId}/products
     * 
     * This endpoint is for merchant product management.
     * It returns ALL products (including inactive/deleted) for management purposes.
     * 
     * Supports:
     * - Status filtering: ?status=ACTIVE
     * - Search within merchant's products: ?search=iPhone
     * - Combined filtering: ?search=iPhone&status=ACTIVE
     * 
     * For public product browsing, use /api/v1/ecommerce/products?merchantId={merchantId} instead.
     */
    @GetMapping("/{merchantId}/products")
    @Operation(summary = "Get Merchant Products", description = "Retrieve all products for a specific merchant with optional filtering")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Products retrieved successfully",
                    content = @Content(mediaType = "application/json", 
                                     schema = @Schema(implementation = Result.class),
                                     examples = @ExampleObject(
                                         name = "Success Response",
                                         value = "{\"code\":\"SUCCESS\",\"message\":\"Operation completed successfully\",\"data\":{\"merchantId\":1,\"products\":[{\"id\":1,\"sku\":\"PHONE-001\",\"name\":\"iPhone 15 Pro\",\"description\":\"Latest iPhone with advanced features\",\"price\":999.00,\"currency\":\"CNY\",\"merchantId\":1,\"availableInventory\":100,\"status\":\"ACTIVE\"}],\"totalCount\":1,\"statusFilter\":\"ACTIVE\",\"searchTerm\":null},\"timestamp\":\"2025-07-11T12:00:00\"}"))),
        @ApiResponse(responseCode = "404", description = "Merchant not found",
                    content = @Content(mediaType = "application/json", 
                                     schema = @Schema(implementation = ErrorResponse.class),
                                     examples = @ExampleObject(
                                         name = "Merchant Not Found",
                                         value = "{\"code\":\"MERCHANT_NOT_FOUND\",\"message\":\"Merchant not found\",\"data\":null,\"timestamp\":\"2025-07-11T12:00:00\"}")))
    })
    public ResponseEntity<Result<MerchantProductListResponse>> getMerchantProducts(
            @Parameter(description = "Merchant ID", required = true, example = "1")
            @PathVariable Long merchantId,
            @Parameter(description = "Product status filter", required = false, example = "ACTIVE")
            @RequestParam(value = "status", required = false) String status,
            @Parameter(description = "Search term for product name/description", required = false, example = "iPhone")
            @RequestParam(value = "search", required = false) String searchTerm) {
        logger.info("Getting products for merchant {}, status: {}, search: {}", merchantId, status, searchTerm);
        
        // Validate merchant exists
        if (!merchantService.merchantExists(merchantId)) {
            throw new MerchantNotFoundException(merchantId);
        }
        
        List<Product> products;
        
        // Get base products by merchant
        products = productService.getProductsByMerchant(merchantId);
        
        // Apply status filter if specified
        if (status != null) {
            ProductStatus productStatus = ProductStatus.valueOf(status.toUpperCase());
            products = products.stream()
                .filter(product -> product.getStatus().equals(productStatus))
                .collect(Collectors.toList());
        }
        
        // Apply search filter if specified (search in name, description, and SKU)
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            String lowerSearchTerm = searchTerm.toLowerCase();
            products = products.stream()
                .filter(product -> product.getName().toLowerCase().contains(lowerSearchTerm) ||
                                 product.getDescription().toLowerCase().contains(lowerSearchTerm) ||
                                 product.getSku().toLowerCase().contains(lowerSearchTerm))
                .collect(Collectors.toList());
        }
        
        List<ProductResponse> productResponses = products.stream()
            .map(product -> new ProductResponse(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getDescription(),
                product.getPrice().getAmount(),
                product.getPrice().getCurrency(),
                product.getMerchantId(),
                product.getAvailableInventory(),
                product.getStatus().toString()
            ))
            .collect(Collectors.toList());
        
        MerchantProductListResponse response = new MerchantProductListResponse(
            merchantId,
            productResponses,
            productResponses.size(),
            status,
            searchTerm
        );
        
        return ResponseEntity.ok(Result.success(response));
    }
    
    /**
     * Get Merchant Product (API v1)
     * GET /api/v1/merchants/{merchantId}/products/{sku}
     */
    @GetMapping("/{merchantId}/products/{sku}")
    @Operation(summary = "Get Merchant Product", description = "Retrieve specific product details for a merchant")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Product retrieved successfully",
                    content = @Content(mediaType = "application/json", 
                                     schema = @Schema(implementation = Result.class),
                                     examples = @ExampleObject(
                                         name = "Success Response",
                                         value = "{\"code\":\"SUCCESS\",\"message\":\"Operation completed successfully\",\"data\":{\"id\":1,\"sku\":\"PHONE-001\",\"name\":\"iPhone 15 Pro\",\"description\":\"Latest iPhone with advanced features\",\"price\":999.00,\"currency\":\"CNY\",\"merchantId\":1,\"availableInventory\":100,\"status\":\"ACTIVE\"},\"timestamp\":\"2025-07-11T12:00:00\"}"))),
        @ApiResponse(responseCode = "404", description = "Merchant or product not found",
                    content = @Content(mediaType = "application/json", 
                                     schema = @Schema(implementation = ErrorResponse.class),
                                     examples = @ExampleObject(
                                         name = "Product Not Found",
                                         value = "{\"code\":\"RESOURCE_NOT_FOUND\",\"message\":\"Product not found\",\"data\":null,\"timestamp\":\"2025-07-11T12:00:00\"}")))
    })
    public ResponseEntity<Result<ProductResponse>> getMerchantProduct(
            @Parameter(description = "Merchant ID", required = true, example = "1")
            @PathVariable Long merchantId,
            @Parameter(description = "Product SKU", required = true, example = "PHONE-001")
            @PathVariable String sku) {
        logger.info("Getting product {} for merchant {}", sku, merchantId);
        
        // Validate merchant exists
        if (!merchantService.merchantExists(merchantId)) {
            throw new MerchantNotFoundException(merchantId);
        }
        
        Product product = productService.getProductBySku(sku);
        
        // Validate product belongs to this merchant
        if (!product.getMerchantId().equals(merchantId)) {
            throw new RuntimeException("Product does not belong to this merchant");
        }
        
        ProductResponse response = new ProductResponse(
            product.getId(),
            product.getSku(),
            product.getName(),
            product.getDescription(),
            product.getPrice().getAmount(),
            product.getPrice().getCurrency(),
            product.getMerchantId(),
            product.getAvailableInventory(),
            product.getStatus().toString()
        );
        
        return ResponseEntity.ok(Result.success(response));
    }
    
    // DTO classes
    @Schema(description = "Merchant creation request")
    public static class CreateMerchantRequest {
        @Schema(description = "Merchant name", example = "Apple Store", required = true)
        @NotBlank(message = "Merchant name is required")
        @Size(min = 2, max = 100, message = "Merchant name must be between 2 and 100 characters")
        private String merchantName;
        
        @Schema(description = "Business license number", example = "BL123456789", required = true)
        @NotBlank(message = "Business license is required")
        @Size(min = 5, max = 50, message = "Business license must be between 5 and 50 characters")
        @Pattern(regexp = "^[A-Z0-9]+$", message = "Business license can only contain uppercase letters and numbers")
        private String businessLicense;
        
        @Schema(description = "Contact email", example = "contact@applestore.com", required = true)
        @NotBlank(message = "Contact email is required")
        @Email(message = "Invalid email format")
        @Size(max = 100, message = "Email must not exceed 100 characters")
        private String contactEmail;
        
        @Schema(description = "Contact phone", example = "13800138000", required = true)
        @NotBlank(message = "Contact phone is required")
        @Pattern(regexp = "^1[3-9]\\d{9}$", message = "Invalid phone number format")
        @Size(max = 20, message = "Phone number must not exceed 20 characters")
        private String contactPhone;
        
        // Getters and Setters
        public String getMerchantName() { return merchantName; }
        public void setMerchantName(String merchantName) { this.merchantName = merchantName; }
        public String getBusinessLicense() { return businessLicense; }
        public void setBusinessLicense(String businessLicense) { this.businessLicense = businessLicense; }
        public String getContactEmail() { return contactEmail; }
        public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }
        public String getContactPhone() { return contactPhone; }
        public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
    }
    
    @Schema(description = "Merchant information response")
    public static class MerchantResponse {
        @Schema(description = "Merchant ID", example = "1")
        private Long id;
        
        @Schema(description = "Merchant name", example = "Apple Store")
        private String merchantName;
        
        @Schema(description = "Business license number", example = "BL123456789")
        private String businessLicense;
        
        @Schema(description = "Contact email", example = "contact@applestore.com")
        private String contactEmail;
        
        @Schema(description = "Contact phone", example = "13800138000")
        private String contactPhone;
        
        @Schema(description = "Current balance", example = "1000.00")
        private BigDecimal balance;
        
        @Schema(description = "Currency code", example = "CNY")
        private String currency;
        
        @Schema(description = "Total income", example = "5000.00")
        private BigDecimal totalIncome;
        
        @Schema(description = "Merchant status", example = "ACTIVE")
        private String status;
        
        public MerchantResponse(Long id, String merchantName, String businessLicense,
                              String contactEmail, String contactPhone, BigDecimal balance,
                              String currency, BigDecimal totalIncome, String status) {
            this.id = id;
            this.merchantName = merchantName;
            this.businessLicense = businessLicense;
            this.contactEmail = contactEmail;
            this.contactPhone = contactPhone;
            this.balance = balance;
            this.currency = currency;
            this.totalIncome = totalIncome;
            this.status = status;
        }
        
        // Getters
        public Long getId() { return id; }
        public String getMerchantName() { return merchantName; }
        public String getBusinessLicense() { return businessLicense; }
        public String getContactEmail() { return contactEmail; }
        public String getContactPhone() { return contactPhone; }
        public BigDecimal getBalance() { return balance; }
        public String getCurrency() { return currency; }
        public BigDecimal getTotalIncome() { return totalIncome; }
        public String getStatus() { return status; }
    }
    
    @Schema(description = "Product creation request")
    public static class CreateProductRequest {
        @Schema(description = "Product SKU", example = "PHONE-001", required = true)
        @NotBlank(message = "SKU is required")
        @Size(min = 3, max = 50, message = "SKU must be between 3 and 50 characters")
        @Pattern(regexp = "^[A-Z0-9-]+$", message = "SKU can only contain uppercase letters, numbers and hyphens")
        private String sku;
        
        @Schema(description = "Product name", example = "iPhone 15 Pro", required = true)
        @NotBlank(message = "Product name is required")
        @Size(min = 2, max = 200, message = "Product name must be between 2 and 200 characters")
        private String name;
        
        @Schema(description = "Product description", example = "Latest iPhone with advanced features")
        @Size(max = 1000, message = "Product description must not exceed 1000 characters")
        private String description;
        
        @Schema(description = "Product price", example = "999.00", required = true)
        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.01", message = "Price must be positive")
        @DecimalMax(value = "9999999.99", message = "Price must not exceed 9999999.99")
        private BigDecimal price;
        
        @Schema(description = "Currency code", example = "CNY", required = true)
        @NotBlank(message = "Currency is required")
        @Size(min = 3, max = 3, message = "Currency code must be exactly 3 characters")
        @Pattern(regexp = "^[A-Z]{3}$", message = "Currency code must be 3 uppercase letters")
        private String currency = "CNY";
        
        @Schema(description = "Initial inventory quantity", example = "100")
        @Min(value = 0, message = "Initial inventory cannot be negative")
        @Max(value = 999999, message = "Initial inventory must not exceed 999999")
        private int initialInventory;
        
        // Getters and Setters
        public String getSku() { return sku; }
        public void setSku(String sku) { this.sku = sku; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
        public int getInitialInventory() { return initialInventory; }
        public void setInitialInventory(int initialInventory) { this.initialInventory = initialInventory; }
    }
    
    @Schema(description = "Product information response")
    public static class ProductResponse {
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
        
        @Schema(description = "Available inventory", example = "50")
        private int availableInventory;
        
        @Schema(description = "Product status", example = "ACTIVE")
        private String status;
        
        public ProductResponse(Long id, String sku, String name, String description,
                             BigDecimal price, String currency, Long merchantId,
                             int availableInventory, String status) {
            this.id = id;
            this.sku = sku;
            this.name = name;
            this.description = description;
            this.price = price;
            this.currency = currency;
            this.merchantId = merchantId;
            this.availableInventory = availableInventory;
            this.status = status;
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
    }
    
    @Schema(description = "Add inventory request")
    public static class AddInventoryRequest {
        @Schema(description = "Quantity to add", example = "10", required = true)
        @Min(value = 1, message = "Quantity must be positive")
        private int quantity;
        
        public AddInventoryRequest() {}
        
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
    }
    
    @Schema(description = "Reduce inventory request")
    public static class ReduceInventoryRequest {
        @Schema(description = "Quantity to reduce", example = "5", required = true)
        @Min(value = 1, message = "Quantity must be positive")
        private int quantity;
        
        public ReduceInventoryRequest() {}
        
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
    }
    
    @Schema(description = "Set inventory request")
    public static class SetInventoryRequest {
        @Schema(description = "New inventory quantity", example = "50", required = true)
        @Min(value = 0, message = "Quantity cannot be negative")
        private int quantity;
        
        public SetInventoryRequest() {}
        
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
    }
    
    @Schema(description = "Inventory operation response")
    public static class InventoryResponse {
        @Schema(description = "Product SKU", example = "PHONE-001")
        private String sku;
        
        @Schema(description = "Product name", example = "iPhone 15 Pro")
        private String productName;
        
        @Schema(description = "Available inventory", example = "100")
        private int availableInventory;
        
        @Schema(description = "Product availability", example = "true")
        private boolean available;
        
        public InventoryResponse(String sku, String productName, int availableInventory,
                               boolean available) {
            this.sku = sku;
            this.productName = productName;
            this.availableInventory = availableInventory;
            this.available = available;
        }
        
        // Getters
        public String getSku() { return sku; }
        public String getProductName() { return productName; }
        public int getAvailableInventory() { return availableInventory; }
        public boolean isAvailable() { return available; }
    }
    
    @Schema(description = "Merchant income response")
    public static class IncomeResponse {
        @Schema(description = "Merchant ID", example = "1")
        private Long merchantId;
        
        @Schema(description = "Current balance", example = "1000.00")
        private BigDecimal currentBalance;
        
        @Schema(description = "Total income", example = "5000.00")
        private BigDecimal totalIncome;
        
        @Schema(description = "Currency code", example = "CNY")
        private String currency;
        
        public IncomeResponse(Long merchantId, BigDecimal currentBalance,
                            BigDecimal totalIncome, String currency) {
            this.merchantId = merchantId;
            this.currentBalance = currentBalance;
            this.totalIncome = totalIncome;
            this.currency = currency;
        }
        
        // Getters
        public Long getMerchantId() { return merchantId; }
        public BigDecimal getCurrentBalance() { return currentBalance; }
        public BigDecimal getTotalIncome() { return totalIncome; }
        public String getCurrency() { return currency; }
    }
    
    @Schema(description = "Merchant product list response")
    public static class MerchantProductListResponse {
        @Schema(description = "Merchant ID", example = "1")
        private Long merchantId;
        
        @Schema(description = "Product list")
        private List<ProductResponse> products;
        
        @Schema(description = "Total product count", example = "10")
        private int totalCount;
        
        @Schema(description = "Status filter applied", example = "ACTIVE")
        private String statusFilter;
        
        @Schema(description = "Search term applied", example = "iPhone")
        private String searchTerm;
        
        public MerchantProductListResponse(Long merchantId, List<ProductResponse> products, 
                                         int totalCount, String statusFilter, String searchTerm) {
            this.merchantId = merchantId;
            this.products = products;
            this.totalCount = totalCount;
            this.statusFilter = statusFilter;
            this.searchTerm = searchTerm;
        }
        
        // Getters
        public Long getMerchantId() { return merchantId; }
        public List<ProductResponse> getProducts() { return products; }
        public int getTotalCount() { return totalCount; }
        public String getStatusFilter() { return statusFilter; }
        public String getSearchTerm() { return searchTerm; }
    }

    @Schema(description = "Settlement request")
    public static class SettlementRequest {
        @Schema(description = "Date for which to execute settlement (YYYY-MM-DD)", example = "2025-07-11")
        private LocalDate settlementDate;

        public LocalDate getSettlementDate() {
            return settlementDate;
        }

        public void setSettlementDate(LocalDate settlementDate) {
            this.settlementDate = settlementDate;
        }
    }

    @Schema(description = "Settlement operation response")
    public static class SettlementResponse {
        @Schema(description = "Merchant ID", example = "1")
        private Long merchantId;
        
        @Schema(description = "Settlement date", example = "2025-07-11")
        private LocalDate settlementDate;
        
        @Schema(description = "Expected income", example = "1000.00")
        private BigDecimal expectedIncome;
        
        @Schema(description = "Actual balance", example = "1000.00")
        private BigDecimal actualBalance;
        
        @Schema(description = "Difference amount", example = "0.00")
        private BigDecimal difference;
        
        @Schema(description = "Settlement status", example = "COMPLETED")
        private String status;
        
        @Schema(description = "Settlement notes", example = "Settlement completed successfully")
        private String notes;

        public SettlementResponse(Long merchantId, LocalDate settlementDate, BigDecimal expectedIncome, BigDecimal actualBalance, BigDecimal difference, String status, String notes) {
            this.merchantId = merchantId;
            this.settlementDate = settlementDate;
            this.expectedIncome = expectedIncome;
            this.actualBalance = actualBalance;
            this.difference = difference;
            this.status = status;
            this.notes = notes;
        }

        public Long getMerchantId() {
            return merchantId;
        }

        public void setMerchantId(Long merchantId) {
            this.merchantId = merchantId;
        }

        public LocalDate getSettlementDate() {
            return settlementDate;
        }

        public void setSettlementDate(LocalDate settlementDate) {
            this.settlementDate = settlementDate;
        }

        public BigDecimal getExpectedIncome() {
            return expectedIncome;
        }

        public void setExpectedIncome(BigDecimal expectedIncome) {
            this.expectedIncome = expectedIncome;
        }

        public BigDecimal getActualBalance() {
            return actualBalance;
        }

        public void setActualBalance(BigDecimal actualBalance) {
            this.actualBalance = actualBalance;
        }

        public BigDecimal getDifference() {
            return difference;
        }

        public void setDifference(BigDecimal difference) {
            this.difference = difference;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getNotes() {
            return notes;
        }

        public void setNotes(String notes) {
            this.notes = notes;
        }
    }

    @Schema(description = "Global Settlement request")
    public static class GlobalSettlementRequest {
        @Schema(description = "Date for which to execute global settlement (YYYY-MM-DD)", example = "2025-07-11")
        private LocalDate settlementDate;

        public LocalDate getSettlementDate() {
            return settlementDate;
        }

        public void setSettlementDate(LocalDate settlementDate) {
            this.settlementDate = settlementDate;
        }
    }

    @Schema(description = "Global settlement response")
    public static class GlobalSettlementResponse {
        @Schema(description = "Settlement date", example = "2025-07-11")
        private LocalDate settlementDate;
        
        @Schema(description = "Settlement execution time", example = "2025-07-11T02:00:00")
        private LocalDateTime settlementTime;

        public GlobalSettlementResponse(LocalDate settlementDate, LocalDateTime settlementTime) {
            this.settlementDate = settlementDate;
            this.settlementTime = settlementTime;
        }

        public LocalDate getSettlementDate() {
            return settlementDate;
        }

        public void setSettlementDate(LocalDate settlementDate) {
            this.settlementDate = settlementDate;
        }

        public LocalDateTime getSettlementTime() {
            return settlementTime;
        }

        public void setSettlementTime(LocalDateTime settlementTime) {
            this.settlementTime = settlementTime;
        }
    }
} 