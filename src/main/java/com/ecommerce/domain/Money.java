package com.ecommerce.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.Set;

/**
 * Money Value Object
 * Uses BigDecimal to ensure precision, follows immutability principle
 * Includes comprehensive validation for currency and amount operations
 */
@Embeddable
@JsonIgnoreProperties({"zero", "positive", "negative"})
public final class Money {
    
    private static final int DEFAULT_SCALE = 2;
    private static final RoundingMode DEFAULT_ROUNDING_MODE = RoundingMode.HALF_UP;
    
    // Supported currencies (ISO 4217 compliant)
    private static final Set<String> SUPPORTED_CURRENCIES = Set.of(
        "CNY", "USD", "EUR", "GBP", "JPY", "HKD", "SGD", "AUD", "CAD"
    );
    
    @Column(name = "amount", precision = 19, scale = 2, nullable = false)
    private final BigDecimal amount;
    
    @Column(name = "currency", length = 3, nullable = false)
    private final String currency;
    
    // Default constructor for JPA
    protected Money() {
        this.amount = BigDecimal.ZERO;
        this.currency = "CNY";
    }
    
    private Money(BigDecimal amount, String currency) {
        this.amount = amount.setScale(DEFAULT_SCALE, DEFAULT_ROUNDING_MODE);
        this.currency = currency;
    }
    
    // Unified validation method
    private static void validateInput(BigDecimal amount, String currency) {
        if (amount == null) {
            throw new IllegalArgumentException("Amount cannot be null");
        }
        if (currency == null || currency.trim().isEmpty()) {
            throw new IllegalArgumentException("Currency cannot be null or empty");
        }
        validateCurrency(currency);
    }
    
    private static void validateCurrency(String currency) {
        String normalizedCurrency = currency.toUpperCase().trim();
        if (!SUPPORTED_CURRENCIES.contains(normalizedCurrency)) {
            throw new IllegalArgumentException("Unsupported currency: " + currency + 
                ". Supported currencies: " + SUPPORTED_CURRENCIES);
        }
    }
    
    public static Money of(BigDecimal amount, String currency) {
        validateInput(amount, currency);
        return new Money(amount, currency.toUpperCase());
    }
    
    public static Money of(double amount, String currency) {
        return of(BigDecimal.valueOf(amount), currency);
    }
    
    public static Money of(String amount, String currency) {
        if (amount == null) {
            throw new IllegalArgumentException("Amount cannot be null");
        }
        return of(new BigDecimal(amount), currency);
    }
    
    public static Money zero(String currency) {
        if (currency == null || currency.trim().isEmpty()) {
            throw new IllegalArgumentException("Currency cannot be null or empty");
        }
        validateCurrency(currency);
        return new Money(BigDecimal.ZERO, currency.toUpperCase());
    }
    
    /**
     * Add another money amount
     * Must be same currency
     */
    public Money add(Money other) {
        validateSameCurrency(other);
        return new Money(this.amount.add(other.amount), this.currency);
    }
    
    /**
     * Subtract another money amount
     * Must be same currency
     */
    public Money subtract(Money other) {
        validateSameCurrency(other);
        return new Money(this.amount.subtract(other.amount), this.currency);
    }
    
    /**
     * Multiply by a factor
     * Factor must be non-negative for business operations
     */
    public Money multiply(int factor) {
        if (factor < 0) {
            throw new IllegalArgumentException("Factor cannot be negative in money operations. Factor: " + factor);
        }
        return new Money(this.amount.multiply(BigDecimal.valueOf(factor)), this.currency);
    }
    
    /**
     * Multiply by a decimal factor
     * Factor must be non-negative for business operations
     */
    public Money multiply(BigDecimal factor) {
        if (factor == null) {
            throw new IllegalArgumentException("Factor cannot be null");
        }
        if (factor.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Factor cannot be negative in money operations. Factor: " + factor);
        }
        return new Money(this.amount.multiply(factor), this.currency);
    }
    
    // Comparison methods
    @com.fasterxml.jackson.annotation.JsonIgnore
    public boolean isZero() {
        return this.amount.compareTo(BigDecimal.ZERO) == 0;
    }
    
    public boolean isGreaterThan(Money other) {
        validateSameCurrency(other);
        return this.amount.compareTo(other.amount) > 0;
    }
    
    public boolean isGreaterThanOrEqual(Money other) {
        validateSameCurrency(other);
        return this.amount.compareTo(other.amount) >= 0;
    }
    
    public boolean isLessThan(Money other) {
        validateSameCurrency(other);
        return this.amount.compareTo(other.amount) < 0;
    }
    
    public boolean isLessThanOrEqual(Money other) {
        validateSameCurrency(other);
        return this.amount.compareTo(other.amount) <= 0;
    }
    
    private void validateSameCurrency(Money other) {
        if (other == null) {
            throw new IllegalArgumentException("Other money cannot be null");
        }
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException(
                String.format("Currency mismatch: %s vs %s", this.currency, other.currency));
        }
    }
    
    // Getters
    public BigDecimal getAmount() {
        return amount;
    }
    
    public String getCurrency() {
        return currency;
    }
    
    @JsonIgnore
    public boolean isPositive() {
        return amount.compareTo(BigDecimal.ZERO) > 0;
    }
    
    @JsonIgnore
    public boolean isNegative() {
        return amount.compareTo(BigDecimal.ZERO) < 0;
    }
    
    /**
     * Get list of supported currencies
     */
    public static Set<String> getSupportedCurrencies() {
        return Set.copyOf(SUPPORTED_CURRENCIES);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Money money = (Money) o;
        return Objects.equals(amount, money.amount) && Objects.equals(currency, money.currency);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(amount, currency);
    }
    
    @Override
    public String toString() {
        return String.format("%s %s", amount.toPlainString(), currency);
    }
} 