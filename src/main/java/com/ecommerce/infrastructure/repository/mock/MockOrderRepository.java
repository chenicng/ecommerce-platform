package com.ecommerce.infrastructure.repository.mock;

import com.ecommerce.domain.order.Order;
import com.ecommerce.domain.order.OrderStatus;
import com.ecommerce.infrastructure.repository.OrderRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Mock Order Repository Implementation
 * Uses in-memory storage
 */
@Repository
@Profile("mock")
public class MockOrderRepository implements OrderRepository {
    
    private final Map<Long, Order> storage = new HashMap<>();
    private final Map<String, Order> orderNumberIndex = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);
    
    @Override
    public Order save(Order order) {
        if (order.getId() == null) {
            order.setId(idGenerator.getAndIncrement());
        }
        storage.put(order.getId(), order);
        orderNumberIndex.put(order.getOrderNumber(), order);
        return order;
    }
    
    @Override
    public Optional<Order> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }
    
    @Override
    public Optional<Order> findByOrderNumber(String orderNumber) {
        return Optional.ofNullable(orderNumberIndex.get(orderNumber));
    }
    
    @Override
    public boolean existsByOrderNumber(String orderNumber) {
        return orderNumberIndex.containsKey(orderNumber);
    }
    
    @Override
    public List<Order> findByMerchantIdAndOrderTimeBetween(Long merchantId, LocalDateTime start, LocalDateTime end) {
        return storage.values().stream()
                .filter(order -> Objects.equals(order.getMerchantId(), merchantId))
                .filter(order -> {
                    LocalDateTime orderTime = order.getOrderTime();
                    return orderTime != null && 
                           !orderTime.isBefore(start) && 
                           !orderTime.isAfter(end);
                })
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Order> findByMerchantIdAndStatusAndOrderTimeBetween(Long merchantId, OrderStatus status, LocalDateTime start, LocalDateTime end) {
        // Filter orders by merchant ID, status and date range
        return storage.values().stream()
                .filter(order -> Objects.equals(order.getMerchantId(), merchantId))
                .filter(order -> Objects.equals(order.getStatus(), status))
                .filter(order -> {
                    LocalDateTime orderTime = order.getOrderTime();
                    return orderTime != null && 
                           !orderTime.isBefore(start) && 
                           !orderTime.isAfter(end);
                })
                .collect(Collectors.toList());
    }
    
    @Override
    public void deleteById(Long id) {
        Order order = storage.remove(id);
        if (order != null) {
            orderNumberIndex.remove(order.getOrderNumber());
        }
    }
} 