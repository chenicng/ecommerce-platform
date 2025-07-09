# E-commerce Platform API Documentation

## Overview

This project has successfully integrated **Springdoc OpenAPI 3.0** and **Swagger UI**, providing comprehensive API documentation and interactive testing interface.

## Access Methods

### 1. Swagger UI Interface
- **Recommended URL**: `http://localhost:8080/swagger-ui/index.html` (default path, direct access)
- **Alternative URL**: `http://localhost:8080/swagger-ui.html` (configured path, redirects to default)
- **Features**: Interactive API documentation with live testing capabilities
- **Capabilities**: 
  - Grouped by business modules
  - API version switching support
  - Real-time request/response testing
  - Complete parameter descriptions and examples

### 2. OpenAPI JSON Documentation
- **URL**: `http://localhost:8080/v3/api-docs`
- **Purpose**: Standard OpenAPI 3.0 JSON format documentation
- **Usage**: Can be used to generate client SDKs, import to other tools

### 3. Grouped API Documentation
- **V1 API**: `http://localhost:8080/v3/api-docs/v1`
- **V2 API**: `http://localhost:8080/v3/api-docs/v2`
- **Public API**: `http://localhost:8080/v3/api-docs/public`

## API Group Descriptions

### 🏥 Health Check
- **Description**: System health monitoring endpoints
- **Endpoints**: `/api/health`, `/api/healthz`
- **Purpose**: Service health checks and system status monitoring

### 📋 API Version
- **Description**: API version information and compatibility
- **Endpoints**: `/api/version/*`
- **Purpose**: Query supported API versions, check version compatibility

### 👤 User Management
- **Description**: User registration, authentication and account management
- **Endpoints**: `/api/v1/users/*`
- **Features**: User creation, balance inquiry, account recharge

### 👤 User Management V2
- **Description**: Enhanced user management features (API v2)
- **Endpoints**: `/api/v2/users/*`
- **Features**: Additional user information and enhanced functionality

### 🏪 Merchant Management
- **Description**: Merchant registration, product management and income tracking
- **Endpoints**: `/api/v1/merchants/*`
- **Features**: Merchant registration, product management, inventory control, income inquiry

### 🛒 Product & Purchase
- **Description**: Product browsing, purchasing and order management
- **Endpoints**: `/api/v1/ecommerce/*`
- **Features**: Product queries, purchase orders, order cancellation

## Key Features

### 🔐 Security Authentication
- **Authentication Method**: JWT Bearer Token
- **Configuration**: Pre-configured JWT security scheme
- **Usage**: Click "Authorize" button in Swagger UI to input token

### 📊 API Version Control
- **Supported Versions**: v1, v2
- **Version Strategies**: 
  - URL path versioning: `/api/v1/*`, `/api/v2/*`
  - Request header versioning: `API-Version: v1`
  - Query parameter versioning: `?version=v1`

### 📝 Complete Documentation Annotations
- **@Operation**: Operation descriptions and summaries
- **@ApiResponse**: HTTP status codes and descriptions (200, 400, 404, etc.)
- **@Parameter**: Parameter descriptions and examples
- **@Schema**: Data model definitions
- **@Tag**: API grouping tags

### 📋 Response Code System
The project uses a two-layer response code system:
- **HTTP Status Codes**: Standard HTTP codes (200, 400, 404, 500, etc.) for transport layer
- **Business Codes**: Application-specific codes in response body:
  - `"SUCCESS"` - Operation completed successfully
  - `"VALIDATION_ERROR"` - Request validation failed
  - `"RESOURCE_NOT_FOUND"` - Requested resource not found
  - `"INSUFFICIENT_BALANCE"` - Insufficient user balance
  - `"BUSINESS_ERROR"` - General business logic error
  - And other specific business error codes

### 🎯 Unified Response Format
All API responses use the unified `Result<T>` format:
```json
{
  "code": "SUCCESS",
  "message": "Operation completed successfully",
  "data": { ... },
  "timestamp": "2025-01-01T12:00:00"
}
```

## Usage Guide

### 1. Start Application
```bash
mvn spring-boot:run
```

### 2. Access Swagger UI
Open browser and visit: `http://localhost:8080/swagger-ui/index.html` (recommended)

### 3. Explore APIs
- View APIs for each business module
- Click specific endpoints to view detailed information
- Use "Try it out" functionality to test APIs

### 4. Authentication Testing
For testing endpoints that require authentication:
1. Click the "Authorize" button in the top right corner
2. Enter JWT token (format: `Bearer your-token-here`)
3. Click "Authorize" to confirm

### 5. Version Switching
In the top right corner of Swagger UI, you can select different API version groups:
- **API Version 1**: View v1 version APIs
- **API Version 2**: View v2 version APIs
- **Public APIs**: View public APIs

## Configuration Details

### Dependency Configuration
```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.3.0</version>
</dependency>
```

### Application Configuration (application.yml)
```yaml
springdoc:
  api-docs:
    path: /v3/api-docs
    enabled: true
  swagger-ui:
    path: /swagger-ui.html
    enabled: true
    try-it-out-enabled: true
    operations-sorter: method
    tags-sorter: alpha
  show-actuator: false
  group-configs:
    - group: v1
      paths-to-match: /api/v1/**
      display-name: API Version 1
    - group: v2
      paths-to-match: /api/v2/**
      display-name: API Version 2
    - group: public
      paths-to-match: /api/health, /api/healthz, /api/version/**
      display-name: Public APIs
```

## Best Practices

### 1. Annotation Usage
- Add `@Tag` annotations to each controller
- Add `@Operation` annotations to each endpoint
- Add `@Parameter` annotations to important parameters
- Add `@Schema` annotations to DTO classes

### 2. Documentation Maintenance
- Keep annotation descriptions accurate and up-to-date
- Provide meaningful example data
- Regularly update API version information

### 3. Security Considerations
- Consider restricting Swagger UI access in production environments
- Don't expose sensitive information in examples
- Properly configure authentication and authorization

## Technical Advantages

### ✅ Why Choose Springdoc
1. **Modern**: Supports OpenAPI 3.0 standard
2. **Compatibility**: Perfect support for Spring Boot 3.x and Jakarta EE
3. **Active Maintenance**: Continuously updated and maintained
4. **Feature Rich**: Supports various advanced features

### ❌ Why Not Choose Springfox
1. **No Longer Maintained**: Last updated in 2020
2. **Incompatible**: Does not support Spring Boot 3.x and Jakarta EE
3. **Outdated Technology**: Based on Swagger 2.0 standard

## Troubleshooting

### Common Issues
1. **Swagger UI inaccessible**: Check if application started properly
2. **Incomplete API documentation**: Verify controller annotations are correct
3. **Authentication failures**: Confirm JWT token format is correct

### Debugging Methods
- Check console logs
- Verify `/v3/api-docs` endpoint is working properly
- Validate application configuration is correct
