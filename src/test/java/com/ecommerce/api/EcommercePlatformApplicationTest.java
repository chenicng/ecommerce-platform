package com.ecommerce.api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

class EcommercePlatformApplicationTest {

    @Test
    void main_ShouldExistAndBeCallable() {
        // Test that main method exists and is callable
        // We don't actually call it to avoid port conflicts
        assertNotNull(EcommercePlatformApplication.class.getDeclaredMethods());
        assertTrue(java.util.Arrays.stream(EcommercePlatformApplication.class.getDeclaredMethods())
            .anyMatch(method -> method.getName().equals("main")));
    }

    @Test
    void applicationShouldHaveCorrectAnnotations() {
        // Test that the application class has the expected annotations
        assertTrue(EcommercePlatformApplication.class.isAnnotationPresent(
            org.springframework.boot.autoconfigure.SpringBootApplication.class));
        assertTrue(EcommercePlatformApplication.class.isAnnotationPresent(
            org.springframework.context.annotation.ComponentScan.class));
        
        // Verify ComponentScan base packages
        org.springframework.context.annotation.ComponentScan componentScan = 
            EcommercePlatformApplication.class.getAnnotation(org.springframework.context.annotation.ComponentScan.class);
        assertEquals("com.ecommerce", componentScan.basePackages()[0]);
        
        // Verify SpringBootApplication excludes
        org.springframework.boot.autoconfigure.SpringBootApplication springBootApp = 
            EcommercePlatformApplication.class.getAnnotation(org.springframework.boot.autoconfigure.SpringBootApplication.class);
        assertEquals(2, springBootApp.exclude().length);
    }

    @Test
    void applicationShouldImplementCommandLineRunner() {
        // Test that the application implements CommandLineRunner
        assertTrue(org.springframework.boot.CommandLineRunner.class.isAssignableFrom(EcommercePlatformApplication.class));
    }

    @Test
    void constructor_ShouldCreateInstance() {
        // Test that the application can be instantiated
        assertDoesNotThrow(() -> new EcommercePlatformApplication());
    }

    @Test
    void run_MethodShouldExist() throws Exception {
        // Test that run method exists (from CommandLineRunner interface)
        EcommercePlatformApplication app = new EcommercePlatformApplication();
        assertTrue(java.util.Arrays.stream(app.getClass().getDeclaredMethods())
            .anyMatch(method -> method.getName().equals("run")));
    }

    @Test
    void applicationShouldBeInstanceOfCommandLineRunner() {
        // Test that the application is an instance of CommandLineRunner
        EcommercePlatformApplication app = new EcommercePlatformApplication();
        assertTrue(app instanceof org.springframework.boot.CommandLineRunner);
    }
} 