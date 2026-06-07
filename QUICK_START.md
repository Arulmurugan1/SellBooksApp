# Quick Start Guide - Book Selling App

## 🚀 Start All Services in 30 Seconds

### Prerequisites
- Docker and Docker Compose installed
- 4GB RAM available
- Ports 8080, 8761, 9092, 3306, 9090, 3000 available

### Step 1: Navigate to Project Directory
```bash
cd d:\Tech Stack\Projects\BookSellingApp
```

### Step 2: Start All Services
```bash
docker-compose up -d
```

### Step 3: Wait for Services to Start
```bash
# Check status
docker-compose ps

# Should show all 11 services running
```

### Step 4: Verify Services
```bash
# Check Eureka Server
curl http://localhost:8761

# Check API Gateway
curl http://localhost:8080/actuator/health

# Check Product Service
curl http://localhost:8081/actuator/health
```

## 📊 Access Dashboards

| Service | URL |
|---------|-----|
| **Eureka Registry** | http://localhost:8761 |
| **Prometheus** | http://localhost:9090 |
| **Grafana** | http://localhost:3000 (admin/admin) |
| **API Gateway** | http://localhost:8080 |

## 🧪 Test with Sample Commands

### 1. Create a Product
```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "productCode": "BOOK-100",
    "title": "Clean Code",
    "description": "Guide to writing better code",
    "author": "Robert C. Martin",
    "isbn": "978-0132350884",
    "price": 45.99,
    "category": "Programming",
    "available": true
  }'
```

### 2. Get All Products
```bash
curl http://localhost:8080/api/products
```

### 3. Create an Order
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "CUST-100",
    "customerEmail": "test@example.com",
    "items": [
      {
        "productId": "PROD001",
        "quantity": 1
      },
      {
        "productId": "PROD002",
        "quantity": 2
      }
    ]
  }'
```

**Sample Response**:
```json
{
  "id": 11,
  "orderId": "3f5f8a9b-1234-5678-9abc-def012345678",
  "customerId": "CUST-100",
  "customerEmail": "test@example.com",
  "items": [
    {
      "productId": "PROD001",
      "productName": "The Great Gatsby",
      "quantity": 1,
      "price": 10.99,
      "totalPrice": 10.99
    },
    {
      "productId": "PROD002",
      "productName": "To Kill a Mockingbird",
      "quantity": 2,
      "price": 12.99,
      "totalPrice": 25.98
    }
  ],
  "totalAmount": 36.97,
  "status": "PENDING"
}
```

### 4. Check Order Status
```bash
curl http://localhost:8080/api/orders/{orderId}
```

After ~2-3 seconds, order status will change to **CONFIRMED** or **PAYMENT_FAILED** based on simulated payment processing (70% success rate).

### 5. Get Order History
```bash
curl http://localhost:8080/api/orders/customer/CUST-100
```

### 6. Check Inventory
```bash
curl http://localhost:8080/api/inventory
```

### 7. Get Payment Status
```bash
curl http://localhost:8080/api/payments/order/{orderId}
```

## 📝 Saga Pattern in Action

### Success Flow (70% Probability)
```
1. Create Order → Status: PENDING
2. Check Products → Available ✓
3. Reserve Inventory → Reserved ✓
4. Publish Event → OrderCreatedEvent
5. Process Payment → SUCCESS ✓
6. Update Order → Status: CONFIRMED
7. Send Email → Notification sent
```

### Failure Flow (30% Probability)
```
1. Create Order → Status: PENDING
2. Check Products → Available ✓
3. Reserve Inventory → Reserved ✓
4. Publish Event → OrderCreatedEvent
5. Process Payment → FAILED ✗
6. Release Inventory → Compensation ✓
7. Update Order → Status: PAYMENT_FAILED
8. Send Email → Failure notification sent
```

## 🔍 Monitoring

### Prometheus Metrics
```bash
# Visit http://localhost:9090

# Useful queries:
- jvm_memory_used_bytes
- http_server_requests_seconds
- resilience4j_circuitbreaker_state
- kafka_consumer_records_consumed_total
```

### Grafana Dashboards
```bash
# Visit http://localhost:3000
# Login: admin / admin
# Add Prometheus datasource: http://prometheus:9090
```

### Docker Logs
```bash
# View specific service logs
docker-compose logs -f order-service
docker-compose logs -f payment-service
docker-compose logs -f inventory-service

# View all logs
docker-compose logs -f

