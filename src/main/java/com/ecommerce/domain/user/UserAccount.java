package com.ecommerce.domain.user;

import com.ecommerce.domain.Money;
import java.util.Objects;

/**
 * 用户账户值对象
 * 包含用户的预存余额信息
 */
public final class UserAccount {
    
    private final Money balance;
    
    public UserAccount(Money balance) {
        if (balance == null) {
            throw new IllegalArgumentException("Balance cannot be null");
        }
        this.balance = balance;
    }
    
    /**
     * 增加余额
     */
    public UserAccount add(Money amount) {
        if (amount == null || amount.isZero()) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        return new UserAccount(this.balance.add(amount));
    }
    
    /**
     * 减少余额
     */
    public UserAccount subtract(Money amount) {
        if (amount == null || amount.isZero()) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        return new UserAccount(this.balance.subtract(amount));
    }
    
    /**
     * 检查是否有足够余额
     */
    public boolean hasEnoughBalance(Money amount) {
        return this.balance.isGreaterThanOrEqual(amount);
    }
    
    public Money getBalance() {
        return balance;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserAccount that = (UserAccount) o;
        return Objects.equals(balance, that.balance);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(balance);
    }
    
    @Override
    public String toString() {
        return "UserAccount{balance=" + balance + '}';
    }
} 