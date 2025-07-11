package com.ecommerce.domain.merchant;

import com.ecommerce.domain.Money;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class MerchantTest {

    @Test
    void shouldCreateMerchant() {
        Merchant merchant = new Merchant("Test Store", "BL123456", "test@store.com", "555-1234");
        
        assertEquals("Test Store", merchant.getMerchantName());
        assertEquals("BL123456", merchant.getBusinessLicense());
        assertEquals("test@store.com", merchant.getContactEmail());
        assertEquals("555-1234", merchant.getContactPhone());
        assertEquals(MerchantStatus.ACTIVE, merchant.getStatus());
        assertTrue(merchant.isActive());
        assertEquals(Money.zero("CNY"), merchant.getBalance());
        assertEquals(Money.zero("CNY"), merchant.getTotalIncome());
        assertNotNull(merchant.getAccount());
    }

    @Test
    void shouldCreateMerchantWithCustomCurrency() {
        Merchant merchant = new Merchant("Test Store", "BL123456", "test@store.com", "555-1234", "USD");
        
        assertEquals("Test Store", merchant.getMerchantName());
        assertEquals("BL123456", merchant.getBusinessLicense());
        assertEquals("test@store.com", merchant.getContactEmail());
        assertEquals("555-1234", merchant.getContactPhone());
        assertEquals(MerchantStatus.ACTIVE, merchant.getStatus());
        assertTrue(merchant.isActive());
        assertEquals(Money.zero("USD"), merchant.getBalance());
        assertEquals(Money.zero("USD"), merchant.getTotalIncome());
        assertNotNull(merchant.getAccount());
    }

    @Test
    void shouldCreateMerchantWithDefaultCurrency() {
        Merchant merchant1 = new Merchant("Store 1", "BL111111", "store1@test.com", "555-0001");
        Merchant merchant2 = new Merchant("Store 2", "BL222222", "store2@test.com", "555-0002", "CNY");
        
        // Both should have CNY currency
        assertEquals(Money.zero("CNY"), merchant1.getBalance());
        assertEquals(Money.zero("CNY"), merchant2.getBalance());
    }

    @Test
    void shouldReceiveIncome() {
        Merchant merchant = new Merchant("Test Store", "BL123456", "test@store.com", "555-1234");
        Money incomeAmount = Money.of("100.00", "CNY");
        
        merchant.receiveIncome(incomeAmount);
        
        assertEquals(Money.of("100.00", "CNY"), merchant.getBalance());
        assertEquals(Money.of("100.00", "CNY"), merchant.getTotalIncome());
    }

    @Test
    void shouldReceiveMultipleIncomes() {
        Merchant merchant = new Merchant("Test Store", "BL123456", "test@store.com", "555-1234");
        
        merchant.receiveIncome(Money.of("50.00", "CNY"));
        merchant.receiveIncome(Money.of("30.00", "CNY"));
        
        assertEquals(Money.of("80.00", "CNY"), merchant.getBalance());
        assertEquals(Money.of("80.00", "CNY"), merchant.getTotalIncome());
    }

    @Test
    void shouldThrowExceptionWhenReceivingNullOrZeroIncome() {
        Merchant merchant = new Merchant("Test Store", "BL123456", "test@store.com", "555-1234");
        
        assertThrows(IllegalArgumentException.class, () -> merchant.receiveIncome(null));
        assertThrows(IllegalArgumentException.class, () -> merchant.receiveIncome(Money.zero("CNY")));
    }

    @Test
    void shouldWithdrawIncome() {
        Merchant merchant = new Merchant("Test Store", "BL123456", "test@store.com", "555-1234");
        merchant.receiveIncome(Money.of("100.00", "CNY"));
        
        merchant.withdrawIncome(Money.of("30.00", "CNY"));
        
        assertEquals(Money.of("70.00", "CNY"), merchant.getBalance());
        assertEquals(Money.of("100.00", "CNY"), merchant.getTotalIncome()); // Total income unchanged
    }

    @Test
    void shouldThrowExceptionWhenWithdrawingMoreThanBalance() {
        Merchant merchant = new Merchant("Test Store", "BL123456", "test@store.com", "555-1234");
        merchant.receiveIncome(Money.of("50.00", "CNY"));
        
        assertThrows(InsufficientFundsException.class, 
            () -> merchant.withdrawIncome(Money.of("100.00", "CNY")));
    }

    @Test
    void shouldThrowExceptionWhenWithdrawingNullOrZeroAmount() {
        Merchant merchant = new Merchant("Test Store", "BL123456", "test@store.com", "555-1234");
        merchant.receiveIncome(Money.of("100.00", "CNY"));
        
        assertThrows(IllegalArgumentException.class, () -> merchant.withdrawIncome(null));
        assertThrows(IllegalArgumentException.class, () -> merchant.withdrawIncome(Money.zero("CNY")));
    }

    @Test
    void shouldCheckIfCanWithdraw() {
        Merchant merchant = new Merchant("Test Store", "BL123456", "test@store.com", "555-1234");
        merchant.receiveIncome(Money.of("100.00", "CNY"));
        
        assertTrue(merchant.canWithdraw(Money.of("50.00", "CNY")));
        assertTrue(merchant.canWithdraw(Money.of("100.00", "CNY")));
        assertFalse(merchant.canWithdraw(Money.of("150.00", "CNY")));
    }

    @Test
    void shouldActivateMerchant() {
        Merchant merchant = new Merchant("Test Store", "BL123456", "test@store.com", "555-1234");
        
        merchant.deactivate();
        assertFalse(merchant.isActive());
        assertEquals(MerchantStatus.INACTIVE, merchant.getStatus());
        
        merchant.activate();
        assertTrue(merchant.isActive());
        assertEquals(MerchantStatus.ACTIVE, merchant.getStatus());
    }

    @Test
    void shouldDeactivateMerchant() {
        Merchant merchant = new Merchant("Test Store", "BL123456", "test@store.com", "555-1234");
        
        merchant.deactivate();
        
        assertFalse(merchant.isActive());
        assertEquals(MerchantStatus.INACTIVE, merchant.getStatus());
    }

    @Test
    void shouldThrowExceptionWhenPerformingOperationsOnInactiveMerchant() {
        Merchant merchant = new Merchant("Test Store", "BL123456", "test@store.com", "555-1234");
        merchant.receiveIncome(Money.of("100.00", "CNY"));
        merchant.deactivate();
        
        assertThrows(IllegalStateException.class, 
            () -> merchant.receiveIncome(Money.of("50.00", "CNY")));
        assertThrows(IllegalStateException.class, 
            () -> merchant.withdrawIncome(Money.of("50.00", "CNY")));
    }

    @Test
    void shouldAllowActivatingInactiveMerchant() {
        Merchant merchant = new Merchant("Test Store", "BL123456", "test@store.com", "555-1234");
        merchant.deactivate();
        
        // Should be able to activate even when inactive
        merchant.activate();
        assertTrue(merchant.isActive());
        
        // Should be able to perform operations after reactivation
        merchant.receiveIncome(Money.of("50.00", "CNY"));
        assertEquals(Money.of("50.00", "CNY"), merchant.getBalance());
    }

    @Test
    void shouldGetCorrectMerchantDetails() {
        Merchant merchant = new Merchant("My Store", "BL789012", "contact@mystore.com", "555-9876");
        
        assertEquals("My Store", merchant.getMerchantName());
        assertEquals("BL789012", merchant.getBusinessLicense());
        assertEquals("contact@mystore.com", merchant.getContactEmail());
        assertEquals("555-9876", merchant.getContactPhone());
        assertEquals(MerchantStatus.ACTIVE, merchant.getStatus());
        assertNotNull(merchant.getAccount());
    }

    @Test
    void shouldMaintainCorrectAccountState() {
        Merchant merchant = new Merchant("Test Store", "BL123456", "test@store.com", "555-1234");
        
        // Initial state
        assertEquals(Money.zero("CNY"), merchant.getBalance());
        assertEquals(Money.zero("CNY"), merchant.getTotalIncome());
        
        // After receiving income
        merchant.receiveIncome(Money.of("200.00", "CNY"));
        assertEquals(Money.of("200.00", "CNY"), merchant.getBalance());
        assertEquals(Money.of("200.00", "CNY"), merchant.getTotalIncome());
        
        // After withdrawal
        merchant.withdrawIncome(Money.of("50.00", "CNY"));
        assertEquals(Money.of("150.00", "CNY"), merchant.getBalance());
        assertEquals(Money.of("200.00", "CNY"), merchant.getTotalIncome()); // Total income unchanged
        
        // After more income
        merchant.receiveIncome(Money.of("100.00", "CNY"));
        assertEquals(Money.of("250.00", "CNY"), merchant.getBalance()); // 150 + 100
        assertEquals(Money.of("300.00", "CNY"), merchant.getTotalIncome()); // 200 + 100
    }

    @Test
    void shouldHandleComplexTransactionScenario() {
        Merchant merchant = new Merchant("Complex Store", "BL999999", "complex@store.com", "555-0000");
        
        // Series of transactions
        merchant.receiveIncome(Money.of("1000.00", "CNY"));
        merchant.withdrawIncome(Money.of("300.00", "CNY"));
        merchant.receiveIncome(Money.of("500.00", "CNY"));
        merchant.withdrawIncome(Money.of("200.00", "CNY"));
        
        // Final state should be correct
        assertEquals(Money.of("1000.00", "CNY"), merchant.getBalance()); // 1000 - 300 + 500 - 200
        assertEquals(Money.of("1500.00", "CNY"), merchant.getTotalIncome()); // 1000 + 500
        
        // Should still be able to withdraw remaining balance
        assertTrue(merchant.canWithdraw(Money.of("1000.00", "CNY")));
        assertFalse(merchant.canWithdraw(Money.of("1001.00", "CNY")));
    }

    @Test
    void shouldTestDefaultConstructor() {
        // Test the protected default constructor
        Merchant merchant = new Merchant();
        
        // Default constructor should set base entity fields
        assertNotNull(merchant);
        assertNull(merchant.getMerchantName());
        assertNull(merchant.getBusinessLicense());
        assertNull(merchant.getContactEmail());
        assertNull(merchant.getContactPhone());
        assertNull(merchant.getAccount());
        assertNull(merchant.getStatus());
    }

    @Test
    void shouldTestPackagePrivateSetters() {
        Merchant merchant = new Merchant();
        
        // Test setMerchantName
        merchant.setMerchantName("Test Merchant");
        assertEquals("Test Merchant", merchant.getMerchantName());
        
        // Test setBusinessLicense
        merchant.setBusinessLicense("BL123456");
        assertEquals("BL123456", merchant.getBusinessLicense());
        
        // Test setContactEmail
        merchant.setContactEmail("test@merchant.com");
        assertEquals("test@merchant.com", merchant.getContactEmail());
        
        // Test setContactPhone
        merchant.setContactPhone("555-1234");
        assertEquals("555-1234", merchant.getContactPhone());
        
        // Test setAccount
        MerchantAccount account = new MerchantAccount(Money.of("100.00", "CNY"));
        merchant.setAccount(account);
        assertEquals(account, merchant.getAccount());
        
        // Test setStatus
        merchant.setStatus(MerchantStatus.INACTIVE);
        assertEquals(MerchantStatus.INACTIVE, merchant.getStatus());
        
        // Test setStatus with null
        merchant.setStatus(null);
        assertNull(merchant.getStatus());
    }

    @Test
    void shouldHandleNullValues() {
        Merchant merchant = new Merchant();
        
        // Test with null values
        merchant.setMerchantName(null);
        assertNull(merchant.getMerchantName());
        
        merchant.setBusinessLicense(null);
        assertNull(merchant.getBusinessLicense());
        
        merchant.setContactEmail(null);
        assertNull(merchant.getContactEmail());
        
        merchant.setContactPhone(null);
        assertNull(merchant.getContactPhone());
        
        merchant.setAccount(null);
        assertNull(merchant.getAccount());
    }

    @Test
    void shouldTestMerchantWithDifferentCurrencies() {
        Merchant usdMerchant = new Merchant("USD Store", "BL111111", "usd@store.com", "555-0001", "USD");
        Merchant eurMerchant = new Merchant("EUR Store", "BL222222", "eur@store.com", "555-0002", "EUR");
        
        usdMerchant.receiveIncome(Money.of("100.00", "USD"));
        eurMerchant.receiveIncome(Money.of("200.00", "EUR"));
        
        assertEquals(Money.of("100.00", "USD"), usdMerchant.getBalance());
        assertEquals(Money.of("200.00", "EUR"), eurMerchant.getBalance());
    }

    @Test
    void shouldTestCanWithdrawWithZeroBalance() {
        Merchant merchant = new Merchant("Test Store", "BL123456", "test@store.com", "555-1234");
        
        // Should not be able to withdraw anything with zero balance
        assertFalse(merchant.canWithdraw(Money.of("0.01", "CNY")));
        assertFalse(merchant.canWithdraw(Money.of("100.00", "CNY")));
    }

    @Test
    void shouldTestCanWithdrawWithExactBalance() {
        Merchant merchant = new Merchant("Test Store", "BL123456", "test@store.com", "555-1234");
        merchant.receiveIncome(Money.of("100.00", "CNY"));
        
        // Should be able to withdraw exact balance
        assertTrue(merchant.canWithdraw(Money.of("100.00", "CNY")));
        
        // Should not be able to withdraw more than balance
        assertFalse(merchant.canWithdraw(Money.of("100.01", "CNY")));
    }

    @Test
    void shouldTestActivateAlreadyActiveMerchant() {
        Merchant merchant = new Merchant("Test Store", "BL123456", "test@store.com", "555-1234");
        
        // Should already be active
        assertTrue(merchant.isActive());
        
        // Activating again should not cause issues
        merchant.activate();
        assertTrue(merchant.isActive());
        assertEquals(MerchantStatus.ACTIVE, merchant.getStatus());
    }

    @Test
    void shouldTestDeactivateAlreadyInactiveMerchant() {
        Merchant merchant = new Merchant("Test Store", "BL123456", "test@store.com", "555-1234");
        
        // Deactivate first
        merchant.deactivate();
        assertFalse(merchant.isActive());
        
        // Deactivating again should not cause issues
        merchant.deactivate();
        assertFalse(merchant.isActive());
        assertEquals(MerchantStatus.INACTIVE, merchant.getStatus());
    }

    @Test
    void shouldTestMerchantInheritanceFromBaseEntity() {
        Merchant merchant = new Merchant("Test Store", "BL123456", "test@store.com", "555-1234");
        
        // Test that Merchant inherits from BaseEntity
        // ID can be null for new entities until persisted
        assertNotNull(merchant.getCreatedAt());
        assertNotNull(merchant.getUpdatedAt());
        assertNotNull(merchant.getVersion());
        
        // Test that operations mark the entity as updated
        LocalDateTime originalUpdatedAt = merchant.getUpdatedAt();
        Long originalVersion = merchant.getVersion();
        
        // Wait a bit to ensure different timestamp
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        merchant.receiveIncome(Money.of("100.00", "CNY"));
        
        // Verify entity was marked as updated
        assertTrue(merchant.getUpdatedAt().isAfter(originalUpdatedAt) || 
                  merchant.getVersion() > originalVersion);
    }

    @Test
    void shouldTestMerchantWithEmptyStrings() {
        Merchant merchant = new Merchant("", "", "", "");
        
        assertEquals("", merchant.getMerchantName());
        assertEquals("", merchant.getBusinessLicense());
        assertEquals("", merchant.getContactEmail());
        assertEquals("", merchant.getContactPhone());
        assertEquals(MerchantStatus.ACTIVE, merchant.getStatus());
    }

    @Test
    void shouldTestMerchantWithWhitespaceStrings() {
        Merchant merchant = new Merchant("  ", "  ", "  ", "  ");
        
        assertEquals("  ", merchant.getMerchantName());
        assertEquals("  ", merchant.getBusinessLicense());
        assertEquals("  ", merchant.getContactEmail());
        assertEquals("  ", merchant.getContactPhone());
    }

    @Test
    void shouldTestMerchantWithSpecialCharacters() {
        Merchant merchant = new Merchant("Store!@#", "BL!@#$%", "test@store.com!@#", "555-1234!@#");
        
        assertEquals("Store!@#", merchant.getMerchantName());
        assertEquals("BL!@#$%", merchant.getBusinessLicense());
        assertEquals("test@store.com!@#", merchant.getContactEmail());
        assertEquals("555-1234!@#", merchant.getContactPhone());
    }

    @Test
    void shouldTestMerchantWithVeryLongStrings() {
        String longString = "A".repeat(1000);
        Merchant merchant = new Merchant(longString, longString, longString, longString);
        
        assertEquals(longString, merchant.getMerchantName());
        assertEquals(longString, merchant.getBusinessLicense());
        assertEquals(longString, merchant.getContactEmail());
        assertEquals(longString, merchant.getContactPhone());
    }

    @Test
    void shouldTestMerchantWithMinimalPositiveAmounts() {
        Merchant merchant = new Merchant("Test Store", "BL123456", "test@store.com", "555-1234");
        
        // Test with minimal positive amount
        Money minAmount = Money.of("0.01", "CNY");
        merchant.receiveIncome(minAmount);
        assertEquals(minAmount, merchant.getBalance());
        
        // Test withdrawal of minimal amount
        merchant.withdrawIncome(minAmount);
        assertEquals(Money.zero("CNY"), merchant.getBalance());
    }

    @Test
    void shouldTestStatusConsistency() {
        Merchant merchant = new Merchant("Test Store", "BL123456", "test@store.com", "555-1234");
        
        // Test status consistency
        assertEquals(MerchantStatus.ACTIVE, merchant.getStatus());
        assertTrue(merchant.isActive());
        
        merchant.deactivate();
        assertEquals(MerchantStatus.INACTIVE, merchant.getStatus());
        assertFalse(merchant.isActive());
        
        merchant.activate();
        assertEquals(MerchantStatus.ACTIVE, merchant.getStatus());
        assertTrue(merchant.isActive());
    }
}