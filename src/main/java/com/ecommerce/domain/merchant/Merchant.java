package com.ecommerce.domain.merchant;

import com.ecommerce.domain.BaseEntity;
import com.ecommerce.domain.Money;

/**
 * Merchant Aggregate Root
 * Contains merchant basic information and income account
 */
public class Merchant extends BaseEntity {
    
    private String merchantName;
    private String businessLicense;
    private String contactEmail;
    private String contactPhone;
    private MerchantAccount account;
    private MerchantStatus status;
    
    // Constructor
    protected Merchant() {
        super();
    }
    
    public Merchant(String merchantName, String businessLicense, 
                   String contactEmail, String contactPhone) {
        super();
        this.merchantName = merchantName;
        this.businessLicense = businessLicense;
        this.contactEmail = contactEmail;
        this.contactPhone = contactPhone;
        this.account = new MerchantAccount(Money.zero("CNY"));
        this.status = MerchantStatus.ACTIVE;
    }
    
    /**
     * Receive income
     */
    public void receiveIncome(Money amount) {
        validateActiveStatus();
        if (amount == null || amount.isZero()) {
            throw new IllegalArgumentException("Income amount must be positive");
        }
        this.account = this.account.addIncome(amount);
        this.markAsUpdated();
    }
    
    /**
     * Withdraw income
     */
    public void withdrawIncome(Money amount) {
        validateActiveStatus();
        if (amount == null || amount.isZero()) {
            throw new IllegalArgumentException("Withdrawal amount must be positive");
        }
        
        if (!canWithdraw(amount)) {
            throw new InsufficientFundsException("Insufficient funds for withdrawal");
        }
        
        this.account = this.account.withdraw(amount);
        this.markAsUpdated();
    }
    
    /**
     * Check if can withdraw specified amount
     */
    public boolean canWithdraw(Money amount) {
        return this.account.hasEnoughBalance(amount);
    }
    
    /**
     * Get account balance
     */
    public Money getBalance() {
        return this.account.getBalance();
    }
    
    /**
     * Get total income
     */
    public Money getTotalIncome() {
        return this.account.getTotalIncome();
    }
    
    /**
     * Activate merchant
     */
    public void activate() {
        this.status = MerchantStatus.ACTIVE;
        this.markAsUpdated();
    }
    
    /**
     * Deactivate merchant
     */
    public void deactivate() {
        this.status = MerchantStatus.INACTIVE;
        this.markAsUpdated();
    }
    
    private void validateActiveStatus() {
        if (!isActive()) {
            throw new IllegalStateException("Merchant is not active");
        }
    }
    
    public boolean isActive() {
        return MerchantStatus.ACTIVE.equals(this.status);
    }
    
    // Getters
    public String getMerchantName() {
        return merchantName;
    }
    
    public String getBusinessLicense() {
        return businessLicense;
    }
    
    public String getContactEmail() {
        return contactEmail;
    }
    
    public String getContactPhone() {
        return contactPhone;
    }
    
    public MerchantStatus getStatus() {
        return status;
    }
    
    public MerchantAccount getAccount() {
        return account;
    }
    
    // Package private setters for JPA
    void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }
    
    void setBusinessLicense(String businessLicense) {
        this.businessLicense = businessLicense;
    }
    
    void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }
    
    void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }
    
    void setAccount(MerchantAccount account) {
        this.account = account;
    }
    
    void setStatus(MerchantStatus status) {
        this.status = status;
    }
} 