package com.ecommerce.api.controller;

import com.ecommerce.application.service.UserService;
import com.ecommerce.domain.Money;
import com.ecommerce.domain.user.User;
import com.ecommerce.api.config.ApiVersionConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@WebMvcTest(UserController.class)
class UserControllerTest {

    private static final String API_BASE_PATH = ApiVersionConfig.API_V1 + "/users";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private UserController.CreateUserRequest createUserRequest;
    private UserController.RechargeRequest rechargeRequest;

    @BeforeEach
    void setUp() {
        // Setup test user
        testUser = new User(
            "testuser",
            "test@example.com",
            "13800138000",
            "CNY"
        );

        // Setup create user request
        createUserRequest = new UserController.CreateUserRequest();
        createUserRequest.setUsername("testuser");
        createUserRequest.setEmail("test@example.com");
        createUserRequest.setPhone("13800138000");

        // Setup recharge request
        rechargeRequest = new UserController.RechargeRequest();
        rechargeRequest.setAmount(new BigDecimal("500.00"));
        rechargeRequest.setCurrency("CNY");
    }

    @Test
    void createUser_Success() throws Exception {
        // Given
        when(userService.createUser("testuser", "test@example.com", "13800138000", "CNY"))
            .thenReturn(testUser);

        // When & Then
        mockMvc.perform(post(API_BASE_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createUserRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("testuser"))
                .andExpect(jsonPath("$.data.email").value("test@example.com"));

        verify(userService).createUser("testuser", "test@example.com", "13800138000", "CNY");
    }

    @Test
    void createUser_ValidationError_MissingUsername() throws Exception {
        // Given
        UserController.CreateUserRequest invalidRequest = new UserController.CreateUserRequest();
        invalidRequest.setEmail("test@example.com");
        invalidRequest.setPhone("13800138000");

        // When & Then
        mockMvc.perform(post(API_BASE_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).createUser(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void createUser_ValidationError_MissingEmail() throws Exception {
        // Given
        UserController.CreateUserRequest invalidRequest = new UserController.CreateUserRequest();
        invalidRequest.setUsername("testuser");
        invalidRequest.setPhone("13800138000");

        // When & Then
        mockMvc.perform(post(API_BASE_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).createUser(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void createUser_ValidationError_MissingPhone() throws Exception {
        // Given
        UserController.CreateUserRequest invalidRequest = new UserController.CreateUserRequest();
        invalidRequest.setUsername("testuser");
        invalidRequest.setEmail("test@example.com");

        // When & Then
        mockMvc.perform(post(API_BASE_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).createUser(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void createUser_ValidationError_EmptyFields() throws Exception {
        // Given
        UserController.CreateUserRequest invalidRequest = new UserController.CreateUserRequest();
        invalidRequest.setUsername("");
        invalidRequest.setEmail("");
        invalidRequest.setPhone("");

        // When & Then
        mockMvc.perform(post(API_BASE_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).createUser(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void createUser_ValidationError_InvalidEmail() throws Exception {
        // Given
        UserController.CreateUserRequest invalidRequest = new UserController.CreateUserRequest();
        invalidRequest.setUsername("testuser");
        invalidRequest.setEmail("invalid-email");
        invalidRequest.setPhone("13800138000");

        // When & Then
        mockMvc.perform(post(API_BASE_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).createUser(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void createUser_ServiceException() throws Exception {
        // Given
        when(userService.createUser("testuser", "test@example.com", "13800138000", "CNY"))
            .thenThrow(new RuntimeException("User creation failed"));

        // When & Then
        mockMvc.perform(post(API_BASE_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createUserRequest)))
                .andExpect(status().isInternalServerError());

        verify(userService).createUser("testuser", "test@example.com", "13800138000", "CNY");
    }

    @Test
    void rechargeUser_Success() throws Exception {
        // Given
        Money newBalance = Money.of("1500.00", "CNY");
        when(userService.getUserBalance(1L)).thenReturn(newBalance);
        doNothing().when(userService).rechargeUser(eq(1L), any(Money.class));

        // When & Then
        mockMvc.perform(post(API_BASE_PATH + "/{userId}/recharge", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(rechargeRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value(1))
                .andExpect(jsonPath("$.data.balance").value(1500.00));

        verify(userService).rechargeUser(eq(1L), any(Money.class));
    }

    @Test
    void rechargeUser_ValidationError_NegativeAmount() throws Exception {
        // Given
        UserController.RechargeRequest invalidRequest = new UserController.RechargeRequest();
        invalidRequest.setAmount(new BigDecimal("-100.00"));
        invalidRequest.setCurrency("CNY");

        // When & Then
        mockMvc.perform(post(API_BASE_PATH + "/{userId}/recharge", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).rechargeUser(anyLong(), any(Money.class));
    }

    @Test
    void rechargeUser_ValidationError_ZeroAmount() throws Exception {
        // Given
        UserController.RechargeRequest invalidRequest = new UserController.RechargeRequest();
        invalidRequest.setAmount(BigDecimal.ZERO);
        invalidRequest.setCurrency("CNY");

        // When & Then
        mockMvc.perform(post(API_BASE_PATH + "/{userId}/recharge", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).rechargeUser(anyLong(), any(Money.class));
    }

    @Test
    void rechargeUser_ValidationError_MissingAmount() throws Exception {
        // Given
        UserController.RechargeRequest invalidRequest = new UserController.RechargeRequest();
        invalidRequest.setCurrency("CNY");

        // When & Then
        mockMvc.perform(post(API_BASE_PATH + "/{userId}/recharge", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).rechargeUser(anyLong(), any(Money.class));
    }

    @Test
    void rechargeUser_ValidationError_MissingCurrency() throws Exception {
        // Given
        UserController.RechargeRequest invalidRequest = new UserController.RechargeRequest();
        invalidRequest.setAmount(new BigDecimal("100.00"));
        invalidRequest.setCurrency(null); // Explicitly set to null to trigger validation

        // When & Then
        mockMvc.perform(post(API_BASE_PATH + "/{userId}/recharge", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).rechargeUser(anyLong(), any(Money.class));
    }

    @Test
    void rechargeUser_ServiceException() throws Exception {
        // Given
        doThrow(new RuntimeException("Recharge failed")).when(userService).rechargeUser(eq(1L), any(Money.class));

        // When & Then
        mockMvc.perform(post(API_BASE_PATH + "/{userId}/recharge", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(rechargeRequest)))
                .andExpect(status().isInternalServerError());

        verify(userService).rechargeUser(eq(1L), any(Money.class));
    }

    @Test
    void rechargeUser_UserNotFound() throws Exception {
        // Given
        doThrow(new RuntimeException("User not found with id: 999")).when(userService).rechargeUser(eq(999L), any(Money.class));

        // When & Then
        mockMvc.perform(post(API_BASE_PATH + "/{userId}/recharge", 999L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(rechargeRequest)))
                .andExpect(status().isNotFound());

        verify(userService).rechargeUser(eq(999L), any(Money.class));
    }

    @Test
    void getUserBalance_Success() throws Exception {
        // Given
        Money balance = Money.of("1000.00", "CNY");
        when(userService.getUserBalance(1L)).thenReturn(balance);

        // When & Then
        mockMvc.perform(get(API_BASE_PATH + "/{userId}/balance", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value(1))
                .andExpect(jsonPath("$.data.balance").value(1000.00));

        verify(userService).getUserBalance(1L);
    }

    @Test
    void getUserBalance_UserNotFound() throws Exception {
        // Given
        when(userService.getUserBalance(999L)).thenThrow(new RuntimeException("User not found with id: 999"));

        // When & Then
        mockMvc.perform(get(API_BASE_PATH + "/{userId}/balance", 999L))
                .andExpect(status().isNotFound());

        verify(userService).getUserBalance(999L);
    }

    @Test
    void getAllUserIds_Success() throws Exception {
        // Given
        when(userService.getAllUserIds()).thenReturn(Set.of(1L, 2L, 3L));
        when(userService.getUserCount()).thenReturn(3);

        // When & Then
        mockMvc.perform(get(API_BASE_PATH + "/debug/all-ids"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalUsers").value(3));

        verify(userService).getAllUserIds();
        verify(userService).getUserCount();
    }

    @Test
    void getAllUserIds_EmptyList() throws Exception {
        // Given
        when(userService.getAllUserIds()).thenReturn(Set.of());
        when(userService.getUserCount()).thenReturn(0);

        // When & Then
        mockMvc.perform(get(API_BASE_PATH + "/debug/all-ids"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalUsers").value(0));

        verify(userService).getAllUserIds();
        verify(userService).getUserCount();
    }

    @Test
    void getAllUserIds_ServiceException() throws Exception {
        // Given
        when(userService.getAllUserIds()).thenThrow(new RuntimeException("Service error"));

        // When & Then
        mockMvc.perform(get(API_BASE_PATH + "/debug/all-ids"))
                .andExpect(status().isInternalServerError());

        verify(userService).getAllUserIds();
    }

    @Test
    void getUserById_Success() throws Exception {
        // Given
        Long userId = 1L;
        User mockUser = new User("alice", "alice@example.com", "13800001111", "CNY");
        mockUser.setId(userId);
        mockUser.recharge(Money.of("1000.00", "CNY"));
        
        when(userService.getUserById(userId)).thenReturn(mockUser);
        
        // When & Then
        mockMvc.perform(get("/api/v1/users/{userId}", userId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.id").value(userId))
                .andExpect(jsonPath("$.data.username").value("alice"))
                .andExpect(jsonPath("$.data.email").value("alice@example.com"))
                .andExpect(jsonPath("$.data.phone").value("13800001111"))
                .andExpect(jsonPath("$.data.balance").value(1000.00))
                .andExpect(jsonPath("$.data.currency").value("CNY"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
        
        verify(userService).getUserById(userId);
    }
    
    @Test
    void getUserById_UserNotFound() throws Exception {
        // Given
        Long userId = 999L;
        when(userService.getUserById(userId)).thenThrow(new RuntimeException("User not found with id: " + userId));
        
        // When & Then
        mockMvc.perform(get("/api/v1/users/{userId}", userId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("User not found with id: " + userId));
        
        verify(userService).getUserById(userId);
    }

    @Test
    void getUserById_ServiceException() throws Exception {
        // Given
        Long userId = 1L;
        when(userService.getUserById(userId)).thenThrow(new RuntimeException("Database connection failed"));
        
        // When & Then
        mockMvc.perform(get("/api/v1/users/{userId}", userId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
        
        verify(userService).getUserById(userId);
    }

    @Test
    void createUser_InvalidJson() throws Exception {
        // When & Then
        mockMvc.perform(post(API_BASE_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content("invalid json"))
                .andExpect(status().isBadRequest());

        verify(userService, never()).createUser(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void rechargeUser_InvalidJson() throws Exception {
        // When & Then
        mockMvc.perform(post(API_BASE_PATH + "/{userId}/recharge", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content("invalid json"))
                .andExpect(status().isBadRequest());

        verify(userService, never()).rechargeUser(anyLong(), any(Money.class));
    }

    @Test
    void testCreateUserRequest_SettersAndGetters() {
        UserController.CreateUserRequest request = new UserController.CreateUserRequest();
        
        request.setUsername("testuser");
        request.setEmail("test@example.com");
        request.setPhone("13800138000");
        
        assertEquals("testuser", request.getUsername());
        assertEquals("test@example.com", request.getEmail());
        assertEquals("13800138000", request.getPhone());
    }

    @Test
    void testRechargeRequest_SettersAndGetters() {
        UserController.RechargeRequest request = new UserController.RechargeRequest();
        
        request.setAmount(new BigDecimal("100.00"));
        request.setCurrency("USD");
        
        assertEquals(new BigDecimal("100.00"), request.getAmount());
        assertEquals("USD", request.getCurrency());
    }

    @Test
    void testUserResponse_ConstructorAndGetters() {
        UserController.UserResponse response = new UserController.UserResponse(
            1L, "testuser", "test@example.com", "13800138000", 
            new BigDecimal("1000.00"), "CNY", "ACTIVE"
        );
        
        assertEquals(1L, response.getId());
        assertEquals("testuser", response.getUsername());
        assertEquals("test@example.com", response.getEmail());
        assertEquals("13800138000", response.getPhone());
        assertEquals(new BigDecimal("1000.00"), response.getBalance());
        assertEquals("CNY", response.getCurrency());
        assertEquals("ACTIVE", response.getStatus());
    }

    @Test
    void testBalanceResponse_ConstructorAndGetters() {
        UserController.BalanceResponse response = new UserController.BalanceResponse(
            1L, new BigDecimal("1000.00"), "CNY"
        );
        
        assertEquals(1L, response.getUserId());
        assertEquals(new BigDecimal("1000.00"), response.getBalance());
        assertEquals("CNY", response.getCurrency());
    }
}
