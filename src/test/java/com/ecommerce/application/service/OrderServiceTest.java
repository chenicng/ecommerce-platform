package com.ecommerce.application.service;

import com.ecommerce.domain.order.Order;
import com.ecommerce.domain.order.OrderStatus;
import com.ecommerce.domain.Money;
import com.ecommerce.infrastructure.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    void saveOrder_ShouldCallRepositorySave() {
        // Arrange
        Order order = new Order("ORD-001", 1L, 2L);

        // Act
        orderService.saveOrder(order);

        // Assert
        verify(orderRepository).save(order);
    }

    @Test
    void getOrderByNumber_WithValidOrderNumber_ShouldReturnOrder() {
        // Arrange
        String orderNumber = "ORD-001";
        Order expectedOrder = new Order(orderNumber, 1L, 2L);
        when(orderRepository.findByOrderNumber(orderNumber)).thenReturn(Optional.of(expectedOrder));

        // Act
        Order result = orderService.getOrderByNumber(orderNumber);

        // Assert
        assertEquals(expectedOrder, result);
        verify(orderRepository).findByOrderNumber(orderNumber);
    }

    @Test
    void getOrderByNumber_WithInvalidOrderNumber_ShouldThrowException() {
        // Arrange
        String orderNumber = "INVALID-ORDER";
        when(orderRepository.findByOrderNumber(orderNumber)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            orderService.getOrderByNumber(orderNumber);
        });
        
        assertTrue(exception.getMessage().contains("Order not found"));
        verify(orderRepository).findByOrderNumber(orderNumber);
    }

    @Test
    void getOrderById_WithValidId_ShouldReturnOrder() {
        // Arrange
        Long orderId = 1L;
        Order expectedOrder = new Order("ORD-001", 1L, 2L);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(expectedOrder));

        // Act
        Order result = orderService.getOrderById(orderId);

        // Assert
        assertEquals(expectedOrder, result);
        verify(orderRepository).findById(orderId);
    }

    @Test
    void getOrderById_WithInvalidId_ShouldThrowException() {
        // Arrange
        Long orderId = 999L;
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            orderService.getOrderById(orderId);
        });
        
        assertTrue(exception.getMessage().contains("Order not found"));
        verify(orderRepository).findById(orderId);
    }

    @Test
    void orderExists_WithExistingOrder_ShouldReturnTrue() {
        // Arrange
        String orderNumber = "ORD-001";
        when(orderRepository.existsByOrderNumber(orderNumber)).thenReturn(true);

        // Act
        boolean result = orderService.orderExists(orderNumber);

        // Assert
        assertTrue(result);
        verify(orderRepository).existsByOrderNumber(orderNumber);
    }

    @Test
    void orderExists_WithNonExistingOrder_ShouldReturnFalse() {
        // Arrange
        String orderNumber = "INVALID-ORDER";
        when(orderRepository.existsByOrderNumber(orderNumber)).thenReturn(false);

        // Act
        boolean result = orderService.orderExists(orderNumber);

        // Assert
        assertFalse(result);
        verify(orderRepository).existsByOrderNumber(orderNumber);
    }
} 