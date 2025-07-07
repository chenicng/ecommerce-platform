package com.ecommerce.domain.merchant;

import com.ecommerce.domain.Money;
import java.util.Objects;

/**
 * 商家账户值对象
 * 包含商家的收入和余额信息
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
     * 增加收入
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
     * 提取金额
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
     * 检查是否有足够余额
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