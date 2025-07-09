package com.ecommerce.infrastructure.config;

import com.ecommerce.api.EcommercePlatformApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = EcommercePlatformApplication.class)
class DatabaseConfigTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void contextLoads() {
        assertNotNull(applicationContext);
    }

    @Test
    void databaseConfigNotLoadedInMockProfile() {
        // DatabaseConfig should not be loaded when mock profile is active (default test profile)
        assertFalse(applicationContext.containsBean("databaseConfig"));
    }

    @Test
    void databaseConfigClassExists() {
        // Test that the DatabaseConfig class can be instantiated
        DatabaseConfig config = new DatabaseConfig();
        assertNotNull(config);
    }

    @Test
    void databaseConfigHasCorrectAnnotations() {
        // Test that the DatabaseConfig class has the expected annotations
        assertTrue(DatabaseConfig.class.isAnnotationPresent(org.springframework.context.annotation.Configuration.class));
        assertTrue(DatabaseConfig.class.isAnnotationPresent(org.springframework.context.annotation.Profile.class));
        assertTrue(DatabaseConfig.class.isAnnotationPresent(org.springframework.data.jpa.repository.config.EnableJpaRepositories.class));
        assertTrue(DatabaseConfig.class.isAnnotationPresent(org.springframework.boot.autoconfigure.domain.EntityScan.class));
    }
} 