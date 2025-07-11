package com.ecommerce.application.service;

import com.ecommerce.domain.user.User;
import com.ecommerce.domain.user.DuplicateUserException;
import com.ecommerce.domain.Money;
import com.ecommerce.infrastructure.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private Money testAmount;

    @BeforeEach
    void setUp() {
        testUser = new User("testuser", "test@example.com", "1234567890", "CNY");
        testAmount = Money.of(100.00, "CNY");
    }

    @Test
    void createUser_Success() {
        // Given
        when(userRepository.existsByPhone("1234567890")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        User result = userService.createUser("testuser", "test@example.com", "1234567890", "CNY");

        // Then
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("test@example.com", result.getEmail());
        assertEquals("1234567890", result.getPhone());
        verify(userRepository).existsByPhone("1234567890");
        verify(userRepository).existsByEmail("test@example.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_DuplicatePhone_ThrowsException() {
        // Given
        when(userRepository.existsByPhone("1234567890")).thenReturn(true);

        // When & Then
        DuplicateUserException exception = assertThrows(DuplicateUserException.class,
            () -> userService.createUser("testuser", "test@example.com", "1234567890", "CNY"));
        assertEquals("User with phone number '1234567890' already exists", exception.getMessage());
        verify(userRepository).existsByPhone("1234567890");
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUser_DuplicateEmail_ThrowsException() {
        // Given
        when(userRepository.existsByPhone("1234567890")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        // When & Then
        DuplicateUserException exception = assertThrows(DuplicateUserException.class,
            () -> userService.createUser("testuser", "test@example.com", "1234567890", "CNY"));
        assertEquals("User with email 'test@example.com' already exists", exception.getMessage());
        verify(userRepository).existsByPhone("1234567890");
        verify(userRepository).existsByEmail("test@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getUserById_Success() {
        // Given
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        // When
        User result = userService.getUserById(userId);

        // Then
        assertNotNull(result);
        assertEquals(testUser, result);
        verify(userRepository).findById(userId);
    }

    @Test
    void getUserById_UserNotFound() {
        // Given
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> userService.getUserById(userId));
        assertEquals("User not found with id: 999", exception.getMessage());
        verify(userRepository).findById(userId);
    }

    @Test
    void rechargeUser_Success() {
        // Given
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        userService.rechargeUser(userId, testAmount);

        // Then
        verify(userRepository).findById(userId);
        verify(userRepository).save(testUser);
    }

    @Test
    void rechargeUser_UserNotFound() {
        // Given
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> userService.rechargeUser(userId, testAmount));
        assertEquals("User not found with id: 999", exception.getMessage());
        verify(userRepository).findById(userId);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void saveUser_Success() {
        // Given
        when(userRepository.save(testUser)).thenReturn(testUser);

        // When
        userService.saveUser(testUser);

        // Then
        verify(userRepository).save(testUser);
    }

    @Test
    void getUserBalance_Success() {
        // Given
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        // When
        Money result = userService.getUserBalance(userId);

        // Then
        assertNotNull(result);
        assertEquals(testUser.getBalance(), result);
        verify(userRepository).findById(userId);
    }

    @Test
    void getUserBalance_UserNotFound() {
        // Given
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> userService.getUserBalance(userId));
        assertEquals("User not found with id: 999", exception.getMessage());
        verify(userRepository).findById(userId);
    }

    @Test
    void userExists_True() {
        // Given
        Long userId = 1L;
        when(userRepository.existsById(userId)).thenReturn(true);

        // When
        boolean result = userService.userExists(userId);

        // Then
        assertTrue(result);
        verify(userRepository).existsById(userId);
    }

    @Test
    void userExists_False() {
        // Given
        Long userId = 999L;
        when(userRepository.existsById(userId)).thenReturn(false);

        // When
        boolean result = userService.userExists(userId);

        // Then
        assertFalse(result);
        verify(userRepository).existsById(userId);
    }

    @Test
    void getAllUserIds_Success() {
        // Given
        Set<Long> userIds = Set.of(1L, 2L, 3L);
        when(userRepository.getAllUserIds()).thenReturn(userIds);

        // When
        Set<Long> result = userService.getAllUserIds();

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.contains(1L));
        assertTrue(result.contains(2L));
        assertTrue(result.contains(3L));
        verify(userRepository).getAllUserIds();
    }

    @Test
    void getUserCount_Success() {
        // Given
        when(userRepository.count()).thenReturn(5);

        // When
        int result = userService.getUserCount();

        // Then
        assertEquals(5, result);
        verify(userRepository).count();
    }
} 