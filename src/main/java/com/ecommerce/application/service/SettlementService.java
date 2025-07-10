package com.ecommerce.application.service;

import com.ecommerce.domain.settlement.Settlement;
import com.ecommerce.domain.Money;
import com.ecommerce.domain.order.Order;
import com.ecommerce.infrastructure.repository.SettlementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Settlement Service
 * Handles business logic related to merchant settlement
 */
@Service
@Transactional
public class SettlementService {
    
    private static final Logger logger = LoggerFactory.getLogger(SettlementService.class);
    
    private final SettlementRepository settlementRepository;
    private final MerchantService merchantService;
    private final OrderService orderService;
    
    public SettlementService(SettlementRepository settlementRepository,
                           MerchantService merchantService, 
                           OrderService orderService) {
        this.settlementRepository = settlementRepository;
        this.merchantService = merchantService;
        this.orderService = orderService;
    }
    
    /**
     * Execute settlement for all merchants (called by scheduler)
     */
    public void executeSettlement() {
        LocalDate settlementDate = LocalDate.now().minusDays(1); // Settle previous day's data
        
        logger.info("Starting daily settlement for date: {}", settlementDate);
        
        // In a real project, this should query database for all active merchants
        // For demo purposes, we'll settle for known merchants
        List<Long> merchantIds = List.of(1L, 2L, 3L); // Demo merchant IDs
        
        for (Long merchantId : merchantIds) {
            try {
                if (merchantService.merchantExists(merchantId)) {
                    Settlement settlement = executeMerchantSettlement(merchantId, settlementDate);
                    logger.info("Settlement completed for merchant {}: expected={}, actual={}, status={}", 
                              merchantId, settlement.getExpectedIncome(), settlement.getActualBalance(), 
                              settlement.getStatus());
                }
            } catch (Exception e) {
                logger.error("Failed to settle merchant {}: {}", merchantId, e.getMessage(), e);
            }
        }
        
        logger.info("Daily settlement task completed for date: {}", settlementDate);
    }
    
    /**
     * Execute merchant settlement
     * Validates: Previous day balance + Current day order income = Current day balance
     */
    public Settlement executeMerchantSettlement(Long merchantId, LocalDate settlementDate) {
        logger.info("Executing settlement for merchant {} on date {}", merchantId, settlementDate);
        
        // Calculate date range for the settlement date
        LocalDateTime startOfDay = settlementDate.atStartOfDay();
        LocalDateTime endOfDay = settlementDate.atTime(LocalTime.MAX);
        
        // Get completed orders for the merchant on the settlement date
        List<Order> completedOrders = orderService.getCompletedOrdersByMerchantAndDateRange(
            merchantId, startOfDay, endOfDay);
        
        // Calculate expected income from completed orders (Current day order income)
        Money currentDayOrderIncome = calculateExpectedIncomeFromOrders(completedOrders);
        
        // Get current balance (Current day balance)
        Money currentDayBalance = merchantService.getMerchantBalance(merchantId);
        
        // Calculate previous day balance (Current day balance - Current day order income)
        Money previousDayBalance = currentDayBalance.subtract(currentDayOrderIncome);
        
        logger.info("Settlement calculation for merchant {}: {} completed orders", 
                  merchantId, completedOrders.size());
        logger.info("Previous day balance: {}, Current day order income: {}, Current day balance: {}", 
                  previousDayBalance, currentDayOrderIncome, currentDayBalance);
        
        // Validate: Expected income (from orders) should match the current balance
        // In a real system, we would track previous day balance separately and validate:
        // Previous day balance + Current day order income = Current day balance
        // For this demo, we'll validate that the current balance matches the expected income
        boolean isMatched = currentDayOrderIncome.equals(currentDayBalance);
        
        if (!isMatched) {
            logger.warn("Balance mismatch for merchant {}: expected income={}, actual balance={}", 
                       merchantId, currentDayOrderIncome, currentDayBalance);
        }
        
        // Create settlement record
        Settlement settlement = new Settlement(merchantId, settlementDate, currentDayOrderIncome, currentDayBalance);
        
        // Add notes about the calculation
        String notes = String.format("Previous day balance: %s, Current day order income: %s, Current day balance: %s, Match: %s", 
                                   previousDayBalance, currentDayOrderIncome, currentDayBalance,
                                   isMatched ? "Yes" : "No");
        settlement.addNotes(notes);
        
        return settlementRepository.save(settlement);
    }
    
    /**
     * Calculate expected income from completed orders
     */
    private Money calculateExpectedIncomeFromOrders(List<Order> completedOrders) {
        if (completedOrders.isEmpty()) {
            return Money.zero("CNY");
        }
        
        // Sum up total amounts from all completed orders
        Money totalExpectedIncome = completedOrders.stream()
                .map(Order::getTotalAmount)
                .reduce(Money.zero("CNY"), Money::add);
        
        logger.debug("Calculated expected income from {} orders: {}", 
                   completedOrders.size(), totalExpectedIncome);
        
        return totalExpectedIncome;
    }
    
    /**
     * Get settlement by ID
     */
    @Transactional(readOnly = true)
    public Settlement getSettlementById(Long settlementId) {
        return settlementRepository.findById(settlementId)
                .orElseThrow(() -> new RuntimeException("Settlement not found with id: " + settlementId));
    }
    
    /**
     * Save settlement
     */
    public void saveSettlement(Settlement settlement) {
        settlementRepository.save(settlement);
    }
} 