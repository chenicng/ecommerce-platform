# Calculator API Service

## Project Overview
Calculator API is a RESTful web service built with Spring Boot that provides basic arithmetic operations (addition, subtraction, multiplication, and division) with high precision. The calculator maintains up to 10 decimal places of precision in all calculations, making it suitable for financial and scientific applications requiring accurate results.

## Features
- Four basic arithmetic operations:
  - Addition
  - Subtraction
  - Multiplication
  - Division
- High-precision calculations with up to 10 decimal places
- RESTful API interface
- Robust error handling for invalid inputs and division by zero
- Input validation with detailed error messages
- Complete API documentation
- Comprehensive test coverage
- Health check endpoints for monitoring
- Request ID tracking for better debugging
- Unified response format for consistency
- Spring Boot Actuator integration for monitoring

## Technology Stack
- Java 21
- Spring Boot 3.2.0
- Maven 3.x
- Spring MVC for REST API
- Spring Boot Actuator for monitoring
- JUnit 5 for testing
- JaCoCo for test coverage reporting
- BigDecimal for high-precision arithmetic
- Spring Boot Validation for input validation
- SLF4J + Logback for logging with request tracking

## Project Structure
```
calculator-api/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/calculator/api/
│   │   │       ├── CalculatorApiApplication.java
│   │   │       ├── controller/
│   │   │       │   ├── CalculatorController.java
│   │   │       │   └── HealthController.java
│   │   │       ├── exception/
│   │   │       │   ├── CalculatorException.java
│   │   │       │   └── GlobalExceptionHandler.java
│   │   │       ├── filter/
│   │   │       │   └── RequestIdFilter.java
│   │   │       ├── model/
│   │   │       │   ├── ApiResponse.java
│   │   │       │   ├── CalculationRequest.java
│   │   │       │   ├── CalculationResponse.java
│   │   │       │   └── ErrorResponse.java
│   │   │       └── service/
│   │   │           ├── CalculatorService.java
│   │   │           └── CalculatorServiceImpl.java
│   │   └── resources/
│   │       ├── application.yml
│   │       └── application.yml.example
│   └── test/
│       └── java/
│           └── com/calculator/api/
│               ├── controller/
│               │   └── CalculatorControllerTest.java
│               └── service/
│                   └── CalculatorServiceImplTest.java
├── target/
│   └── site/jacoco/              # Test coverage reports
├── pom.xml
└── README.md
```

## Building and Running the Application

### Prerequisites
- Java Development Kit (JDK) 21 or later
- Maven 3.x

### Building the Project
Clone the repository and build the project using Maven:

```bash
git clone https://github.com/yourusername/calculator-api.git
cd calculator-api
mvn clean install
```

### Running the Application
After building, you can run the application using:

```bash
mvn spring-boot:run
```

Or you can run the generated JAR file:

```bash
java -jar target/calculator-api-1.0.0.jar
```

The application will start on port 8080 by default (configurable in `application.yml`).

### Configuration

The application uses YAML configuration. Copy `application.yml.example` to `application.yml` and modify as needed:

```bash
cp src/main/resources/application.yml.example src/main/resources/application.yml
```

Key configuration options:
- `server.port`: Application port (default: 8080)
- `logging.level.root`: Root logging level (default: INFO)
- `management.endpoints.web.exposure.include`: Exposed actuator endpoints
- For production, consider limiting actuator endpoints to only `health`

## API Documentation

### API Versioning and Grouping

The Calculator API uses a versioned approach to ensure backward compatibility and clear API organization:

**Version Control:**
- All endpoints are prefixed with `/api/v1/` to support future API versions
- Version 1 (`v1`) is the current stable version
- Future versions (e.g., `v2`) can be introduced without breaking existing clients

**Functional Groups:**
- `/api/v1/calculator/` - All calculation operations (add, subtract, multiply, divide)
- `/api/v1/health` - Health check endpoint
- `/api/v1/system/` - System information and monitoring endpoints

This design provides:
- **Clear separation** between different functional areas
- **Version compatibility** for client applications  
- **Scalable architecture** for future feature additions
- **Consistent URL patterns** following REST best practices

### Endpoints

#### Health Check Endpoints

##### Application Health Check
- **URL**: `/api/v1/health`
- **Method**: GET
- **Response**:
```json
{
  "success": true,
  "message": "Service is healthy",
  "data": {
    "status": "UP",
    "service": "Calculator API",
    "version": "1.0.0",
    "timestamp": "2024-01-01T10:00:00"
  },
  "timestamp": "2024-01-01T10:00:00"
}
```

##### Application Information
- **URL**: `/api/v1/system/info`
- **Method**: GET
- **Response**: Returns detailed service information including Java and OS details

##### Spring Actuator Health
- **URL**: `/actuator/health`
- **Method**: GET
- **Response**: Standard Spring Boot health check

#### Calculator Endpoints

All calculation endpoints accept POST requests with a JSON request body containing two operands.

#### Addition
- **URL**: `/api/v1/calculator/add`
- **Method**: POST
- **Request Body**:
```json
{
  "operand1": <first_number>,
  "operand2": <second_number>
}
```
- **Response**:
```json
{
  "operation": "addition",
  "result": <result_with_up_to_10_decimal_places>
}
```

