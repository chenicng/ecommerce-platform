package com.ecommerce.infrastructure.repository;

import com.ecommerce.domain.merchant.Merchant;
import java.util.List;
import java.util.Optional;

/**
 * Merchant Repository Interface
 * Defines data access contract for Merchant aggregate
 */
public interface MerchantRepository {
    
    /**
     * Save merchant
     */
    Merchant save(Merchant merchant);
    
    /**
     * Find merchant by ID
     */
    Optional<Merchant> findById(Long id);
    
    /**
     * Check if merchant exists by ID
     */
    boolean existsById(Long id);
    
    /**
     * Find merchant by business license
     */
    Optional<Merchant> findByBusinessLicense(String businessLicense);
    
    /**
     * Find merchant by contact email
     */
    Optional<Merchant> findByContactEmail(String contactEmail);
    
    /**
     * Check if merchant exists by business license
     */
    boolean existsByBusinessLicense(String businessLicense);
    
    /**
     * Check if merchant exists by contact email
     */
    boolean existsByContactEmail(String contactEmail);
    
    /**
     * Delete merchant by ID
     */
    void deleteById(Long id);
    
    /**
     * Find all active merchants
     * Used for settlement processing
     */
    List<Merchant> findAllActive();
} 