package com.ecommerce.domain.merchant;

import com.ecommerce.domain.Money;
import org.junit.jupiter.api.Test;
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
}