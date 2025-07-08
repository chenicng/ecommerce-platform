package com.ecommerce.infrastructure.repository;

import com.ecommerce.domain.order.Order;
import java.util.Optional;

/**
 * Order Repository Interface
 * Defines data access contract for Order aggregate
 */
public interface OrderRepository {
    
    /**
     * Save order
     */
    Order save(Order order);
    
    /**
     * Find order by ID
     */
    Optional<Order> findById(Long id);
    
    /**
     * Find order by order number
     */
    Optional<Order> findByOrderNumber(String orderNumber);
    
    /**
     * Check if order exists by order number
     */
    boolean existsByOrderNumber(String orderNumber);
    
    /**
     * Delete order by ID
     */
    void deleteById(Long id);
} 