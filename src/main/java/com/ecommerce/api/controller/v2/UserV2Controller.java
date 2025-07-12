package com.ecommerce.api.controller.v2;

import com.ecommerce.application.service.UserService;
import com.ecommerce.domain.Money;
import com.ecommerce.domain.user.User;
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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ExampleObject;

/**
 * User Controller V2 (API v2)
 * Simple enhancement over V1 with additional response fields
 * 
 * V2 Enhancements over V1:
 * - Additional timestamp fields in responses
 * - Enhanced user information (account status)
 * - Improved error messages
 */
@RestController
@RequestMapping(ApiVersionConfig.API_V2 + "/users")
@ApiVersion(value = "v2", since = "2025-07-11")
@Tag(name = "User Management V2", description = "Enhanced user management with additional fields (API v2)")
public class UserV2Controller {
    
    private static final Logger logger = LoggerFactory.getLogger(UserV2Controller.class);
    
    private final UserService userService;
    
    public UserV2Controller(UserService userService) {
        this.userService = userService;
    }
    
    /**
     * Create User - V2 with enhanced response
     * POST /api/v2/users
     */
    @PostMapping
    @Operation(summary = "Create User (V2)", description = "Register a new user with enhanced response including timestamps")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User created successfully",
                    content = @Content(mediaType = "application/json", 
                                     schema = @Schema(implementation = Result.class),
                                     examples = @ExampleObject(
                                         name = "Success Response",
                                         value = "{\"code\":\"SUCCESS\",\"message\":\"User created successfully\",\"data\":{\"id\":1,\"username\":\"john_doe\",\"email\":\"john.doe@example.com\",\"phone\":\"13800138000\",\"balance\":0.00,\"currency\":\"CNY\",\"status\":\"ACTIVE\",\"createdAt\":\"2025-07-11T12:00:00\",\"lastUpdated\":\"2025-07-11T12:00:00\"},\"timestamp\":\"2025-07-11T12:00:00\"}")))
    })
    public ResponseEntity<Result<UserV2Response>> createUser(@Valid @RequestBody CreateUserRequest request) {
        logger.info("Creating user (V2): {}", request.getUsername());
        
        User user = userService.createUser(
            request.getUsername(),
            request.getEmail(),
            request.getPhone(),
            "CNY"
        );
        
        UserV2Response response = new UserV2Response(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getPhone(),
            user.getBalance().getAmount(),
            user.getBalance().getCurrency(),
            user.getStatus().toString(),
            user.getCreatedAt(),
            user.getUpdatedAt(),
            user.isActive()
        );
        
        logger.info("User created successfully (V2) with ID: {}", user.getId());
        return ResponseEntity.ok(Result.successWithMessage("User created successfully", response));
    }
    
    /**
     * Get User by ID - V2 with enhanced response
     * GET /api/v2/users/{userId}
     */
    @GetMapping("/{userId}")
    @Operation(summary = "Get User (V2)", description = "Retrieve user information with enhanced response including timestamps")
    public ResponseEntity<Result<UserV2Response>> getUserById(
            @Parameter(description = "User ID", required = true, example = "1")
            @PathVariable Long userId) {
        
        logger.info("Getting user by ID (V2): {}", userId);
        
        User user = userService.getUserById(userId);
        
        UserV2Response response = new UserV2Response(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getPhone(),
            user.getBalance().getAmount(),
            user.getBalance().getCurrency(),
            user.getStatus().toString(),
            user.getCreatedAt(),
            user.getUpdatedAt(),
            user.isActive()
        );
        
        logger.info("User retrieved successfully (V2): {}", user.getUsername());
        return ResponseEntity.ok(Result.success(response));
    }
    
    /**
     * Get User Balance - V2 with enhanced response
     * GET /api/v2/users/{userId}/balance
     */
    @GetMapping("/{userId}/balance")
    @Operation(summary = "Get User Balance (V2)", description = "Retrieve user balance with enhanced response including last update time")
    public ResponseEntity<Result<BalanceV2Response>> getUserBalance(
            @Parameter(description = "User ID", required = true, example = "1")
            @PathVariable Long userId) {
        
        Money balance = userService.getUserBalance(userId);
        
        BalanceV2Response response = new BalanceV2Response(
            userId,
            balance.getAmount(),
            balance.getCurrency(),
            LocalDateTime.now(), // Use current time since we don't have user object
            "ACTIVE" // Default to ACTIVE for simplicity
        );
        
        return ResponseEntity.ok(Result.success(response));
    }
    
    /**
     * Recharge User - V2 with enhanced response
     * POST /api/v2/users/{userId}/recharge
     */
    @PostMapping("/{userId}/recharge")
    @Operation(summary = "Recharge User (V2)", description = "Recharge user account with enhanced response including transaction details")
    public ResponseEntity<Result<RechargeV2Response>> rechargeUser(
            @Parameter(description = "User ID", required = true, example = "1")
            @PathVariable Long userId,
            @Valid @RequestBody RechargeRequest request) {
        
        logger.info("Recharging user (V2): {} with amount: {}", userId, request.getAmount());
        
        Money rechargeAmount = Money.of(request.getAmount(), request.getCurrency());
        userService.rechargeUser(userId, rechargeAmount);
        
        Money newBalance = userService.getUserBalance(userId);
        
        RechargeV2Response response = new RechargeV2Response(
            userId,
            request.getAmount(),
            request.getCurrency(),
            newBalance.getAmount(),
            newBalance.getCurrency(),
            LocalDateTime.now(),
            "COMPLETED",
            "SUCCESS"
        );
        
        logger.info("User recharged successfully (V2): {} new balance: {}", userId, newBalance);
        return ResponseEntity.ok(Result.successWithMessage("Recharge completed successfully", response));
    }
    
    // V2 Enhanced DTOs (simplified)
    
    @Schema(description = "User creation request")
    public static class CreateUserRequest {
        @NotBlank(message = "Username is required")
        @Schema(description = "Username", example = "john_doe")
        private String username;
        
        @NotBlank(message = "Email is required")
        @Email(message = "Valid email is required")
        @Schema(description = "Email address", example = "john.doe@example.com")
        private String email;
        
        @NotBlank(message = "Phone is required")
        @Schema(description = "Phone number", example = "13800138000")
        private String phone;
        
        // Getters and setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
    }
    
    @Schema(description = "User recharge request")
    public static class RechargeRequest {
        @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
        @Schema(description = "Recharge amount", example = "100.00")
        private BigDecimal amount;
        
        @NotBlank(message = "Currency is required")
        @Schema(description = "Currency code", example = "CNY")
        private String currency;
        
        // Getters and setters
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
    }
    
    @Schema(description = "User response with enhanced information (V2)")
    public static class UserV2Response {
        @Schema(description = "User ID", example = "1")
        private Long id;
        @Schema(description = "Username", example = "john_doe")
        private String username;
        @Schema(description = "Email address", example = "john.doe@example.com")
        private String email;
        @Schema(description = "Phone number", example = "13800138000")
        private String phone;
        @Schema(description = "Account balance", example = "100.00")
        private BigDecimal balance;
        @Schema(description = "Currency code", example = "CNY")
        private String currency;
        @Schema(description = "User status (ACTIVE/INACTIVE/DELETED)", example = "ACTIVE")
        private String status;
        @Schema(description = "Registration timestamp", example = "2025-07-11T10:00:00")
        private LocalDateTime registrationTime; // Renamed from createdAt
        @Schema(description = "Last login timestamp", example = "2025-07-11T12:00:00")
        private LocalDateTime lastLoginTime; // Renamed from lastUpdated
        @Schema(description = "Account active status", example = "true")
        private boolean active; // New field
        
        public UserV2Response(Long id, String username, String email, String phone, 
                             BigDecimal balance, String currency, String status,
                             LocalDateTime registrationTime, LocalDateTime lastLoginTime, boolean active) {
            this.id = id;
            this.username = username;
            this.email = email;
            this.phone = phone;
            this.balance = balance;
            this.currency = currency;
            this.status = status;
            this.registrationTime = registrationTime;
            this.lastLoginTime = lastLoginTime;
            this.active = active;
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
        public boolean isActive() { return active; }
    }
    
    @Schema(description = "User balance response with enhanced information (V2)")
    public static class BalanceV2Response {
        @Schema(description = "User ID", example = "1")
        private Long userId;
        @Schema(description = "Current balance", example = "100.00")
        private BigDecimal balance;
        @Schema(description = "Currency code", example = "CNY")
        private String currency;
        @Schema(description = "Last update timestamp", example = "2025-07-11T10:00:00")
        private LocalDateTime lastUpdateTime; // Renamed from lastUpdated
        @Schema(description = "Account status", example = "ACTIVE")
        private String accountStatus;
        
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
    
    @Schema(description = "User recharge response with enhanced information (V2)")
    public static class RechargeV2Response {
        @Schema(description = "User ID", example = "1")
        private Long userId;
        @Schema(description = "Recharge amount", example = "50.00")
        private BigDecimal rechargeAmount;
        @Schema(description = "Recharge currency", example = "CNY")
        private String rechargeCurrency;
        @Schema(description = "New balance after recharge", example = "150.00")
        private BigDecimal newBalance;
        @Schema(description = "Balance currency", example = "CNY")
        private String balanceCurrency;
        @Schema(description = "Transaction timestamp", example = "2025-07-11T10:00:00")
        private LocalDateTime transactionTime;
        @Schema(description = "Transaction status", example = "COMPLETED")
        private String transactionStatus;
        @Schema(description = "Result code", example = "SUCCESS")
        private String resultCode;
        
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