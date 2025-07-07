package com.calculator.api.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for CalculatorServiceImpl.
 * Tests the basic arithmetic operations with various cases.
 */
@ExtendWith(MockitoExtension.class)
public class CalculatorServiceImplTest {

    private CalculatorService calculatorService;

    @BeforeEach
    void setUp() {
        calculatorService = new CalculatorServiceImpl();
    }

    @Test
    @DisplayName("Test addition with positive integers")
    void testAddWithPositiveIntegers() {
        // Arrange
        BigDecimal operand1 = new BigDecimal("5");
        BigDecimal operand2 = new BigDecimal("7");
        
        // Act
        BigDecimal result = calculatorService.add(operand1, operand2);
        
        // Assert
        assertEquals(new BigDecimal("12.0000000000"), result);
    }

    @Test
    @DisplayName("Test addition with decimals")
    void testAddWithDecimals() {
        // Arrange
        BigDecimal operand1 = new BigDecimal("5.25");
        BigDecimal operand2 = new BigDecimal("7.75");
        
        // Act
        BigDecimal result = calculatorService.add(operand1, operand2);
        
        // Assert
        assertEquals(new BigDecimal("13.0000000000"), result);
    }

    @Test
    @DisplayName("Test addition with negative numbers")
    void testAddWithNegativeNumbers() {
        // Arrange
        BigDecimal operand1 = new BigDecimal("-5");
        BigDecimal operand2 = new BigDecimal("7");
        
        // Act
        BigDecimal result = calculatorService.add(operand1, operand2);
        
        // Assert
        assertEquals(new BigDecimal("2.0000000000"), result);
    }

    @Test
    @DisplayName("Test addition with very large numbers")
    void testAddWithLargeNumbers() {
        // Arrange
        BigDecimal operand1 = new BigDecimal("999999999999999.999999");
        BigDecimal operand2 = new BigDecimal("0.000001");
        
        // Act
        BigDecimal result = calculatorService.add(operand1, operand2);
        
        // Assert
        assertEquals(new BigDecimal("1000000000000000.0000000000"), result);
    }

    @Test
    @DisplayName("Test subtraction with positive integers")
    void testSubtractWithPositiveIntegers() {
        // Arrange
        BigDecimal operand1 = new BigDecimal("10");
        BigDecimal operand2 = new BigDecimal("7");
        
        // Act
        BigDecimal result = calculatorService.subtract(operand1, operand2);
        
        // Assert
        assertEquals(new BigDecimal("3.0000000000"), result);
    }

    @Test
    @DisplayName("Test subtraction with decimals")
    void testSubtractWithDecimals() {
        // Arrange
        BigDecimal operand1 = new BigDecimal("10.5");
        BigDecimal operand2 = new BigDecimal("7.25");
        
        // Act
        BigDecimal result = calculatorService.subtract(operand1, operand2);
        
        // Assert
        assertEquals(new BigDecimal("3.2500000000"), result);
    }

    @Test
    @DisplayName("Test subtraction with negative result")
    void testSubtractWithNegativeResult() {
        // Arrange
        BigDecimal operand1 = new BigDecimal("5");
        BigDecimal operand2 = new BigDecimal("10");
        
        // Act
        BigDecimal result = calculatorService.subtract(operand1, operand2);
        
        // Assert
        assertEquals(new BigDecimal("-5.0000000000"), result);
    }

    @Test
    @DisplayName("Test multiplication with positive integers")
    void testMultiplyWithPositiveIntegers() {
        // Arrange
        BigDecimal operand1 = new BigDecimal("5");
        BigDecimal operand2 = new BigDecimal("7");
        
        // Act
        BigDecimal result = calculatorService.multiply(operand1, operand2);
        
        // Assert
        assertEquals(new BigDecimal("35.0000000000"), result);
    }

    @Test
    @DisplayName("Test multiplication with decimals")
    void testMultiplyWithDecimals() {
        // Arrange
        BigDecimal operand1 = new BigDecimal("5.5");
        BigDecimal operand2 = new BigDecimal("2.5");
        
        // Act
        BigDecimal result = calculatorService.multiply(operand1, operand2);
        
        // Assert
        assertEquals(new BigDecimal("13.7500000000"), result);
    }

    @Test
    @DisplayName("Test multiplication with negative numbers")
    void testMultiplyWithNegativeNumbers() {
        // Arrange
        BigDecimal operand1 = new BigDecimal("-5");
        BigDecimal operand2 = new BigDecimal("7");
        
        // Act
        BigDecimal result = calculatorService.multiply(operand1, operand2);
        
        // Assert
        assertEquals(new BigDecimal("-35.0000000000"), result);
    }

