package com.ecommerce.application.service;

import com.ecommerce.domain.user.User;
import com.ecommerce.domain.user.UserNotFoundException;
import com.ecommerce.domain.Money;
import com.ecommerce.infrastructure.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Set;

/**
 * User Service
 * Manages user-related business operations
 */
@Service
@Transactional
public class UserService {
    
    private final UserRepository userRepository;
    
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    /**
     * Create user
     */
    public User createUser(String username, String email, String phone, String currency) {
        User user = new User(username, email, phone, currency);
        return userRepository.save(user);
    }
    
    /**
     * Get user by ID
     */
    @Transactional(readOnly = true)
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }
    
    /**
     * User account recharge
     */
    public void rechargeUser(Long userId, Money amount) {
        User user = getUserById(userId);
        user.recharge(amount);
        userRepository.save(user);
    }
    
    /**
     * Save user
     */
    public void saveUser(User user) {
        userRepository.save(user);
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
        return userRepository.existsById(userId);
    }

    /**
     * Get all existing user IDs (for debugging)
     */
    @Transactional(readOnly = true)
    public Set<Long> getAllUserIds() {
        return userRepository.getAllUserIds();
    }

    /**
     * Get total user count
     */
    @Transactional(readOnly = true)
    public int getUserCount() {
        return userRepository.count();
    }
} 