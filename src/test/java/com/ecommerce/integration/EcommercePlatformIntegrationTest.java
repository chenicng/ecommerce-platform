package com.ecommerce.integration;

import com.ecommerce.application.service.*;
import com.ecommerce.application.dto.PurchaseRequest;
import com.ecommerce.application.dto.PurchaseResponse;
import com.ecommerce.domain.Money;
import com.ecommerce.domain.user.User;
import com.ecommerce.domain.product.Product;
import com.ecommerce.domain.merchant.Merchant;
import com.ecommerce.domain.order.Order;
import com.ecommerce.domain.order.OrderStatus;
import com.ecommerce.domain.settlement.Settlement;
import com.ecommerce.infrastructure.repository.*;
import com.ecommerce.infrastructure.repository.mock.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests that verify complex business workflows
 * Tests the interaction between multiple services and domain objects
 */
@ExtendWith(MockitoExtension.class)
class EcommercePlatformIntegrationTest {

    private UserService userService;
    private ProductService productService;
    private MerchantService merchantService;
    private OrderService orderService;
    private SettlementService settlementService;
    private EcommerceService ecommerceService;

    private UserRepository userRepository;
    private ProductRepository productRepository;
    private MerchantRepository merchantRepository;
    private OrderRepository orderRepository;
    private SettlementRepository settlementRepository;

    @BeforeEach
    void setUp() {
        // Setup mock repositories
        userRepository = new MockUserRepository();
        productRepository = new MockProductRepository();
        merchantRepository = new MockMerchantRepository();
        orderRepository = new MockOrderRepository();
        settlementRepository = new MockSettlementRepository();

        // Setup services
        userService = new UserService(userRepository);
        productService = new ProductService(productRepository);
        merchantService = new MerchantService(merchantRepository);
        orderService = new OrderService(orderRepository);
        settlementService = new SettlementService(settlementRepository, merchantService, orderService);
        ecommerceService = new EcommerceService(userService, merchantService, productService, orderService);
    }

    @Test
    void shouldCompleteFullPurchaseWorkflow() {
        // 1. Create merchant
        Merchant merchant = merchantService.createMerchant(
            "Tech Store", "BL123456", "tech@store.com", "555-1234");
        assertNotNull(merchant.getId());

        // 2. Create product
        Product product = productService.createProduct(
            "LAPTOP001", "Gaming Laptop", "High-performance gaming laptop",
            Money.of("1500.00", "CNY"), merchant.getId(), 10);
        assertNotNull(product);

        // 3. Create user
        User user = userService.createUser("gamer123", "gamer@example.com", "555-9876", "CNY");
        assertNotNull(user.getId());

        // 4. User recharge
        userService.rechargeUser(user.getId(), Money.of("4000.00", "CNY"));
        Money userBalance = userService.getUserBalance(user.getId());
        assertEquals(Money.of("4000.00", "CNY"), userBalance);

        // 5. Purchase product
        PurchaseRequest request = new PurchaseRequest(user.getId(), "LAPTOP001", 2);
        PurchaseResponse response = ecommerceService.processPurchase(request);

        // 6. Verify purchase result
        assertEquals(Money.of("3000.00", "CNY"), response.getTotalAmount()); // 1500 * 2

        // 7. Verify user balance after purchase
        Money finalUserBalance = userService.getUserBalance(user.getId());
        assertEquals(Money.of("1000.00", "CNY"), finalUserBalance); // 4000 - 3000 = 1000

        // 8. Verify merchant received payment
        Money merchantBalance = merchantService.getMerchantBalance(merchant.getId());
        assertEquals(Money.of("3000.00", "CNY"), merchantBalance);

        // 9. Verify product inventory reduced
        Product updatedProduct = productService.getProductBySku("LAPTOP001");
        assertEquals(8, updatedProduct.getAvailableInventory()); // 10 - 2

        // 10. Verify order was created
        Order order = orderService.getOrderByNumber(response.getOrderNumber());
        assertEquals(OrderStatus.COMPLETED, order.getStatus());
        assertEquals(Money.of("3000.00", "CNY"), order.getTotalAmount());
    }

