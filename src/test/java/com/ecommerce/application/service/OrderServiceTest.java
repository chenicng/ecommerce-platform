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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    private Order testOrder;

    @BeforeEach
    void setUp() {
        testOrder = new Order("ORD123", 1L, 1L);
    }

    @Test
    void saveOrder_Success() {
        // Given
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // When
        orderService.saveOrder(testOrder);

        // Then
        verify(orderRepository).save(testOrder);
    }

    @Test
    void getOrderByNumber_Success() {
        // Given
        String orderNumber = "ORD123";
        when(orderRepository.findByOrderNumber(orderNumber)).thenReturn(Optional.of(testOrder));

        // When
        Order result = orderService.getOrderByNumber(orderNumber);

        // Then
        assertNotNull(result);
        assertEquals(testOrder, result);
        verify(orderRepository).findByOrderNumber(orderNumber);
    }

    @Test
    void getOrderByNumber_OrderNotFound() {
        // Given
        String orderNumber = "ORD999";
        when(orderRepository.findByOrderNumber(orderNumber)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> orderService.getOrderByNumber(orderNumber));
        assertEquals("Order not found with number: ORD999", exception.getMessage());
        verify(orderRepository).findByOrderNumber(orderNumber);
    }

    @Test
    void getOrderById_Success() {
        // Given
        Long orderId = 1L;
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));

        // When
        Order result = orderService.getOrderById(orderId);

        // Then
        assertNotNull(result);
        assertEquals(testOrder, result);
        verify(orderRepository).findById(orderId);
    }

    @Test
    void getOrderById_OrderNotFound() {
        // Given
        Long orderId = 999L;
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> orderService.getOrderById(orderId));
        assertEquals("Order not found with id: 999", exception.getMessage());
        verify(orderRepository).findById(orderId);
    }

    @Test
    void orderExists_True() {
        // Given
        String orderNumber = "ORD123";
        when(orderRepository.existsByOrderNumber(orderNumber)).thenReturn(true);

        // When
        boolean result = orderService.orderExists(orderNumber);

        // Then
        assertTrue(result);
        verify(orderRepository).existsByOrderNumber(orderNumber);
    }

    @Test
    void orderExists_False() {
        // Given
        String orderNumber = "ORD999";
        when(orderRepository.existsByOrderNumber(orderNumber)).thenReturn(false);

        // When
        boolean result = orderService.orderExists(orderNumber);

        // Then
        assertFalse(result);
        verify(orderRepository).existsByOrderNumber(orderNumber);
    }
} 