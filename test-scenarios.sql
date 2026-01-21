-- ================================================
-- TEST SCENARIOS SQL SCRIPTS
-- ================================================
-- Use these scripts to setup specific test scenarios
-- Run BEFORE testing edge cases
-- ================================================

-- ================================================
-- SCENARIO 1: Out of Stock Test
-- ================================================
-- Setup: Product có stock thấp để test out of stock

-- Reset stock về giá trị cao
UPDATE product_variants SET stock_quantity = 100 WHERE id IN (1, 2, 3);

-- Set stock thấp cho test
UPDATE product_variants SET stock_quantity = 2 WHERE id = 1;
UPDATE product_variants SET stock_quantity = 0 WHERE id = 2;
UPDATE product_variants SET stock_quantity = 5 WHERE id = 3;

SELECT id, stock_quantity, price,
       CONCAT(size, 'GB ', color) as variant
FROM product_variants
WHERE id IN (1, 2, 3);

-- ================================================
-- SCENARIO 2: Race Condition Test (Last Item)
-- ================================================
-- Setup: Chỉ còn 1-3 items để test concurrent checkout

UPDATE product_variants SET stock_quantity = 1 WHERE id = 15;  -- Nike Air Max Red size 40
UPDATE product_variants SET stock_quantity = 2 WHERE id = 16;  -- Nike Air Max Red size 41
UPDATE product_variants SET stock_quantity = 3 WHERE id = 17;  -- Nike Air Max Red size 42

SELECT id, stock_quantity, price,
       CONCAT('Nike Air Max size ', size, ' ', color) as variant
FROM product_variants
WHERE id IN (15, 16, 17);

-- ================================================
-- SCENARIO 3: Reservation Expiry Test
-- ================================================
-- Create expired reservations manually

-- Insert expired reservation (expires 10 minutes ago)
INSERT INTO stock_reservations (sku_id, cart_token, quantity, status, expires_at, created_at)
VALUES
(1, 'test-cart-expired-1', 2, 'RESERVED', DATE_SUB(NOW(), INTERVAL 10 MINUTE), DATE_SUB(NOW(), INTERVAL 25 MINUTE)),
(2, 'test-cart-expired-2', 1, 'RESERVED', DATE_SUB(NOW(), INTERVAL 5 MINUTE), DATE_SUB(NOW(), INTERVAL 20 MINUTE)),
(3, 'test-cart-expired-3', 3, 'RESERVED', DATE_SUB(NOW(), INTERVAL 1 MINUTE), DATE_SUB(NOW(), INTERVAL 16 MINUTE));

-- Insert active reservation (expires in 10 minutes)
INSERT INTO stock_reservations (sku_id, cart_token, quantity, status, expires_at, created_at)
VALUES
(4, 'test-cart-active-1', 1, 'RESERVED', DATE_ADD(NOW(), INTERVAL 10 MINUTE), NOW()),
(5, 'test-cart-active-2', 2, 'RESERVED', DATE_ADD(NOW(), INTERVAL 14 MINUTE), NOW());

-- Check reservations
SELECT
    id,
    sku_id,
    cart_token,
    quantity,
    status,
    expires_at,
    CASE
        WHEN expires_at < NOW() THEN 'EXPIRED'
        ELSE 'ACTIVE'
    END as is_expired,
    TIMESTAMPDIFF(MINUTE, NOW(), expires_at) as minutes_until_expiry
FROM stock_reservations
WHERE status = 'RESERVED'
ORDER BY expires_at;

-- ================================================
-- SCENARIO 4: Partial Checkout Rollback Test
-- ================================================
-- Setup: Mix of in-stock and out-of-stock items

UPDATE product_variants SET stock_quantity = 10 WHERE id = 23;  -- Levi's Jeans 30 Blue (in stock)
UPDATE product_variants SET stock_quantity = 0 WHERE id = 24;   -- Levi's Jeans 32 Blue (OUT of stock)
UPDATE product_variants SET stock_quantity = 20 WHERE id = 25;  -- Levi's Jeans 34 Blue (in stock)

SELECT id, stock_quantity, price,
       CONCAT('Levis Jeans size ', size, ' ', color) as variant,
       CASE WHEN stock_quantity > 0 THEN 'IN STOCK' ELSE 'OUT OF STOCK' END as status
FROM product_variants
WHERE id IN (23, 24, 25);

