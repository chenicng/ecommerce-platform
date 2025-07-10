package com.ecommerce.application.service;

import com.ecommerce.domain.order.Order;
import com.ecommerce.domain.order.OrderStatus;
import com.ecommerce.infrastructure.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Order Service
 * Manages order-related business operations
 */
@Service
@Transactional
public class OrderService {
    
    private final OrderRepository orderRepository;
    
    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }
    
    /**
     * Save order
     */
    public void saveOrder(Order order) {
        orderRepository.save(order);
    }
    
    /**
     * Get order by number
     */
    @Transactional(readOnly = true)
    public Order getOrderByNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new RuntimeException("Order not found with number: " + orderNumber));
    }
    
    /**
     * Get order by ID
     */
    @Transactional(readOnly = true)
    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));
    }
    
    /**
     * Check if order exists
     */
    @Transactional(readOnly = true)
    public boolean orderExists(String orderNumber) {
        return orderRepository.existsByOrderNumber(orderNumber);
    }
    
    /**
     * Get orders by merchant ID and date range
     */
    @Transactional(readOnly = true)
    public List<Order> getOrdersByMerchantAndDateRange(Long merchantId, LocalDateTime start, LocalDateTime end) {
        return orderRepository.findByMerchantIdAndOrderTimeBetween(merchantId, start, end);
    }
    
    /**
     * Get completed orders by merchant ID and date range
     * Used for settlement calculation
     */
    @Transactional(readOnly = true)
    public List<Order> getCompletedOrdersByMerchantAndDateRange(Long merchantId, LocalDateTime start, LocalDateTime end) {
        return orderRepository.findByMerchantIdAndStatusAndOrderTimeBetween(merchantId, OrderStatus.COMPLETED, start, end);
    }
} 