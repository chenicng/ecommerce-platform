package com.calculator.api.controller;

import com.calculator.api.exception.CalculatorException;
import com.calculator.api.service.CalculatorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit test for CalculatorController.
 * Tests all API endpoints using MockMvc.
 */
@WebMvcTest(CalculatorController.class)
public class CalculatorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CalculatorService calculatorService;

    private String validRequestJson;
    private String invalidRequestJson;
    private String zeroOperandRequestJson;

    @BeforeEach
    void setUp() {
        // Set up valid request JSON
        validRequestJson = "{\"operand1\": 10, \"operand2\": 5}";

        // Set up invalid request JSON (missing operand2)
        invalidRequestJson = "{\"operand1\": 10}";

        // Set up request with zero divisor
        zeroOperandRequestJson = "{\"operand1\": 10, \"operand2\": 0}";
    }

    @Test
    @DisplayName("Test addition endpoint with valid input")
    void testAddEndpointWithValidInput() throws Exception {
        // Arrange
        when(calculatorService.add(any(BigDecimal.class), any(BigDecimal.class)))
                .thenReturn(new BigDecimal("15.0000000000"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/calculator/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validRequestJson))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.operation", is("addition")))
                .andExpect(jsonPath("$.data.result", is(15.0000000000)));
    }

    @Test
    @DisplayName("Test subtraction endpoint with valid input")
    void testSubtractEndpointWithValidInput() throws Exception {
        // Arrange
        when(calculatorService.subtract(any(BigDecimal.class), any(BigDecimal.class)))
                .thenReturn(new BigDecimal("5.0000000000"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/calculator/subtract")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validRequestJson))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.operation", is("subtraction")))
                .andExpect(jsonPath("$.data.result", is(5.0000000000)));
    }

    @Test
    @DisplayName("Test multiplication endpoint with valid input")
    void testMultiplyEndpointWithValidInput() throws Exception {
        // Arrange
        when(calculatorService.multiply(any(BigDecimal.class), any(BigDecimal.class)))
                .thenReturn(new BigDecimal("50.0000000000"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/calculator/multiply")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validRequestJson))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.operation", is("multiplication")))
                .andExpect(jsonPath("$.data.result", is(50.0000000000)));
    }

    @Test
    @DisplayName("Test division endpoint with valid input")
    void testDivideEndpointWithValidInput() throws Exception {
        // Arrange
        when(calculatorService.divide(any(BigDecimal.class), any(BigDecimal.class)))
                .thenReturn(new BigDecimal("2.0000000000"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/calculator/divide")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validRequestJson))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.operation", is("division")))
                .andExpect(jsonPath("$.data.result", is(2.0000000000)));
    }

    @Test
    @DisplayName("Test division endpoint with zero divisor")
    void testDivideEndpointWithZeroDivisor() throws Exception {
        // Arrange
        when(calculatorService.divide(any(BigDecimal.class), any(BigDecimal.class)))
                .thenThrow(new ArithmeticException("Division by zero is not allowed"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/calculator/divide")
                .contentType(MediaType.APPLICATION_JSON)
                .content(zeroOperandRequestJson))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.status", is("ARITHMETIC_ERROR")))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("Test addition endpoint with invalid input (missing operand)")
    void testAddEndpointWithInvalidInput() throws Exception {
        // Act & Assert
        performInvalidRequest("/api/v1/calculator/add", invalidRequestJson);
    }

    @Test
    @DisplayName("Test subtraction endpoint with invalid input (missing operand)")
    void testSubtractEndpointWithInvalidInput() throws Exception {
        // Act & Assert
        performInvalidRequest("/api/v1/calculator/subtract", invalidRequestJson);
    }

    @Test
    @DisplayName("Test multiplication endpoint with invalid input (missing operand)")
    void testMultiplyEndpointWithInvalidInput() throws Exception {
        // Act & Assert
        performInvalidRequest("/api/v1/calculator/multiply", invalidRequestJson);
    }

    @Test
    @DisplayName("Test division endpoint with invalid input (missing operand)")
    void testDivideEndpointWithInvalidInput() throws Exception {
        // Act & Assert
        performInvalidRequest("/api/v1/calculator/divide", invalidRequestJson);
    }

    @Test
    @DisplayName("Test error handling with calculator service exception")
    void testErrorHandlingWithCalculatorServiceException() throws Exception {
        // Arrange
        when(calculatorService.add(any(BigDecimal.class), any(BigDecimal.class)))
                .thenThrow(new CalculatorException("Test error message"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/calculator/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequestJson))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.status", is("VALIDATION_ERROR")))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    /**
     * Helper method to perform request with invalid input and verify response.
     *
     * @param endpoint    the API endpoint to test
     * @param requestJson the invalid request JSON
     * @throws Exception if an error occurs during the request
     */
    private void performInvalidRequest(String endpoint, String requestJson) throws Exception {
        mockMvc.perform(post(endpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status", is("VALIDATION_ERROR")));
    }
}