-- Expected: When checkout với cả 3 items, sẽ fail vì item 24 out of stock
-- Stock của item 23 và 25 phải KHÔNG thay đổi (rollback)

-- ================================================
-- SCENARIO 5: Concurrent Checkout Test
-- ================================================
-- Setup: Limited stock cho multiple users test

UPDATE product_variants SET stock_quantity = 10 WHERE id = 36;  -- T-Shirt S Black
UPDATE product_variants SET stock_quantity = 15 WHERE id = 37;  -- T-Shirt M Black
UPDATE product_variants SET stock_quantity = 20 WHERE id = 38;  -- T-Shirt L Black

SELECT id, stock_quantity, price,
       CONCAT('T-Shirt size ',
              CASE size
                  WHEN 1 THEN 'S'
                  WHEN 2 THEN 'M'
                  WHEN 3 THEN 'L'
                  WHEN 4 THEN 'XL'
              END, ' ', color) as variant
FROM product_variants
WHERE id IN (36, 37, 38);

-- Run 50 concurrent checkouts (mỗi checkout mua 1 item)
-- Expected: Chỉ 10 orders thành công cho SKU 36, 15 cho SKU 37, 20 cho SKU 38

-- ================================================
-- VERIFICATION QUERIES
-- ================================================

-- 1. Check stock integrity
SELECT
    pv.id,
    pv.stock_quantity AS current_stock,
    COALESCE(SUM(oi.quantity), 0) AS total_sold,
    p.name as product_name
FROM product_variants pv
LEFT JOIN order_items oi ON oi.sku_id = pv.id
LEFT JOIN orders o ON o.id = oi.order_id AND o.created_at > DATE_SUB(NOW(), INTERVAL 1 HOUR)
JOIN products p ON p.id = pv.product_id
WHERE pv.id IN (1, 2, 3, 15, 16, 17, 23, 24, 25, 36, 37, 38)
GROUP BY pv.id, pv.stock_quantity, p.name
ORDER BY pv.id;

-- 2. Check reservations status
SELECT
    COUNT(*) as total_reservations,
    SUM(CASE WHEN status = 'RESERVED' THEN 1 ELSE 0 END) as reserved_count,
    SUM(CASE WHEN status = 'RELEASED' THEN 1 ELSE 0 END) as released_count,
    SUM(CASE WHEN status = 'CONSUMED' THEN 1 ELSE 0 END) as consumed_count,
    SUM(CASE WHEN status = 'RESERVED' AND expires_at < NOW() THEN 1 ELSE 0 END) as expired_count
FROM stock_reservations;

-- 3. Check recent orders
SELECT
    o.id,
    o.order_code,
    o.status,
    o.total_money,
    COUNT(oi.id) as item_count,
    o.created_at
FROM orders o
LEFT JOIN order_items oi ON oi.order_id = o.id
WHERE o.created_at > DATE_SUB(NOW(), INTERVAL 1 HOUR)
GROUP BY o.id, o.order_code, o.status, o.total_money, o.created_at
ORDER BY o.created_at DESC;

-- 4. Check orders by user
SELECT
    u.email,
    COUNT(o.id) as order_count,
    SUM(o.total_money) as total_spent
FROM users u
LEFT JOIN orders o ON o.user_id = u.id AND o.created_at > DATE_SUB(NOW(), INTERVAL 1 HOUR)
WHERE u.id IN (2, 3)  -- customer@example.com, jane.smith@example.com
GROUP BY u.email;

-- 5. Find negative stock (SHOULD BE EMPTY!)
SELECT id, stock_quantity, product_id
FROM product_variants
WHERE stock_quantity < 0;

-- ================================================
-- CLEANUP SCRIPTS
-- ================================================

-- Reset all test data
DELETE FROM stock_reservations WHERE cart_token LIKE 'test-cart-%';

-- Reset stock to original values
UPDATE product_variants SET stock_quantity = 50 WHERE id = 1;
UPDATE product_variants SET stock_quantity = 30 WHERE id = 2;
UPDATE product_variants SET stock_quantity = 20 WHERE id = 3;
UPDATE product_variants SET stock_quantity = 100 WHERE id IN (15, 16, 17);
UPDATE product_variants SET stock_quantity = 120 WHERE id = 23;
UPDATE product_variants SET stock_quantity = 150 WHERE id = 24;
UPDATE product_variants SET stock_quantity = 140 WHERE id = 25;
UPDATE product_variants SET stock_quantity = 100 WHERE id = 36;
UPDATE product_variants SET stock_quantity = 150 WHERE id = 37;
UPDATE product_variants SET stock_quantity = 120 WHERE id = 38;

