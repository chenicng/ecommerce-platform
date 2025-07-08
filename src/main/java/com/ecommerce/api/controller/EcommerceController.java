package com.ecommerce.api.controller;

import com.ecommerce.application.service.EcommerceService;
import com.ecommerce.application.service.ProductService;
import com.ecommerce.application.dto.PurchaseRequest;
import com.ecommerce.application.dto.PurchaseResponse;
import com.ecommerce.domain.product.Product;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Ecommerce Controller
 * Handles REST API related to product purchasing and product browsing
 */
@RestController
@RequestMapping("/api/ecommerce")
public class EcommerceController {
    
    private static final Logger logger = LoggerFactory.getLogger(EcommerceController.class);
    
    private final EcommerceService ecommerceService;
    private final ProductService productService;
    
    public EcommerceController(EcommerceService ecommerceService, ProductService productService) {
        this.ecommerceService = ecommerceService;
        this.productService = productService;
    }
    
    /**
     * Purchase Product
     * POST /api/ecommerce/purchase
     */
    @PostMapping("/purchase")
    public ResponseEntity<PurchaseResponse> purchaseProduct(@RequestBody PurchaseRequest request) {
        try {
            logger.info("Processing purchase request: {}", request);
            
            // Validate request parameters
            validatePurchaseRequest(request);
            
            // Process purchase
            PurchaseResponse response = ecommerceService.processPurchase(request);
            
            logger.info("Purchase completed successfully: {}", response.getOrderNumber());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Purchase failed: {}", e.getMessage(), e);
            
            // Return error response
            PurchaseResponse errorResponse = new PurchaseResponse();
            errorResponse.setStatus("FAILED");
            errorResponse.setMessage(e.getMessage());
            
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * Get product details by SKU
     * GET /api/ecommerce/products/{sku}
     */
    @GetMapping("/products/{sku}")
    public ResponseEntity<ProductDetailResponse> getProductBySku(@PathVariable String sku) {
        try {
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
                product.getAvailableStock(),
                product.getStatus().toString(),
                product.isAvailable()
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to get product with SKU {}: {}", sku, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Get all available products (Public browsing interface)
     * GET /api/ecommerce/products
     * 
     * This endpoint is for public product browsing and purchasing.
     * It only returns AVAILABLE products (active and in stock).
     * 
     * Supports:
     * - Global search: ?search=iPhone
     * - Merchant filtering: ?merchantId=1  
     * - Combined search: ?search=iPhone&merchantId=1
     * 
     * For merchant product management, use /api/merchants/{merchantId}/products instead.
     */
    @GetMapping("/products")
    public ResponseEntity<ProductListResponse> getAvailableProducts(
            @RequestParam(value = "search", required = false) String searchTerm,
            @RequestParam(value = "merchantId", required = false) Long merchantId) {
        try {
            logger.info("Getting available products with search: {}, merchantId: {}", searchTerm, merchantId);
            
            List<Product> products;
            
            // Support combined search and merchant filtering
            if (searchTerm != null && !searchTerm.trim().isEmpty() && merchantId != null) {
                // Search within specific merchant's products
                products = productService.getProductsByMerchant(merchantId)
                    .stream()
                    .filter(Product::isAvailable)
                    .filter(product -> product.getName().toLowerCase().contains(searchTerm.toLowerCase()) ||
                                     product.getDescription().toLowerCase().contains(searchTerm.toLowerCase()))
                    .collect(Collectors.toList());
            } else if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                // Global search across all merchants
                products = productService.searchProductsByName(searchTerm)
                    .stream()
                    .filter(Product::isAvailable)
                    .collect(Collectors.toList());
            } else if (merchantId != null) {
                // Get all available products from specific merchant
                products = productService.getProductsByMerchant(merchantId)
                    .stream()
                    .filter(Product::isAvailable)
                    .collect(Collectors.toList());
            } else {
                // Get all available products
                products = productService.getAvailableProducts();
            }

            List<ProductSummaryResponse> productSummaries = products.stream()
                .map(product -> new ProductSummaryResponse(
                    product.getId(),
                    product.getSku(),
                    product.getName(),
                    product.getPrice().getAmount(),
                    product.getPrice().getCurrency(),
                    product.getMerchantId(),
                    product.getAvailableStock(),
                    product.isAvailable()
                ))
                .collect(Collectors.toList());
            
            ProductListResponse response = new ProductListResponse(
                productSummaries,
                productSummaries.size(),
                searchTerm,
                merchantId
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to get products: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Check product stock
     * GET /api/ecommerce/products/{sku}/stock
     */
    @GetMapping("/products/{sku}/stock")
    public ResponseEntity<StockResponse> getProductStock(@PathVariable String sku) {
        try {
            logger.info("Checking stock for product: {}", sku);
            
            Product product = productService.getProductBySku(sku);
            
            StockResponse response = new StockResponse(
                product.getSku(),
                product.getName(),
                product.getAvailableStock(),
                product.isAvailable(),
                product.getStatus().toString()
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to get stock for product {}: {}", sku, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    private void validatePurchaseRequest(PurchaseRequest request) {
        if (request.getUserId() == null) {
            throw new IllegalArgumentException("User ID is required");
        }
        if (request.getSku() == null || request.getSku().trim().isEmpty()) {
            throw new IllegalArgumentException("SKU is required");
        }
        if (request.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
    }
    
    // DTO classes for product queries
    public static class ProductDetailResponse {
        private Long id;
        private String sku;
        private String name;
        private String description;
        private BigDecimal price;
        private String currency;
        private Long merchantId;
        private int availableStock;
        private String status;
        private boolean available;
        
        public ProductDetailResponse(Long id, String sku, String name, String description,
                                   BigDecimal price, String currency, Long merchantId,
                                   int availableStock, String status, boolean available) {
            this.id = id;
            this.sku = sku;
            this.name = name;
            this.description = description;
            this.price = price;
            this.currency = currency;
            this.merchantId = merchantId;
            this.availableStock = availableStock;
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
        public int getAvailableStock() { return availableStock; }
        public String getStatus() { return status; }
        public boolean isAvailable() { return available; }
    }
    
    public static class ProductSummaryResponse {
        private Long id;
        private String sku;
        private String name;
        private BigDecimal price;
        private String currency;
        private Long merchantId;
        private int availableStock;
        private boolean available;
        
        public ProductSummaryResponse(Long id, String sku, String name, BigDecimal price,
                                    String currency, Long merchantId, int availableStock, boolean available) {
            this.id = id;
            this.sku = sku;
            this.name = name;
            this.price = price;
            this.currency = currency;
            this.merchantId = merchantId;
            this.availableStock = availableStock;
            this.available = available;
        }
        
        // Getters
        public Long getId() { return id; }
        public String getSku() { return sku; }
        public String getName() { return name; }
        public BigDecimal getPrice() { return price; }
        public String getCurrency() { return currency; }
        public Long getMerchantId() { return merchantId; }
        public int getAvailableStock() { return availableStock; }
        public boolean isAvailable() { return available; }
    }
    
    public static class ProductListResponse {
        private List<ProductSummaryResponse> products;
        private int totalCount;
        private String searchTerm;
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
    
    public static class StockResponse {
        private String sku;
        private String productName;
        private int availableStock;
        private boolean available;
        private String status;
        
        public StockResponse(String sku, String productName, int availableStock,
                           boolean available, String status) {
            this.sku = sku;
            this.productName = productName;
            this.availableStock = availableStock;
            this.available = available;
            this.status = status;
        }
        
        // Getters
        public String getSku() { return sku; }
        public String getProductName() { return productName; }
        public int getAvailableStock() { return availableStock; }
        public boolean isAvailable() { return available; }
        public String getStatus() { return status; }
    }
} 