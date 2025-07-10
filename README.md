# E-commerce Platform

A complete e-commerce platform system based on Spring Boot 3.2 and DDD (Domain-Driven Design), supporting user product purchases, merchant inventory management, automatic settlement, and other features. The system supports both development mode (in-memory storage) and production mode (MySQL database).

## System Features

### Core Functions
- **User Management**: User registration, account recharge, balance inquiry
- **Merchant Management**: Merchant registration, product management, inventory management, income inquiry
- **Product Trading**: Complete purchase process, including inventory deduction and fund transfer
- **Automatic Settlement**: Scheduled tasks for daily merchant settlement, matching income with account balance

### Technical Architecture
- **Domain-Driven Design (DDD)**: Clear domain division and aggregate root design
- **Spring Boot 3.2**: Modern Spring framework
- **MySQL Database**: Production-ready database with JPA support
- **Mock Mode**: In-memory storage for demonstration and development
- **REST API**: Complete RESTful interface design
- **Scheduled Tasks**: Settlement tasks based on Spring Scheduler
- **TestContainers**: Integration testing support

## Quick Start

### Environment Requirements
- Java 21+
- Maven 3.6+
- MySQL 8.0+ (for production mode)

### Start Application

#### Development Mode (Mock - Default)
```bash
mvn clean spring-boot:run
```

#### Production Mode (MySQL)
```bash
# First, ensure MySQL is running and create database
mysql -u root -p
CREATE DATABASE ecommerce_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# Set environment variables for database connection
export DB_USERNAME=your_username
export DB_PASSWORD=your_password

# Run with MySQL profile
mvn clean spring-boot:run -Dspring-boot.run.profiles=mysql
```

After application startup, access:
- Application port: http://localhost:8080
- Health check: http://localhost:8080/actuator/health

## API Documentation

### User Management

#### Create User
```bash
POST /api/users
Content-Type: application/json

{
  "username": "testuser",
  "email": "test@example.com",
  "phone": "13800138000"
}
```

#### User Recharge
```bash
POST /api/users/{userId}/recharge
Content-Type: application/json

{
  "amount": 10000.00,
  "currency": "CNY"
}
```

#### Query Balance
```bash
GET /api/users/{userId}/balance
```

### Merchant Management

#### Create Merchant
```bash
POST /api/merchants
Content-Type: application/json

{
  "merchantName": "Test Merchant",
  "businessLicense": "12345678",
  "contactEmail": "merchant@example.com",
  "contactPhone": "13900139000"
}
```

#### Create Product
```bash
POST /api/merchants/{merchantId}/products
Content-Type: application/json

{
  "sku": "IPHONE15",
  "name": "iPhone 15",
  "description": "Latest iPhone model",
  "price": 6999.00,
  "initialInventory": 100
}
```

#### Add Inventory
```bash
POST /api/merchants/{merchantId}/products/{sku}/add-inventory
Content-Type: application/json

{
  "quantity": 50
}
```

#### Query Income
```bash
GET /api/merchants/{merchantId}/income
```

### Product Browsing and Inventory Management

#### Get Product Details by SKU
```bash
GET /api/ecommerce/products/{sku}
```

#### Browse Available Products (Public Interface)
```bash
GET /api/ecommerce/products
# This endpoint is for public product browsing and purchasing
# Only returns AVAILABLE products (active and have inventory)
# Optional query parameters:
# - search: Search products by name or description
# - merchantId: Filter products by specific merchant
# - Combined: Search within specific merchant's products
GET /api/ecommerce/products?search=iPhone
GET /api/ecommerce/products?merchantId=1
GET /api/ecommerce/products?search=iPhone&merchantId=1
```

#### Check Product Inventory
```bash
GET /api/ecommerce/products/{sku}/inventory
```

#### Get Merchant's Products (Management Interface)
```bash
GET /api/merchants/{merchantId}/products
# This endpoint is for merchant product management
# Returns ALL products (including inactive/deleted) for management purposes
# Optional query parameters:
# - status: Filter by product status (ACTIVE, INACTIVE, DELETED)
# - search: Search within merchant's products (by name, description, or SKU)
# - Combined: Search and filter by status
GET /api/merchants/{merchantId}/products?status=ACTIVE
GET /api/merchants/{merchantId}/products?search=iPhone
GET /api/merchants/{merchantId}/products?search=iPhone&status=ACTIVE
```

