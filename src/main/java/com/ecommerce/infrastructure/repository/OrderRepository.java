package com.ecommerce.infrastructure.repository;

import com.ecommerce.domain.order.Order;
import com.ecommerce.domain.order.OrderStatus;
import java.time.LocalDateTime;
import java.util.List;
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
     * Find orders by merchant ID and date range
     */
    List<Order> findByMerchantIdAndOrderTimeBetween(Long merchantId, LocalDateTime start, LocalDateTime end);
    
    /**
     * Find completed orders by merchant ID and date range
     * Used for settlement calculation
     */
    List<Order> findByMerchantIdAndStatusAndOrderTimeBetween(Long merchantId, OrderStatus status, LocalDateTime start, LocalDateTime end);
    
    /**
     * Delete order by ID
     */
    void deleteById(Long id);
} 