package com.ecommerce.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * E-commerce Platform Application Startup Class
 * 
 * This is the main entry point of the e-commerce platform backend service.
 * Uses Spring Boot framework and follows Domain-Driven Design (DDD) principles.
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
@ComponentScan(basePackages = "com.ecommerce")
@EnableScheduling
public class EcommercePlatformApplication implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(EcommercePlatformApplication.class);

    @Autowired
    private Environment environment;

    public static void main(String[] args) {
        SpringApplication.run(EcommercePlatformApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        logger.info("=".repeat(80));
        logger.info("E-commerce Platform API Service Started Successfully!");
        logger.info("=".repeat(80));
        logger.info("Active Profile: {}", String.join(", ", environment.getActiveProfiles()));
        logger.info("Server Port: {}", environment.getProperty("server.port", "8080"));
        logger.info("API Base URL: http://localhost:{}", environment.getProperty("server.port", "8080"));
        logger.info("Health Check: http://localhost:{}/actuator/health", environment.getProperty("server.port", "8080"));
        
        String activeProfile = environment.getActiveProfiles().length > 0 ? 
                              environment.getActiveProfiles()[0] : "default";
        
        if ("mock".equals(activeProfile)) {
            logger.info("Running in MOCK mode - using in-memory storage with demo data");
            logger.info("Demo users: alice (ID: 1), bob (ID: 2), charlie (ID: 3)");
            logger.info("Demo merchants: Apple Store (ID: 1), Tech Books Store (ID: 2)");
            logger.info("Demo products: 6 products available (phones, laptops, books, headphones)");
        } else if ("mysql".equals(activeProfile)) {
            logger.info("Running in MYSQL mode - using MySQL database for persistence");
            logger.info("Database URL: {}", environment.getProperty("spring.datasource.url"));
        }
        
        logger.info("=".repeat(80));
    }
} 