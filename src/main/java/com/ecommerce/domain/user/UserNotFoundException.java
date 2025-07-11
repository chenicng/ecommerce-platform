package com.ecommerce.domain.user;

import com.ecommerce.api.dto.ErrorCode;
import com.ecommerce.api.exception.BusinessException;

/**
 * Exception thrown when a user is not found
 */
public class UserNotFoundException extends BusinessException {
    
    public UserNotFoundException(String message) {
        super(ErrorCode.RESOURCE_NOT_FOUND, message);
    }
    
    public UserNotFoundException(Long userId) {
        super(ErrorCode.RESOURCE_NOT_FOUND, "User not found with id: " + userId);
    }
} 