package com.trading.application.service;

import com.trading.domain.merchant.Merchant;
import com.trading.domain.Money;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 商家服务
 * 管理商家相关的业务操作
 */
@Service
@Transactional
public class MerchantService {
    
    // 简单的内存存储，生产环境应该使用数据库
    private final Map<Long, Merchant> merchantStorage = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);
    
    /**
     * 创建商家
     */
    public Merchant createMerchant(String merchantName, String businessLicense, 
                                 String contactEmail, String contactPhone, String currency) {
        Merchant merchant = new Merchant(merchantName, businessLicense, contactEmail, contactPhone, currency);
        Long id = idGenerator.getAndIncrement();
        merchant.setId(id);
        merchantStorage.put(id, merchant);
        return merchant;
    }
    
    /**
     * 根据ID获取商家
     */
    @Transactional(readOnly = true)
    public Merchant getMerchantById(Long merchantId) {
        Merchant merchant = merchantStorage.get(merchantId);
        if (merchant == null) {
            throw new RuntimeException("Merchant not found with id: " + merchantId);
        }
        return merchant;
    }
    
    /**
     * 保存商家
     */
    public void saveMerchant(Merchant merchant) {
        merchantStorage.put(merchant.getId(), merchant);
    }
    
    /**
     * 获取商家余额
     */
    @Transactional(readOnly = true)
    public Money getMerchantBalance(Long merchantId) {
        return getMerchantById(merchantId).getBalance();
    }
    
    /**
     * 获取商家总收入
     */
    @Transactional(readOnly = true)
    public Money getMerchantTotalIncome(Long merchantId) {
        return getMerchantById(merchantId).getTotalIncome();
    }
    
    /**
     * 检查商家是否存在
     */
    @Transactional(readOnly = true)
    public boolean merchantExists(Long merchantId) {
        return merchantStorage.containsKey(merchantId);
    }
} 