package com.ecommerce.domain.merchant;

import com.ecommerce.domain.Money;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MerchantAccountTest {

    @Test
    void shouldCreateAccountWithBalance() {
        Money balance = Money.of("100.00", "USD");
        MerchantAccount account = new MerchantAccount(balance);
        
        assertEquals(balance, account.getBalance());
        assertEquals(balance, account.getTotalIncome()); // Total income equals initial balance
    }

    @Test
    void shouldCreateAccountWithBalanceAndTotalIncome() {
        Money balance = Money.of("50.00", "USD");
        Money totalIncome = Money.of("100.00", "USD");
        MerchantAccount account = new MerchantAccount(balance, totalIncome);
        
        assertEquals(balance, account.getBalance());
        assertEquals(totalIncome, account.getTotalIncome());
    }

    @Test
    void shouldThrowExceptionForNullBalance() {
        assertThrows(IllegalArgumentException.class, 
            () -> new MerchantAccount(null));
        assertThrows(IllegalArgumentException.class, 
            () -> new MerchantAccount(null, Money.of("100.00", "USD")));
    }

    @Test
    void shouldThrowExceptionForNullTotalIncome() {
        Money balance = Money.of("50.00", "USD");
        assertThrows(IllegalArgumentException.class, 
            () -> new MerchantAccount(balance, null));
    }

    @Test
    void shouldAddIncome() {
        Money initialBalance = Money.of("100.00", "USD");
        MerchantAccount account = new MerchantAccount(initialBalance);
        
        Money incomeAmount = Money.of("50.00", "USD");
        MerchantAccount newAccount = account.addIncome(incomeAmount);
        
        // Original account unchanged
        assertEquals(Money.of("100.00", "USD"), account.getBalance());
        assertEquals(Money.of("100.00", "USD"), account.getTotalIncome());
        
        // New account has updated values
        assertEquals(Money.of("150.00", "USD"), newAccount.getBalance());
        assertEquals(Money.of("150.00", "USD"), newAccount.getTotalIncome());
    }

    @Test
    void shouldAddIncomeToAccountWithDifferentBalanceAndTotalIncome() {
        Money balance = Money.of("30.00", "USD");
        Money totalIncome = Money.of("100.00", "USD");
        MerchantAccount account = new MerchantAccount(balance, totalIncome);
        
        Money incomeAmount = Money.of("20.00", "USD");
        MerchantAccount newAccount = account.addIncome(incomeAmount);
        
        assertEquals(Money.of("50.00", "USD"), newAccount.getBalance()); // 30 + 20
        assertEquals(Money.of("120.00", "USD"), newAccount.getTotalIncome()); // 100 + 20
    }

    @Test
    void shouldThrowExceptionWhenAddingNullOrZeroIncome() {
        MerchantAccount account = new MerchantAccount(Money.of("100.00", "USD"));
        
        assertThrows(IllegalArgumentException.class, () -> account.addIncome(null));
        assertThrows(IllegalArgumentException.class, () -> account.addIncome(Money.zero("USD")));
    }

    @Test
    void shouldWithdrawAmount() {
        Money balance = Money.of("100.00", "USD");
        Money totalIncome = Money.of("200.00", "USD");
        MerchantAccount account = new MerchantAccount(balance, totalIncome);
        
        Money withdrawAmount = Money.of("30.00", "USD");
        MerchantAccount newAccount = account.withdraw(withdrawAmount);
        
        // Original account unchanged
        assertEquals(Money.of("100.00", "USD"), account.getBalance());
        assertEquals(Money.of("200.00", "USD"), account.getTotalIncome());
        
        // New account has updated balance but same total income
        assertEquals(Money.of("70.00", "USD"), newAccount.getBalance()); // 100 - 30
        assertEquals(Money.of("200.00", "USD"), newAccount.getTotalIncome()); // Unchanged
    }

    @Test
    void shouldThrowExceptionWhenWithdrawingNullOrZeroAmount() {
        MerchantAccount account = new MerchantAccount(Money.of("100.00", "USD"));
        
        assertThrows(IllegalArgumentException.class, () -> account.withdraw(null));
        assertThrows(IllegalArgumentException.class, () -> account.withdraw(Money.zero("USD")));
    }

    @Test
    void shouldCheckIfHasEnoughBalance() {
        MerchantAccount account = new MerchantAccount(Money.of("100.00", "USD"));
        
        assertTrue(account.hasEnoughBalance(Money.of("50.00", "USD")));
        assertTrue(account.hasEnoughBalance(Money.of("100.00", "USD")));
        assertFalse(account.hasEnoughBalance(Money.of("150.00", "USD")));
    }

    @Test
    void shouldTestEquality() {
        Money balance = Money.of("100.00", "USD");
        Money totalIncome = Money.of("200.00", "USD");
        
        MerchantAccount account1 = new MerchantAccount(balance, totalIncome);
        MerchantAccount account2 = new MerchantAccount(balance, totalIncome);
        MerchantAccount account3 = new MerchantAccount(Money.of("50.00", "USD"), totalIncome);
        MerchantAccount account4 = new MerchantAccount(balance, Money.of("300.00", "USD"));
        
        assertEquals(account1, account2);
        assertNotEquals(account1, account3); // Different balance
        assertNotEquals(account1, account4); // Different total income
        assertNotEquals(account1, null);
        assertNotEquals(account1, "not an account");
    }

    @Test
    void shouldTestHashCode() {
        Money balance = Money.of("100.00", "USD");
        Money totalIncome = Money.of("200.00", "USD");
        
        MerchantAccount account1 = new MerchantAccount(balance, totalIncome);
        MerchantAccount account2 = new MerchantAccount(balance, totalIncome);
        
        assertEquals(account1.hashCode(), account2.hashCode());
    }

    @Test
    void shouldTestToString() {
        Money balance = Money.of("100.00", "USD");
        Money totalIncome = Money.of("250.00", "USD");
        MerchantAccount account = new MerchantAccount(balance, totalIncome);
        
        String result = account.toString();
        assertTrue(result.contains("MerchantAccount"));
        assertTrue(result.contains("100.00"));
        assertTrue(result.contains("250.00"));
    }
}