package com.ecommerce.api.controller.v2;

import com.ecommerce.application.service.UserService;
import com.ecommerce.domain.Money;
import com.ecommerce.api.dto.Result;
import com.ecommerce.api.annotation.ApiVersion;
import com.ecommerce.api.config.ApiVersionConfig;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * User Controller V2
 * Handles user-related REST APIs - Version 2
 * 
 * V2 Changes:
 * - Enhanced user response with registration timestamp
 * - Improved balance response with last update time
 * - Added user profile information
 */
@RestController
@RequestMapping(ApiVersionConfig.API_V2 + "/users")
@ApiVersion(value = "v2", since = "2025-06-01")
public class UserV2Controller {
    
    private static final Logger logger = LoggerFactory.getLogger(UserV2Controller.class);
    
    private final UserService userService;
    
    public UserV2Controller(UserService userService) {
        this.userService = userService;
    }
    
    /**
     * Create user - V2 with enhanced response
     * POST /api/v2/users
     */
    @PostMapping
    public ResponseEntity<UserV2Response> createUser(@Valid @RequestBody CreateUserRequest request) {
        logger.info("Creating user (V2): {}", request.getUsername());
        
        var user = userService.createUser(
            request.getUsername(),
            request.getEmail(),
            request.getPhone(),
            "CNY" // Default currency
        );
        
        // V2: Enhanced response with additional fields
        UserV2Response response = new UserV2Response(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getPhone(),
            user.getBalance().getAmount(),
            user.getBalance().getCurrency(),
            user.getStatus().toString(),
            LocalDateTime.now(), // registrationTime - new in V2
            LocalDateTime.now(), // lastLoginTime - new in V2
            true // isActive - new in V2
        );
        
        logger.info("User created successfully (V2) with ID: {}", user.getId());
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get user balance - V2 with enhanced response
     * GET /api/v2/users/{userId}/balance
     */
    @GetMapping("/{userId}/balance")
    public ResponseEntity<BalanceV2Response> getUserBalance(@PathVariable Long userId) {
        Money balance = userService.getUserBalance(userId);
        
        // V2: Enhanced response with additional fields
        BalanceV2Response response = new BalanceV2Response(
            userId,
            balance.getAmount(),
            balance.getCurrency(),
            LocalDateTime.now(), // lastUpdateTime - new in V2
            "ACTIVE" // accountStatus - new in V2
        );
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * User account recharge - V2 with enhanced response
     * POST /api/v2/users/{userId}/recharge
     */
    @PostMapping("/{userId}/recharge")
    public ResponseEntity<RechargeV2Response> rechargeUser(@PathVariable Long userId, 
                                                          @Valid @RequestBody RechargeRequest request) {
        logger.info("Processing recharge (V2) for user {}: amount={}", userId, request.getAmount());
        
        Money rechargeAmount = Money.of(request.getAmount(), request.getCurrency());
        userService.rechargeUser(userId, rechargeAmount);
        
        Money balance = userService.getUserBalance(userId);
        
        // V2: Enhanced response with transaction details
        RechargeV2Response response = new RechargeV2Response(
            userId,
            request.getAmount(),
            request.getCurrency(),
            balance.getAmount(),
            balance.getCurrency(),
            LocalDateTime.now(), // transactionTime - new in V2
            "COMPLETED", // transactionStatus - new in V2
            "SUCCESS" // resultCode - new in V2
        );
        
        logger.info("Recharge completed (V2) for user {}: new balance={}", userId, balance);
        return ResponseEntity.ok(response);
    }
    
    // V2 DTO classes with enhanced fields
    public static class CreateUserRequest {
        @NotBlank(message = "Username is required")
        private String username;
        
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        private String email;
        
        @NotBlank(message = "Phone is required")
        private String phone;
        
        // Getters and Setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
    }
    
    public static class UserV2Response {
        private Long id;
        private String username;
        private String email;
        private String phone;
        private BigDecimal balance;
        private String currency;
        private String status;
        private LocalDateTime registrationTime; // New in V2
        private LocalDateTime lastLoginTime; // New in V2
        private boolean isActive; // New in V2
        
        public UserV2Response(Long id, String username, String email, String phone, 
                             BigDecimal balance, String currency, String status,
                             LocalDateTime registrationTime, LocalDateTime lastLoginTime, boolean isActive) {
            this.id = id;
            this.username = username;
            this.email = email;
            this.phone = phone;
            this.balance = balance;
            this.currency = currency;
            this.status = status;
            this.registrationTime = registrationTime;
            this.lastLoginTime = lastLoginTime;
            this.isActive = isActive;
        }
        
        // Getters
        public Long getId() { return id; }
        public String getUsername() { return username; }
        public String getEmail() { return email; }
        public String getPhone() { return phone; }
        public BigDecimal getBalance() { return balance; }
        public String getCurrency() { return currency; }
        public String getStatus() { return status; }
        public LocalDateTime getRegistrationTime() { return registrationTime; }
        public LocalDateTime getLastLoginTime() { return lastLoginTime; }
        public boolean isActive() { return isActive; }
    }
    
    public static class BalanceV2Response {
        private Long userId;
        private BigDecimal balance;
        private String currency;
        private LocalDateTime lastUpdateTime; // New in V2
        private String accountStatus; // New in V2
        
        public BalanceV2Response(Long userId, BigDecimal balance, String currency,
                                LocalDateTime lastUpdateTime, String accountStatus) {
            this.userId = userId;
            this.balance = balance;
            this.currency = currency;
            this.lastUpdateTime = lastUpdateTime;
            this.accountStatus = accountStatus;
        }
        
        // Getters
        public Long getUserId() { return userId; }
        public BigDecimal getBalance() { return balance; }
        public String getCurrency() { return currency; }
        public LocalDateTime getLastUpdateTime() { return lastUpdateTime; }
        public String getAccountStatus() { return accountStatus; }
    }
    
    public static class RechargeRequest {
        @DecimalMin(value = "0.01", message = "Recharge amount must be positive")
        private BigDecimal amount;
        
        @NotBlank(message = "Currency is required")
        private String currency = "CNY";
        
        // Getters and Setters
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
    }
    
    public static class RechargeV2Response {
        private Long userId;
        private BigDecimal rechargeAmount;
        private String rechargeCurrency;
        private BigDecimal newBalance;
        private String balanceCurrency;
        private LocalDateTime transactionTime; // New in V2
        private String transactionStatus; // New in V2
        private String resultCode; // New in V2
        
        public RechargeV2Response(Long userId, BigDecimal rechargeAmount, String rechargeCurrency,
                                 BigDecimal newBalance, String balanceCurrency,
                                 LocalDateTime transactionTime, String transactionStatus, String resultCode) {
            this.userId = userId;
            this.rechargeAmount = rechargeAmount;
            this.rechargeCurrency = rechargeCurrency;
            this.newBalance = newBalance;
            this.balanceCurrency = balanceCurrency;
            this.transactionTime = transactionTime;
            this.transactionStatus = transactionStatus;
            this.resultCode = resultCode;
        }
        
        // Getters
        public Long getUserId() { return userId; }
        public BigDecimal getRechargeAmount() { return rechargeAmount; }
        public String getRechargeCurrency() { return rechargeCurrency; }
        public BigDecimal getNewBalance() { return newBalance; }
        public String getBalanceCurrency() { return balanceCurrency; }
        public LocalDateTime getTransactionTime() { return transactionTime; }
        public String getTransactionStatus() { return transactionStatus; }
        public String getResultCode() { return resultCode; }
    }
} 