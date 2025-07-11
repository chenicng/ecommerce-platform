# E-commerce Platform

A comprehensive e-commerce platform built with **Spring Boot 3.2** and **Domain-Driven Design (DDD)**, featuring user management, merchant operations, product trading, and automated settlement systems.

## 🚀 Quick Start

### Prerequisites
- **Java 21+**
- **Maven 3.6+**
- **MySQL 8.0+** (for production mode)

### Development Mode (Default)
```bash
# Clone the repository
git clone https://github.com/chenlicong0821/ecommerce-platform.git
cd ecommerce-platform

# Start with mock data (in-memory storage)
mvn clean spring-boot:run
```

### Production Mode (MySQL)
```bash
# Create database
mysql -u root -p
CREATE DATABASE ecommerce_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# Set environment variables
export DB_USERNAME=your_username
export DB_PASSWORD=your_password

# Start with MySQL profile
mvn clean spring-boot:run -Dspring-boot.run.profiles=mysql
```

### Access Points
- **Application**: http://localhost:8080
- **Health Check**: http://localhost:8080/actuator/health
- **API Documentation**: http://localhost:8080/swagger-ui/index.html

## 📋 Table of Contents

1. [System Overview](#system-overview)
2. [Architecture](#architecture)
3. [Core Features](#core-features)
4. [API Documentation](#api-documentation)
5. [Testing](#testing)
6. [Configuration](#configuration)
7. [Development Guide](#development-guide)
8. [Production Deployment](#production-deployment)

## 🏗️ System Overview

### Technology Stack
- **Framework**: Spring Boot 3.2.0
- **Language**: Java 21
- **Database**: MySQL 8.0 (production) / In-memory (development)
- **API Documentation**: Springdoc OpenAPI 3.0 + Swagger UI
- **Testing**: JUnit 5 + TestContainers + Mockito
- **Build Tool**: Maven
- **Architecture**: Domain-Driven Design (DDD)

### Key Features
- ✅ **User Management**: Registration, account management, balance operations
- ✅ **Merchant Management**: Merchant registration, product management, income tracking
- ✅ **Product Trading**: Complete purchase workflow with inventory management
- ✅ **Automated Settlement**: Daily merchant settlement with scheduled tasks
- ✅ **API Versioning**: Support for multiple API versions (v1, v2)
- ✅ **Comprehensive Testing**: Full unit and integration test coverage
- ✅ **Interactive Documentation**: Swagger UI with live testing capabilities
- ✅ **Performance Monitoring**: Request logging, execution time tracking, and metrics
- ✅ **Security Features**: Input validation, rate limiting, and sensitive data masking

## 🏛️ Architecture

### Domain-Driven Design (DDD) Structure

```
src/main/java/com/ecommerce/
├── api/                    # Application API Layer
│   ├── controller/        # REST Controllers
│   ├── dto/              # Data Transfer Objects
│   ├── exception/         # Global Exception Handling
│   ├── aspect/            # Cross-cutting Concerns
│   └── interceptor/       # Request Interceptors
├── application/           # Application Service Layer
│   ├── service/           # Application Services
│   └── dto/              # Application DTOs
├── domain/               # Domain Layer
│   ├── user/             # User Domain
│   ├── merchant/         # Merchant Domain
│   ├── product/          # Product Domain
│   ├── order/            # Order Domain
│   └── settlement/       # Settlement Domain
└── infrastructure/       # Infrastructure Layer
    ├── repository/        # Data Access Layer
    ├── config/           # Configuration
    └── scheduler/        # Scheduled Tasks
```

### Core Domains

#### 1. User Domain
- **User**: Aggregate root managing user information
- **UserAccount**: Value object for balance operations
- **UserStatus**: Enum for user state management

#### 2. Merchant Domain
- **Merchant**: Aggregate root for merchant operations
- **MerchantAccount**: Value object for income management
- **MerchantStatus**: Enum for merchant state management

#### 3. Product Domain
- **Product**: Aggregate root for product information
- **ProductInventory**: Value object for inventory operations
- **ProductStatus**: Enum for product state management

#### 4. Order Domain
- **Order**: Aggregate root for order processing
- **OrderItem**: Value object for order details
- **OrderStatus**: Enum for order state transitions

#### 5. Settlement Domain
- **Settlement**: Aggregate root for settlement records
- **SettlementStatus**: Enum for settlement state management

## 🎯 Core Features

### User Management
- **User Registration**: Create new user accounts
- **Account Recharge**: Add funds to user accounts
- **Balance Inquiry**: Check current account balance
- **Account Status**: Active/Inactive user management

### Merchant Management
- **Merchant Registration**: Create merchant accounts
- **Product Management**: Add, update, and manage products
- **Inventory Control**: Real-time inventory tracking
- **Income Tracking**: Monitor sales and revenue

### Product Trading
- **Product Browsing**: Public product catalog
- **Purchase Process**: Complete transaction workflow
- **Inventory Deduction**: Automatic stock management
- **Order Management**: Order creation and tracking

### Automated Settlement
- **Daily Settlement**: Scheduled merchant settlement (2 AM daily)
- **Income Verification**: Compare expected vs actual income
- **Settlement Reports**: Generate settlement records
- **Account Reconciliation**: Balance verification

## 📚 API Documentation

For comprehensive API documentation, examples, and testing scenarios, see [API Documentation](API_DOCUMENTATION.md).

### Interactive Documentation
- **Swagger UI**: http://localhost:8080/swagger-ui/index.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs
- **Grouped APIs**: Version-specific documentation

### API Groups
- **🏥 Health Check**: System monitoring endpoints
- **📋 API Version**: Version information and compatibility
- **👤 User Management**: User operations (v1 & v2)
- **🏪 Merchant Management**: Merchant and product operations
- **🛒 Product & Purchase**: E-commerce operations

### Response Format
All APIs use a unified response format:
```json
{
  "code": "SUCCESS",
  "message": "Operation completed successfully",
  "data": { ... },
  "timestamp": "2025-07-11T12:00:00"
}
```

### Quick API Examples

For detailed API documentation and examples, see [API Documentation](API_DOCUMENTATION.md).

```bash
# Create user
curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","email":"test@example.com","phone":"13800138000"}'

# Purchase product
curl -X POST http://localhost:8080/api/v1/ecommerce/purchase \
  -H "Content-Type: application/json" \
  -d '{"userId":1,"sku":"BOOK-001","quantity":1}'
```

## 🧪 Testing

### Test Structure
- **Unit Tests**: Comprehensive business logic coverage
- **Integration Tests**: TestContainers for database testing
- **Coverage**: Full code coverage with JaCoCo

### Running Tests
```bash
# Run all tests
mvn test

# Run with coverage report
mvn clean test

# View coverage report
open target/site/jacoco/index.html
```

### Test Reports
- **Surefire Reports**: `target/surefire-reports/`
- **JaCoCo Coverage**: `target/site/jacoco/index.html`


## ⚙️ Configuration

### Application Profiles

#### Mock Profile (Default)
- **Storage**: In-memory with demo data
- **Database**: Not required
- **Use Case**: Development and demonstration

#### MySQL Profile (Production)
- **Storage**: MySQL database
- **Database**: MySQL 8.0+
- **Use Case**: Production deployment

### Key Configuration Sections

#### Database Configuration
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ecommerce_db
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:password}
  jpa:
    hibernate:
      ddl-auto: update
```

#### Settlement Configuration
```yaml
ecommerce:
  settlement:
    cron: "0 0 2 * * ?"  # Daily at 2 AM
    enabled: true
```

#### API Documentation
```yaml
springdoc:
  swagger-ui:
    path: /swagger-ui.html
    try-it-out-enabled: true
```

## 🛠️ Development Guide

### Project Structure
```
ecommerce-platform/
├── src/
│   ├── main/
│   │   ├── java/com/ecommerce/
│   │   │   ├── api/           # API Layer
│   │   │   ├── application/   # Application Layer
│   │   │   ├── domain/        # Domain Layer
│   │   │   └── infrastructure/ # Infrastructure Layer
│   │   └── resources/
│   │       └── application.yml # Configuration
│   └── test/                  # Test Suite
├── logs/                      # Application Logs
├── pom.xml                    # Maven Configuration
└── README.md                  # This File
```

### Development Workflow
1. **Setup**: Clone repository and install dependencies
2. **Development**: Use mock profile for rapid development
3. **Testing**: Run comprehensive test suite
4. **Documentation**: Update API documentation
5. **Deployment**: Use MySQL profile for production

### Code Quality
- **Architecture**: DDD principles
- **Testing**: Comprehensive unit and integration tests
- **Documentation**: Complete API documentation
- **Logging**: Structured logging with request tracking
- **Error Handling**: Global exception handling

## 🚀 Production Deployment

### Environment Setup
1. **Database**: MySQL 8.0+ with proper configuration
2. **Java**: Java 21 runtime environment
3. **Memory**: Minimum 2GB RAM recommended
4. **Storage**: Adequate disk space for logs and data

### Configuration
```bash
# Environment variables
export DB_USERNAME=production_user
export DB_PASSWORD=secure_password
export SPRING_PROFILES_ACTIVE=mysql

# Start application
java -jar ecommerce-platform-1.0.0-SNAPSHOT.jar
```

### Monitoring
- **Health Checks**: `/actuator/health`
- **Metrics**: `/actuator/metrics`
- **Application Info**: `/actuator/info`
- **Logs**: `logs/ecommerce-platform.log`

### Security Considerations
- **Database Security**: Use strong passwords and proper access controls
- **Network Security**: Configure firewall rules
- **API Security**: Implement authentication and authorization
- **Logging**: Secure log storage and rotation
- **Backup**: Regular database backups

### Performance Optimization
- **Database**: Optimize indexes and queries
- **Caching**: Consider Redis for session storage
- **Connection Pooling**: Configure database connection pool
- **JVM Tuning**: Optimize heap size and garbage collection

## 🔧 Troubleshooting

### Common Issues

#### Application Won't Start
```bash
# Check Java version
java -version

# Check port availability
netstat -an | grep 8080

# Check logs
tail -f logs/ecommerce-platform.log
```

#### Database Connection Issues
```bash
# Verify MySQL is running
sudo systemctl status mysql

# Test database connection
mysql -u username -p -h localhost ecommerce_db

# Check environment variables
echo $DB_USERNAME
echo $DB_PASSWORD
```

#### API Documentation Not Accessible
- Verify application is running on port 8080
- Check if Swagger UI is enabled in configuration
- Ensure no firewall blocking access


## 📈 Extensibility

### Future Enhancements
- **Payment Integration**: Multiple payment gateways
- **Multi-currency**: Support for various currencies
- **Promotional System**: Discounts and coupons
- **Inventory Alerts**: Low stock notifications
- **Analytics**: Sales and performance reports
- **Microservices**: Service decomposition

### Contributing
1. Fork the repository
2. Create a feature branch
3. Implement changes with tests
4. Update documentation
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🤝 Support

For questions, issues, or contributions:
- **Issues**: [GitHub Issues](https://github.com/chenlicong0821/ecommerce-platform/issues)
- **Documentation**: [API Documentation](http://localhost:8080/swagger-ui/index.html)
- **Health Check**: [System Status](http://localhost:8080/actuator/health)

---

**Built with ❤️ using Spring Boot 3.2 and Domain-Driven Design**