# Clear old logs
docker-compose logs --tail=100
```

## 🛑 Stop Services

### Stop All Services
```bash
docker-compose down
```

### Stop and Remove All Data
```bash
docker-compose down -v
```

### Restart Services
```bash
docker-compose restart
```

## 🐛 Troubleshooting

### Services Not Starting?
```bash
# Check logs
docker-compose logs

# Rebuild images
docker-compose build --no-cache

# Restart
docker-compose down -v && docker-compose up -d
```

### Database Connection Error?
```bash
# Verify MySQL is running
docker-compose ps mysql

# Check database exists
docker exec mysql-booksellingapp mysql -u root -proot -e "SHOW DATABASES;"

# Restart MySQL
docker-compose restart mysql
```

### Kafka Issues?
```bash
# Check Kafka logs
docker-compose logs kafka

# List topics
docker exec kafka-booksellingapp kafka-topics --list --bootstrap-server localhost:9092

# Restart Kafka
docker-compose restart kafka zookeeper
```

### Services Not Registering with Eureka?
```bash
# Check Eureka dashboard
http://localhost:8761

# Look for red "UNKNOWN" status
# Restart service if needed
docker-compose restart order-service
```

## 📚 Example Workflow

### Complete Order Creation & Payment Flow
```bash
# 1. List available products
curl http://localhost:8080/api/products | jq '.[0:3]'

# 2. Check inventory
curl http://localhost:8080/api/inventory | jq '.[0:3]'

# 3. Create order
ORDER_ID=$(curl -s -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "CUST-TEST",
    "customerEmail": "test@example.com",
    "items": [
      {"productId": "PROD001", "quantity": 1},
      {"productId": "PROD003", "quantity": 2}
    ]
  }' | jq -r '.orderId')

echo "Order Created: $ORDER_ID"

# 4. Wait for payment processing (2-3 seconds)
sleep 3

# 5. Check order status
curl http://localhost:8080/api/orders/$ORDER_ID | jq '.status'

# 6. Check payment status
curl http://localhost:8080/api/payments/order/$ORDER_ID | jq '.status'

# 7. Verify inventory was updated
curl http://localhost:8080/api/inventory/PROD001 | jq '.quantityReserved'
```

## 🎯 Key Features Demonstrated

✅ **Microservices Architecture**
- 5 independent services with separate databases
- Each service manages its own data

✅ **Service Discovery**
- Eureka server for service registration
- Dynamic service lookup by name

✅ **API Gateway**
- Single entry point for all clients
- Request routing and load balancing
- Circuit breaker per service

✅ **Saga Pattern**
- Distributed transaction coordination
- Synchronous checks (product/inventory)
- Asynchronous payments
- Automatic compensation on failure

✅ **Resilience**
- Circuit breaker pattern
- Retry logic (3 attempts)
- Timeout handling
- Fallback responses

✅ **Event-Driven**
- Kafka for asynchronous messaging
- Event consumers and producers
- Loose coupling between services

✅ **Database**
- Separate databases per service
- Flyway migrations with sample data
- Eventual consistency

✅ **Monitoring**
- Prometheus metrics collection
- Grafana dashboards
- Health checks for all services
- Detailed application logs

✅ **Containerization**
- Docker images for each service
- Docker Compose orchestration
- Easy one-command deployment

## 📖 Documentation

- **README.md** - Complete project documentation
- **SAGA_PATTERN_GUIDE.md** - Detailed Saga Pattern explanation
- **API Documentation** - Swagger/OpenAPI (can be added)

## 🚀 Next Steps

1. **Explore the Saga Flow**
   - Create multiple orders
   - Observe success and failure patterns
   - Check database state changes

2. **Monitor Performance**
   - View Prometheus metrics
   - Check Grafana dashboards
   - Monitor circuit breaker states

3. **Review Code**
   - Examine Order Service (saga orchestration)
   - Check Payment Service (event consumer)
   - Study Inventory Service (compensation)

4. **Extend the Application**
   - Add Email Service
   - Implement Search functionality
   - Add Authentication/Authorization
   - Create Admin Dashboard

5. **Run Tests**
   ```bash
   # Build and run tests
   mvn clean test
   
   # Build with Docker
   mvn clean package
   
   # Build Docker images
   docker build -t product-service ./product-service
   ```

## ✉️ Support

For issues or questions:
1. Check logs: `docker-compose logs [service-name]`
2. Verify services: `docker-compose ps`
3. Check Eureka: http://localhost:8761
4. Review documentation files

---

**Happy Testing! 🎉**

The application demonstrates production-grade microservices architecture with:
- Service Discovery
- API Gateway
- Saga Pattern
- Circuit Breaker
- Kafka Messaging
- Database Per Service
- Monitoring & Observability
