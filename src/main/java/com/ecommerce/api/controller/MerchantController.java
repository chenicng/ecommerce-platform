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

/**
 * Merchant Controller
 * Handles merchant registration, product management, and income inquiry
 */
@RestController
@RequestMapping(ApiVersionConfig.API_V1 + "/merchants")
@ApiVersion(value = "v1", since = "2025-01-01")
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
    public ResponseEntity<MerchantResponse> createMerchant(@Valid @RequestBody CreateMerchantRequest request) {
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
        return ResponseEntity.ok(response);
    }
    
    /**
     * Create a new product for merchant
     * POST /api/merchants/{merchantId}/products
     */
    @PostMapping("/{merchantId}/products")
    public ResponseEntity<Result<ProductResponse>> createProduct(@PathVariable Long merchantId,
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
        return ResponseEntity.ok(Result.success("Product created successfully", response));
    }
    
    /**
     * Add product inventory
     * POST /api/merchants/{merchantId}/products/{sku}/inventory/add
     */
    @PostMapping("/{merchantId}/products/{sku}/inventory/add")
    public ResponseEntity<InventoryResponse> addProductInventory(@PathVariable Long merchantId,
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
        return ResponseEntity.ok(response);
    }
    
    /**
     * Reduce product inventory
     * POST /api/merchants/{merchantId}/products/{sku}/inventory/reduce
     */
    @PostMapping("/{merchantId}/products/{sku}/inventory/reduce")
    public ResponseEntity<InventoryResponse> reduceProductInventory(@PathVariable Long merchantId,
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
        return ResponseEntity.ok(response);
    }
    
    /**
     * Set product inventory to absolute value
     * PUT /api/merchants/{merchantId}/products/{sku}/inventory
     */
    @PutMapping("/{merchantId}/products/{sku}/inventory")
    public ResponseEntity<InventoryResponse> setProductInventory(@PathVariable Long merchantId,
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
        return ResponseEntity.ok(response);
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
    public ResponseEntity<IncomeResponse> getMerchantIncome(@PathVariable Long merchantId) {
        logger.info("Getting income for merchant: {}", merchantId);
        
        Money balance = merchantService.getMerchantBalance(merchantId);
        Money totalIncome = merchantService.getMerchantTotalIncome(merchantId);
        
        IncomeResponse response = new IncomeResponse(
            merchantId,
            balance.getAmount(),
            totalIncome.getAmount(),
            balance.getCurrency()
        );
        
        return ResponseEntity.ok(response);
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
    public ResponseEntity<MerchantProductListResponse> getMerchantProducts(
            @PathVariable Long merchantId,
            @RequestParam(value = "status", required = false) String status,
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
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get single product details for merchant
     * GET /api/merchants/{merchantId}/products/{sku}
     */
    @GetMapping("/{merchantId}/products/{sku}")
    public ResponseEntity<Result<ProductResponse>> getMerchantProduct(@PathVariable Long merchantId,
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
    public static class CreateMerchantRequest {
        @NotBlank(message = "Merchant name is required")
        private String merchantName;
        
        @NotBlank(message = "Business license is required")
        private String businessLicense;
        
        @NotBlank(message = "Contact email is required")
        private String contactEmail;
        
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
    
    public static class CreateProductRequest {
        @NotBlank(message = "SKU is required")
        private String sku;
        
        @NotBlank(message = "Product name is required")
        private String name;
        
        private String description;
        
        @DecimalMin(value = "0.01", message = "Price must be positive")
        private BigDecimal price;
        
        @NotBlank(message = "Currency is required")
        private String currency = "CNY";
        
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