    @Test
    void shouldHandlePurchaseAndCancellationWorkflow() {
        // Setup
        Merchant merchant = merchantService.createMerchant(
            "Electronics Store", "BL789012", "electronics@store.com", "555-5678");
        Product product = productService.createProduct(
            "PHONE001", "Smartphone", "Latest smartphone",
            Money.of("800.00", "CNY"), merchant.getId(), 20);
        User user = userService.createUser("buyer456", "buyer@example.com", "555-4321", "CNY");
        userService.rechargeUser(user.getId(), Money.of("1000.00", "CNY"));

        // Purchase
        PurchaseRequest request = new PurchaseRequest(user.getId(), "PHONE001", 1);
        PurchaseResponse response = ecommerceService.processPurchase(request);

        // Verify state after purchase
        assertEquals(Money.of("200.00", "CNY"), userService.getUserBalance(user.getId())); // 1000 - 800
        assertEquals(Money.of("800.00", "CNY"), merchantService.getMerchantBalance(merchant.getId()));
        assertEquals(19, productService.getProductBySku("PHONE001").getAvailableInventory()); // 20 - 1

        // Verify order is completed and cannot be cancelled (this is the expected business behavior)
        Order order = orderService.getOrderByNumber(response.getOrderNumber());
        assertTrue(order.isCompleted());
        
        // Attempt to cancel completed order should fail
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> ecommerceService.cancelOrder(response.getOrderNumber(), "Customer request"));
        assertTrue(exception.getMessage().contains("Cannot cancel completed order"));

