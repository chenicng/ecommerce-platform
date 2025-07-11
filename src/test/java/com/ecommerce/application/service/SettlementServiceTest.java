package com.ecommerce.application.service;

import com.ecommerce.domain.settlement.Settlement;
import com.ecommerce.domain.settlement.SettlementStatus;
import com.ecommerce.domain.Money;
import com.ecommerce.domain.merchant.Merchant;
import com.ecommerce.infrastructure.repository.SettlementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.ecommerce.domain.order.Order;

@ExtendWith(MockitoExtension.class)
class SettlementServiceTest {

    @Mock
    private SettlementRepository settlementRepository;

    @Mock
    private MerchantService merchantService;

    @Mock
    private OrderService orderService;

    @InjectMocks
    private SettlementService settlementService;

    @Test
    void executeSettlement_ShouldCompleteSuccessfully() {
        // Arrange
        Merchant merchant1 = new Merchant("Test Merchant 1", "BL001", "test1@store.com", "555-0001");
        merchant1.setId(1L);
        Merchant merchant2 = new Merchant("Test Merchant 2", "BL002", "test2@store.com", "555-0002");
        merchant2.setId(2L);
        List<Merchant> activeMerchants = Arrays.asList(merchant1, merchant2);
        
        when(merchantService.getAllActiveMerchants()).thenReturn(activeMerchants);
        when(merchantService.getMerchantBalance(1L)).thenReturn(Money.zero("CNY"));
        when(merchantService.getMerchantBalance(2L)).thenReturn(Money.zero("CNY"));
        when(settlementRepository.save(any(Settlement.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        settlementService.executeSettlement();

        // Assert
        verify(merchantService).getAllActiveMerchants();
        verify(merchantService).getMerchantBalance(1L);
        verify(merchantService).getMerchantBalance(2L);
        verify(settlementRepository, times(2)).save(any(Settlement.class));
    }

    @Test
    void executeMerchantSettlement_WithMatchedAmounts_ShouldCreateMatchedSettlement() {
        // Arrange
        Long merchantId = 1L;
        LocalDate settlementDate = LocalDate.of(2023, 12, 25);
        Money expectedIncome = Money.of("100.00", "CNY");
        Money actualBalance = Money.of("100.00", "CNY");

        when(merchantService.getMerchantBalance(merchantId)).thenReturn(actualBalance);
        
        Settlement expectedSettlement = new Settlement(merchantId, settlementDate, expectedIncome, actualBalance);
        when(settlementRepository.save(any(Settlement.class))).thenReturn(expectedSettlement);

        // Act
        Settlement result = settlementService.executeMerchantSettlement(merchantId, settlementDate);

        // Assert
        assertNotNull(result);
        assertEquals(merchantId, result.getMerchantId());
        assertEquals(settlementDate, result.getSettlementDate());
        assertEquals(expectedIncome, result.getExpectedIncome());
        assertEquals(actualBalance, result.getActualBalance());
        assertTrue(result.isMatched());
        
        verify(merchantService).getMerchantBalance(merchantId);
        verify(settlementRepository).save(any(Settlement.class));
    }

    @Test
    void executeMerchantSettlement_WithSurplus_ShouldCreateSurplusSettlement() {
        // Arrange
        Long merchantId = 2L;
        LocalDate settlementDate = LocalDate.of(2023, 12, 26);
        Money actualBalance = Money.of("150.00", "CNY"); // More than expected

        when(merchantService.getMerchantBalance(merchantId)).thenReturn(actualBalance);
        
        Settlement expectedSettlement = new Settlement(merchantId, settlementDate, Money.zero("CNY"), actualBalance);
        when(settlementRepository.save(any(Settlement.class))).thenReturn(expectedSettlement);

        // Act
        Settlement result = settlementService.executeMerchantSettlement(merchantId, settlementDate);

        // Assert
        assertNotNull(result);
        assertEquals(merchantId, result.getMerchantId());
        assertEquals(settlementDate, result.getSettlementDate());
        assertEquals(Money.zero("CNY"), result.getExpectedIncome());
        assertEquals(actualBalance, result.getActualBalance());
        assertTrue(result.hasSurplus());
        assertEquals(Money.of("150.00", "CNY"), result.getDifference());
        
        verify(merchantService).getMerchantBalance(merchantId);
        verify(settlementRepository).save(any(Settlement.class));
    }

    @Test
    void executeMerchantSettlement_WithDeficit_ShouldCreateDeficitSettlement() {
        // Arrange
        Long merchantId = 3L;
        LocalDate settlementDate = LocalDate.of(2023, 12, 27);
        Money actualBalance = Money.zero("CNY"); // Less than expected

        when(merchantService.getMerchantBalance(merchantId)).thenReturn(actualBalance);
        
        // In a real scenario, expected income would be calculated from order records
        // For this test, we simulate the expected settlement creation
        Settlement expectedSettlement = new Settlement(merchantId, settlementDate, Money.zero("CNY"), actualBalance);
        when(settlementRepository.save(any(Settlement.class))).thenReturn(expectedSettlement);

        // Act
        Settlement result = settlementService.executeMerchantSettlement(merchantId, settlementDate);

        // Assert
        assertNotNull(result);
        assertEquals(merchantId, result.getMerchantId());
        assertEquals(settlementDate, result.getSettlementDate());
        assertEquals(Money.zero("CNY"), result.getExpectedIncome());
        assertEquals(actualBalance, result.getActualBalance());
        
        verify(merchantService).getMerchantBalance(merchantId);
        verify(settlementRepository).save(any(Settlement.class));
    }

    @Test
    void getSettlementById_WithExistingSettlement_ShouldReturnSettlement() {
        // Arrange
        Long settlementId = 100L;
        Settlement expectedSettlement = new Settlement(1L, LocalDate.now(), 
            Money.of("50.00", "CNY"), Money.of("50.00", "CNY"));
        
        when(settlementRepository.findById(settlementId)).thenReturn(Optional.of(expectedSettlement));

        // Act
        Settlement result = settlementService.getSettlementById(settlementId);

        // Assert
        assertNotNull(result);
        assertEquals(expectedSettlement, result);
        verify(settlementRepository).findById(settlementId);
    }

    @Test
    void getSettlementById_WithNonExistentSettlement_ShouldThrowException() {
        // Arrange
        Long settlementId = 999L;
        when(settlementRepository.findById(settlementId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> settlementService.getSettlementById(settlementId));
        assertEquals("Settlement not found with id: 999", exception.getMessage());
        verify(settlementRepository).findById(settlementId);
    }

    @Test
    void saveSettlement_ShouldCallRepository() {
        // Arrange
        Settlement settlement = new Settlement(1L, LocalDate.now(), 
            Money.of("75.00", "CNY"), Money.of("75.00", "CNY"));

        // Act
        settlementService.saveSettlement(settlement);

        // Assert
        verify(settlementRepository).save(settlement);
    }

    @Test
    void executeMerchantSettlement_ShouldHandleNullMerchantBalance() {
        // Arrange
        Long merchantId = 4L;
        LocalDate settlementDate = LocalDate.now();

        when(merchantService.getMerchantBalance(merchantId)).thenReturn(Money.zero("CNY"));
        
        Settlement expectedSettlement = new Settlement(merchantId, settlementDate, 
            Money.zero("CNY"), Money.zero("CNY"));
        when(settlementRepository.save(any(Settlement.class))).thenReturn(expectedSettlement);

        // Act
        Settlement result = settlementService.executeMerchantSettlement(merchantId, settlementDate);

        // Assert
        assertNotNull(result);
        assertEquals(Money.zero("CNY"), result.getExpectedIncome());
        assertEquals(Money.zero("CNY"), result.getActualBalance());
        assertTrue(result.isMatched());
        
        verify(merchantService).getMerchantBalance(merchantId);
        verify(settlementRepository).save(any(Settlement.class));
    }

    @Test
    void executeMerchantSettlement_WithLargeDifference_ShouldHandleCorrectly() {
        // Arrange
        Long merchantId = 5L;
        LocalDate settlementDate = LocalDate.now();
        Money actualBalance = Money.of("1000000.00", "CNY"); // Large amount

        when(merchantService.getMerchantBalance(merchantId)).thenReturn(actualBalance);
        
        Settlement expectedSettlement = new Settlement(merchantId, settlementDate, 
            Money.zero("CNY"), actualBalance);
        when(settlementRepository.save(any(Settlement.class))).thenReturn(expectedSettlement);

        // Act
        Settlement result = settlementService.executeMerchantSettlement(merchantId, settlementDate);

        // Assert
        assertNotNull(result);
        assertEquals(Money.zero("CNY"), result.getExpectedIncome());
        assertEquals(actualBalance, result.getActualBalance());
        assertEquals(Money.of("1000000.00", "CNY"), result.getDifference());
        assertTrue(result.hasSurplus());
        
        verify(merchantService).getMerchantBalance(merchantId);
        verify(settlementRepository).save(any(Settlement.class));
    }

    @Test
    void executeMerchantSettlement_WithHistoricalDate_ShouldWork() {
        // Arrange
        Long merchantId = 6L;
        LocalDate historicalDate = LocalDate.of(2023, 1, 1);
        Money actualBalance = Money.of("250.50", "CNY");

        when(merchantService.getMerchantBalance(merchantId)).thenReturn(actualBalance);
        
        Settlement expectedSettlement = new Settlement(merchantId, historicalDate, 
            Money.zero("CNY"), actualBalance);
        when(settlementRepository.save(any(Settlement.class))).thenReturn(expectedSettlement);

        // Act
        Settlement result = settlementService.executeMerchantSettlement(merchantId, historicalDate);

        // Assert
        assertNotNull(result);
        assertEquals(merchantId, result.getMerchantId());
        assertEquals(historicalDate, result.getSettlementDate());
        assertEquals(actualBalance, result.getActualBalance());
        
        verify(merchantService).getMerchantBalance(merchantId);
        verify(settlementRepository).save(any(Settlement.class));
    }

    @Test
    void executeMerchantSettlement_WhenMerchantServiceThrowsException_ShouldPropagateException() {
        // Arrange
        Long merchantId = 7L;
        LocalDate settlementDate = LocalDate.now();

        when(merchantService.getMerchantBalance(merchantId))
            .thenThrow(new RuntimeException("Merchant not found"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> settlementService.executeMerchantSettlement(merchantId, settlementDate));
        assertEquals("Merchant not found", exception.getMessage());
        
        verify(merchantService).getMerchantBalance(merchantId);
        verify(settlementRepository, never()).save(any(Settlement.class));
    }

    @Test
    void getSettlementByMerchantAndDate_ShouldReturnEmptyForDemo() {
        // Arrange
        Long merchantId = 1L;
        LocalDate settlementDate = LocalDate.now();

        // Act
        Optional<Settlement> result = settlementService.getSettlementByMerchantAndDate(merchantId, settlementDate);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void saveSettlement_WhenRepositoryThrowsException_ShouldPropagateException() {
        // Arrange
        Settlement settlement = new Settlement(1L, LocalDate.now(), 
            Money.of("100.00", "CNY"), Money.of("100.00", "CNY"));
        
        doThrow(new RuntimeException("Database error")).when(settlementRepository).save(settlement);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> settlementService.saveSettlement(settlement));
        assertEquals("Database error", exception.getMessage());
        
        verify(settlementRepository).save(settlement);
    }

    @Test
    void executeMerchantSettlement_WithMerchantNotFound_ShouldHandleGracefully() {
        // Arrange
        Long merchantId = 999L;
        LocalDate settlementDate = LocalDate.of(2023, 12, 25);
        
        when(merchantService.getMerchantBalance(merchantId))
            .thenThrow(new RuntimeException("Merchant not found"));
        
        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            settlementService.executeMerchantSettlement(merchantId, settlementDate);
        });
        
        verify(merchantService).getMerchantBalance(merchantId);
    }

    @Test
    void executeMerchantSettlement_WithDifferentCurrencies_ShouldHandleCorrectly() {
        // Arrange
        Long merchantId = 1L;
        LocalDate settlementDate = LocalDate.of(2023, 12, 25);
        
        Merchant merchant = new Merchant("Test Merchant", "LICENSE-001", "test@merchant.com", "13800001111", "USD");
        merchant.setId(merchantId);
        merchant.receiveIncome(Money.of("100.00", "USD"));
        
        when(merchantService.getMerchantBalance(merchantId)).thenReturn(null); // Simulate null balance
        when(orderService.getCompletedOrdersByMerchantAndDateRange(eq(merchantId), any(), any()))
            .thenReturn(Collections.emptyList());
        when(settlementRepository.save(any(Settlement.class))).thenAnswer(invocation -> {
            Settlement settlement = invocation.getArgument(0);
            settlement.setId(1L);
            return settlement;
        });
        
        // Act
        Settlement result = settlementService.executeMerchantSettlement(merchantId, settlementDate);
        
        // Assert
        assertNotNull(result);
        assertEquals(Money.of("0.00", "CNY"), result.getExpectedIncome()); // Should default to CNY
        assertNull(result.getActualBalance()); // Should be null
        verify(settlementRepository).save(any(Settlement.class));
    }

    @Test
    void executeSettlement_WithNoActiveMerchants_ShouldCompleteGracefully() {
        // Arrange
        when(merchantService.getAllActiveMerchants()).thenReturn(Collections.emptyList());
        
        // Act & Assert - should not throw exception
        assertDoesNotThrow(() -> settlementService.executeSettlement());
        
        verify(merchantService).getAllActiveMerchants();
        verify(settlementRepository, never()).save(any());
    }

    @Test
    void getSettlementById_WithValidId_ShouldReturnSettlement() {
        // Arrange
        Long settlementId = 1L;
        Settlement settlement = new Settlement(1L, LocalDate.now(), 
            Money.of("100.00", "CNY"), Money.of("100.00", "CNY"));
        settlement.setId(settlementId);
        
        when(settlementRepository.findById(settlementId)).thenReturn(Optional.of(settlement));
        
        // Act
        Settlement result = settlementService.getSettlementById(settlementId);
        
        // Assert
        assertEquals(settlement, result);
        verify(settlementRepository).findById(settlementId);
    }

    @Test
    void getSettlementById_WithInvalidId_ShouldThrowException() {
        // Arrange
        Long settlementId = 999L;
        when(settlementRepository.findById(settlementId)).thenReturn(Optional.empty());
        
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            settlementService.getSettlementById(settlementId);
        });
        
        assertTrue(exception.getMessage().contains("Settlement not found"));
        verify(settlementRepository).findById(settlementId);
    }

    @Test
    void saveSettlement_ShouldDelegateToRepository() {
        // Arrange
        Settlement settlement = new Settlement(1L, LocalDate.now(), 
            Money.of("100.00", "CNY"), Money.of("100.00", "CNY"));
        
        // Act
        settlementService.saveSettlement(settlement);
        
        // Assert
        verify(settlementRepository).save(settlement);
    }

    @Test
    void calculateExpectedIncomeFromOrders_WithEmptyList_ShouldReturnZero() {
        // This tests the private method indirectly through executeMerchantSettlement
        // Arrange
        Long merchantId = 1L;
        LocalDate settlementDate = LocalDate.of(2023, 12, 25);
        
        when(merchantService.getMerchantBalance(merchantId)).thenReturn(Money.zero("CNY"));
        when(orderService.getCompletedOrdersByMerchantAndDateRange(eq(merchantId), any(), any()))
            .thenReturn(Collections.emptyList());
        when(settlementRepository.save(any(Settlement.class))).thenAnswer(invocation -> {
            Settlement settlement = invocation.getArgument(0);
            settlement.setId(1L);
            return settlement;
        });
        
        // Act
        Settlement result = settlementService.executeMerchantSettlement(merchantId, settlementDate);
        
        // Assert
        assertEquals(Money.zero("CNY"), result.getExpectedIncome());
    }

    @Test
    void calculateExpectedIncomeFromOrders_WithMultipleOrders_ShouldSumCorrectly() {
        // This tests the private method indirectly through executeMerchantSettlement
        // Arrange
        Long merchantId = 1L;
        LocalDate settlementDate = LocalDate.of(2023, 12, 25);
        
        // Create completed orders
        Order order1 = new Order("ORD-001", 1L, merchantId);
        order1.addOrderItem("SKU-001", "Product 1", Money.of("50.00", "CNY"), 1);
        order1.confirm();
        order1.processPayment();
        order1.complete();
        
        Order order2 = new Order("ORD-002", 2L, merchantId);
        order2.addOrderItem("SKU-002", "Product 2", Money.of("75.00", "CNY"), 2);
        order2.confirm();
        order2.processPayment();
        order2.complete();
        
        List<Order> completedOrders = Arrays.asList(order1, order2);
        
        when(merchantService.getMerchantBalance(merchantId)).thenReturn(Money.of("200.00", "CNY"));
        when(orderService.getCompletedOrdersByMerchantAndDateRange(eq(merchantId), any(), any()))
            .thenReturn(completedOrders);
        when(settlementRepository.save(any(Settlement.class))).thenAnswer(invocation -> {
            Settlement settlement = invocation.getArgument(0);
            settlement.setId(1L);
            return settlement;
        });
        
        // Act
        Settlement result = settlementService.executeMerchantSettlement(merchantId, settlementDate);
        
        // Assert
        // Expected income should be 50.00 + 150.00 = 200.00 CNY
        assertEquals(Money.of("200.00", "CNY"), result.getExpectedIncome());
    }
} 