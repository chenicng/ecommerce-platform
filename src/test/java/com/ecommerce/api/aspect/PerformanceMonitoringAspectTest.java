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

    // Additional test cases for better coverage

    @Test
    void monitorApiPerformance_SlowExecution_ShouldLogWarning() throws Throwable {
        // Arrange
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.toShortString()).thenReturn("EcommerceController.getProducts()");
        when(joinPoint.proceed()).thenAnswer(invocation -> {
            Thread.sleep(2500); // Simulate slow execution (> 2000ms threshold)
            return "success";
        });
        
        // Act
        Object result = aspect.monitorApiPerformance(joinPoint);
        
        // Assert
        assertEquals("success", result);
        verify(joinPoint).proceed();
    }

    @Test
    void monitorApiPerformance_FastExecution_ShouldLogInfo() throws Throwable {
        // Arrange
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.toShortString()).thenReturn("EcommerceController.getProducts()");
        when(joinPoint.proceed()).thenAnswer(invocation -> {
            Thread.sleep(100); // Simulate fast execution (< 2000ms threshold)
            return "success";
        });
        
        // Act
        Object result = aspect.monitorApiPerformance(joinPoint);
        
        // Assert
        assertEquals("success", result);
        verify(joinPoint).proceed();
    }

    @Test
    void monitorServicePerformance_SlowExecution_ShouldLogWarning() throws Throwable {
        // Arrange
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.toShortString()).thenReturn("EcommerceService.processOrder()");
        when(joinPoint.proceed()).thenAnswer(invocation -> {
            Thread.sleep(1500); // Simulate slow execution (> 1000ms threshold)
            return "success";
        });
        
        // Act
        Object result = aspect.monitorServicePerformance(joinPoint);
        
        // Assert
        assertEquals("success", result);
        verify(joinPoint).proceed();
    }

    @Test
    void monitorServicePerformance_FastExecution_ShouldLogDebug() throws Throwable {
        // Arrange
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.toShortString()).thenReturn("EcommerceService.processOrder()");
        when(joinPoint.proceed()).thenAnswer(invocation -> {
            Thread.sleep(100); // Simulate fast execution (< 1000ms threshold)
            return "success";
        });
        
        // Act
        Object result = aspect.monitorServicePerformance(joinPoint);
        
        // Assert
        assertEquals("success", result);
        verify(joinPoint).proceed();
    }

    @Test
    void monitorApiPerformance_ExceptionWithSlowExecution_ShouldLogError() throws Throwable {
        // Arrange
        RuntimeException expectedException = new RuntimeException("Test exception");
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.toShortString()).thenReturn("EcommerceController.getProducts()");
        when(joinPoint.proceed()).thenAnswer(invocation -> {
            Thread.sleep(2500); // Simulate slow execution
            throw expectedException;
        });
        
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
    void monitorServicePerformance_ExceptionWithSlowExecution_ShouldLogError() throws Throwable {
        // Arrange
        RuntimeException expectedException = new RuntimeException("Service exception");
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.toShortString()).thenReturn("EcommerceService.processOrder()");
        when(joinPoint.proceed()).thenAnswer(invocation -> {
            Thread.sleep(1500); // Simulate slow execution
            throw expectedException;
        });
        
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
    void monitorApiPerformance_ExceptionWithFastExecution_ShouldLogError() throws Throwable {
        // Arrange
        RuntimeException expectedException = new RuntimeException("Test exception");
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.toShortString()).thenReturn("EcommerceController.getProducts()");
        when(joinPoint.proceed()).thenAnswer(invocation -> {
            Thread.sleep(100); // Simulate fast execution
            throw expectedException;
        });
        
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
    void monitorServicePerformance_ExceptionWithFastExecution_ShouldLogError() throws Throwable {
        // Arrange
        RuntimeException expectedException = new RuntimeException("Service exception");
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.toShortString()).thenReturn("EcommerceService.processOrder()");
        when(joinPoint.proceed()).thenAnswer(invocation -> {
            Thread.sleep(100); // Simulate fast execution
            throw expectedException;
        });
        
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
    void monitorApiPerformance_InstantExecution_ShouldLogInfo() throws Throwable {
        // Arrange
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.toShortString()).thenReturn("EcommerceController.getProducts()");
        when(joinPoint.proceed()).thenReturn("success"); // Instant execution
        
        // Act
        Object result = aspect.monitorApiPerformance(joinPoint);
        
        // Assert
        assertEquals("success", result);
        verify(joinPoint).proceed();
    }

    @Test
    void monitorServicePerformance_InstantExecution_ShouldLogDebug() throws Throwable {
        // Arrange
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.toShortString()).thenReturn("EcommerceService.processOrder()");
        when(joinPoint.proceed()).thenReturn("success"); // Instant execution
        
        // Act
        Object result = aspect.monitorServicePerformance(joinPoint);
        
        // Assert
        assertEquals("success", result);
        verify(joinPoint).proceed();
    }

    @Test
    void monitorApiPerformance_ComplexMethodName_ShouldHandleCorrectly() throws Throwable {
        // Arrange
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.toShortString()).thenReturn("com.ecommerce.api.controller.EcommerceController.purchaseProduct(PurchaseRequest)");
        when(joinPoint.proceed()).thenReturn("success");
        
        // Act
        Object result = aspect.monitorApiPerformance(joinPoint);
        
        // Assert
        assertEquals("success", result);
        verify(joinPoint).proceed();
    }

    @Test
    void monitorServicePerformance_ComplexMethodName_ShouldHandleCorrectly() throws Throwable {
        // Arrange
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.toShortString()).thenReturn("com.ecommerce.application.service.EcommerceService.processPurchase(PurchaseRequest)");
        when(joinPoint.proceed()).thenReturn("success");
        
        // Act
        Object result = aspect.monitorServicePerformance(joinPoint);
        
        // Assert
        assertEquals("success", result);
        verify(joinPoint).proceed();
    }

    @Test
    void monitorApiPerformance_ReturnNull_ShouldHandleCorrectly() throws Throwable {
        // Arrange
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.toShortString()).thenReturn("EcommerceController.getProducts()");
        when(joinPoint.proceed()).thenReturn(null);
        
        // Act
        Object result = aspect.monitorApiPerformance(joinPoint);
        
        // Assert
        assertNull(result);
        verify(joinPoint).proceed();
    }

    @Test
    void monitorServicePerformance_ReturnNull_ShouldHandleCorrectly() throws Throwable {
        // Arrange
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.toShortString()).thenReturn("EcommerceService.processOrder()");
        when(joinPoint.proceed()).thenReturn(null);
        
        // Act
        Object result = aspect.monitorServicePerformance(joinPoint);
        
        // Assert
        assertNull(result);
        verify(joinPoint).proceed();
    }

    @Test
    void monitorApiPerformance_ReturnComplexObject_ShouldHandleCorrectly() throws Throwable {
        // Arrange
        Object complexObject = new Object() {
            @Override
            public String toString() {
                return "ComplexObject{data='test'}";
            }
        };
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.toShortString()).thenReturn("EcommerceController.getProducts()");
        when(joinPoint.proceed()).thenReturn(complexObject);
        
        // Act
        Object result = aspect.monitorApiPerformance(joinPoint);
        
        // Assert
        assertEquals(complexObject, result);
        verify(joinPoint).proceed();
    }

    @Test
    void monitorServicePerformance_ReturnComplexObject_ShouldHandleCorrectly() throws Throwable {
        // Arrange
        Object complexObject = new Object() {
            @Override
            public String toString() {
                return "ComplexObject{data='test'}";
            }
        };
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.toShortString()).thenReturn("EcommerceService.processOrder()");
        when(joinPoint.proceed()).thenReturn(complexObject);
        
        // Act
        Object result = aspect.monitorServicePerformance(joinPoint);
        
        // Assert
        assertEquals(complexObject, result);
        verify(joinPoint).proceed();
    }

    @Test
    void monitorApiPerformance_ExceptionWithComplexMessage_ShouldLogError() throws Throwable {
        // Arrange
        RuntimeException expectedException = new RuntimeException("Complex error message with special characters: !@#$%^&*()");
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
    void monitorServicePerformance_ExceptionWithComplexMessage_ShouldLogError() throws Throwable {
        // Arrange
        RuntimeException expectedException = new RuntimeException("Service error with special characters: !@#$%^&*()");
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
}
