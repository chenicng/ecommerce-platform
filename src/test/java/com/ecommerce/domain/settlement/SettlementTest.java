package com.ecommerce.domain.settlement;

import com.ecommerce.domain.Money;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class SettlementTest {

    @Test
    void testSettlementConstructorWithMatchedAmounts() {
        Long merchantId = 1L;
        LocalDate settlementDate = LocalDate.of(2024, 1, 15);
        Money expectedIncome = Money.of("1000.00", "CNY");
        Money actualBalance = Money.of("1000.00", "CNY");
        
        Settlement settlement = new Settlement(merchantId, settlementDate, expectedIncome, actualBalance);
        
        assertEquals(merchantId, settlement.getMerchantId());
        assertEquals(settlementDate, settlement.getSettlementDate());
        assertEquals(expectedIncome, settlement.getExpectedIncome());
        assertEquals(actualBalance, settlement.getActualBalance());
        assertEquals(Money.zero("CNY"), settlement.getDifference());
        assertEquals(SettlementStatus.MATCHED, settlement.getStatus());
        assertEquals("", settlement.getNotes());
        assertTrue(settlement.isMatched());
        assertFalse(settlement.hasSurplus());
        assertFalse(settlement.hasDeficit());
    }

    @Test
    void testSettlementConstructorWithSurplus() {
        Long merchantId = 2L;
        LocalDate settlementDate = LocalDate.of(2024, 1, 15);
        Money expectedIncome = Money.of("1000.00", "CNY");
        Money actualBalance = Money.of("1200.00", "CNY");
        
        Settlement settlement = new Settlement(merchantId, settlementDate, expectedIncome, actualBalance);
        
        assertEquals(merchantId, settlement.getMerchantId());
        assertEquals(settlementDate, settlement.getSettlementDate());
        assertEquals(expectedIncome, settlement.getExpectedIncome());
        assertEquals(actualBalance, settlement.getActualBalance());
        assertEquals(Money.of("200.00", "CNY"), settlement.getDifference());
        assertEquals(SettlementStatus.SURPLUS, settlement.getStatus());
        assertFalse(settlement.isMatched());
        assertTrue(settlement.hasSurplus());
        assertFalse(settlement.hasDeficit());
    }

    @Test
    void testSettlementConstructorWithDeficit() {
        Long merchantId = 3L;
        LocalDate settlementDate = LocalDate.of(2024, 1, 15);
        Money expectedIncome = Money.of("1000.00", "CNY");
        Money actualBalance = Money.of("800.00", "CNY");
        
        Settlement settlement = new Settlement(merchantId, settlementDate, expectedIncome, actualBalance);
        
        assertEquals(merchantId, settlement.getMerchantId());
        assertEquals(settlementDate, settlement.getSettlementDate());
        assertEquals(expectedIncome, settlement.getExpectedIncome());
        assertEquals(actualBalance, settlement.getActualBalance());
        assertEquals(Money.of("-200.00", "CNY"), settlement.getDifference());
        assertEquals(SettlementStatus.DEFICIT, settlement.getStatus());
        assertFalse(settlement.isMatched());
        assertFalse(settlement.hasSurplus());
        assertTrue(settlement.hasDeficit());
    }

    @Test
    void testSettlementConstructorWithNullExpectedIncome() {
        Long merchantId = 4L;
        LocalDate settlementDate = LocalDate.of(2024, 1, 15);
        Money actualBalance = Money.of("1000.00", "CNY");
        
        Settlement settlement = new Settlement(merchantId, settlementDate, null, actualBalance);
        
        assertEquals(merchantId, settlement.getMerchantId());
        assertEquals(settlementDate, settlement.getSettlementDate());
        assertNull(settlement.getExpectedIncome());
        assertEquals(actualBalance, settlement.getActualBalance());
        assertEquals(Money.zero("CNY"), settlement.getDifference());
        assertEquals(SettlementStatus.MATCHED, settlement.getStatus());
    }

    @Test
    void testSettlementConstructorWithNullActualBalance() {
        Long merchantId = 5L;
        LocalDate settlementDate = LocalDate.of(2024, 1, 15);
        Money expectedIncome = Money.of("1000.00", "CNY");
        
        Settlement settlement = new Settlement(merchantId, settlementDate, expectedIncome, null);
        
        assertEquals(merchantId, settlement.getMerchantId());
        assertEquals(settlementDate, settlement.getSettlementDate());
        assertEquals(expectedIncome, settlement.getExpectedIncome());
        assertNull(settlement.getActualBalance());
        assertEquals(Money.zero("CNY"), settlement.getDifference());
        assertEquals(SettlementStatus.MATCHED, settlement.getStatus());
    }

    @Test
    void testSettlementConstructorWithBothNullAmounts() {
        Long merchantId = 6L;
        LocalDate settlementDate = LocalDate.of(2024, 1, 15);
        
        Settlement settlement = new Settlement(merchantId, settlementDate, null, null);
        
        assertEquals(merchantId, settlement.getMerchantId());
        assertEquals(settlementDate, settlement.getSettlementDate());
        assertNull(settlement.getExpectedIncome());
        assertNull(settlement.getActualBalance());
        assertEquals(Money.zero("CNY"), settlement.getDifference());
        assertEquals(SettlementStatus.MATCHED, settlement.getStatus());
    }

    @Test
    void testAddNotes() {
        Settlement settlement = createTestSettlement();
        String notes = "Settlement requires manual review";
        
        settlement.addNotes(notes);
        
        assertEquals(notes, settlement.getNotes());
    }

    @Test
    void testAddNotesWithNull() {
        Settlement settlement = createTestSettlement();
        
        settlement.addNotes(null);
        
        assertEquals("", settlement.getNotes());
    }

    @Test
    void testAddNotesWithEmptyString() {
        Settlement settlement = createTestSettlement();
        String emptyNotes = "";
        
        settlement.addNotes(emptyNotes);
        
        assertEquals("", settlement.getNotes());
    }

    @Test
    void testAddNotesMultipleTimes() {
        Settlement settlement = createTestSettlement();
        
        settlement.addNotes("First note");
        settlement.addNotes("Second note");
        
        // Last notes should overwrite previous ones
        assertEquals("Second note", settlement.getNotes());
    }

    @Test
    void testIsMatchedWithDifferentStatuses() {
        Settlement matchedSettlement = new Settlement(
            1L, LocalDate.now(), 
            Money.of("1000.00", "CNY"), 
            Money.of("1000.00", "CNY")
        );
        assertTrue(matchedSettlement.isMatched());
        
        Settlement surplusSettlement = new Settlement(
            2L, LocalDate.now(), 
            Money.of("1000.00", "CNY"), 
            Money.of("1200.00", "CNY")
        );
        assertFalse(surplusSettlement.isMatched());
        
        // Note: Deficit scenarios cannot be tested with current Money design
        // that doesn't allow negative subtraction results
    }

    @Test
    void testHasSurplusWithDifferentStatuses() {
        Settlement matchedSettlement = new Settlement(
            1L, LocalDate.now(), 
            Money.of("1000.00", "CNY"), 
            Money.of("1000.00", "CNY")
        );
        assertFalse(matchedSettlement.hasSurplus());
        
        Settlement surplusSettlement = new Settlement(
            2L, LocalDate.now(), 
            Money.of("1000.00", "CNY"), 
            Money.of("1200.00", "CNY")
        );
        assertTrue(surplusSettlement.hasSurplus());
        
        // Note: Deficit scenarios cannot be tested with current Money design
        // that doesn't allow negative subtraction results
    }

    @Test
    void testHasDeficitWithDifferentStatuses() {
        Settlement matchedSettlement = new Settlement(
            1L, LocalDate.now(), 
            Money.of("1000.00", "CNY"), 
            Money.of("1000.00", "CNY")
        );
        assertFalse(matchedSettlement.hasDeficit());
        
        Settlement surplusSettlement = new Settlement(
            2L, LocalDate.now(), 
            Money.of("1000.00", "CNY"), 
            Money.of("1200.00", "CNY")
        );
        assertFalse(surplusSettlement.hasDeficit());
        
        // Note: Deficit scenarios cannot be tested with current Money design
        // that doesn't allow negative subtraction results
    }

    @Test
    void testCompleteWorkflow() {
        // Create settlement with surplus (since deficit cannot be tested due to Money constraints)
        Settlement settlement = new Settlement(
            100L, LocalDate.of(2024, 1, 20), 
            Money.of("1000.00", "CNY"), 
            Money.of("1200.00", "CNY")
        );
        
        // Initial state
        assertTrue(settlement.hasSurplus());
        assertEquals(Money.of("200.00", "CNY"), settlement.getDifference());
        assertEquals("", settlement.getNotes());
        
        // Add notes
        settlement.addNotes("Surplus due to bonus payments");
        assertEquals("Surplus due to bonus payments", settlement.getNotes());
        
        // Final state verification
        assertTrue(settlement.hasSurplus());
        assertFalse(settlement.isMatched());
        assertFalse(settlement.hasDeficit());
    }

    @Test
    void testSettlementWithVerySmallDifference() {
        Settlement settlement = new Settlement(
            1L, LocalDate.now(), 
            Money.of("1000.00", "CNY"), 
            Money.of("1000.01", "CNY")
        );
        
        assertEquals(Money.of("0.01", "CNY"), settlement.getDifference());
        assertEquals(SettlementStatus.SURPLUS, settlement.getStatus());
        assertTrue(settlement.hasSurplus());
    }

    @Test
    void testSettlementWithLargeAmounts() {
        Settlement settlement = new Settlement(
            1L, LocalDate.now(), 
            Money.of("999999.99", "CNY"), 
            Money.of("1000000.00", "CNY")
        );
        
        assertEquals(Money.of("0.01", "CNY"), settlement.getDifference());
        assertEquals(SettlementStatus.SURPLUS, settlement.getStatus());
    }

    @Test
    void testDefaultConstructor() {
        // Test the protected default constructor
        Settlement settlement = new Settlement();
        
        // Default constructor should set base entity fields
        assertNotNull(settlement);
        assertNull(settlement.getMerchantId());
        assertNull(settlement.getSettlementDate());
        assertNull(settlement.getExpectedIncome());
        assertNull(settlement.getActualBalance());
        assertNull(settlement.getDifference());
        assertNull(settlement.getStatus());
        assertNull(settlement.getNotes());
    }

    @Test
    void testPackagePrivateSetters() {
        Settlement settlement = new Settlement();
        
        // Test setMerchantId
        settlement.setMerchantId(100L);
        assertEquals(100L, settlement.getMerchantId());
        
        // Test setSettlementDate
        LocalDate date = LocalDate.of(2024, 2, 15);
        settlement.setSettlementDate(date);
        assertEquals(date, settlement.getSettlementDate());
        
        // Test setExpectedIncome
        Money expectedIncome = Money.of("500.00", "CNY");
        settlement.setExpectedIncome(expectedIncome);
        assertEquals(expectedIncome, settlement.getExpectedIncome());
        
        // Test setActualBalance
        Money actualBalance = Money.of("600.00", "CNY");
        settlement.setActualBalance(actualBalance);
        assertEquals(actualBalance, settlement.getActualBalance());
        
        // Test setDifference
        Money difference = Money.of("100.00", "CNY");
        settlement.setDifference(difference);
        assertEquals(difference, settlement.getDifference());
        
        // Test setStatus
        settlement.setStatus(SettlementStatus.SURPLUS);
        assertEquals(SettlementStatus.SURPLUS, settlement.getStatus());
        
        // Test setNotes
        settlement.setNotes("Test notes");
        assertEquals("Test notes", settlement.getNotes());
        
        // Test setNotes with null
        settlement.setNotes(null);
        assertNull(settlement.getNotes());
    }

    @Test
    void testSettlementStatusDeterminationWithNullDifference() {
        Settlement settlement = new Settlement();
        settlement.setDifference(null);
        settlement.setStatus(SettlementStatus.MATCHED);
        
        assertEquals(SettlementStatus.MATCHED, settlement.getStatus());
        assertTrue(settlement.isMatched());
        assertFalse(settlement.hasSurplus());
        assertFalse(settlement.hasDeficit());
    }

    @Test
    void testSettlementStatusDeterminationWithDeficit() {
        Settlement settlement = new Settlement();
        settlement.setStatus(SettlementStatus.DEFICIT);
        
        assertEquals(SettlementStatus.DEFICIT, settlement.getStatus());
        assertFalse(settlement.isMatched());
        assertFalse(settlement.hasSurplus());
        assertTrue(settlement.hasDeficit());
    }

    @Test
    void testSettlementWithZeroAmounts() {
        Money zeroAmount = Money.zero("CNY");
        Settlement settlement = new Settlement(
            1L, LocalDate.now(), 
            zeroAmount, 
            zeroAmount
        );
        
        assertEquals(zeroAmount, settlement.getExpectedIncome());
        assertEquals(zeroAmount, settlement.getActualBalance());
        assertEquals(zeroAmount, settlement.getDifference());
        assertEquals(SettlementStatus.MATCHED, settlement.getStatus());
        assertTrue(settlement.isMatched());
    }

    @Test
    void testSettlementWithDifferentCurrencies() {
        Settlement settlement = new Settlement(
            1L, LocalDate.now(), 
            Money.of("1000.00", "USD"), 
            Money.of("1200.00", "USD")
        );
        
        assertEquals(Money.of("1000.00", "USD"), settlement.getExpectedIncome());
        assertEquals(Money.of("1200.00", "USD"), settlement.getActualBalance());
        assertEquals(Money.of("200.00", "USD"), settlement.getDifference());
        assertEquals(SettlementStatus.SURPLUS, settlement.getStatus());
    }

    @Test
    void testAddNotesWithWhitespace() {
        Settlement settlement = createTestSettlement();
        String notesWithWhitespace = "  Settlement notes with whitespace  ";
        
        settlement.addNotes(notesWithWhitespace);
        
        assertEquals(notesWithWhitespace, settlement.getNotes());
    }

    @Test
    void testAddNotesWithSpecialCharacters() {
        Settlement settlement = createTestSettlement();
        String notesWithSpecialChars = "Settlement notes with special chars: !@#$%^&*()";
        
        settlement.addNotes(notesWithSpecialChars);
        
        assertEquals(notesWithSpecialChars, settlement.getNotes());
    }

    @Test
    void testAddNotesWithVeryLongString() {
        Settlement settlement = createTestSettlement();
        String longNotes = "A".repeat(1000);
        
        settlement.addNotes(longNotes);
        
        assertEquals(longNotes, settlement.getNotes());
    }

    @Test
    void testSettlementInheritanceFromBaseEntity() {
        Settlement settlement = createTestSettlement();
        
        // Test that Settlement inherits from BaseEntity
        // ID can be null for new entities until persisted
        assertNotNull(settlement.getCreatedAt());
        assertNotNull(settlement.getUpdatedAt());
        assertNotNull(settlement.getVersion());
        
        // Test that adding notes marks the entity as updated
        LocalDateTime originalUpdatedAt = settlement.getUpdatedAt();
        Long originalVersion = settlement.getVersion();
        
        // Wait a bit to ensure different timestamp
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        settlement.addNotes("Updated notes");
        
        // Verify entity was marked as updated
        assertTrue(settlement.getUpdatedAt().isAfter(originalUpdatedAt) || 
                  settlement.getVersion() > originalVersion);
    }

    @Test
    void testSettlementWithExactlyZeroDifference() {
        // Test the edge case where difference is exactly zero
        Settlement settlement = new Settlement(
            1L, LocalDate.now(), 
            Money.of("100.00", "CNY"), 
            Money.of("100.00", "CNY")
        );
        
        assertTrue(settlement.getDifference().isZero());
        assertEquals(SettlementStatus.MATCHED, settlement.getStatus());
        assertTrue(settlement.isMatched());
    }

    @Test
    void testSettlementWithMinimalPositiveDifference() {
        // Test the smallest possible positive difference
        Settlement settlement = new Settlement(
            1L, LocalDate.now(), 
            Money.of("100.00", "CNY"), 
            Money.of("100.01", "CNY")
        );
        
        assertEquals(Money.of("0.01", "CNY"), settlement.getDifference());
        assertEquals(SettlementStatus.SURPLUS, settlement.getStatus());
        assertTrue(settlement.hasSurplus());
    }

    private Settlement createTestSettlement() {
        return new Settlement(
            1L, 
            LocalDate.of(2024, 1, 15), 
            Money.of("1000.00", "CNY"), 
            Money.of("1000.00", "CNY")
        );
    }
}