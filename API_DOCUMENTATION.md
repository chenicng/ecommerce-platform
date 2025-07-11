# E-commerce Platform API Documentation

## 📖 Overview

This document provides comprehensive API documentation through **Springdoc OpenAPI 3.0** and **Swagger UI**, offering interactive testing capabilities and complete API reference.

> **Note**: For project overview, quick start, and architecture information, see [README.md](README.md).

## 🔗 Access Points

### Interactive Documentation
- **Primary URL**: http://localhost:8080/swagger-ui/index.html
- **Alternative URL**: http://localhost:8080/swagger-ui.html
- **Features**: Interactive API testing with live request/response capabilities

### OpenAPI Documentation
- **JSON Format**: http://localhost:8080/v3/api-docs
- **Purpose**: Standard OpenAPI 3.0 specification for client generation

### Version-Specific Documentation
- **V1 APIs**: http://localhost:8080/v3/api-docs/v1
- **V2 APIs**: http://localhost:8080/v3/api-docs/v2
- **Public APIs**: http://localhost:8080/v3/api-docs/public

## 📋 API Groups

### 🏥 Health Check APIs
**Description**: System health monitoring and status endpoints
- **Endpoints**: `/api/health`
- **Purpose**: Service health checks, load balancer integration, monitoring

### 📋 API Version Management
**Description**: API version information and compatibility checking
- **Endpoints**: `/api/version/*`
- **Purpose**: Version discovery, compatibility verification

### 👤 User Management (V1)
**Description**: Core user operations and account management
- **Endpoints**: `/api/v1/users/*`
- **Features**: User registration, balance management, account operations

### 👤 User Management (V2)
**Description**: Enhanced user management with additional features
- **Endpoints**: `/api/v2/users/*`
- **Features**: Extended user information, advanced account features

### 🏪 Merchant Management
**Description**: Merchant operations and product management
- **Endpoints**: `/api/v1/merchants/*`
- **Features**: Merchant registration, product management, inventory control, income tracking

### 🛒 E-commerce Operations
**Description**: Product browsing and purchase operations
- **Endpoints**: `/api/v1/ecommerce/*`
- **Features**: Product catalog, purchase processing, order management

## 🔐 Security & Authentication

### JWT Bearer Token Authentication
- **Method**: JWT Bearer Token
- **Format**: `Bearer <token>`
- **Configuration**: Pre-configured in Swagger UI
- **Usage**: Click "Authorize" button in Swagger UI interface

### Authentication Flow
1. Click "Authorize" button in Swagger UI
2. Enter JWT token in format: `Bearer your-token-here`
3. Click "Authorize" to confirm
4. All subsequent requests will include the token

## 📊 API Versioning Strategy

### Supported Versions
- **v1**: Current stable version
- **v2**: Enhanced features and improvements

### Version Implementation
- **URL Path**: `/api/v1/*`, `/api/v2/*`
- **Request Header**: `API-Version: v1`
- **Query Parameter**: `?version=v1`

### Version Selection in Swagger UI
- Use the version selector in the top-right corner
- Switch between different API groups
- View version-specific documentation

## 📝 Response Format

### Unified Response Structure
All API responses follow a consistent format:

```json
{
  "code": "SUCCESS",
  "message": "Operation completed successfully",
  "data": {
    // Response data
  },
  "timestamp": "2025-01-01T12:00:00"
}
```

### Response Codes
- **HTTP Status Codes**: Standard HTTP codes (200, 400, 404, 500)
- **Business Codes**: Application-specific codes in response body

#### Business Error Codes
- `SUCCESS` - Operation completed successfully
- `VALIDATION_ERROR` - Request validation failed
- `RESOURCE_NOT_FOUND` - Requested resource not found
- `INSUFFICIENT_BALANCE` - Insufficient user balance
- `INSUFFICIENT_INVENTORY` - Insufficient product inventory
- `BUSINESS_ERROR` - General business logic error
- `INTERNAL_ERROR` - Internal server error

## 🎯 API Examples

### User Management

#### Create User
```bash
curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","email":"test@example.com","phone":"13800138000"}'
```

#### User Account Recharge
```bash
curl -X POST http://localhost:8080/api/v1/users/1/recharge \
  -H "Content-Type: application/json" \
  -d '{"amount":1000.00,"currency":"CNY"}'
```

#### Query User Balance
```bash
curl -X GET http://localhost:8080/api/v1/users/1/balance
```

### Merchant Management

#### Create Merchant
```bash
curl -X POST http://localhost:8080/api/v1/merchants \
  -H "Content-Type: application/json" \
  -d '{"merchantName":"Test Store","businessLicense":"TEST123","contactEmail":"test@store.com","contactPhone":"13900139000"}'
```

#### Create Product
```bash
curl -X POST http://localhost:8080/api/v1/merchants/1/products \
  -H "Content-Type: application/json" \
  -d '{"sku":"PRODUCT1","name":"Test Product","price":100.00,"initialInventory":10}'
```

#### Add Inventory
```bash
curl -X POST http://localhost:8080/api/v1/merchants/1/products/PRODUCT1/inventory/add \
  -H "Content-Type: application/json" \
  -d '{"quantity":50}'
```

