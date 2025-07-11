package com.ecommerce.application.service;

import com.ecommerce.domain.Money;
import com.ecommerce.domain.user.User;
import com.ecommerce.domain.merchant.Merchant;
import com.ecommerce.domain.product.Product;
import com.ecommerce.domain.order.Order;
import com.ecommerce.application.dto.PurchaseRequest;
import com.ecommerce.application.dto.PurchaseResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Ecommerce Service
 * Handles the complete business process of user purchasing products
 * 
 * Performance Notes:
 * - For high-concurrency scenarios, consider implementing optimistic locking
 * - Inventory deduction could benefit from distributed locks (Redis) in clustered environments
 * - Order number generation is now thread-safe and includes business context
 */
@Service
public class EcommerceService {
    
    private final UserService userService;
    private final MerchantService merchantService;
    private final ProductService productService;
    private final OrderService orderService;
    
    // Order number generation - format: ORD202507110001 (12 chars, supports 9999 orders/day)
    private static final AtomicLong orderSequence = new AtomicLong(1);
    private static final DateTimeFormatter ORDER_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static volatile String lastOrderDate = "";
    
    public EcommerceService(UserService userService, MerchantService merchantService,
                          ProductService productService, OrderService orderService) {
        this.userService = userService;
        this.merchantService = merchantService;
        this.productService = productService;
        this.orderService = orderService;
    }
    
