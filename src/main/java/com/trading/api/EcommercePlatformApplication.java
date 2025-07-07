package com.trading.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main application class for E-commerce Platform API service
 * 
 * This application implements a complete e-commerce platform following DDD principles
 * with support for users, merchants, products, orders, and settlement.
 */
@SpringBootApplication(scanBasePackages = "com.trading")
@EnableScheduling
public class EcommercePlatformApplication {

    private static final Logger logger = LoggerFactory.getLogger(EcommercePlatformApplication.class);

    public static void main(String[] args) {
        logger.info("Starting E-commerce Platform API Service...");
        SpringApplication.run(EcommercePlatformApplication.class, args);
        logger.info("E-commerce Platform API Service started successfully");
        logger.info("H2 Console available at: http://localhost:8080/h2-console");
        logger.info("API Documentation available at: http://localhost:8080/actuator");
    }
} 