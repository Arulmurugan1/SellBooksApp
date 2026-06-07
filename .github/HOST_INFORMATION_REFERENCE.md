API Gateway:       localhost:8080
Product Service:   localhost:8081
Order Service:     localhost:8082
Inventory Service: localhost:8083
Payment Service:   localhost:8084
MySQL:            localhost:3306 (root/root)
Kafka:            localhost:9092
Eureka:           localhost:8761
Prometheus:       localhost:9090
Grafana:          localhost:3000 (admin/admin)

# Host Information Reference
## Book Selling Application - Docker Environment

**Generated:** June 7, 2026  
**Environment:** Docker Compose with 11 Services  
**Network:** book-selling-network (Bridge)

---

## 🚀 Quick Access URLs

| Service | URL | Purpose |
|---------|-----|---------|
| **API Gateway** | http://localhost:8080 | Main entry point for all requests |
| **Eureka Dashboard** | http://localhost:8761 | Service registry & health status |
| **Prometheus** | http://localhost:9090 | Metrics collection & queries |
| **Grafana** | http://localhost:3000 | Dashboard visualization (admin/admin) |

---

## 📱 Microservices Details

### 1. API Gateway
| Property | Value |
|----------|-------|
| **Host** | localhost |
| **Port** | 8080 |
| **Container Name** | api-gateway-app |
| **Protocol** | HTTP |
| **Base URL** | http://localhost:8080 |
| **Health Check** | http://localhost:8080/actuator/health |
| **Description** | Routes requests to appropriate microservices |

#### API Gateway Routes
```
/api/products/**    → product-service:8081
/api/orders/**      → order-service:8082
/api/inventory/**   → inventory-service:8083
/api/payments/**    → payment-service:8084
```

---

### 2. Product Service
| Property | Value |
|----------|-------|
| **Host** | localhost |
| **Port** | 8081 |
| **Container Name** | product-service-app |
| **Protocol** | HTTP |
| **Base URL** | http://localhost:8081 |
| **Health Check** | http://localhost:8081/actuator/health |
| **Database** | product_db |
| **Description** | Product catalog management & availability checks |

#### Key Endpoints
```
GET    /api/products                          - Get all products
POST   /api/products                          - Create new product
GET    /api/products/{id}                     - Get product by ID
GET    /api/products/code/{productCode}       - Get product by code
GET    /api/products/check-availability/{code} - Check availability
PUT    /api/products/{id}                     - Update product
DELETE /api/products/{id}                     - Delete product
```

---

### 3. Order Service
| Property | Value |
|----------|-------|
| **Host** | localhost |
| **Port** | 8082 |
| **Container Name** | order-service-app |
| **Protocol** | HTTP |
| **Base URL** | http://localhost:8082 |
| **Health Check** | http://localhost:8082/actuator/health |
| **Database** | order_db |
| **Description** | Saga orchestration & order management |

#### Key Endpoints
```
POST   /api/orders                     - Create new order
GET    /api/orders/{orderId}           - Get order by ID
GET    /api/orders/customer/{customerId} - Get orders by customer
GET    /api/orders                     - Get all orders
```

---

### 4. Inventory Service
| Property | Value |
|----------|-------|
| **Host** | localhost |
| **Port** | 8083 |
| **Container Name** | inventory-service-app |
| **Protocol** | HTTP |
| **Base URL** | http://localhost:8083 |
| **Health Check** | http://localhost:8083/actuator/health |
| **Database** | inventory_db |
| **Description** | Stock tracking & inventory reservations |

#### Key Endpoints
```
POST   /api/inventory/check-stock      - Check stock availability
POST   /api/inventory/reserve          - Reserve inventory
POST   /api/inventory/release          - Release reserved inventory
GET    /api/inventory/{productId}      - Get inventory by product
GET    /api/inventory                  - Get all inventory
GET    /api/inventory/low-stock        - Get low stock items
PUT    /api/inventory/{productId}      - Update inventory
```

---

### 5. Payment Service
| Property | Value |
|----------|-------|
| **Host** | localhost |
| **Port** | 8084 |
| **Container Name** | payment-service-app |
| **Protocol** | HTTP |
| **Base URL** | http://localhost:8084 |
| **Health Check** | http://localhost:8084/actuator/health |
| **Database** | payment_db |
| **Description** | Asynchronous payment processing |

#### Key Endpoints
```
GET /api/payments/transaction/{transactionId}  - Get payment by transaction
GET /api/payments/order/{orderId}               - Get payment by order
```

---

## 🔌 Infrastructure Services

