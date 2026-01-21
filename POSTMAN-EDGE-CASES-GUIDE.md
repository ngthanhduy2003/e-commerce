# 🧪 Postman Edge Cases Testing Collection

## 📋 Hướng dẫn Import & Setup

### 1. Tạo Collection mới trong Postman

**Collection Name:** `E-Commerce - Edge Cases Testing`

### 2. Setup Collection Variables

```javascript
// Variables tab
baseUrl: http://localhost:8080
accessToken: (auto-saved from login)
cartToken: (auto-saved from create cart)
orderId: (auto-saved from checkout)
testSkuId: 1
```

### 3. Setup Collection Authorization

```
Type: Bearer Token
Token: {{accessToken}}
```

---

## 📁 Folder Structure

```
E-Commerce - Edge Cases Testing
├── 1. Setup
│   ├── Login Customer 1
│   ├── Login Customer 2
│   └── Get Test System Info
├── 2. Out of Stock Tests
│   ├── Create Cart
│   ├── Add Out of Stock Item
│   ├── Checkout (Should Fail)
│   └── Verify Stock Unchanged
├── 3. Race Condition Tests
│   ├── Concurrent Setup (Run 10x)
│   └── Verify Only N Orders Created
├── 4. Reservation Expiry Tests
│   ├── Create Cart with Items
│   ├── Check Reservation Status
│   ├── Trigger Manual Cleanup
│   └── Verify Reservation Released
├── 5. Idempotency Tests
│   ├── Checkout with Fixed Key
│   ├── Checkout with Same Key (Should Return Same Order)
│   └── Verify Only One Order
└── 6. Rollback Tests
    ├── Setup Mixed Stock Cart
    ├── Checkout (Should Fail)
    └── Verify All Stock Rolled Back
```

---

## 🔧 Detailed Request Setup

### **Folder 1: Setup**

#### **Request: Login Customer 1**
```
Method: POST
URL: {{baseUrl}}/api/auth/login

Body (raw JSON):
{
  "email": "customer@example.com",
  "password": "password123"
}

Tests Script:
pm.test("Login successful", function () {
    pm.response.to.have.status(200);
    var jsonData = pm.response.json();
    pm.collectionVariables.set("accessToken", jsonData.data.accessToken);
});
```

#### **Request: Get Test System Info**
```
Method: GET
URL: {{baseUrl}}/api/test/system-info

Tests Script:
pm.test("System info retrieved", function () {
    pm.response.to.have.status(200);
});
```

---

### **Folder 2: Out of Stock Tests**

#### **Pre-requisite:**
Run SQL: `UPDATE product_variants SET stock_quantity = 2 WHERE id = 1;`

#### **Request: Add Out of Stock Item**
```
Method: POST
URL: {{baseUrl}}/api/carts/{{cartToken}}/items

Body (raw JSON):
{
  "skuId": 1,
  "quantity": 5
}

Tests Script:
pm.test("Should fail - insufficient stock", function () {
    pm.response.to.have.status(400);
    var jsonData = pm.response.json();
    pm.expect(jsonData.success).to.be.false;
});
```

#### **Request: Checkout (Should Fail)**
```
Method: POST
URL: {{baseUrl}}/api/checkout

Body (raw JSON):
{
  "cartToken": "{{cartToken}}",
  "paymentMethod": "COD",
  "shippingAddress": "123 Test Street",
  "idempotencyKey": "{{$guid}}"
}

Tests Script:
pm.test("Checkout failed - out of stock", function () {
    pm.response.to.have.status(400);
    var jsonData = pm.response.json();
    pm.expect(jsonData.message).to.include("Insufficient stock");
});
```

---

### **Folder 3: Race Condition Tests**

#### **Pre-requisite:**
Run SQL: `UPDATE product_variants SET stock_quantity = 5 WHERE id = 15;`

