package com.trading.api.controller;

import com.trading.application.service.MerchantService;
import com.trading.application.service.ProductService;
import com.trading.domain.merchant.Merchant;
import com.trading.domain.product.Product;
import com.trading.domain.Money;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.math.BigDecimal;

/**
 * 商家控制器
 * 处理商家相关的REST API
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
     * 创建商家
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
                request.getContactPhone(),
                "CNY"
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
            logger.error("Failed to create merchant: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 创建商品
     * POST /api/merchants/{merchantId}/products
     */
    @PostMapping("/{merchantId}/products")
    public ResponseEntity<ProductResponse> createProduct(@PathVariable Long merchantId,
                                                       @RequestBody CreateProductRequest request) {
        try {
            logger.info("Creating product for merchant {}: {}", merchantId, request.getSku());
            
            // 验证商家是否存在
            if (!merchantService.merchantExists(merchantId)) {
                throw new RuntimeException("Merchant not found with id: " + merchantId);
            }
            
            Money price = Money.of(request.getPrice(), request.getCurrency());
            
            Product product = productService.createProduct(
                request.getSku(),
                request.getName(),
                request.getDescription(),
                price,
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
            logger.error("Failed to create product: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 添加商品库存
     * POST /api/merchants/{merchantId}/products/{sku}/add-stock
     */
    @PostMapping("/{merchantId}/products/{sku}/add-stock")
    public ResponseEntity<AddStockResponse> addProductStock(@PathVariable Long merchantId,
                                                          @PathVariable String sku,
                                                          @RequestBody AddStockRequest request) {
        try {
            logger.info("Adding stock for product {} (merchant {}): {} units", sku, merchantId, request.getQuantity());
            
            // 验证商家是否存在
            if (!merchantService.merchantExists(merchantId)) {
                throw new RuntimeException("Merchant not found with id: " + merchantId);
            }
            
            // 验证商品是否存在且属于该商家
            Product product = productService.getProductBySku(sku);
            if (!product.getMerchantId().equals(merchantId)) {
                throw new RuntimeException("Product does not belong to merchant");
            }
            
            int originalStock = product.getAvailableStock();
            
            // 添加库存
            productService.addProductStock(sku, request.getQuantity());
            
            // 获取更新后的库存
            int newStock = productService.getProductStock(sku);
            
            AddStockResponse response = new AddStockResponse(
                merchantId,
                sku,
                request.getQuantity(),
                originalStock,
                newStock,
                "SUCCESS",
                "Stock added successfully"
            );
            
            logger.info("Stock added successfully for {}: {} -> {}", sku, originalStock, newStock);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to add stock for {}: {}", sku, e.getMessage(), e);
            
            AddStockResponse errorResponse = new AddStockResponse();
            errorResponse.setMerchantId(merchantId);
            errorResponse.setSku(sku);
            errorResponse.setStatus("FAILED");
            errorResponse.setMessage(e.getMessage());
            
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * 获取商家收入信息
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
    
    public static class AddStockResponse {
        private Long merchantId;
        private String sku;
        private int addedQuantity;
        private int originalStock;
        private int newStock;
        private String status;
        private String message;
        
        public AddStockResponse() {}
        
        public AddStockResponse(Long merchantId, String sku, int addedQuantity,
                              int originalStock, int newStock, String status, String message) {
            this.merchantId = merchantId;
            this.sku = sku;
            this.addedQuantity = addedQuantity;
            this.originalStock = originalStock;
            this.newStock = newStock;
            this.status = status;
            this.message = message;
        }
        
        // Getters and Setters
        public Long getMerchantId() { return merchantId; }
        public void setMerchantId(Long merchantId) { this.merchantId = merchantId; }
        public String getSku() { return sku; }
        public void setSku(String sku) { this.sku = sku; }
        public int getAddedQuantity() { return addedQuantity; }
        public int getOriginalStock() { return originalStock; }
        public int getNewStock() { return newStock; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
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