#### Get Single Product for Merchant
```bash
GET /api/merchants/{merchantId}/products/{sku}
```

### Product Trading

#### Purchase Product
```bash
POST /api/ecommerce/purchase
Content-Type: application/json

{
  "userId": 1,
  "sku": "IPHONE15",
  "quantity": 1
}
```

## Complete Test Flow

The following is a complete test flow example:

```bash
# 1. Create user
curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","email":"test@example.com","phone":"13800138000"}'

# 2. Create merchant
curl -X POST http://localhost:8080/api/merchants \
  -H "Content-Type: application/json" \
  -d '{"merchantName":"Test Merchant","businessLicense":"12345678","contactEmail":"merchant@example.com","contactPhone":"13900139000"}'

# 3. Create product
curl -X POST http://localhost:8080/api/merchants/1/products \
  -H "Content-Type: application/json" \
  -d '{"sku":"IPHONE15","name":"iPhone 15","description":"Latest iPhone model","price":6999.00,"initialInventory":100}'

# 4. User recharge
curl -X POST http://localhost:8080/api/users/1/recharge \
  -H "Content-Type: application/json" \
  -d '{"amount":10000.00,"currency":"CNY"}'

# 5. Purchase product
curl -X POST http://localhost:8080/api/ecommerce/purchase \
  -H "Content-Type: application/json" \
  -d '{"userId":1,"sku":"IPHONE15","quantity":1}'

# 6. Query user balance (should decrease by 6999.00)
curl http://localhost:8080/api/users/1/balance

# 7. Query merchant income (should increase by 6999.00)
curl http://localhost:8080/api/merchants/1/income

# 8. Add inventory
curl -X POST http://localhost:8080/api/merchants/1/products/IPHONE15/add-inventory \
  -H "Content-Type: application/json" \
  -d '{"quantity":50}'

# 9. Browse available products
curl http://localhost:8080/api/ecommerce/products

# 10. Search products by name (global search)
curl "http://localhost:8080/api/ecommerce/products?search=iPhone"

# 11. Get products from specific merchant (public browsing)
curl "http://localhost:8080/api/ecommerce/products?merchantId=1"

# 12. Search within specific merchant's products (combined search)
curl "http://localhost:8080/api/ecommerce/products?search=iPhone&merchantId=1"

# 13. Get product details
curl http://localhost:8080/api/ecommerce/products/IPHONE15

# 14. Check product inventory
curl http://localhost:8080/api/ecommerce/products/IPHONE15/inventory

# 15. Get merchant's products (management interface - all products)
curl http://localhost:8080/api/merchants/1/products

# 16. Get merchant's active products only
curl "http://localhost:8080/api/merchants/1/products?status=ACTIVE"

# 17. Search within merchant's products (management interface)
curl "http://localhost:8080/api/merchants/1/products?search=iPhone"

# 18. Combined search and status filter for merchant
curl "http://localhost:8080/api/merchants/1/products?search=iPhone&status=ACTIVE"
```

## Testing

### Run Unit Tests
```bash
mvn test
```

### Run Tests with Coverage Report
```bash
mvn clean test
```

### Test Reports

After running tests, the following reports will be generated:

#### 1. Surefire Test Reports
- **Location**: `target/surefire-reports/`
- **Description**: Detailed test execution results for each test class
- **Formats**:
  - XML files (`TEST-*.xml`): Machine-readable format for CI/CD systems
  - Text files (`*.txt`): Human-readable summary for each test class

#### 2. JaCoCo Code Coverage Reports  
- **Location**: `target/site/jacoco/`
- **Main Report**: `target/site/jacoco/index.html` (open in browser)
- **Description**: Interactive HTML reports showing code coverage by package, class, and method
- **Additional Formats**:
  - CSV: `target/site/jacoco/jacoco.csv`
  - XML: `target/site/jacoco/jacoco.xml`

