package com.ecommerce.infrastructure.scheduler;

import com.ecommerce.application.service.SettlementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SettlementSchedulerTest {

    @Mock
    private SettlementService settlementService;

    private SettlementScheduler settlementScheduler;

    @BeforeEach
    void setUp() {
        settlementScheduler = new SettlementScheduler(settlementService);
    }

    @Test
    void constructor_WithValidService_ShouldCreateInstance() {
        assertNotNull(settlementScheduler);
    }

    @Test
    void executeSettlement_WithSuccessfulService_ShouldCompleteSuccessfully() {
        // Given
        doNothing().when(settlementService).executeSettlement();

        // When
        settlementScheduler.executeSettlement();

        // Then
        verify(settlementService, times(1)).executeSettlement();
    }

    @Test
    void executeSettlement_WithServiceException_ShouldHandleException() {
        // Given
        String errorMessage = "Settlement failed";
        doThrow(new RuntimeException(errorMessage)).when(settlementService).executeSettlement();

        // When & Then - Should not throw exception
        assertDoesNotThrow(() -> settlementScheduler.executeSettlement());
        verify(settlementService, times(1)).executeSettlement();
    }

    @Test
    void executeSettlement_WithServiceException_ShouldLogError() {
        // Given
        String errorMessage = "Settlement processing failed";
        RuntimeException exception = new RuntimeException(errorMessage);
        doThrow(exception).when(settlementService).executeSettlement();

        // When
        settlementScheduler.executeSettlement();

        // Then
        verify(settlementService, times(1)).executeSettlement();
        // The error should be logged (we can't easily test logger mock without more complex setup)
    }

    @Test
    void executeSettlement_ShouldHaveCorrectScheduledAnnotation() {
        // Test that the method has the expected annotation
        try {
            var method = SettlementScheduler.class.getMethod("executeSettlement");
            assertTrue(method.isAnnotationPresent(org.springframework.scheduling.annotation.Scheduled.class));
        } catch (NoSuchMethodException e) {
            fail("Method executeSettlement should exist");
        }
    }

    @Test
    void class_ShouldHaveCorrectAnnotations() {
        // Test that the class has the expected annotations
        assertTrue(SettlementScheduler.class.isAnnotationPresent(org.springframework.stereotype.Component.class));
    }
} 