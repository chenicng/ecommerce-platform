package com.ecommerce.infrastructure.repository.mock;

import com.ecommerce.domain.user.User;
import com.ecommerce.domain.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class MockUserRepositoryTest {

    private MockUserRepository repository;

    @BeforeEach
    void setUp() {
        repository = new MockUserRepository();
    }

    @Test
    void constructor_ShouldInitializeDemoData() {
        // Verify that demo data is initialized
        assertEquals(3, repository.count());
        assertTrue(repository.existsById(1L));
        assertTrue(repository.existsById(2L));
        assertTrue(repository.existsById(3L));
    }

    @Test
    void save_WithNewUser_ShouldAssignIdAndSave() {
        // Given
        User newUser = new User("testuser", "test@example.com", "13800004444", "CNY");
        
        // When
        User savedUser = repository.save(newUser);
        
        // Then
        assertNotNull(savedUser.getId());
        assertEquals(4L, savedUser.getId());
        assertEquals("testuser", savedUser.getUsername());
        assertEquals(4, repository.count());
    }

    @Test
    void save_WithExistingUser_ShouldKeepSameId() {
        // Given
        User existingUser = repository.findById(1L).orElse(null);
        assertNotNull(existingUser);
        
        // When
        User savedUser = repository.save(existingUser);
        
        // Then
        assertEquals(1L, savedUser.getId());
        assertEquals("alice", savedUser.getUsername());
        assertEquals(3, repository.count()); // Count should remain the same
    }

    @Test
    void findById_WithExistingId_ShouldReturnUser() {
        // When
        Optional<User> user = repository.findById(1L);
        
        // Then
        assertTrue(user.isPresent());
        assertEquals("alice", user.get().getUsername());
        assertEquals("alice@example.com", user.get().getEmail());
    }

    @Test
    void findById_WithNonExistingId_ShouldReturnEmpty() {
        // When
        Optional<User> user = repository.findById(999L);
        
        // Then
        assertFalse(user.isPresent());
    }

    @Test
    void existsById_WithExistingId_ShouldReturnTrue() {
        // When & Then
        assertTrue(repository.existsById(1L));
        assertTrue(repository.existsById(2L));
        assertTrue(repository.existsById(3L));
    }

    @Test
    void existsById_WithNonExistingId_ShouldReturnFalse() {
        // When & Then
        assertFalse(repository.existsById(999L));
    }

    @Test
    void getAllUserIds_ShouldReturnAllUserIds() {
        // When
        Set<Long> userIds = repository.getAllUserIds();
        
        // Then
        assertEquals(3, userIds.size());
        assertTrue(userIds.contains(1L));
        assertTrue(userIds.contains(2L));
        assertTrue(userIds.contains(3L));
    }

    @Test
    void count_ShouldReturnCorrectCount() {
        // When & Then
        assertEquals(3, repository.count());
    }

    @Test
    void deleteById_WithExistingId_ShouldRemoveUser() {
        // Given
        assertTrue(repository.existsById(1L));
        
        // When
        repository.deleteById(1L);
        
        // Then
        assertFalse(repository.existsById(1L));
        assertEquals(2, repository.count());
    }

    @Test
    void deleteById_WithNonExistingId_ShouldNotThrowException() {
        // When & Then
        assertDoesNotThrow(() -> repository.deleteById(999L));
        assertEquals(3, repository.count());
    }

    @Test
    void demoData_ShouldHaveCorrectInitialBalance() {
        // Check that demo users have the expected initial balance
        User alice = repository.findById(1L).orElse(null);
        User bob = repository.findById(2L).orElse(null);
        User charlie = repository.findById(3L).orElse(null);
        
        assertNotNull(alice);
        assertNotNull(bob);
        assertNotNull(charlie);
        
        assertEquals(Money.of("1000.00", "CNY"), alice.getBalance());
        assertEquals(Money.of("500.00", "CNY"), bob.getBalance());
        assertEquals(Money.of("2000.00", "CNY"), charlie.getBalance());
    }

    @Test
    void class_ShouldHaveCorrectAnnotations() {
        // Test that the class has the expected annotations
        assertTrue(MockUserRepository.class.isAnnotationPresent(org.springframework.stereotype.Repository.class));
        assertTrue(MockUserRepository.class.isAnnotationPresent(org.springframework.context.annotation.Profile.class));
    }
} 