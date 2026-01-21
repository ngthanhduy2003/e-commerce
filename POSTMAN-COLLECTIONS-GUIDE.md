# 🧪 Postman Collections - Import Guide

## 📦 Có 2 Collections

### 1. **postman-collection.json** (Main Collection)
**Mục đích:** Testing API thông thường

**Bao gồm:**
- ✅ Authentication (Register, Login, Refresh Token, Validate)
- ✅ Products (Get All, Get by ID, Filter, Search, Categories)
- ✅ Cart Management (Create, Add Items, Update, Remove, Clear)
- ✅ Checkout (COD, CARD)
- ✅ Order Management (Get My Orders, Get by ID, Cancel)
- ✅ Order Tracking (Public tracking by token)
- ✅ Admin Panel (Get All Orders, Update Status)

**Sử dụng:** Normal API testing, development

---

### 2. **postman-edge-cases-collection.json** (Edge Cases Collection) ⭐ **MỚI**
**Mục đích:** Testing edge cases & stress scenarios

**Bao gồm:**
- ✅ **Out of Stock Tests** - Test hết hàng vẫn bán
- ✅ **Race Condition Tests** - Test concurrent checkout (chạy 10x)
- ✅ **Reservation Expiry Tests** - Test cart reservation 15 phút
- ✅ **Idempotency Tests** - Test same key returns same order
- ✅ **Rollback Tests** - Test rollback khi partial out of stock

**Sử dụng:** Testing edge cases, QA, pre-production validation

---

## 📥 Hướng dẫn Import

### **Bước 1: Mở Postman**
1. Launch Postman application
2. Click **"Import"** (top left)

### **Bước 2: Import Collections**

#### **Import Main Collection:**
1. Click "Import"
2. Select file: `postman-collection.json`
3. Click "Import"
4. ✅ Collection "E-commerce API v2.0" xuất hiện

#### **Import Edge Cases Collection:**
1. Click "Import"
2. Select file: `postman-edge-cases-collection.json`
3. Click "Import"
4. ✅ Collection "E-commerce API - Edge Cases Testing" xuất hiện

---

## ⚙️ Setup Variables

### **Main Collection Variables:**
```
baseUrl: http://localhost:8080
accessToken: (auto-saved)
refreshToken: (auto-saved)
adminToken: (auto-saved)
cartToken: (auto-saved)
orderId: (auto-saved)
trackingToken: (auto-saved)
cartItemId: (auto-saved)
```

### **Edge Cases Collection Variables:**
```
baseUrl: http://localhost:8080
accessToken: (auto-saved)
cartToken: (auto-saved)
cartItemId: (auto-saved)
orderId: (auto-saved)
firstOrderCode: (auto-saved)
testSkuId: 1
```

**Lưu ý:** Chỉ cần set `baseUrl`, các biến khác tự động lưu khi chạy requests.

---

## 🚀 Cách sử dụng

### **Main Collection - Normal Testing:**

**Test flow:**
```
1. Authentication → Login Customer
2. Products → Get All Products
3. Cart Management → Create Cart → Add Items
4. Checkout → Checkout with COD
5. Order Management → Get My Orders
6. Order Tracking → Track Order by Token
7. Admin Panel → Login Admin → Get All Orders
```

**Chạy thủ công:** Click từng request → Send

---

### **Edge Cases Collection - Advanced Testing:**

#### **Test 1: Out of Stock**
```
Setup → Login Customer
Out of Stock Tests → Run all (Create Cart → Add Item → Checkout)
Expected: Checkout fails with "Insufficient stock"
```

#### **Test 2: Race Condition**
```
Setup → Login Customer
Race Condition Tests → Select "Concurrent Checkout Setup"
Click "Run" → Runner:
  - Iterations: 10
  - Delay: 0ms
  - Run
Expected: ~5 success, ~5 fail (if stock = 5)
```

#### **Test 3: Reservation Expiry**
```
Reservation Expiry Tests:
1. Create Cart → Add Item
2. Trigger Manual Cleanup
3. Check DB: status = RELEASED
```

#### **Test 4: Idempotency**
```
Idempotency Tests:
1. Setup Cart → Add Item
2. Checkout with Fixed Key (1st) → 201 Created
3. Checkout with Same Key (2nd) → 200 OK (same order)
```

