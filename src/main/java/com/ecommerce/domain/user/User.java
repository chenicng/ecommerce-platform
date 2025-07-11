package com.ecommerce.domain.user;

import com.ecommerce.domain.BaseEntity;
import com.ecommerce.domain.Money;
import com.ecommerce.domain.ResourceInactiveException;
import jakarta.persistence.*;

/**
 * User Aggregate Root
 * Contains user basic information and prepaid account
 */
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_user_email", columnList = "email", unique = true)
})
public class User extends BaseEntity {
    
    @Column(name = "username", nullable = false, length = 50)
    private String username;
    
    @Column(name = "email", nullable = false, length = 100, unique = true)
    private String email;
    
    @Column(name = "phone", nullable = false, length = 20)
    private String phone;
    
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "balance_amount", precision = 19, scale = 2)),
        @AttributeOverride(name = "currency", column = @Column(name = "balance_currency", length = 3))
    })
    private UserAccount account;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private UserStatus status;
    
    // Constructor
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
     * Recharge account
     */
    public void recharge(Money amount) {
        validateActiveStatus();
        if (amount == null || amount.isZero()) {
            throw new IllegalArgumentException("Recharge amount must be positive");
        }
        this.account = this.account.addBalance(amount);
        this.markAsUpdated();
    }
    
    /**
     * Deduct from account
     */
    public void deduct(Money amount) {
        validateActiveStatus();
        if (amount == null || amount.isZero()) {
            throw new IllegalArgumentException("Deduct amount must be positive");
        }
        if (!canAfford(amount)) {
            throw new InsufficientBalanceException("Insufficient balance for deduction. Required: " + amount + ", Available: " + getBalance());
        }
        this.account = this.account.deduct(amount);
        this.markAsUpdated();
    }
    
    /**
     * Check if has enough balance
     */
    public boolean canAfford(Money amount) {
        return this.account.getBalance().isGreaterThanOrEqual(amount);
    }
    
    /**
     * Get account balance
     */
    public Money getBalance() {
        return this.account.getBalance();
    }
    
    /**
     * Activate user
     */
    public void activate() {
        this.status = UserStatus.ACTIVE;
        this.markAsUpdated();
    }
    
    /**
     * Deactivate user
     */
    public void deactivate() {
        this.status = UserStatus.INACTIVE;
        this.markAsUpdated();
    }
    
    private void validateActiveStatus() {
        if (!isActive()) {
            throw new ResourceInactiveException("User is not active. User ID: " + getId());
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