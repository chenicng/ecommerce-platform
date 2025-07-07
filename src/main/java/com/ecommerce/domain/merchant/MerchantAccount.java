package com.ecommerce.domain.merchant;

import com.ecommerce.domain.Money;
import java.util.Objects;

/**
 * Merchant Account Value Object
 * Contains merchant income and balance information
 */
public final class MerchantAccount {
    
    private final Money balance;
    private final Money totalIncome;
    
    public MerchantAccount(Money balance) {
        this(balance, balance);
    }
    
    public MerchantAccount(Money balance, Money totalIncome) {
        if (balance == null) {
            throw new IllegalArgumentException("Balance cannot be null");
        }
        if (totalIncome == null) {
            throw new IllegalArgumentException("Total income cannot be null");
        }
        this.balance = balance;
        this.totalIncome = totalIncome;
    }
    
    /**
     * Add income
     */
    public MerchantAccount addIncome(Money amount) {
        if (amount == null || amount.isZero()) {
            throw new IllegalArgumentException("Income amount must be positive");
        }
        return new MerchantAccount(
            this.balance.add(amount),
            this.totalIncome.add(amount)
        );
    }
    
    /**
     * Withdraw amount
     */
    public MerchantAccount withdraw(Money amount) {
        if (amount == null || amount.isZero()) {
            throw new IllegalArgumentException("Withdraw amount must be positive");
        }
        return new MerchantAccount(
            this.balance.subtract(amount),
            this.totalIncome
        );
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
    
    public Money getTotalIncome() {
        return totalIncome;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MerchantAccount that = (MerchantAccount) o;
        return Objects.equals(balance, that.balance) && 
               Objects.equals(totalIncome, that.totalIncome);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(balance, totalIncome);
    }
    
    @Override
    public String toString() {
        return String.format("MerchantAccount{balance=%s, totalIncome=%s}", balance, totalIncome);
    }
} 