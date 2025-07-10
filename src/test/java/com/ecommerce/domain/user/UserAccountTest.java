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
}