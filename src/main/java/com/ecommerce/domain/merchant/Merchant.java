package com.ecommerce.domain.merchant;

import com.ecommerce.domain.BaseEntity;
import com.ecommerce.domain.Money;
import com.ecommerce.domain.ResourceInactiveException;
import jakarta.persistence.*;

/**
 * Merchant Aggregate Root
 * Contains merchant basic information and income account
 */
@Entity
@Table(name = "merchants", indexes = {
    @Index(name = "idx_merchant_name", columnList = "merchant_name"),
    @Index(name = "idx_merchant_license", columnList = "business_license", unique = true),
    @Index(name = "idx_merchant_email", columnList = "contact_email", unique = true)
})
public class Merchant extends BaseEntity {
    
    @Column(name = "merchant_name", nullable = false, length = 100)
    private String merchantName;
    
    @Column(name = "business_license", nullable = false, length = 50, unique = true)
    private String businessLicense;
    
    @Column(name = "contact_email", nullable = false, length = 100, unique = true)
    private String contactEmail;
    
    @Column(name = "contact_phone", nullable = false, length = 20)
    private String contactPhone;
    
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "balance.amount", column = @Column(name = "balance_amount", precision = 19, scale = 2)),
        @AttributeOverride(name = "balance.currency", column = @Column(name = "balance_currency", length = 3)),
        @AttributeOverride(name = "totalIncome.amount", column = @Column(name = "total_income_amount", precision = 19, scale = 2)),
        @AttributeOverride(name = "totalIncome.currency", column = @Column(name = "total_income_currency", length = 3))
    })
    private MerchantAccount account;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private MerchantStatus status;
    
    // Constructor
    protected Merchant() {
        super();
    }
    
    public Merchant(String merchantName, String businessLicense, 
                   String contactEmail, String contactPhone) {
        this(merchantName, businessLicense, contactEmail, contactPhone, "CNY");
    }
    
    public Merchant(String merchantName, String businessLicense, 
                   String contactEmail, String contactPhone, String currency) {
        super();
        this.merchantName = merchantName;
        this.businessLicense = businessLicense;
        this.contactEmail = contactEmail;
        this.contactPhone = contactPhone;
        this.account = new MerchantAccount(Money.zero(currency));
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
            throw new InsufficientFundsException("Insufficient funds for withdrawal. Required: " + amount + ", Available: " + getBalance());
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
            throw new ResourceInactiveException("Merchant is not active. Merchant ID: " + getId());
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