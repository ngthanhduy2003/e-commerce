# 🚀 Postman Quick Start Guide

## Chuẩn bị

### 1. Rebuild Application
```bash
./gradlew.bat clean build -x test
./gradlew.bat bootRun
```

### 2. Import Postman Collection
- Import file: `postman-collection.json`
- Collection: "E-commerce API v2.0"

### 3. Kiểm tra Variables
- Click vào collection name
- Tab **Variables**
- `baseUrl`: http://localhost:8080

---

## 📋 Collection Structure

```
E-commerce API v2.0
├── 1. Authentication (5 endpoints)
├── 2. Products (6 endpoints)
├── 3. Cart Management (7 endpoints)
├── 4. Checkout (2 endpoints)
├── 5. Order Management (3 endpoints)
├── 6. Order Tracking (1 endpoint)
└── 7. Admin Panel (5 endpoints)
```

**Total: 29 endpoints**

---

# 🔐 1. AUTHENTICATION

## 1.1 Register Customer
**POST** `/api/auth/register`

**Body:**
```json
{
  "email": "newcustomer@example.com",
  "password": "password123",
  "fullName": "New Customer"
}
```

**Kết quả:** 
- Status 201
- ✓ `accessToken` tự động lưu
- ✓ `refreshToken` tự động lưu

---

## 1.2 Login Customer
**POST** `/api/auth/login`

**Body:**
```json
{
  "email": "customer@example.com",
  "password": "password123"
}
```

**Kết quả:** ✓ `accessToken` và `refreshToken` tự động lưu

---

## 1.3 Login Admin
**POST** `/api/auth/login`

**Body:**
```json
{
  "email": "admin@ecommerce.com",
  "password": "password123"
}
```

**Kết quả:** ✓ `adminToken` tự động lưu

---

## 1.4 Refresh Token
**POST** `/api/auth/refresh`

**Body:**
```json
{
  "refreshToken": "{{refreshToken}}"
}
```

**Kết quả:** Token mới được lưu vào `accessToken`

---

## 1.5 Validate Token
**GET** `/api/auth/validate`

**Auth:** Bearer {{accessToken}}

**Kết quả:** Trả về user info và authorities

---

# 📦 2. PRODUCTS

## 2.1 Get All Products
**GET** `/api/products?page=0&size=20`

**Query Params (optional):**
- `page` - Số trang (default: 0)
- `size` - Số items/trang (default: 20)
- `categoryId` - Filter theo category
- `minPrice` - Giá tối thiểu
- `maxPrice` - Giá tối đa

**Kết quả:** Danh sách 8 products với 43 variants

---

## 2.2 Get Product by ID
**GET** `/api/products/1`

**Kết quả:** Chi tiết product ID 1 (iPhone 15 Pro)

---

## 2.3 Filter Products by Category
**GET** `/api/products?page=0&size=20&categoryId=1`

**Category IDs:**
- `1` - Electronics
- `2` - Clothing
- `3` - Books
- `4` - Home & Garden
- `5` - Sports

---

## 2.4 Filter Products by Price
**GET** `/api/products?page=0&size=20&minPrice=100&maxPrice=1000`

**Ví dụ:**
- `minPrice=50&maxPrice=200` - Tìm sản phẩm từ $50-$200

---

## 2.5 Search Products
**GET** `/api/products?page=0&size=20&search=iPhone`

**Search keywords:**
- `iPhone` - Tìm iPhone products
- `Nike` - Tìm Nike shoes
- `Book` - Tìm sách

---

## 2.6 Get All Categories
**GET** `/api/categories`

**Public endpoint** - Không cần authentication

**Kết quả:** 5 categories

---

# 🛒 3. CART MANAGEMENT

## 3.1 Create Cart
**POST** `/api/carts`

**Không cần body**

**Kết quả:** ✓ `cartToken` tự động lưu

---

## 3.2 Get Cart
**GET** `/api/carts/{{cartToken}}`

**Kết quả:** Thông tin cart hiện tại

