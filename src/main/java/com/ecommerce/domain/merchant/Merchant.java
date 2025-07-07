package com.ecommerce.domain.merchant;

import com.ecommerce.domain.BaseEntity;
import com.ecommerce.domain.Money;

/**
 * 商家聚合根
 * 包含商家基本信息和收入账户
 */
public class Merchant extends BaseEntity {
    
    private String merchantName;
    private String businessLicense;
    private String contactEmail;
    private String contactPhone;
    private MerchantAccount account;
    private MerchantStatus status;
    
    // 构造函数
    protected Merchant() {
        super();
    }
    
    public Merchant(String merchantName, String businessLicense, String contactEmail, 
                   String contactPhone, String currency) {
        super();
        this.merchantName = merchantName;
        this.businessLicense = businessLicense;
        this.contactEmail = contactEmail;
        this.contactPhone = contactPhone;
        this.account = new MerchantAccount(Money.zero(currency));
        this.status = MerchantStatus.ACTIVE;
    }
    
    /**
     * 接收收入
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
     * 提取收入
     */
    public void withdraw(Money amount) {
        validateActiveStatus();
        if (amount == null || amount.isZero()) {
            throw new IllegalArgumentException("Withdraw amount must be positive");
        }
        if (!canWithdraw(amount)) {
            throw new InsufficientFundsException("Insufficient funds for withdrawal");
        }
        this.account = this.account.withdraw(amount);
        this.markAsUpdated();
    }
    
    /**
     * 检查是否可以提取指定金额
     */
    public boolean canWithdraw(Money amount) {
        return this.account.getBalance().isGreaterThanOrEqual(amount);
    }
    
    /**
     * 获取账户余额
     */
    public Money getBalance() {
        return this.account.getBalance();
    }
    
    /**
     * 获取总收入
     */
    public Money getTotalIncome() {
        return this.account.getTotalIncome();
    }
    
    /**
     * 激活商家
     */
    public void activate() {
        this.status = MerchantStatus.ACTIVE;
        this.markAsUpdated();
    }
    
    /**
     * 停用商家
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