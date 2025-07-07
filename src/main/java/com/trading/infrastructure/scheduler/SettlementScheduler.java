package com.trading.infrastructure.scheduler;

import com.trading.application.service.SettlementService;
import com.trading.application.service.MerchantService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDate;

/**
 * 结算定时任务
 * 每天凌晨2点执行商家结算
 */
@Component
public class SettlementScheduler {
    
    private static final Logger logger = LoggerFactory.getLogger(SettlementScheduler.class);
    
    private final SettlementService settlementService;
    private final MerchantService merchantService;
    
    public SettlementScheduler(SettlementService settlementService, MerchantService merchantService) {
        this.settlementService = settlementService;
        this.merchantService = merchantService;
    }
    
    /**
     * 每天凌晨2点执行结算
     * cron表达式: 0 0 2 * * ? (秒 分 时 日 月 周)
     */
    @Scheduled(cron = "${ecommerce.settlement.cron:0 0 2 * * ?}")
    public void performDailySettlement() {
        logger.info("开始执行每日结算任务...");
        
        try {
            LocalDate settlementDate = LocalDate.now().minusDays(1); // 结算前一天的数据
            
            // 获取所有商家并执行结算
            // 在实际项目中，这里应该从数据库查询所有活跃的商家
            performSettlementForAllMerchants(settlementDate);
            
            logger.info("每日结算任务执行完成");
            
        } catch (Exception e) {
            logger.error("每日结算任务执行失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 为所有商家执行结算
     */
    private void performSettlementForAllMerchants(LocalDate settlementDate) {
        // 由于我们使用内存存储，这里模拟为已知的商家ID执行结算
        // 在实际项目中，这里应该查询数据库获取所有商家
        
        // 假设我们有商家ID 1, 2, 3
        for (Long merchantId = 1L; merchantId <= 3L; merchantId++) {
            try {
                if (merchantService.merchantExists(merchantId)) {
                    settlementService.performSettlement(merchantId, settlementDate);
                    logger.info("商家 {} 结算完成", merchantId);
                } else {
                    logger.debug("商家 {} 不存在，跳过结算", merchantId);
                }
            } catch (Exception e) {
                logger.error("商家 {} 结算失败: {}", merchantId, e.getMessage(), e);
                // 继续处理下一个商家，不因为单个商家失败而停止整个任务
            }
        }
    }
    
    /**
     * 手动触发结算（用于测试）
     * 每分钟执行一次，仅在开发环境使用
     */
    @Scheduled(fixedDelay = 60000, initialDelay = 30000)
    public void manualSettlementTrigger() {
        // 这个方法在生产环境应该被禁用
        // 这里只是为了演示和测试目的
        if (isManualTriggerEnabled()) {
            logger.debug("手动触发结算检查...");
            
            // 检查是否有需要结算的商家
            LocalDate today = LocalDate.now();
            
            // 为测试目的，检查商家1是否存在并执行结算
            try {
                if (merchantService.merchantExists(1L)) {
                    logger.info("执行手动结算测试...");
                    settlementService.performSettlement(1L, today);
                }
            } catch (Exception e) {
                logger.debug("手动结算测试失败: {}", e.getMessage());
            }
        }
    }
    
    /**
     * 检查是否启用手动触发
     * 在生产环境应该返回false
     */
    private boolean isManualTriggerEnabled() {
        // 可以通过配置文件控制
        String env = System.getProperty("spring.profiles.active", "dev");
        return "dev".equals(env) || "test".equals(env);
    }
} 