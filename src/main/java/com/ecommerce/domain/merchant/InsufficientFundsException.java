package com.ecommerce.domain.merchant;

import com.ecommerce.api.dto.ErrorCode;
import com.ecommerce.api.exception.BusinessException;

/**
 * Insufficient Funds Exception
 */
public class InsufficientFundsException extends BusinessException {
    
    public InsufficientFundsException(String message) {
        super(ErrorCode.INSUFFICIENT_FUNDS, message);
    }
    
    public InsufficientFundsException(String message, Throwable cause) {
        super(ErrorCode.INSUFFICIENT_FUNDS, message, cause);
    }
} 