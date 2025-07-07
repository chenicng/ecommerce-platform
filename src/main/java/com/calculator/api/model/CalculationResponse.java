package com.calculator.api.model;

import java.math.BigDecimal;

/**
 * Model class representing a calculation response with operation type and result.
 * This class is used to return calculation results to API clients.
 */
public class CalculationResponse {

    private String operation;
    private BigDecimal result;

    /**
     * Default constructor for serialization frameworks.
     */
    public CalculationResponse() {
    }

    /**
     * Constructor with all fields.
     *
     * @param operation the type of operation performed (add, subtract, multiply, divide)
     * @param result the result of the calculation
     */
    public CalculationResponse(String operation, BigDecimal result) {
        this.operation = operation;
        this.result = result;
    }

    /**
     * Gets the operation type.
     *
     * @return the operation type
     */
    public String getOperation() {
        return operation;
    }

    /**
     * Sets the operation type.
     *
     * @param operation the operation type to set
     */
    public void setOperation(String operation) {
        this.operation = operation;
    }

    /**
     * Gets the calculation result.
     *
     * @return the calculation result
     */
    public BigDecimal getResult() {
        return result;
    }

    /**
     * Sets the calculation result.
     *
     * @param result the calculation result to set
     */
    public void setResult(BigDecimal result) {
        this.result = result;
    }

    /**
     * Returns a string representation of the calculation response.
     *
     * @return a string representation of the calculation response
     */
    @Override
    public String toString() {
        return "CalculationResponse{" +
                "operation='" + operation + '\'' +
                ", result=" + result +
                '}';
    }
}