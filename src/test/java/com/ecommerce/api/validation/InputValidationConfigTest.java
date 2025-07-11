package com.ecommerce.api.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import static org.junit.jupiter.api.Assertions.*;

class InputValidationConfigTest {

    private InputValidationConfig config;

    @BeforeEach
    void setUp() {
        config = new InputValidationConfig();
    }

    @Test
    void testDefaultValues() {
        assertEquals(1000, config.getMaxStringLength());
        assertEquals(10 * 1024 * 1024, config.getMaxRequestSize());
        assertEquals(50, config.getMaxUsernameLength());
        assertEquals(100, config.getMaxEmailLength());
        assertEquals(20, config.getMaxPhoneLength());
        assertEquals(200, config.getMaxProductNameLength());
        assertEquals(1000, config.getMaxProductDescriptionLength());
    }

    @Test
    void testSetAndGetMaxStringLength() {
        config.setMaxStringLength(2000);
        assertEquals(2000, config.getMaxStringLength());
    }

    @Test
    void testSetAndGetMaxRequestSize() {
        config.setMaxRequestSize(20 * 1024 * 1024);
        assertEquals(20 * 1024 * 1024, config.getMaxRequestSize());
    }

    @Test
    void testSetAndGetMaxUsernameLength() {
        config.setMaxUsernameLength(100);
        assertEquals(100, config.getMaxUsernameLength());
    }

    @Test
    void testSetAndGetMaxEmailLength() {
        config.setMaxEmailLength(150);
        assertEquals(150, config.getMaxEmailLength());
    }

    @Test
    void testSetAndGetMaxPhoneLength() {
        config.setMaxPhoneLength(25);
        assertEquals(25, config.getMaxPhoneLength());
    }

    @Test
    void testSetAndGetMaxProductNameLength() {
        config.setMaxProductNameLength(300);
        assertEquals(300, config.getMaxProductNameLength());
    }

    @Test
    void testSetAndGetMaxProductDescriptionLength() {
        config.setMaxProductDescriptionLength(1500);
        assertEquals(1500, config.getMaxProductDescriptionLength());
    }

    @Test
    void testSetZeroValues() {
        config.setMaxStringLength(0);
        config.setMaxRequestSize(0);
        config.setMaxUsernameLength(0);
        config.setMaxEmailLength(0);
        config.setMaxPhoneLength(0);
        config.setMaxProductNameLength(0);
        config.setMaxProductDescriptionLength(0);

        assertEquals(0, config.getMaxStringLength());
        assertEquals(0, config.getMaxRequestSize());
        assertEquals(0, config.getMaxUsernameLength());
        assertEquals(0, config.getMaxEmailLength());
        assertEquals(0, config.getMaxPhoneLength());
        assertEquals(0, config.getMaxProductNameLength());
        assertEquals(0, config.getMaxProductDescriptionLength());
    }

    @Test
    void testSetNegativeValues() {
        config.setMaxStringLength(-100);
        config.setMaxRequestSize(-1024);
        config.setMaxUsernameLength(-50);
        config.setMaxEmailLength(-75);
        config.setMaxPhoneLength(-10);
        config.setMaxProductNameLength(-200);
        config.setMaxProductDescriptionLength(-500);

        assertEquals(-100, config.getMaxStringLength());
        assertEquals(-1024, config.getMaxRequestSize());
        assertEquals(-50, config.getMaxUsernameLength());
        assertEquals(-75, config.getMaxEmailLength());
        assertEquals(-10, config.getMaxPhoneLength());
        assertEquals(-200, config.getMaxProductNameLength());
        assertEquals(-500, config.getMaxProductDescriptionLength());
    }

    @Test
    void testSetLargeValues() {
        config.setMaxStringLength(Integer.MAX_VALUE);
        config.setMaxRequestSize(Integer.MAX_VALUE);
        config.setMaxUsernameLength(Integer.MAX_VALUE);
        config.setMaxEmailLength(Integer.MAX_VALUE);
        config.setMaxPhoneLength(Integer.MAX_VALUE);
        config.setMaxProductNameLength(Integer.MAX_VALUE);
        config.setMaxProductDescriptionLength(Integer.MAX_VALUE);

        assertEquals(Integer.MAX_VALUE, config.getMaxStringLength());
        assertEquals(Integer.MAX_VALUE, config.getMaxRequestSize());
        assertEquals(Integer.MAX_VALUE, config.getMaxUsernameLength());
        assertEquals(Integer.MAX_VALUE, config.getMaxEmailLength());
        assertEquals(Integer.MAX_VALUE, config.getMaxPhoneLength());
        assertEquals(Integer.MAX_VALUE, config.getMaxProductNameLength());
        assertEquals(Integer.MAX_VALUE, config.getMaxProductDescriptionLength());
    }

    @Test
    void testClassAnnotations() {
        assertTrue(InputValidationConfig.class.isAnnotationPresent(Configuration.class));
        assertTrue(InputValidationConfig.class.isAnnotationPresent(ConfigurationProperties.class));
        
        ConfigurationProperties annotation = InputValidationConfig.class.getAnnotation(ConfigurationProperties.class);
        assertEquals("security.validation", annotation.value());
    }

    @Test
    void testMultipleSetOperations() {
        // Test multiple set operations on the same instance
        config.setMaxStringLength(500);
        config.setMaxStringLength(1000);
        config.setMaxStringLength(1500);
        assertEquals(1500, config.getMaxStringLength());

        config.setMaxRequestSize(5 * 1024 * 1024);
        config.setMaxRequestSize(15 * 1024 * 1024);
        config.setMaxRequestSize(25 * 1024 * 1024);
        assertEquals(25 * 1024 * 1024, config.getMaxRequestSize());
    }

    @Test
    void testInstanceIndependence() {
        InputValidationConfig config1 = new InputValidationConfig();
        InputValidationConfig config2 = new InputValidationConfig();

        config1.setMaxStringLength(500);
        config2.setMaxStringLength(1000);

        assertEquals(500, config1.getMaxStringLength());
        assertEquals(1000, config2.getMaxStringLength());
        assertEquals(1000, config.getMaxStringLength()); // Default value unchanged
    }
} 