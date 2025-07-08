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
}