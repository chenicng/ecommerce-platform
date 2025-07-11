package com.ecommerce.infrastructure.repository.mock;

import com.ecommerce.domain.merchant.Merchant;
import com.ecommerce.infrastructure.repository.MerchantRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Mock Merchant Repository Implementation
 * Uses in-memory storage with pre-loaded demo data
 */
@Repository
@Profile("mock")
public class MockMerchantRepository implements MerchantRepository {
    
    private final Map<Long, Merchant> storage = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);
    
    public MockMerchantRepository() {
        initializeDemoData();
    }
    
    private void initializeDemoData() {
        // Create demo merchants
        Merchant merchant1 = new Merchant("Apple Store", "LICENSE-001", 
                                         "apple@store.com", "400-666-8888");
        merchant1.setId(idGenerator.getAndIncrement());
        storage.put(merchant1.getId(), merchant1);
        
        Merchant merchant2 = new Merchant("Tech Books Store", "LICENSE-002", 
                                         "books@tech.com", "400-888-6666");
        merchant2.setId(idGenerator.getAndIncrement());
        storage.put(merchant2.getId(), merchant2);
    }
    
    @Override
    public Merchant save(Merchant merchant) {
        if (merchant.getId() == null) {
            merchant.setId(idGenerator.getAndIncrement());
        }
        storage.put(merchant.getId(), merchant);
        return merchant;
    }
    
    @Override
    public Optional<Merchant> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }
    
    @Override
    public Optional<Merchant> findByBusinessLicense(String businessLicense) {
        return storage.values().stream()
                .filter(merchant -> businessLicense.equals(merchant.getBusinessLicense()))
                .findFirst();
    }
    
    @Override
    public Optional<Merchant> findByContactEmail(String contactEmail) {
        return storage.values().stream()
                .filter(merchant -> contactEmail.equals(merchant.getContactEmail()))
                .findFirst();
    }
    
    @Override
    public boolean existsByBusinessLicense(String businessLicense) {
        return storage.values().stream()
                .anyMatch(merchant -> businessLicense.equals(merchant.getBusinessLicense()));
    }
    
    @Override
    public boolean existsByContactEmail(String contactEmail) {
        return storage.values().stream()
                .anyMatch(merchant -> contactEmail.equals(merchant.getContactEmail()));
    }
    
    @Override
    public boolean existsById(Long id) {
        return storage.containsKey(id);
    }
    
    @Override
    public void deleteById(Long id) {
        storage.remove(id);
    }
    
    @Override
    public List<Merchant> findAllActive() {
        return storage.values().stream()
                .filter(Merchant::isActive)
                .collect(Collectors.toList());
    }
} 