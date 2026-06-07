-- Inventory Service Database Migration

CREATE TABLE IF NOT EXISTS inventories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id VARCHAR(100) NOT NULL UNIQUE,
    product_name VARCHAR(255) NOT NULL,
    quantity_available INT NOT NULL,
    quantity_reserved INT NOT NULL DEFAULT 0,
    reorder_level INT NOT NULL,
    last_restock_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_product_id (product_id)
);

CREATE TABLE IF NOT EXISTS inventory_reservations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id VARCHAR(50) NOT NULL,
    product_id VARCHAR(100) NOT NULL,
    quantity INT NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_order_id (order_id),
    INDEX idx_product_id (product_id)
);

-- Insert 10 sample inventory items matching the products
INSERT INTO inventories (product_id, product_name, quantity_available, quantity_reserved, reorder_level)
VALUES
('PROD001', 'The Great Gatsby', 50, 5, 10),
('PROD002', 'To Kill a Mockingbird', 35, 3, 10),
('PROD003', 'The Catcher in the Rye', 28, 2, 10),
('PROD004', 'Sapiens', 42, 4, 10),
('PROD005', 'Educated', 38, 1, 10),
('PROD006', '1984', 45, 8, 10),
('PROD007', 'The Hobbit', 52, 6, 10),
('PROD008', 'Dune', 30, 2, 10),
('PROD009', 'Atomic Habits', 60, 10, 15),
('PROD010', 'The Midnight Library', 25, 1, 10);
