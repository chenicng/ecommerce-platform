package com.ecommerce.infrastructure.scheduler;

import com.ecommerce.application.service.SettlementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Settlement Scheduled Task
 * Executes merchant settlement daily at 2 AM
 */
@Component
public class SettlementScheduler {
    
    private static final Logger logger = LoggerFactory.getLogger(SettlementScheduler.class);
    
    private final SettlementService settlementService;
    
    public SettlementScheduler(SettlementService settlementService) {
        this.settlementService = settlementService;
    }
    
    /**
     * Daily settlement task
     * Executes daily at 2 AM based on cron expression configuration
     */
    @Scheduled(cron = "${ecommerce.settlement.cron:0 0 2 * * ?}")
    public void executeSettlement() {
        try {
            logger.info("Starting daily settlement task...");
            settlementService.executeSettlement();
            logger.info("Daily settlement task completed successfully");
        } catch (Exception e) {
            logger.error("Daily settlement task failed: {}", e.getMessage(), e);
        }
    }
} 