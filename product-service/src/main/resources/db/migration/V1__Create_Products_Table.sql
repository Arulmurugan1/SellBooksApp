-- Product Service Database Migration

CREATE TABLE IF NOT EXISTS products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_code VARCHAR(50) NOT NULL UNIQUE,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    author VARCHAR(255) NOT NULL,
    isbn VARCHAR(20) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    category VARCHAR(100) NOT NULL,
    available BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_product_code (product_code),
    INDEX idx_isbn (isbn),
    INDEX idx_category (category)
);

-- Insert 10 sample products
INSERT INTO products (product_code, title, description, author, isbn, price, category, available)
VALUES
('PROD001', 'The Great Gatsby', 'A classic tale of love and ambition in the Jazz Age', 'F. Scott Fitzgerald', '978-0743273565', 10.99, 'Fiction', true),
('PROD002', 'To Kill a Mockingbird', 'A gripping tale of racial injustice and childhood innocence', 'Harper Lee', '978-0061120084', 12.99, 'Fiction', true),
('PROD003', 'The Catcher in the Rye', 'The story of teenage angst and alienation', 'J.D. Salinger', '978-0316769174', 11.99, 'Fiction', true),
('PROD004', 'Sapiens', 'A brief history of humankind from the Stone Age to modern times', 'Yuval Noah Harari', '978-0062316097', 18.99, 'Non-Fiction', true),
('PROD005', 'Educated', 'A memoir about a woman who leaves her survivalist family to pursue education', 'Tara Westover', '978-0399590504', 16.99, 'Biography', true),
('PROD006', '1984', 'A dystopian novel about totalitarianism and surveillance', 'George Orwell', '978-0451524935', 13.99, 'Fiction', true),
('PROD007', 'The Hobbit', 'An adventure fantasy novel about a reluctant hero', 'J.R.R. Tolkien', '978-0547928227', 14.99, 'Fantasy', true),
('PROD008', 'Dune', 'An epic science fiction novel set on a desert planet', 'Frank Herbert', '978-0441172719', 15.99, 'Science Fiction', true),
('PROD009', 'Atomic Habits', 'A guide to building good habits and breaking bad ones', 'James Clear', '978-0735211292', 17.99, 'Self-Help', true),
('PROD010', 'The Midnight Library', 'A novel about the choices we make and the lives we could have lived', 'Matt Haig', '978-0525559474', 18.99, 'Fiction', true);
