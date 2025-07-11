package com.ecommerce.domain;

import com.ecommerce.api.dto.ErrorCode;
import com.ecommerce.api.exception.BusinessException;

/**
 * Exception thrown when an operation is attempted on an inactive resource
 */
public class ResourceInactiveException extends BusinessException {
    
    public ResourceInactiveException(String message) {
        super(ErrorCode.RESOURCE_INACTIVE, message);
    }
    
    public ResourceInactiveException(String message, Throwable cause) {
        super(ErrorCode.RESOURCE_INACTIVE, message, cause);
    }
} 