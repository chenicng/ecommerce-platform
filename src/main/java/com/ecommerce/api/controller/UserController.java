package com.ecommerce.api.controller;

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
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.Valid;
import org.springframework.context.annotation.Profile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import com.ecommerce.api.dto.ErrorResponse;

import java.math.BigDecimal;
import java.util.Map;
import jakarta.validation.constraints.NotNull;

/**
 * User Controller (API v1)
 * Handles user registration, account management, and balance operations
 */
@RestController
@RequestMapping(ApiVersionConfig.API_V1 + "/users")
@ApiVersion(value = "v1", since = "2025-07-11")
@Tag(name = "User Management", description = "User registration, authentication and account management")
public class UserController {
    
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    
    private final UserService userService;
    
    public UserController(UserService userService) {
        this.userService = userService;
    }
    
    /**
     * Create User (API v1)
     * POST /api/v1/users
     */
    @PostMapping
    @Operation(summary = "Create User", description = "Register a new user with username, email, and phone")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User created successfully",
                    content = @Content(mediaType = "application/json", 
                                     schema = @Schema(implementation = Result.class),
                                     examples = @ExampleObject(
                                         name = "Success Response",
                                         value = "{\"code\":\"SUCCESS\",\"message\":\"User created successfully\",\"data\":{\"id\":1,\"username\":\"john_doe\",\"email\":\"john.doe@example.com\",\"phone\":\"13800138000\",\"balance\":0.00,\"currency\":\"CNY\",\"status\":\"ACTIVE\"},\"timestamp\":\"2025-07-11T12:00:00\"}"))),
        @ApiResponse(responseCode = "400", description = "Invalid request data",
                    content = @Content(mediaType = "application/json", 
                                     schema = @Schema(implementation = ErrorResponse.class),
                                     examples = @ExampleObject(
                                         name = "Validation Error",
                                         value = "{\"code\":\"VALIDATION_ERROR\",\"message\":\"Username is required\",\"data\":null,\"timestamp\":\"2025-07-11T12:00:00\"}"))),
        @ApiResponse(responseCode = "409", description = "User already exists",
                    content = @Content(mediaType = "application/json", 
                                     schema = @Schema(implementation = ErrorResponse.class),
                                     examples = @ExampleObject(
                                         name = "User Already Exists",
                                         value = "{\"code\":\"RESOURCE_ALREADY_EXISTS\",\"message\":\"User already exists\",\"data\":null,\"timestamp\":\"2025-07-11T12:00:00\"}"))),
        @ApiResponse(responseCode = "413", description = "Request too large",
                    content = @Content(mediaType = "application/json", 
                                     schema = @Schema(implementation = ErrorResponse.class),
                                     examples = @ExampleObject(
                                         name = "Request Too Large",
                                         value = "{\"code\":\"INPUT_TOO_LARGE\",\"message\":\"Request too large\",\"data\":null,\"timestamp\":\"2025-07-11T12:00:00\"}")))
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
     * Get user by ID
     * GET /api/v1/users/{userId}
     */
    @GetMapping("/{userId}")
    @Operation(summary = "Get User", description = "Retrieve user information by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User retrieved successfully",
                    content = @Content(mediaType = "application/json", 
                                     schema = @Schema(implementation = Result.class),
                                     examples = @ExampleObject(
                                         name = "Success Response",
                                         value = "{\"code\":\"SUCCESS\",\"message\":\"Operation completed successfully\",\"data\":{\"id\":1,\"username\":\"john_doe\",\"email\":\"john.doe@example.com\",\"phone\":\"13800138000\",\"balance\":100.00,\"currency\":\"CNY\",\"status\":\"ACTIVE\"},\"timestamp\":\"2025-07-11T12:00:00\"}"))),
        @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = "application/json", 
                                     schema = @Schema(implementation = ErrorResponse.class),
                                     examples = @ExampleObject(
                                         name = "User Not Found",
                                         value = "{\"code\":\"RESOURCE_NOT_FOUND\",\"message\":\"User not found\",\"data\":null,\"timestamp\":\"2025-07-11T12:00:00\"}")))
    })
    public ResponseEntity<Result<UserResponse>> getUserById(
            @Parameter(description = "User ID", required = true, example = "1")
            @PathVariable Long userId) {
        logger.info("Getting user by ID: {}", userId);
        
        User user = userService.getUserById(userId);
        
        UserResponse response = new UserResponse(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getPhone(),
            user.getBalance().getAmount(),
            user.getBalance().getCurrency(),
            user.getStatus().toString()
        );
        
        return ResponseEntity.ok(Result.success(response));
    }
    
    /**
     * Recharge User Account (API v1)
     * POST /api/v1/users/{userId}/recharge
     */
    @PostMapping("/{userId}/recharge")
    @Operation(summary = "Recharge User Account", description = "Add funds to user's account balance")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Recharge completed successfully",
                    content = @Content(mediaType = "application/json", 
                                     schema = @Schema(implementation = Result.class),
                                     examples = @ExampleObject(
                                         name = "Success Response",
                                         value = "{\"code\":\"SUCCESS\",\"message\":\"Recharge completed successfully\",\"data\":{\"userId\":1,\"balance\":200.00,\"currency\":\"CNY\"},\"timestamp\":\"2025-07-11T12:00:00\"}"))),
        @ApiResponse(responseCode = "400", description = "Invalid request data or insufficient funds",
                    content = @Content(mediaType = "application/json", 
                                     schema = @Schema(implementation = ErrorResponse.class),
                                     examples = @ExampleObject(
                                         name = "Validation Error",
                                         value = "{\"code\":\"VALIDATION_ERROR\",\"message\":\"Recharge amount must be positive\",\"data\":null,\"timestamp\":\"2025-07-11T12:00:00\"}"))),
        @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = "application/json", 
                                     schema = @Schema(implementation = ErrorResponse.class),
                                     examples = @ExampleObject(
                                         name = "User Not Found",
                                         value = "{\"code\":\"RESOURCE_NOT_FOUND\",\"message\":\"User not found\",\"data\":null,\"timestamp\":\"2025-07-11T12:00:00\"}")))
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
     * Get User Balance (API v1)
     * GET /api/v1/users/{userId}/balance
     */
    @GetMapping("/{userId}/balance")
    @Operation(summary = "Get User Balance", description = "Retrieve current balance for a specific user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Balance retrieved successfully",
                    content = @Content(mediaType = "application/json", 
                                     schema = @Schema(implementation = Result.class),
                                     examples = @ExampleObject(
                                         name = "Success Response",
                                         value = "{\"code\":\"SUCCESS\",\"message\":\"Operation completed successfully\",\"data\":{\"userId\":1,\"balance\":100.00,\"currency\":\"CNY\"},\"timestamp\":\"2025-07-11T12:00:00\"}"))),
        @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = "application/json", 
                                     schema = @Schema(implementation = ErrorResponse.class),
                                     examples = @ExampleObject(
                                         name = "User Not Found",
                                         value = "{\"code\":\"RESOURCE_NOT_FOUND\",\"message\":\"User not found\",\"data\":null,\"timestamp\":\"2025-07-11T12:00:00\"}")))
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
     * Get All User IDs (Debug) (API v1)
     * GET /api/v1/users/debug/all-ids
     * 
     * Debug endpoint - only available in dev/test environments
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
        @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
        @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers and underscores")
        private String username;
        
        @Schema(description = "Email address", example = "john.doe@example.com", required = true)
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        @Size(max = 100, message = "Email must not exceed 100 characters")
        private String email;
        
        @Schema(description = "Phone number", example = "13800138000", required = true)
        @NotBlank(message = "Phone is required")
        @Pattern(regexp = "^1[3-9]\\d{9}$", message = "Invalid phone number format")
        @Size(max = 20, message = "Phone number must not exceed 20 characters")
        private String phone;
        
        // Getters and Setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
    }
    
    @Schema(description = "User information response")
    public static class UserResponse {
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
        
        @Schema(description = "User status", example = "ACTIVE")
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
        @NotNull(message = "Recharge amount is required")
        @DecimalMin(value = "0.01", message = "Recharge amount must be positive")
        @DecimalMax(value = "999999.99", message = "Recharge amount must not exceed 999999.99")
        private BigDecimal amount;
        
        @Schema(description = "Currency code", example = "CNY", required = true)
        @NotBlank(message = "Currency is required")
        @Size(min = 3, max = 3, message = "Currency code must be exactly 3 characters")
        @Pattern(regexp = "^[A-Z]{3}$", message = "Currency code must be 3 uppercase letters")
        private String currency;
        
        // Getters and Setters
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
    }
    
    @Schema(description = "User account recharge response")
    public static class RechargeResponse {
        @Schema(description = "User ID", example = "1")
        private Long userId;
        
        @Schema(description = "Recharged amount", example = "100.00")
        private BigDecimal rechargeAmount;
        
        @Schema(description = "Recharge currency", example = "CNY")
        private String rechargeCurrency;
        
        @Schema(description = "New account balance", example = "200.00")
        private BigDecimal newBalance;
        
        @Schema(description = "Balance currency", example = "CNY")
        private String balanceCurrency;
        
        public RechargeResponse() {}
        
        public RechargeResponse(Long userId, BigDecimal rechargeAmount, String rechargeCurrency,
                              BigDecimal newBalance, String balanceCurrency) {
            this.userId = userId;
            this.rechargeAmount = rechargeAmount;
            this.rechargeCurrency = rechargeCurrency;
            this.newBalance = newBalance;
            this.balanceCurrency = balanceCurrency;
        }
        
        // Getters and Setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public BigDecimal getRechargeAmount() { return rechargeAmount; }
        public String getRechargeCurrency() { return rechargeCurrency; }
        public BigDecimal getNewBalance() { return newBalance; }
        public String getBalanceCurrency() { return balanceCurrency; }
    }
    
    @Schema(description = "User balance response")
    public static class BalanceResponse {
        @Schema(description = "User ID", example = "1")
        private Long userId;
        
        @Schema(description = "Current balance", example = "100.00")
        private BigDecimal balance;
        
        @Schema(description = "Currency code", example = "CNY")
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
    

} 