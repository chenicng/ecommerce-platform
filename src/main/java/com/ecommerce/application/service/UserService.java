package com.ecommerce.application.service;

import com.ecommerce.domain.user.User;
import com.ecommerce.domain.Money;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 用户服务
 * 管理用户相关的业务操作
 */
@Service
@Transactional
public class UserService {
    
    // 简单的内存存储，生产环境应该使用数据库
    private final Map<Long, User> userStorage = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);
    
    /**
     * 创建用户
     */
    public User createUser(String username, String email, String phone, String currency) {
        User user = new User(username, email, phone, currency);
        Long id = idGenerator.getAndIncrement();
        user.setId(id);
        userStorage.put(id, user);
        return user;
    }
    
    /**
     * 根据ID获取用户
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
     * 用户账户充值
     */
    public void rechargeUserAccount(Long userId, Money amount) {
        User user = getUserById(userId);
        user.recharge(amount);
        userStorage.put(userId, user);
    }
    
    /**
     * 保存用户
     */
    public void saveUser(User user) {
        userStorage.put(user.getId(), user);
    }
    
    /**
     * 获取用户余额
     */
    @Transactional(readOnly = true)
    public Money getUserBalance(Long userId) {
        return getUserById(userId).getBalance();
    }
    
    /**
     * 检查用户是否存在
     */
    @Transactional(readOnly = true)
    public boolean userExists(Long userId) {
        return userStorage.containsKey(userId);
    }
} 