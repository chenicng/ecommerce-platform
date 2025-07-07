package com.calculator.api.exception;

/**
 * Custom exception class for calculator operations.
 * This exception is thrown when errors occur during calculator operations.
 */
public class CalculatorException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    
    private int statusCode;

    /**
     * Constructs a new calculator exception with the specified detail message.
     * Default HTTP status code is 500.
     * 
     * @param message the detail message
     */
    public CalculatorException(String message) {
        super(message);
        this.statusCode = 500;
    }

    /**
     * Constructs a new calculator exception with the specified detail message and cause.
     * Default HTTP status code is 500.
     * 
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public CalculatorException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = 500;
    }
    
    /**
     * Constructs a new calculator exception with the specified detail message and status code.
     * 
     * @param message the detail message
     * @param statusCode the HTTP status code
     */
    public CalculatorException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }
    
    /**
     * Constructs a new calculator exception with the specified detail message, cause and status code.
     * 
     * @param message the detail message
     * @param cause the cause of the exception
     * @param statusCode the HTTP status code
     */
    public CalculatorException(String message, Throwable cause, int statusCode) {
        super(message, cause);
        this.statusCode = statusCode;
    }
    
    /**
     * Gets the HTTP status code.
     *
     * @return the HTTP status code
     */
    public int getStatusCode() {
        return statusCode;
    }
}