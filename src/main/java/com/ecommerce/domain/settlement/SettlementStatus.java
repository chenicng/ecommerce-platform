package com.ecommerce.domain.settlement;

/**
 * Settlement Status Enum
 */
public enum SettlementStatus {
    /**
     * Matched status - expected income matches actual balance
     */
    MATCHED,
    
    /**
     * Surplus status - actual balance exceeds expected income
     */
    SURPLUS,
    
    /**
     * Deficit status - actual balance is less than expected income
     */
    DEFICIT,
    
    /**
     * Processed status - settlement has been handled
     */
    PROCESSED
} 