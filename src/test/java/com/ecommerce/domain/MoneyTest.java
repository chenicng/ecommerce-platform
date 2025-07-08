package com.ecommerce.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;

class MoneyTest {

    @Test
    void shouldCreateMoney() {
        Money money = Money.of("100.50", "USD");
        assertEquals(new BigDecimal("100.50"), money.getAmount());
        assertEquals("USD", money.getCurrency());
    }

    @Test
    void shouldCreateZeroMoney() {
        Money money = Money.zero("USD");
        assertEquals(new BigDecimal("0.00"), money.getAmount());
    }

    @Test
    void shouldAddMoney() {
        Money money1 = Money.of("100.50", "USD");
        Money money2 = Money.of("50.25", "USD");
        Money result = money1.add(money2);
        assertEquals(new BigDecimal("150.75"), result.getAmount());
    }

    @Test
    void shouldSubtractMoney() {
        Money money1 = Money.of("100.50", "USD");
        Money money2 = Money.of("50.25", "USD");
        Money result = money1.subtract(money2);
        assertEquals(new BigDecimal("50.25"), result.getAmount());
    }

    @Test
    void shouldMultiplyMoney() {
        Money money = Money.of("10.50", "USD");
        Money result = money.multiply(3);
        assertEquals(new BigDecimal("31.50"), result.getAmount());
    }

    @Test
    void shouldThrowExceptionForNegativeAmount() {
        assertThrows(IllegalArgumentException.class, () -> Money.of("-10", "USD"));
    }

    @Test
    void shouldThrowExceptionForNullCurrency() {
        assertThrows(IllegalArgumentException.class, () -> Money.of("100", null));
    }

    @Test
    void shouldCompareMoney() {
        Money money1 = Money.of("100.00", "USD");
        Money money2 = Money.of("50.00", "USD");
        
        assertTrue(money1.isGreaterThan(money2));
        assertTrue(money2.isLessThan(money1));
        assertTrue(money1.isGreaterThanOrEqual(money2));
    }

    @Test
    void shouldCheckEquality() {
        Money money1 = Money.of("100.00", "USD");
        Money money2 = Money.of("100.00", "USD");
        Money money3 = Money.of("100.00", "EUR");
        
        assertEquals(money1, money2);
        assertNotEquals(money1, money3);
    }
}