package com.ecommerce.domain.merchant;

import com.ecommerce.api.dto.ErrorCode;
import com.ecommerce.api.exception.BusinessException;

/**
 * Exception thrown when attempting to create a merchant with duplicate unique fields
 */
public class DuplicateMerchantException extends BusinessException {
    
    public DuplicateMerchantException(String message) {
        super(ErrorCode.RESOURCE_ALREADY_EXISTS, message);
    }
    
    public DuplicateMerchantException(String message, Throwable cause) {
        super(ErrorCode.RESOURCE_ALREADY_EXISTS, message, cause);
    }
    
    public static DuplicateMerchantException forBusinessLicense(String businessLicense) {
        return new DuplicateMerchantException("Merchant with business license '" + businessLicense + "' already exists");
    }
    
    public static DuplicateMerchantException forContactEmail(String contactEmail) {
        return new DuplicateMerchantException("Merchant with contact email '" + contactEmail + "' already exists");
    }
} 