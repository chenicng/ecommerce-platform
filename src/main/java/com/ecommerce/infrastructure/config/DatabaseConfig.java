package com.ecommerce.infrastructure.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Database Configuration
 * Only enabled for MySQL profile
 */
@Configuration
@Profile("mysql")
@EnableJpaRepositories(basePackages = "com.ecommerce.infrastructure.repository.jpa")
@EntityScan(basePackages = "com.ecommerce.domain")
public class DatabaseConfig {
    // Configuration will be loaded when mysql profile is active
} 