#### Subtraction
- **URL**: `/api/v1/calculator/subtract`
- **Method**: POST
- **Request Body**:
```json
{
  "operand1": <first_number>,
  "operand2": <second_number>
}
```
- **Response**:
```json
{
  "operation": "subtraction",
  "result": <result_with_up_to_10_decimal_places>
}
```

#### Multiplication
- **URL**: `/api/v1/calculator/multiply`
- **Method**: POST
- **Request Body**:
```json
{
  "operand1": <first_number>,
  "operand2": <second_number>
}
```
- **Response**:
```json
{
  "operation": "multiplication",
  "result": <result_with_up_to_10_decimal_places>
}
```

#### Division
- **URL**: `/api/v1/calculator/divide`
- **Method**: POST
- **Request Body**:
```json
{
  "operand1": <dividend>,
  "operand2": <divisor>
}
```
- **Response**:
```json
{
  "operation": "division",
  "result": <result_with_up_to_10_decimal_places>
}
```

### API Usage Examples

#### Example 1: Addition
**Request**:
```bash
curl -X POST http://localhost:8080/api/v1/calculator/add \
  -H "Content-Type: application/json" \
  -d '{"operand1": 10.5, "operand2": 5.25}'
```

**Response**:
```json
{
  "operation": "addition",
  "result": 15.7500000000
}
```

#### Example 2: Division
**Request**:
```bash
curl -X POST http://localhost:8080/api/v1/calculator/divide \
  -H "Content-Type: application/json" \
  -d '{"operand1": 10, "operand2": 3}'
```

**Response**:
```json
{
  "operation": "division",
  "result": 3.3333333333
}
```

## Request Tracking

Each request is automatically assigned a unique request ID for tracking purposes:
- Request ID is returned in the `X-Request-ID` response header
- All log entries include the request ID for easier debugging
- You can provide your own request ID by including it in the `X-Request-ID` request header

Example with request tracking:
```bash
curl -X POST http://localhost:8080/api/v1/calculator/add \
  -H "Content-Type: application/json" \
  -H "X-Request-ID: my-custom-id-123" \
  -d '{"operand1": 10.5, "operand2": 5.25}'
```

## Error Handling

The API handles various error conditions with appropriate HTTP status codes and error messages:

### Common Errors

#### 1. Invalid Input
- **Status Code**: 400 Bad Request
- **Response Body**:
```json
{
  "timestamp": "2023-10-30T14:25:30.123",
  "status": 400,
  "message": "Validation failed",
  "path": "/api/v1/calculator/add"
}
```

#### 2. Division by Zero
- **Status Code**: 400 Bad Request
- **Response Body**:
```json
{
  "timestamp": "2023-10-30T14:25:30.123",
  "status": 400,
  "message": "Error performing division: Division by zero is not allowed",
  "path": "/api/v1/calculator/divide"
}
```

#### 3. Server Error
- **Status Code**: 500 Internal Server Error
- **Response Body**:
```json
{
  "timestamp": "2023-10-30T14:25:30.123",
  "status": 500,
  "message": "An unexpected error occurred",
  "path": "/api/v1/calculator/add"
}
```

## Running Tests

The project includes comprehensive unit tests for both the service layer and REST controller with excellent test coverage.

### Running All Tests
```bash
mvn clean test
```

### Running Specific Test Classes
```bash
mvn test -Dtest=CalculatorServiceImplTest
mvn test -Dtest=CalculatorControllerTest
```

### Test Coverage Report
The project uses JaCoCo for test coverage analysis. To generate and view the coverage report:

```bash
mvn clean test jacoco:report
```

After running the above command, you can view the detailed coverage report by opening:
- **HTML Report**: `target/site/jacoco/index.html` (open in browser)
- **CSV Report**: `target/site/jacoco/jacoco.csv`
- **XML Report**: `target/site/jacoco/jacoco.xml`

### Current Test Coverage Statistics
- **Total Tests**: 31 (all passing)
- **Instruction Coverage**: 63%
- **Branch Coverage**: 16%
- **Line Coverage**: 70%
- **Method Coverage**: 77%
- **Class Coverage**: 100%

### Test Coverage by Package
| Package | Instruction Coverage | Line Coverage |
|---------|---------------------|---------------|
| `com.calculator.api.service` | 92% | 89% |
| `com.calculator.api.controller` | 79% | 84% |
| `com.calculator.api.model` | 53% | 66% |
| `com.calculator.api.exception` | 44% | 64% |
| `com.calculator.api` | 38% | 33% |

### Test Coverage
The comprehensive test suite covers:
- **Basic arithmetic operations** with various inputs
- **Decimal precision handling** (up to 10 decimal places)
- **Edge cases**: large numbers, small numbers, negative numbers, zero values
- **Division by zero handling** and error responses
- **Input validation** for null and invalid values
- **API response format** and HTTP status codes
- **Exception handling** and error messages
- **Service layer** business logic (21 tests)
- **Controller layer** REST API endpoints (10 tests)