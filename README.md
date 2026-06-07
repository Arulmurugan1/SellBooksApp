# Book Selling Application - Microservices Architecture

A comprehensive Spring Boot microservices application for a book selling platform using Saga Pattern for distributed transactions.

## Architecture Overview

```
Customer
    ↓
API Gateway (Port 8080)
    ↓
  Service Discovery (Eureka Server - Port 8761)
    ↓
┌─────────────────────────────────────────────────────────────┐
│  Product Service (8081)  │  Order Service (8082)             │
│  Inventory Service (8083) │  Payment Service (8084)           │
└─────────────────────────────────────────────────────────────┘
    ↓
MySQL Database (3306)  │  Kafka Message Broker (9092)
```

## Technology Stack

- **Framework**: Spring Boot 3.2.0
- **Java Version**: Java 17 LTS
- **Database**: MySQL 8.0
- **Message Broker**: Apache Kafka
- **Service Discovery**: Spring Cloud Netflix Eureka
- **API Gateway**: Spring Cloud Gateway
- **Circuit Breaker**: Resilience4j
- **Database Migration**: Flyway
- **ORM**: Spring Data JPA, Hibernate
- **Monitoring**: Prometheus & Grafana
- **Containerization**: Docker & Docker Compose

## Services Description

### 1. Eureka Server (Service Discovery)
- **Port**: 8761
- **Purpose**: Service registry and discovery
- **URL**: http://localhost:8761

### 2. API Gateway
- **Port**: 8080
- **Purpose**: Entry point for all client requests
- **Features**:
  - Request routing to appropriate microservices
  - Circuit breaker with fallback responses
  - Load balancing
  - Request filtering

### 3. Product Service
- **Port**: 8081
- **Database**: product_db
- **Responsibilities**:
  - Manage product catalog
  - Product availability checking (Synchronous)
  - CRUD operations for products
  
**API Endpoints**:
```
GET    /api/products
POST   /api/products
GET    /api/products/{id}
GET    /api/products/code/{productCode}
GET    /api/products/check-availability/{productCode}
PUT    /api/products/{id}
DELETE /api/products/{id}
```

### 4. Order Service
- **Port**: 8082
- **Database**: order_db
- **Responsibilities**:
  - Create and manage orders
  - Orchestrate Saga Pattern workflow
  - Coordinate with Product Service (synchronous)
  - Coordinate with Inventory Service (synchronous)
  - Publish events for asynchronous processing
  
**API Endpoints**:
```
POST   /api/orders
GET    /api/orders/{orderId}
GET    /api/orders/customer/{customerId}
GET    /api/orders
```

### 5. Inventory Service
- **Port**: 8083
- **Database**: inventory_db
- **Responsibilities**:
  - Track stock levels
  - Reserve inventory (Synchronous)
  - Release reserved inventory (Compensation)
  - Check stock availability (Synchronous)
  
**API Endpoints**:
```
POST   /api/inventory/check-stock
POST   /api/inventory/reserve-stock
POST   /api/inventory/release-stock
GET    /api/inventory
GET    /api/inventory/{productId}
GET    /api/inventory/low-stock
PUT    /api/inventory/{productId}
```

### 6. Payment Service
- **Port**: 8084
- **Database**: payment_db
- **Responsibilities**:
  - Process payments asynchronously
  - Publish payment events
  - Handle payment failures and compensation
  
**API Endpoints**:
```
GET    /api/payments/transaction/{transactionId}
GET    /api/payments/order/{orderId}
```

## Saga Pattern Implementation

### Order Creation Flow

```
1. Customer submits order
   ↓
2. Order Service receives request
   ├─ Creates order with PENDING status
   ├─ Synchronously checks product availability (Product Service)
   ├─ Synchronously reserves inventory (Inventory Service)
   └─ Publishes "OrderCreatedEvent"
   ↓
3. Payment Service listens to "OrderCreatedEvent"
   ├─ Processes payment asynchronously (70% success rate)
   ├─ If SUCCESS: Publishes "PaymentProcessedEvent" (SUCCESS)
   └─ If FAILED: Publishes "PaymentProcessedEvent" (FAILED)
   ↓
4. Order Service listens to "PaymentProcessedEvent"
   ├─ If SUCCESS: Updates order status to CONFIRMED
   │                Sends confirmation email (async)
   │
   └─ If FAILED: Updates order status to PAYMENT_FAILED
                 Releases reserved inventory
                 Publishes "OrderCancelledEvent"
                 Sends failure email (async)
```

