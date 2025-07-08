package com.ecommerce.domain.user;

import com.ecommerce.domain.Money;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void shouldCreateUser() {
        User user = new User("john", "john@example.com", "123-456-7890", "USD");
        
        assertEquals("john", user.getUsername());
        assertEquals("john@example.com", user.getEmail());
        assertEquals("123-456-7890", user.getPhone());
        assertEquals(UserStatus.ACTIVE, user.getStatus());
        assertEquals(Money.zero("USD"), user.getAccount().getBalance());
    }

    @Test
    void shouldRechargeUserAccount() {
        User user = new User("john", "john@example.com", "123-456-7890", "USD");
        Money rechargeAmount = Money.of("50.00", "USD");
        
        user.recharge(rechargeAmount);
        assertEquals(Money.of("50.00", "USD"), user.getAccount().getBalance());
    }

    @Test
    void shouldDeductFromUserAccount() {
        User user = new User("john", "john@example.com", "123-456-7890", "USD");
        user.recharge(Money.of("100.00", "USD"));
        Money deductAmount = Money.of("30.00", "USD");
        
        user.deduct(deductAmount);
        assertEquals(Money.of("70.00", "USD"), user.getAccount().getBalance());
    }

    @Test
    void shouldCheckSufficientBalance() {
        User user = new User("john", "john@example.com", "123-456-7890", "USD");
        user.recharge(Money.of("100.00", "USD"));
        
        assertTrue(user.canAfford(Money.of("50.00", "USD")));
        assertFalse(user.canAfford(Money.of("150.00", "USD")));
    }

    @Test
    void shouldActivateUser() {
        User user = new User("john", "john@example.com", "123-456-7890", "USD");
        user.deactivate();
        assertEquals(UserStatus.INACTIVE, user.getStatus());
        
        user.activate();
        assertEquals(UserStatus.ACTIVE, user.getStatus());
    }

    @Test
    void shouldDeactivateUser() {
        User user = new User("john", "john@example.com", "123-456-7890", "USD");
        
        user.deactivate();
        assertEquals(UserStatus.INACTIVE, user.getStatus());
    }

    @Test
    void shouldThrowExceptionWhenDeductingMoreThanBalance() {
        User user = new User("john", "john@example.com", "123-456-7890", "USD");
        user.recharge(Money.of("50.00", "USD"));
        
        assertThrows(InsufficientBalanceException.class, () -> user.deduct(Money.of("100.00", "USD")));
    }

    @Test
    void shouldThrowExceptionWhenInactiveUserTriesToRecharge() {
        User user = new User("john", "john@example.com", "123-456-7890", "USD");
        user.deactivate();
        
        assertThrows(IllegalStateException.class, () -> user.recharge(Money.of("50.00", "USD")));
    }
}