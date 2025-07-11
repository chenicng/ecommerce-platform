package com.ecommerce.domain.user;

import com.ecommerce.api.dto.ErrorCode;
import com.ecommerce.api.exception.BusinessException;

/**
 * Exception thrown when attempting to create a user with duplicate unique fields
 */
public class DuplicateUserException extends BusinessException {
    
    public DuplicateUserException(String message) {
        super(ErrorCode.RESOURCE_ALREADY_EXISTS, message);
    }
    
    public DuplicateUserException(String message, Throwable cause) {
        super(ErrorCode.RESOURCE_ALREADY_EXISTS, message, cause);
    }
    
    public static DuplicateUserException forPhone(String phone) {
        return new DuplicateUserException("User with phone number '" + phone + "' already exists");
    }
    
    public static DuplicateUserException forEmail(String email) {
        return new DuplicateUserException("User with email '" + email + "' already exists");
    }
} 