### Compensation Transactions

If payment fails:
1. Inventory reserved during order creation is released
2. Order status is updated to CANCELLED
3. Customer is notified asynchronously
4. System maintains eventual consistency

## Resilience Patterns

### Circuit Breaker Configuration
- **Sliding Window Size**: 10 calls
- **Failure Rate Threshold**: 50%
- **Slow Call Duration**: 2000ms
- **Wait Duration in Open State**: 60 seconds

### Retry Configuration
- **Max Attempts**: 3
- **Wait Duration**: 1000ms between retries

### Timeout & Fallback
- **Timeout**: 2000ms for service calls
- **Fallback**: Fallback responses on circuit breaker opening

## Database Schema

### Products
```sql
- product_code (UNIQUE)
- title, description, author, isbn
- price, category
- available (Boolean)
- created_at, updated_at
```

### Orders
```sql
- order_id (UNIQUE)
- customer_id, customer_email
- items (OneToMany with OrderItems)
- total_amount
- status (PENDING, INVENTORY_RESERVED, PAYMENT_PROCESSING, CONFIRMED, PAYMENT_FAILED, CANCELLED, COMPLETED)
- created_at, updated_at
```

### Inventory
```sql
- product_id (UNIQUE)
- product_name
- quantity_available, quantity_reserved
- reorder_level
- last_restock_date
- created_at, updated_at
```

### Payments
```sql
- transaction_id (UNIQUE)
- order_id (UNIQUE)
- customer_id
- amount
- payment_method (CARD, PAYPAL, WALLET, BANK_TRANSFER)
- status (PENDING, PROCESSING, SUCCESS, FAILED)
- failure_reason
- created_at, updated_at
```

## Setup & Installation

### Prerequisites
- Docker and Docker Compose
- Java 17 JDK
- Maven 3.9+
- Git

### Using Docker Compose (Recommended)

1. **Clone the repository**
```bash
cd d:\Tech Stack\Projects\BookSellingApp
```

2. **Build and start all services**
```bash
docker-compose up -d
```

3. **Verify services are running**
```bash
docker-compose ps
```

4. **View logs**
```bash
docker-compose logs -f [service-name]
```

5. **Stop services**
```bash
docker-compose down
```

### Local Setup (without Docker)

1. **Start MySQL**
```bash
# Install MySQL 8.0 and start the service
```

2. **Start Kafka**
```bash
# Download Kafka
tar -xzf kafka_2.13-3.5.0.tgz
cd kafka_2.13-3.5.0

# Start Zookeeper
bin/zookeeper-server-start.sh config/zookeeper.properties

# Start Kafka
bin/kafka-server-start.sh config/server.properties
```

3. **Build all services**
```bash
mvn clean package -DskipTests
```

4. **Start each service in separate terminals**
```bash
# Terminal 1: Eureka Server
cd eureka-server
mvn spring-boot:run

# Terminal 2: Product Service
cd product-service
mvn spring-boot:run

# Terminal 3: Inventory Service
cd inventory-service
mvn spring-boot:run

# Terminal 4: Order Service
cd order-service
mvn spring-boot:run

# Terminal 5: Payment Service
cd payment-service
mvn spring-boot:run

# Terminal 6: API Gateway
cd api-gateway
mvn spring-boot:run
```

## Service URLs

| Service | URL | Health Check |
|---------|-----|--------------|
| Eureka Server | http://localhost:8761 | http://localhost:8761 |
| API Gateway | http://localhost:8080 | http://localhost:8080/actuator/health |
| Product Service | http://localhost:8081 | http://localhost:8081/actuator/health |
| Order Service | http://localhost:8082 | http://localhost:8082/actuator/health |
| Inventory Service | http://localhost:8083 | http://localhost:8083/actuator/health |
| Payment Service | http://localhost:8084 | http://localhost:8084/actuator/health |
| Prometheus | http://localhost:9090 | http://localhost:9090 |
| Grafana | http://localhost:3000 | http://localhost:3000 |

## Monitoring & Observability

