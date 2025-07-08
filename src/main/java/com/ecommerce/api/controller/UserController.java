package com.ecommerce.api.controller;

import com.ecommerce.application.service.UserService;
import com.ecommerce.domain.Money;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.math.BigDecimal;

/**
 * User Controller
 * Handles user-related REST APIs
 */
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    
    private final UserService userService;
    
    public UserController(UserService userService) {
        this.userService = userService;
    }
    
    /**
     * Create user
     * POST /api/users
     */
    @PostMapping
    public ResponseEntity<Object> createUser(@RequestBody CreateUserRequest request) {
        try {
            logger.info("Creating user: {}", request.getUsername());
            
            var user = userService.createUser(
                request.getUsername(),
                request.getEmail(),
                request.getPhone(),
                "CNY" // Default currency
            );
            
            UserResponse response = new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPhone(),
                user.getBalance().getAmount(),
                user.getBalance().getCurrency(),
                user.getStatus().toString()
            );
            
            logger.info("User created successfully with ID: {}", user.getId());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to create user: {}", e.getMessage());
            
            ErrorResponse errorResponse = new ErrorResponse(
                "USER_CREATION_FAILED",
                e.getMessage(),
                "/api/users"
            );
            
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * User account recharge
     * POST /api/users/{userId}/recharge
     */
    @PostMapping("/{userId}/recharge")
    public ResponseEntity<Object> rechargeUser(@PathVariable Long userId, 
                                             @RequestBody RechargeRequest request) {
        try {
            logger.info("Processing recharge for user {}: amount={}", userId, request.getAmount());
            
            // Validate request parameters
            if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Recharge amount must be positive");
            }
            
            // Recharge
            Money rechargeAmount = Money.of(request.getAmount(), request.getCurrency());
            userService.rechargeUser(userId, rechargeAmount);
            
            // Get balance after recharge
            Money balance = userService.getUserBalance(userId);
            
            BalanceResponse response = new BalanceResponse(
                userId,
                balance.getAmount(),
                balance.getCurrency()
            );
            
            logger.info("Recharge completed for user {}: new balance={}", userId, balance);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to recharge user {}: {}", userId, e.getMessage());
            
            // Provide more specific error messages
            String errorCode;
            String errorMessage;
            if (e.getMessage().contains("User not found")) {
                errorCode = "USER_NOT_FOUND";
                errorMessage = String.format("User with ID %d does not exist. Please verify the user ID or create the user first.", userId);
                
                // Log existing users for debugging
                var existingUsers = userService.getAllUserIds();
                logger.warn("Available user IDs: {}", existingUsers);
            } else {
                errorCode = "RECHARGE_FAILED";
                errorMessage = e.getMessage();
            }
            
            ErrorResponse errorResponse = new ErrorResponse(
                errorCode,
                errorMessage,
                "/api/users/" + userId + "/recharge"
            );
            
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * Get user balance
     * GET /api/users/{userId}/balance
     */
    @GetMapping("/{userId}/balance")
    public ResponseEntity<Object> getUserBalance(@PathVariable Long userId) {
        try {
            Money balance = userService.getUserBalance(userId);
            
            BalanceResponse response = new BalanceResponse(
                userId,
                balance.getAmount(),
                balance.getCurrency()
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to get balance for user {}: {}", userId, e.getMessage());
            
            ErrorResponse errorResponse = new ErrorResponse(
                "BALANCE_QUERY_FAILED",
                e.getMessage(),
                "/api/users/" + userId + "/balance"
            );
            
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Get all existing user IDs (for debugging)
     * GET /api/users/debug/all-ids
     */
    @GetMapping("/debug/all-ids")
    public ResponseEntity<Object> getAllUserIds() {
        try {
            var userIds = userService.getAllUserIds();
            int userCount = userService.getUserCount();
            
            var response = new java.util.HashMap<String, Object>();
            response.put("totalUsers", userCount);
            response.put("existingUserIds", userIds);
            response.put("timestamp", java.time.LocalDateTime.now());
            
            logger.info("Debug: Current system has {} users with IDs: {}", userCount, userIds);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to get user IDs: {}", e.getMessage());
            
            ErrorResponse errorResponse = new ErrorResponse(
                "DEBUG_FAILED",
                e.getMessage(),
                "/api/users/debug/all-ids"
            );
            
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    // DTO classes
    public static class CreateUserRequest {
        private String username;
        private String email;
        private String phone;
        
        // Getters and Setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
    }
    
    public static class UserResponse {
        private Long id;
        private String username;
        private String email;
        private String phone;
        private BigDecimal balance;
        private String currency;
        private String status;
        
        public UserResponse(Long id, String username, String email, String phone, 
                          BigDecimal balance, String currency, String status) {
            this.id = id;
            this.username = username;
            this.email = email;
            this.phone = phone;
            this.balance = balance;
            this.currency = currency;
            this.status = status;
        }
        
        // Getters and Setters
        public Long getId() { return id; }
        public String getUsername() { return username; }
        public String getEmail() { return email; }
        public String getPhone() { return phone; }
        public BigDecimal getBalance() { return balance; }
        public String getCurrency() { return currency; }
        public String getStatus() { return status; }
    }
    
    public static class RechargeRequest {
        private BigDecimal amount;
        private String currency = "CNY";
        
        // Getters and Setters
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
    }
    
    public static class RechargeResponse {
        private Long userId;
        private BigDecimal rechargeAmount;
        private String rechargeCurrency;
        private BigDecimal newBalance;
        private String balanceCurrency;
        private String status;
        private String message;
        
        public RechargeResponse() {}
        
        public RechargeResponse(Long userId, BigDecimal rechargeAmount, String rechargeCurrency,
                              BigDecimal newBalance, String balanceCurrency, String status, String message) {
            this.userId = userId;
            this.rechargeAmount = rechargeAmount;
            this.rechargeCurrency = rechargeCurrency;
            this.newBalance = newBalance;
            this.balanceCurrency = balanceCurrency;
            this.status = status;
            this.message = message;
        }
        
        // Getters and Setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public BigDecimal getRechargeAmount() { return rechargeAmount; }
        public String getRechargeCurrency() { return rechargeCurrency; }
        public BigDecimal getNewBalance() { return newBalance; }
        public String getBalanceCurrency() { return balanceCurrency; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
    
    public static class BalanceResponse {
        private Long userId;
        private BigDecimal balance;
        private String currency;
        
        public BalanceResponse(Long userId, BigDecimal balance, String currency) {
            this.userId = userId;
            this.balance = balance;
            this.currency = currency;
        }
        
        // Getters
        public Long getUserId() { return userId; }
        public BigDecimal getBalance() { return balance; }
        public String getCurrency() { return currency; }
    }
    
    public static class ErrorResponse {
        private String error;
        private String message;
        private String path;
        private long timestamp;
        
        public ErrorResponse(String error, String message, String path) {
            this.error = error;
            this.message = message;
            this.path = path;
            this.timestamp = System.currentTimeMillis();
        }
        
        // Getters
        public String getError() { return error; }
        public String getMessage() { return message; }
        public String getPath() { return path; }
        public long getTimestamp() { return timestamp; }
    }
} 