package com.ecommerce.application.service;

import com.ecommerce.domain.settlement.Settlement;
import com.ecommerce.domain.settlement.SettlementStatus;
import com.ecommerce.domain.Money;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.List;
import java.util.ArrayList;

/**
 * Settlement Service
 * Handles business logic related to merchant settlement
 */
@Service
@Transactional
public class SettlementService {
    
    private static final Logger logger = LoggerFactory.getLogger(SettlementService.class);
    
    // Simple in-memory storage, production should use database
    private final Map<Long, Settlement> settlementStorage = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);
    
    private final MerchantService merchantService;
    private final OrderService orderService;
    
    public SettlementService(MerchantService merchantService, OrderService orderService) {
        this.merchantService = merchantService;
        this.orderService = orderService;
    }
    
    /**
     * Execute settlement for all merchants (called by scheduler)
     */
    public void executeSettlement() {
        LocalDate settlementDate = LocalDate.now().minusDays(1); // Settle previous day's data
        
        // In a real project, this should query database for all active merchants
        // For demo purposes, we'll skip this implementation
        logger.info("Settlement execution completed for date: {}", settlementDate);
    }
    
    /**
     * Execute merchant settlement
     */
    public Settlement performSettlement(Long merchantId, LocalDate settlementDate) {
        try {
            logger.info("Starting settlement for merchant {} on date {}", merchantId, settlementDate);
            
            // 1. Calculate expected income (based on completed orders)
            Money expectedIncome = calculateExpectedIncome(merchantId, settlementDate);
            
            // 2. Get merchant actual balance
            Money actualBalance = merchantService.getMerchantBalance(merchantId);
            
            // 3. Create settlement record
            Settlement settlement = new Settlement(
                merchantId,
                settlementDate,
                expectedIncome,
                actualBalance
            );
            
            // 4. Save settlement record
            saveSettlement(settlement);
            
            // 5. Log settlement result
            logSettlementResult(settlement);
            
            logger.info("Settlement completed for merchant {}: status={}, difference={}", 
                       merchantId, settlement.getStatus(), settlement.getDifference());
            
            return settlement;
            
        } catch (Exception e) {
            logger.error("Settlement failed for merchant {}: {}", merchantId, e.getMessage(), e);
            throw new RuntimeException("Settlement failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Calculate expected income
     * In a real project, this should query database for order records
     */
    private Money calculateExpectedIncome(Long merchantId, LocalDate settlementDate) {
        // Since we use in-memory storage, return merchant's total income as expected income
        // In a real project, this should calculate sales amount for the specified date
        Money totalIncome = merchantService.getMerchantTotalIncome(merchantId);
        
        logger.debug("Calculated expected income for merchant {}: {}", merchantId, totalIncome);
        return totalIncome;
    }
    
    /**
     * Save settlement record
     */
    private void saveSettlement(Settlement settlement) {
        Long id = idGenerator.getAndIncrement();
        settlement.setId(id);
        settlementStorage.put(id, settlement);
    }
    
    /**
     * Log settlement result
     */
    private void logSettlementResult(Settlement settlement) {
        String statusDesc;
        switch (settlement.getStatus()) {
            case MATCHED:
                statusDesc = "Perfect match";
                break;
            case SURPLUS:
                statusDesc = "Has surplus";
                settlement.addNotes("Account balance exceeds expected income " + settlement.getDifference());
                break;
            case DEFICIT:
                statusDesc = "Has deficit";
                settlement.addNotes("Account balance is below expected income " + settlement.getDifference());
                break;
            default:
                statusDesc = "Unknown status";
                break;
        }
        
        logger.info("Settlement result - Merchant ID: {}, Status: {}, Expected income: {}, Actual balance: {}, Difference: {}", 
                   settlement.getMerchantId(), 
                   statusDesc,
                   settlement.getExpectedIncome(),
                   settlement.getActualBalance(),
                   settlement.getDifference());
    }
    
    /**
     * Get settlement record
     */
    @Transactional(readOnly = true)
    public Settlement getSettlementById(Long settlementId) {
        Settlement settlement = settlementStorage.get(settlementId);
        if (settlement == null) {
            throw new RuntimeException("Settlement not found with id: " + settlementId);
        }
        return settlement;
    }
    
    /**
     * Get all settlement records for a merchant
     */
    @Transactional(readOnly = true)
    public List<Settlement> getSettlementsByMerchant(Long merchantId) {
        return settlementStorage.values().stream()
                .filter(s -> s.getMerchantId().equals(merchantId))
                .toList();
    }
    
    /**
     * Get all mismatched settlement records
     */
    @Transactional(readOnly = true)
    public List<Settlement> getMismatchedSettlements() {
        return settlementStorage.values().stream()
                .filter(s -> !s.isMatched())
                .toList();
    }
} 