#### Query Merchant Income
```bash
curl -X GET http://localhost:8080/api/v1/merchants/1/income
```

### Product Operations

#### Browse Available Products
```bash
# Get all available products
curl -X GET http://localhost:8080/api/v1/ecommerce/products

# Search products by name
curl -X GET "http://localhost:8080/api/v1/ecommerce/products?search=product"

# Filter by merchant
curl -X GET "http://localhost:8080/api/v1/ecommerce/products?merchantId=1"
```

#### Get Product Details
```bash
curl -X GET http://localhost:8080/api/v1/ecommerce/products/PRODUCT1
```

#### Check Product Inventory
```bash
curl -X GET http://localhost:8080/api/v1/ecommerce/products/PRODUCT1/inventory
```

### Purchase Operations

#### Purchase Product
```bash
curl -X POST http://localhost:8080/api/v1/ecommerce/purchase \
  -H "Content-Type: application/json" \
  -d '{"userId":1,"sku":"PRODUCT1","quantity":1}'
```

## 🔧 Configuration

### Springdoc Configuration
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
```

### Maven Dependency
```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.3.0</version>
</dependency>
```

## 📚 Documentation Best Practices

### Controller Annotations
```java
@Tag(name = "User Management", description = "User registration and account operations")
@RestController
@RequestMapping(ApiVersionConfig.API_V1 + "/users")
public class UserController {
    
    @Operation(summary = "Create new user", description = "Register a new user account")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "User created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "409", description = "User already exists")
    })
    @PostMapping
    public Result<UserResponse> createUser(@RequestBody @Valid CreateUserRequest request) {
        // Implementation
    }
}
```

### DTO Annotations
```java
@Schema(description = "User creation request")
public class CreateUserRequest {
    
    @Schema(description = "Username", example = "john_doe", required = true)
    @NotBlank(message = "Username is required")
    private String username;
    
    @Schema(description = "Email address", example = "john@example.com", required = true)
    @Email(message = "Invalid email format")
    private String email;
}
```

## 🚀 Getting Started

### 1. Start Application
```bash
mvn spring-boot:run
```

### 2. Access Swagger UI
Open browser and navigate to: http://localhost:8080/swagger-ui/index.html

### 3. Explore APIs
- Browse API groups by clicking on different sections
- View detailed endpoint information
- Test APIs using the "Try it out" functionality

### 4. Authentication Testing
1. Click "Authorize" button in Swagger UI
2. Enter JWT token: `Bearer your-token-here`
3. Click "Authorize" to confirm
4. Test protected endpoints

### 5. Version Switching
Use the version selector in Swagger UI to switch between:
- **API Version 1**: Core functionality
- **API Version 2**: Enhanced features
- **Public APIs**: Health checks and version info

## 🔍 API Testing Workflow

### Quick Test Scenario
```bash
# Create user and product
curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","email":"test@example.com","phone":"13800138000"}'

curl -X POST http://localhost:8080/api/v1/merchants/1/products \
  -H "Content-Type: application/json" \
  -d '{"sku":"PRODUCT1","name":"Test Product","price":100.00,"initialInventory":10}'

# Purchase and verify
curl -X POST http://localhost:8080/api/v1/ecommerce/purchase \
  -H "Content-Type: application/json" \
  -d '{"userId":1,"sku":"PRODUCT1","quantity":1}'

curl http://localhost:8080/api/v1/users/1/balance
```

## 🛠️ Troubleshooting

### Common Issues

#### Swagger UI Not Accessible
- Verify application is running on port 8080
- Check if springdoc is enabled in configuration
- Ensure no firewall blocking access

#### API Documentation Incomplete
- Verify controller annotations are properly configured
- Check for compilation errors
- Ensure all DTOs have proper schema annotations

#### Authentication Issues
- Verify JWT token format: `Bearer <token>`
- Check token expiration
- Ensure token has required permissions

#### Version-Specific Endpoints Not Working
- Ensure GroupedOpenApi beans are properly configured
- Check that only one primary OpenAPI bean exists
- Verify path patterns match controller endpoints


## 📈 Advanced Features

### Custom Response Examples
```java
@ApiResponse(
    responseCode = "200",
    description = "User created successfully",
    content = @Content(
        mediaType = "application/json",
        schema = @Schema(implementation = UserResponse.class)
    )
)
```

### Parameter Validation
```java
@Parameter(
    name = "search",
    description = "Search term for product name or description",
    example = "product"
)
@RequestParam(required = false) String search
```

## 🔄 API Evolution

### Versioning Strategy
- **Backward Compatibility**: Maintain compatibility within major versions
- **Deprecation Policy**: Clear deprecation notices and migration guides
- **Breaking Changes**: Only in major version updates

## 📞 Support

For API-related questions or issues:
- **Documentation**: http://localhost:8080/swagger-ui/index.html
- **Health Check**: http://localhost:8080/actuator/health
- **Issues**: [GitHub Issues](https://github.com/chenlicong0821/ecommerce-platform/issues)

---

**API Documentation powered by Springdoc OpenAPI 3.0**