### MySQL Database
| Property | Value |
|----------|-------|
| **Host** | localhost |
| **Port** | 3306 |
| **Container Name** | mysql-booksellingapp |
| **Username** | root |
| **Password** | root |
| **Protocol** | MySQL |
| **Health Check** | `mysqladmin ping -h localhost` |

#### Connection Strings
```java
// Product Service
jdbc:mysql://mysql:3306/product_db?createDatabaseIfNotExist=true&allowPublicKeyRetrieval=true&useSSL=false

// Order Service
jdbc:mysql://mysql:3306/order_db?createDatabaseIfNotExist=true&allowPublicKeyRetrieval=true&useSSL=false

// Inventory Service
jdbc:mysql://mysql:3306/inventory_db?createDatabaseIfNotExist=true&allowPublicKeyRetrieval=true&useSSL=false

// Payment Service
jdbc:mysql://mysql:3306/payment_db?createDatabaseIfNotExist=true&allowPublicKeyRetrieval=true&useSSL=false
```

#### Databases
| Database Name | Purpose | Services |
|---------------|---------|----------|
| product_db | Product catalog | product-service |
| order_db | Order transactions | order-service |
| inventory_db | Inventory tracking | inventory-service |
| payment_db | Payment records | payment-service |

---

### Apache Kafka Message Broker
| Property | Value |
|----------|-------|
| **Host** | localhost |
| **Port** | 9092 |
| **Container Name** | kafka-booksellingapp |
| **Protocol** | PLAINTEXT |
| **Broker Connection** | kafka:9092 |
| **Health Check** | `kafka-broker-api-versions --bootstrap-server localhost:9092` |

#### Kafka Topics
| Topic Name | Publisher | Subscribers | Purpose |
|------------|-----------|-------------|---------|
| order-created | Order Service | Payment Service | Triggered when order is created |
| payment-processed | Payment Service | Order Service | Sends payment success/failure result |
| order-cancelled | Order Service | Consumers | Triggered when order is cancelled |

---

### Apache Zookeeper
| Property | Value |
|----------|-------|
| **Host** | localhost |
| **Port** | 2181 |
| **Container Name** | zookeeper-booksellingapp |
| **Protocol** | CLIENT_PORT |
| **Tick Time** | 2000ms |
| **Purpose** | Kafka cluster coordination |

---

### Eureka Service Discovery
| Property | Value |
|----------|-------|
| **Host** | localhost |
| **Port** | 8761 |
| **Container Name** | eureka-server-app |
| **URL** | http://localhost:8761 |
| **Dashboard** | http://localhost:8761 |
| **Protocol** | HTTP |
| **Purpose** | Service registry for all microservices |

#### Registered Services
```
Service Name              Status  Instances
-------------------------------------------
API-GATEWAY              UP      1
PRODUCT-SERVICE          UP      1
ORDER-SERVICE            UP      1
INVENTORY-SERVICE        UP      1
PAYMENT-SERVICE          UP      1
EUREKA-SERVER            UP      1
```

---

## 📊 Monitoring Stack

### Prometheus
| Property | Value |
|----------|-------|
| **Host** | localhost |
| **Port** | 9090 |
| **Container Name** | prometheus-booksellingapp |
| **URL** | http://localhost:9090 |
| **Config File** | ./docker/prometheus.yml |
| **Data Directory** | /prometheus |
| **Scrape Interval** | 15 seconds |

#### Monitored Endpoints
```
Endpoint: http://service:port/actuator/prometheus

Services:
- API Gateway:     http://localhost:8080/actuator/prometheus
- Product Service: http://localhost:8081/actuator/prometheus
- Order Service:   http://localhost:8082/actuator/prometheus
- Inventory Service: http://localhost:8083/actuator/prometheus
- Payment Service: http://localhost:8084/actuator/prometheus
```

#### Metrics Available
- JVM Memory (Heap, Non-Heap)
- HTTP Request Duration
- HTTP Request Count
- Circuit Breaker Status
- Thread Count
- Database Connection Pool

---

### Grafana
| Property | Value |
|----------|-------|
| **Host** | localhost |
| **Port** | 3000 |
| **Container Name** | grafana-booksellingapp |
| **URL** | http://localhost:3000 |
| **Admin Username** | admin |
| **Admin Password** | admin |
| **Data Source** | Prometheus (localhost:9090) |

#### Default Dashboards
- System Metrics
- JVM Metrics
- HTTP Metrics
- Service Health

---

## 🔐 Credentials & Authentication

