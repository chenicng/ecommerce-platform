package com.ecommerce.application.service;

import com.ecommerce.domain.merchant.Merchant;
import com.ecommerce.domain.merchant.MerchantNotFoundException;
import com.ecommerce.domain.merchant.DuplicateMerchantException;
import com.ecommerce.domain.Money;
import com.ecommerce.infrastructure.repository.MerchantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Merchant Service
 * Manages merchant-related business operations
 */
@Service
@Transactional
public class MerchantService {
    
    private final MerchantRepository merchantRepository;
    
    public MerchantService(MerchantRepository merchantRepository) {
        this.merchantRepository = merchantRepository;
    }
    
    /**
     * Create merchant with uniqueness validation
     */
    public Merchant createMerchant(String merchantName, String businessLicense,
                                 String contactEmail, String contactPhone) {
        // Validate business license uniqueness
        if (merchantRepository.existsByBusinessLicense(businessLicense)) {
            throw DuplicateMerchantException.forBusinessLicense(businessLicense);
        }
        
        // Validate contact email uniqueness
        if (merchantRepository.existsByContactEmail(contactEmail)) {
            throw DuplicateMerchantException.forContactEmail(contactEmail);
        }
        
        Merchant merchant = new Merchant(merchantName, businessLicense, contactEmail, contactPhone);
        return merchantRepository.save(merchant);
    }
    
    /**
     * Get merchant by ID
     */
    @Transactional(readOnly = true)
    public Merchant getMerchantById(Long merchantId) {
        return merchantRepository.findById(merchantId)
                .orElseThrow(() -> new MerchantNotFoundException(merchantId));
    }
    
    /**
     * Save merchant
     */
    public void saveMerchant(Merchant merchant) {
        merchantRepository.save(merchant);
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
        return merchantRepository.existsById(merchantId);
    }
    
    /**
     * Get all active merchants
     * Used for settlement processing
     */
    @Transactional(readOnly = true)
    public List<Merchant> getAllActiveMerchants() {
        return merchantRepository.findAllActive();
    }
} 