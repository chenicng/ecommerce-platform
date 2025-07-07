package com.ecommerce.api.controller;

import com.ecommerce.application.service.MerchantService;
import com.ecommerce.application.service.ProductService;
import com.ecommerce.domain.merchant.Merchant;
import com.ecommerce.domain.product.Product;
import com.ecommerce.domain.Money;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.math.BigDecimal;

/**
 * Merchant Controller
 * Handles merchant-related REST APIs
 */
@RestController
@RequestMapping("/api/merchants")
public class MerchantController {
    
    private static final Logger logger = LoggerFactory.getLogger(MerchantController.class);
    
    private final MerchantService merchantService;
    private final ProductService productService;
    
    public MerchantController(MerchantService merchantService, ProductService productService) {
        this.merchantService = merchantService;
        this.productService = productService;
    }
    
    /**
     * Create merchant
     * POST /api/merchants
     */
    @PostMapping
    public ResponseEntity<MerchantResponse> createMerchant(@RequestBody CreateMerchantRequest request) {
        try {
            logger.info("Creating merchant: {}", request.getMerchantName());
            
            Merchant merchant = merchantService.createMerchant(
                request.getMerchantName(),
                request.getBusinessLicense(),
                request.getContactEmail(),
                request.getContactPhone()
            );
            
            MerchantResponse response = new MerchantResponse(
                merchant.getId(),
                merchant.getMerchantName(),
                merchant.getBusinessLicense(),
                merchant.getContactEmail(),
                merchant.getContactPhone(),
                merchant.getBalance().getAmount(),
                merchant.getBalance().getCurrency(),
                merchant.getTotalIncome().getAmount(),
                merchant.getStatus().toString()
            );
            
            logger.info("Merchant created successfully with ID: {}", merchant.getId());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to create merchant: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Create product
     * POST /api/merchants/{merchantId}/products
     */
    @PostMapping("/{merchantId}/products")
    public ResponseEntity<ProductResponse> createProduct(@PathVariable Long merchantId,
                                                        @RequestBody CreateProductRequest request) {
        try {
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
                request.getInitialStock()
            );
            
            ProductResponse response = new ProductResponse(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getDescription(),
                product.getPrice().getAmount(),
                product.getPrice().getCurrency(),
                product.getMerchantId(),
                product.getAvailableStock(),
                product.getStatus().toString()
            );
            
            logger.info("Product created successfully: {}", product.getSku());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to create product for merchant {}: {}", merchantId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Add product stock
     * POST /api/merchants/{merchantId}/products/{sku}/add-stock
     */
    @PostMapping("/{merchantId}/products/{sku}/add-stock")
    public ResponseEntity<String> addProductStock(@PathVariable Long merchantId,
                                                  @PathVariable String sku,
                                                  @RequestBody AddStockRequest request) {
        try {
            logger.info("Adding stock for merchant {}, product {}: quantity={}", merchantId, sku, request.getQuantity());
            
            // Validate merchant exists
            if (!merchantService.merchantExists(merchantId)) {
                throw new RuntimeException("Merchant not found with id: " + merchantId);
            }
            
            // Validate product exists and belongs to this merchant
            Product product = productService.getProductBySku(sku);
            if (!product.getMerchantId().equals(merchantId)) {
                throw new RuntimeException("Product does not belong to this merchant");
            }
            
            productService.addProductStock(sku, request.getQuantity());
            
            logger.info("Stock added successfully for product {}: quantity={}", sku, request.getQuantity());
            return ResponseEntity.ok("Stock added successfully");
            
        } catch (Exception e) {
            logger.error("Failed to add stock for merchant {}, product {}: {}", merchantId, sku, e.getMessage());
            return ResponseEntity.badRequest().body("Failed to add stock: " + e.getMessage());
        }
    }
    
    /**
     * Get merchant income information
     * GET /api/merchants/{merchantId}/income
     */
    @GetMapping("/{merchantId}/income")
    public ResponseEntity<IncomeResponse> getMerchantIncome(@PathVariable Long merchantId) {
        try {
            Money balance = merchantService.getMerchantBalance(merchantId);
            Money totalIncome = merchantService.getMerchantTotalIncome(merchantId);
            
            IncomeResponse response = new IncomeResponse(
                merchantId,
                balance.getAmount(),
                totalIncome.getAmount(),
                balance.getCurrency()
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to get income for merchant {}: {}", merchantId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    // DTO classes
    public static class CreateMerchantRequest {
        private String merchantName;
        private String businessLicense;
        private String contactEmail;
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
        private String sku;
        private String name;
        private String description;
        private BigDecimal price;
        private String currency = "CNY";
        private int initialStock;
        
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
        public int getInitialStock() { return initialStock; }
        public void setInitialStock(int initialStock) { this.initialStock = initialStock; }
    }
    
    public static class ProductResponse {
        private Long id;
        private String sku;
        private String name;
        private String description;
        private BigDecimal price;
        private String currency;
        private Long merchantId;
        private int availableStock;
        private String status;
        
        public ProductResponse(Long id, String sku, String name, String description,
                             BigDecimal price, String currency, Long merchantId,
                             int availableStock, String status) {
            this.id = id;
            this.sku = sku;
            this.name = name;
            this.description = description;
            this.price = price;
            this.currency = currency;
            this.merchantId = merchantId;
            this.availableStock = availableStock;
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
        public int getAvailableStock() { return availableStock; }
        public String getStatus() { return status; }
    }
    
    public static class AddStockRequest {
        private int quantity;
        
        // Getters and Setters
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
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
} 