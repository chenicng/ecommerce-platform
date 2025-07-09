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
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));

        verify(userService).createUser("testuser", "test@example.com", "13800138000", "CNY");
    }

    @Test
    void createUser_ValidationError() throws Exception {
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
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.balance").value(1500.00));

        verify(userService).rechargeUser(eq(1L), any(Money.class));
    }

    @Test
    void getUserBalance_Success() throws Exception {
        // Given
        Money balance = Money.of("1000.00", "CNY");
        when(userService.getUserBalance(1L)).thenReturn(balance);

        // When & Then
        mockMvc.perform(get(API_BASE_PATH + "/{userId}/balance", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.balance").value(1000.00));

        verify(userService).getUserBalance(1L);
    }

    @Test
    void getAllUserIds_Success() throws Exception {
        // Given
        when(userService.getAllUserIds()).thenReturn(Set.of(1L, 2L, 3L));
        when(userService.getUserCount()).thenReturn(3);

        // When & Then
        mockMvc.perform(get(API_BASE_PATH + "/debug/all-ids"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers").value(3));

        verify(userService).getAllUserIds();
        verify(userService).getUserCount();
    }
}
