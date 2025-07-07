package com.ecommerce.domain.user;

import com.ecommerce.domain.Money;
import java.util.Objects;

/**
 * User Account Value Object
 * Contains user prepaid balance information
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
     * Add balance
     */
    public UserAccount addBalance(Money amount) {
        if (amount == null || amount.isZero()) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        return new UserAccount(this.balance.add(amount));
    }
    
    /**
     * Deduct balance
     */
    public UserAccount deduct(Money amount) {
        if (amount == null || amount.isZero()) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        return new UserAccount(this.balance.subtract(amount));
    }
    
    /**
     * Check if has enough balance
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