---

## 3.3 Add Item to Cart
**POST** `/api/carts/{{cartToken}}/items`

**Body:**
```json
{
  "skuId": 1,
  "quantity": 2
}
```

**SKU IDs có sẵn:**
- `1` - iPhone 15 Pro 128GB Black ($999)
- `7` - MacBook Pro M3 512GB Silver ($1999)
- `11` - Samsung Galaxy S24 256GB Black ($899)
- `15` - Nike Air Max size 40 Red ($150)
- `23` - Levi's Jeans size 30 Blue ($79.99)
- `31` - The Great Gatsby Paperback ($12.99)

**Kết quả:** 
- Item added to cart
- ✓ `cartItemId` tự động lưu

---

## 3.4 Get Cart Details
**GET** `/api/carts/{{cartToken}}`

**Auth:** Bearer {{accessToken}}

**Kết quả:** Chi tiết giỏ hàng với items

---

## 3.5 Update Cart Item
**PATCH** `/api/carts/{{cartToken}}/items/{{cartItemId}}`

**Auth:** Bearer {{accessToken}}

**Body:**
```json
{
  "quantity": 3
}
```

**Kết quả:** Số lượng item được cập nhật

---

## 3.6 Remove Item from Cart
**DELETE** `/api/carts/{{cartToken}}/items/{{cartItemId}}`

**Auth:** Bearer {{accessToken}}

**Kết quả:** Item bị xóa khỏi cart

---

## 3.7 Clear Cart
**DELETE** `/api/carts/{{cartToken}}`

**Auth:** Bearer {{accessToken}}

**Kết quả:** Cart bị xóa hoàn toàn

---

# 💳 4. CHECKOUT

## 4.1 Checkout with COD
**POST** `/api/checkout`

**Auth:** Bearer {{accessToken}}

**Body:**
```json
{
  "cartToken": "{{cartToken}}",
  "paymentMethod": "COD",
  "shippingAddress": "123 Main Street, Hanoi, Vietnam, 10000",
  "idempotencyKey": "{{$guid}}"
}
```

**Payment Methods:**
- `COD` - Cash on Delivery
- `CARD` - Credit/Debit Card
- `CASH` - Cash
- `BANK_TRANSFER` - Bank Transfer

**Kết quả:** 
- Order created (Status 201)
- ✓ `orderId` tự động lưu
- ✓ `trackingToken` tự động lưu

---

## 4.2 Checkout with Bank Transfer
**POST** `/api/checkout`

**Auth:** Bearer {{accessToken}}

**Body:**
```json
{
  "cartToken": "{{cartToken}}",
  "paymentMethod": "BANK_TRANSFER",
  "shippingAddress": "456 Another Street, Ho Chi Minh City, Vietnam",
  "idempotencyKey": "{{$guid}}"
}
```

**Lưu ý:** `{{$guid}}` tự động tạo UUID mới mỗi request

---

# 📋 5. ORDER MANAGEMENT (Customer)

## 5.1 Get My Orders
**GET** `/api/orders/my?page=0&size=10`

**Auth:** Bearer {{accessToken}}

**Query Params:**
- `page` - Số trang (default: 0)
- `size` - Số items/trang (default: 10)

**Kết quả:** Danh sách orders của user đang login

---

## 5.2 Get Order by ID
**GET** `/api/orders/{{orderId}}`

**Auth:** Bearer {{accessToken}}

**Kết quả:** Chi tiết order cụ thể

---

## 5.3 Cancel Order
**PATCH** `/api/orders/{{orderId}}/cancel`

**Auth:** Bearer {{accessToken}}

**Không cần body**

**Kết quả:** 
- Order status → CANCELLED
- Stock được hoàn lại

---

# 📍 6. ORDER TRACKING (Public)

## 6.1 Track Order by Token
**GET** `/api/tracking/{{trackingToken}}`

**Public endpoint** - Không cần authentication

**Kết quả:** Thông tin tracking order (cho khách hàng không đăng nhập)

