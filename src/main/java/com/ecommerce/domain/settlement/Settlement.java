package com.ecommerce.domain.settlement;

import com.ecommerce.domain.BaseEntity;
import com.ecommerce.domain.Money;
import java.time.LocalDate;

/**
 * Settlement Aggregate Root
 * Records daily settlement information for merchants
 */
public class Settlement extends BaseEntity {
    
    private Long merchantId;
    private LocalDate settlementDate;
    private Money expectedIncome;  // Expected income calculated from sales records
    private Money actualBalance;   // Actual balance in merchant account
    private Money difference;      // Difference amount
    private SettlementStatus status;
    private String notes;
    
    // Constructor
    protected Settlement() {
        super();
    }
    
    public Settlement(Long merchantId, LocalDate settlementDate, 
                     Money expectedIncome, Money actualBalance) {
        super();
        this.merchantId = merchantId;
        this.settlementDate = settlementDate;
        this.expectedIncome = expectedIncome;
        this.actualBalance = actualBalance;
        this.difference = calculateDifference();
        this.status = determineStatus();
        this.notes = "";
    }
    
    /**
     * Calculate difference amount
     */
    private Money calculateDifference() {
        if (this.expectedIncome == null || this.actualBalance == null) {
            return Money.zero("CNY");
        }
        return this.actualBalance.subtract(this.expectedIncome);
    }
    
    /**
     * Determine settlement status based on difference
     */
    private SettlementStatus determineStatus() {
        if (this.difference == null || this.difference.isZero()) {
            return SettlementStatus.MATCHED;
        } else if (this.difference.getAmount().signum() > 0) {
            return SettlementStatus.SURPLUS;
        } else {
            return SettlementStatus.DEFICIT;
        }
    }
    
    /**
     * Add notes
     */
    public void addNotes(String notes) {
        this.notes = notes != null ? notes : "";
        this.markAsUpdated();
    }
    
    /**
     * Check if matched
     */
    public boolean isMatched() {
        return this.status == SettlementStatus.MATCHED;
    }
    
    /**
     * Check if has surplus
     */
    public boolean hasSurplus() {
        return this.status == SettlementStatus.SURPLUS;
    }
    
    /**
     * Check if has deficit
     */
    public boolean hasDeficit() {
        return this.status == SettlementStatus.DEFICIT;
    }
    
    // Getters
    public Long getMerchantId() {
        return merchantId;
    }
    
    public LocalDate getSettlementDate() {
        return settlementDate;
    }
    
    public Money getExpectedIncome() {
        return expectedIncome;
    }
    
    public Money getActualBalance() {
        return actualBalance;
    }
    
    public Money getDifference() {
        return difference;
    }
    
    public SettlementStatus getStatus() {
        return status;
    }
    
    public String getNotes() {
        return notes;
    }
    
    // Package private setters for JPA
    void setMerchantId(Long merchantId) {
        this.merchantId = merchantId;
    }
    
    void setSettlementDate(LocalDate settlementDate) {
        this.settlementDate = settlementDate;
    }
    
    void setExpectedIncome(Money expectedIncome) {
        this.expectedIncome = expectedIncome;
    }
    
    void setActualBalance(Money actualBalance) {
        this.actualBalance = actualBalance;
    }
    
    void setDifference(Money difference) {
        this.difference = difference;
    }
    
    void setStatus(SettlementStatus status) {
        this.status = status;
    }
    
    void setNotes(String notes) {
        this.notes = notes;
    }
} 