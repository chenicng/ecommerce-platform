package com.ecommerce.application.service;

import com.ecommerce.domain.merchant.Merchant;
import com.ecommerce.domain.merchant.DuplicateMerchantException;
import com.ecommerce.domain.Money;
import com.ecommerce.infrastructure.repository.MerchantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MerchantServiceTest {

    @Mock
    private MerchantRepository merchantRepository;

    @InjectMocks
    private MerchantService merchantService;

    private Merchant testMerchant;

    @BeforeEach
    void setUp() {
        testMerchant = new Merchant("Test Store", "BL123456", "test@store.com", "1234567890");
    }

    @Test
    void createMerchant_Success() {
        // Given
        when(merchantRepository.existsByBusinessLicense("BL123456")).thenReturn(false);
        when(merchantRepository.existsByContactEmail("test@store.com")).thenReturn(false);
        when(merchantRepository.save(any(Merchant.class))).thenReturn(testMerchant);

        // When
        Merchant result = merchantService.createMerchant("Test Store", "BL123456", "test@store.com", "1234567890");

        // Then
        assertNotNull(result);
        assertEquals("Test Store", result.getMerchantName());
        assertEquals("BL123456", result.getBusinessLicense());
        assertEquals("test@store.com", result.getContactEmail());
        assertEquals("1234567890", result.getContactPhone());
        verify(merchantRepository).existsByBusinessLicense("BL123456");
        verify(merchantRepository).existsByContactEmail("test@store.com");
        verify(merchantRepository).save(any(Merchant.class));
    }

    @Test
    void createMerchant_DuplicateBusinessLicense_ThrowsException() {
        // Given
        when(merchantRepository.existsByBusinessLicense("BL123456")).thenReturn(true);

        // When & Then
        DuplicateMerchantException exception = assertThrows(DuplicateMerchantException.class,
            () -> merchantService.createMerchant("Test Store", "BL123456", "test@store.com", "1234567890"));
        assertEquals("Merchant with business license 'BL123456' already exists", exception.getMessage());
        verify(merchantRepository).existsByBusinessLicense("BL123456");
        verify(merchantRepository, never()).existsByContactEmail(anyString());
        verify(merchantRepository, never()).save(any(Merchant.class));
    }

    @Test
    void createMerchant_DuplicateContactEmail_ThrowsException() {
        // Given
        when(merchantRepository.existsByBusinessLicense("BL123456")).thenReturn(false);
        when(merchantRepository.existsByContactEmail("test@store.com")).thenReturn(true);

        // When & Then
        DuplicateMerchantException exception = assertThrows(DuplicateMerchantException.class,
            () -> merchantService.createMerchant("Test Store", "BL123456", "test@store.com", "1234567890"));
        assertEquals("Merchant with contact email 'test@store.com' already exists", exception.getMessage());
        verify(merchantRepository).existsByBusinessLicense("BL123456");
        verify(merchantRepository).existsByContactEmail("test@store.com");
        verify(merchantRepository, never()).save(any(Merchant.class));
    }

    @Test
    void getMerchantById_Success() {
        // Given
        Long merchantId = 1L;
        when(merchantRepository.findById(merchantId)).thenReturn(Optional.of(testMerchant));

        // When
        Merchant result = merchantService.getMerchantById(merchantId);

        // Then
        assertNotNull(result);
        assertEquals(testMerchant, result);
        verify(merchantRepository).findById(merchantId);
    }

    @Test
    void getMerchantById_MerchantNotFound() {
        // Given
        Long merchantId = 999L;
        when(merchantRepository.findById(merchantId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> merchantService.getMerchantById(merchantId));
        assertEquals("Merchant not found with id: 999", exception.getMessage());
        verify(merchantRepository).findById(merchantId);
    }

    @Test
    void saveMerchant_Success() {
        // Given
        when(merchantRepository.save(testMerchant)).thenReturn(testMerchant);

        // When
        merchantService.saveMerchant(testMerchant);

        // Then
        verify(merchantRepository).save(testMerchant);
    }

    @Test
    void getMerchantBalance_Success() {
        // Given
        Long merchantId = 1L;
        when(merchantRepository.findById(merchantId)).thenReturn(Optional.of(testMerchant));

        // When
        Money result = merchantService.getMerchantBalance(merchantId);

        // Then
        assertNotNull(result);
        assertEquals(testMerchant.getBalance(), result);
        verify(merchantRepository).findById(merchantId);
    }

    @Test
    void getMerchantBalance_MerchantNotFound() {
        // Given
        Long merchantId = 999L;
        when(merchantRepository.findById(merchantId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> merchantService.getMerchantBalance(merchantId));
        assertEquals("Merchant not found with id: 999", exception.getMessage());
        verify(merchantRepository).findById(merchantId);
    }

    @Test
    void getMerchantTotalIncome_Success() {
        // Given
        Long merchantId = 1L;
        when(merchantRepository.findById(merchantId)).thenReturn(Optional.of(testMerchant));

        // When
        Money result = merchantService.getMerchantTotalIncome(merchantId);

        // Then
        assertNotNull(result);
        assertEquals(testMerchant.getTotalIncome(), result);
        verify(merchantRepository).findById(merchantId);
    }

    @Test
    void getMerchantTotalIncome_MerchantNotFound() {
        // Given
        Long merchantId = 999L;
        when(merchantRepository.findById(merchantId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> merchantService.getMerchantTotalIncome(merchantId));
        assertEquals("Merchant not found with id: 999", exception.getMessage());
        verify(merchantRepository).findById(merchantId);
    }

    @Test
    void merchantExists_True() {
        // Given
        Long merchantId = 1L;
        when(merchantRepository.existsById(merchantId)).thenReturn(true);

        // When
        boolean result = merchantService.merchantExists(merchantId);

        // Then
        assertTrue(result);
        verify(merchantRepository).existsById(merchantId);
    }

    @Test
    void merchantExists_False() {
        // Given
        Long merchantId = 999L;
        when(merchantRepository.existsById(merchantId)).thenReturn(false);

        // When
        boolean result = merchantService.merchantExists(merchantId);

        // Then
        assertFalse(result);
        verify(merchantRepository).existsById(merchantId);
    }

    @Test
    void getAllActiveMerchants_Success() {
        // Given
        List<Merchant> activeMerchants = Arrays.asList(testMerchant);
        when(merchantRepository.findAllActive()).thenReturn(activeMerchants);

        // When
        List<Merchant> result = merchantService.getAllActiveMerchants();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testMerchant, result.get(0));
        verify(merchantRepository).findAllActive();
    }
} 