        // Verify nothing changed after failed cancellation attempt
        assertEquals(Money.of("200.00", "CNY"), userService.getUserBalance(user.getId()));
        assertEquals(Money.of("800.00", "CNY"), merchantService.getMerchantBalance(merchant.getId()));
        assertEquals(19, productService.getProductBySku("PHONE001").getAvailableInventory());
        assertTrue(order.isCompleted()); // Order remains completed
    }

    @Test
    void shouldHandleMultipleUsersAndProducts() {
        // Setup merchants
        Merchant merchant1 = merchantService.createMerchant(
            "Store A", "BL111111", "storea@example.com", "555-1111");
        Merchant merchant2 = merchantService.createMerchant(
            "Store B", "BL222222", "storeb@example.com", "555-2222");

        // Setup products
        Product product1 = productService.createProduct(
            "BOOK001", "Programming Book", "Learn programming",
            Money.of("50.00", "CNY"), merchant1.getId(), 100);
        Product product2 = productService.createProduct(
            "GAME001", "Video Game", "Action game",
            Money.of("60.00", "CNY"), merchant2.getId(), 50);

        // Setup users
        User user1 = userService.createUser("reader", "reader@example.com", "555-1000", "CNY");
        User user2 = userService.createUser("gamer", "gamer@example.com", "555-2000", "CNY");
        userService.rechargeUser(user1.getId(), Money.of("200.00", "CNY"));
        userService.rechargeUser(user2.getId(), Money.of("300.00", "CNY"));

        // User 1 buys books
        PurchaseRequest request1 = new PurchaseRequest(user1.getId(), "BOOK001", 3);
        PurchaseResponse response1 = ecommerceService.processPurchase(request1);

        // User 2 buys games
        PurchaseRequest request2 = new PurchaseRequest(user2.getId(), "GAME001", 2);
        PurchaseResponse response2 = ecommerceService.processPurchase(request2);

        // Verify final states
        assertEquals(Money.of("50.00", "CNY"), userService.getUserBalance(user1.getId())); // 200 - 150
        assertEquals(Money.of("180.00", "CNY"), userService.getUserBalance(user2.getId())); // 300 - 120
        assertEquals(Money.of("150.00", "CNY"), merchantService.getMerchantBalance(merchant1.getId()));
        assertEquals(Money.of("120.00", "CNY"), merchantService.getMerchantBalance(merchant2.getId()));
        assertEquals(97, productService.getProductBySku("BOOK001").getAvailableInventory()); // 100 - 3
        assertEquals(48, productService.getProductBySku("GAME001").getAvailableInventory()); // 50 - 2
    }

    @Test
    void shouldHandleInsufficientBalanceGracefully() {
        // Setup
        Merchant merchant = merchantService.createMerchant(
            "Luxury Store", "BL999999", "luxury@store.com", "555-9999");
        Product expensiveProduct = productService.createProduct(
            "LUXURY001", "Luxury Watch", "Premium watch",
            Money.of("5000.00", "CNY"), merchant.getId(), 5);
        User poorUser = userService.createUser("poor_user", "poor@example.com", "555-0001", "CNY");
        userService.rechargeUser(poorUser.getId(), Money.of("100.00", "CNY")); // Not enough for luxury watch

        // Attempt purchase
        PurchaseRequest request = new PurchaseRequest(poorUser.getId(), "LUXURY001", 1);
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> ecommerceService.processPurchase(request));
        assertTrue(exception.getMessage().contains("Insufficient balance"));

        // Verify nothing changed
        assertEquals(Money.of("100.00", "CNY"), userService.getUserBalance(poorUser.getId()));
        assertEquals(Money.zero("CNY"), merchantService.getMerchantBalance(merchant.getId()));
        assertEquals(5, productService.getProductBySku("LUXURY001").getAvailableInventory());
    }

    @Test
    void shouldHandleInsufficientInventoryGracefully() {
        // Setup
        Merchant merchant = merchantService.createMerchant(
            "Limited Store", "BL777777", "limited@store.com", "555-7777");
        Product limitedProduct = productService.createProduct(
            "LIMITED001", "Limited Edition", "Rare item",
            Money.of("100.00", "CNY"), merchant.getId(), 2); // Only 2 in stock
        User buyer = userService.createUser("buyer", "buyer@example.com", "555-0002", "CNY");
        userService.rechargeUser(buyer.getId(), Money.of("1000.00", "CNY")); // Enough money

        // Attempt to buy more than available
        PurchaseRequest request = new PurchaseRequest(buyer.getId(), "LIMITED001", 5); // Want 5, only 2 available
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> ecommerceService.processPurchase(request));
        assertTrue(exception.getMessage().contains("Insufficient inventory"));

        // Verify nothing changed
        assertEquals(Money.of("1000.00", "CNY"), userService.getUserBalance(buyer.getId()));
        assertEquals(Money.zero("CNY"), merchantService.getMerchantBalance(merchant.getId()));
        assertEquals(2, productService.getProductBySku("LIMITED001").getAvailableInventory());
    }

    @Test
    void shouldHandleSettlementWorkflow() {
        // Setup
        Merchant merchant = merchantService.createMerchant(
            "Settlement Store", "BL555555", "settlement@store.com", "555-5555");
        
        // Simulate some business activity
        merchant.receiveIncome(Money.of("1000.00", "CNY"));
        merchant.withdrawIncome(Money.of("200.00", "CNY"));
        merchantService.saveMerchant(merchant);

        // Execute settlement
        LocalDate settlementDate = LocalDate.now();
        Settlement settlement = settlementService.executeMerchantSettlement(
            merchant.getId(), settlementDate);

        // Verify settlement
        assertNotNull(settlement);
        assertEquals(merchant.getId(), settlement.getMerchantId());
        assertEquals(settlementDate, settlement.getSettlementDate());
        assertEquals(Money.zero("CNY"), settlement.getExpectedIncome()); // Mock calculation
        assertEquals(Money.of("800.00", "CNY"), settlement.getActualBalance()); // 1000 - 200
        assertTrue(settlement.hasSurplus()); // More than expected (0)
    }

    @Test
    void shouldMaintainDataConsistencyAcrossOperations() {
        // Setup complete ecosystem
        Merchant merchant = merchantService.createMerchant(
            "Consistency Store", "BL333333", "consistency@store.com", "555-3333");
        Product product = productService.createProduct(
            "CONSISTENT001", "Test Product", "For consistency testing",
            Money.of("25.00", "CNY"), merchant.getId(), 100);
        User user = userService.createUser("consistent_user", "consistent@example.com", "555-0003", "CNY");

        // Initial state
        userService.rechargeUser(user.getId(), Money.of("500.00", "CNY"));
        Money initialUserBalance = Money.of("500.00", "CNY");
        Money initialMerchantBalance = Money.zero("CNY");
        int initialInventory = 100;

        // Perform multiple operations
        for (int i = 1; i <= 10; i++) {
            PurchaseRequest request = new PurchaseRequest(user.getId(), "CONSISTENT001", 1);
            PurchaseResponse response = ecommerceService.processPurchase(request);
        }

        // Verify final state
        Money expectedUserBalance = initialUserBalance.subtract(Money.of("250.00", "CNY")); // 25 * 10
        Money expectedMerchantBalance = initialMerchantBalance.add(Money.of("250.00", "CNY"));
        int expectedInventory = initialInventory - 10;

        assertEquals(expectedUserBalance, userService.getUserBalance(user.getId()));
        assertEquals(expectedMerchantBalance, merchantService.getMerchantBalance(merchant.getId()));
        assertEquals(expectedInventory, productService.getProductBySku("CONSISTENT001").getAvailableInventory());

        // Verify total system balance (user + merchant should equal initial user balance)
        Money totalSystemBalance = userService.getUserBalance(user.getId())
            .add(merchantService.getMerchantBalance(merchant.getId()));
        assertEquals(initialUserBalance, totalSystemBalance);
    }

    @Test
    void shouldMatchOrderIncomeAndMerchantBalanceInSettlement() {
        // 1. Create merchant, product, user
        Merchant merchant = merchantService.createMerchant(
            "Settlement Test Merchant", "BL000001", "settle@store.com", "555-0000");
        Product product = productService.createProduct(
            "SETTLE001", "Settlement Product", "For settlement testing",
            Money.of("100.00", "CNY"), merchant.getId(), 10);
        User user = userService.createUser("settle_user", "settle@example.com", "555-0001", "CNY");
        userService.rechargeUser(user.getId(), Money.of("500.00", "CNY"));

        // 2. User purchases 2 products
        PurchaseRequest request = new PurchaseRequest(user.getId(), "SETTLE001", 2);
        PurchaseResponse response = ecommerceService.processPurchase(request);

        // 3. Verify merchant balance and order income
        Money expectedIncome = Money.of("200.00", "CNY"); // 100 * 2
        Money merchantBalance = merchantService.getMerchantBalance(merchant.getId());
        assertEquals(expectedIncome, merchantBalance);

        // 4. Execute settlement
        LocalDate today = LocalDate.now();
        Settlement settlement = settlementService.executeMerchantSettlement(merchant.getId(), today);

        // 5. Verify settlement record
        assertNotNull(settlement);
        assertEquals(expectedIncome, settlement.getExpectedIncome());
        assertEquals(merchantBalance, settlement.getActualBalance());
        assertEquals(Money.zero("CNY"), settlement.getDifference());
        assertTrue(settlement.isMatched());
        assertTrue(settlement.getNotes().contains("Match: Yes"));
    }

    @Test
    void shouldDetectBalanceMismatchInSettlement() {
        // 1. Create merchant, product, user
        Merchant merchant = merchantService.createMerchant(
            "Mismatch Test Merchant", "BL000002", "mismatch@store.com", "555-0002");
        Product product = productService.createProduct(
            "MISMATCH001", "Mismatch Product", "For mismatch testing",
            Money.of("50.00", "CNY"), merchant.getId(), 5);
        User user = userService.createUser("mismatch_user", "mismatch@example.com", "555-0003", "CNY");
        userService.rechargeUser(user.getId(), Money.of("100.00", "CNY"));

        // 2. User purchases 1 product
        PurchaseRequest request = new PurchaseRequest(user.getId(), "MISMATCH001", 1);
        PurchaseResponse response = ecommerceService.processPurchase(request);

        // 3. Manually adjust merchant balance to create mismatch
        Merchant loaded = merchantService.getMerchantById(merchant.getId());
        loaded.receiveIncome(Money.of("100.00", "CNY")); // Add extra amount
        merchantService.saveMerchant(loaded);

        // 4. Execute settlement
        LocalDate today = LocalDate.now();
        Settlement settlement = settlementService.executeMerchantSettlement(merchant.getId(), today);

        // 5. Verify settlement record
        assertNotNull(settlement);
        assertEquals(Money.of("50.00", "CNY"), settlement.getExpectedIncome());
        assertEquals(Money.of("100.00", "CNY"), settlement.getDifference());
        assertFalse(settlement.isMatched());
        assertTrue(settlement.getNotes().contains("Match: No"));
    }

    @Test
    void shouldSupportManualAndScheduledSettlement() {
        // 1. Create merchant, product, user
        Merchant merchant = merchantService.createMerchant(
            "Scheduled Settlement Merchant", "BL000003", "scheduled@store.com", "555-0004");
        Product product = productService.createProduct(
            "SCHEDULED001", "Scheduled Product", "For scheduled settlement testing",
            Money.of("20.00", "CNY"), merchant.getId(), 3);
        User user = userService.createUser("scheduled_user", "scheduled@example.com", "555-0005", "CNY");
        userService.rechargeUser(user.getId(), Money.of("100.00", "CNY"));

        // 2. User purchases 2 products
        PurchaseRequest request = new PurchaseRequest(user.getId(), "SCHEDULED001", 2);
        PurchaseResponse response = ecommerceService.processPurchase(request);

        // 3. Manual settlement
        LocalDate today = LocalDate.now();
        Settlement manualSettlement = settlementService.executeMerchantSettlement(merchant.getId(), today);
        assertNotNull(manualSettlement);
        assertEquals(Money.of("40.00", "CNY"), manualSettlement.getExpectedIncome());

        // 4. Scheduled settlement (global)
        assertDoesNotThrow(() -> settlementService.executeSettlement());
    }
} 