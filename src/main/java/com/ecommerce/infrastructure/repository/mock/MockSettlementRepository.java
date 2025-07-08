package com.ecommerce.infrastructure.repository.mock;

import com.ecommerce.domain.settlement.Settlement;
import com.ecommerce.infrastructure.repository.SettlementRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Mock Settlement Repository Implementation
 * Uses in-memory storage
 */
@Repository
@Profile("mock")
public class MockSettlementRepository implements SettlementRepository {
    
    private final Map<Long, Settlement> storage = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);
    
    @Override
    public Settlement save(Settlement settlement) {
        if (settlement.getId() == null) {
            settlement.setId(idGenerator.getAndIncrement());
        }
        storage.put(settlement.getId(), settlement);
        return settlement;
    }
    
    @Override
    public Optional<Settlement> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }
    
    @Override
    public void deleteById(Long id) {
        storage.remove(id);
    }
} 