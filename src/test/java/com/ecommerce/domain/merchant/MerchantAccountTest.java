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

    @Test
    void shouldMaintainImmutability() {
        Money balance = Money.of("100.00", "USD");
        Money totalIncome = Money.of("200.00", "USD");
        MerchantAccount account = new MerchantAccount(balance, totalIncome);
        
        // Perform operations
        MerchantAccount afterIncome = account.addIncome(Money.of("50.00", "USD"));
        MerchantAccount afterWithdrawal = account.withdraw(Money.of("30.00", "USD"));
        
        // Original account should remain unchanged
        assertEquals(Money.of("100.00", "USD"), account.getBalance());
        assertEquals(Money.of("200.00", "USD"), account.getTotalIncome());
        
        // New accounts should have correct values
        assertEquals(Money.of("150.00", "USD"), afterIncome.getBalance());
        assertEquals(Money.of("250.00", "USD"), afterIncome.getTotalIncome());
        
        assertEquals(Money.of("70.00", "USD"), afterWithdrawal.getBalance());
        assertEquals(Money.of("200.00", "USD"), afterWithdrawal.getTotalIncome());
    }

    @Test
    void shouldHandleMultipleCurrencies() {
        // Test with EUR
        Money eurBalance = Money.of("150.00", "EUR");
        MerchantAccount eurAccount = new MerchantAccount(eurBalance);
        
        assertEquals(Money.of("150.00", "EUR"), eurAccount.getBalance());
        assertEquals(Money.of("150.00", "EUR"), eurAccount.getTotalIncome());
        
        // Test with JPY
        Money jpyBalance = Money.of("10000.00", "JPY");
        Money jpyTotalIncome = Money.of("50000.00", "JPY");
        MerchantAccount jpyAccount = new MerchantAccount(jpyBalance, jpyTotalIncome);
        
        assertEquals(Money.of("10000.00", "JPY"), jpyAccount.getBalance());
        assertEquals(Money.of("50000.00", "JPY"), jpyAccount.getTotalIncome());
    }

    @Test
    void shouldThrowExceptionForCurrencyMismatchInIncome() {
        MerchantAccount account = new MerchantAccount(Money.of("100.00", "USD"));
        
        // Should throw exception when trying to add income in different currency
        assertThrows(IllegalArgumentException.class, 
            () -> account.addIncome(Money.of("50.00", "EUR")));
    }

    @Test
    void shouldThrowExceptionForCurrencyMismatchInWithdrawal() {
        MerchantAccount account = new MerchantAccount(Money.of("100.00", "USD"));
        
        // Should throw exception when trying to withdraw in different currency
        assertThrows(IllegalArgumentException.class, 
            () -> account.withdraw(Money.of("30.00", "EUR")));
    }

    @Test
    void shouldHandleZeroBalanceAccount() {
        MerchantAccount account = new MerchantAccount(Money.zero("USD"));
        
        assertEquals(Money.zero("USD"), account.getBalance());
        assertEquals(Money.zero("USD"), account.getTotalIncome());
        
        // Should be able to add income to zero balance account
        MerchantAccount afterIncome = account.addIncome(Money.of("100.00", "USD"));
        assertEquals(Money.of("100.00", "USD"), afterIncome.getBalance());
        assertEquals(Money.of("100.00", "USD"), afterIncome.getTotalIncome());
        
        // Should not be able to withdraw from zero balance
        assertFalse(account.hasEnoughBalance(Money.of("1.00", "USD")));
    }

    @Test
    void shouldHandleLargeAmounts() {
        Money largeBalance = Money.of("999999999.99", "USD");
        Money largeTotalIncome = Money.of("1999999999.99", "USD");
        MerchantAccount account = new MerchantAccount(largeBalance, largeTotalIncome);
        
        assertEquals(largeBalance, account.getBalance());
        assertEquals(largeTotalIncome, account.getTotalIncome());
        
        // Should handle large income addition
        Money largeIncome = Money.of("1000000.00", "USD");
        MerchantAccount afterIncome = account.addIncome(largeIncome);
        assertEquals(Money.of("1000999999.99", "USD"), afterIncome.getBalance());
        assertEquals(Money.of("2000999999.99", "USD"), afterIncome.getTotalIncome());
    }

    @Test
    void shouldHandleSmallDecimalAmounts() {
        Money smallBalance = Money.of("0.01", "USD");
        Money smallTotalIncome = Money.of("0.02", "USD");
        MerchantAccount account = new MerchantAccount(smallBalance, smallTotalIncome);
        
        assertEquals(Money.of("0.01", "USD"), account.getBalance());
        assertEquals(Money.of("0.02", "USD"), account.getTotalIncome());
        
        // Should handle small income addition
        MerchantAccount afterIncome = account.addIncome(Money.of("0.01", "USD"));
        assertEquals(Money.of("0.02", "USD"), afterIncome.getBalance());
        assertEquals(Money.of("0.03", "USD"), afterIncome.getTotalIncome());
        
        // Should handle small withdrawal
        MerchantAccount afterWithdrawal = afterIncome.withdraw(Money.of("0.01", "USD"));
        assertEquals(Money.of("0.01", "USD"), afterWithdrawal.getBalance());
        assertEquals(Money.of("0.03", "USD"), afterWithdrawal.getTotalIncome());
    }

    @Test
    void shouldChainOperations() {
        MerchantAccount account = new MerchantAccount(Money.of("100.00", "USD"));
        
        // Chain multiple operations
        MerchantAccount result = account
            .addIncome(Money.of("50.00", "USD"))
            .addIncome(Money.of("25.00", "USD"))
            .withdraw(Money.of("30.00", "USD"))
            .addIncome(Money.of("10.00", "USD"));
        
        assertEquals(Money.of("155.00", "USD"), result.getBalance()); // 100 + 50 + 25 - 30 + 10
        assertEquals(Money.of("185.00", "USD"), result.getTotalIncome()); // 100 + 50 + 25 + 10
    }

    @Test
    void shouldEqualsSelf() {
        MerchantAccount account = new MerchantAccount(Money.of("100.00", "USD"));
        assertEquals(account, account);
    }

    @Test
    void shouldNotEqualDifferentClass() {
        MerchantAccount account = new MerchantAccount(Money.of("100.00", "USD"));
        assertNotEquals(account, "not an account");
        assertNotEquals(account, 123);
        assertNotEquals(account, null);
    }

    @Test
    void shouldCompareCorrectlyWithComplexScenarios() {
        // Create two accounts that should be equal
        MerchantAccount account1 = new MerchantAccount(Money.of("100.00", "USD"), Money.of("200.00", "USD"));
        MerchantAccount account2 = new MerchantAccount(Money.of("100.00", "USD"), Money.of("200.00", "USD"));
        
        // Should be equal
        assertEquals(account1, account2);
        assertEquals(account1.hashCode(), account2.hashCode());
        
        // Create account by operations to verify immutability doesn't affect equality
        MerchantAccount account3 = new MerchantAccount(Money.of("50.00", "USD"), Money.of("150.00", "USD"))
            .addIncome(Money.of("50.00", "USD"));
        
        assertEquals(account1, account3);
        assertEquals(account1.hashCode(), account3.hashCode());
    }

    @Test
    void shouldThrowExceptionForConstructorWithCurrencyMismatch() {
        Money usdBalance = Money.of("100.00", "USD");
        Money eurTotalIncome = Money.of("200.00", "EUR");
        
        // Should throw exception when balance and total income have different currencies
        assertThrows(IllegalArgumentException.class, 
            () -> new MerchantAccount(usdBalance, eurTotalIncome));
    }

    @Test
    void shouldHandleRepeatedOperations() {
        MerchantAccount account = new MerchantAccount(Money.zero("USD"));
        
        // Perform 100 small operations
        for (int i = 1; i <= 100; i++) {
            account = account.addIncome(Money.of("1.00", "USD"));
        }
        
        assertEquals(Money.of("100.00", "USD"), account.getBalance());
        assertEquals(Money.of("100.00", "USD"), account.getTotalIncome());
        
        // Now withdraw 50 times
        for (int i = 1; i <= 50; i++) {
            account = account.withdraw(Money.of("1.00", "USD"));
        }
        
        assertEquals(Money.of("50.00", "USD"), account.getBalance());
        assertEquals(Money.of("100.00", "USD"), account.getTotalIncome()); // Total income unchanged
    }
}