#### **Request: Concurrent Setup**
```
Method: POST
URL: {{baseUrl}}/api/checkout

Pre-request Script:
// Create unique cart and add item
pm.sendRequest({
    url: pm.collectionVariables.get("baseUrl") + "/api/carts",
    method: "POST",
    header: {
        "Authorization": "Bearer " + pm.collectionVariables.get("accessToken")
    }
}, function (err, response) {
    var cartData = response.json();
    var cartToken = cartData.data.cartToken;
    
    pm.sendRequest({
        url: pm.collectionVariables.get("baseUrl") + "/api/carts/" + cartToken + "/items",
        method: "POST",
        header: {
            "Authorization": "Bearer " + pm.collectionVariables.get("accessToken"),
            "Content-Type": "application/json"
        },
        body: {
            mode: "raw",
            raw: JSON.stringify({
                "skuId": 15,
                "quantity": 1
            })
        }
    }, function (err, response) {
        pm.collectionVariables.set("cartToken", cartToken);
    });
});

Body (raw JSON):
{
  "cartToken": "{{cartToken}}",
  "paymentMethod": "COD",
  "shippingAddress": "Race Test Address",
  "idempotencyKey": "{{$guid}}"
}

Tests Script:
pm.test("Response is 201 or 400", function () {
    pm.expect([201, 400]).to.include(pm.response.code);
});

// Log result for analysis
console.log("Checkout result: " + pm.response.code + " - " + pm.response.json().message);
```

**How to run:**
1. Select this request
2. Click "Run" → "Run manually"
3. Set Iterations: 10
4. Set Delay: 0ms
5. Click "Run E-Commerce..."

**Expected Result:**
- ~5 requests: `201 Created`
- ~5 requests: `400 Bad Request` (Insufficient stock)

---

### **Folder 4: Reservation Expiry Tests**

#### **Request: Create Cart with Items**
```
Method: POST
URL: {{baseUrl}}/api/carts/{{cartToken}}/items

Body (raw JSON):
{
  "skuId": 1,
  "quantity": 2
}

Tests Script:
pm.test("Item added to cart", function () {
    pm.response.to.have.status(201);
});

console.log("Reservation created. Wait 15 minutes or run manual cleanup.");
```

#### **Request: Check Reservation Status**
```
Method: GET
URL: {{baseUrl}}/api/carts/{{cartToken}}

Tests Script:
pm.test("Cart still active", function () {
    pm.response.to.have.status(200);
    var jsonData = pm.response.json();
    pm.expect(jsonData.data.status).to.eql("ACTIVE");
});
```

#### **Request: Trigger Manual Cleanup**
```
Method: POST
URL: {{baseUrl}}/api/test/cleanup-reservations

Tests Script:
pm.test("Cleanup triggered", function () {
    pm.response.to.have.status(200);
});

console.log("Cleanup completed. Check reservation status in DB.");
```

**Verification SQL:**
```sql
SELECT * FROM stock_reservations 
WHERE cart_token = 'your-cart-token' 
  AND status = 'RELEASED';
```

---

### **Folder 5: Idempotency Tests**

#### **Request: Checkout with Fixed Key**
```
Method: POST
URL: {{baseUrl}}/api/checkout

Body (raw JSON):
{
  "cartToken": "{{cartToken}}",
  "paymentMethod": "COD",
  "shippingAddress": "Idempotency Test",
  "idempotencyKey": "FIXED-KEY-12345"
}

Tests Script:
pm.test("First checkout successful", function () {
    pm.response.to.have.status(201);
    var jsonData = pm.response.json();
    pm.collectionVariables.set("firstOrderCode", jsonData.data.orderCode);
});
```

#### **Request: Checkout with Same Key**
```
Method: POST
URL: {{baseUrl}}/api/checkout

Body (raw JSON):
{
  "cartToken": "{{cartToken}}",
  "paymentMethod": "COD",
  "shippingAddress": "Idempotency Test",
  "idempotencyKey": "FIXED-KEY-12345"
}

Tests Script:
pm.test("Should return same order", function () {
    pm.response.to.have.status(200);
    var jsonData = pm.response.json();
    var firstOrderCode = pm.collectionVariables.get("firstOrderCode");
    pm.expect(jsonData.data.orderCode).to.eql(firstOrderCode);
});

console.log("Idempotency working! Same order returned.");
```

---

### **Folder 6: Rollback Tests**

