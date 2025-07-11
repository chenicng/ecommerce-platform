package com.ecommerce.domain.user;

import com.ecommerce.api.dto.ErrorCode;
import com.ecommerce.api.exception.BusinessException;

/**
 * Insufficient Balance Exception
 */
public class InsufficientBalanceException extends BusinessException {
    
    public InsufficientBalanceException(String message) {
        super(ErrorCode.INSUFFICIENT_BALANCE, message);
    }
    
    public InsufficientBalanceException(String message, Throwable cause) {
        super(ErrorCode.INSUFFICIENT_BALANCE, message, cause);
    }
} 