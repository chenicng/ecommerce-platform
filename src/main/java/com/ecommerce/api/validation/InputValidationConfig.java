package com.ecommerce.api.validation;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Input Validation Configuration
 * 
 * Centralized configuration for input validation rules
 */
@Configuration
@ConfigurationProperties("security.validation")
public class InputValidationConfig {
    
    private int maxStringLength = 1000;
    private int maxRequestSize = 10 * 1024 * 1024; // 10MB
    private int maxUsernameLength = 50;
    private int maxEmailLength = 100;
    private int maxPhoneLength = 20;
    private int maxProductNameLength = 200;
    private int maxProductDescriptionLength = 1000;
    
    // Getters and Setters
    public int getMaxStringLength() {
        return maxStringLength;
    }
    
    public void setMaxStringLength(int maxStringLength) {
        this.maxStringLength = maxStringLength;
    }
    
    public int getMaxRequestSize() {
        return maxRequestSize;
    }
    
    public void setMaxRequestSize(int maxRequestSize) {
        this.maxRequestSize = maxRequestSize;
    }
    
    public int getMaxUsernameLength() {
        return maxUsernameLength;
    }
    
    public void setMaxUsernameLength(int maxUsernameLength) {
        this.maxUsernameLength = maxUsernameLength;
    }
    
    public int getMaxEmailLength() {
        return maxEmailLength;
    }
    
    public void setMaxEmailLength(int maxEmailLength) {
        this.maxEmailLength = maxEmailLength;
    }
    
    public int getMaxPhoneLength() {
        return maxPhoneLength;
    }
    
    public void setMaxPhoneLength(int maxPhoneLength) {
        this.maxPhoneLength = maxPhoneLength;
    }
    
    public int getMaxProductNameLength() {
        return maxProductNameLength;
    }
    
    public void setMaxProductNameLength(int maxProductNameLength) {
        this.maxProductNameLength = maxProductNameLength;
    }
    
    public int getMaxProductDescriptionLength() {
        return maxProductDescriptionLength;
    }
    
    public void setMaxProductDescriptionLength(int maxProductDescriptionLength) {
        this.maxProductDescriptionLength = maxProductDescriptionLength;
    }
} 