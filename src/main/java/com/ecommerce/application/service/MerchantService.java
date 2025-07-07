package com.ecommerce.application.service;

import com.ecommerce.domain.merchant.Merchant;
import com.ecommerce.domain.Money;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Merchant Service
 * Manages merchant-related business operations
 */
@Service
@Transactional
public class MerchantService {
    
    // Simple in-memory storage, production should use database
    private final Map<Long, Merchant> merchantStorage = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);
    
    /**
     * Create merchant
     */
    public Merchant createMerchant(String merchantName, String businessLicense,
                                 String contactEmail, String contactPhone) {
        Merchant merchant = new Merchant(merchantName, businessLicense, contactEmail, contactPhone);
        Long id = idGenerator.getAndIncrement();
        merchant.setId(id);
        merchantStorage.put(id, merchant);
        return merchant;
    }
    
    /**
     * Get merchant by ID
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
     * Save merchant
     */
    public void saveMerchant(Merchant merchant) {
        merchantStorage.put(merchant.getId(), merchant);
    }
    
    /**
     * Get merchant balance
     */
    @Transactional(readOnly = true)
    public Money getMerchantBalance(Long merchantId) {
        Merchant merchant = getMerchantById(merchantId);
        return merchant.getBalance();
    }
    
    /**
     * Get merchant total income
     */
    @Transactional(readOnly = true)
    public Money getMerchantTotalIncome(Long merchantId) {
        Merchant merchant = getMerchantById(merchantId);
        return merchant.getTotalIncome();
    }
    
    /**
     * Check if merchant exists
     */
    @Transactional(readOnly = true)
    public boolean merchantExists(Long merchantId) {
        return merchantStorage.containsKey(merchantId);
    }
} 