### Prometheus Metrics
Access metrics at: http://localhost:9090

Available metrics:
- JVM metrics (memory, threads, GC)
- Spring Boot metrics (requests, response times)
- Circuit breaker metrics
- Database metrics

### Grafana Dashboards
Access Grafana at: http://localhost:3000
- **Default Credentials**: admin / admin
- **Datasource**: Prometheus (http://prometheus:9090)

### Application Logs
- **Log Level**: DEBUG for com.booksellingapp, INFO for others
- **Log Format**: Timestamp [Thread] Level Logger - Message

## Sample Requests

### Create a Product
```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "productCode": "PROD011",
    "title": "The Lord of the Rings",
    "description": "Epic fantasy trilogy",
    "author": "J.R.R. Tolkien",
    "isbn": "978-0544003415",
    "price": 29.99,
    "category": "Fantasy",
    "available": true
  }'
```

### Create an Order
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "CUST-100",
    "customerEmail": "customer@example.com",
    "items": [
      {
        "productId": "PROD001",
        "quantity": 2
      },
      {
        "productId": "PROD002",
        "quantity": 1
      }
    ]
  }'
```

### Get Order Status
```bash
curl http://localhost:8080/api/orders/{orderId}
```

### Check Inventory
```bash
curl http://localhost:8080/api/inventory/{productId}
```

### Get Payment Status
```bash
curl http://localhost:8080/api/payments/order/{orderId}
```

## Event Topics (Kafka)

| Topic | Direction | Purpose |
|-------|-----------|---------|
| order-created | Order Service → Payment Service | Notify about new orders |
| payment-processed | Payment Service → Order Service | Notify about payment results |
| order-cancelled | Order Service → Email Service | Notify order cancellation |

## Database Initialization

Flyway automatically:
1. Creates tables on service startup
2. Inserts 10 sample records per service
3. Runs migrations in version order (V1__xxx.sql format)

## Troubleshooting

### Service Not Registering with Eureka
- Check if Eureka Server is running on port 8761
- Verify `eureka.client.service-url.defaultZone` in application.yml

### Kafka Connection Issues
- Ensure Zookeeper is running (port 2181)
- Ensure Kafka is running (port 9092)
- Check Kafka logs: `docker-compose logs kafka`

### Database Connection Errors
- Verify MySQL is running (port 3306)
- Check database credentials (root:root)
- Ensure databases are created: product_db, order_db, inventory_db, payment_db

### Circuit Breaker Opening
- Check service logs for errors
- Verify inter-service communication
- Check network connectivity between services

## Performance Tuning

### Connection Pooling
- Database: HikariCP (default 10 connections)
- Kafka: Consumer group optimization

### Caching
- Consider Redis for frequently accessed products
- Cache Eureka service registry

### Load Testing
```bash
# Using Apache JMeter or similar tools
# Load test: 100 concurrent users, 1000 requests per service
```

## Security Considerations

For production deployment:
1. Implement OAuth2/JWT authentication
2. Use Spring Security for API protection
3. Add rate limiting and throttling
4. Encrypt sensitive data (SSL/TLS)
5. Use environment variables for sensitive config
6. Implement API versioning
7. Add API documentation (Swagger/OpenAPI)

## Future Enhancements

1. **Authentication & Authorization**: Implement OAuth2 and JWT tokens
2. **API Documentation**: Add Swagger/Springdoc-OpenAPI
3. **Caching**: Add Redis for product and inventory caching
4. **Email Service**: Create dedicated async email service
5. **Notification Service**: Push notifications and SMS alerts
6. **Analytics**: Track orders, revenue, and customer behavior
7. **Admin Dashboard**: Analytics and monitoring UI
8. **Search**: Full-text search with Elasticsearch
9. **Review & Rating**: Product reviews and ratings system
10. **Recommendation Engine**: ML-based product recommendations

## License

MIT License - Feel free to use this project for educational and commercial purposes.

## Support

For issues or questions:
1. Check the troubleshooting section
2. Review service logs: `docker-compose logs [service-name]`
3. Verify Eureka Server dashboard: http://localhost:8761
4. Check Prometheus metrics: http://localhost:9090

---

**Created**: June 2026  
**Version**: 1.0.0  
**Architecture Pattern**: Microservices with Saga Pattern
