package com.trading.domain.order;

/**
 * 订单状态枚举
 */
public enum OrderStatus {
    /**
     * 待处理状态
     */
    PENDING,
    
    /**
     * 已确认状态
     */
    CONFIRMED,
    
    /**
     * 已支付状态
     */
    PAID,
    
    /**
     * 已完成状态
     */
    COMPLETED,
    
    /**
     * 已取消状态
     */
    CANCELLED
} 