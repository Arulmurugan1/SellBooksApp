# Postman Collection for Book Selling Application

## 📝 Overview

This document contains a complete Postman collection JSON for testing the Book Selling Application microservices. The collection includes:

- **Direct Service Testing**: Call each service directly on their individual ports
- **API Gateway Testing**: Call services through the API Gateway (port 8080)
- **All CRUD Operations**: Complete endpoints for each microservice
- **Sample Requests**: Pre-configured with example data
- **Environments**: Variables for easy switching between direct and gateway access

---

## 🚀 How to Import

1. Copy the JSON collection code below
2. Open Postman
3. Click **Import** (top left)
4. Choose **Raw text** tab
5. Paste the entire JSON collection
6. Click **Import**

---

## 📦 Postman Collection JSON

```json
{
  "info": {
    "name": "Book Selling Application - Complete API Collection",
    "description": "Comprehensive collection for testing all microservices with direct and API Gateway access",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "🔧 Setup Instructions",
      "item": [
        {
          "name": "README - Start Here",
          "request": {
            "method": "GET",
            "header": [],
            "url": {
              "raw": "http://localhost:8080/actuator/health",
              "protocol": "http",
              "host": ["localhost"],
              "port": "8080",
              "path": ["actuator", "health"]
            },
            "description": "This collection tests the Book Selling Application\n\n**Before running requests:**\n1. Ensure docker-compose is running: `docker-compose up -d`\n2. Wait 30-40 seconds for all services to start\n3. Check all services are healthy by running health check endpoints\n4. Services run on ports: 8080-8084, Database: 3306, Kafka: 9092\n\n**Environment Variables Available:**\n- `base_url`: http://localhost (use for direct service calls)\n- `gateway_url`: http://localhost:8080 (use for API Gateway calls)\n- `product_service_port`: 8081\n- `order_service_port`: 8082\n- `inventory_service_port`: 8083\n- `payment_service_port`: 8084\n\n**Testing Approach:**\n1. Test each service directly first (using individual ports)\n2. Then test through API Gateway (using port 8080)\n3. Verify Saga pattern with Order → Product → Inventory → Payment flow"
          }
        }
      ]
    },
    {
      "name": "🏥 Health Checks",
      "item": [
        {
          "name": "API Gateway Health",
          "request": {
            "method": "GET",
            "header": [],
            "url": {
              "raw": "http://localhost:8080/actuator/health",
              "protocol": "http",
              "host": ["localhost"],
              "port": "8080",
              "path": ["actuator", "health"]
            },
            "description": "Check API Gateway health status"
          }
        },
        {
          "name": "Product Service Health",
          "request": {
            "method": "GET",
            "header": [],
            "url": {
              "raw": "http://localhost:8081/actuator/health",
              "protocol": "http",
              "host": ["localhost"],
              "port": "8081",
              "path": ["actuator", "health"]
            },
            "description": "Check Product Service health status"
          }
        },
        {
          "name": "Order Service Health",
          "request": {
            "method": "GET",
            "header": [],
            "url": {
              "raw": "http://localhost:8082/actuator/health",
              "protocol": "http",
              "host": ["localhost"],
              "port": "8082",
              "path": ["actuator", "health"]
            },
            "description": "Check Order Service health status"
          }
        },
        {
          "name": "Inventory Service Health",
          "request": {
            "method": "GET",
            "header": [],
            "url": {
              "raw": "http://localhost:8083/actuator/health",
              "protocol": "http",
              "host": ["localhost"],
              "port": "8083",
              "path": ["actuator", "health"]
            },
            "description": "Check Inventory Service health status"
          }
        },
        {
          "name": "Payment Service Health",
          "request": {
            "method": "GET",
            "header": [],
            "url": {
              "raw": "http://localhost:8084/actuator/health",
              "protocol": "http",
              "host": ["localhost"],
              "port": "8084",
              "path": ["actuator", "health"]
            },
            "description": "Check Payment Service health status"
          }
        },
        {
          "name": "Eureka Server - View All Services",
          "request": {
            "method": "GET",
            "header": [],
            "url": {
              "raw": "http://localhost:8761",
              "protocol": "http",
              "host": ["localhost"],
              "port": "8761",
              "path": []
            },
            "description": "View all registered services in Eureka (open in browser)"
          }
        }
      ]
    },
    {
      "name": "📦 PRODUCT SERVICE",
      "item": [
        {
          "name": "🔗 Direct Service Calls (Port 8081)",
          "item": [
            {
              "name": "Get All Products",
              "request": {
                "method": "GET",
                "header": [],
                "url": {
                  "raw": "http://localhost:8081/api/products",
                  "protocol": "http",
                  "host": ["localhost"],
                  "port": "8081",
                  "path": ["api", "products"]
                },
                "description": "Retrieve all products from product service"
              }
            },
            {
              "name": "Get Product by ID",
              "request": {
                "method": "GET",
                "header": [],
                "url": {
                  "raw": "http://localhost:8081/api/products/1",
                  "protocol": "http",
                  "host": ["localhost"],
                  "port": "8081",
                  "path": ["api", "products", "1"]
                },
                "description": "Get specific product by ID. Replace '1' with actual product ID"
              }
            },
            {
              "name": "Get Product by Code",
              "request": {
                "method": "GET",
                "header": [],
                "url": {
                  "raw": "http://localhost:8081/api/products/code/PROD001",
                  "protocol": "http",
                  "host": ["localhost"],
                  "port": "8081",
                  "path": ["api", "products", "code", "PROD001"]
                },
                "description": "Get product by product code"
              }
            },
            {
              "name": "Check Product Availability",
              "request": {
                "method": "GET",
                "header": [],
                "url": {
                  "raw": "http://localhost:8081/api/products/check-availability/PROD001",
                  "protocol": "http",
                  "host": ["localhost"],
                  "port": "8081",
                  "path": ["api", "products", "check-availability", "PROD001"]
                },
                "description": "Check if a product is available"
              }
            },
            {
              "name": "Create Product",
              "request": {
                "method": "POST",
                "header": [
                  {
                    "key": "Content-Type",
                    "value": "application/json"
                  }
                ],
                "body": {
                  "mode": "raw",
                  "raw": "{\n  \"productCode\": \"PROD011\",\n  \"title\": \"Clean Code\",\n  \"description\": \"A Handbook of Agile Software Craftsmanship\",\n  \"author\": \"Robert C. Martin\",\n  \"isbn\": \"978-0132350884\",\n  \"price\": 39.99,\n  \"category\": \"Programming\",\n  \"available\": true\n}"
                },
                "url": {
                  "raw": "http://localhost:8081/api/products",
                  "protocol": "http",
                  "host": ["localhost"],
                  "port": "8081",
                  "path": ["api", "products"]
                },
                "description": "Create a new product"
              }
            },
            {
              "name": "Update Product",
              "request": {
                "method": "PUT",
                "header": [
                  {
                    "key": "Content-Type",
                    "value": "application/json"
                  }
                ],
                "body": {
                  "mode": "raw",
                  "raw": "{\n  \"productCode\": \"PROD011\",\n  \"title\": \"Clean Code - Updated\",\n  \"description\": \"A Handbook of Agile Software Craftsmanship\",\n  \"author\": \"Robert C. Martin\",\n  \"isbn\": \"978-0132350884\",\n  \"price\": 35.99,\n  \"category\": \"Programming\",\n  \"available\": true\n}"
                },
                "url": {
                  "raw": "http://localhost:8081/api/products/1",
                  "protocol": "http",
                  "host": ["localhost"],
                  "port": "8081",
                  "path": ["api", "products", "1"]
                },
                "description": "Update an existing product. Replace '1' with product ID"
              }
            },
            {
              "name": "Delete Product",
              "request": {
                "method": "DELETE",
                "header": [],
                "url": {
                  "raw": "http://localhost:8081/api/products/1",
                  "protocol": "http",
                  "host": ["localhost"],
                  "port": "8081",
                  "path": ["api", "products", "1"]
                },
                "description": "Delete a product. Replace '1' with product ID"
              }
            }
          ]
        },
        {
          "name": "🚪 API Gateway Access (Port 8080)",
          "item": [
            {
              "name": "Get All Products via Gateway",
              "request": {
                "method": "GET",
                "header": [],
                "url": {
                  "raw": "http://localhost:8080/api/products",
                  "protocol": "http",
                  "host": ["localhost"],
                  "port": "8080",
                  "path": ["api", "products"]
                },
                "description": "Get all products through API Gateway"
              }
            },
            {
              "name": "Get Product by Code via Gateway",
              "request": {
                "method": "GET",
                "header": [],
                "url": {
                  "raw": "http://localhost:8080/api/products/code/PROD001",
                  "protocol": "http",
                  "host": ["localhost"],
                  "port": "8080",
                  "path": ["api", "products", "code", "PROD001"]
                },
                "description": "Get product via API Gateway"
              }
            },
            {
              "name": "Create Product via Gateway",
              "request": {
                "method": "POST",
                "header": [
                  {
                    "key": "Content-Type",
                    "value": "application/json"
                  }
                ],
                "body": {
                  "mode": "raw",
                  "raw": "{\n  \"productCode\": \"PROD012\",\n  \"title\": \"Design Patterns\",\n  \"description\": \"Elements of Reusable Object-Oriented Software\",\n  \"author\": \"Gang of Four\",\n  \"isbn\": \"978-0201633610\",\n  \"price\": 45.99,\n  \"category\": \"Programming\",\n  \"available\": true\n}"
                },
                "url": {
                  "raw": "http://localhost:8080/api/products",
                  "protocol": "http",
                  "host": ["localhost"],
                  "port": "8080",
                  "path": ["api", "products"]
                },
                "description": "Create product through API Gateway"
              }
            }
          ]
        }
      ]
    },
    {
      "name": "📋 ORDER SERVICE",
      "item": [
        {
          "name": "🔗 Direct Service Calls (Port 8082)",
          "item": [
            {
              "name": "Create Order",
              "request": {
                "method": "POST",
                "header": [
                  {
                    "key": "Content-Type",
                    "value": "application/json"
                  }
                ],
                "body": {
                  "mode": "raw",
                  "raw": "{\n  \"customerId\": \"CUST001\",\n  \"customerEmail\": \"customer@example.com\",\n  \"items\": [\n    {\n      \"productCode\": \"PROD001\",\n      \"quantity\": 2\n    },\n    {\n      \"productCode\": \"PROD002\",\n      \"quantity\": 1\n    }\n  ]\n}"
                },
                "url": {
                  "raw": "http://localhost:8082/api/orders",
                  "protocol": "http",
                  "host": ["localhost"],
                  "port": "8082",
                  "path": ["api", "orders"]
                },
                "description": "Create a new order. This triggers the Saga pattern:\n1. Check product availability\n2. Reserve inventory\n3. Publish order-created event\n4. Payment service processes payment\n5. On success: order confirmed\n6. On failure: inventory released (compensation)"
              }
            },
            {
              "name": "Get Order by ID",
              "request": {
                "method": "GET",
                "header": [],
                "url": {
                  "raw": "http://localhost:8082/api/orders/ORD001",
                  "protocol": "http",
                  "host": ["localhost"],
                  "port": "8082",
                  "path": ["api", "orders", "ORD001"]
                },
                "description": "Get order details by order ID"
              }
            },
            {
              "name": "Get Orders by Customer",
              "request": {
                "method": "GET",
                "header": [],
                "url": {
                  "raw": "http://localhost:8082/api/orders/customer/CUST001",
                  "protocol": "http",
                  "host": ["localhost"],
                  "port": "8082",
                  "path": ["api", "orders", "customer", "CUST001"]
                },
                "description": "Get all orders for a specific customer"
              }
            },
            {
              "name": "Get All Orders",
              "request": {
                "method": "GET",
                "header": [],
                "url": {
                  "raw": "http://localhost:8082/api/orders",
                  "protocol": "http",
                  "host": ["localhost"],
                  "port": "8082",
                  "path": ["api", "orders"]
                },
                "description": "Get all orders from the system"
              }
            }
          ]
        },
        {
          "name": "🚪 API Gateway Access (Port 8080)",
          "item": [
            {
              "name": "Create Order via Gateway",
              "request": {
                "method": "POST",
                "header": [
                  {
                    "key": "Content-Type",
                    "value": "application/json"
                  }
                ],
                "body": {
                  "mode": "raw",
                  "raw": "{\n  \"customerId\": \"CUST002\",\n  \"customerEmail\": \"john.doe@example.com\",\n  \"items\": [\n    {\n      \"productCode\": \"PROD003\",\n      \"quantity\": 3\n    }\n  ]\n}"
                },
                "url": {
                  "raw": "http://localhost:8080/api/orders",
                  "protocol": "http",
                  "host": ["localhost"],
                  "port": "8080",
                  "path": ["api", "orders"]
                },
                "description": "Create order through API Gateway"
              }
            },
            {
              "name": "Get All Orders via Gateway",
              "request": {
                "method": "GET",
                "header": [],
                "url": {
                  "raw": "http://localhost:8080/api/orders",
                  "protocol": "http",
                  "host": ["localhost"],
                  "port": "8080",
                  "path": ["api", "orders"]
                },
                "description": "Retrieve all orders through API Gateway"
              }
            },
            {
              "name": "Get Order by ID via Gateway",
              "request": {
                "method": "GET",
                "header": [],
                "url": {
                  "raw": "http://localhost:8080/api/orders/ORD001",
                  "protocol": "http",
                  "host": ["localhost"],
                  "port": "8080",
                  "path": ["api", "orders", "ORD001"]
                },
                "description": "Get order details through API Gateway"
              }
            }
          ]
        }
      ]
    },
    {
      "name": "📦 INVENTORY SERVICE",
      "item": [
        {
          "name": "🔗 Direct Service Calls (Port 8083)",
          "item": [
            {
              "name": "Check Stock Availability",
              "request": {
                "method": "POST",
                "header": [
                  {
                    "key": "Content-Type",
                    "value": "application/json"
                  }
                ],
                "body": {
                  "mode": "raw",
                  "raw": "{\n  \"items\": [\n    {\n      \"productId\": 1,\n      \"quantity\": 2\n    },\n    {\n      \"productId\": 2,\n      \"quantity\": 1\n    }\n  ]\n}"
                },
                "url": {
                  "raw": "http://localhost:8083/api/inventory/check-stock",
                  "protocol": "http",
                  "host": ["localhost"],
                  "port": "8083",
                  "path": ["api", "inventory", "check-stock"]
                },
                "description": "Check if inventory is available for requested items"
              }
            },
            {
              "name": "Reserve Inventory",
              "request": {
                "method": "POST",
                "header": [
                  {
                    "key": "Content-Type",
                    "value": "application/json"
                  }
                ],
                "body": {
                  "mode": "raw",
                  "raw": "{\n  \"orderId\": \"ORD001\",\n  \"items\": [\n    {\n      \"productId\": 1,\n      \"quantity\": 2\n    }\n  ]\n}"
                },
                "url": {
                  "raw": "http://localhost:8083/api/inventory/reserve",
                  "protocol": "http",
                  "host": ["localhost"],
                  "port": "8083",
                  "path": ["api", "inventory", "reserve"]
                },
                "description": "Reserve inventory for an order"
              }
            },
            {
              "name": "Release Reserved Inventory",
              "request": {
                "method": "POST",
                "header": [
                  {
                    "key": "Content-Type",
                    "value": "application/json"
                  }
                ],
                "body": {
                  "mode": "raw",
                  "raw": "{\n  \"orderId\": \"ORD001\",\n  \"items\": [\n    {\n      \"productId\": 1,\n      \"quantity\": 2\n    }\n  ]\n}"
                },
                "url": {
                  "raw": "http://localhost:8083/api/inventory/release",
                  "protocol": "http",
                  "host": ["localhost"],
                  "port": "8083",
                  "path": ["api", "inventory", "release"]
                },
                "description": "Release reserved inventory (compensation when payment fails)"
              }
            },
            {
              "name": "Get Inventory by Product",
              "request": {
                "method": "GET",
                "header": [],
                "url": {
                  "raw": "http://localhost:8083/api/inventory/1",
                  "protocol": "http",
                  "host": ["localhost"],
                  "port": "8083",
                  "path": ["api", "inventory", "1"]
                },
                "description": "Get inventory details for a specific product"
              }
            },
            {
              "name": "Get All Inventory",
              "request": {
                "method": "GET",
                "header": [],
                "url": {
                  "raw": "http://localhost:8083/api/inventory",
                  "protocol": "http",
                  "host": ["localhost"],
                  "port": "8083",
                  "path": ["api", "inventory"]
                },
                "description": "Get all inventory items"
              }
            },
            {
              "name": "Get Low Stock Items",
              "request": {
                "method": "GET",
                "header": [],
                "url": {
                  "raw": "http://localhost:8083/api/inventory/low-stock",
                  "protocol": "http",
                  "host": ["localhost"],
                  "port": "8083",
                  "path": ["api", "inventory", "low-stock"]
                },
                "description": "Get items with low stock levels"
              }
            },
            {
              "name": "Update Inventory",
              "request": {
                "method": "PUT",
                "header": [
                  {
                    "key": "Content-Type",
                    "value": "application/json"
                  }
                ],
                "body": {
                  "mode": "raw",
                  "raw": "{\n  \"quantityAvailable\": 50,\n  \"reorderLevel\": 10\n}"
                },
                "url": {
                  "raw": "http://localhost:8083/api/inventory/1",
                  "protocol": "http",
                  "host": ["localhost"],
                  "port": "8083",
                  "path": ["api", "inventory", "1"]
                },
                "description": "Update inventory for a product"
              }
            }
          ]
        },
        {
          "name": "🚪 API Gateway Access (Port 8080)",
          "item": [
            {
              "name": "Check Stock via Gateway",
              "request": {
                "method": "POST",
                "header": [
                  {
                    "key": "Content-Type",
                    "value": "application/json"
                  }
                ],
                "body": {
                  "mode": "raw",
                  "raw": "{\n  \"items\": [\n    {\n      \"productId\": 1,\n      \"quantity\": 1\n    }\n  ]\n}"
                },
                "url": {
                  "raw": "http://localhost:8080/api/inventory/check-stock",
                  "protocol": "http",
                  "host": ["localhost"],
                  "port": "8080",
                  "path": ["api", "inventory", "check-stock"]
                },
                "description": "Check stock through API Gateway"
              }
            },
            {
              "name": "Get All Inventory via Gateway",
              "request": {
                "method": "GET",
                "header": [],
                "url": {
                  "raw": "http://localhost:8080/api/inventory",
                  "protocol": "http",
                  "host": ["localhost"],
                  "port": "8080",
                  "path": ["api", "inventory"]
                },
                "description": "Get all inventory through API Gateway"
              }
            }
          ]
        }
      ]
    },
    {
      "name": "💳 PAYMENT SERVICE",
      "item": [
        {
          "name": "🔗 Direct Service Calls (Port 8084)",
          "item": [
            {
              "name": "Get Payment by Transaction ID",
              "request": {
                "method": "GET",
                "header": [],
                "url": {
                  "raw": "http://localhost:8084/api/payments/transaction/TXN001",
                  "protocol": "http",
                  "host": ["localhost"],
                  "port": "8084",
                  "path": ["api", "payments", "transaction", "TXN001"]
                },
                "description": "Get payment details by transaction ID"
              }
            },
            {
              "name": "Get Payment by Order ID",
              "request": {
                "method": "GET",
                "header": [],
                "url": {
                  "raw": "http://localhost:8084/api/payments/order/ORD001",
                  "protocol": "http",
                  "host": ["localhost"],
                  "port": "8084",
                  "path": ["api", "payments", "order", "ORD001"]
                },
                "description": "Get payment status by order ID"
              }
            }
          ]
        },
        {
          "name": "🚪 API Gateway Access (Port 8080)",
          "item": [
            {
              "name": "Get Payment by Order ID via Gateway",
              "request": {
                "method": "GET",
                "header": [],
                "url": {
                  "raw": "http://localhost:8080/api/payments/order/ORD001",
                  "protocol": "http",
                  "host": ["localhost"],
                  "port": "8080",
                  "path": ["api", "payments", "order", "ORD001"]
                },
                "description": "Get payment details through API Gateway"
              }
            },
            {
              "name": "Get Payment by Transaction ID via Gateway",
              "request": {
                "method": "GET",
                "header": [],
                "url": {
                  "raw": "http://localhost:8080/api/payments/transaction/TXN001",
                  "protocol": "http",
                  "host": ["localhost"],
                  "port": "8080",
                  "path": ["api", "payments", "transaction", "TXN001"]
                },
                "description": "Get payment by transaction ID through API Gateway"
              }
            }
          ]
        }
      ]
    },
    {
      "name": "🔄 SAGA PATTERN - COMPLETE FLOW",
      "item": [
        {
          "name": "Step 1: Create Order (Triggers Saga)",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Content-Type",
                "value": "application/json"
              }
            ],
            "body": {
              "mode": "raw",
              "raw": "{\n  \"customerId\": \"CUST_SAGA_TEST\",\n  \"customerEmail\": \"saga.test@example.com\",\n  \"items\": [\n    {\n      \"productCode\": \"PROD001\",\n      \"quantity\": 1\n    },\n    {\n      \"productCode\": \"PROD002\",\n      \"quantity\": 2\n    }\n  ]\n}"
            },
            "url": {
              "raw": "http://localhost:8080/api/orders",
              "protocol": "http",
              "host": ["localhost"],
              "port": "8080",
              "path": ["api", "orders"]
            },
            "description": "**SAGA START**: Create an order which triggers the complete Saga flow\n\nFlow:\n1. Order Service creates order with PENDING status\n2. Product Service checks availability (synchronous)\n3. Inventory Service reserves stock (synchronous)\n4. Order status changes to INVENTORY_RESERVED\n5. Order-created event published to Kafka\n6. Payment Service consumes event and processes payment\n7. On SUCCESS: Order confirmed, payment recorded\n8. On FAILURE: Order cancelled, inventory released (compensation)\n\n**Note down the orderId from the response for the next steps**"
            }
          }
        },
        {
          "name": "Step 2: Check Order Status",
          "request": {
            "method": "GET",
            "header": [],
            "url": {
              "raw": "http://localhost:8080/api/orders/{{orderId}}",
              "protocol": "http",
              "host": ["localhost"],
              "port": "8080",
              "path": ["api", "orders", "{{orderId}}"]
            },
            "description": "Check the order status after creation. Replace {{orderId}} with actual order ID from Step 1\n\nPossible statuses:\n- PENDING: Initial state\n- INVENTORY_RESERVED: Stock reserved\n- PAYMENT_PROCESSING: Payment in progress\n- CONFIRMED: Success (payment succeeded)\n- PAYMENT_FAILED: Order failed (payment failed, inventory released)\n- COMPLETED: Order completed"
          }
        },
        {
          "name": "Step 3: Check Payment Status",
          "request": {
            "method": "GET",
            "header": [],
            "url": {
              "raw": "http://localhost:8080/api/payments/order/{{orderId}}",
              "protocol": "http",
              "host": ["localhost"],
              "port": "8080",
              "path": ["api", "payments", "order", "{{orderId}}"]
            },
            "description": "Check payment status for the order. Replace {{orderId}} with actual order ID\n\nPayment statuses:\n- PENDING: Waiting to process\n- PROCESSING: Payment in progress\n- SUCCESS: Payment successful\n- FAILED: Payment failed (70% success rate in simulation)"
          }
        },
        {
          "name": "Step 4: Verify Inventory Reservation",
          "request": {
            "method": "GET",
            "header": [],
            "url": {
              "raw": "http://localhost:8080/api/inventory",
              "protocol": "http",
              "host": ["localhost"],
              "port": "8080",
              "path": ["api", "inventory"]
            },
            "description": "Verify inventory levels after order completion\n\nCheck if:\n- quantityReserved increased for ordered products\n- On payment success: Reserved quantities remain as confirmed\n- On payment failure: Inventory released back to available"
          }
        }
      ]
    },
    {
      "name": "📊 MONITORING & METRICS",
      "item": [
        {
          "name": "Prometheus - Query Metrics",
          "request": {
            "method": "GET",
            "header": [],
            "url": {
              "raw": "http://localhost:9090/api/v1/query?query=http_request_duration_seconds_sum",
              "protocol": "http",
              "host": ["localhost"],
              "port": "9090",
              "path": ["api", "v1", "query"],
              "query": [
                {
                  "key": "query",
                  "value": "http_request_duration_seconds_sum"
                }
              ]
            },
            "description": "Query Prometheus for HTTP request duration metrics"
          }
        },
        {
          "name": "Prometheus - Circuit Breaker Status",
          "request": {
            "method": "GET",
            "header": [],
            "url": {
              "raw": "http://localhost:9090/api/v1/query?query=resilience4j_circuitbreaker_state",
              "protocol": "http",
              "host": ["localhost"],
              "port": "9090",
              "path": ["api", "v1", "query"],
              "query": [
                {
                  "key": "query",
                  "value": "resilience4j_circuitbreaker_state"
                }
              ]
            },
            "description": "Check circuit breaker states across services"
          }
        },
        {
          "name": "Grafana Dashboard",
          "request": {
            "method": "GET",
            "header": [],
            "url": {
              "raw": "http://localhost:3000",
              "protocol": "http",
              "host": ["localhost"],
              "port": "3000",
              "path": []
            },
            "description": "Access Grafana dashboards (open in browser)\nLogin: admin / admin\n\nAvailable Dashboards:\n- JVM Metrics\n- HTTP Request Metrics\n- Service Health\n- Circuit Breaker Status"
          }
        }
      ]
    },
    {
      "name": "🔍 TESTING SCENARIOS",
      "item": [
        {
          "name": "Scenario 1: Successful Order Flow",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Content-Type",
                "value": "application/json"
              }
            ],
            "body": {
              "mode": "raw",
              "raw": "{\n  \"customerId\": \"CUST_SUCCESS\",\n  \"customerEmail\": \"success@example.com\",\n  \"items\": [\n    {\n      \"productCode\": \"PROD001\",\n      \"quantity\": 1\n    }\n  ]\n}\n\n**Expected Flow:**\n1. Order created with PENDING status\n2. Product availability checked ✓\n3. Inventory reserved ✓\n4. Payment processed (70% success rate)\n5. Order status → CONFIRMED\n6. Inventory remains reserved"
            },
            "url": {
              "raw": "http://localhost:8080/api/orders",
              "protocol": "http",
              "host": ["localhost"],
              "port": "8080",
              "path": ["api", "orders"]
            },
            "description": "Test successful order completion scenario"
          }
        },
        {
          "name": "Scenario 2: Payment Failure with Compensation",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Content-Type",
                "value": "application/json"
              }
            ],
            "body": {
              "mode": "raw",
              "raw": "{\n  \"customerId\": \"CUST_FAILURE\",\n  \"customerEmail\": \"failure@example.com\",\n  \"items\": [\n    {\n      \"productCode\": \"PROD002\",\n      \"quantity\": 1\n    }\n  ]\n}\n\n**Expected Flow (30% of attempts):**\n1. Order created with PENDING status\n2. Product availability checked ✓\n3. Inventory reserved ✓\n4. Payment processing fails ✗\n5. COMPENSATION: Inventory released\n6. Order status → PAYMENT_FAILED\n7. Event published: order-cancelled"
            },
            "url": {
              "raw": "http://localhost:8080/api/orders",
              "protocol": "http",
              "host": ["localhost"],
              "port": "8080",
              "path": ["api", "orders"]
            },
            "description": "Test order with payment failure and inventory compensation"
          }
        },
        {
          "name": "Scenario 3: Test with Multiple Items",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Content-Type",
                "value": "application/json"
              }
            ],
            "body": {
              "mode": "raw",
              "raw": "{\n  \"customerId\": \"CUST_BULK\",\n  \"customerEmail\": \"bulk@example.com\",\n  \"items\": [\n    {\n      \"productCode\": \"PROD001\",\n      \"quantity\": 3\n    },\n    {\n      \"productCode\": \"PROD002\",\n      \"quantity\": 2\n    },\n    {\n      \"productCode\": \"PROD003\",\n      \"quantity\": 1\n    }\n  ]\n}"
            },
            "url": {
              "raw": "http://localhost:8080/api/orders",
              "protocol": "http",
              "host": ["localhost"],
              "port": "8080",
              "path": ["api", "orders"]
            },
            "description": "Test order with multiple items to verify batch processing"
          }
        },
        {
          "name": "Scenario 4: Test via Direct Service vs Gateway",
          "request": {
            "method": "GET",
            "header": [],
            "url": {
              "raw": "http://localhost:8080/api/products",
              "protocol": "http",
              "host": ["localhost"],
              "port": "8080",
              "path": ["api", "products"]
            },
            "description": "Compare responses:\n\n1. Run this request (via Gateway)\n2. Run identical request to http://localhost:8081/api/products (direct)\n3. Compare response times and data\n\n**Gateway adds:**\n- Routing overhead\n- Circuit breaker protection\n- Load balancing\n- Service discovery"
          }
        }
      ]
    },
    {
      "name": "⚡ QUICK TEST SEQUENCE",
      "item": [
        {
          "name": "1️⃣ First: Check All Services Health",
          "request": {
            "method": "GET",
            "header": [],
            "url": {
              "raw": "http://localhost:8080/actuator/health",
              "protocol": "http",
              "host": ["localhost"],
              "port": "8080",
              "path": ["actuator", "health"]
            },
            "description": "Run this first to verify API Gateway is healthy"
          }
        },
        {
          "name": "2️⃣ Then: Get All Products",
          "request": {
            "method": "GET",
            "header": [],
            "url": {
              "raw": "http://localhost:8080/api/products",
              "protocol": "http",
              "host": ["localhost"],
              "port": "8080",
              "path": ["api", "products"]
            },
            "description": "Verify product service is accessible. Note a productCode for order creation."
          }
        },
        {
          "name": "3️⃣ Then: Create Test Order",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Content-Type",
                "value": "application/json"
              }
            ],
            "body": {
              "mode": "raw",
              "raw": "{\n  \"customerId\": \"QUICK_TEST\",\n  \"customerEmail\": \"quicktest@example.com\",\n  \"items\": [\n    {\n      \"productCode\": \"PROD001\",\n      \"quantity\": 1\n    }\n  ]\n}"
            },
            "url": {
              "raw": "http://localhost:8080/api/orders",
              "protocol": "http",
              "host": ["localhost"],
              "port": "8080",
              "path": ["api", "orders"]
            },
            "description": "Create an order and note the orderId from response"
          }
        },
        {
          "name": "4️⃣ Finally: Check Order Status",
          "request": {
            "method": "GET",
            "header": [],
            "url": {
              "raw": "http://localhost:8080/api/orders",
              "protocol": "http",
              "host": ["localhost"],
              "port": "8080",
              "path": ["api", "orders"]
            },
            "description": "Get all orders to see the newly created order and its status. Wait 2-3 seconds after order creation for payment to process."
          }
        }
      ]
    }
  ]
}
```

