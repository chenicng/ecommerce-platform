package com.ecommerce.domain.user;

import com.ecommerce.domain.Money;
import com.ecommerce.domain.ResourceInactiveException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("User Domain Tests")
class UserTest {

    @Nested
    @DisplayName("User Creation")
    class UserCreationTests {
        
        @Test
        @DisplayName("Should create user with valid parameters")
        void shouldCreateUserWithValidParameters() {
            User user = new User("john", "john@example.com", "123-456-7890", "USD");
            
            assertEquals("john", user.getUsername());
            assertEquals("john@example.com", user.getEmail());
            assertEquals("123-456-7890", user.getPhone());
            assertEquals(UserStatus.ACTIVE, user.getStatus());
            assertEquals(Money.zero("USD"), user.getBalance());
            assertTrue(user.isActive());
        }

        @Test
        @DisplayName("Should create user with default CNY currency")
        void shouldCreateUserWithDefaultCurrency() {
            User user = new User("alice", "alice@example.com", "987-654-3210", "CNY");
            
            assertEquals("CNY", user.getBalance().getCurrency());
            assertTrue(user.getBalance().isZero());
        }
    }

    @Nested
    @DisplayName("Account Operations")
    class AccountOperationTests {
        
        @Test
        @DisplayName("Should recharge user account successfully")
        void shouldRechargeUserAccountSuccessfully() {
            User user = new User("john", "john@example.com", "123-456-7890", "USD");
            Money rechargeAmount = Money.of("50.00", "USD");
            
            user.recharge(rechargeAmount);
            
            assertEquals(Money.of("50.00", "USD"), user.getBalance());
        }

        @Test
        @DisplayName("Should deduct from user account when sufficient balance")
        void shouldDeductFromUserAccountWhenSufficientBalance() {
            User user = new User("john", "john@example.com", "123-456-7890", "USD");
            user.recharge(Money.of("100.00", "USD"));
            Money deductAmount = Money.of("30.00", "USD");
            
            user.deduct(deductAmount);
            
            assertEquals(Money.of("70.00", "USD"), user.getBalance());
        }
        
        @Test
        @DisplayName("Should throw exception when deducting more than balance")
        void shouldThrowExceptionWhenDeductingMoreThanBalance() {
            User user = new User("john", "john@example.com", "123-456-7890", "USD");
            user.recharge(Money.of("50.00", "USD"));
            Money deductAmount = Money.of("100.00", "USD");
            
            assertThrows(InsufficientBalanceException.class, () -> user.deduct(deductAmount));
        }

        @Test
        @DisplayName("Should throw exception when recharging with null amount")
        void shouldThrowExceptionWhenRechargingWithNullAmount() {
            User user = new User("john", "john@example.com", "123-456-7890", "USD");
            
            assertThrows(IllegalArgumentException.class, () -> user.recharge(null));
        }

        @Test
        @DisplayName("Should throw exception when recharging with zero amount")
        void shouldThrowExceptionWhenRechargingWithZeroAmount() {
            User user = new User("john", "john@example.com", "123-456-7890", "USD");
            Money zeroAmount = Money.zero("USD");
            
            assertThrows(IllegalArgumentException.class, () -> user.recharge(zeroAmount));
        }
    }

    @Nested
    @DisplayName("Balance Checking")
    class BalanceCheckingTests {
        
        @Test
        @DisplayName("Should return true when user can afford amount")
        void shouldReturnTrueWhenUserCanAffordAmount() {
            User user = new User("john", "john@example.com", "123-456-7890", "USD");
            user.recharge(Money.of("100.00", "USD"));
            
            assertTrue(user.canAfford(Money.of("50.00", "USD")));
            assertTrue(user.canAfford(Money.of("100.00", "USD")));
        }

        @Test
        @DisplayName("Should return false when user cannot afford amount")
        void shouldReturnFalseWhenUserCannotAffordAmount() {
            User user = new User("john", "john@example.com", "123-456-7890", "USD");
            user.recharge(Money.of("100.00", "USD"));
            
            assertFalse(user.canAfford(Money.of("150.00", "USD")));
        }
    }

    @Nested
    @DisplayName("User Status Management")
    class UserStatusTests {
        
        @Test
        @DisplayName("Should activate inactive user")
        void shouldActivateInactiveUser() {
            User user = new User("john", "john@example.com", "123-456-7890", "USD");
            user.deactivate();
            assertEquals(UserStatus.INACTIVE, user.getStatus());
            assertFalse(user.isActive());
            
            user.activate();
            
            assertEquals(UserStatus.ACTIVE, user.getStatus());
            assertTrue(user.isActive());
        }

        @Test
        @DisplayName("Should throw exception when operating on inactive user")
        void shouldThrowExceptionWhenOperatingOnInactiveUser() {
            User user = new User("john", "john@example.com", "123-456-7890", "USD");
            user.deactivate();
            Money amount = Money.of("50.00", "USD");
            
            assertThrows(ResourceInactiveException.class, () -> user.recharge(amount));
            assertThrows(ResourceInactiveException.class, () -> user.deduct(amount));
        }
    }
}