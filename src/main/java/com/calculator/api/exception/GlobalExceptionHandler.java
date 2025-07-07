package com.calculator.api.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.calculator.api.model.ApiResponse;
import com.calculator.api.model.ResponseStatus;

/**
 * Global exception handler for the calculator API.
 * This class handles exceptions across the whole application.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles CalculatorException.
     * 
     * @param ex the CalculatorException
     * @return ResponseEntity containing error details wrapped in ApiResponse
     */
    @ExceptionHandler(CalculatorException.class)
    public ResponseEntity<ApiResponse<Object>> handleCalculatorException(CalculatorException ex) {
        logger.error("Calculator exception occurred: {}", ex.getMessage(), ex);
        
        String errorCode = ex.getStatusCode() == 400 ? ResponseStatus.ARITHMETIC_ERROR.getCode() : ResponseStatus.INTERNAL_ERROR.getCode();
        String requestId = MDC.get("requestId");
        
        ApiResponse<Object> response = ApiResponse.error(errorCode, ex.getMessage(), requestId);
        
        return new ResponseEntity<>(response, HttpStatus.valueOf(ex.getStatusCode()));
    }
    
    /**
     * Handles validation exceptions.
     * 
     * @param ex the MethodArgumentNotValidException
     * @return ResponseEntity containing error details wrapped in ApiResponse
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationException(MethodArgumentNotValidException ex) {
        logger.error("Validation exception occurred: {}", ex.getMessage(), ex);
        
        String errorMessage = "Validation error: " + 
            ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .findFirst().orElse("Invalid request parameters");
        
        String requestId = MDC.get("requestId");
        ApiResponse<Object> response = ApiResponse.error(ResponseStatus.VALIDATION_ERROR.getCode(), errorMessage, requestId);
        
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Handles JSON parsing errors.
     * 
     * @param ex the HttpMessageNotReadableException
     * @return ResponseEntity containing error details wrapped in ApiResponse
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Object>> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        logger.error("JSON parse error occurred: {}", ex.getMessage(), ex);
        
        String errorMessage = "Invalid request format";
        String errorCode = ResponseStatus.INVALID_REQUEST.getCode();
        
        // Check for BigDecimal parse errors
        if (ex.getMessage() != null && 
            (ex.getMessage().contains("BigDecimal") || 
             ex.getMessage().contains("numeric"))) {
            errorMessage = "Invalid number format in the request. Please provide valid numeric values";
            errorCode = ResponseStatus.INVALID_NUMBER_FORMAT.getCode();
        } else if (ex.getMessage() != null && ex.getMessage().contains("JSON parse error")) {
            errorMessage = "Invalid JSON format in the request";
            errorCode = ResponseStatus.INVALID_JSON_FORMAT.getCode();
        }
        
        String requestId = MDC.get("requestId");
        ApiResponse<Object> response = ApiResponse.error(errorCode, errorMessage, requestId);
        
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Handles ArithmeticException.
     * 
     * @param ex the ArithmeticException
     * @return ResponseEntity containing error details wrapped in ApiResponse
     */
    @ExceptionHandler(ArithmeticException.class)
    public ResponseEntity<ApiResponse<Object>> handleArithmeticException(ArithmeticException ex) {
        logger.error("Arithmetic exception occurred: {}", ex.getMessage(), ex);
        
        String errorCode = ex.getMessage().contains("/ by zero") ? 
            ResponseStatus.DIVISION_BY_ZERO.getCode() : ResponseStatus.ARITHMETIC_ERROR.getCode();
        
        String requestId = MDC.get("requestId");
        ApiResponse<Object> response = ApiResponse.error(errorCode, "Arithmetic error: " + ex.getMessage(), requestId);
        
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Handles all other exceptions.
     * 
     * @param ex the Exception
     * @return ResponseEntity containing error details wrapped in ApiResponse
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGenericException(Exception ex) {
        logger.error("Unexpected error occurred: {}", ex.getMessage(), ex);
        
        String requestId = MDC.get("requestId");
        ApiResponse<Object> response = ApiResponse.error(
            ResponseStatus.INTERNAL_ERROR.getCode(), 
            "An unexpected error occurred: " + ex.getMessage(),
            requestId
        );
        
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}