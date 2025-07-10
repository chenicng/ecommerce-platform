package com.ecommerce.api.controller;

import com.ecommerce.application.service.MerchantService;
import com.ecommerce.application.service.ProductService;
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
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Merchant Controller
 * Handles merchant registration, product management, and income inquiry
 */
@RestController
@RequestMapping(ApiVersionConfig.API_V1 + "/merchants")
@ApiVersion(value = "v1", since = "2025-01-01")
@Tag(name = "Merchant Management", description = "Merchant registration, product management and income tracking")
public class MerchantController {
    
    private static final Logger logger = LoggerFactory.getLogger(MerchantController.class);
    
    private final MerchantService merchantService;
    private final ProductService productService;
    
    public MerchantController(MerchantService merchantService, ProductService productService) {
        this.merchantService = merchantService;
        this.productService = productService;
    }
    
    /**
     * Register a new merchant
     * POST /api/merchants
     */
    @PostMapping
    @Operation(summary = "Create Merchant", description = "Register a new merchant with business information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Merchant created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data", 
                    content = @Content(schema = @Schema(implementation = Result.class)))
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
     * Create a new product for merchant
     * POST /api/merchants/{merchantId}/products
     */
    @PostMapping("/{merchantId}/products")
    @Operation(summary = "Create Product", description = "Create a new product for a specific merchant")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Product created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "404", description = "Merchant not found")
    })
    public ResponseEntity<Result<ProductResponse>> createProduct(
            @Parameter(description = "Merchant ID", required = true, example = "1")
            @PathVariable Long merchantId,
            @Valid @RequestBody CreateProductRequest request) {
        logger.info("Creating product for merchant {}: {}", merchantId, request.getSku());
        
        // Validate merchant exists
        if (!merchantService.merchantExists(merchantId)) {
            throw new RuntimeException("Merchant not found with id: " + merchantId);
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
     * Add product inventory
     * POST /api/merchants/{merchantId}/products/{sku}/inventory/add
     */
    @PostMapping("/{merchantId}/products/{sku}/inventory/add")
    @Operation(summary = "Add Product Inventory", description = "Add inventory quantity to a specific product")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Inventory added successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "404", description = "Merchant or product not found")
    })
    public ResponseEntity<Result<InventoryResponse>> addProductInventory(
            @Parameter(description = "Merchant ID", required = true, example = "1")
            @PathVariable Long merchantId,
            @Parameter(description = "Product SKU", required = true, example = "IPHONE-15-PRO")
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
            product.isAvailable(),
            product.getStatus().toString(),
            "Inventory added successfully"
        );
        
        logger.info("Inventory added successfully for product {}: quantity={}", sku, request.getQuantity());
        return ResponseEntity.ok(Result.successWithMessage("Inventory added successfully", response));
    }
    
    /**
     * Reduce product inventory
     * POST /api/merchants/{merchantId}/products/{sku}/inventory/reduce
     */
    @PostMapping("/{merchantId}/products/{sku}/inventory/reduce")
    @Operation(summary = "Reduce Product Inventory", description = "Reduce inventory quantity for a specific product")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Inventory reduced successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data or insufficient inventory"),
        @ApiResponse(responseCode = "404", description = "Merchant or product not found")
    })
    public ResponseEntity<Result<InventoryResponse>> reduceProductInventory(
            @Parameter(description = "Merchant ID", required = true, example = "1")
            @PathVariable Long merchantId,
            @Parameter(description = "Product SKU", required = true, example = "IPHONE-15-PRO")
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
            product.isAvailable(),
            product.getStatus().toString(),
            "Inventory reduced successfully"
        );
        
        logger.info("Inventory reduced successfully for product {}: quantity={}", sku, request.getQuantity());
        return ResponseEntity.ok(Result.success(response));
    }
    
    /**
     * Set product inventory to absolute value
     * PUT /api/merchants/{merchantId}/products/{sku}/inventory
     */
    @PutMapping("/{merchantId}/products/{sku}/inventory")
    @Operation(summary = "Set Product Inventory", description = "Set inventory quantity to absolute value for a specific product")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Inventory set successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "404", description = "Merchant or product not found")
    })
    public ResponseEntity<Result<InventoryResponse>> setProductInventory(
            @Parameter(description = "Merchant ID", required = true, example = "1")
            @PathVariable Long merchantId,
            @Parameter(description = "Product SKU", required = true, example = "IPHONE-15-PRO")
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
            product.isAvailable(),
            product.getStatus().toString(),
            "Inventory set successfully"
        );
        
        logger.info("Inventory set successfully for product {}: quantity={}", sku, request.getQuantity());
        return ResponseEntity.ok(Result.success(response));
    }
    
    /**
     * Helper method to validate merchant and product
     */
    private void validateMerchantAndProduct(Long merchantId, String sku) {
        // Validate merchant exists
        if (!merchantService.merchantExists(merchantId)) {
            throw new RuntimeException("Merchant not found with id: " + merchantId);
        }
        
        // Validate product exists and belongs to this merchant
        Product product = productService.getProductBySku(sku);
        if (!product.getMerchantId().equals(merchantId)) {
            throw new RuntimeException("Product does not belong to this merchant");
        }
    }
    
    /**
     * Get merchant income information
     * GET /api/merchants/{merchantId}/income
     */
    @GetMapping("/{merchantId}/income")
    @Operation(summary = "Get Merchant Income", description = "Retrieve merchant's current balance and total income")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Income information retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Merchant not found")
    })
    public ResponseEntity<Result<IncomeResponse>> getMerchantIncome(
            @Parameter(description = "Merchant ID", required = true, example = "1")
            @PathVariable Long merchantId) {
        logger.info("Getting income for merchant: {}", merchantId);
        
        Money balance = merchantService.getMerchantBalance(merchantId);
        Money totalIncome = merchantService.getMerchantTotalIncome(merchantId);
        
        IncomeResponse response = new IncomeResponse(
            merchantId,
            balance.getAmount(),
            totalIncome.getAmount(),
            balance.getCurrency()
        );
        
        return ResponseEntity.ok(Result.success(response));
    }
    
    /**
     * Get merchant's products (Merchant management interface)
     * GET /api/merchants/{merchantId}/products
     * 
     * This endpoint is for merchant product management.
     * It returns ALL products (including inactive/deleted) for management purposes.
     * 
     * Supports:
     * - Status filtering: ?status=ACTIVE
     * - Search within merchant's products: ?search=iPhone
     * - Combined filtering: ?search=iPhone&status=ACTIVE
     * 
     * For public product browsing, use /api/ecommerce/products?merchantId={merchantId} instead.
     */
    @GetMapping("/{merchantId}/products")
    @Operation(summary = "Get Merchant Products", description = "Retrieve all products for a specific merchant with optional filtering")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Products retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Merchant not found")
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
            throw new RuntimeException("Merchant not found with id: " + merchantId);
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
        
        // Apply search filter if specified
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
     * Get single product details for merchant
     * GET /api/merchants/{merchantId}/products/{sku}
     */
    @GetMapping("/{merchantId}/products/{sku}")
    @Operation(summary = "Get Merchant Product", description = "Retrieve specific product details for a merchant")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Product retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Merchant or product not found")
    })
    public ResponseEntity<Result<ProductResponse>> getMerchantProduct(
            @Parameter(description = "Merchant ID", required = true, example = "1")
            @PathVariable Long merchantId,
            @Parameter(description = "Product SKU", required = true, example = "IPHONE-15-PRO")
            @PathVariable String sku) {
        logger.info("Getting product {} for merchant {}", sku, merchantId);
        
        // Validate merchant exists
        if (!merchantService.merchantExists(merchantId)) {
            throw new RuntimeException("Merchant not found with id: " + merchantId);
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
        private String merchantName;
        
        @Schema(description = "Business license number", example = "BL123456789", required = true)
        @NotBlank(message = "Business license is required")
        private String businessLicense;
        
        @Schema(description = "Contact email", example = "contact@applestore.com", required = true)
        @NotBlank(message = "Contact email is required")
        private String contactEmail;
        
        @Schema(description = "Contact phone", example = "400-123-4567", required = true)
        @NotBlank(message = "Contact phone is required")
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
    
    public static class MerchantResponse {
        private Long id;
        private String merchantName;
        private String businessLicense;
        private String contactEmail;
        private String contactPhone;
        private BigDecimal balance;
        private String currency;
        private BigDecimal totalIncome;
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
        @Schema(description = "Product SKU", example = "IPHONE-15-PRO", required = true)
        @NotBlank(message = "SKU is required")
        private String sku;
        
        @Schema(description = "Product name", example = "iPhone 15 Pro", required = true)
        @NotBlank(message = "Product name is required")
        private String name;
        
        @Schema(description = "Product description", example = "Latest iPhone with advanced features")
        private String description;
        
        @Schema(description = "Product price", example = "7999.00", required = true)
        @DecimalMin(value = "0.01", message = "Price must be positive")
        private BigDecimal price;
        
        @Schema(description = "Currency code", example = "CNY", defaultValue = "CNY")
        @NotBlank(message = "Currency is required")
        private String currency = "CNY";
        
        @Schema(description = "Initial inventory quantity", example = "100", required = true)
        @Min(value = 0, message = "Initial inventory cannot be negative")
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
    
    public static class ProductResponse {
        private Long id;
        private String sku;
        private String name;
        private String description;
        private BigDecimal price;
        private String currency;
        private Long merchantId;
        private int availableInventory;
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
    
    public static class AddInventoryRequest {
        @Min(value = 1, message = "Quantity must be positive")
        private int quantity;
        
        public AddInventoryRequest() {}
        
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
    }
    
    public static class ReduceInventoryRequest {
        @Min(value = 1, message = "Quantity must be positive")
        private int quantity;
        
        public ReduceInventoryRequest() {}
        
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
    }
    
    public static class SetInventoryRequest {
        @Min(value = 0, message = "Quantity cannot be negative")
        private int quantity;
        
        public SetInventoryRequest() {}
        
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
    }
    
    public static class InventoryResponse {
        private String sku;
        private String productName;
        private int availableInventory;
        private boolean available;
        private String status;
        private String message;
        
        public InventoryResponse(String sku, String productName, int availableInventory,
                               boolean available, String status, String message) {
            this.sku = sku;
            this.productName = productName;
            this.availableInventory = availableInventory;
            this.available = available;
            this.status = status;
            this.message = message;
        }
        
        // Getters
        public String getSku() { return sku; }
        public String getProductName() { return productName; }
        public int getAvailableInventory() { return availableInventory; }
        public boolean isAvailable() { return available; }
        public String getStatus() { return status; }
        public String getMessage() { return message; }
    }
    
    public static class IncomeResponse {
        private Long merchantId;
        private BigDecimal currentBalance;
        private BigDecimal totalIncome;
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
    
    public static class MerchantProductListResponse {
        private Long merchantId;
        private List<ProductResponse> products;
        private int totalCount;
        private String statusFilter;
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
} 