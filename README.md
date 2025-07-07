# 电商平台系统 (E-commerce Platform)

基于Spring Boot和DDD（领域驱动设计）的完整电商平台系统，支持用户购买商品、商家管理库存、自动结算等功能。

## 系统特性

### 核心功能
- **用户管理**: 用户注册、账户充值、余额查询
- **商家管理**: 商家注册、商品管理、库存管理、收入查询
- **商品交易**: 完整的购买流程，包括库存扣减、资金转移
- **自动结算**: 定时任务每天进行商家结算，匹配收入与账户余额

### 技术架构
- **领域驱动设计(DDD)**: 清晰的领域划分和聚合根设计
- **Spring Boot 3.2**: 现代化的Spring框架
- **内存存储**: 简化的数据存储，便于演示（生产环境应使用数据库）
- **REST API**: 完整的RESTful接口设计
- **定时任务**: 基于Spring Scheduler的结算任务

## 快速开始

### 环境要求
- Java 21+
- Maven 3.6+

### 启动应用
```bash
mvn clean spring-boot:run
```

应用启动后访问：
- 应用端口: http://localhost:8080
- 健康检查: http://localhost:8080/actuator/health
- H2控制台: http://localhost:8080/h2-console

## API 接口文档

### 用户管理

#### 创建用户
```bash
POST /api/users
Content-Type: application/json

{
  "username": "testuser",
  "email": "test@example.com",
  "phone": "13800138000"
}
```

#### 用户充值
```bash
POST /api/users/{userId}/recharge
Content-Type: application/json

{
  "amount": 10000.00,
  "currency": "CNY"
}
```

#### 查询余额
```bash
GET /api/users/{userId}/balance
```

### 商家管理

#### 创建商家
```bash
POST /api/merchants
Content-Type: application/json

{
  "merchantName": "测试商家",
  "businessLicense": "12345678",
  "contactEmail": "merchant@example.com",
  "contactPhone": "13900139000"
}
```

#### 创建商品
```bash
POST /api/merchants/{merchantId}/products
Content-Type: application/json

{
  "sku": "IPHONE15",
  "name": "iPhone 15",
  "description": "最新款iPhone",
  "price": 6999.00,
  "initialStock": 100
}
```

#### 添加库存
```bash
POST /api/merchants/{merchantId}/products/{sku}/add-stock
Content-Type: application/json

{
  "quantity": 50
}
```

#### 查询收入
```bash
GET /api/merchants/{merchantId}/income
```

### 商品交易

#### 购买商品
```bash
POST /api/commerce/purchase
Content-Type: application/json

{
  "userId": 1,
  "sku": "IPHONE15",
  "quantity": 1
}
```

## 完整测试流程

以下是一个完整的测试流程示例：

```bash
# 1. 创建用户
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","email":"test@example.com","phone":"13800138000"}'

# 2. 创建商家
curl -X POST http://localhost:8080/api/merchants \
  -H "Content-Type: application/json" \
  -d '{"merchantName":"测试商家","businessLicense":"12345678","contactEmail":"merchant@example.com","contactPhone":"13900139000"}'

# 3. 创建商品
curl -X POST http://localhost:8080/api/merchants/1/products \
  -H "Content-Type: application/json" \
  -d '{"sku":"IPHONE15","name":"iPhone 15","description":"最新款iPhone","price":6999.00,"initialStock":100}'

# 4. 用户充值
curl -X POST http://localhost:8080/api/users/1/recharge \
  -H "Content-Type: application/json" \
  -d '{"amount":10000.00,"currency":"CNY"}'

# 5. 购买商品
curl -X POST http://localhost:8080/api/commerce/purchase \
  -H "Content-Type: application/json" \
  -d '{"userId":1,"sku":"IPHONE15","quantity":1}'

# 6. 查询用户余额（应该减少6999.00）
curl http://localhost:8080/api/users/1/balance

# 7. 查询商家收入（应该增加6999.00）
curl http://localhost:8080/api/merchants/1/income

# 8. 添加库存
curl -X POST http://localhost:8080/api/merchants/1/products/IPHONE15/add-stock \
  -H "Content-Type: application/json" \
  -d '{"quantity":50}'
```

## 系统设计

### DDD领域划分

1. **User Context（用户上下文）**
   - User聚合根：管理用户信息和预存账户
   - UserAccount值对象：封装账户余额操作
   - UserStatus枚举：用户状态管理

2. **Merchant Context（商家上下文）**
   - Merchant聚合根：管理商家信息和收入账户
   - MerchantAccount值对象：封装收入和余额管理
   - MerchantStatus枚举：商家状态管理

3. **Product Context（商品上下文）**
   - Product聚合根：管理商品信息、价格和库存
   - ProductInventory值对象：封装库存操作
   - ProductStatus枚举：商品状态管理

4. **Order Context（订单上下文）**
   - Order聚合根：管理订单流程和状态
   - OrderItem值对象：订单项明细
   - OrderStatus枚举：订单状态流转

5. **Settlement Context（结算上下文）**
   - Settlement聚合根：管理结算记录和状态
   - SettlementStatus枚举：结算状态管理

### 核心业务流程

#### 购买流程
1. 验证用户、商品、商家状态
2. 检查库存和余额充足性
3. 创建订单并确认
4. 扣减商品库存
5. 扣减用户账户余额
6. 增加商家收入
7. 完成订单并返回结果

#### 结算流程
1. 定时任务每天凌晨2点触发
2. 计算商家预期收入（基于订单记录）
3. 获取商家实际余额
4. 对比并记录差异
5. 生成结算报告

## 配置说明

应用配置文件 `application.yml` 包含以下重要配置：

- **数据库配置**: H2内存数据库
- **结算配置**: 定时任务cron表达式
- **货币配置**: 默认货币和精度设置
- **日志配置**: SQL日志和应用日志级别

## 生产环境考虑

当前系统为演示版本，生产环境部署需要考虑：

1. **数据持久化**: 使用MySQL/PostgreSQL等关系型数据库
2. **事务管理**: 确保数据一致性和并发安全
3. **缓存**: 使用Redis等缓存热点数据
4. **监控**: 集成监控和告警系统
5. **安全**: 添加认证授权机制
6. **性能**: 数据库索引优化和查询优化
7. **扩展性**: 考虑微服务拆分和分布式部署

## 扩展性

系统采用DDD设计，具有良好的扩展性：

- **新增支付方式**: 扩展支付领域
- **多货币支持**: 扩展Money值对象
- **促销活动**: 新增促销上下文
- **库存预警**: 扩展库存管理功能
- **数据报表**: 新增分析和报表功能

## 许可证

MIT License