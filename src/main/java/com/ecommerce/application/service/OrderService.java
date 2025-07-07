package com.ecommerce.application.service;

import com.ecommerce.domain.order.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Order Service
 * Manages order-related business operations
 */
@Service
@Transactional
public class OrderService {
    
    // Simple in-memory storage, production should use database
    private final Map<Long, Order> orderStorage = new HashMap<>();
    private final Map<String, Order> orderNumberIndex = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);
    
    /**
     * Save order
     */
    public void saveOrder(Order order) {
        if (order.getId() == null) {
            Long id = idGenerator.getAndIncrement();
            order.setId(id);
        }
        orderStorage.put(order.getId(), order);
        orderNumberIndex.put(order.getOrderNumber(), order);
    }
    
    /**
     * Get order by number
     */
    @Transactional(readOnly = true)
    public Order getOrderByNumber(String orderNumber) {
        Order order = orderNumberIndex.get(orderNumber);
        if (order == null) {
            throw new RuntimeException("Order not found with number: " + orderNumber);
        }
        return order;
    }
    
    /**
     * Get order by ID
     */
    @Transactional(readOnly = true)
    public Order getOrderById(Long orderId) {
        Order order = orderStorage.get(orderId);
        if (order == null) {
            throw new RuntimeException("Order not found with id: " + orderId);
        }
        return order;
    }
    
    /**
     * Check if order exists
     */
    @Transactional(readOnly = true)
    public boolean orderExists(String orderNumber) {
        return orderNumberIndex.containsKey(orderNumber);
    }
} 