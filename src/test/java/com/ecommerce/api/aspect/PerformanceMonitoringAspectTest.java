package com.ecommerce.api.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test class for PerformanceMonitoringAspect
 */
@ExtendWith(MockitoExtension.class)
class PerformanceMonitoringAspectTest {

    private PerformanceMonitoringAspect aspect;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private Signature signature;

    @BeforeEach
    void setUp() {
        aspect = new PerformanceMonitoringAspect();
    }

    @Test
    void monitorApiPerformance_SuccessfulExecution_ShouldReturnResult() throws Throwable {
        // Arrange
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.toShortString()).thenReturn("EcommerceController.getProducts()");
        when(joinPoint.proceed()).thenReturn("success");
        
        // Act
        Object result = aspect.monitorApiPerformance(joinPoint);
        
        // Assert
        assertEquals("success", result);
        verify(joinPoint).proceed();
    }

    @Test
    void monitorApiPerformance_Exception_ShouldRethrow() throws Throwable {
        // Arrange
        RuntimeException expectedException = new RuntimeException("Test exception");
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.toShortString()).thenReturn("EcommerceController.getProducts()");
        when(joinPoint.proceed()).thenThrow(expectedException);
        
        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            try {
                aspect.monitorApiPerformance(joinPoint);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        });
        
        verify(joinPoint).proceed();
    }

    @Test
    void monitorServicePerformance_SuccessfulExecution_ShouldReturnResult() throws Throwable {
        // Arrange
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.toShortString()).thenReturn("EcommerceService.processOrder()");
        when(joinPoint.proceed()).thenReturn("success");
        
        // Act
        Object result = aspect.monitorServicePerformance(joinPoint);
        
        // Assert
        assertEquals("success", result);
        verify(joinPoint).proceed();
    }

    @Test
    void monitorServicePerformance_Exception_ShouldRethrow() throws Throwable {
        // Arrange
        RuntimeException expectedException = new RuntimeException("Service exception");
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.toShortString()).thenReturn("EcommerceService.processOrder()");
        when(joinPoint.proceed()).thenThrow(expectedException);
        
        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            try {
                aspect.monitorServicePerformance(joinPoint);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        });
        
        verify(joinPoint).proceed();
    }

    @Test
    void aspect_ShouldHaveCorrectThresholds() {
        // Test that the aspect has the correct threshold values
        long slowApiThreshold = (Long) ReflectionTestUtils.getField(aspect, "SLOW_API_THRESHOLD");
        assertEquals(2000L, slowApiThreshold);
    }
}
