package com.trading.application.service;

import com.trading.domain.settlement.Settlement;
import com.trading.domain.settlement.SettlementStatus;
import com.trading.domain.Money;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.List;
import java.util.ArrayList;

/**
 * 结算服务
 * 处理商家结算相关的业务逻辑
 */
@Service
@Transactional
public class SettlementService {
    
    private static final Logger logger = LoggerFactory.getLogger(SettlementService.class);
    
    // 简单的内存存储，生产环境应该使用数据库
    private final Map<Long, Settlement> settlementStorage = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);
    
    private final MerchantService merchantService;
    private final OrderService orderService;
    
    public SettlementService(MerchantService merchantService, OrderService orderService) {
        this.merchantService = merchantService;
        this.orderService = orderService;
    }
    
    /**
     * 执行商家结算
     */
    public Settlement performSettlement(Long merchantId, LocalDate settlementDate) {
        try {
            logger.info("Starting settlement for merchant {} on date {}", merchantId, settlementDate);
            
            // 1. 计算预期收入（基于完成的订单）
            Money expectedIncome = calculateExpectedIncome(merchantId, settlementDate);
            
            // 2. 获取商家实际余额
            Money actualBalance = merchantService.getMerchantBalance(merchantId);
            
            // 3. 创建结算记录
            Settlement settlement = new Settlement(
                merchantId,
                settlementDate.atTime(2, 0), // 结算时间设为凌晨2点
                expectedIncome,
                actualBalance
            );
            
            // 4. 保存结算记录
            saveSettlement(settlement);
            
            // 5. 记录结算结果
            logSettlementResult(settlement);
            
            logger.info("Settlement completed for merchant {}: status={}, difference={}", 
                       merchantId, settlement.getStatus(), settlement.getDifference());
            
            return settlement;
            
        } catch (Exception e) {
            logger.error("Settlement failed for merchant {}: {}", merchantId, e.getMessage(), e);
            throw new RuntimeException("Settlement failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * 计算预期收入
     * 在实际项目中，这里应该查询数据库中的订单记录
     */
    private Money calculateExpectedIncome(Long merchantId, LocalDate settlementDate) {
        // 由于我们使用内存存储，这里返回商家的总收入作为预期收入
        // 在真实项目中，这里应该计算指定日期的销售额
        Money totalIncome = merchantService.getMerchantTotalIncome(merchantId);
        
        logger.debug("Calculated expected income for merchant {}: {}", merchantId, totalIncome);
        return totalIncome;
    }
    
    /**
     * 保存结算记录
     */
    private void saveSettlement(Settlement settlement) {
        Long id = idGenerator.getAndIncrement();
        settlement.setId(id);
        settlementStorage.put(id, settlement);
    }
    
    /**
     * 记录结算结果
     */
    private void logSettlementResult(Settlement settlement) {
        String statusDesc;
        switch (settlement.getStatus()) {
            case MATCHED:
                statusDesc = "完全匹配";
                break;
            case SURPLUS:
                statusDesc = "有盈余";
                settlement.addRemarks("账户余额超出预期收入 " + settlement.getDifference());
                break;
            case DEFICIT:
                statusDesc = "有亏损";
                settlement.addRemarks("账户余额低于预期收入 " + settlement.getDifference());
                break;
            default:
                statusDesc = "未知状态";
                break;
        }
        
        logger.info("结算结果 - 商家ID: {}, 状态: {}, 预期收入: {}, 实际余额: {}, 差异: {}", 
                   settlement.getMerchantId(), 
                   statusDesc,
                   settlement.getExpectedIncome(),
                   settlement.getActualBalance(),
                   settlement.getDifference());
    }
    
    /**
     * 获取结算记录
     */
    @Transactional(readOnly = true)
    public Settlement getSettlementById(Long settlementId) {
        Settlement settlement = settlementStorage.get(settlementId);
        if (settlement == null) {
            throw new RuntimeException("Settlement not found with id: " + settlementId);
        }
        return settlement;
    }
    
    /**
     * 获取商家的所有结算记录
     */
    @Transactional(readOnly = true)
    public List<Settlement> getSettlementsByMerchant(Long merchantId) {
        return settlementStorage.values().stream()
                .filter(s -> s.getMerchantId().equals(merchantId))
                .toList();
    }
    
    /**
     * 获取所有不匹配的结算记录
     */
    @Transactional(readOnly = true)
    public List<Settlement> getMismatchedSettlements() {
        return settlementStorage.values().stream()
                .filter(s -> !s.isMatched())
                .toList();
    }
} 