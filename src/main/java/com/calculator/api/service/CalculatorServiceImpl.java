package com.calculator.api.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Implementation of the calculator service that performs basic arithmetic operations.
 * All operations maintain a precision of 10 decimal places.
 */
@Service
public class CalculatorServiceImpl implements CalculatorService {

    private static final Logger logger = LoggerFactory.getLogger(CalculatorServiceImpl.class);
    private static final int DECIMAL_PRECISION = 10;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    /**
     * {@inheritDoc}
     */
    @Override
    public BigDecimal add(BigDecimal operand1, BigDecimal operand2) {
        logger.info("Performing addition: {} + {}", operand1, operand2);
        BigDecimal result = operand1.add(operand2).setScale(DECIMAL_PRECISION, ROUNDING_MODE);
        logger.debug("Addition result: {}", result);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BigDecimal subtract(BigDecimal operand1, BigDecimal operand2) {
        logger.info("Performing subtraction: {} - {}", operand1, operand2);
        BigDecimal result = operand1.subtract(operand2).setScale(DECIMAL_PRECISION, ROUNDING_MODE);
        logger.debug("Subtraction result: {}", result);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BigDecimal multiply(BigDecimal operand1, BigDecimal operand2) {
        logger.info("Performing multiplication: {} * {}", operand1, operand2);
        BigDecimal result = operand1.multiply(operand2).setScale(DECIMAL_PRECISION, ROUNDING_MODE);
        logger.debug("Multiplication result: {}", result);
        return result;
    }

    /**
     * {@inheritDoc}
     * 
     * @throws ArithmeticException if the divisor (operand2) is zero
     */
    @Override
    public BigDecimal divide(BigDecimal operand1, BigDecimal operand2) throws ArithmeticException {
        logger.info("Performing division: {} / {}", operand1, operand2);
        
        if (operand2.compareTo(BigDecimal.ZERO) == 0) {
            logger.error("Division by zero attempted");
            throw new ArithmeticException("Division by zero is not allowed");
        }
        
        try {
            BigDecimal result = operand1.divide(operand2, DECIMAL_PRECISION, ROUNDING_MODE);
            logger.debug("Division result: {}", result);
            return result;
        } catch (ArithmeticException e) {
            logger.error("Arithmetic error during division: {}", e.getMessage());
            throw e;
        }
    }
}