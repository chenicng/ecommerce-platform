package com.ecommerce.domain.order;

import com.ecommerce.api.dto.ErrorCode;
import com.ecommerce.api.exception.BusinessException;

/**
 * Exception thrown when an operation is attempted on an order in an invalid state
 */
public class InvalidOrderStateException extends BusinessException {
    
    public InvalidOrderStateException(String message) {
        super(ErrorCode.OPERATION_NOT_ALLOWED, message);
    }
    
    public InvalidOrderStateException(String message, Throwable cause) {
        super(ErrorCode.OPERATION_NOT_ALLOWED, message, cause);
    }
} 