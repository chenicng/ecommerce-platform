package com.ecommerce.infrastructure.repository;

import com.ecommerce.domain.settlement.Settlement;
import java.util.Optional;

/**
 * Settlement Repository Interface
 * Defines data access contract for Settlement aggregate
 */
public interface SettlementRepository {
    
    /**
     * Save settlement
     */
    Settlement save(Settlement settlement);
    
    /**
     * Find settlement by ID
     */
    Optional<Settlement> findById(Long id);
    
    /**
     * Delete settlement by ID
     */
    void deleteById(Long id);
} 