#### **Pre-requisite:**
```sql
UPDATE product_variants SET stock_quantity = 10 WHERE id = 23;
UPDATE product_variants SET stock_quantity = 0 WHERE id = 24;  -- OUT OF STOCK
UPDATE product_variants SET stock_quantity = 20 WHERE id = 25;
```

#### **Request: Setup Mixed Stock Cart**
```
Method: POST
URL: {{baseUrl}}/api/carts/{{cartToken}}/items

Body (raw JSON):
{
  "skuId": 23,
  "quantity": 2
}

Then add SKU 24 (will fail):
{
  "skuId": 24,
  "quantity": 1
}

Then add SKU 25:
{
  "skuId": 25,
  "quantity": 1
}

Tests Script:
pm.test("Items added to cart", function () {
    pm.response.to.have.status(201);
});
```

#### **Request: Checkout (Should Fail)**
```
Method: POST
URL: {{baseUrl}}/api/checkout

Body (raw JSON):
{
  "cartToken": "{{cartToken}}",
  "paymentMethod": "COD",
  "shippingAddress": "Rollback Test",
  "idempotencyKey": "{{$guid}}"
}

Tests Script:
pm.test("Checkout failed", function () {
    pm.response.to.have.status(400);
});

console.log("Checkout failed as expected. Verify stock rollback in DB.");
```

**Verification SQL:**
```sql
SELECT id, stock_quantity FROM product_variants WHERE id IN (23, 24, 25);
-- Expected:
-- 23 | 10  (unchanged - rollback worked!)
-- 24 | 0   (unchanged)
-- 25 | 20  (unchanged)
```

---

## 🚀 Quick Test Scripts

### **Run All Tests:**
```bash
# Using Newman (Postman CLI)
newman run edge-cases-collection.json \
  --environment edge-cases-environment.json \
  --reporters cli,html \
  --reporter-html-export test-results.html
```

### **Run Specific Folder:**
```bash
newman run edge-cases-collection.json \
  --folder "Race Condition Tests" \
  --iteration-count 10
```

---

## 📊 Test Results Template

```markdown
## Test Execution Report

**Date:** 2026-01-21
**Environment:** localhost:8080
**Database:** MySQL 8.0

### Test Results

| Test Case | Status | Details |
|-----------|--------|---------|
| Out of Stock Prevention | ✅ PASS | Order rejected, stock unchanged |
| Race Condition (10 users, 5 items) | ✅ PASS | 5 orders created, 5 rejected |
| Reservation Expiry (15 min) | ✅ PASS | Reservations released after expiry |
| Idempotency | ✅ PASS | Same key returns same order |
| Rollback on Partial Stock | ✅ PASS | All stock rolled back |

### Performance Metrics

- Average response time: 250ms
- Peak concurrent users: 50
- No 500 errors
- Stock integrity: 100%

### Issues Found

- None

### Recommendations

- Monitor Redis memory usage
- Consider increasing scheduler frequency to 1 minute
- Add alerting for stock < 10
```

---

## 🔍 Debugging Tips

### **Check Request Logs:**
```javascript
// Add to Tests tab
console.log("Request:", pm.request);
console.log("Response:", pm.response.json());
console.log("Variables:", {
    cartToken: pm.collectionVariables.get("cartToken"),
    accessToken: pm.collectionVariables.get("accessToken")
});
```

### **Postman Console:**
- View → Show Postman Console
- See all requests/responses in real-time

### **Network Errors:**
```javascript
pm.test("No network errors", function () {
    pm.expect(pm.response.code).to.be.oneOf([200, 201, 400, 401, 403, 404]);
    pm.expect(pm.response.code).to.not.equal(500);
});
```

---

## ✅ Test Checklist

Before running tests:
- [ ] Database has test data (`init-data.sql` executed)
- [ ] Application is running (`./gradlew bootRun`)
- [ ] Redis is running (`redis-server`)
- [ ] Test scenarios SQL executed (`test-scenarios.sql`)
- [ ] Postman collection imported
- [ ] Environment variables set

After tests:
- [ ] Check application logs for errors
- [ ] Verify stock quantities in database
- [ ] Check Redis keys (`redis-cli KEYS "*"`)
- [ ] Review test results in Postman
- [ ] Clean up test data if needed

---

**Happy Testing! 🎉**

