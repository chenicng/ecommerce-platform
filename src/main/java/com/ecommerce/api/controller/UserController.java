package com.ecommerce.api.controller;

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
import org.springframework.context.annotation.Profile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.Map;

/**
 * User Controller
 * Handles user-related REST APIs
 */
@RestController
@RequestMapping(ApiVersionConfig.API_V1 + "/users")
@ApiVersion(value = "v1", since = "2025-01-01")
@Tag(name = "User Management", description = "User registration, authentication and account management")
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
    @Operation(summary = "Create User", description = "Register a new user with username, email, and phone")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data", 
                    content = @Content(schema = @Schema(implementation = Result.class)))
    })
    public ResponseEntity<Result<UserResponse>> createUser(@Valid @RequestBody CreateUserRequest request) {
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
        return ResponseEntity.ok(Result.successWithMessage("User created successfully", response));
    }
    
    /**
     * User account recharge
     * POST /api/users/{userId}/recharge
     */
    @PostMapping("/{userId}/recharge")
    @Operation(summary = "Recharge User Account", description = "Add funds to user's account balance")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Recharge completed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data or insufficient funds"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<Result<BalanceResponse>> rechargeUser(
            @Parameter(description = "User ID", required = true, example = "1")
            @PathVariable Long userId, 
            @Valid @RequestBody RechargeRequest request) {
        logger.info("Processing recharge for user {}: amount={}", userId, request.getAmount());
        
        // Recharge - validation is handled by @Valid annotation
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
        return ResponseEntity.ok(Result.successWithMessage("Recharge completed successfully", response));
    }
    
    /**
     * Get user balance
     * GET /api/users/{userId}/balance
     */
    @GetMapping("/{userId}/balance")
    @Operation(summary = "Get User Balance", description = "Retrieve current balance for a specific user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Balance retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<Result<BalanceResponse>> getUserBalance(
            @Parameter(description = "User ID", required = true, example = "1")
            @PathVariable Long userId) {
        Money balance = userService.getUserBalance(userId);
        
        BalanceResponse response = new BalanceResponse(
            userId,
            balance.getAmount(),
            balance.getCurrency()
        );
        
        return ResponseEntity.ok(Result.success(response));
    }

    /**
     * Get all existing user IDs (for debugging - only available in dev/test environments)
     * GET /api/users/debug/all-ids
     */
    @GetMapping("/debug/all-ids")
    @Profile({"dev", "test"})
    public ResponseEntity<Result<Map<String, Object>>> getAllUserIds() {
        var userIds = userService.getAllUserIds();
        int userCount = userService.getUserCount();
        
        var response = new java.util.HashMap<String, Object>();
        response.put("totalUsers", userCount);
        response.put("existingUserIds", userIds);
        response.put("timestamp", java.time.LocalDateTime.now());
        
        logger.info("Debug: Current system has {} users with IDs: {}", userCount, userIds);
        return ResponseEntity.ok(Result.successWithMessage("Debug information retrieved successfully", response));
    }
    
    // DTO classes
    @Schema(description = "User creation request")
    public static class CreateUserRequest {
        @Schema(description = "Username", example = "john_doe", required = true)
        @NotBlank(message = "Username is required")
        private String username;
        
        @Schema(description = "Email address", example = "john.doe@example.com", required = true)
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        private String email;
        
        @Schema(description = "Phone number", example = "13800138000", required = true)
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
    
    @Schema(description = "User account recharge request")
    public static class RechargeRequest {
        @Schema(description = "Recharge amount", example = "100.00", required = true)
        @DecimalMin(value = "0.01", message = "Recharge amount must be positive")
        private BigDecimal amount;
        
        @Schema(description = "Currency code", example = "CNY", defaultValue = "CNY")
        @NotBlank(message = "Currency is required")
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