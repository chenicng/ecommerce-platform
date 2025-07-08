package com.ecommerce.infrastructure.repository;

import com.ecommerce.domain.merchant.Merchant;
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
     * Delete merchant by ID
     */
    void deleteById(Long id);
} 