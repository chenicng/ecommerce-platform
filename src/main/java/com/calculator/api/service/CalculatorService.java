package com.calculator.api.service;

import java.math.BigDecimal;

/**
 * Calculator service interface defining basic arithmetic operations.
 * All operations accept two BigDecimal parameters and return a BigDecimal result.
 */
public interface CalculatorService {

    /**
     * Adds two numbers.
     * 
     * @param operand1 first operand
     * @param operand2 second operand
     * @return the sum of operand1 and operand2, with precision of 10 decimal places
     */
    BigDecimal add(BigDecimal operand1, BigDecimal operand2);

    /**
     * Subtracts the second number from the first.
     * 
     * @param operand1 first operand (minuend)
     * @param operand2 second operand (subtrahend)
     * @return the difference of operand1 and operand2, with precision of 10 decimal places
     */
    BigDecimal subtract(BigDecimal operand1, BigDecimal operand2);

    /**
     * Multiplies two numbers.
     * 
     * @param operand1 first operand
     * @param operand2 second operand
     * @return the product of operand1 and operand2, with precision of 10 decimal places
     */
    BigDecimal multiply(BigDecimal operand1, BigDecimal operand2);

    /**
     * Divides the first number by the second.
     * 
     * @param operand1 first operand (dividend)
     * @param operand2 second operand (divisor)
     * @return the quotient of operand1 and operand2, with precision of 10 decimal places
     * @throws ArithmeticException if operand2 is zero
     */
    BigDecimal divide(BigDecimal operand1, BigDecimal operand2) throws ArithmeticException;
}