-- Delete test orders (optional)
-- DELETE FROM order_items WHERE order_id IN (SELECT id FROM orders WHERE created_at > DATE_SUB(NOW(), INTERVAL 1 HOUR));
-- DELETE FROM orders WHERE created_at > DATE_SUB(NOW(), INTERVAL 1 HOUR);

-- ================================================
-- MONITORING QUERIES (Run during test)
-- ================================================

-- Real-time stock monitoring
SELECT
    pv.id,
    p.name,
    CONCAT(pv.size, ' ', pv.color) as variant,
    pv.stock_quantity,
    pv.updated_at
FROM product_variants pv
JOIN products p ON p.id = pv.product_id
WHERE pv.id IN (1, 2, 3, 15, 16, 17)
ORDER BY pv.updated_at DESC;

-- Real-time order monitoring
SELECT
    o.order_code,
    u.email,
    o.status,
    o.total_money,
    o.created_at,
    COUNT(oi.id) as items_count
FROM orders o
JOIN users u ON u.id = o.user_id
LEFT JOIN order_items oi ON oi.order_id = o.id
WHERE o.created_at > DATE_SUB(NOW(), INTERVAL 10 MINUTE)
GROUP BY o.id, o.order_code, u.email, o.status, o.total_money, o.created_at
ORDER BY o.created_at DESC
LIMIT 20;

-- Real-time reservation monitoring
SELECT
    sr.id,
    sr.cart_token,
    sr.sku_id,
    sr.quantity,
    sr.status,
    sr.expires_at,
    TIMESTAMPDIFF(SECOND, NOW(), sr.expires_at) as seconds_until_expiry,
    CASE
        WHEN sr.expires_at < NOW() THEN '⚠️ EXPIRED'
        WHEN TIMESTAMPDIFF(MINUTE, NOW(), sr.expires_at) < 5 THEN '⚡ EXPIRING SOON'
        ELSE '✅ ACTIVE'
    END as expiry_status
FROM stock_reservations sr
WHERE sr.status = 'RESERVED'
ORDER BY sr.expires_at;

-- ================================================
-- STRESS TEST SETUP
-- ================================================

-- Create 10 test users for concurrent testing
INSERT IGNORE INTO users (email, password_hash, full_name, role_id, status, created_at, updated_at)
SELECT
    CONCAT('testuser', n, '@test.com'),
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH',  -- password: password123
    CONCAT('Test User ', n),
    1,  -- USER role
    'ACTIVE',
    NOW(),
    NOW()
FROM (
    SELECT 1 AS n UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5
    UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10
) AS numbers;

-- Verify test users
SELECT id, email, full_name FROM users WHERE email LIKE 'testuser%@test.com';

-- ================================================
-- POST-TEST ANALYSIS
-- ================================================

-- Analysis: Stock changes over time
SELECT
    pv.id,
    p.name,
    100 AS initial_stock,  -- Adjust based on your setup
    pv.stock_quantity AS final_stock,
    (100 - pv.stock_quantity) AS items_sold,
    COUNT(DISTINCT oi.order_id) AS orders_count
FROM product_variants pv
JOIN products p ON p.id = pv.product_id
LEFT JOIN order_items oi ON oi.sku_id = pv.id
LEFT JOIN orders o ON o.id = oi.order_id AND o.created_at > DATE_SUB(NOW(), INTERVAL 1 HOUR)
WHERE pv.id IN (1, 2, 3, 15, 16, 17)
GROUP BY pv.id, p.name, pv.stock_quantity;

-- Analysis: Success rate
SELECT
    COUNT(*) AS total_orders,
    SUM(CASE WHEN status = 'PENDING' THEN 1 ELSE 0 END) AS pending_orders,
    SUM(CASE WHEN status = 'CONFIRMED' THEN 1 ELSE 0 END) AS confirmed_orders,
    SUM(CASE WHEN status = 'CANCELLED' THEN 1 ELSE 0 END) AS cancelled_orders,
    ROUND(SUM(CASE WHEN status != 'CANCELLED' THEN 1 ELSE 0 END) * 100.0 / COUNT(*), 2) AS success_rate
FROM orders
WHERE created_at > DATE_SUB(NOW(), INTERVAL 1 HOUR);

-- ================================================
-- END OF TEST SCRIPTS
-- ================================================

COMMIT;

