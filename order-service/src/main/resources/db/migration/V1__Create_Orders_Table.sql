-- Order Service Database Migration

CREATE TABLE IF NOT EXISTS orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id VARCHAR(50) NOT NULL UNIQUE,
    customer_id VARCHAR(100) NOT NULL,
    customer_email VARCHAR(255) NOT NULL,
    total_amount DECIMAL(10, 2) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_order_id (order_id),
    INDEX idx_customer_id (customer_id)
);

CREATE TABLE IF NOT EXISTS order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id VARCHAR(100) NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    quantity INT NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    total_price DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);

-- Insert 10 sample orders
INSERT INTO orders (order_id, customer_id, customer_email, total_amount, status)
VALUES
('ORD-001', 'CUST-001', 'john@example.com', 21.98, 'PENDING'),
('ORD-002', 'CUST-002', 'jane@example.com', 25.98, 'CONFIRMED'),
('ORD-003', 'CUST-003', 'mike@example.com', 18.99, 'PAYMENT_FAILED'),
('ORD-004', 'CUST-004', 'sarah@example.com', 30.97, 'CONFIRMED'),
('ORD-005', 'CUST-005', 'david@example.com', 28.98, 'PENDING'),
('ORD-006', 'CUST-001', 'john@example.com', 34.98, 'CONFIRMED'),
('ORD-007', 'CUST-006', 'emily@example.com', 24.99, 'CANCELLED'),
('ORD-008', 'CUST-007', 'robert@example.com', 29.98, 'CONFIRMED'),
('ORD-009', 'CUST-008', 'lisa@example.com', 31.98, 'PAYMENT_FAILED'),
('ORD-010', 'CUST-009', 'james@example.com', 26.98, 'CONFIRMED');

-- Insert order items
INSERT INTO order_items (order_id, product_id, product_name, quantity, price, total_price)
SELECT id, 'PROD001', 'The Great Gatsby', 1, 10.99, 10.99 FROM orders WHERE order_id = 'ORD-001'
UNION ALL
SELECT id, 'PROD002', 'To Kill a Mockingbird', 1, 10.99, 10.99 FROM orders WHERE order_id = 'ORD-001'
UNION ALL
SELECT id, 'PROD003', 'The Catcher in the Rye', 1, 12.99, 12.99 FROM orders WHERE order_id = 'ORD-002'
UNION ALL
SELECT id, 'PROD004', 'Sapiens', 1, 12.99, 12.99 FROM orders WHERE order_id = 'ORD-002'
UNION ALL
SELECT id, 'PROD005', 'Educated', 1, 18.99, 18.99 FROM orders WHERE order_id = 'ORD-003'
UNION ALL
SELECT id, 'PROD006', '1984', 2, 13.99, 27.98 FROM orders WHERE order_id = 'ORD-004'
UNION ALL
SELECT id, 'PROD001', 'The Great Gatsby', 2, 10.99, 21.98 FROM orders WHERE order_id = 'ORD-005'
UNION ALL
SELECT id, 'PROD007', 'The Hobbit', 2, 14.99, 29.98 FROM orders WHERE order_id = 'ORD-006'
UNION ALL
SELECT id, 'PROD008', 'Dune', 1, 15.99, 15.99 FROM orders WHERE order_id = 'ORD-007'
UNION ALL
SELECT id, 'PROD009', 'Atomic Habits', 1, 17.99, 17.99 FROM orders WHERE order_id = 'ORD-008'
UNION ALL
SELECT id, 'PROD010', 'The Midnight Library', 1, 18.99, 18.99 FROM orders WHERE order_id = 'ORD-009'
UNION ALL
SELECT id, 'PROD002', 'To Kill a Mockingbird', 1, 12.99, 12.99 FROM orders WHERE order_id = 'ORD-010';