---

# 👨‍💼 7. ADMIN PANEL (Admin Only)

## 7.1 Get All Orders
**GET** `/api/admin/orders?page=0&size=20`

**Auth:** Bearer {{adminToken}}

**Query Params (optional):**
- `page` - Số trang
- `size` - Số items/trang
- `status` - Filter theo status (PENDING, CONFIRMED, etc.)

**Kết quả:** Tất cả orders trong hệ thống

---

## 7.2 Filter Orders by Status - PENDING
**GET** `/api/admin/orders?page=0&size=20&status=PENDING`

**Auth:** Bearer {{adminToken}}

**Order Statuses:**
- `PENDING` - Đơn hàng mới
- `CONFIRMED` - Đã xác nhận
- `PAID` - Đã thanh toán
- `SHIPPED` - Đang vận chuyển
- `DELIVERED` - Đã giao hàng
- `CANCELLED` - Đã hủy

---

## 7.3 Update Order Status - Confirm
**PATCH** `/api/admin/orders/{{orderId}}/status`

**Auth:** Bearer {{adminToken}}

**Body:**
```json
{
  "status": "CONFIRMED"
}
```

**Kết quả:** Order status → CONFIRMED

---

## 7.4 Update Order Status - Ship
**PATCH** `/api/admin/orders/{{orderId}}/status`

**Auth:** Bearer {{adminToken}}

**Body:**
```json
{
  "status": "SHIPPED"
}
```

**Kết quả:** Order status → SHIPPED

---

## 7.5 Update Order Status - Deliver
**PATCH** `/api/admin/orders/{{orderId}}/status`

**Auth:** Bearer {{adminToken}}

**Body:**
```json
{
  "status": "DELIVERED"
}
```

**Kết quả:** Order status → DELIVERED

---

# 🎯 COMPLETE TEST FLOW

## Flow 1: Customer Purchase Journey (8 steps)

1. **Register/Login Customer** → Get `accessToken`
2. **Get All Products** → Browse catalog
3. **Get Categories** → Filter by category
4. **Create Cart** → Get `cartToken`
5. **Add Item to Cart** (x2-3 items) → Build cart
6. **Get Cart Details** → Review cart
7. **Checkout with COD** → Get `orderId` & `trackingToken`
8. **Get My Orders** → View order history

---

## Flow 2: Admin Order Management (6 steps)

1. **Login Admin** → Get `adminToken`
2. **Get All Orders** → View all orders
3. **Filter by Status PENDING** → Find new orders
4. **Update Status → CONFIRMED** → Confirm order
5. **Update Status → SHIPPED** → Ship order
6. **Update Status → DELIVERED** → Complete order

---

## Flow 3: Public Order Tracking (1 step)

1. **Track Order by Token** → No auth needed, use `{{trackingToken}}`

---

# 📊 SAMPLE DATA REFERENCE

## Test Accounts

| Email                    | Password    | Role     | Token Variable  |
|--------------------------|-------------|----------|-----------------|
| customer@example.com     | password123 | Customer | {{accessToken}} |
| jane.smith@example.com   | password123 | Customer | {{accessToken}} |
| admin@ecommerce.com      | password123 | Admin    | {{adminToken}}  |
| staff@ecommerce.com      | password123 | Staff    | N/A             |

---

## Product Categories

| ID | Name           | Products |
|----|----------------|----------|
| 1  | Electronics    | 3        |
| 2  | Clothing       | 3        |
| 3  | Books          | 1        |
| 4  | Home & Garden  | 1        |
| 5  | Sports         | 0        |

---

## Popular SKU IDs for Testing

