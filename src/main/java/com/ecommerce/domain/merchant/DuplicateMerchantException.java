package com.ecommerce.domain.merchant;

/**
 * Exception thrown when attempting to create a merchant with duplicate unique fields
 */
public class DuplicateMerchantException extends RuntimeException {
    
    public DuplicateMerchantException(String message) {
        super(message);
    }
    
    public DuplicateMerchantException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public static DuplicateMerchantException forBusinessLicense(String businessLicense) {
        return new DuplicateMerchantException("Merchant with business license '" + businessLicense + "' already exists");
    }
    
    public static DuplicateMerchantException forContactEmail(String contactEmail) {
        return new DuplicateMerchantException("Merchant with contact email '" + contactEmail + "' already exists");
    }
} 