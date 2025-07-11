package com.ecommerce.domain.merchant;

import com.ecommerce.api.dto.ErrorCode;
import com.ecommerce.api.exception.BusinessException;

/**
 * Exception thrown when a merchant is not found
 */
public class MerchantNotFoundException extends BusinessException {
    
    public MerchantNotFoundException(String message) {
        super(ErrorCode.MERCHANT_NOT_FOUND, message);
    }
    
    public MerchantNotFoundException(Long merchantId) {
        super(ErrorCode.MERCHANT_NOT_FOUND, "Merchant not found with id: " + merchantId);
    }
} 