package com.ecommerce.api.controller.v2;

import com.ecommerce.application.service.UserService;
import com.ecommerce.domain.Money;
import com.ecommerce.domain.user.User;
import com.ecommerce.domain.user.UserStatus;
import com.ecommerce.api.exception.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for UserV2Controller
 * Tests all endpoints and validates V2 enhanced features
 */
@ExtendWith(MockitoExtension.class)
class UserV2ControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserV2Controller userV2Controller;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userV2Controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void createUser_ValidRequest_ShouldReturnUserV2Response() throws Exception {
        // Arrange
        UserV2Controller.CreateUserRequest request = new UserV2Controller.CreateUserRequest();
        request.setUsername("testuser");
        request.setEmail("test@example.com");
        request.setPhone("1234567890");

        User user = new User("testuser", "test@example.com", "1234567890", "CNY");
        user.setId(1L);

        when(userService.createUser(eq("testuser"), eq("test@example.com"), eq("1234567890"), eq("CNY")))
                .thenReturn(user);

        // Act & Assert
        mockMvc.perform(post("/api/v2/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.phone").value("1234567890"))
                .andExpect(jsonPath("$.balance").value(0))
                .andExpect(jsonPath("$.currency").value("CNY"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.registrationTime").exists())
                .andExpect(jsonPath("$.lastLoginTime").exists())
                .andExpect(jsonPath("$.active").value(true));

        verify(userService).createUser("testuser", "test@example.com", "1234567890", "CNY");
    }

    @Test
    void createUser_InvalidUsername_ShouldReturn400() throws Exception {
        // Arrange
        UserV2Controller.CreateUserRequest request = new UserV2Controller.CreateUserRequest();
        request.setUsername(""); // Invalid: blank username
        request.setEmail("test@example.com");
        request.setPhone("1234567890");

        // Act & Assert
        mockMvc.perform(post("/api/v2/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).createUser(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void createUser_InvalidEmail_ShouldReturn400() throws Exception {
        // Arrange
        UserV2Controller.CreateUserRequest request = new UserV2Controller.CreateUserRequest();
        request.setUsername("testuser");
        request.setEmail("invalid-email"); // Invalid email format
        request.setPhone("1234567890");

        // Act & Assert
        mockMvc.perform(post("/api/v2/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).createUser(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void createUser_BlankPhone_ShouldReturn400() throws Exception {
        // Arrange
        UserV2Controller.CreateUserRequest request = new UserV2Controller.CreateUserRequest();
        request.setUsername("testuser");
        request.setEmail("test@example.com");
        request.setPhone(""); // Invalid: blank phone

        // Act & Assert
        mockMvc.perform(post("/api/v2/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).createUser(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void getUserBalance_ValidUserId_ShouldReturnBalanceV2Response() throws Exception {
        // Arrange
        Long userId = 1L;
        Money balance = Money.of(new BigDecimal("100.50"), "CNY");

        when(userService.getUserBalance(userId)).thenReturn(balance);

        // Act & Assert
        mockMvc.perform(get("/api/v2/users/{userId}/balance", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.balance").value(100.50))
                .andExpect(jsonPath("$.currency").value("CNY"))
                .andExpect(jsonPath("$.lastUpdateTime").exists())
                .andExpect(jsonPath("$.accountStatus").value("ACTIVE"));

        verify(userService).getUserBalance(userId);
    }

    @Test
    void getUserBalance_UserNotFound_ShouldReturn404() throws Exception {
        // Arrange
        Long userId = 999L;

        when(userService.getUserBalance(userId)).thenThrow(new RuntimeException("User not found"));

        // Act & Assert
        mockMvc.perform(get("/api/v2/users/{userId}/balance", userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("User not found"));

        verify(userService).getUserBalance(userId);
    }

    @Test
    void rechargeUser_ValidRequest_ShouldReturnRechargeV2Response() throws Exception {
        // Arrange
        Long userId = 1L;
        UserV2Controller.RechargeRequest request = new UserV2Controller.RechargeRequest();
        request.setAmount(new BigDecimal("50.00"));
        request.setCurrency("CNY");

        Money balanceAfterRecharge = Money.of(new BigDecimal("150.50"), "CNY");

        when(userService.getUserBalance(userId)).thenReturn(balanceAfterRecharge);

        // Act & Assert
        mockMvc.perform(post("/api/v2/users/{userId}/recharge", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.rechargeAmount").value(50.00))
                .andExpect(jsonPath("$.rechargeCurrency").value("CNY"))
                .andExpect(jsonPath("$.newBalance").value(150.50))
                .andExpect(jsonPath("$.balanceCurrency").value("CNY"))
                .andExpect(jsonPath("$.transactionTime").exists())
                .andExpect(jsonPath("$.transactionStatus").value("COMPLETED"))
                .andExpect(jsonPath("$.resultCode").value("SUCCESS"));

        verify(userService).rechargeUser(eq(userId), any(Money.class));
        verify(userService).getUserBalance(userId);
    }

    @Test
    void rechargeUser_InvalidAmount_ShouldReturn400() throws Exception {
        // Arrange
        Long userId = 1L;
        UserV2Controller.RechargeRequest request = new UserV2Controller.RechargeRequest();
        request.setAmount(new BigDecimal("-10.00")); // Invalid: negative amount
        request.setCurrency("CNY");

        // Act & Assert
        mockMvc.perform(post("/api/v2/users/{userId}/recharge", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).rechargeUser(anyLong(), any(Money.class));
    }

    @Test
    void rechargeUser_ZeroAmount_ShouldReturn400() throws Exception {
        // Arrange
        Long userId = 1L;
        UserV2Controller.RechargeRequest request = new UserV2Controller.RechargeRequest();
        request.setAmount(BigDecimal.ZERO); // Invalid: zero amount
        request.setCurrency("CNY");

        // Act & Assert
        mockMvc.perform(post("/api/v2/users/{userId}/recharge", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).rechargeUser(anyLong(), any(Money.class));
    }

    @Test
    void rechargeUser_BlankCurrency_ShouldReturn400() throws Exception {
        // Arrange
        Long userId = 1L;
        UserV2Controller.RechargeRequest request = new UserV2Controller.RechargeRequest();
        request.setAmount(new BigDecimal("50.00"));
        request.setCurrency(""); // Invalid: blank currency

        // Act & Assert
        mockMvc.perform(post("/api/v2/users/{userId}/recharge", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).rechargeUser(anyLong(), any(Money.class));
    }

    @Test
    void rechargeUser_UserNotFound_ShouldReturn404() throws Exception {
        // Arrange
        Long userId = 999L;
        UserV2Controller.RechargeRequest request = new UserV2Controller.RechargeRequest();
        request.setAmount(new BigDecimal("50.00"));
        request.setCurrency("CNY");

        doThrow(new RuntimeException("User not found")).when(userService).rechargeUser(anyLong(), any(Money.class));

        // Act & Assert
        mockMvc.perform(post("/api/v2/users/{userId}/recharge", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("User not found"));

        verify(userService).rechargeUser(eq(userId), any(Money.class));
    }

    // Test DTO classes coverage
    @Test
    void createUserRequest_GettersAndSetters_ShouldWork() {
        // Arrange & Act
        UserV2Controller.CreateUserRequest request = new UserV2Controller.CreateUserRequest();
        request.setUsername("test");
        request.setEmail("test@example.com");
        request.setPhone("123456789");

        // Assert
        assertEquals("test", request.getUsername());
        assertEquals("test@example.com", request.getEmail());
        assertEquals("123456789", request.getPhone());
    }

    @Test
    void userV2Response_GettersAndConstructor_ShouldWork() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        
        // Act
        UserV2Controller.UserV2Response response = new UserV2Controller.UserV2Response(
                1L, "test", "test@example.com", "123456789",
                new BigDecimal("100.00"), "CNY", "ACTIVE",
                now, now, true
        );

        // Assert
        assertEquals(1L, response.getId());
        assertEquals("test", response.getUsername());
        assertEquals("test@example.com", response.getEmail());
        assertEquals("123456789", response.getPhone());
        assertEquals(new BigDecimal("100.00"), response.getBalance());
        assertEquals("CNY", response.getCurrency());
        assertEquals("ACTIVE", response.getStatus());
        assertEquals(now, response.getRegistrationTime());
        assertEquals(now, response.getLastLoginTime());
        assertTrue(response.isActive());
    }

    @Test
    void balanceV2Response_GettersAndConstructor_ShouldWork() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        
        // Act
        UserV2Controller.BalanceV2Response response = new UserV2Controller.BalanceV2Response(
                1L, new BigDecimal("100.00"), "CNY", now, "ACTIVE"
        );

        // Assert
        assertEquals(1L, response.getUserId());
        assertEquals(new BigDecimal("100.00"), response.getBalance());
        assertEquals("CNY", response.getCurrency());
        assertEquals(now, response.getLastUpdateTime());
        assertEquals("ACTIVE", response.getAccountStatus());
    }

    @Test
    void rechargeRequest_GettersAndSetters_ShouldWork() {
        // Arrange & Act
        UserV2Controller.RechargeRequest request = new UserV2Controller.RechargeRequest();
        request.setAmount(new BigDecimal("50.00"));
        request.setCurrency("USD");

        // Assert
        assertEquals(new BigDecimal("50.00"), request.getAmount());
        assertEquals("USD", request.getCurrency());
    }

    @Test
    void rechargeV2Response_GettersAndConstructor_ShouldWork() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        
        // Act
        UserV2Controller.RechargeV2Response response = new UserV2Controller.RechargeV2Response(
                1L, new BigDecimal("50.00"), "CNY",
                new BigDecimal("150.00"), "CNY",
                now, "COMPLETED", "SUCCESS"
        );

        // Assert
        assertEquals(1L, response.getUserId());
        assertEquals(new BigDecimal("50.00"), response.getRechargeAmount());
        assertEquals("CNY", response.getRechargeCurrency());
        assertEquals(new BigDecimal("150.00"), response.getNewBalance());
        assertEquals("CNY", response.getBalanceCurrency());
        assertEquals(now, response.getTransactionTime());
        assertEquals("COMPLETED", response.getTransactionStatus());
        assertEquals("SUCCESS", response.getResultCode());
    }
} 