| SKU ID | Product              | Size | Color      | Price   | Stock |
|--------|----------------------|------|------------|---------|-------|
| 1      | iPhone 15 Pro        | 128  | Black      | $999    | 50    |
| 7      | MacBook Pro M3       | 512  | Silver     | $1999   | 15    |
| 11     | Samsung Galaxy S24   | 256  | Black      | $899    | 40    |
| 15     | Nike Air Max         | 40   | Red        | $150    | 100   |
| 23     | Levi's Jeans         | 30   | Blue       | $79.99  | 120   |
| 31     | The Great Gatsby     | -    | Paperback  | $12.99  | 200   |
| 36     | Cotton T-Shirt       | S    | Black      | $29.99  | 100   |

**Total: 43 SKUs across 8 products**

---

# ❓ FAQ & TROUBLESHOOTING

## Q: Tôi có phải nhập key-value thủ công không?

**A:** KHÔNG. Tất cả tự động:
- Tab **Body** → chọn raw → chọn JSON → nhập JSON
- Token tự động từ Variables ({{accessToken}}, {{adminToken}})
- Không cần chỉnh Headers hoặc Authorization tab
- Variables tự động lưu sau mỗi request thành công

---

## Q: Token không tự động lưu?

**A:** Kiểm tra:
1. ✅ Response có status 200 (Login) hoặc 201 (Register)?
2. ✅ Mở Console (View → Show Postman Console) → Có log "tokens saved"?
3. ✅ Click collection name → Variables tab → `accessToken` có giá trị?
4. ✅ Response body có field `data.accessToken`?

---

## Q: Lỗi "skuId is required"?

**A:** 
- ✅ Đảm bảo dùng `skuId` chứ KHÔNG phải `productVariantId`
- ✅ Import lại file `postman-collection.json` (đã được update)
- ❌ Sai: `{"productVariantId": 1}`
- ✅ Đúng: `{"skuId": 1}`

---

## Q: Lỗi "idempotencyKey is required"?

**A:** 
- ✅ Import lại `postman-collection.json` (đã thêm idempotencyKey)
- ✅ Body phải có: `"idempotencyKey": "{{$guid}}"`
- `{{$guid}}` tự động tạo UUID mỗi request

---

## Q: Categories trả về 401?

**A:** 
- ✅ Restart application (SecurityConfig đã được fix)
- `/api/categories` giờ là public endpoint

---

## Q: "Get My Orders" không có data?

**A:** 
- ✅ Login đúng user (`customer@example.com`)
- ✅ Run `init-data.sql` để tạo sample order
- ✅ Hoặc tạo order mới qua checkout flow

---

## Q: Response 401 Unauthorized?

**A:** 
1. ✅ Login trước để lấy token
2. ✅ Kiểm tra Variables tab → `accessToken` có giá trị?
3. ✅ Token hết hạn? → Login lại
4. ✅ Admin endpoint? → Dùng `{{adminToken}}`

---

## Q: Variables {{cartToken}}, {{orderId}} bị empty?

**A:** Chạy requests theo thứ tự:
1. Login → lưu `accessToken`
2. Create Cart → lưu `cartToken`
3. Add to Cart → dùng `{{cartToken}}`
4. Checkout → lưu `orderId` và `trackingToken`

---

# ✅ PRE-FLIGHT CHECKLIST

- [ ] Application đang chạy trên port 8080
- [ ] Database đã run `init-data.sql`
- [ ] Postman collection đã import
- [ ] baseUrl = `http://localhost:8080`
- [ ] Console đã mở (Ctrl+Alt+C)

---

# 🎯 QUICK TEST (5 phút)

1. ✅ Login Customer → Check `accessToken`
2. ✅ Get All Products → Check 8 products
3. ✅ Get Categories → Check 5 categories
4. ✅ Create Cart → Check `cartToken`
5. ✅ Add to Cart (SKU 1) → Check success
6. ✅ Checkout COD → Check `orderId`
7. ✅ Get My Orders → Check ≥1 order

**Nếu tất cả pass → System OK! 🎉**

---

**📖 Xem thêm:**
- `POSTMAN-FIXES.md` - Chi tiết 7 fixes
- `CHANGES-SUMMARY.md` - Tổng hợp thay đổi
- `init-data.sql` - Sample data

**🎉 Happy Testing!**

