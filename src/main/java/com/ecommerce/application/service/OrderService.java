package com.ecommerce.application.service;

import com.ecommerce.domain.order.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 订单服务
 * 管理订单相关的业务操作
 */
@Service
@Transactional
public class OrderService {
    
    // 简单的内存存储，生产环境应该使用数据库
    private final Map<Long, Order> orderStorage = new HashMap<>();
    private final Map<String, Order> orderNumberIndex = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);
    
    /**
     * 保存订单
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
     * 根据订单号获取订单
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
     * 根据ID获取订单
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
     * 检查订单是否存在
     */
    @Transactional(readOnly = true)
    public boolean orderExists(String orderNumber) {
        return orderNumberIndex.containsKey(orderNumber);
    }
} 