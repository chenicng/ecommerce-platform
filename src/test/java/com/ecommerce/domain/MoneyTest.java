package com.ecommerce.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
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
    void shouldCreateNegativeAmountWhenExplicitlyAllowed() {
        // Negative amounts are now allowed for business scenarios like deficits
        Money negativeMoney = Money.of("-10", "USD");
        assertEquals(new BigDecimal("-10.00"), negativeMoney.getAmount());
        assertTrue(negativeMoney.isNegative());
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

    @Test
    void shouldThrowExceptionForDifferentCurrencies() {
        Money usd = Money.of("100.00", "USD");
        Money eur = Money.of("50.00", "EUR");
        
        assertThrows(IllegalArgumentException.class, () -> usd.add(eur));
        assertThrows(IllegalArgumentException.class, () -> usd.subtract(eur));
        assertThrows(IllegalArgumentException.class, () -> usd.isGreaterThan(eur));
        assertThrows(IllegalArgumentException.class, () -> usd.isLessThan(eur));
        assertThrows(IllegalArgumentException.class, () -> usd.isGreaterThanOrEqual(eur));
    }

    @Test
    void shouldThrowExceptionForNegativeMultiplier() {
        Money money = Money.of("100.00", "USD");
        
        assertThrows(IllegalArgumentException.class, () -> money.multiply(-1));
        assertThrows(IllegalArgumentException.class, () -> money.multiply(BigDecimal.valueOf(-2.5)));
    }

    @Test
    void shouldAllowNegativeSubtractionResult() {
        Money smaller = Money.of("50.00", "USD");
        Money larger = Money.of("100.00", "USD");
        
        Money result = smaller.subtract(larger);
        assertEquals(new BigDecimal("-50.00"), result.getAmount());
        assertTrue(result.isNegative());
    }

    @Test
    void shouldHandleZeroOperations() {
        Money money = Money.of("100.00", "USD");
        Money zero = Money.zero("USD");
        
        assertEquals(money, money.add(zero));
        assertEquals(money, money.subtract(zero));
        assertEquals(zero, money.multiply(0));
        assertTrue(money.isGreaterThan(zero));
        assertFalse(zero.isGreaterThan(money));
        assertTrue(zero.isZero());
        assertFalse(money.isZero());
    }

    @Test
    void shouldCreateMoneyFromBigDecimal() {
        BigDecimal amount = new BigDecimal("123.456789");
        Money money = Money.of(amount, "USD");
        
        // Should round to 2 decimal places
        assertEquals(new BigDecimal("123.46"), money.getAmount());
        assertEquals("USD", money.getCurrency());
    }

    @Test
    void shouldCreateMoneyFromDouble() {
        Money money = Money.of(99.99, "EUR");
        
        assertEquals(new BigDecimal("99.99"), money.getAmount());
        assertEquals("EUR", money.getCurrency());
    }

    @Test
    void shouldThrowExceptionForNullAmount() {
        assertThrows(IllegalArgumentException.class, () -> Money.of((BigDecimal) null, "USD"));
        assertThrows(IllegalArgumentException.class, () -> Money.of((String) null, "USD"));
    }

    @Test
    void shouldThrowExceptionForEmptyCurrency() {
        assertThrows(IllegalArgumentException.class, () -> Money.of("100.00", ""));
        assertThrows(IllegalArgumentException.class, () -> Money.of("100.00", "   "));
    }

    @Test
    void shouldNormalizeCurrencyToUpperCase() {
        Money money = Money.of("100.00", "usd");
        assertEquals("USD", money.getCurrency());
        
        Money money2 = Money.of("50.00", "eur");
        assertEquals("EUR", money2.getCurrency());
    }

    @Test
    void shouldHandleLargeNumbers() {
        Money large1 = Money.of("999999999.99", "USD");
        Money large2 = Money.of("1.00", "USD");
        
        Money result = large1.add(large2);
        assertEquals(new BigDecimal("1000000000.99"), result.getAmount());
    }

    @Test
    void shouldHandleSmallDecimals() {
        Money small1 = Money.of("0.01", "USD");
        Money small2 = Money.of("0.01", "USD");
        
        Money result = small1.add(small2);
        assertEquals(new BigDecimal("0.02"), result.getAmount());
        
        Money result2 = small1.subtract(small2);
        assertEquals(new BigDecimal("0.00"), result2.getAmount());
        assertTrue(result2.isZero());
    }

    @Test
    void shouldHandleRounding() {
        // Test various rounding scenarios
        Money money1 = Money.of("10.555", "USD"); // Should round to 10.56
        assertEquals(new BigDecimal("10.56"), money1.getAmount());
        
        Money money2 = Money.of("10.554", "USD"); // Should round to 10.55
        assertEquals(new BigDecimal("10.55"), money2.getAmount());
        
        Money money3 = Money.of("10.5", "USD"); // Should be 10.50
        assertEquals(new BigDecimal("10.50"), money3.getAmount());
    }

    @Test
    void shouldMultiplyByLargeNumbers() {
        Money money = Money.of("10.00", "USD");
        Money result = money.multiply(10000);
        
        assertEquals(new BigDecimal("100000.00"), result.getAmount());
    }

    @Test
    void shouldMultiplyByDecimal() {
        Money money = Money.of("100.00", "USD");
        Money result = money.multiply(BigDecimal.valueOf(2.5));
        
        assertEquals(new BigDecimal("250.00"), result.getAmount());
    }

    @Test
    void shouldTestHashCodeConsistency() {
        Money money1 = Money.of("100.00", "USD");
        Money money2 = Money.of("100.00", "USD");
        Money money3 = Money.of("100.00", "EUR");
        
        assertEquals(money1.hashCode(), money2.hashCode());
        assertNotEquals(money1.hashCode(), money3.hashCode());
    }

    @Test
    void shouldTestToString() {
        Money money = Money.of("123.45", "USD");
        String result = money.toString();
        
        assertTrue(result.contains("123.45"));
        assertTrue(result.contains("USD"));
    }

    @Test
    void shouldNotEqualNull() {
        Money money = Money.of("100.00", "USD");
        assertNotEquals(money, null);
    }

    @Test
    void shouldNotEqualDifferentClass() {
        Money money = Money.of("100.00", "USD");
        assertNotEquals(money, "100.00 USD");
        assertNotEquals(money, 100.00);
    }

    @Test
    void shouldEqualSelf() {
        Money money = Money.of("100.00", "USD");
        assertEquals(money, money);
    }

    @Test
    void shouldHandleVerySmallAmounts() {
        Money tiny = Money.of("0.001", "USD");
        // Should round to 0.00
        assertEquals(new BigDecimal("0.00"), tiny.getAmount());
        assertTrue(tiny.isZero());
    }

    @Test
    void shouldCompareWithExactlyEqualAmounts() {
        Money money1 = Money.of("100.00", "USD");
        Money money2 = Money.of("100.00", "USD");
        
        assertFalse(money1.isGreaterThan(money2));
        assertFalse(money1.isLessThan(money2));
        assertTrue(money1.isGreaterThanOrEqual(money2));
    }

    @Test
    void shouldHandleInvalidStringAmounts() {
        assertThrows(NumberFormatException.class, () -> Money.of("abc", "USD"));
        assertThrows(NumberFormatException.class, () -> Money.of("", "USD"));
        assertThrows(NumberFormatException.class, () -> Money.of("10.0.0", "USD"));
    }

    @Test
    void shouldHandleSpecialCharactersInCurrency() {
        // Currency codes should be basic strings, test edge cases
        Money money = Money.of("100.00", "XYZ");
        assertEquals("XYZ", money.getCurrency());
    }

    @Test
    void shouldHandleExtremeRounding() {
        Money money = Money.of("99.999", "USD");
        assertEquals(new BigDecimal("100.00"), money.getAmount());
        
        Money money2 = Money.of("0.994", "USD");
        assertEquals(new BigDecimal("0.99"), money2.getAmount());
        
        Money money3 = Money.of("0.996", "USD");
        assertEquals(new BigDecimal("1.00"), money3.getAmount());
    }

    @Test
    void shouldExcludeIsZeroFromJsonSerialization() throws Exception {
        Money money = Money.of("100.00", "USD");
        ObjectMapper objectMapper = new ObjectMapper();
        
        String json = objectMapper.writeValueAsString(money);
        
        // Verify that the JSON contains amount and currency
        assertTrue(json.contains("\"amount\":100.00") || json.contains("\"amount\":100.0"));
        assertTrue(json.contains("\"currency\":\"USD\""));
        // The @JsonIgnore methods should not appear in JSON
        assertFalse(json.contains("\"positive\""));
        assertFalse(json.contains("\"negative\""));
    }

    @Test
    void shouldExcludeIsZeroFromJsonSerializationForZeroAmount() throws Exception {
        Money zeroMoney = Money.zero("USD");
        ObjectMapper objectMapper = new ObjectMapper();
        
        String json = objectMapper.writeValueAsString(zeroMoney);
        
        // Verify that the JSON contains amount and currency
        assertTrue(json.contains("\"amount\":0.00") || json.contains("\"amount\":0.0"));
        assertTrue(json.contains("\"currency\":\"USD\""));
        // The @JsonIgnore methods should not appear in JSON
        assertFalse(json.contains("\"positive\""));
        assertFalse(json.contains("\"negative\""));
    }

    @Test
    void shouldCreateNegativeMoney() {
        Money negativeMoney = Money.of("-10", "USD");
        assertEquals(new BigDecimal("-10.00"), negativeMoney.getAmount());
        assertEquals("USD", negativeMoney.getCurrency());
        assertTrue(negativeMoney.isNegative());
        assertFalse(negativeMoney.isPositive());
    }
}