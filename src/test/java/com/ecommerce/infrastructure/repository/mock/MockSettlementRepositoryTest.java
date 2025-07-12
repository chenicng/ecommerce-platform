package com.ecommerce.infrastructure.repository.mock;

import com.ecommerce.domain.settlement.Settlement;
import com.ecommerce.domain.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class MockSettlementRepositoryTest {

    private MockSettlementRepository repository;

    @BeforeEach
    void setUp() {
        repository = new MockSettlementRepository();
    }

    @Test
    void constructor_ShouldInitializeEmptyStorage() {
        // Verify that storage is empty initially
        assertEquals(0, count());
        assertFalse(repository.findById(1L).isPresent());
    }

    @Test
    void save_WithNewSettlement_ShouldAssignIdAndSave() {
        // Given
        Settlement newSettlement = new Settlement(
            1L, LocalDate.of(2024, 1, 15), 
            Money.of("1000.00", "CNY"), 
            Money.of("1000.00", "CNY")
        );
        
        // When
        Settlement savedSettlement = repository.save(newSettlement);
        
        // Then
        assertNotNull(savedSettlement.getId());
        assertEquals(1L, savedSettlement.getId());
        assertEquals(1L, savedSettlement.getMerchantId());
        assertEquals(1, count());
    }

    @Test
    void save_WithExistingSettlement_ShouldUpdateSettlement() {
        // Given
        Settlement settlement = new Settlement(
            1L, LocalDate.of(2024, 1, 15), 
            Money.of("1000.00", "CNY"), 
            Money.of("1000.00", "CNY")
        );
        Settlement savedSettlement = repository.save(settlement);
        
        // Add notes to the existing settlement
        savedSettlement.addNotes("Updated notes");
        
        // When
        Settlement updatedSettlement = repository.save(savedSettlement);
        
        // Then
        assertEquals(savedSettlement.getId(), updatedSettlement.getId());
        assertEquals("Updated notes", updatedSettlement.getNotes());
        assertEquals(1, count()); // Count should remain the same
    }

    @Test
    void save_WithNullSettlement_ShouldThrowException() {
        // When & Then
        assertThrows(NullPointerException.class, () -> repository.save(null));
    }

    @Test
    void save_WithPresetId_ShouldKeepSameId() {
        // Given
        Settlement settlement = new Settlement(
            1L, LocalDate.of(2024, 1, 15), 
            Money.of("1000.00", "CNY"), 
            Money.of("1000.00", "CNY")
        );
        settlement.setId(100L);
        
        // When
        Settlement savedSettlement = repository.save(settlement);
        
        // Then
        assertEquals(100L, savedSettlement.getId());
        assertEquals(1, count());
    }

    @Test
    void findById_WithExistingId_ShouldReturnSettlement() {
        // Given
        Settlement settlement = new Settlement(
            1L, LocalDate.of(2024, 1, 15), 
            Money.of("1000.00", "CNY"), 
            Money.of("1000.00", "CNY")
        );
        Settlement savedSettlement = repository.save(settlement);
        
        // When
        Optional<Settlement> foundSettlement = repository.findById(savedSettlement.getId());
        
        // Then
        assertTrue(foundSettlement.isPresent());
        assertEquals(savedSettlement.getId(), foundSettlement.get().getId());
        assertEquals(savedSettlement.getMerchantId(), foundSettlement.get().getMerchantId());
    }

    @Test
    void findById_WithNonExistentId_ShouldReturnEmpty() {
        // When
        Optional<Settlement> foundSettlement = repository.findById(999L);
        
        // Then
        assertFalse(foundSettlement.isPresent());
    }

    @Test
    void findById_WithNullId_ShouldReturnEmpty() {
        // When
        Optional<Settlement> foundSettlement = repository.findById(null);
        
        // Then
        assertFalse(foundSettlement.isPresent());
    }

    @Test
    void deleteById_WithExistingId_ShouldRemoveSettlement() {
        // Given
        Settlement settlement = new Settlement(
            1L, LocalDate.of(2024, 1, 15), 
            Money.of("1000.00", "CNY"), 
            Money.of("1000.00", "CNY")
        );
        Settlement savedSettlement = repository.save(settlement);
        assertEquals(1, count());
        
        // When
        repository.deleteById(savedSettlement.getId());
        
        // Then
        assertEquals(0, count());
        assertFalse(repository.findById(savedSettlement.getId()).isPresent());
    }

    @Test
    void deleteById_WithNonExistentId_ShouldNotThrowException() {
        // When & Then
        assertDoesNotThrow(() -> repository.deleteById(999L));
        assertEquals(0, count());
    }

    @Test
    void deleteById_WithNullId_ShouldNotThrowException() {
        // When & Then
        assertDoesNotThrow(() -> repository.deleteById(null));
        assertEquals(0, count());
    }

    @Test
    void idGenerator_ShouldGenerateSequentialIds() {
        // Given
        Settlement settlement1 = new Settlement(
            1L, LocalDate.of(2024, 1, 15), 
            Money.of("1000.00", "CNY"), 
            Money.of("1000.00", "CNY")
        );
        Settlement settlement2 = new Settlement(
            2L, LocalDate.of(2024, 1, 16), 
            Money.of("2000.00", "CNY"), 
            Money.of("2000.00", "CNY")
        );
        
        // When
        Settlement saved1 = repository.save(settlement1);
        Settlement saved2 = repository.save(settlement2);
        
        // Then
        assertEquals(1L, saved1.getId());
        assertEquals(2L, saved2.getId());
    }

    @Test
    void multipleOperations_ShouldWorkCorrectly() {
        // Save multiple settlements
        Settlement settlement1 = repository.save(new Settlement(
            1L, LocalDate.of(2024, 1, 15), 
            Money.of("1000.00", "CNY"), 
            Money.of("1000.00", "CNY")
        ));
        
        Settlement settlement2 = repository.save(new Settlement(
            2L, LocalDate.of(2024, 1, 16), 
            Money.of("2000.00", "CNY"), 
            Money.of("2200.00", "CNY")
        ));
        
        Settlement settlement3 = repository.save(new Settlement(
            3L, LocalDate.of(2024, 1, 17), 
            Money.of("3000.00", "CNY"), 
            Money.of("3000.00", "CNY")
        ));
        
        // Verify all are saved
        assertEquals(3, count());
        
        // Find specific settlements
        assertTrue(repository.findById(settlement1.getId()).isPresent());
        assertTrue(repository.findById(settlement2.getId()).isPresent());
        assertTrue(repository.findById(settlement3.getId()).isPresent());
        
        // Delete one settlement
        repository.deleteById(settlement2.getId());
        assertEquals(2, count());
        assertFalse(repository.findById(settlement2.getId()).isPresent());
        
        // Verify others still exist
        assertTrue(repository.findById(settlement1.getId()).isPresent());
        assertTrue(repository.findById(settlement3.getId()).isPresent());
    }

    @Test
    void save_WithDifferentSettlementTypes_ShouldHandleCorrectly() {
        // Matched settlement
        Settlement matchedSettlement = repository.save(new Settlement(
            1L, LocalDate.of(2024, 1, 15), 
            Money.of("1000.00", "CNY"), 
            Money.of("1000.00", "CNY")
        ));
        assertTrue(matchedSettlement.isMatched());
        
        // Surplus settlement
        Settlement surplusSettlement = repository.save(new Settlement(
            2L, LocalDate.of(2024, 1, 16), 
            Money.of("1000.00", "CNY"), 
            Money.of("1200.00", "CNY")
        ));
        assertTrue(surplusSettlement.hasSurplus());
        
        // Verify both are saved correctly
        assertEquals(2, count());
        
        Optional<Settlement> found1 = repository.findById(matchedSettlement.getId());
        Optional<Settlement> found2 = repository.findById(surplusSettlement.getId());
        
        assertTrue(found1.isPresent());
        assertTrue(found2.isPresent());
        assertTrue(found1.get().isMatched());
        assertTrue(found2.get().hasSurplus());
    }

    @Test
    void save_WithLargeAmounts_ShouldHandleCorrectly() {
        // Given
        Settlement largeSettlement = new Settlement(
            1L, LocalDate.of(2024, 1, 15), 
            Money.of("999999.99", "CNY"), 
            Money.of("1000000.00", "CNY")
        );
        
        // When
        Settlement savedSettlement = repository.save(largeSettlement);
        
        // Then
        assertNotNull(savedSettlement.getId());
        assertEquals(Money.of("999999.99", "CNY"), savedSettlement.getExpectedIncome());
        assertEquals(Money.of("1000000.00", "CNY"), savedSettlement.getActualBalance());
        assertTrue(savedSettlement.hasSurplus());
    }

    @Test
    void save_WithHistoricalDates_ShouldHandleCorrectly() {
        // Given
        Settlement historicalSettlement = new Settlement(
            1L, LocalDate.of(2020, 1, 1), 
            Money.of("500.00", "CNY"), 
            Money.of("500.00", "CNY")
        );
        
        // When
        Settlement savedSettlement = repository.save(historicalSettlement);
        
        // Then
        assertNotNull(savedSettlement.getId());
        assertEquals(LocalDate.of(2020, 1, 1), savedSettlement.getSettlementDate());
        assertTrue(savedSettlement.isMatched());
    }

    @Test
    void save_WithFutureDates_ShouldHandleCorrectly() {
        // Given
        Settlement futureSettlement = new Settlement(
            1L, LocalDate.of(2030, 12, 31), 
            Money.of("1000.00", "CNY"), 
            Money.of("1000.00", "CNY")
        );
        
        // When
        Settlement savedSettlement = repository.save(futureSettlement);
        
        // Then
        assertNotNull(savedSettlement.getId());
        assertEquals(LocalDate.of(2030, 12, 31), savedSettlement.getSettlementDate());
        assertTrue(savedSettlement.isMatched());
    }

    @Test
    void repository_ShouldHaveCorrectAnnotations() {
        // Verify that the repository has the expected annotations
        assertTrue(MockSettlementRepository.class.isAnnotationPresent(org.springframework.stereotype.Repository.class));
        assertTrue(MockSettlementRepository.class.isAnnotationPresent(org.springframework.context.annotation.Profile.class));
        
        // Verify profile annotation value
        org.springframework.context.annotation.Profile profileAnnotation = 
            MockSettlementRepository.class.getAnnotation(org.springframework.context.annotation.Profile.class);
        assertEquals("mock", profileAnnotation.value()[0]);
    }

    // Helper method to count settlements in repository
    private int count() {
        int count = 0;
        for (long i = 1; i <= 1000; i++) {
            if (repository.findById(i).isPresent()) {
                count++;
            }
        }
        return count;
    }
} 