# Book Selling Application - Microservices Architecture
**Version:** 1.0.0 | **Date:** June 2026 | **Status:** Production Ready

---

## Table of Contents
1. [Overview](#overview)
2. [Architecture](#architecture)
3. [Services](#services)
4. [Technology Stack](#technology-stack)
5. [Setup & Deployment](#setup--deployment)
6. [API Endpoints](#api-endpoints)
7. [Saga Pattern Flow](#saga-pattern-flow)
8. [Database Schema](#database-schema)
9. [Monitoring](#monitoring)
10. [Quick Commands](#quick-commands)

---

## Overview

A **production-ready Spring Boot microservices application** for a book selling platform using:
- **Microservices Architecture** - 6 independent services
- **Saga Pattern** - Distributed transaction management
- **Event-Driven Design** - Kafka for asynchronous communication
- **Service Discovery** - Eureka for dynamic service registration
- **Resilience Patterns** - Circuit breaker, retry, timeout mechanisms

### Key Characteristics
| Feature | Details |
|---------|---------|
| **Java Version** | 17 LTS |
| **Framework** | Spring Boot 3.2.0 |
| **Services** | 6 microservices |
| **Databases** | MySQL 8.0 (4 separate) |
| **Message Broker** | Apache Kafka |
| **Monitoring** | Prometheus & Grafana |
| **Deployment** | Docker Compose |

---

## Architecture

### System Diagram
```
┌─────────────────────────────────────────────────────────────────┐
│                          CLIENT LAYER                            │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         ▼
        ┌────────────────────────────────┐
        │      API GATEWAY (8080)        │
        │  • Request Routing             │
        │  • Load Balancing              │
        │  • Circuit Breaker             │
        └────────────────────────────────┘
                         │
        ┌────────────────┼────────────────┐
        │                │                │
        ▼                ▼                ▼
    ┌─────────┐    ┌─────────┐    ┌─────────┐
    │ EUREKA  │    │ EUREKA  │    │ EUREKA  │
    │ SERVER  │    │ CLIENT  │    │ CLIENT  │
    │(8761)   │    │ (8081)  │    │ (8083)  │
    └─────────┘    └─────────┘    └─────────┘
        │                │                │
        ▼                ▼                ▼
    ┌──────────────┬──────────────┬──────────────┐
    │ PRODUCT SVC  │ ORDER SVC    │ INVENTORY    │
    │   (8081)     │   (8082)     │   SVC(8083)  │
    └──────────────┴──────────────┴──────────────┘
        │                │                │
        └────────────────┼────────────────┘
                         │
        ┌────────────────┴────────────────┐
        │                                 │
        ▼                                 ▼
    ┌────────────┐             ┌──────────────────┐
    │ PAYMENT    │             │ MESSAGE BROKER   │
    │ SERVICE    │◄────────────┤ KAFKA (9092)     │
    │ (8084)     │             │ • order-created  │
    └────────────┘             │ • payment-result │
        │                      └──────────────────┘
        │
        └──────────┬─────────────────┐
                   │                 │
                   ▼                 ▼
            ┌──────────────┐  ┌──────────────┐
            │   MYSQL      │  │ PROMETHEUS   │
            │  (3306)      │  │ GRAFANA      │
            │ 4 databases  │  │ (9090/3000)  │
            └──────────────┘  └──────────────┘
```

### Service Discovery & Registration
```
All Services ─────► Eureka Server (8761)
                         │
                    Health Checks
                         │
            ┌────────────┼────────────┐
            ▼            ▼            ▼
        Service A    Service B    Service C
        (Running)    (Running)    (Down) ❌
```

---

## Services

### 1️⃣ Eureka Server
| Property | Value |
|----------|-------|
| **Port** | 8761 |
| **Purpose** | Service Discovery & Registry |
| **Type** | Infrastructure Service |
| **Database** | N/A |
| **URL** | http://localhost:8761 |

**Responsibilities:**
- Register all microservices
- Monitor service health
- Provide service lookup for clients
- Load balancer integration

---

### 2️⃣ API Gateway
| Property | Value |
|----------|-------|
| **Port** | 8080 |
| **Purpose** | Request Router & Gateway |
| **Type** | Infrastructure Service |
| **Database** | N/A |
| **URL** | http://localhost:8080 |

**Responsibilities:**
- Route requests to appropriate services
- Circuit breaker with fallback
- Load balancing
- Request/Response filtering
- Service discovery integration

**Routes:**
```
/api/products/**    → Product Service (8081)
/api/orders/**      → Order Service (8082)
/api/inventory/**   → Inventory Service (8083)
/api/payments/**    → Payment Service (8084)
```

---

### 3️⃣ Product Service
| Property | Value |
|----------|-------|
| **Port** | 8081 |
| **Database** | product_db (MySQL) |
| **Type** | Business Service |
| **Discovery** | Eureka ✓ |
| **Type** | Eureka ✓ |

**Responsibilities:**
- Manage product catalog (books)
- Check product availability
- CRUD operations
- Product information retrieval

**Database Tables:**
```sql
products (
  id BIGINT PRIMARY KEY,
  product_code VARCHAR(50) UNIQUE,
  title VARCHAR(255),
  author VARCHAR(255),
  isbn VARCHAR(20),
  price DECIMAL(10,2),
  category VARCHAR(100),
  available BOOLEAN,
  created_at, updated_at
)
```

**Sample Data:** 10 popular books

**API Endpoints:**
```
GET    /api/products                    → Get all products
POST   /api/products                    → Create product
GET    /api/products/{id}               → Get by ID
GET    /api/products/code/{productCode} → Get by code
GET    /api/products/check-availability/{code} → Check availability
PUT    /api/products/{id}               → Update product
DELETE /api/products/{id}               → Delete product
```

---

### 4️⃣ Order Service
| Property | Value |
|----------|-------|
| **Port** | 8082 |
| **Database** | order_db (MySQL) |
| **Type** | Business Service (Saga Orchestrator) |
| **Discovery** | Eureka ✓ |
| **Messaging** | Kafka Producer ✓ |

**Responsibilities:**
- Create and manage orders
- Orchestrate Saga Pattern workflow
- Coordinate with Product & Inventory services
- Publish order events
- Handle payment results

**Database Tables:**
```sql
orders (
  id BIGINT PRIMARY KEY,
  order_id VARCHAR(50) UNIQUE,
  customer_id VARCHAR(100),
  customer_email VARCHAR(255),
  total_amount DECIMAL(10,2),
  status ENUM(PENDING, INVENTORY_RESERVED, PAYMENT_PROCESSING, 
              CONFIRMED, PAYMENT_FAILED, CANCELLED),
  created_at, updated_at
)

order_items (
  id BIGINT PRIMARY KEY,
  order_id BIGINT (FK),
  product_id VARCHAR(100),
  product_name VARCHAR(255),
  quantity INT,
  price DECIMAL(10,2),
  total_price DECIMAL(10,2)
)
```

**Sample Data:** 10 sample orders with various statuses

**API Endpoints:**
```
POST   /api/orders                      → Create order
GET    /api/orders/{orderId}            → Get order by ID
GET    /api/orders/customer/{customerId} → Get customer orders
GET    /api/orders                      → Get all orders
```

**Events Published:**
- `order-created` - When order is created
- `order-cancelled` - When order is cancelled

---

### 5️⃣ Inventory Service
| Property | Value |
|----------|-------|
| **Port** | 8083 |
| **Database** | inventory_db (MySQL) |
| **Type** | Business Service |
| **Discovery** | Eureka ✓ |
| **Clients** | Order Service (OpenFeign) |

**Responsibilities:**
- Track stock levels
- Check inventory availability
- Reserve stock for orders
- Release stock (compensation)
- Monitor low stock items

**Database Tables:**
```sql
inventories (
  id BIGINT PRIMARY KEY,
  product_id VARCHAR(100) UNIQUE,
  product_name VARCHAR(255),
  quantity_available INT,
  quantity_reserved INT,
  reorder_level INT,
  last_restock_date TIMESTAMP,
  created_at, updated_at
)

inventory_reservations (
  id BIGINT PRIMARY KEY,
  order_id VARCHAR(50),
  product_id VARCHAR(100),
  quantity INT,
  status ENUM(RESERVED, RELEASED, CONFIRMED),
  created_at, updated_at
)
```

**Sample Data:** Stock levels for all 10 products

**API Endpoints:**
```
POST   /api/inventory/check-stock       → Check availability
POST   /api/inventory/reserve-stock     → Reserve stock
POST   /api/inventory/release-stock     → Release stock
GET    /api/inventory                   → Get all inventory
GET    /api/inventory/{productId}       → Get product inventory
GET    /api/inventory/low-stock         → Get low stock items
PUT    /api/inventory/{productId}       → Update quantity
```

---

### 6️⃣ Payment Service
| Property | Value |
|----------|-------|
| **Port** | 8084 |
| **Database** | payment_db (MySQL) |
| **Type** | Business Service |
| **Discovery** | Eureka ✓ |
| **Messaging** | Kafka Producer & Consumer |

**Responsibilities:**
- Process payments asynchronously
- Publish payment result events
- Handle payment failures
- Maintain payment records

**Database Tables:**
```sql
payments (
  id BIGINT PRIMARY KEY,
  transaction_id VARCHAR(100) UNIQUE,
  order_id VARCHAR(50) UNIQUE,
  customer_id VARCHAR(100),
  amount DECIMAL(10,2),
  payment_method ENUM(CARD, PAYPAL, WALLET, BANK_TRANSFER),
  status ENUM(PENDING, PROCESSING, SUCCESS, FAILED),
  failure_reason VARCHAR(255),
  created_at, updated_at
)
```

**Sample Data:** 10 sample payment records

**API Endpoints:**
```
GET    /api/payments/transaction/{transactionId} → Get by transaction ID
GET    /api/payments/order/{orderId}             → Get by order ID
```

**Events Consumed:**
- `order-created` - Listen for new orders

**Events Published:**
- `payment-processed` - Payment result (SUCCESS/FAILED)

---

## Technology Stack

### Core Framework
```
Spring Boot              3.2.0    Language runtime
Spring Cloud           2023.0.0   Cloud patterns
OpenJDK/Eclipse Temurin   17      Java JDK
Maven                    3.9      Build tool
```

### Microservices & Cloud
```
Spring Cloud Gateway              API Gateway & Routing
Spring Cloud Netflix Eureka       Service Discovery
Spring Cloud OpenFeign            REST Client
Spring Cloud Stream + Kafka       Event Messaging
```

### Data Access
```
Spring Data JPA              ORM & Repository pattern
Hibernate                    Object-Relational Mapping
MySQL Connector              Database driver
Flyway                       Database migrations
HikariCP                     Connection pooling
```

### Resilience & Monitoring
```
Resilience4j                 Circuit Breaker, Retry, Timeout
Micrometer Prometheus        Metrics collection
Micrometer Grafana           Visualization
Spring Boot Actuator         Health checks & monitoring
```

### Infrastructure
```
Apache Kafka (3.6.0)         Message broker
MySQL (8.0)                  Relational database
Zookeeper                    Kafka coordination
Docker                       Containerization
Docker Compose               Orchestration
```

### Utilities
```
Lombok                       Code generation
Jackson                      JSON processing
SLF4J + Logback              Logging
JUnit 5                      Testing
```

---

## Setup & Deployment

### Prerequisites
```
✓ Docker & Docker Compose
✓ Java 17 JDK
✓ Maven 3.9+
✓ 4GB RAM minimum
✓ Ports: 8080, 8081-8084, 8761, 9092, 3306, 9090, 3000 available
```

### Option 1: Docker Compose (Recommended)

**Step 1: Navigate to project**
```bash
cd d:\Tech Stack\Projects\BookSellingApp
```

**Step 2: Start all services**
```bash
docker-compose up -d
```

**Step 3: Verify services are running**
```bash
docker-compose ps
```

**Step 4: Check service logs**
```bash
docker-compose logs -f [service-name]
```

**Step 5: Stop services**
```bash
docker-compose down
```

### Option 2: Local Development

**Prerequisites:**
- MySQL 8.0 running
- Kafka & Zookeeper running
- Java 17 & Maven installed

**Steps:**
```bash
# 1. Build all services
mvn clean package -DskipTests

# 2. Start each service in separate terminal
cd eureka-server && mvn spring-boot:run
cd product-service && mvn spring-boot:run
cd order-service && mvn spring-boot:run
cd inventory-service && mvn spring-boot:run
cd payment-service && mvn spring-boot:run
cd api-gateway && mvn spring-boot:run
```

### Option 3: Building Custom Docker Images

```bash
# Build parent project
mvn clean package

# Build individual service images
docker build -t product-service ./product-service
docker build -t order-service ./order-service
docker build -t inventory-service ./inventory-service
docker build -t payment-service ./payment-service
docker build -t api-gateway ./api-gateway
docker build -t eureka-server ./eureka-server
```

---

## API Endpoints

### Product Service (Port 8081)
```http
GET /api/products
POST /api/products
GET /api/products/{id}
GET /api/products/code/{productCode}
GET /api/products/check-availability/{productCode}
PUT /api/products/{id}
DELETE /api/products/{id}
```

### Order Service (Port 8082)
```http
POST /api/orders
GET /api/orders/{orderId}
GET /api/orders/customer/{customerId}
GET /api/orders
```

### Inventory Service (Port 8083)
```http
POST /api/inventory/check-stock
POST /api/inventory/reserve-stock
POST /api/inventory/release-stock
GET /api/inventory
GET /api/inventory/{productId}
GET /api/inventory/low-stock
PUT /api/inventory/{productId}
```

### Payment Service (Port 8084)
```http
GET /api/payments/transaction/{transactionId}
GET /api/payments/order/{orderId}
```

### Health & Monitoring
```http
GET /actuator/health          (All services)
GET /actuator/metrics         (All services)
GET /actuator/prometheus      (All services)
```

---

## Saga Pattern Flow

### Success Scenario (70% Probability)

```
Step 1: CREATE ORDER
├─ Input: Customer ID, Email, Items
├─ Service: Order Service
├─ Action: Create order with status = PENDING
└─ Output: Order ID, Status = PENDING

Step 2: CHECK PRODUCT AVAILABILITY (Synchronous)
├─ Input: Product IDs
├─ Service: Order Service → Product Service
├─ Action: Verify all products available
├─ Timeout: 2000ms
├─ Retry: 3 attempts with 1s wait
└─ Output: Available = true/false

Step 3: RESERVE INVENTORY (Synchronous)
├─ Input: Order ID, Product IDs, Quantities
├─ Service: Order Service → Inventory Service
├─ Action: Reserve stock, move available → reserved
├─ Database: Create inventory_reservations record
└─ Output: Order Status = INVENTORY_RESERVED

Step 4: PUBLISH ORDER CREATED EVENT
├─ Input: Order details
├─ Destination: Kafka topic "order-created"
├─ Format: JSON (OrderCreatedEvent)
└─ Subscribers: Payment Service

Step 5: PROCESS PAYMENT (Asynchronous)
├─ Input: OrderCreatedEvent
├─ Service: Payment Service
├─ Action: Create payment record, process payment
├─ Success Probability: 70%
├─ Database: Insert payment with status = SUCCESS
└─ Action: Publish PaymentProcessedEvent

Step 6: UPDATE ORDER TO CONFIRMED
├─ Input: PaymentProcessedEvent (SUCCESS)
├─ Service: Order Service
├─ Action: Update order status = CONFIRMED
├─ Database: Update orders table
├─ Email: Send confirmation (async)
└─ Result: ✅ Order Complete

Order Status Timeline:
PENDING → INVENTORY_RESERVED → CONFIRMED ✅
```

### Failure Scenario (30% Probability)

```
Step 1-4: SAME AS SUCCESS

Step 5: PROCESS PAYMENT FAILS
├─ Payment Service: Payment fails (30%)
├─ Database: Insert payment with status = FAILED
└─ Action: Publish PaymentProcessedEvent (FAILED)

Step 6: COMPENSATION TRANSACTION
├─ Input: PaymentProcessedEvent (FAILED)
├─ Service: Order Service
├─ Action 1: Update order status = PAYMENT_FAILED
├─ Action 2: Release reserved inventory
│   └─ Call: Inventory Service.releaseStock()
│   └─ Database: Move reserved → available
│   └─ Database: Update inventory_reservations.status = RELEASED
├─ Action 3: Publish OrderCancelledEvent
└─ Action 4: Send failure email (async)

Step 7: SYSTEM RECOVERY
├─ Order Status: PAYMENT_FAILED ❌
├─ Inventory: Restored to original levels
├─ Customer: Notified of failure
└─ Eventual Consistency: Achieved

Order Status Timeline:
PENDING → INVENTORY_RESERVED → PAYMENT_FAILED ❌
                                    ↓
                         (Compensation triggered)
                                    ↓
                           Inventory Released ✅
```

### Distributed Transaction Coordination

```
Timeline View:

Order Service              Inventory Service         Payment Service
     │                          │                         │
     │─ Create Order            │                         │
     │  (PENDING)               │                         │
     │                          │                         │
     │─ Check Products          │                         │
     │                          │                         │
     │─ Reserve Stock ─────────→│                         │
     │  (Sync)                  │─ Update DB              │
     │                          │                         │
     │  Status Update ◄─────────│                         │
     │  (INVENTORY_RESERVED)    │                         │
     │                          │                         │
     │─ Publish Event ─────────────────────────────────→│
     │  (order-created)         │                         │
     │                          │                         │
     │                          │                    Process Payment
     │                          │                    70% Success
     │                          │                         │
     │◄─────── PaymentProcessedEvent ─────────────────────│
     │         SUCCESS/FAILED   │                         │
     │                          │                         │
     ├─ If SUCCESS:             │                         │
     │  └─ Status = CONFIRMED   │                         │
     │                          │                         │
     └─ If FAILED:              │                         │
        └─ Release Stock ───────→│                         │
        └─ Status = PAYMENT_FAILED                        │
           └─ compensation triggered
```

---

## Database Schema

### Product Database (product_db)
```
Table: products
┌──────────────────────────────────────┐
│ Column           │ Type              │
├──────────────────────────────────────┤
│ id               │ BIGINT (PK)       │
│ product_code     │ VARCHAR(50) (UQ)  │
│ title            │ VARCHAR(255)      │
│ description      │ TEXT              │
│ author           │ VARCHAR(255)      │
│ isbn             │ VARCHAR(20)       │
│ price            │ DECIMAL(10,2)     │
│ category         │ VARCHAR(100)      │
│ available        │ BOOLEAN           │
│ created_at       │ TIMESTAMP         │
│ updated_at       │ TIMESTAMP         │
└──────────────────────────────────────┘

Sample Data: 10 books
```

### Order Database (order_db)
```
Table: orders
┌──────────────────────────────────────┐
│ Column           │ Type              │
├──────────────────────────────────────┤
│ id               │ BIGINT (PK)       │
│ order_id         │ VARCHAR(50) (UQ)  │
│ customer_id      │ VARCHAR(100)      │
│ customer_email   │ VARCHAR(255)      │
│ total_amount     │ DECIMAL(10,2)     │
│ status           │ ENUM              │
│ created_at       │ TIMESTAMP         │
│ updated_at       │ TIMESTAMP         │
└──────────────────────────────────────┘

Table: order_items
┌──────────────────────────────────────┐
│ Column           │ Type              │
├──────────────────────────────────────┤
│ id               │ BIGINT (PK)       │
│ order_id         │ BIGINT (FK)       │
│ product_id       │ VARCHAR(100)      │
│ product_name     │ VARCHAR(255)      │
│ quantity         │ INT               │
│ price            │ DECIMAL(10,2)     │
│ total_price      │ DECIMAL(10,2)     │
└──────────────────────────────────────┘

Sample Data: 10 orders
```

### Inventory Database (inventory_db)
```
Table: inventories
┌──────────────────────────────────────┐
│ Column           │ Type              │
├──────────────────────────────────────┤
│ id               │ BIGINT (PK)       │
│ product_id       │ VARCHAR(100) (UQ) │
│ product_name     │ VARCHAR(255)      │
│ quantity_available  │ INT            │
│ quantity_reserved   │ INT            │
│ reorder_level    │ INT               │
│ last_restock_date   │ TIMESTAMP      │
│ created_at       │ TIMESTAMP         │
│ updated_at       │ TIMESTAMP         │
└──────────────────────────────────────┘

Table: inventory_reservations
┌──────────────────────────────────────┐
│ Column           │ Type              │
├──────────────────────────────────────┤
│ id               │ BIGINT (PK)       │
│ order_id         │ VARCHAR(50)       │
│ product_id       │ VARCHAR(100)      │
│ quantity         │ INT               │
│ status           │ ENUM              │
│ created_at       │ TIMESTAMP         │
│ updated_at       │ TIMESTAMP         │
└──────────────────────────────────────┘

Sample Data: Stock for 10 products
```

### Payment Database (payment_db)
```
Table: payments
┌──────────────────────────────────────┐
│ Column           │ Type              │
├──────────────────────────────────────┤
│ id               │ BIGINT (PK)       │
│ transaction_id   │ VARCHAR(100) (UQ) │
│ order_id         │ VARCHAR(50) (UQ)  │
│ customer_id      │ VARCHAR(100)      │
│ amount           │ DECIMAL(10,2)     │
│ payment_method   │ ENUM              │
│ status           │ ENUM              │
│ failure_reason   │ VARCHAR(255)      │
│ created_at       │ TIMESTAMP         │
│ updated_at       │ TIMESTAMP         │
└──────────────────────────────────────┘

Sample Data: 10 payment records
```

---

## Monitoring

### Service Health Checks
```
Service              | Port | Health Endpoint
─────────────────────┼──────┼──────────────────────────────
Eureka Server        | 8761 | http://localhost:8761/
API Gateway          | 8080 | http://localhost:8080/actuator/health
Product Service      | 8081 | http://localhost:8081/actuator/health
Order Service        | 8082 | http://localhost:8082/actuator/health
Inventory Service    | 8083 | http://localhost:8083/actuator/health
Payment Service      | 8084 | http://localhost:8084/actuator/health
```

### Metrics & Observability
```
Prometheus (Metrics Collection)
├─ URL: http://localhost:9090
├─ Scrape Interval: 15 seconds
├─ Data Retention: 15 days (default)
├─ Metrics:
│  ├─ jvm_memory_used_bytes
│  ├─ jvm_threads_live
│  ├─ http_server_requests_seconds
│  ├─ http_server_requests_seconds_count
│  ├─ resilience4j_circuitbreaker_state
│  └─ kafka_consumer_records_consumed_total
└─ Endpoint: /actuator/prometheus (all services)

Grafana (Visualization)
├─ URL: http://localhost:3000
├─ Default Login: admin / admin
├─ Datasource: Prometheus (http://prometheus:9090)
├─ Dashboards:
│  ├─ JVM Metrics
│  ├─ HTTP Requests
│  ├─ Circuit Breaker Status
│  └─ Kafka Consumer Lag
└─ Refresh Rate: 30 seconds
```

### Key Metrics to Monitor
```
1. Order Success Rate
   Formula: (Confirmed Orders / Total Orders) × 100%
   Target: > 90%

2. Payment Success Rate
   Formula: (Successful Payments / Total Payments) × 100%
   Expected: ~70% (by design)

3. Average Order Processing Time
   Formula: Total Time / Number of Orders
   Target: < 5 seconds

4. Saga Compensation Rate
   Formula: (Compensation Transactions / Total Orders) × 100%
   Expected: ~30% (failures)

5. Circuit Breaker Health
   States: CLOSED (normal) | OPEN (failing) | HALF_OPEN (testing)
   Target: CLOSED

6. Database Connection Pool
   Active: Number of active connections
   Available: Available connections
   Max: Maximum pool size (default 10)
```

### Logging Configuration
```yaml
Logging Levels:
├─ Root: INFO
├─ com.booksellingapp: DEBUG
├─ Spring Framework: INFO
└─ Hibernate: INFO

Log Format:
%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n

Example Log Output:
10:45:23.456 [pool-1-thread-5] INFO  com.booksellingapp.order.service.OrderService - Order created successfully with id: 123
```

---

## Quick Commands

### Docker Compose Commands
```bash
# Start all services
docker-compose up -d

# View service status
docker-compose ps

# View logs (all services)
docker-compose logs -f

# View logs (specific service)
docker-compose logs -f order-service

# Restart a service
docker-compose restart payment-service

# Stop all services
docker-compose stop

# Stop and remove containers
docker-compose down

# Stop, remove, and delete volumes
docker-compose down -v

# Rebuild images
docker-compose build --no-cache

# Update and restart
docker-compose up -d --build
```

### curl API Testing Commands
```bash
# Get all products
curl http://localhost:8080/api/products

# Create a product
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{"productCode":"BOOK-100","title":"Sample Book",...}'

# Create an order
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{"customerId":"CUST-001","customerEmail":"test@example.com","items":[...]}'

# Get order status
curl http://localhost:8080/api/orders/{orderId}

# Check inventory
curl http://localhost:8080/api/inventory

# Get health status
curl http://localhost:8080/actuator/health

# View Eureka dashboard
curl http://localhost:8761
```

### Service-Specific Ports Reference
```
8080  → API Gateway
8081  → Product Service
8082  → Order Service
8083  → Inventory Service
8084  → Payment Service
8761  → Eureka Server
9092  → Kafka Broker
3306  → MySQL
9090  → Prometheus
3000  → Grafana
2181  → Zookeeper
```

---

## Configuration Summary

### Resilience Configuration
```yaml
Circuit Breaker:
  Sliding Window Size: 10 calls
  Failure Rate Threshold: 50%
  Slow Call Duration Threshold: 2000ms
  Wait Duration in Open State: 60 seconds
  Permitted Calls in Half-Open: 3

Retry:
  Max Attempts: 3
  Wait Duration: 1000ms between retries

Timeout:
  Default: 2000ms per service call
```

### Kafka Configuration
```yaml
Kafka Brokers: localhost:9092
Zookeeper: localhost:2181

Topics:
  order-created      → Order Service → Payment Service
  payment-processed  → Payment Service → Order Service
  order-cancelled    → Order Service → Email Service (future)

Consumer Groups:
  order-service-group     (listens to payment-processed)
  payment-service-group   (listens to order-created)

Message Format: JSON
Content Type: application/json
```

---

## Features Summary

### ✅ Implemented Features
- [x] 6 Microservices with independent databases
- [x] Service discovery with Eureka
- [x] API Gateway with routing & load balancing
- [x] Saga Pattern (choreography-based)
- [x] Synchronous inter-service communication (Product & Inventory)
- [x] Asynchronous event processing (Kafka)
- [x] Circuit breaker with retry & timeout
- [x] Database migrations with Flyway
- [x] 10 sample records per service
- [x] Prometheus & Grafana monitoring
- [x] Docker & Docker Compose
- [x] Health checks & actuator endpoints
- [x] Structured logging (SLF4J/Logback)
- [x] Resilience patterns
- [x] Compensation transactions

### 🔄 Event-Driven Flow
- [x] Order creation events
- [x] Payment result events
- [x] Order cancellation events
- [x] Event-based service communication
- [x] Asynchronous message processing

### 📊 Monitoring & Observability
- [x] Prometheus metrics collection
- [x] Grafana dashboards
- [x] Service health indicators
- [x] Circuit breaker monitoring
- [x] Request/response metrics
- [x] JVM metrics
- [x] Kafka consumer metrics

---

## File Structure
```
BookSellingApp/
├── eureka-server/
├── api-gateway/
├── product-service/
├── order-service/
├── inventory-service/
├── payment-service/
├── common-library/
├── docker/
│   └── prometheus.yml
├── docker-compose.yml
├── pom.xml
├── README.md
├── QUICK_START.md
├── SAGA_PATTERN_GUIDE.md
├── PROJECT_SUMMARY.md
└── .gitignore
```

---

## Next Steps & Enhancements

### Immediate (v1.1)
- [ ] Add Swagger/OpenAPI documentation
- [ ] Implement JWT authentication
- [ ] Add request validation
- [ ] Create admin dashboard

### Short Term (v1.2)
- [ ] Email service implementation
- [ ] Redis caching layer
- [ ] Full-text search with Elasticsearch
- [ ] Product recommendations

### Long Term (v2.0)
- [ ] Multi-tenant support
- [ ] Advanced analytics
- [ ] Mobile application
- [ ] AI-powered recommendations
- [ ] Machine learning for pricing

---

## Contact & Support

**Documentation Files:**
- `README.md` - Complete architecture & setup
- `QUICK_START.md` - 30-second quick start
- `SAGA_PATTERN_GUIDE.md` - Detailed pattern explanation
- `PROJECT_SUMMARY.md` - Project overview

**Key URLs:**
- Eureka: http://localhost:8761
- Prometheus: http://localhost:9090
- Grafana: http://localhost:3000

**Troubleshooting:**
1. Check service logs: `docker-compose logs [service]`
2. Verify Eureka registration: http://localhost:8761
3. Check health: `curl http://localhost:8080/actuator/health`
4. Review documentation files

---

**Created:** June 2026  
**Version:** 1.0.0  
**Status:** Production Ready ✅

