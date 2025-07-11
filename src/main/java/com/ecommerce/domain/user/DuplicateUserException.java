package com.ecommerce.domain.user;

/**
 * Exception thrown when attempting to create a user with duplicate unique fields
 */
public class DuplicateUserException extends RuntimeException {
    
    public DuplicateUserException(String message) {
        super(message);
    }
    
    public DuplicateUserException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public static DuplicateUserException forPhone(String phone) {
        return new DuplicateUserException("User with phone number '" + phone + "' already exists");
    }
    
    public static DuplicateUserException forEmail(String email) {
        return new DuplicateUserException("User with email '" + email + "' already exists");
    }
} 