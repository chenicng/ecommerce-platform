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
import java.util.UUID;

/**
 * Ecommerce Service
 * Handles the complete business process of user purchasing products
 */
@Service
@Transactional
public class EcommerceService {
    
    private final UserService userService;
    private final MerchantService merchantService;
    private final ProductService productService;
    private final OrderService orderService;
    
    public EcommerceService(UserService userService, MerchantService merchantService,
                          ProductService productService, OrderService orderService) {
        this.userService = userService;
        this.merchantService = merchantService;
        this.productService = productService;
        this.orderService = orderService;
    }
    
    /**
     * Process purchase request
     * Complete business flow: validate -> deduct inventory -> deduct money -> add money -> create order
     */
    public PurchaseResponse processPurchase(PurchaseRequest request) {
        // 1. Get and validate related entities
        User user = userService.getUserById(request.getUserId());
        Product product = productService.getProductBySku(request.getSku());
        Merchant merchant = merchantService.getMerchantById(product.getMerchantId());
        
        // 2. Business validation
        validatePurchaseRequest(user, product, merchant, request.getQuantity());
        
        // 3. Calculate total price
        Money totalPrice = product.calculateTotalPrice(request.getQuantity());
        
        // 4. Check user balance
        if (!user.canAfford(totalPrice)) {
            throw new RuntimeException("Insufficient balance. Required: " + totalPrice + 
                                     ", Available: " + user.getBalance());
        }
        
        // 5. Check product inventory
        if (!product.hasEnoughStock(request.getQuantity())) {
            throw new RuntimeException("Insufficient stock. Required: " + request.getQuantity() + 
                                     ", Available: " + product.getAvailableStock());
        }
        
        try {
            // 6. Create order
            String orderNumber = generateOrderNumber();
            Order order = new Order(orderNumber, user.getId(), merchant.getId());
            order.addOrderItem(product.getSku(), product.getName(), product.getPrice(), request.getQuantity());
            order.confirm();
            
            // 7. Reduce inventory
            product.reduceStock(request.getQuantity());
            
            // 8. Deduct user account
            user.deduct(totalPrice);
            
            // 9. Add merchant income
            merchant.receiveIncome(totalPrice);
            
            // 10. Process payment and complete order
            order.processPayment();
            order.complete();
            
            // 11. Save all changes
            userService.saveUser(user);
            productService.saveProduct(product);
            merchantService.saveMerchant(merchant);
            orderService.saveOrder(order);
            
            // 12. Return result
            return new PurchaseResponse(
                order.getOrderNumber(),
                user.getId(),
                merchant.getId(),
                product.getSku(),
                product.getName(),
                request.getQuantity(),
                totalPrice,
                "SUCCESS",
                "Purchase completed successfully"
            );
            
        } catch (Exception e) {
            // If an exception occurs, the transaction will automatically rollback
            throw new RuntimeException("Purchase failed: " + e.getMessage(), e);
        }
    }
    
    private void validatePurchaseRequest(User user, Product product, Merchant merchant, int quantity) {
        if (!user.isActive()) {
            throw new RuntimeException("User is not active");
        }
        
        if (!product.isActive()) {
            throw new RuntimeException("Product is not active");
        }
        
        if (!merchant.isActive()) {
            throw new RuntimeException("Merchant is not active");
        }
        
        if (quantity <= 0) {
            throw new RuntimeException("Quantity must be positive");
        }
    }
    
    private String generateOrderNumber() {
        return "ORD-" + LocalDateTime.now().toString().replace(":", "").replace("-", "").replace(".", "") + 
               "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
} 