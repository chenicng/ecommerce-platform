package com.ecommerce.domain.user;

import com.ecommerce.domain.Money;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UserAccountTest {

    @Test
    void shouldCreateUserAccountWithInitialBalance() {
        Money initialBalance = Money.of("100.00", "USD");
        UserAccount account = new UserAccount(initialBalance);
        assertEquals(initialBalance, account.getBalance());
    }

    @Test
    void shouldAddBalanceToAccount() {
        Money initialBalance = Money.of("100.00", "USD");
        UserAccount account = new UserAccount(initialBalance);
        Money toAdd = Money.of("50.00", "USD");
        
        UserAccount newAccount = account.addBalance(toAdd);
        assertEquals(Money.of("150.00", "USD"), newAccount.getBalance());
        // Check immutability
        assertEquals(Money.of("100.00", "USD"), account.getBalance());
    }

    @Test
    void shouldDeductFromAccount() {
        Money initialBalance = Money.of("100.00", "USD");
        UserAccount account = new UserAccount(initialBalance);
        Money toDeduct = Money.of("30.00", "USD");
        
        UserAccount newAccount = account.deduct(toDeduct);
        assertEquals(Money.of("70.00", "USD"), newAccount.getBalance());
    }

    @Test
    void shouldThrowExceptionWhenDeductingMoreThanBalance() {
        Money initialBalance = Money.of("50.00", "USD");
        UserAccount account = new UserAccount(initialBalance);
        Money toDeduct = Money.of("100.00", "USD");
        
        assertThrows(IllegalArgumentException.class, () -> account.deduct(toDeduct));
    }

    @Test
    void shouldCheckIfBalanceIsSufficient() {
        Money balance = Money.of("100.00", "USD");
        UserAccount account = new UserAccount(balance);
        
        assertTrue(account.hasEnoughBalance(Money.of("50.00", "USD")));
        assertTrue(account.hasEnoughBalance(Money.of("100.00", "USD")));
        assertFalse(account.hasEnoughBalance(Money.of("150.00", "USD")));
    }

    @Test
    void shouldCheckEquality() {
        Money balance = Money.of("100.00", "USD");
        UserAccount account1 = new UserAccount(balance);
        UserAccount account2 = new UserAccount(balance);
        UserAccount account3 = new UserAccount(Money.of("200.00", "USD"));
        
        assertEquals(account1, account2);
        assertNotEquals(account1, account3);
    }

    @Test
    void shouldHandleAddBalanceWithNullAmount() {
        // Given
        UserAccount account = new UserAccount(Money.of("100.00", "CNY"));
        
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> account.addBalance(null));
        assertEquals("Amount must be positive", exception.getMessage());
    }

    @Test
    void shouldHandleAddBalanceWithZeroAmount() {
        // Given
        UserAccount account = new UserAccount(Money.of("100.00", "CNY"));
        
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> account.addBalance(Money.zero("CNY")));
        assertEquals("Amount must be positive", exception.getMessage());
    }

    @Test
    void shouldHandleDeductWithNullAmount() {
        // Given
        UserAccount account = new UserAccount(Money.of("100.00", "CNY"));
        
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> account.deduct(null));
        assertEquals("Amount must be positive", exception.getMessage());
    }

    @Test
    void shouldHandleDeductWithZeroAmount() {
        // Given
        UserAccount account = new UserAccount(Money.of("100.00", "CNY"));
        
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> account.deduct(Money.zero("CNY")));
        assertEquals("Amount must be positive", exception.getMessage());
    }

    @Test
    void shouldHandleMultipleAddBalanceOperations() {
        // Given
        UserAccount account = new UserAccount(Money.of("100.00", "CNY"));
        
        // When
        UserAccount result1 = account.addBalance(Money.of("50.00", "CNY"));
        UserAccount result2 = result1.addBalance(Money.of("25.00", "CNY"));
        UserAccount result3 = result2.addBalance(Money.of("75.00", "CNY"));
        
        // Then
        assertEquals(Money.of("250.00", "CNY"), result3.getBalance());
    }

    @Test
    void shouldHandleMultipleDeductOperations() {
        // Given
        UserAccount account = new UserAccount(Money.of("500.00", "CNY"));
        
        // When
        UserAccount result1 = account.deduct(Money.of("100.00", "CNY"));
        UserAccount result2 = result1.deduct(Money.of("200.00", "CNY"));
        UserAccount result3 = result2.deduct(Money.of("50.00", "CNY"));
        
        // Then
        assertEquals(Money.of("150.00", "CNY"), result3.getBalance());
    }

    @Test
    void shouldHandleExactBalanceDeduction() {
        // Given
        UserAccount account = new UserAccount(Money.of("100.00", "CNY"));
        
        // When
        UserAccount result = account.deduct(Money.of("100.00", "CNY"));
        
        // Then
        assertEquals(Money.of("0.00", "CNY"), result.getBalance());
    }

    @Test
    void shouldThrowExceptionWhenConstructorReceivesNullBalance() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> new UserAccount(null));
        assertEquals("Balance cannot be null", exception.getMessage());
    }

    @Test
    void shouldGenerateCorrectHashCode() {
        // Given
        Money balance1 = Money.of("100.00", "USD");
        Money balance2 = Money.of("100.00", "USD");
        Money balance3 = Money.of("200.00", "USD");
        
        UserAccount account1 = new UserAccount(balance1);
        UserAccount account2 = new UserAccount(balance2);
        UserAccount account3 = new UserAccount(balance3);
        
        // Then
        assertEquals(account1.hashCode(), account2.hashCode());
        assertNotEquals(account1.hashCode(), account3.hashCode());
    }

    @Test
    void shouldGenerateCorrectToString() {
        // Given
        Money balance = Money.of("150.75", "EUR");
        UserAccount account = new UserAccount(balance);
        
        // When
        String result = account.toString();
        
        // Then
        assertNotNull(result);
        assertTrue(result.contains("UserAccount"));
        assertTrue(result.contains("balance"));
        assertTrue(result.contains("150.75"));
        assertTrue(result.contains("EUR"));
    }

    @Test
    void shouldHandleEqualsWithSameObject() {
        // Given
        UserAccount account = new UserAccount(Money.of("100.00", "USD"));
        
        // When & Then
        assertEquals(account, account);
    }

    @Test
    void shouldHandleEqualsWithNull() {
        // Given
        UserAccount account = new UserAccount(Money.of("100.00", "USD"));
        
        // When & Then
        assertNotEquals(account, null);
    }

    @Test
    void shouldHandleEqualsWithDifferentClass() {
        // Given
        UserAccount account = new UserAccount(Money.of("100.00", "USD"));
        String differentObject = "not a UserAccount";
        
        // When & Then
        assertNotEquals(account, differentObject);
    }

    @Test
    void shouldHandleEqualsWithDifferentBalance() {
        // Given
        UserAccount account1 = new UserAccount(Money.of("100.00", "USD"));
        UserAccount account2 = new UserAccount(Money.of("200.00", "USD"));
        
        // When & Then
        assertNotEquals(account1, account2);
    }

    @Test
    void shouldHandleEqualsWithSameBalance() {
        // Given
        UserAccount account1 = new UserAccount(Money.of("100.00", "USD"));
        UserAccount account2 = new UserAccount(Money.of("100.00", "USD"));
        
        // When & Then
        assertEquals(account1, account2);
    }

    @Test
    void shouldMaintainImmutabilityInAllOperations() {
        // Given
        Money initialBalance = Money.of("100.00", "USD");
        UserAccount originalAccount = new UserAccount(initialBalance);
        
        // When
        UserAccount afterAdd = originalAccount.addBalance(Money.of("50.00", "USD"));
        UserAccount afterDeduct = originalAccount.deduct(Money.of("25.00", "USD"));
        
        // Then
        assertEquals(Money.of("100.00", "USD"), originalAccount.getBalance());
        assertEquals(Money.of("150.00", "USD"), afterAdd.getBalance());
        assertEquals(Money.of("75.00", "USD"), afterDeduct.getBalance());
    }

    @Test
    void shouldHandleZeroBalance() {
        // Given
        UserAccount account = new UserAccount(Money.zero("USD"));
        
        // When & Then
        assertEquals(Money.zero("USD"), account.getBalance());
        assertFalse(account.hasEnoughBalance(Money.of("0.01", "USD")));
        assertTrue(account.hasEnoughBalance(Money.zero("USD")));
    }

    @Test
    void shouldHandleLargeAmounts() {
        // Given
        Money largeAmount = Money.of("999999.99", "USD");
        UserAccount account = new UserAccount(largeAmount);
        
        // When
        UserAccount afterAdd = account.addBalance(Money.of("0.01", "USD"));
        
        // Then
        assertEquals(Money.of("1000000.00", "USD"), afterAdd.getBalance());
    }
}