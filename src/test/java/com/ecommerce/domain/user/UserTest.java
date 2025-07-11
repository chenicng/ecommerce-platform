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

    @Test
    void shouldHandleRechargeWithZeroAmount() {
        // Given
        User user = new User("testuser", "test@example.com", "123456789", "CNY");
        
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> user.recharge(Money.zero("CNY")));
        assertEquals("Recharge amount must be positive", exception.getMessage());
    }

    @Test
    void shouldHandleRechargeWithNullAmount() {
        // Given
        User user = new User("testuser", "test@example.com", "123456789", "CNY");
        
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> user.recharge(null));
        assertEquals("Recharge amount must be positive", exception.getMessage());
    }

    @Test
    void shouldHandleDeductWithZeroAmount() {
        // Given
        User user = new User("testuser", "test@example.com", "123456789", "CNY");
        user.recharge(Money.of("100.00", "CNY"));
        
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> user.deduct(Money.zero("CNY")));
        assertEquals("Deduct amount must be positive", exception.getMessage());
    }

    @Test
    void shouldHandleDeductWithNullAmount() {
        // Given
        User user = new User("testuser", "test@example.com", "123456789", "CNY");
        user.recharge(Money.of("100.00", "CNY"));
        
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> user.deduct(null));
        assertEquals("Deduct amount must be positive", exception.getMessage());
    }

    @Test
    void shouldHandleRechargeWhenInactive() {
        // Given
        User user = new User("testuser", "test@example.com", "123456789", "CNY");
        user.deactivate();
        
        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> user.recharge(Money.of("100.00", "CNY")));
        assertEquals("User is not active", exception.getMessage());
    }

    @Test
    void shouldHandleDeductWhenInactive() {
        // Given
        User user = new User("testuser", "test@example.com", "123456789", "CNY");
        user.recharge(Money.of("100.00", "CNY"));
        user.deactivate();
        
        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> user.deduct(Money.of("50.00", "CNY")));
        assertEquals("User is not active", exception.getMessage());
    }

    @Test
    void shouldHandleActivationAndDeactivation() {
        // Given
        User user = new User("testuser", "test@example.com", "123456789", "CNY");
        
        // When & Then
        assertTrue(user.isActive());
        
        user.deactivate();
        assertFalse(user.isActive());
        
        user.activate();
        assertTrue(user.isActive());
    }

    @Test
    void shouldHandleMultipleRecharges() {
        // Given
        User user = new User("testuser", "test@example.com", "123456789", "CNY");
        
        // When
        user.recharge(Money.of("100.00", "CNY"));
        user.recharge(Money.of("200.00", "CNY"));
        user.recharge(Money.of("50.00", "CNY"));
        
        // Then
        assertEquals(Money.of("350.00", "CNY"), user.getBalance());
    }

    @Test
    void shouldHandleMultipleDeductions() {
        // Given
        User user = new User("testuser", "test@example.com", "123456789", "CNY");
        user.recharge(Money.of("500.00", "CNY"));
        
        // When
        user.deduct(Money.of("100.00", "CNY"));
        user.deduct(Money.of("200.00", "CNY"));
        user.deduct(Money.of("50.00", "CNY"));
        
        // Then
        assertEquals(Money.of("150.00", "CNY"), user.getBalance());
    }

    @Test
    void shouldHandleExactBalanceDeduction() {
        // Given
        User user = new User("testuser", "test@example.com", "123456789", "CNY");
        user.recharge(Money.of("100.00", "CNY"));
        
        // When
        user.deduct(Money.of("100.00", "CNY"));
        
        // Then
        assertTrue(user.getBalance().isZero());
    }
    
    @Test
    void shouldHandleLargeAmountOperations() {
        // Given
        User user = new User("testuser", "test@example.com", "123456789", "CNY");
        user.recharge(Money.of("999999.99", "CNY"));
        
        // When
        user.deduct(Money.of("999999.99", "CNY"));
        
        // Then
        assertTrue(user.getBalance().isZero());
    }
    
    @Test
    void shouldHandleCurrencyCaseInsensitivity() {
        // Given
        User user = new User("testuser", "test@example.com", "123456789", "cny");
        
        // Then
        assertEquals("CNY", user.getBalance().getCurrency());
    }

    @Test
    void shouldHandleCanAffordWithExactAmount() {
        // Given
        User user = new User("testuser", "test@example.com", "123456789", "CNY");
        user.recharge(Money.of("100.00", "CNY"));
        
        // When & Then
        assertTrue(user.canAfford(Money.of("100.00", "CNY")));
    }

    @Test
    void shouldHandleCanAffordWithLessAmount() {
        // Given
        User user = new User("testuser", "test@example.com", "123456789", "CNY");
        user.recharge(Money.of("100.00", "CNY"));
        
        // When & Then
        assertTrue(user.canAfford(Money.of("50.00", "CNY")));
    }

    @Test
    void shouldHandleCanAffordWithZeroBalance() {
        // Given
        User user = new User("testuser", "test@example.com", "123456789", "CNY");
        
        // When & Then
        assertFalse(user.canAfford(Money.of("1.00", "CNY")));
    }

    @Test
    void shouldCreateUserWithDefaultConstructor() {
        // Test package-private default constructor
        User user = new User();
        assertNotNull(user);
        assertNull(user.getUsername());
        assertNull(user.getEmail());
        assertNull(user.getPhone());
        assertNull(user.getAccount());
        assertNull(user.getStatus());
    }

    @Test
    void shouldSetUsernameWithPackagePrivateSetter() {
        // Test package-private setter
        User user = new User();
        user.setUsername("newuser");
        assertEquals("newuser", user.getUsername());
    }

    @Test
    void shouldSetEmailWithPackagePrivateSetter() {
        // Test package-private setter
        User user = new User();
        user.setEmail("new@example.com");
        assertEquals("new@example.com", user.getEmail());
    }

    @Test
    void shouldSetPhoneWithPackagePrivateSetter() {
        // Test package-private setter
        User user = new User();
        user.setPhone("987654321");
        assertEquals("987654321", user.getPhone());
    }

    @Test
    void shouldSetAccountWithPackagePrivateSetter() {
        // Test package-private setter
        User user = new User();
        UserAccount account = new UserAccount(Money.of("100.00", "CNY"));
        user.setAccount(account);
        assertEquals(account, user.getAccount());
    }

    @Test
    void shouldSetStatusWithPackagePrivateSetter() {
        // Test package-private setter
        User user = new User();
        user.setStatus(UserStatus.INACTIVE);
        assertEquals(UserStatus.INACTIVE, user.getStatus());
    }

    @Test
    void shouldHandleNullAmountInCanAfford() {
        // Given
        User user = new User("testuser", "test@example.com", "123456789", "CNY");
        user.recharge(Money.of("100.00", "CNY"));
        
        // When & Then
        assertThrows(NullPointerException.class, () -> user.canAfford(null));
    }

    @Test
    void shouldHandleBaseEntityInheritance() {
        // Test that User extends BaseEntity
        User user = new User("testuser", "test@example.com", "123456789", "CNY");
        assertTrue(user instanceof com.ecommerce.domain.BaseEntity);
    }

    @Test
    void shouldMarkAsUpdatedWhenRecharging() {
        // Given
        User user = new User("testuser", "test@example.com", "123456789", "CNY");
        
        // When
        user.recharge(Money.of("100.00", "CNY"));
        
        // Then - markAsUpdated() is called internally, verify state change
        assertEquals(Money.of("100.00", "CNY"), user.getBalance());
    }

    @Test
    void shouldMarkAsUpdatedWhenDeducting() {
        // Given
        User user = new User("testuser", "test@example.com", "123456789", "CNY");
        user.recharge(Money.of("100.00", "CNY"));
        
        // When
        user.deduct(Money.of("50.00", "CNY"));
        
        // Then - markAsUpdated() is called internally, verify state change
        assertEquals(Money.of("50.00", "CNY"), user.getBalance());
    }

    @Test
    void shouldMarkAsUpdatedWhenActivating() {
        // Given
        User user = new User("testuser", "test@example.com", "123456789", "CNY");
        user.deactivate();
        
        // When
        user.activate();
        
        // Then - markAsUpdated() is called internally, verify state change
        assertTrue(user.isActive());
    }

    @Test
    void shouldMarkAsUpdatedWhenDeactivating() {
        // Given
        User user = new User("testuser", "test@example.com", "123456789", "CNY");
        
        // When
        user.deactivate();
        
        // Then - markAsUpdated() is called internally, verify state change
        assertFalse(user.isActive());
    }

    @Test
    void shouldHandleUserCreationWithDifferentCurrencies() {
        // Test creation with different currencies
        User usdUser = new User("user1", "user1@example.com", "123456789", "USD");
        User eurUser = new User("user2", "user2@example.com", "987654321", "EUR");
        User jpyUser = new User("user3", "user3@example.com", "555666777", "JPY");
        
        assertEquals("USD", usdUser.getBalance().getCurrency());
        assertEquals("EUR", eurUser.getBalance().getCurrency());
        assertEquals("JPY", jpyUser.getBalance().getCurrency());
    }

    @Test
    void shouldHandleEdgeCaseWithVerySmallAmount() {
        // Given
        User user = new User("testuser", "test@example.com", "123456789", "CNY");
        user.recharge(Money.of("0.01", "CNY"));
        
        // When & Then
        assertTrue(user.canAfford(Money.of("0.01", "CNY")));
        assertFalse(user.canAfford(Money.of("0.02", "CNY")));
    }

    @Test
    void shouldHandleStatusTransitions() {
        // Given
        User user = new User("testuser", "test@example.com", "123456789", "CNY");
        
        // Initial state
        assertEquals(UserStatus.ACTIVE, user.getStatus());
        assertTrue(user.isActive());
        
        // Deactivate
        user.deactivate();
        assertEquals(UserStatus.INACTIVE, user.getStatus());
        assertFalse(user.isActive());
        
        // Reactivate
        user.activate();
        assertEquals(UserStatus.ACTIVE, user.getStatus());
        assertTrue(user.isActive());
    }
}