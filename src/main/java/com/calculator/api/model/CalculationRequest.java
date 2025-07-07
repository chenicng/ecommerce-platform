package com.calculator.api.model;

import java.math.BigDecimal;
import jakarta.validation.constraints.NotNull;

/**
 * Model class representing a calculation request with two operands.
 * This class is used to receive calculation inputs from API clients.
 */
public class CalculationRequest {

    @NotNull(message = "First operand must not be null")
    private BigDecimal operand1;

    @NotNull(message = "Second operand must not be null")
    private BigDecimal operand2;

    /**
     * Default constructor for serialization frameworks.
     */
    public CalculationRequest() {
    }

    /**
     * Constructor with all fields.
     *
     * @param operand1 the first operand for the calculation
     * @param operand2 the second operand for the calculation
     */
    public CalculationRequest(BigDecimal operand1, BigDecimal operand2) {
        this.operand1 = operand1;
        this.operand2 = operand2;
    }

    /**
     * Gets the first operand.
     *
     * @return the first operand
     */
    public BigDecimal getOperand1() {
        return operand1;
    }

    /**
     * Sets the first operand.
     *
     * @param operand1 the first operand to set
     */
    public void setOperand1(BigDecimal operand1) {
        this.operand1 = operand1;
    }

    /**
     * Gets the second operand.
     *
     * @return the second operand
     */
    public BigDecimal getOperand2() {
        return operand2;
    }

    /**
     * Sets the second operand.
     *
     * @param operand2 the second operand to set
     */
    public void setOperand2(BigDecimal operand2) {
        this.operand2 = operand2;
    }

    /**
     * Returns a string representation of the calculation request.
     *
     * @return a string representation of the calculation request
     */
    @Override
    public String toString() {
        return "CalculationRequest{" +
                "operand1=" + operand1 +
                ", operand2=" + operand2 +
                '}';
    }
}