    @Test
    @DisplayName("Test multiplication with one zero operand")
    void testMultiplyWithZero() {
        // Arrange
        BigDecimal operand1 = new BigDecimal("5");
        BigDecimal operand2 = new BigDecimal("0");
        
        // Act
        BigDecimal result = calculatorService.multiply(operand1, operand2);
        
        // Assert
        assertEquals(new BigDecimal("0.0000000000"), result);
    }

    @Test
    @DisplayName("Test division with positive integers")
    void testDivideWithPositiveIntegers() {
        // Arrange
        BigDecimal operand1 = new BigDecimal("10");
        BigDecimal operand2 = new BigDecimal("2");
        
        // Act
        BigDecimal result = calculatorService.divide(operand1, operand2);
        
        // Assert
        assertEquals(new BigDecimal("5.0000000000"), result);
    }

    @Test
    @DisplayName("Test division with recurring decimals")
    void testDivideWithRecurringDecimals() {
        // Arrange
        BigDecimal operand1 = new BigDecimal("10");
        BigDecimal operand2 = new BigDecimal("3");
        
        // Act
        BigDecimal result = calculatorService.divide(operand1, operand2);
        
        // Assert
        assertEquals(new BigDecimal("3.3333333333"), result);
    }

    @Test
    @DisplayName("Test division with negative numbers")
    void testDivideWithNegativeNumbers() {
        // Arrange
        BigDecimal operand1 = new BigDecimal("-10");
        BigDecimal operand2 = new BigDecimal("2");
        
        // Act
        BigDecimal result = calculatorService.divide(operand1, operand2);
        
        // Assert
        assertEquals(new BigDecimal("-5.0000000000"), result);
    }

    @Test
    @DisplayName("Test division by zero throws ArithmeticException")
    void testDivideByZero() {
        // Arrange
        BigDecimal operand1 = new BigDecimal("10");
        BigDecimal operand2 = new BigDecimal("0");
        
        // Act & Assert
        assertThrows(ArithmeticException.class, () -> calculatorService.divide(operand1, operand2));
    }

    @Test
    @DisplayName("Test division with very small result")
    void testDivideWithVerySmallResult() {
        // Arrange
        BigDecimal operand1 = new BigDecimal("1");
        BigDecimal operand2 = new BigDecimal("1000000000");
        
        // Act
        BigDecimal result = calculatorService.divide(operand1, operand2);
        
        // Assert
        assertEquals(new BigDecimal("0.0000000010"), result);
    }

    @ParameterizedTest
    @CsvSource({
        "1.1234567890, 2.2, 3.3234567890",
        "9.9999999999, 0.0000000001, 10.0000000000",
        "0.0000000001, 0.0000000001, 0.0000000002"
    })
    @DisplayName("Test decimal precision control in addition")
    void testDecimalPrecisionInAddition(String op1, String op2, String expected) {
        // Arrange
        BigDecimal operand1 = new BigDecimal(op1);
        BigDecimal operand2 = new BigDecimal(op2);
        BigDecimal expectedResult = new BigDecimal(expected);
        
        // Act
        BigDecimal result = calculatorService.add(operand1, operand2);
        
        // Assert
        assertEquals(expectedResult, result);
        assertEquals(10, result.scale());
    }

    @Test
    @DisplayName("Test extremely large number calculation")
    void testExtremelyLargeNumberCalculation() {
        // Arrange
        BigDecimal operand1 = new BigDecimal("99999999999999999999999999999999999999");
        BigDecimal operand2 = new BigDecimal("1");
        
        // Act
        BigDecimal sumResult = calculatorService.add(operand1, operand2);
        BigDecimal productResult = calculatorService.multiply(operand1, operand2);
        
        // Assert
        assertEquals(new BigDecimal("100000000000000000000000000000000000000.0000000000"), sumResult);
        assertEquals(new BigDecimal("99999999999999999999999999999999999999.0000000000"), productResult);
    }

    @Test
    @DisplayName("Test zero operands")
    void testZeroOperands() {
        // Arrange
        BigDecimal zero = BigDecimal.ZERO;
        BigDecimal five = new BigDecimal("5");
        
        // Act
        BigDecimal addResult = calculatorService.add(zero, zero);
        BigDecimal subtractResult = calculatorService.subtract(zero, zero);
        BigDecimal multiplyResult = calculatorService.multiply(five, zero);
        
        // Assert
        assertEquals(new BigDecimal("0.0000000000"), addResult);
        assertEquals(new BigDecimal("0.0000000000"), subtractResult);
        assertEquals(new BigDecimal("0.0000000000"), multiplyResult);
    }
}