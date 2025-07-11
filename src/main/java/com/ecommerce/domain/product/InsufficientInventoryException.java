package com.ecommerce.domain.product;

import com.ecommerce.api.dto.ErrorCode;
import com.ecommerce.api.exception.BusinessException;

/**
 * Insufficient Inventory Exception
 */
public class InsufficientInventoryException extends BusinessException {
    
    public InsufficientInventoryException(String message) {
        super(ErrorCode.INSUFFICIENT_INVENTORY, message);
    }
    
    public InsufficientInventoryException(String message, Throwable cause) {
        super(ErrorCode.INSUFFICIENT_INVENTORY, message, cause);
    }
} 