package com.trading.domain.user;

import com.trading.domain.BaseEntity;
import com.trading.domain.Money;

/**
 * 用户聚合根
 * 包含用户基本信息和预存账户
 */
public class User extends BaseEntity {
    
    private String username;
    private String email;
    private String phone;
    private UserAccount account;
    private UserStatus status;
    
    // 构造函数
    protected User() {
        super();
    }
    
    public User(String username, String email, String phone, String currency) {
        super();
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.account = new UserAccount(Money.zero(currency));
        this.status = UserStatus.ACTIVE;
    }
    
    /**
     * 向账户充值
     */
    public void recharge(Money amount) {
        validateActiveStatus();
        if (amount == null || amount.isZero()) {
            throw new IllegalArgumentException("Recharge amount must be positive");
        }
        this.account = this.account.add(amount);
        this.markAsUpdated();
    }
    
    /**
     * 从账户扣款
     */
    public void deduct(Money amount) {
        validateActiveStatus();
        if (amount == null || amount.isZero()) {
            throw new IllegalArgumentException("Deduct amount must be positive");
        }
        if (!canAfford(amount)) {
            throw new InsufficientBalanceException("Insufficient balance for deduction");
        }
        this.account = this.account.subtract(amount);
        this.markAsUpdated();
    }
    
    /**
     * 检查是否有足够余额
     */
    public boolean canAfford(Money amount) {
        return this.account.getBalance().isGreaterThanOrEqual(amount);
    }
    
    /**
     * 获取账户余额
     */
    public Money getBalance() {
        return this.account.getBalance();
    }
    
    /**
     * 激活用户
     */
    public void activate() {
        this.status = UserStatus.ACTIVE;
        this.markAsUpdated();
    }
    
    /**
     * 停用用户
     */
    public void deactivate() {
        this.status = UserStatus.INACTIVE;
        this.markAsUpdated();
    }
    
    private void validateActiveStatus() {
        if (!isActive()) {
            throw new IllegalStateException("User is not active");
        }
    }
    
    public boolean isActive() {
        return UserStatus.ACTIVE.equals(this.status);
    }
    
    // Getters
    public String getUsername() {
        return username;
    }
    
    public String getEmail() {
        return email;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public UserStatus getStatus() {
        return status;
    }
    
    public UserAccount getAccount() {
        return account;
    }
    
    // Package private setters for JPA
    void setUsername(String username) {
        this.username = username;
    }
    
    void setEmail(String email) {
        this.email = email;
    }
    
    void setPhone(String phone) {
        this.phone = phone;
    }
    
    void setAccount(UserAccount account) {
        this.account = account;
    }
    
    void setStatus(UserStatus status) {
        this.status = status;
    }
} 