package com.ecommerce.application.service;

import com.ecommerce.domain.user.User;
import com.ecommerce.domain.Money;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * User Service
 * Manages user-related business operations
 */
@Service
@Transactional
public class UserService {
    
    // Simple in-memory storage, production should use database
    private final Map<Long, User> userStorage = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);
    
    /**
     * Create user
     */
    public User createUser(String username, String email, String phone, String currency) {
        User user = new User(username, email, phone, currency);
        Long id = idGenerator.getAndIncrement();
        user.setId(id);
        userStorage.put(id, user);
        return user;
    }
    
    /**
     * Get user by ID
     */
    @Transactional(readOnly = true)
    public User getUserById(Long userId) {
        User user = userStorage.get(userId);
        if (user == null) {
            throw new RuntimeException("User not found with id: " + userId);
        }
        return user;
    }
    
    /**
     * User account recharge
     */
    public void rechargeUser(Long userId, Money amount) {
        User user = getUserById(userId);
        user.recharge(amount);
        saveUser(user);
    }
    
    /**
     * Save user
     */
    public void saveUser(User user) {
        userStorage.put(user.getId(), user);
    }
    
    /**
     * Get user balance
     */
    @Transactional(readOnly = true)
    public Money getUserBalance(Long userId) {
        User user = getUserById(userId);
        return user.getBalance();
    }
    
    /**
     * Check if user exists
     */
    @Transactional(readOnly = true)
    public boolean userExists(Long userId) {
        return userStorage.containsKey(userId);
    }
} 