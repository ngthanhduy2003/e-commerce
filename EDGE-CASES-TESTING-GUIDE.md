# 🧪 Edge Cases Testing Guide

## 📋 Mục lục

1. [Test "Hết hàng mà vẫn bán" (Out of Stock Prevention)](#1-test-hết-hàng-mà-vẫn-bán)
2. [Test Cart Reservation 15 phút](#2-test-cart-reservation-15-phút)
3. [Test Race Condition "Last Item"](#3-test-race-condition-last-item)
4. [Test Email Sending](#4-test-email-sending)
5. [Test Idempotency](#5-test-idempotency)
6. [Test Rollback khi Out of Stock](#6-test-rollback-khi-out-of-stock)
7. [Test Scheduler Cleanup](#7-test-scheduler-cleanup)

---

## 1. Test "Hết hàng mà vẫn bán"

### ❓ Vấn đề:
Product có stock = 5, nhưng 10 users cùng checkout → Bán quá số lượng

### ✅ Cơ chế bảo vệ:
```java
// ProductVariantRepository.java
@Modifying
@Query("UPDATE ProductVariant pv SET pv.stockQuantity = pv.stockQuantity - :quantity " +
       "WHERE pv.id = :id AND pv.stockQuantity >= :quantity")
int decrementStock(@Param("id") Long id, @Param("quantity") Integer quantity);
```

**Atomic operation:** Chỉ update nếu `stockQuantity >= quantity`

---

### 🧪 Test Case 1: Single User - Out of Stock

**Setup:**
```sql
-- Tạo product có stock thấp
UPDATE product_variants SET stock_quantity = 2 WHERE id = 1;
```

**Test Steps:**
```bash
# 1. Login
POST {{baseUrl}}/api/auth/login
Body: {"email": "customer@example.com", "password": "password123"}

# 2. Create Cart
POST {{baseUrl}}/api/carts

# 3. Try to add MORE than available stock
POST {{baseUrl}}/api/carts/{{cartToken}}/items
Body: {
  "skuId": 1,
  "quantity": 5  // ← Stock chỉ có 2!
}

# 4. Checkout
POST {{baseUrl}}/api/checkout
Body: {
  "cartToken": "{{cartToken}}",
  "paymentMethod": "COD",
  "shippingAddress": "123 Test St",
  "idempotencyKey": "{{$guid}}"
}
```

**Expected Result:**
```json
{
  "success": false,
  "message": "Insufficient stock for product variant: 1",
  "data": null
}
```
**HTTP Status:** `400 Bad Request`

---

### 🧪 Test Case 2: Multiple Users - Race Condition

**Setup:**
```sql
-- Stock = 5
UPDATE product_variants SET stock_quantity = 5 WHERE id = 1;
```

**Test với Postman Runner:**

1. **Tạo Collection:**
   - Create 10 requests giống nhau
   - Mỗi request mua 3 items (total = 30, nhưng stock = 5)

2. **Run Collection:**
   ```
   Postman → Collections → Run
   - Iterations: 10
   - Delay: 0ms (concurrent)
   ```

3. **Expected Results:**
   - ✅ **1-2 orders** thành công (stock = 5, mỗi order mua 3, còn dư 2 hoặc 5)
   - ❌ **8-9 orders** failed với message "Insufficient stock"
   - ✅ Final stock = 0 hoặc 2 (KHÔNG BAO GIỜ âm)

**Verify:**
```sql
SELECT stock_quantity FROM product_variants WHERE id = 1;
-- Must be >= 0
```

---

### 🧪 Test Case 3: Concurrent JMeter Test

**JMeter Setup:**
```xml
Thread Group:
  - Number of Threads: 50
  - Ramp-up Period: 1 second
  - Loop Count: 1

HTTP Request:
  POST /api/checkout
  Body: {"cartToken": "${cartToken}", ...}
```

**Result Analysis:**
```bash
# Check successful orders
SELECT COUNT(*) FROM orders WHERE created_at > NOW() - INTERVAL 1 MINUTE;

# Check total items sold
SELECT SUM(oi.quantity) 
FROM order_items oi 
JOIN orders o ON oi.order_id = o.id
WHERE o.created_at > NOW() - INTERVAL 1 MINUTE
  AND oi.sku_id = 1;

# Verify stock
SELECT stock_quantity FROM product_variants WHERE id = 1;
```

**Expected:**
- `Total sold` ≤ `Initial stock`
- `Final stock` = `Initial stock` - `Total sold`
- No negative stock

---

## 2. Test Cart Reservation 15 phút

### ❓ Vấn đề:
User thêm vào cart → Không checkout → Stock bị "hold" mãi mãi?

### ✅ Cơ chế:
```java
// ReservationService.java
LocalDateTime expiresAt = LocalDateTime.now()
    .plusMinutes(reservationConfig.getExpirationMinutes()); // 15 minutes
```

```java
// ReservationCleanupScheduler.java
@Scheduled(fixedRate = 300000) // Every 5 minutes
public void cleanupExpiredReservations() {
    reservationService.cleanupExpiredReservations();
}
```

---

### 🧪 Test Case 1: Check Reservation Expiry

**Test Steps:**
```bash
# 1. Create cart & add items
POST {{baseUrl}}/api/carts
POST {{baseUrl}}/api/carts/{{cartToken}}/items
Body: {"skuId": 1, "quantity": 2}

# 2. Check reservation in DB
SELECT * FROM stock_reservations 
WHERE cart_token = 'your-cart-token'
  AND status = 'RESERVED';
```

**Expected Result:**
```sql
id | sku_id | cart_token | quantity | status   | expires_at           | created_at
---|--------|------------|----------|----------|---------------------|------------
1  | 1      | abc-123    | 2        | RESERVED | 2026-01-21 09:22:00 | 2026-01-21 09:07:00
```
**Note:** `expires_at` = `created_at` + 15 minutes

---

### 🧪 Test Case 2: Manual Cleanup Test

**Option 1: Change config (Fast testing)**
```yaml
# application.yaml
reservation:
  expiration-minutes: 1  # ← Change to 1 minute for testing
  cleanup-interval-minutes: 1  # ← Cleanup every 1 minute
```

**Test:**
```bash
# 1. Create reservation
POST {{baseUrl}}/api/carts/{{cartToken}}/items

# 2. Wait 2 minutes

# 3. Check reservation status
SELECT * FROM stock_reservations WHERE cart_token = 'your-token';
```

**Expected:** Status changed from `RESERVED` → `RELEASED`

---

**Option 2: Manual time manipulation (Advanced)**
```sql
-- Manually set expiry time to past
UPDATE stock_reservations 
SET expires_at = NOW() - INTERVAL 10 MINUTE
WHERE cart_token = 'your-cart-token';

-- Trigger cleanup manually (call scheduler)
-- Or wait for next scheduled run (every 5 minutes)
```

**Verify:**
```sql
SELECT status, expires_at 
FROM stock_reservations 
WHERE cart_token = 'your-cart-token';
-- Status should be 'RELEASED'
```

---

### 🧪 Test Case 3: Redis TTL Check

```bash
# Redis CLI
redis-cli

# Check reservation key
GET reservation:your-cart-token:1

# Check TTL (Time To Live)
TTL reservation:your-cart-token:1
# Should return ~900 seconds (15 minutes)

# Wait 16 minutes
TTL reservation:your-cart-token:1
# Should return -2 (key expired and deleted)
```

---

## 3. Test Race Condition "Last Item"

### ❓ Vấn đề:
Stock còn 1 item, 2 users cùng checkout → Ai được mua?

### ✅ Cơ chế:
```java
// Pessimistic Locking + Atomic Update
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT pv FROM ProductVariant pv WHERE pv.id = :id")
Optional<ProductVariant> findByIdForUpdate(@Param("id") Long id);

// Atomic decrement with WHERE clause
@Query("UPDATE ProductVariant pv SET pv.stockQuantity = pv.stockQuantity - :quantity " +
       "WHERE pv.id = :id AND pv.stockQuantity >= :quantity")
int decrementStock(@Param("id") Long id, @Param("quantity") Integer quantity);
```

---

### 🧪 Test Case 1: 2 Users, 1 Item Left

**Setup:**
```sql
UPDATE product_variants SET stock_quantity = 1 WHERE id = 1;
```

**Test với 2 Postman tabs:**

**Tab 1 (User A):**
```bash
POST {{baseUrl}}/api/auth/login
Body: {"email": "customer@example.com", "password": "password123"}

POST {{baseUrl}}/api/carts
POST {{baseUrl}}/api/carts/{{cartToken}}/items
Body: {"skuId": 1, "quantity": 1}

# Click Send EXACTLY at the same time as User B
POST {{baseUrl}}/api/checkout
```

**Tab 2 (User B):**
```bash
POST {{baseUrl}}/api/auth/login
Body: {"email": "jane.smith@example.com", "password": "password123"}

POST {{baseUrl}}/api/carts
POST {{baseUrl}}/api/carts/{{cartToken}}/items
Body: {"skuId": 1, "quantity": 1}

# Click Send EXACTLY at the same time as User A
POST {{baseUrl}}/api/checkout
```

**Expected Results:**
- ✅ **User A (hoặc B):** `201 Created` - Order thành công
- ❌ **User B (hoặc A):** `400 Bad Request` - "Insufficient stock"

**Verify:**
```sql
-- Check stock
SELECT stock_quantity FROM product_variants WHERE id = 1;
-- Result: 0

-- Check orders
SELECT COUNT(*) FROM orders WHERE created_at > NOW() - INTERVAL 1 MINUTE;
-- Result: 1 (chỉ 1 order thành công)

-- Check order items
SELECT SUM(quantity) FROM order_items oi
JOIN orders o ON oi.order_id = o.id
WHERE o.created_at > NOW() - INTERVAL 1 MINUTE
  AND oi.sku_id = 1;
-- Result: 1 (chỉ bán 1 item)
```

---

### 🧪 Test Case 2: 10 Users, 3 Items Left

**Setup:**
```sql
UPDATE product_variants SET stock_quantity = 3 WHERE id = 1;
```

**Postman Collection Runner:**
```javascript
// Pre-request Script (Collection level)
pm.environment.set("uniqueEmail", "user" + Date.now() + "@test.com");
pm.environment.set("cartToken", "");

// Test 1: Register
POST /api/auth/register
Body: {"email": "{{uniqueEmail}}", "password": "test123", "fullName": "Test User"}

// Test 2: Login
POST /api/auth/login

// Test 3: Create Cart
POST /api/carts

// Test 4: Add Item (quantity=1)
POST /api/carts/{{cartToken}}/items
Body: {"skuId": 1, "quantity": 1}

// Test 5: Checkout
POST /api/checkout
```

**Run:**
- Iterations: 10
- Delay: 0ms

**Expected:**
- ✅ **3 orders** thành công
- ❌ **7 orders** failed (insufficient stock)
- ✅ Final stock = 0

---

### 🧪 Test Case 3: JMeter Stress Test

**JMeter Config:**
```xml
Thread Group:
  - Threads: 100
  - Ramp-up: 1 second
  - Duration: 10 seconds

HTTP Request Sampler:
  POST /api/checkout
  
Assertions:
  - Response Code: 201 OR 400 (not 500!)
  - Stock never negative
```

**Post-test Verification:**
```sql
-- Check stock integrity
SELECT 
  pv.id,
  pv.stock_quantity AS current_stock,
  (SELECT SUM(oi.quantity) 
   FROM order_items oi 
   WHERE oi.sku_id = pv.id) AS total_sold,
  100 AS initial_stock,  -- Your initial value
  (100 - (SELECT SUM(oi.quantity) FROM order_items oi WHERE oi.sku_id = pv.id)) AS expected_stock
FROM product_variants pv
WHERE pv.id = 1;
```

**Expected:**
- `current_stock` = `expected_stock`
- `current_stock` ≥ 0
- No 500 Internal Server Error

---

## 4. Test Email Sending

### ✅ Cơ chế:
```java
@Async
public void sendOrderConfirmation(Order order) {
    // Async email sending
}
```

---

### 🧪 Test Case 1: Check Email Configuration

**application.yaml:**
```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: your-email@gmail.com
    password: your-app-password
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
```

**Test:**
```bash
# Checkout order
POST {{baseUrl}}/api/checkout

# Check application logs
grep "Order confirmation email sent" logs/application.log
```

**Expected Log:**
```
2026-01-21 09:15:32 INFO  EmailService - Order confirmation email sent for order: ORD-2026012100001
```

---

### 🧪 Test Case 2: Fake SMTP Server (MailHog)

**Setup MailHog:**
```bash
# Windows (Chocolatey)
choco install mailhog

# Start MailHog
mailhog

# Web UI: http://localhost:8025
# SMTP Server: localhost:1025
```

**application-dev.yaml:**
```yaml
spring:
  mail:
    host: localhost
    port: 1025
    username: 
    password:
```

**Test:**
1. Checkout order
2. Open http://localhost:8025
3. Check inbox

**Expected:**
- Email appears in MailHog
- Subject: "Order Confirmation - ORD-2026012100001"
- Body contains order details

---

### 🧪 Test Case 3: Email Failure Handling

**Simulate failure:**
```yaml
spring:
  mail:
    host: invalid-smtp-server.com  # ← Wrong host
```

**Test:**
```bash
POST {{baseUrl}}/api/checkout
```

**Expected:**
- ✅ **Order STILL created** (email failure doesn't block checkout)
- ❌ **Email not sent**
- ✅ **Log error:** "Failed to send order confirmation email"

**Verify:**
```sql
SELECT * FROM orders WHERE order_code = 'ORD-2026012100001';
-- Order exists despite email failure
```

---

## 5. Test Idempotency

### ❓ Vấn đề:
User click "Checkout" 2 lần nhanh → Tạo 2 orders?

### ✅ Cơ chế:
```java
private static final String IDEMPOTENCY_KEY_PREFIX = "checkout:idempotency:";

String idempotencyKey = IDEMPOTENCY_KEY_PREFIX + request.getIdempotencyKey();
String existingOrderCode = (String) redisTemplate.opsForValue().get(idempotencyKey);

if (existingOrderCode != null) {
    log.info("Idempotent checkout detected, returning existing order: {}", existingOrderCode);
    Order existingOrder = orderRepository.findByOrderCode(existingOrderCode)
        .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
    return toOrderResponse(existingOrder);
}
```

---

### 🧪 Test Case 1: Same Idempotency Key

**Test:**
```bash
# 1st request
POST {{baseUrl}}/api/checkout
Body: {
  "cartToken": "abc-123",
  "paymentMethod": "COD",
  "shippingAddress": "123 Test St",
  "idempotencyKey": "SAME-KEY-12345"  // ← Fixed key
}

# 2nd request (SAME idempotency key)
POST {{baseUrl}}/api/checkout
Body: {
  "cartToken": "abc-123",
  "paymentMethod": "COD",
  "shippingAddress": "123 Test St",
  "idempotencyKey": "SAME-KEY-12345"  // ← Same key!
}
```

**Expected:**
- ✅ **1st request:** `201 Created` - Order created
- ✅ **2nd request:** `200 OK` - Returns SAME order (not creating new)
- ✅ **Same order_code** in both responses

**Verify:**
```sql
SELECT COUNT(*) FROM orders 
WHERE user_id = 2 
  AND created_at > NOW() - INTERVAL 1 MINUTE;
-- Result: 1 (not 2!)
```

---

### 🧪 Test Case 2: Different Idempotency Keys

**Test:**
```bash
# 1st request
POST {{baseUrl}}/api/checkout
Body: {
  "idempotencyKey": "KEY-001"
}

# 2nd request (Different key)
POST {{baseUrl}}/api/checkout
Body: {
  "idempotencyKey": "KEY-002"  // ← Different!
}
```

**Expected:**
- ✅ **Both requests:** `201 Created`
- ✅ **2 different orders** created

---

### 🧪 Test Case 3: Redis Expiry Check

**Test:**
```bash
# Checkout
POST {{baseUrl}}/api/checkout
Body: {"idempotencyKey": "TEST-KEY-123"}

# Check Redis
redis-cli GET "checkout:idempotency:TEST-KEY-123"
# Result: "ORD-2026012100001"

# Check TTL
redis-cli TTL "checkout:idempotency:TEST-KEY-123"
# Result: ~86400 (24 hours in seconds)
```

**After 24 hours:**
```bash
redis-cli GET "checkout:idempotency:TEST-KEY-123"
# Result: (nil) - Key expired
```

---

## 6. Test Rollback khi Out of Stock

### ❓ Vấn đề:
Cart có 3 items: Item A, B, C. Item B out of stock → Phải rollback Item A đã giảm stock!

### ✅ Cơ chế:
```java
Map<Long, Integer> stockUpdates = new HashMap<>();

for (CartItem item : cart.getItems()) {
    int affectedRows = variantRepository.decrementStock(
        item.getSku().getId(),
        item.getQuantity()
    );

    if (affectedRows == 0) {
        // Rollback: restore previously decremented stock
        for (Map.Entry<Long, Integer> entry : stockUpdates.entrySet()) {
            variantRepository.incrementStock(entry.getKey(), entry.getValue());
        }
        throw new OutOfStockException("Insufficient stock...");
    }
    
    stockUpdates.put(item.getSku().getId(), item.getQuantity());
}
```

---

### 🧪 Test Case: Partial Out of Stock

**Setup:**
```sql
-- Item A: Stock = 10 (enough)
UPDATE product_variants SET stock_quantity = 10 WHERE id = 1;

-- Item B: Stock = 0 (out of stock!)
UPDATE product_variants SET stock_quantity = 0 WHERE id = 2;

-- Item C: Stock = 20 (enough)
UPDATE product_variants SET stock_quantity = 20 WHERE id = 3;
```

**Test:**
```bash
# Add all 3 items to cart
POST {{baseUrl}}/api/carts/{{cartToken}}/items
Body: {"skuId": 1, "quantity": 2}

POST {{baseUrl}}/api/carts/{{cartToken}}/items
Body: {"skuId": 2, "quantity": 1}  // ← This will fail!

POST {{baseUrl}}/api/carts/{{cartToken}}/items
Body: {"skuId": 3, "quantity": 1}

# Checkout
POST {{baseUrl}}/api/checkout
```

**Expected:**
- ❌ **Checkout fails:** "Insufficient stock for product variant: 2"
- ✅ **Item A stock:** Still 10 (NOT 8!) - Rollback successful
- ✅ **Item B stock:** Still 0
- ✅ **Item C stock:** Still 20 (never touched)
- ✅ **No order created**

**Verify:**
```sql
SELECT id, stock_quantity FROM product_variants WHERE id IN (1, 2, 3);
-- Expected:
-- 1 | 10  (unchanged - rollback worked!)
-- 2 | 0   (unchanged - was out of stock)
-- 3 | 20  (unchanged - never decremented)
```

---

## 7. Test Scheduler Cleanup

### ✅ Cơ chế:
```java
@Scheduled(fixedRate = 300000) // Every 5 minutes
public void cleanupExpiredReservations() {
    reservationService.cleanupExpiredReservations();
}
```

---

### 🧪 Test Case 1: Scheduler Execution

**Enable debug logging:**
```yaml
# application.yaml
logging:
  level:
    Ecommerce.scheduler: DEBUG
```

**Test:**
1. Start application
2. Wait 5 minutes
3. Check logs

**Expected Logs:**
```
2026-01-21 09:05:00 INFO  ReservationCleanupScheduler - Starting cleanup of expired reservations...
2026-01-21 09:05:00 INFO  ReservationService - Cleaned up 3 expired reservations
2026-01-21 09:10:00 INFO  ReservationCleanupScheduler - Starting cleanup of expired reservations...
```

---

### 🧪 Test Case 2: Manual Trigger (Testing)

**Create REST endpoint for testing:**
```java
// For testing only - remove in production!
@RestController
@RequestMapping("/api/admin/test")
public class TestController {
    
    private final ReservationService reservationService;
    
    @PostMapping("/cleanup-reservations")
    public ResponseEntity<?> triggerCleanup() {
        reservationService.cleanupExpiredReservations();
        return ResponseEntity.ok("Cleanup triggered");
    }
}
```

**Test:**
```bash
# Create expired reservation
UPDATE stock_reservations 
SET expires_at = NOW() - INTERVAL 1 MINUTE
WHERE id = 1;

# Trigger cleanup
POST {{baseUrl}}/api/admin/test/cleanup-reservations

# Check status
SELECT status FROM stock_reservations WHERE id = 1;
-- Expected: 'RELEASED'
```

---

### 🧪 Test Case 3: Verify Cleanup Results

**Before cleanup:**
```sql
SELECT COUNT(*) FROM stock_reservations 
WHERE status = 'RESERVED' 
  AND expires_at < NOW();
-- Result: 5 (expired reservations)
```

**After scheduler runs:**
```sql
SELECT COUNT(*) FROM stock_reservations 
WHERE status = 'RESERVED' 
  AND expires_at < NOW();
-- Result: 0 (all cleaned up)

SELECT COUNT(*) FROM stock_reservations 
WHERE status = 'RELEASED'
  AND updated_at > NOW() - INTERVAL 10 MINUTE;
-- Result: 5 (same reservations, now RELEASED)
```

---

## 📊 Summary Table

| Test Case | What to Test | How to Verify | Expected Result |
|-----------|--------------|---------------|-----------------|
| **Out of Stock** | Buy more than available | Check stock in DB | Order fails, stock unchanged |
| **Race Condition** | 100 users, 5 items | JMeter + SQL query | Only 5 items sold, stock = 0 |
| **Reservation Expiry** | Wait 15+ minutes | Check reservation status | Status = RELEASED |
| **Redis TTL** | Check TTL after 16 mins | `redis-cli TTL key` | Key deleted (-2) |
| **Email Sending** | Checkout order | Check MailHog/logs | Email received |
| **Email Failure** | Wrong SMTP config | Check order exists | Order created despite email fail |
| **Idempotency** | Same key twice | Check order count | Only 1 order created |
| **Rollback** | Item B out of stock | Check Item A stock | Item A stock restored |
| **Scheduler** | Wait 5 minutes | Check logs | Cleanup executed |

---

## 🚀 Quick Test Script

```bash
# 1. Setup test data
mysql -u root -p ecommerce < test-data.sql

# 2. Start application
./gradlew bootRun

# 3. Run Postman collection
newman run postman-collection.json \
  --environment postman-environment.json \
  --iteration-count 10 \
  --delay-request 0

# 4. Verify results
mysql -u root -p ecommerce < verify-stock.sql

# 5. Check logs
tail -f logs/application.log | grep -E "stock|reservation|email"
```

---

## 🛠️ Tools Required

- ✅ **Postman** - API testing
- ✅ **Postman Runner** - Concurrent requests
- ✅ **JMeter** (Optional) - Load testing
- ✅ **MailHog** - Email testing
- ✅ **Redis CLI** - Check Redis keys
- ✅ **MySQL Client** - Verify DB state

---

## 📝 Test Checklist

- [ ] Out of stock prevention works
- [ ] Race condition handled (no overselling)
- [ ] Reservations expire after 15 minutes
- [ ] Redis TTL working correctly
- [ ] Scheduler runs every 5 minutes
- [ ] Email sent successfully
- [ ] Email failure doesn't block checkout
- [ ] Idempotency prevents duplicate orders
- [ ] Rollback works when partial out of stock
- [ ] No negative stock in any scenario

---

**Happy Testing! 🎉**