#### **Test 5: Rollback**
```
Pre-requisite: Run SQL
  UPDATE product_variants SET stock_quantity = 0 WHERE id = 24;
  
Rollback Tests:
1. Setup Mixed Stock Cart
2. Add Item 1, 2, 3
3. Checkout (fails at item 2)
4. Verify DB: Stock 1 and 3 unchanged (rollback worked)
```

---

## 🔧 Collection Runner (For Stress Testing)

### **Run Race Condition Test:**

1. Open **Edge Cases Collection**
2. Navigate: **"3. Race Condition Tests"**
3. Select request: **"Concurrent Checkout Setup"**
4. Click **"Run"** (top right)
5. In Runner:
   - **Iterations:** `10` (or 50, 100 for stress test)
   - **Delay:** `0ms`
   - **Data:** None
   - Click **"Run E-commerce..."**

6. **Watch Console:**
   - ✅ Success orders
   - ❌ Failed orders (out of stock)

7. **Verify in DB:**
```sql
SELECT stock_quantity FROM product_variants WHERE id = 15;
-- Should be 0 (if initial = 5, after 5 successful orders)

SELECT COUNT(*) FROM orders WHERE created_at > NOW() - INTERVAL 5 MINUTE;
-- Should be 5 (not 10!)
```

---

## 📊 Expected Results

### **Out of Stock Test:**
```
Request: Add 999 items (stock = 50)
Response: 400 Bad Request
Message: "Insufficient stock for product variant: 1"
✅ PASS
```

### **Race Condition Test (10 users, stock = 5):**
```
Iteration 1: 201 Created ✅
Iteration 2: 201 Created ✅
Iteration 3: 201 Created ✅
Iteration 4: 201 Created ✅
Iteration 5: 201 Created ✅
Iteration 6: 400 Bad Request ❌ (Out of stock)
Iteration 7: 400 Bad Request ❌
Iteration 8: 400 Bad Request ❌
Iteration 9: 400 Bad Request ❌
Iteration 10: 400 Bad Request ❌

Final stock: 0
Total orders: 5
✅ PASS - No overselling!
```

### **Idempotency Test:**
```
1st request: 201 Created (Order #1)
2nd request (same key): 200 OK (Order #1 - same)
DB check: Only 1 order created
✅ PASS
```

### **Rollback Test:**
```
Cart: SKU 23 (stock=10), SKU 24 (stock=0), SKU 25 (stock=20)
Checkout: Fails at SKU 24
DB check: 
  - SKU 23: stock = 10 (unchanged) ✅
  - SKU 24: stock = 0 (unchanged) ✅
  - SKU 25: stock = 20 (unchanged) ✅
✅ PASS - Rollback worked!
```

---

## 🐛 Troubleshooting

### **Issue: accessToken empty**
**Solution:** Run "Login Customer" first in Setup folder

### **Issue: cartToken not found**
**Solution:** Run "Create Cart" before adding items

### **Issue: 401 Unauthorized**
**Solution:** Token expired, re-run login

### **Issue: Race condition test all success (no 400)**
**Solution:** Stock too high, reduce stock in DB:
```sql
UPDATE product_variants SET stock_quantity = 5 WHERE id = 15;
```

### **Issue: Rollback test all items success**
**Solution:** SKU 24 not out of stock:
```sql
UPDATE product_variants SET stock_quantity = 0 WHERE id = 24;
```

---

## 📚 Related Documentation

- **POSTMAN-EDGE-CASES-GUIDE.md** - Detailed testing guide
- **EDGE-CASES-TESTING-GUIDE.md** - Comprehensive edge cases explanation
- **test-scenarios.sql** - SQL setup scripts
- **README-EDGE-CASES-TESTING.md** - Master guide

---

## ✅ Quick Checklist

Before testing edge cases:
- [ ] Application running (`./gradlew bootRun`)
- [ ] Database has test data (`init-data.sql`)
- [ ] Redis running (`redis-server`)
- [ ] Test scenarios SQL executed (`test-scenarios.sql`)
- [ ] Both collections imported
- [ ] baseUrl variable set

---

## 🎯 Summary

| Collection | Purpose | When to Use |
|------------|---------|-------------|
| **postman-collection.json** | Normal API testing | Development, debugging, demo |
| **postman-edge-cases-collection.json** | Edge cases testing | QA, stress test, pre-production |

**Import both collections to have complete testing coverage!** ✅

---

**Happy Testing! 🎉**

