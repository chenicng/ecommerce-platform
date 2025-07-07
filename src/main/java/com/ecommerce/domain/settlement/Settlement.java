package com.ecommerce.domain.settlement;

import com.ecommerce.domain.BaseEntity;
import com.ecommerce.domain.Money;
import java.time.LocalDateTime;

/**
 * 结算聚合根
 * 记录商家每日结算信息
 */
public class Settlement extends BaseEntity {
    
    private Long merchantId;
    private LocalDateTime settlementDate;
    private Money expectedIncome;  // 根据销售记录计算的预期收入
    private Money actualBalance;   // 商家账户实际余额
    private Money difference;      // 差异金额
    private SettlementStatus status;
    private String remarks;
    
    // 构造函数
    protected Settlement() {
        super();
    }
    
    public Settlement(Long merchantId, LocalDateTime settlementDate, 
                     Money expectedIncome, Money actualBalance) {
        super();
        this.merchantId = merchantId;
        this.settlementDate = settlementDate;
        this.expectedIncome = expectedIncome;
        this.actualBalance = actualBalance;
        this.difference = calculateDifference(expectedIncome, actualBalance);
        this.status = determinateStatus();
        this.remarks = "";
    }
    
    /**
     * 计算差异金额
     */
    private Money calculateDifference(Money expected, Money actual) {
        if (actual.isGreaterThanOrEqual(expected)) {
            return actual.subtract(expected);
        } else {
            return expected.subtract(actual);
        }
    }
    
    /**
     * 根据差异判断结算状态
     */
    private SettlementStatus determinateStatus() {
        if (this.difference.isZero()) {
            return SettlementStatus.MATCHED;
        } else if (this.actualBalance.isGreaterThan(this.expectedIncome)) {
            return SettlementStatus.SURPLUS;
        } else {
            return SettlementStatus.DEFICIT;
        }
    }
    
    /**
     * 添加备注
     */
    public void addRemarks(String remarks) {
        this.remarks = remarks;
        this.markAsUpdated();
    }
    
    /**
     * 标记为已处理
     */
    public void markAsProcessed(String processRemarks) {
        this.status = SettlementStatus.PROCESSED;
        this.remarks = processRemarks;
        this.markAsUpdated();
    }
    
    /**
     * 检查是否匹配
     */
    public boolean isMatched() {
        return SettlementStatus.MATCHED.equals(this.status);
    }
    
    /**
     * 检查是否有盈余
     */
    public boolean hasSurplus() {
        return SettlementStatus.SURPLUS.equals(this.status);
    }
    
    /**
     * 检查是否有亏损
     */
    public boolean hasDeficit() {
        return SettlementStatus.DEFICIT.equals(this.status);
    }
    
    // Getters
    public Long getMerchantId() {
        return merchantId;
    }
    
    public LocalDateTime getSettlementDate() {
        return settlementDate;
    }
    
    public Money getExpectedIncome() {
        return expectedIncome;
    }
    
    public Money getActualBalance() {
        return actualBalance;
    }
    
    public Money getDifference() {
        return difference;
    }
    
    public SettlementStatus getStatus() {
        return status;
    }
    
    public String getRemarks() {
        return remarks;
    }
    
    // Package private setters for JPA
    void setMerchantId(Long merchantId) {
        this.merchantId = merchantId;
    }
    
    void setSettlementDate(LocalDateTime settlementDate) {
        this.settlementDate = settlementDate;
    }
    
    void setExpectedIncome(Money expectedIncome) {
        this.expectedIncome = expectedIncome;
    }
    
    void setActualBalance(Money actualBalance) {
        this.actualBalance = actualBalance;
    }
    
    void setDifference(Money difference) {
        this.difference = difference;
    }
    
    void setStatus(SettlementStatus status) {
        this.status = status;
    }
    
    void setRemarks(String remarks) {
        this.remarks = remarks;
    }
} 