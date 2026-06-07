-- Payment Service Database Migration

CREATE TABLE IF NOT EXISTS payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    transaction_id VARCHAR(100) NOT NULL UNIQUE,
    order_id VARCHAR(50) NOT NULL UNIQUE,
    customer_id VARCHAR(100) NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    failure_reason VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_transaction_id (transaction_id),
    INDEX idx_order_id (order_id),
    INDEX idx_customer_id (customer_id)
);

-- Insert 10 sample payments
INSERT INTO payments (transaction_id, order_id, customer_id, amount, payment_method, status)
VALUES
('TXN-001', 'ORD-001', 'CUST-001', 21.98, 'CARD', 'SUCCESS'),
('TXN-002', 'ORD-002', 'CUST-002', 25.98, 'CARD', 'SUCCESS'),
('TXN-003', 'ORD-003', 'CUST-003', 18.99, 'CARD', 'FAILED'),
('TXN-004', 'ORD-004', 'CUST-004', 30.97, 'PAYPAL', 'SUCCESS'),
('TXN-005', 'ORD-005', 'CUST-005', 28.98, 'CARD', 'PROCESSING'),
('TXN-006', 'ORD-006', 'CUST-001', 34.98, 'CARD', 'SUCCESS'),
('TXN-007', 'ORD-007', 'CUST-006', 24.99, 'WALLET', 'FAILED'),
('TXN-008', 'ORD-008', 'CUST-007', 29.98, 'CARD', 'SUCCESS'),
('TXN-009', 'ORD-009', 'CUST-008', 31.98, 'BANK_TRANSFER', 'FAILED'),
('TXN-010', 'ORD-010', 'CUST-009', 26.98, 'CARD', 'SUCCESS');
