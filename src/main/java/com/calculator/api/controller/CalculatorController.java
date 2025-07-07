package com.calculator.api.controller;

import com.calculator.api.exception.CalculatorException;
import com.calculator.api.model.ApiResponse;
import com.calculator.api.model.CalculationRequest;
import com.calculator.api.model.CalculationResponse;
import com.calculator.api.service.CalculatorService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.function.BiFunction;

/**
 * REST controller for calculator operations.
 * Provides endpoints for basic arithmetic operations: addition, subtraction, multiplication, and division.
 */
@RestController
@RequestMapping("/api/v1/calculator")
public class CalculatorController {

    private static final Logger logger = LoggerFactory.getLogger(CalculatorController.class);

    private final CalculatorService calculatorService;

    /**
     * Constructor for dependency injection.
     *
     * @param calculatorService the calculator service
     */
    @Autowired
    public CalculatorController(CalculatorService calculatorService) {
        this.calculatorService = calculatorService;
    }

    /**
     * Common operation handler for calculator endpoints
     * @param operationType Human-readable operation name for logging
     * @param operation The calculation function to execute
     * @param request The calculation request containing operands
     * @return ResponseEntity with calculation result wrapped in ApiResponse
     */
    private ResponseEntity<ApiResponse<CalculationResponse>> performOperation(
            String operationType,
            BiFunction<BigDecimal, BigDecimal, BigDecimal> operation,
            CalculationRequest request) {
        
        logger.info("{} request received: {}", operationType, request);
        
        try {
            BigDecimal result = operation.apply(request.getOperand1(), request.getOperand2());
            CalculationResponse response = new CalculationResponse(operationType, result);
            
            // 从MDC中获取请求ID
            String requestId = MDC.get("requestId");
            
            ApiResponse<CalculationResponse> apiResponse = ApiResponse.success(
                operationType + " operation completed successfully", 
                response,
                requestId
            );
            
            logger.info("{} completed successfully: {}", operationType, response);
            return ResponseEntity.ok(apiResponse);
        } catch (ArithmeticException e) {
            logger.error("Arithmetic error during {} operation: {}", operationType, e.getMessage(), e);
            throw new CalculatorException("Error performing " + operationType + ": " + e.getMessage(), e, 400);
        } catch (Exception e) {
            logger.error("Unexpected error during {} operation: {}", operationType, e.getMessage(), e);
            throw new CalculatorException("Error performing " + operationType + ": " + e.getMessage(), e);
        }
    }

    /**
     * Endpoint for addition operation.
     *
     * @param request the calculation request containing two operands
     * @return ResponseEntity with calculation response wrapped in ApiResponse
     */
    @PostMapping("/add")
    public ResponseEntity<ApiResponse<CalculationResponse>> add(@Valid @RequestBody CalculationRequest request) {
        return performOperation("addition", calculatorService::add, request);
    }

    /**
     * Endpoint for subtraction operation.
     *
     * @param request the calculation request containing two operands
     * @return ResponseEntity with calculation response wrapped in ApiResponse
     */
    @PostMapping("/subtract")
    public ResponseEntity<ApiResponse<CalculationResponse>> subtract(@Valid @RequestBody CalculationRequest request) {
        return performOperation("subtraction", calculatorService::subtract, request);
    }

    /**
     * Endpoint for multiplication operation.
     *
     * @param request the calculation request containing two operands
     * @return ResponseEntity with calculation response wrapped in ApiResponse
     */
    @PostMapping("/multiply")
    public ResponseEntity<ApiResponse<CalculationResponse>> multiply(@Valid @RequestBody CalculationRequest request) {
        return performOperation("multiplication", calculatorService::multiply, request);
    }

    /**
     * Endpoint for division operation.
     *
     * @param request the calculation request containing two operands
     * @return ResponseEntity with calculation response wrapped in ApiResponse
     */
    @PostMapping("/divide")
    public ResponseEntity<ApiResponse<CalculationResponse>> divide(@Valid @RequestBody CalculationRequest request) {
        return performOperation("division", calculatorService::divide, request);
    }
}