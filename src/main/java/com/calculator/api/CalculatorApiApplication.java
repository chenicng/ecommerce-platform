package com.calculator.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main application class for Calculator API service
 */
@SpringBootApplication
public class CalculatorApiApplication {

    private static final Logger logger = LoggerFactory.getLogger(CalculatorApiApplication.class);

    public static void main(String[] args) {
        logger.info("Starting Calculator API Service...");
        SpringApplication.run(CalculatorApiApplication.class, args);
        logger.info("Calculator API Service started successfully");
    }
}