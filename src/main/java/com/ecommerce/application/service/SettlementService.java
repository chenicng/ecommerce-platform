package com.ecommerce.application.service;

import com.ecommerce.domain.settlement.Settlement;
import com.ecommerce.domain.Money;
import com.ecommerce.domain.order.Order;
import com.ecommerce.domain.merchant.Merchant;
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
     * Execute settlement for all active merchants (called by scheduler)
     * Improved logic: Calculate orders from yesterday settlement to now and compare with current balance
     * No transaction needed - each merchant settlement has its own transaction
     */
    public void executeSettlement() {
        LocalDate settlementDate = LocalDate.now(); // Settle today's data
        
        logger.info("Starting daily settlement for date: {}", settlementDate);
        
        // Get all active merchants
        List<Merchant> activeMerchants = merchantService.getAllActiveMerchants();
        logger.info("Found {} active merchants for settlement", activeMerchants.size());
        
        for (Merchant merchant : activeMerchants) {
            try {
                Settlement settlement = executeMerchantSettlement(merchant.getId(), settlementDate);
                logger.info("Settlement completed for merchant {} ({}): expected={}, actual={}, status={}", 
                          merchant.getId(), merchant.getMerchantName(), 
                          settlement.getExpectedIncome(), settlement.getActualBalance(), 
                          settlement.getStatus());
            } catch (Exception e) {
                logger.error("Failed to settle merchant {} ({}): {}", 
                           merchant.getId(), merchant.getMerchantName(), e.getMessage(), e);
            }
        }
        
        logger.info("Daily settlement task completed for date: {} with {} merchants processed", 
                   settlementDate, activeMerchants.size());
    }
    
    /**
     * Execute merchant settlement with improved logic
     * Validates: Orders from yesterday settlement to now should match current balance
     * If there was a settlement record yesterday, then: yesterday's balance + orders since yesterday = current balance
     * Requires transaction for single merchant settlement atomicity
     */
    @Transactional
    public Settlement executeMerchantSettlement(Long merchantId, LocalDate settlementDate) {
        logger.info("Executing settlement for merchant {} on date {}", merchantId, settlementDate);
        
        // Calculate date range: from yesterday settlement time to now
        LocalDateTime startTime;
        
        // Check if there was a settlement record yesterday
        Optional<Settlement> yesterdaySettlement = getSettlementByMerchantAndDate(merchantId, settlementDate.minusDays(1));
        
        if (yesterdaySettlement.isPresent()) {
            // If there was a settlement yesterday, start from yesterday's settlement time
            startTime = yesterdaySettlement.get().getCreatedAt();
            logger.info("Found yesterday settlement for merchant {}, starting from: {}", merchantId, startTime);
        } else {
            // If no settlement yesterday, start from yesterday's start of day
            startTime = settlementDate.minusDays(1).atStartOfDay();
            logger.info("No yesterday settlement found for merchant {}, starting from: {}", merchantId, startTime);
        }
        
        // Set settlement time point to ensure data consistency between order query and balance retrieval
        LocalDateTime settlementTime = LocalDateTime.now();
         // Get current balance at the same time point as settlementTime
         Money currentBalance = merchantService.getMerchantBalance(merchantId);
         
        // Get completed orders for the merchant from yesterday settlement to settlement time
        List<Order> completedOrders = orderService.getCompletedOrdersByMerchantAndDateRange(
            merchantId, startTime, settlementTime);
        
        // Calculate expected income from completed orders (orders since yesterday settlement)
        Money recentOrderIncome = calculateExpectedIncomeFromOrders(completedOrders);
        
        Money expectedBalance;
        String calculationNotes;
        
        if (yesterdaySettlement.isPresent()) {
            // If there was a settlement yesterday, calculate: yesterday's balance + orders since yesterday
            Money yesterdayBalance = yesterdaySettlement.get().getActualBalance();
            expectedBalance = yesterdayBalance.add(recentOrderIncome);
            calculationNotes = String.format("Yesterday's balance: %s, Orders since yesterday: %s, Expected balance: %s", 
                                          yesterdayBalance, recentOrderIncome, expectedBalance);
        } else {
            // If no settlement yesterday, assume current balance should match recent orders
            expectedBalance = recentOrderIncome;
            calculationNotes = String.format("No previous settlement found, Recent orders income: %s", recentOrderIncome);
        }
        
        // Validate: Expected balance should match current balance
        boolean isMatched = expectedBalance.equals(currentBalance);
        
        logger.info("Settlement calculation for merchant {}: {} completed orders from {} to {}", 
                  merchantId, completedOrders.size(), startTime, settlementTime);
        logger.info("Recent order income: {}, Current balance: {}, Expected balance: {}, Match: {}", 
                  recentOrderIncome, currentBalance, expectedBalance, isMatched);
        
        if (!isMatched) {
            logger.warn("Balance mismatch for merchant {}: expected={}, actual={}", 
                       merchantId, expectedBalance, currentBalance);
        }
        
        // Create settlement record
        Settlement settlement = new Settlement(merchantId, settlementDate, recentOrderIncome, currentBalance);
        
        // Add detailed notes about the calculation
        String notes = String.format("%s, Current balance: %s, Match: %s", 
                                   calculationNotes, currentBalance,
                                   isMatched ? "Yes" : "No");
        settlement.addNotes(notes);
        
        return settlementRepository.save(settlement);
    }
    
    /**
     * Get settlement by merchant ID and date
     * 
     * Note: This is a placeholder implementation for demo purposes.
     * In a real implementation, this would query the repository with:
     * settlementRepository.findByMerchantIdAndSettlementDate(merchantId, settlementDate)
     */
    @Transactional(readOnly = true)
    public Optional<Settlement> getSettlementByMerchantAndDate(Long merchantId, LocalDate settlementDate) {
        // Demo implementation - always return empty to simulate first-time settlement
        // This ensures the settlement logic calculates from beginning of day
        return Optional.empty();
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
    @Transactional
    public void saveSettlement(Settlement settlement) {
        settlementRepository.save(settlement);
    }
} 