#### 3. View Coverage Report
```bash
# Open the main coverage report in browser (macOS)
open target/site/jacoco/index.html

# Or use your preferred browser
open -a "Google Chrome" target/site/jacoco/index.html


#### 4. Quick Test Summary
```bash
# View test summary from surefire reports
find target/surefire-reports -name "*.txt" -exec cat {} \; | grep "Tests run"
```

#### 5. Current Test Status
- ✅ **Total Tests**: 208
- ✅ **Passed**: 208  
- ❌ **Failed**: 0
- ⚠️ **Errors**: 0
- ⏭️ **Skipped**: 0

### Integration Testing
The project uses TestContainers for integration testing, which automatically manages database containers during testing. No additional setup is required.

## Database Architecture

### Profiles
- **Mock Profile (Default)**: All data is stored in memory using mock repositories. Perfect for development and demonstration.
- **MySQL Profile**: Production-ready database persistence with JPA entities and automatic schema management.

### Schema Management
When running with MySQL profile, JPA will automatically create/update tables based on domain entities. The system uses:
- **Hibernate DDL Auto**: `update` mode for automatic schema evolution
- **Character Set**: UTF8MB4 for full Unicode support
- **Timezone**: Asia/Shanghai

## System Design

### DDD Domain Division

1. **User Context**
   - User aggregate root: Manages user information and prepaid account
   - UserAccount value object: Encapsulates account balance operations
   - UserStatus enum: User status management

2. **Merchant Context**
   - Merchant aggregate root: Manages merchant information and income account
   - MerchantAccount value object: Encapsulates income and balance management
   - MerchantStatus enum: Merchant status management

3. **Product Context**
   - Product aggregate root: Manages product information, price and inventory
   - ProductInventory value object: Encapsulates inventory operations
   - ProductStatus enum: Product status management

4. **Order Context**
   - Order aggregate root: Manages order process and status
   - OrderItem value object: Order item details
   - OrderStatus enum: Order status transitions

5. **Settlement Context**
   - Settlement aggregate root: Manages settlement records and status
   - SettlementStatus enum: Settlement status management

### Core Business Processes

#### Purchase Process
1. Validate user, product, and merchant status
2. Check inventory and balance sufficiency
3. Create and confirm order
4. Reduce product inventory
5. Deduct user account balance
6. Increase merchant income
7. Complete order and return results

#### Settlement Process
1. Scheduled task triggers daily at 2 AM
2. Calculate merchant expected income (based on order records)
3. Get merchant actual balance
4. Compare and record differences
5. Generate settlement report

## Configuration Instructions

Application configuration file `application.yml` contains the following important configurations:

### Profiles
- **mock profile** (default): Uses in-memory storage for demo purposes
- **mysql profile**: Uses MySQL database for production

### Key Configuration Sections
- **Database Configuration**: MySQL connection settings (production mode)
- **Settlement Configuration**: Scheduled task cron expression (daily at 2 AM)
- **Currency Configuration**: Default currency (CNY) and precision settings (2 decimal places)
- **Logging Configuration**: Console logging patterns and application log levels
- **Actuator Configuration**: Health check and monitoring endpoints

### Environment Variables (MySQL mode)
- `DB_USERNAME`: Database username (default: root)
- `DB_PASSWORD`: Database password (default: password)

## Production Environment Considerations

The system supports both development (mock) and production (MySQL) modes. For production deployment, consider:

1. **Database Setup**: MySQL 8.0+ with proper connection pooling and performance tuning
2. **Transaction Management**: JPA transactions ensure data consistency and concurrency safety
3. **Environment Configuration**: Use environment variables for sensitive database credentials
4. **Caching**: Consider Redis for session storage and hot data caching
5. **Monitoring**: Actuator endpoints provide health checks and metrics
6. **Security**: Add authentication and authorization mechanisms
7. **Performance**: Database index optimization and query optimization
8. **Scalability**: Consider microservice splitting and distributed deployment
9. **Testing**: Integrated TestContainers for database integration testing

## Extensibility

The system uses DDD design with good extensibility:

- **New Payment Methods**: Extend payment domain
- **Multi-currency Support**: Extend Money value object
- **Promotional Activities**: Add promotion context
- **Inventory Alerts**: Extend inventory management functions
- **Data Reports**: Add analysis and reporting functions

## License

MIT License