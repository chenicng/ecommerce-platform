package com.trading.application.service;

import com.trading.domain.Money;
import com.trading.domain.user.User;
import com.trading.domain.merchant.Merchant;
import com.trading.domain.product.Product;
import com.trading.domain.order.Order;
import com.trading.application.dto.PurchaseRequest;
import com.trading.application.dto.PurchaseResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 交易服务
 * 处理用户购买商品的完整业务流程
 */
@Service
@Transactional
public class TradingService {
    
    private final UserService userService;
    private final MerchantService merchantService;
    private final ProductService productService;
    private final OrderService orderService;
    
    public TradingService(UserService userService, MerchantService merchantService,
                         ProductService productService, OrderService orderService) {
        this.userService = userService;
        this.merchantService = merchantService;
        this.productService = productService;
        this.orderService = orderService;
    }
    
    /**
     * 处理购买请求
     * 完整的业务流程：验证 -> 扣库存 -> 扣钱 -> 加钱 -> 创建订单
     */
    public PurchaseResponse processPurchase(PurchaseRequest request) {
        // 1. 获取并验证相关实体
        User user = userService.getUserById(request.getUserId());
        Product product = productService.getProductBySku(request.getSku());
        Merchant merchant = merchantService.getMerchantById(product.getMerchantId());
        
        // 2. 业务验证
        validatePurchaseRequest(user, product, merchant, request.getQuantity());
        
        // 3. 计算总价
        Money totalPrice = product.calculateTotalPrice(request.getQuantity());
        
        // 4. 检查用户余额
        if (!user.canAfford(totalPrice)) {
            throw new RuntimeException("Insufficient balance. Required: " + totalPrice + 
                                     ", Available: " + user.getBalance());
        }
        
        // 5. 检查商品库存
        if (!product.hasEnoughStock(request.getQuantity())) {
            throw new RuntimeException("Insufficient stock. Required: " + request.getQuantity() + 
                                     ", Available: " + product.getAvailableStock());
        }
        
        try {
            // 6. 创建订单
            String orderNumber = generateOrderNumber();
            Order order = new Order(orderNumber, user.getId(), merchant.getId());
            order.addOrderItem(product.getSku(), product.getName(), product.getPrice(), request.getQuantity());
            order.confirm();
            
            // 7. 扣减库存
            product.reduceStock(request.getQuantity());
            
            // 8. 扣减用户账户
            user.deduct(totalPrice);
            
            // 9. 增加商家收入
            merchant.receiveIncome(totalPrice);
            
            // 10. 处理支付并完成订单
            order.processPayment();
            order.complete();
            
            // 11. 保存所有更改
            userService.saveUser(user);
            productService.saveProduct(product);
            merchantService.saveMerchant(merchant);
            orderService.saveOrder(order);
            
            // 12. 返回结果
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
            // 如果出现异常，事务会自动回滚
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