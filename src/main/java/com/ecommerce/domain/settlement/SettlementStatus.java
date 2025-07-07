package com.ecommerce.domain.settlement;

/**
 * 结算状态枚举
 */
public enum SettlementStatus {
    /**
     * 匹配状态 - 预期收入与实际余额一致
     */
    MATCHED,
    
    /**
     * 盈余状态 - 实际余额大于预期收入
     */
    SURPLUS,
    
    /**
     * 亏损状态 - 实际余额小于预期收入
     */
    DEFICIT,
    
    /**
     * 已处理状态 - 差异已被人工处理
     */
    PROCESSED
} 