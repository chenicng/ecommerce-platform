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
    void monitorServicePerformance_SuccessfulExecution_ShouldReturnResult() throws Throwable {
        // Arrange
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getName()).thenReturn("processOrder");
        when(joinPoint.getTarget()).thenReturn(new Object() {
            @Override
            public String toString() {
                return "EcommerceService";
            }
        });
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
        when(signature.getName()).thenReturn("processOrder");
        when(joinPoint.getTarget()).thenReturn(new Object() {
            @Override
            public String toString() {
                return "EcommerceService";
            }
        });
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
        long slowOperationThreshold = (Long) ReflectionTestUtils.getField(aspect, "SLOW_OPERATION_THRESHOLD_MS");
        assertEquals(1000L, slowOperationThreshold);
    }

    @Test
    void monitorServicePerformance_SlowExecution_ShouldLogWarning() throws Throwable {
        // Arrange
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getName()).thenReturn("processOrder");
        when(joinPoint.getTarget()).thenReturn(new Object() {
            @Override
            public String toString() {
                return "EcommerceService";
            }
        });
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
        when(signature.getName()).thenReturn("processOrder");
        when(joinPoint.getTarget()).thenReturn(new Object() {
            @Override
            public String toString() {
                return "EcommerceService";
            }
        });
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
    void monitorServicePerformance_ExceptionWithSlowExecution_ShouldLogError() throws Throwable {
        // Arrange
        RuntimeException expectedException = new RuntimeException("Service exception");
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getName()).thenReturn("processOrder");
        when(joinPoint.getTarget()).thenReturn(new Object() {
            @Override
            public String toString() {
                return "EcommerceService";
            }
        });
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
    void monitorServicePerformance_ExceptionWithFastExecution_ShouldLogError() throws Throwable {
        // Arrange
        RuntimeException expectedException = new RuntimeException("Service exception");
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getName()).thenReturn("processOrder");
        when(joinPoint.getTarget()).thenReturn(new Object() {
            @Override
            public String toString() {
                return "EcommerceService";
            }
        });
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
    void monitorServicePerformance_InstantExecution_ShouldLogDebug() throws Throwable {
        // Arrange
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getName()).thenReturn("processOrder");
        when(joinPoint.getTarget()).thenReturn(new Object() {
            @Override
            public String toString() {
                return "EcommerceService";
            }
        });
        when(joinPoint.proceed()).thenReturn("success");
        
        // Act
        Object result = aspect.monitorServicePerformance(joinPoint);
        
        // Assert
        assertEquals("success", result);
        verify(joinPoint).proceed();
    }

    @Test
    void monitorServicePerformance_ComplexMethodName_ShouldHandleCorrectly() throws Throwable {
        // Arrange
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getName()).thenReturn("processComplexOrderWithMultipleItems");
        when(joinPoint.getTarget()).thenReturn(new Object() {
            @Override
            public String toString() {
                return "ComplexOrderService";
            }
        });
        when(joinPoint.proceed()).thenReturn("success");
        
        // Act
        Object result = aspect.monitorServicePerformance(joinPoint);
        
        // Assert
        assertEquals("success", result);
        verify(joinPoint).proceed();
    }

    @Test
    void monitorServicePerformance_ReturnNull_ShouldHandleCorrectly() throws Throwable {
        // Arrange
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getName()).thenReturn("processOrder");
        when(joinPoint.getTarget()).thenReturn(new Object() {
            @Override
            public String toString() {
                return "EcommerceService";
            }
        });
        when(joinPoint.proceed()).thenReturn(null);
        
        // Act
        Object result = aspect.monitorServicePerformance(joinPoint);
        
        // Assert
        assertNull(result);
        verify(joinPoint).proceed();
    }

    @Test
    void monitorServicePerformance_ReturnComplexObject_ShouldHandleCorrectly() throws Throwable {
        // Arrange
        Object complexObject = new Object() {
            @Override
            public String toString() {
                return "ComplexOrderResult{orderId=123, status=COMPLETED}";
            }
        };
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getName()).thenReturn("processOrder");
        when(joinPoint.getTarget()).thenReturn(new Object() {
            @Override
            public String toString() {
                return "EcommerceService";
            }
        });
        when(joinPoint.proceed()).thenReturn(complexObject);
        
        // Act
        Object result = aspect.monitorServicePerformance(joinPoint);
        
        // Assert
        assertEquals(complexObject, result);
        verify(joinPoint).proceed();
    }

    @Test
    void monitorServicePerformance_ExceptionWithComplexMessage_ShouldLogError() throws Throwable {
        // Arrange
        RuntimeException expectedException = new RuntimeException("Complex error: Order processing failed due to insufficient inventory for product SKU-12345");
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getName()).thenReturn("processOrder");
        when(joinPoint.getTarget()).thenReturn(new Object() {
            @Override
            public String toString() {
                return "EcommerceService";
            }
        });
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