    /**
     * Process purchase request
     * Complete business flow: validate -> create order -> deduct inventory -> confirm order -> deduct money -> add money -> complete order
     * Requires transaction due to multiple atomic operations
     */
    @Transactional
    public PurchaseResponse processPurchase(PurchaseRequest request) {
        try {
            // 1. Validate and prepare purchase entities
            PurchaseContext context = preparePurchaseContext(request);
            
            // 2. Validate business rules
            validatePurchaseBusinessRules(context);
            
            // 3. Execute purchase transaction
            Order order = executePurchaseTransaction(context);
            
            // 4. Save all changes
            savePurchaseChanges(context, order);
            
            // 5. Return response
            return createPurchaseResponse(context, order);
            
        } catch (com.ecommerce.api.exception.BusinessException e) {
            // Business exceptions should be re-thrown as-is to preserve error codes
            throw e;
        } catch (Exception e) {
            // System exceptions get wrapped with generic error code
            throw new com.ecommerce.api.exception.BusinessException(
                com.ecommerce.api.dto.ErrorCode.INTERNAL_ERROR, 
                "Purchase failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Prepare purchase context with all required entities
     */
    private PurchaseContext preparePurchaseContext(PurchaseRequest request) {
        User user = userService.getUserById(request.getUserId());
        Product product = productService.getProductBySku(request.getSku());
        Merchant merchant = merchantService.getMerchantById(product.getMerchantId());
        Money totalPrice = product.calculateTotalPrice(request.getQuantity());
        
        return new PurchaseContext(user, product, merchant, request.getQuantity(), totalPrice);
    }
    
    /**
     * Validate all business rules for the purchase
     */
    private void validatePurchaseBusinessRules(PurchaseContext context) {
        // Validate entities are active
        validatePurchaseRequest(context.user, context.product, context.merchant, context.quantity);
        
        // Check user balance
        if (!context.user.canAfford(context.totalPrice)) {
            throw new com.ecommerce.domain.user.InsufficientBalanceException(
                "Insufficient balance. Required: " + context.totalPrice + ", Available: " + context.user.getBalance());
        }
        
        // Check product inventory
        if (!context.product.hasEnoughInventory(context.quantity)) {
            throw new com.ecommerce.domain.product.InsufficientInventoryException(
                "Insufficient inventory. Required: " + context.quantity + ", Available: " + context.product.getAvailableInventory());
        }
    }
    
    /**
     * Execute the core purchase transaction
     */
    private Order executePurchaseTransaction(PurchaseContext context) {
        // Create order
        String orderNumber = generateOrderNumber();
        Order order = new Order(orderNumber, context.user.getId(), context.merchant.getId());
        order.addOrderItem(context.product.getSku(), context.product.getName(), 
                          context.product.getPrice(), context.quantity);
        
        // Execute transaction steps
        context.product.reduceInventory(context.quantity);
        order.confirm();
        context.user.deduct(context.totalPrice);
        context.merchant.receiveIncome(context.totalPrice);
        order.processPayment();
        order.complete();
        
        return order;
    }
    
    /**
     * Save all changes from the purchase
     */
    private void savePurchaseChanges(PurchaseContext context, Order order) {
        userService.saveUser(context.user);
        productService.saveProduct(context.product);
        merchantService.saveMerchant(context.merchant);
        orderService.saveOrder(order);
    }
    
    /**
     * Create purchase response
     */
    private PurchaseResponse createPurchaseResponse(PurchaseContext context, Order order) {
        return new PurchaseResponse(
            order.getOrderNumber(),
            context.user.getId(),
            context.merchant.getId(),
            context.product.getSku(),
            context.product.getName(),
            context.quantity,
            context.totalPrice
        );
    }
    
    /**
     * Inner class to hold purchase context data
     */
    private static class PurchaseContext {
        final User user;
        final Product product;
        final Merchant merchant;
        final int quantity;
        final Money totalPrice;
        
        PurchaseContext(User user, Product product, Merchant merchant, int quantity, Money totalPrice) {
            this.user = user;
            this.product = product;
            this.merchant = merchant;
            this.quantity = quantity;
            this.totalPrice = totalPrice;
        }
    }
    
    private void validatePurchaseRequest(User user, Product product, Merchant merchant, int quantity) {
        if (!user.isActive()) {
            throw new com.ecommerce.domain.ResourceInactiveException("User is not active. User ID: " + user.getId());
        }
        
        if (!product.isActive()) {
            throw new com.ecommerce.domain.ResourceInactiveException("Product is not active: " + product.getSku());
        }
        
        if (!merchant.isActive()) {
            throw new com.ecommerce.domain.ResourceInactiveException("Merchant is not active. Merchant ID: " + merchant.getId());
        }
        
        if (quantity <= 0) {
            throw new com.ecommerce.api.exception.BusinessException(
                com.ecommerce.api.dto.ErrorCode.VALIDATION_ERROR, "Quantity must be positive");
        }
    }
    
    /**
     * Cancel order with proper refund and inventory restore handling
     * Requires transaction due to multiple atomic operations
     */
    @Transactional
    public void cancelOrder(String orderNumber, String reason) {
        try {
            // 1. Get order
            Order order = orderService.getOrderByNumber(orderNumber);
            
            // 2. Check if order can be cancelled
            if (order.isCompleted()) {
                throw new com.ecommerce.domain.order.InvalidOrderStateException("Cannot cancel completed order");
            }
            
            if (order.isCancelled()) {
                throw new com.ecommerce.domain.order.InvalidOrderStateException("Order is already cancelled");
            }
            
            // 3. Handle refund if needed
            if (order.needsRefund()) {
                handleRefund(order);
            }
            
            // 4. Handle inventory restore if needed  
            if (order.needsInventoryRestore()) {
                handleInventoryRestore(order);
            }
            
            // 5. Cancel the order
            order.cancel(reason);
            
            // 6. Save changes
            orderService.saveOrder(order);
            
        } catch (com.ecommerce.api.exception.BusinessException e) {
            // Business exceptions should be re-thrown as-is to preserve error codes
            throw e;
        } catch (Exception e) {
            throw new com.ecommerce.api.exception.BusinessException(
                com.ecommerce.api.dto.ErrorCode.INTERNAL_ERROR,
                "Failed to cancel order: " + e.getMessage(), e);
        }
    }
    
    /**
     * Handle refund when cancelling paid order
     */
    private void handleRefund(Order order) {
        // 1. Get user and merchant
        User user = userService.getUserById(order.getUserId());
        Merchant merchant = merchantService.getMerchantById(order.getMerchantId());
        
        Money refundAmount = order.getTotalAmount();
        
        // 2. First deduct money from merchant (reverse the income)
        if (merchant.canWithdraw(refundAmount)) {
            merchant.withdrawIncome(refundAmount);
        } else {
            // In real scenarios, this might need to be handled differently
            // e.g., create a debt record for the merchant
            throw new com.ecommerce.domain.merchant.InsufficientFundsException(
                "Merchant has insufficient funds for refund. Required: " + refundAmount + 
                ", Available: " + merchant.getBalance());
        }
        
        // 3. Then refund money to user (only after merchant deduction succeeds)
        user.recharge(refundAmount);
        
        // 4. Save changes
        userService.saveUser(user);
        merchantService.saveMerchant(merchant);
    }
    
    /**
     * Handle inventory restore when cancelling confirmed order
     */
    private void handleInventoryRestore(Order order) {
        for (var item : order.getItems()) {
            Product product = productService.getProductBySku(item.getSku());
            product.addInventory(item.getQuantity());
            productService.saveProduct(product);
        }
    }

    private String generateOrderNumber() {
        String currentDate = LocalDateTime.now().format(ORDER_DATE_FORMAT);
        
        // Reset sequence if date changed (new day)
        if (!currentDate.equals(lastOrderDate)) {
            synchronized (this) {
                if (!currentDate.equals(lastOrderDate)) {
                    lastOrderDate = currentDate;
                    orderSequence.set(1);
                }
            }
        }
        
        // Generate sequence with fixed 4 digits (0001, 0002, ... 9999)
        long sequence = orderSequence.getAndIncrement();
        if (sequence > 9999) {
            throw new IllegalStateException("Daily order limit exceeded (9999). Consider implementing additional scaling solutions.");
        }
        return String.format("ORD%s%04d", currentDate, sequence);
    }
} 