---

## 🎯 Environment Variables Setup

You can create an Environment in Postman with these variables:

```json
{
  "id": "book-selling-app-env",
  "name": "Book Selling App - Local Development",
  "values": [
    {
      "key": "base_url",
      "value": "http://localhost",
      "enabled": true
    },
    {
      "key": "gateway_url",
      "value": "http://localhost:8080",
      "enabled": true
    },
    {
      "key": "product_service_port",
      "value": "8081",
      "enabled": true
    },
    {
      "key": "order_service_port",
      "value": "8082",
      "enabled": true
    },
    {
      "key": "inventory_service_port",
      "value": "8083",
      "enabled": true
    },
    {
      "key": "payment_service_port",
      "value": "8084",
      "enabled": true
    },
    {
      "key": "orderId",
      "value": "ORD001",
      "enabled": true
    }
  ]
}
```

---

## 📝 How to Use This Collection

### Basic Flow:
1. **Import the Collection** into Postman
2. **Set Environment** to "Book Selling App - Local Development"
3. **Run Health Checks** first to verify all services are running
4. **Test Each Service** using the organized folders
5. **Test Complete Saga Flow** using the SAGA PATTERN folder
6. **Run Quick Test Sequence** for a complete end-to-end test

### Testing Approach:

**Option A: Direct Service Testing**
- Use requests in the `🔗 Direct Service Calls` folders
- Test each microservice independently
- Ports: 8081-8084

**Option B: API Gateway Testing**
- Use requests in the `🚪 API Gateway Access` folders
- Test routing and load balancing
- Port: 8080

**Option C: Complete Saga Flow**
- Follow the `🔄 SAGA PATTERN` folder
- Test the entire distributed transaction
- Monitor compensation on payment failure

### Key Points:

✅ **Before Running:** Ensure `docker-compose up -d` is executed  
✅ **Wait Time:** Allow 30-40 seconds for all services to start  
✅ **Test Isolation:** Each request is independent and can be run individually  
✅ **Response Format:** All responses are JSON  
✅ **Error Handling:** Check response status codes for errors  
✅ **Async Processing:** Order creation is async - payment processes in background  

---

## 🔗 Related Resources

- **Architecture Overview:** See ARCHITECTURE_OVERVIEW.md
- **Saga Pattern Guide:** See SAGA_PATTERN_GUIDE.md
- **Quick Start:** See QUICK_START.md
- **Host Information:** See HOST_INFORMATION_REFERENCE.md

---

**Version:** 1.0  
**Last Updated:** June 7, 2026  
**For:** Book Selling Application - Microservices Testing
