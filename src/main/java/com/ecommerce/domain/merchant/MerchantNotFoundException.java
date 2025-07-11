package com.ecommerce.domain.merchant;

/**
 * Exception thrown when a merchant is not found
 */
public class MerchantNotFoundException extends RuntimeException {
    
    public MerchantNotFoundException(String message) {
        super(message);
    }
    
    public MerchantNotFoundException(Long merchantId) {
        super("Merchant not found with id: " + merchantId);
    }
} 