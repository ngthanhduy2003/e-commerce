-- ================================================
-- E-COMMERCE DATABASE INITIALIZATION & TEST DATA
-- ================================================
-- This script initializes the database with:
-- 1. Required roles (ADMIN, USER, STAFF)
-- 2. Test users with different roles
-- 3. Product categories
-- 4. Sample products with variants (SKUs)
-- 5. Optional test cart and order data
--
-- Run this AFTER the application creates the schema
-- ================================================

-- ================================================
-- 1. ROLES
-- ================================================

-- Insert default roles if they don't exist
INSERT IGNORE INTO roles (id, name, description) VALUES
(1, 'USER', 'Regular customer with basic permissions'),
(2, 'ADMIN', 'Administrator with full system access'),
(3, 'STAFF', 'Staff member with limited admin access');

-- ================================================
-- 2. TEST USERS
-- ================================================

-- Password for all test users: "password123"
-- BCrypt hash: $2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH

-- Admin user
INSERT IGNORE INTO users (id, email, password_hash, full_name, role_id, status, created_at, updated_at) VALUES
(1, 'admin@ecommerce.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'System Administrator', 2, 'ACTIVE', NOW(), NOW());

-- Regular customer users
INSERT IGNORE INTO users (id, email, password_hash, full_name, role_id, status, created_at, updated_at) VALUES
(2, 'customer@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'John Doe', 1, 'ACTIVE', NOW(), NOW()),
(3, 'jane.smith@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'Jane Smith', 1, 'ACTIVE', NOW(), NOW());

-- Staff user
INSERT IGNORE INTO users (id, email, password_hash, full_name, role_id, status, created_at, updated_at) VALUES
(4, 'staff@ecommerce.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'Staff Member', 3, 'ACTIVE', NOW(), NOW());

-- ================================================
-- 3. CATEGORIES
-- ================================================

INSERT IGNORE INTO categories (id, name, description) VALUES
(1, 'Electronics', 'Electronic devices and accessories'),
(2, 'Clothing', 'Men and women clothing and footwear'),
(3, 'Books', 'Books and magazines'),
(4, 'Home & Garden', 'Home decor and garden supplies'),
(5, 'Sports', 'Sports equipment and accessories');

-- ================================================
-- 4. PRODUCTS
-- ================================================

INSERT IGNORE INTO products (id, name, category_id, description, status, created_at, updated_at) VALUES
(1, 'iPhone 15 Pro', 1, 'Latest iPhone with A17 Pro chip, Dynamic Island, and titanium design', 'ACTIVE', NOW(), NOW()),
(2, 'MacBook Pro M3', 1, '14-inch MacBook Pro with M3 chip, Liquid Retina XDR display', 'ACTIVE', NOW(), NOW()),
(3, 'Samsung Galaxy S24', 1, 'Flagship Android phone with AI features and exceptional camera', 'ACTIVE', NOW(), NOW()),
(4, 'Nike Air Max', 2, 'Comfortable running shoes with Air cushioning technology', 'ACTIVE', NOW(), NOW()),
(5, 'Levi''s Jeans', 2, 'Classic denim jeans with timeless style', 'ACTIVE', NOW(), NOW()),
(6, 'The Great Gatsby', 3, 'Classic American novel by F. Scott Fitzgerald', 'ACTIVE', NOW(), NOW()),
(7, 'Garden Tools Set', 4, 'Complete gardening tool set with premium quality', 'ACTIVE', NOW(), NOW()),
(8, 'Cotton T-Shirt', 2, 'Premium 100% cotton t-shirt, soft and comfortable', 'ACTIVE', NOW(), NOW());

-- ================================================
-- 5. PRODUCT VARIANTS
-- ================================================

-- iPhone 15 Pro variants (Storage x Color)
-- Note: Size as INTEGER representing storage in GB (128, 256, 512)
INSERT IGNORE INTO product_variants (id, product_id, size, color, price, stock_quantity, status, created_at, updated_at) VALUES
(1, 1, 128, 'Black', 999.00, 50, 'ACTIVE', NOW(), NOW()),
(2, 1, 256, 'Black', 1099.00, 30, 'ACTIVE', NOW(), NOW()),
(3, 1, 512, 'Black', 1299.00, 20, 'ACTIVE', NOW(), NOW()),
(4, 1, 128, 'White', 999.00, 45, 'ACTIVE', NOW(), NOW()),
(5, 1, 256, 'White', 1099.00, 25, 'ACTIVE', NOW(), NOW()),
(6, 1, 512, 'White', 1299.00, 15, 'ACTIVE', NOW(), NOW());

-- MacBook Pro M3 variants (Storage x Color)
INSERT IGNORE INTO product_variants (id, product_id, size, color, price, stock_quantity, status, created_at, updated_at) VALUES
(7, 2, 512, 'Silver', 1999.00, 15, 'ACTIVE', NOW(), NOW()),
(8, 2, 1024, 'Silver', 2499.00, 10, 'ACTIVE', NOW(), NOW()),
(9, 2, 512, 'Space Gray', 1999.00, 18, 'ACTIVE', NOW(), NOW()),
(10, 2, 1024, 'Space Gray', 2499.00, 8, 'ACTIVE', NOW(), NOW());

-- Samsung Galaxy S24 variants (Storage x Color)
INSERT IGNORE INTO product_variants (id, product_id, size, color, price, stock_quantity, status, created_at, updated_at) VALUES
(11, 3, 256, 'Black', 899.00, 40, 'ACTIVE', NOW(), NOW()),
(12, 3, 512, 'Black', 1099.00, 25, 'ACTIVE', NOW(), NOW()),
(13, 3, 256, 'Violet', 899.00, 35, 'ACTIVE', NOW(), NOW()),
(14, 3, 512, 'Violet', 1099.00, 20, 'ACTIVE', NOW(), NOW());

-- Nike Air Max variants (Shoe Size x Color)
INSERT IGNORE INTO product_variants (id, product_id, size, color, price, stock_quantity, status, created_at, updated_at) VALUES
(15, 4, 40, 'Red', 150.00, 100, 'ACTIVE', NOW(), NOW()),
(16, 4, 41, 'Red', 150.00, 80, 'ACTIVE', NOW(), NOW()),
(17, 4, 42, 'Red', 150.00, 90, 'ACTIVE', NOW(), NOW()),
(18, 4, 43, 'Red', 150.00, 70, 'ACTIVE', NOW(), NOW()),
(19, 4, 40, 'Blue', 150.00, 95, 'ACTIVE', NOW(), NOW()),
(20, 4, 41, 'Blue', 150.00, 85, 'ACTIVE', NOW(), NOW()),
(21, 4, 42, 'Blue', 150.00, 88, 'ACTIVE', NOW(), NOW()),
(22, 4, 43, 'Blue', 150.00, 75, 'ACTIVE', NOW(), NOW());

-- Levi's Jeans variants (Waist Size x Color)
INSERT IGNORE INTO product_variants (id, product_id, size, color, price, stock_quantity, status, created_at, updated_at) VALUES
(23, 5, 30, 'Blue', 79.99, 120, 'ACTIVE', NOW(), NOW()),
(24, 5, 32, 'Blue', 79.99, 150, 'ACTIVE', NOW(), NOW()),
(25, 5, 34, 'Blue', 79.99, 140, 'ACTIVE', NOW(), NOW()),
(26, 5, 36, 'Blue', 79.99, 100, 'ACTIVE', NOW(), NOW()),
(27, 5, 30, 'Black', 79.99, 110, 'ACTIVE', NOW(), NOW()),
(28, 5, 32, 'Black', 79.99, 145, 'ACTIVE', NOW(), NOW()),
(29, 5, 34, 'Black', 79.99, 135, 'ACTIVE', NOW(), NOW()),
(30, 5, 36, 'Black', 79.99, 95, 'ACTIVE', NOW(), NOW());

-- The Great Gatsby variants (Book Format - no size needed)
INSERT IGNORE INTO product_variants (id, product_id, size, color, price, stock_quantity, status, created_at, updated_at) VALUES
(31, 6, NULL, 'Paperback', 12.99, 200, 'ACTIVE', NOW(), NOW()),
(32, 6, NULL, 'Hardcover', 24.99, 50, 'ACTIVE', NOW(), NOW()),
(33, 6, NULL, 'E-Book', 9.99, 9999, 'ACTIVE', NOW(), NOW());

-- Garden Tools Set variants (Quality level - no size)
INSERT IGNORE INTO product_variants (id, product_id, size, color, price, stock_quantity, status, created_at, updated_at) VALUES
(34, 7, NULL, 'Standard', 89.99, 60, 'ACTIVE', NOW(), NOW()),
(35, 7, NULL, 'Premium', 149.99, 30, 'ACTIVE', NOW(), NOW());

-- Cotton T-Shirt variants (Clothing Size as numbers: 1=S, 2=M, 3=L, 4=XL)
INSERT IGNORE INTO product_variants (id, product_id, size, color, price, stock_quantity, status, created_at, updated_at) VALUES
(36, 8, 1, 'Black', 29.99, 100, 'ACTIVE', NOW(), NOW()),
(37, 8, 2, 'Black', 29.99, 150, 'ACTIVE', NOW(), NOW()),
(38, 8, 3, 'Black', 29.99, 120, 'ACTIVE', NOW(), NOW()),
(39, 8, 4, 'Black', 29.99, 80, 'ACTIVE', NOW(), NOW()),
(40, 8, 1, 'White', 29.99, 95, 'ACTIVE', NOW(), NOW()),
(41, 8, 2, 'White', 29.99, 140, 'ACTIVE', NOW(), NOW()),
(42, 8, 3, 'White', 29.99, 110, 'ACTIVE', NOW(), NOW()),
(43, 8, 4, 'White', 29.99, 75, 'ACTIVE', NOW(), NOW());

-- ================================================
-- 6. SAMPLE CART (Optional - for testing)
-- ================================================

-- Uncomment to create a test cart with items
-- INSERT IGNORE INTO carts (id, cart_token, user_id, status, expires_at, created_at, updated_at) VALUES
-- (1, UUID(), 2, 'ACTIVE', DATE_ADD(NOW(), INTERVAL 24 HOUR), NOW(), NOW());

-- INSERT IGNORE INTO cart_items (id, cart_id, product_variant_id, quantity, created_at, updated_at) VALUES
-- (1, 1, 1, 2, NOW(), NOW()),  -- 2x iPhone 15 Pro 128GB Black
-- (2, 1, 15, 1, NOW(), NOW()); -- 1x Nike Air Max size 40 Red

-- ================================================
-- 7. SAMPLE ORDERS (For testing)
-- ================================================

-- Order 1: PENDING order - Clothing items (customer@example.com)
INSERT IGNORE INTO orders (id, order_code, user_id, status, payment_method, total_money, currency, shipping_address, tracking_token, created_at) VALUES
(1, 'ORD-2026012100001', 2, 'PENDING', 'COD', 309.98, 'VND', '123 Main Street, Hanoi, Vietnam, 10000', 'track-abc123def456', NOW());

INSERT IGNORE INTO order_items (id, order_id, sku_id, price_checkout, quantity) VALUES
(1, 1, 15, 150.00, 1),  -- 1x Nike Air Max size 40 Red
(2, 1, 23, 79.99, 2);   -- 2x Levi's Jeans size 30 Blue

-- Order 2: CONFIRMED order - Cotton T-Shirts (customer@example.com)
INSERT IGNORE INTO orders (id, order_code, user_id, status, payment_method, total_money, currency, shipping_address, tracking_token, created_at) VALUES
(2, 'ORD-2026012100002', 2, 'CONFIRMED', 'CARD', 119.96, 'VND', '123 Main Street, Hanoi, Vietnam, 10000', 'track-xyz789abc012', DATE_SUB(NOW(), INTERVAL 1 DAY));

INSERT IGNORE INTO order_items (id, order_id, sku_id, price_checkout, quantity) VALUES
(3, 2, 36, 29.99, 2),  -- 2x Cotton T-Shirt S Black
(4, 2, 40, 29.99, 2);  -- 2x Cotton T-Shirt S White

-- Order 3: SHIPPED order - Shoes and Jeans (customer@example.com)
INSERT IGNORE INTO orders (id, order_code, user_id, status, payment_method, total_money, currency, shipping_address, tracking_token, created_at) VALUES
(3, 'ORD-2026012000003', 2, 'SHIPPED', 'COD', 309.97, 'VND', '123 Main Street, Hanoi, Vietnam, 10000', 'track-ship456def789', DATE_SUB(NOW(), INTERVAL 2 DAY));

INSERT IGNORE INTO order_items (id, order_id, sku_id, price_checkout, quantity) VALUES
(5, 3, 19, 150.00, 1),  -- 1x Nike Air Max size 40 Blue
(6, 3, 24, 79.99, 2);   -- 2x Levi's Jeans size 32 Blue

-- Order 4: DELIVERED order - Complete outfit (customer@example.com)
INSERT IGNORE INTO orders (id, order_code, user_id, status, payment_method, total_money, currency, shipping_address, tracking_token, created_at) VALUES
(4, 'ORD-2026011900004', 2, 'DELIVERED', 'CARD', 259.97, 'VND', '123 Main Street, Hanoi, Vietnam, 10000', 'track-delivered123', DATE_SUB(NOW(), INTERVAL 5 DAY));

INSERT IGNORE INTO order_items (id, order_id, sku_id, price_checkout, quantity) VALUES
(7, 4, 37, 29.99, 2),  -- 2x Cotton T-Shirt M Black
(8, 4, 20, 150.00, 1),  -- 1x Nike Air Max size 41 Blue
(9, 4, 27, 79.99, 1);   -- 1x Levi's Jeans size 30 Black

-- Order 5: CONFIRMED order - Women's clothing (jane.smith@example.com)
INSERT IGNORE INTO orders (id, order_code, user_id, status, payment_method, total_money, currency, shipping_address, tracking_token, created_at) VALUES
(5, 'ORD-2026012100005', 3, 'CONFIRMED', 'COD', 389.95, 'VND', '789 Another Street, Ho Chi Minh City, Vietnam', 'track-jane123abc456', NOW());

INSERT IGNORE INTO order_items (id, order_id, sku_id, price_checkout, quantity) VALUES
(10, 5, 16, 150.00, 1),  -- 1x Nike Air Max size 41 Red
(11, 5, 25, 79.99, 1),   -- 1x Levi's Jeans size 34 Blue
(12, 5, 38, 29.99, 2),   -- 2x Cotton T-Shirt L Black
(13, 5, 42, 29.99, 2);   -- 2x Cotton T-Shirt L White

-- Order 6: PENDING order - Sports clothing (jane.smith@example.com)
INSERT IGNORE INTO orders (id, order_code, user_id, status, payment_method, total_money, currency, shipping_address, tracking_token, created_at) VALUES
(6, 'ORD-2026012000006', 3, 'PENDING', 'COD', 359.96, 'VND', '789 Another Street, Ho Chi Minh City, Vietnam', 'track-sports789xyz', DATE_SUB(NOW(), INTERVAL 1 DAY));

INSERT IGNORE INTO order_items (id, order_id, sku_id, price_checkout, quantity) VALUES
(14, 6, 21, 150.00, 2),  -- 2x Nike Air Max size 42 Blue
(15, 6, 39, 29.99, 2);   -- 2x Cotton T-Shirt XL Black

-- ================================================
COMMIT;

