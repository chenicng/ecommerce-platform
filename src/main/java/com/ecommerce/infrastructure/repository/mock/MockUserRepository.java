package com.ecommerce.infrastructure.repository.mock;

import com.ecommerce.domain.user.User;
import com.ecommerce.domain.Money;
import com.ecommerce.infrastructure.repository.UserRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Mock User Repository Implementation
 * Uses in-memory storage with pre-loaded demo data
 */
@Repository
@Profile("mock")
public class MockUserRepository implements UserRepository {
    
    private final Map<Long, User> storage = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);
    
    public MockUserRepository() {
        initializeDemoData();
    }
    
    private void initializeDemoData() {
        // Create demo users with some balance
        User user1 = new User("alice", "alice@example.com", "13800001111", "CNY");
        user1.setId(idGenerator.getAndIncrement());
        user1.recharge(Money.of("1000.00", "CNY"));
        storage.put(user1.getId(), user1);
        
        User user2 = new User("bob", "bob@example.com", "13800002222", "CNY");
        user2.setId(idGenerator.getAndIncrement());
        user2.recharge(Money.of("500.00", "CNY"));
        storage.put(user2.getId(), user2);
        
        User user3 = new User("charlie", "charlie@example.com", "13800003333", "CNY");
        user3.setId(idGenerator.getAndIncrement());
        user3.recharge(Money.of("2000.00", "CNY"));
        storage.put(user3.getId(), user3);
    }
    
    @Override
    public User save(User user) {
        if (user.getId() == null) {
            user.setId(idGenerator.getAndIncrement());
        }
        storage.put(user.getId(), user);
        return user;
    }
    
    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }
    
    @Override
    public boolean existsById(Long id) {
        return storage.containsKey(id);
    }
    
    @Override
    public Set<Long> getAllUserIds() {
        return new HashSet<>(storage.keySet());
    }
    
    @Override
    public int count() {
        return storage.size();
    }
    
    @Override
    public void deleteById(Long id) {
        storage.remove(id);
    }
} 