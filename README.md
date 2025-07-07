# E-commerce Platform

A complete e-commerce platform system based on Spring Boot and DDD (Domain-Driven Design), supporting user product purchases, merchant inventory management, automatic settlement, and other features.

## System Features

### Core Functions
- **User Management**: User registration, account recharge, balance inquiry
- **Merchant Management**: Merchant registration, product management, inventory management, income inquiry
- **Product Trading**: Complete purchase process, including inventory deduction and fund transfer
- **Automatic Settlement**: Scheduled tasks for daily merchant settlement, matching income with account balance

### Technical Architecture
- **Domain-Driven Design (DDD)**: Clear domain division and aggregate root design
- **Spring Boot 3.2**: Modern Spring framework
- **In-Memory Storage**: Simplified data storage for demonstration (production should use database)
- **REST API**: Complete RESTful interface design
- **Scheduled Tasks**: Settlement tasks based on Spring Scheduler

## Quick Start

### Environment Requirements
- Java 21+
- Maven 3.6+

### Start Application
```bash
mvn clean spring-boot:run
```

After application startup, access:
- Application port: http://localhost:8080
- Health check: http://localhost:8080/actuator/health
- H2 console: http://localhost:8080/h2-console

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
  "initialStock": 100
}
```

#### Add Stock
```bash
POST /api/merchants/{merchantId}/products/{sku}/add-stock
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

#### Browse Available Products
```bash
GET /api/ecommerce/products
# Optional query parameters:
# - search: Search products by name or description
# - merchantId: Filter products by specific merchant
GET /api/ecommerce/products?search=iPhone
GET /api/ecommerce/products?merchantId=1
```

#### Check Product Stock
```bash
GET /api/ecommerce/products/{sku}/stock
```

#### Get Merchant's Products (Merchant Management)
```bash
GET /api/merchants/{merchantId}/products
# Optional query parameters:
# - status: Filter by product status (ACTIVE, INACTIVE, DELETED)
GET /api/merchants/{merchantId}/products?status=ACTIVE
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
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","email":"test@example.com","phone":"13800138000"}'

# 2. Create merchant
curl -X POST http://localhost:8080/api/merchants \
  -H "Content-Type: application/json" \
  -d '{"merchantName":"Test Merchant","businessLicense":"12345678","contactEmail":"merchant@example.com","contactPhone":"13900139000"}'

# 3. Create product
curl -X POST http://localhost:8080/api/merchants/1/products \
  -H "Content-Type: application/json" \
  -d '{"sku":"IPHONE15","name":"iPhone 15","description":"Latest iPhone model","price":6999.00,"initialStock":100}'

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

# 8. Add stock
curl -X POST http://localhost:8080/api/merchants/1/products/IPHONE15/add-stock \
  -H "Content-Type: application/json" \
  -d '{"quantity":50}'

# 9. Browse available products
curl http://localhost:8080/api/ecommerce/products

# 10. Search products by name
curl "http://localhost:8080/api/ecommerce/products?search=iPhone"

# 11. Get product details
curl http://localhost:8080/api/ecommerce/products/IPHONE15

# 12. Check product stock
curl http://localhost:8080/api/ecommerce/products/IPHONE15/stock

# 13. Get merchant's products
curl http://localhost:8080/api/merchants/1/products

# 14. Get merchant's active products only
curl "http://localhost:8080/api/merchants/1/products?status=ACTIVE"
```

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

- **Database Configuration**: H2 in-memory database
- **Settlement Configuration**: Scheduled task cron expression
- **Currency Configuration**: Default currency and precision settings
- **Logging Configuration**: SQL logs and application log levels

## Production Environment Considerations

The current system is a demonstration version. Production deployment should consider:

1. **Data Persistence**: Use relational databases like MySQL/PostgreSQL
2. **Transaction Management**: Ensure data consistency and concurrency safety
3. **Caching**: Use Redis and other caches for hot data
4. **Monitoring**: Integrate monitoring and alerting systems
5. **Security**: Add authentication and authorization mechanisms
6. **Performance**: Database index optimization and query optimization
7. **Scalability**: Consider microservice splitting and distributed deployment

## Extensibility

The system uses DDD design with good extensibility:

- **New Payment Methods**: Extend payment domain
- **Multi-currency Support**: Extend Money value object
- **Promotional Activities**: Add promotion context
- **Inventory Alerts**: Extend inventory management functions
- **Data Reports**: Add analysis and reporting functions

## License

MIT License