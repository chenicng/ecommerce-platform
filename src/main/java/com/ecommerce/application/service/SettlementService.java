package com.ecommerce.application.service;

import com.ecommerce.domain.settlement.Settlement;
import com.ecommerce.domain.Money;
import com.ecommerce.infrastructure.repository.SettlementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDate;

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
        
        // In a real project, this should query database for all active merchants
        // For demo purposes, we'll skip this implementation
        logger.info("Settlement execution completed for date: {}", settlementDate);
    }
    
    /**
     * Execute merchant settlement
     */
    public Settlement executeMerchantSettlement(Long merchantId, LocalDate settlementDate) {
        // In production, this would calculate expected income from order records
        Money expectedIncome = Money.zero("CNY");
        Money actualBalance = merchantService.getMerchantBalance(merchantId);
        
        Settlement settlement = new Settlement(merchantId, settlementDate, expectedIncome, actualBalance);
        return settlementRepository.save(settlement);
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