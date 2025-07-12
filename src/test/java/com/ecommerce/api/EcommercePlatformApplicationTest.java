package com.ecommerce.api;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

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
    void main_ShouldAcceptStringArray() {
        // Test that main method accepts String[] parameter
        assertDoesNotThrow(() -> {
            java.lang.reflect.Method mainMethod = EcommercePlatformApplication.class.getMethod("main", String[].class);
            assertEquals("main", mainMethod.getName());
            assertEquals(1, mainMethod.getParameterCount());
            assertEquals(String[].class, mainMethod.getParameterTypes()[0]);
        });
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

    @Test
    void run_WithMockEnvironment_ShouldExecuteSuccessfully() throws Exception {
        // Test that run method can be called without exception
        EcommercePlatformApplication app = new EcommercePlatformApplication();
        
        // Mock the environment to simulate mock profile
        org.springframework.core.env.Environment environment = org.mockito.Mockito.mock(org.springframework.core.env.Environment.class);
        org.mockito.Mockito.when(environment.getActiveProfiles()).thenReturn(new String[]{"mock"});
        org.mockito.Mockito.when(environment.getProperty("server.port", "8080")).thenReturn("8080");
        
        ReflectionTestUtils.setField(app, "environment", environment);
        
        // This should not throw an exception
        assertDoesNotThrow(() -> app.run());
    }

    @Test
    void run_WithEmptyArgs_ShouldExecuteSuccessfully() throws Exception {
        // Test that run method can be called with empty arguments
        EcommercePlatformApplication app = new EcommercePlatformApplication();
        
        // Mock the environment
        org.springframework.core.env.Environment environment = org.mockito.Mockito.mock(org.springframework.core.env.Environment.class);
        org.mockito.Mockito.when(environment.getActiveProfiles()).thenReturn(new String[]{"mock"});
        org.mockito.Mockito.when(environment.getProperty("server.port", "8080")).thenReturn("8080");
        
        ReflectionTestUtils.setField(app, "environment", environment);
        
        // This should not throw an exception
        assertDoesNotThrow(() -> app.run(new String[]{}));
    }

    @Test
    void run_WithNullArgs_ShouldExecuteSuccessfully() throws Exception {
        // Test that run method can be called with null arguments
        EcommercePlatformApplication app = new EcommercePlatformApplication();
        
        // Mock the environment
        org.springframework.core.env.Environment environment = org.mockito.Mockito.mock(org.springframework.core.env.Environment.class);
        org.mockito.Mockito.when(environment.getActiveProfiles()).thenReturn(new String[]{"mock"});
        org.mockito.Mockito.when(environment.getProperty("server.port", "8080")).thenReturn("8080");
        
        ReflectionTestUtils.setField(app, "environment", environment);
        
        // This should not throw an exception
        assertDoesNotThrow(() -> app.run((String[]) null));
    }

    @Test
    void application_ShouldHaveCorrectPackageStructure() {
        // Test package structure
        String packageName = EcommercePlatformApplication.class.getPackage().getName();
        assertEquals("com.ecommerce.api", packageName);
    }

    @Test
    void run_WithMultipleActiveProfiles_ShouldHandleGracefully() throws Exception {
        // Test that run method handles multiple profiles
        EcommercePlatformApplication app = new EcommercePlatformApplication();
        
        // Mock the environment with multiple profiles
        org.springframework.core.env.Environment environment = org.mockito.Mockito.mock(org.springframework.core.env.Environment.class);
        org.mockito.Mockito.when(environment.getActiveProfiles()).thenReturn(new String[]{"mock", "test"});
        org.mockito.Mockito.when(environment.getProperty("server.port", "8080")).thenReturn("8080");
        
        ReflectionTestUtils.setField(app, "environment", environment);
        
        // This should not throw an exception
        assertDoesNotThrow(() -> app.run());
    }
} 