| Service | Username | Password | Notes |
|---------|----------|----------|-------|
| MySQL | root | root | Database admin |
| Grafana | admin | admin | Web UI only |
| Kafka | N/A | N/A | No authentication (PLAINTEXT) |
| Eureka | N/A | N/A | No authentication |
| Prometheus | N/A | N/A | No authentication |

---

## 🐳 Docker Commands

### Start All Services
```bash
docker-compose up -d
```

### Stop All Services
```bash
docker-compose down
```

### View Logs
```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f product-service
docker-compose logs -f order-service
docker-compose logs -f payment-service
```

### Check Service Status
```bash
docker-compose ps
```

### Rebuild Services
```bash
docker-compose up -d --build
```

### Remove All Containers & Volumes
```bash
docker-compose down -v
```

---

## 🧪 Testing with cURL

### Product Service
```bash
# Get all products
curl http://localhost:8080/api/products

# Create product
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "productCode": "PROD001",
    "title": "Sample Book",
    "price": 29.99,
    "available": true
  }'

# Check availability
curl http://localhost:8080/api/products/check-availability/PROD001
```

### Order Service
```bash
# Create order
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "CUST001",
    "customerEmail": "customer@example.com",
    "items": [
      {
        "productCode": "PROD001",
        "quantity": 2
      }
    ]
  }'

# Get order
curl http://localhost:8080/api/orders/{orderId}

# Get customer orders
curl http://localhost:8080/api/orders/customer/CUST001
```

---

## 📋 Network Configuration

| Property | Value |
|----------|-------|
| **Network Name** | book-selling-network |
| **Driver** | bridge |
| **Type** | Internal Docker network |
| **DNS Resolution** | Service name as hostname (e.g., mysql, kafka) |

### Internal Service URLs (Container-to-Container)
```
API Gateway:      http://api-gateway:8080
Product Service:  http://product-service:8081
Order Service:    http://order-service:8082
Inventory Service: http://inventory-service:8083
Payment Service:  http://payment-service:8084
MySQL:            mysql:3306
Kafka:            kafka:9092
Zookeeper:        zookeeper:2181
Eureka:           http://eureka-server:8761
Prometheus:       http://prometheus:9090
Grafana:          http://grafana:3000
```

---

## 🔄 Service Dependencies

```
startup order:
1. MySQL ← database requirement
2. Zookeeper ← Kafka dependency
3. Kafka ← Eureka doesn't depend, but services do
4. Eureka Server ← Service discovery
5. API Gateway ← depends on Eureka
6. Product Service ← depends on Eureka + MySQL
7. Order Service ← depends on Eureka + MySQL + Kafka
8. Inventory Service ← depends on Eureka + MySQL
9. Payment Service ← depends on Eureka + MySQL + Kafka
10. Prometheus ← optional, for monitoring
11. Grafana ← optional, visualization
```

---

## ⚙️ Configuration Summary

### Resilience4j Circuit Breaker
- **Failure Threshold:** 50%
- **Open State Duration:** 60 seconds
- **Permitted Half-Open Calls:** 3
- **Sliding Window:** 10 calls

### Resilience4j Retry
- **Max Attempts:** 3
- **Wait Duration:** 1000ms
- **Retry Exceptions:** Handled transient failures

### Timeout Configuration
- **Default Timeout:** 2000ms
- **API Gateway:** Per-route configuration

### Kafka Configuration
- **Broker ID:** 1
- **Partitions:** Default (1 per topic)
- **Replication Factor:** 1
- **Auto Topic Creation:** Enabled

---

## 🆘 Troubleshooting

### Port Already in Use
```bash
# Find and kill process on port
lsof -i :8080    # Check port 8080
kill -9 <PID>    # Kill the process
```

### Database Connection Issues
```bash
# Test MySQL connection
mysql -h localhost -u root -proot

# Check database
mysql -h localhost -u root -proot -e "SHOW DATABASES;"
```

### Service Not Registered in Eureka
```bash
# Check service health
curl http://localhost:8080/actuator/health

# View Eureka dashboard
open http://localhost:8761
```

### Kafka Connection Issues
```bash
# Check Kafka broker
kafka-broker-api-versions --bootstrap-server localhost:9092

# List topics
kafka-topics --list --bootstrap-server localhost:9092
```

---

## 📞 Support & Documentation

- **Architecture Overview:** See ARCHITECTURE_OVERVIEW.md
- **Saga Pattern Details:** See SAGA_PATTERN_GUIDE.md
- **Quick Start:** See QUICK_START.md
- **API Documentation:** See individual service README files

---

**Last Updated:** June 7, 2026  
**Version:** 1.0
