# 📋 API Endpoints Reference

## Tổng quan: 29 Endpoints

| # | Method | Endpoint | Auth | Description |
|---|--------|----------|------|-------------|
| **1. AUTHENTICATION (5)** |
| 1 | POST | `/api/auth/register` | None | Đăng ký tài khoản mới |
| 2 | POST | `/api/auth/login` | None | Đăng nhập (Customer/Admin) |
| 3 | POST | `/api/auth/refresh` | None | Refresh access token |
| 4 | GET | `/api/auth/validate` | Bearer | Validate token & get user info |
| **2. PRODUCTS (6)** |
| 5 | GET | `/api/products` | None | Lấy danh sách products (có pagination) |
| 6 | GET | `/api/products/{id}` | None | Lấy chi tiết product theo ID |
| 7 | GET | `/api/products?categoryId={id}` | None | Filter products theo category |
| 8 | GET | `/api/products?minPrice=&maxPrice=` | None | Filter products theo giá |
| 9 | GET | `/api/products?search={keyword}` | None | Tìm kiếm products |
| 10 | GET | `/api/categories` | None | Lấy danh sách categories |
| **3. CART MANAGEMENT (7)** |
| 11 | POST | `/api/carts` | None | Tạo cart mới (anonymous) |
| 12 | GET | `/api/carts/{cartToken}` | None | Xem cart (public) |
| 13 | POST | `/api/carts/{cartToken}/items` | None | Thêm item vào cart |
| 14 | GET | `/api/carts/{cartToken}` | Bearer | Xem cart details (authenticated) |
| 15 | PATCH | `/api/carts/{cartToken}/items/{itemId}` | Bearer | Cập nhật số lượng item |
| 16 | DELETE | `/api/carts/{cartToken}/items/{itemId}` | Bearer | Xóa item khỏi cart |
| 17 | DELETE | `/api/carts/{cartToken}` | Bearer | Xóa toàn bộ cart |
| **4. CHECKOUT (2)** |
| 18 | POST | `/api/checkout` | Bearer | Thanh toán COD |
| 19 | POST | `/api/checkout` | Bearer | Thanh toán Bank Transfer |
| **5. ORDER MANAGEMENT (3)** |
| 20 | GET | `/api/orders/my` | Bearer | Xem orders của mình |
| 21 | GET | `/api/orders/{id}` | Bearer | Xem chi tiết order |
| 22 | PATCH | `/api/orders/{id}/cancel` | Bearer | Hủy order |
| **6. ORDER TRACKING (1)** |
| 23 | GET | `/api/tracking/{trackingToken}` | None | Track order (public) |
| **7. ADMIN PANEL (5)** |
| 24 | GET | `/api/admin/orders` | Admin | Xem tất cả orders |
| 25 | GET | `/api/admin/orders?status={status}` | Admin | Filter orders theo status |
| 26 | PATCH | `/api/admin/orders/{id}/status` | Admin | Update order status (CONFIRMED) |
| 27 | PATCH | `/api/admin/orders/{id}/status` | Admin | Update order status (SHIPPED) |
| 28 | PATCH | `/api/admin/orders/{id}/status` | Admin | Update order status (DELIVERED) |
| 29 | PATCH | `/api/admin/orders/{id}/status` | Admin | Update order status (CANCELLED) |

---

## 🔓 Public Endpoints (Không cần auth)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Đăng ký |
| POST | `/api/auth/login` | Đăng nhập |
| POST | `/api/auth/refresh` | Refresh token |
| GET | `/api/products` | Browse products |
| GET | `/api/products/{id}` | Product details |
| GET | `/api/categories` | Browse categories |
| POST | `/api/carts` | Create cart |
| GET | `/api/carts/{cartToken}` | View cart |
| POST | `/api/carts/{cartToken}/items` | Add to cart |
| GET | `/api/tracking/{trackingToken}` | Track order |

**Total: 10 public endpoints**

---

## 🔒 Authenticated Endpoints (Cần Bearer Token)

| Method | Endpoint | Token | Description |
|--------|----------|-------|-------------|
| GET | `/api/auth/validate` | {{accessToken}} | Validate token |
| GET | `/api/carts/{cartToken}` | {{accessToken}} | Cart details |
| PATCH | `/api/carts/{cartToken}/items/{id}` | {{accessToken}} | Update cart item |
| DELETE | `/api/carts/{cartToken}/items/{id}` | {{accessToken}} | Remove cart item |
| DELETE | `/api/carts/{cartToken}` | {{accessToken}} | Clear cart |
| POST | `/api/checkout` | {{accessToken}} | Place order |
| GET | `/api/orders/my` | {{accessToken}} | My orders |
| GET | `/api/orders/{id}` | {{accessToken}} | Order details |
| PATCH | `/api/orders/{id}/cancel` | {{accessToken}} | Cancel order |

**Total: 9 authenticated endpoints**

---

## 👨‍💼 Admin Endpoints (Cần Admin Token)

| Method | Endpoint | Token | Description |
|--------|----------|-------|-------------|
| GET | `/api/admin/orders` | {{adminToken}} | All orders |
| GET | `/api/admin/orders?status={s}` | {{adminToken}} | Filter by status |
| PATCH | `/api/admin/orders/{id}/status` | {{adminToken}} | Update status |

**Total: 3 admin endpoint patterns (5 variations)**

---

## 📝 Request Body Templates

### Register/Login
```json
{
  "email": "user@example.com",
  "password": "password123",
  "fullName": "User Name"  // Only for register
}
```

### Refresh Token
```json
{
  "refreshToken": "{{refreshToken}}"
}
```

### Add to Cart
```json
{
  "skuId": 1,
  "quantity": 2
}
```

### Update Cart Item
```json
{
  "quantity": 3
}
```

### Checkout
```json
{
  "cartToken": "{{cartToken}}",
  "paymentMethod": "COD",  // COD, CARD, CASH, BANK_TRANSFER
  "shippingAddress": "123 Street, City, Country",
  "idempotencyKey": "{{$guid}}"
}
```

### Update Order Status (Admin)
```json
{
  "status": "CONFIRMED"  // PENDING, CONFIRMED, PAID, SHIPPED, DELIVERED, CANCELLED
}
```

---

## 🔑 Variables Reference

| Variable | Khi nào được lưu | Dùng cho endpoint nào |
|----------|------------------|----------------------|
| `accessToken` | Login Customer/Register | Authenticated endpoints |
| `refreshToken` | Login Customer/Register | Refresh token |
| `adminToken` | Login Admin | Admin endpoints |
| `cartToken` | Create Cart | Cart operations |
| `cartItemId` | Add to Cart | Update/Remove cart item |
| `orderId` | Checkout | Order operations |
| `trackingToken` | Checkout | Order tracking |

---

## 🚦 HTTP Status Codes

| Code | Meaning | When |
|------|---------|------|
| 200 | OK | GET, PATCH, DELETE success |
| 201 | Created | POST success (Register, Checkout) |
| 400 | Bad Request | Validation errors |
| 401 | Unauthorized | Missing/invalid token |
| 403 | Forbidden | Insufficient permissions |
| 404 | Not Found | Resource không tồn tại |
| 500 | Server Error | Backend error |

---

## 📦 Query Parameters

### Products
- `page` (default: 0) - Số trang
- `size` (default: 20) - Items per page
- `categoryId` - Filter by category
- `minPrice` - Giá tối thiểu
- `maxPrice` - Giá tối đa
- `search` - Search keyword

### Orders (Admin)
- `page` (default: 0) - Số trang
- `size` (default: 20) - Items per page
- `status` - Filter by status

### My Orders
- `page` (default: 0) - Số trang
- `size` (default: 10) - Items per page

---

## 🎯 Common Use Cases

### Use Case 1: Anonymous User Browse & Purchase
1. GET `/api/products` - Browse products
2. GET `/api/categories` - View categories
3. POST `/api/carts` - Create cart
4. POST `/api/carts/{token}/items` - Add items
5. POST `/api/auth/register` - Register account
6. POST `/api/checkout` - Place order

### Use Case 2: Registered User Purchase
1. POST `/api/auth/login` - Login
2. GET `/api/products?categoryId=1` - Browse by category
3. POST `/api/carts` - Create cart
4. POST `/api/carts/{token}/items` - Add multiple items
5. GET `/api/carts/{token}` - Review cart
6. POST `/api/checkout` - Checkout
7. GET `/api/orders/my` - View order history

### Use Case 3: Admin Order Management
1. POST `/api/auth/login` - Login as admin
2. GET `/api/admin/orders?status=PENDING` - View new orders
3. PATCH `/api/admin/orders/{id}/status` - Confirm order
4. PATCH `/api/admin/orders/{id}/status` - Ship order
5. PATCH `/api/admin/orders/{id}/status` - Mark delivered

### Use Case 4: Customer Track Order
1. GET `/api/tracking/{trackingToken}` - Track without login
2. Or: POST `/api/auth/login` → GET `/api/orders/my` - View all orders

---

## 🔄 Order Status Flow

```
PENDING → CONFIRMED → PAID → SHIPPED → DELIVERED
   ↓
CANCELLED (có thể cancel ở bất kỳ stage nào)
```

**Transitions:**
- Customer creates order → `PENDING`
- Admin confirms → `CONFIRMED`
- Payment received → `PAID`
- Admin ships → `SHIPPED`
- Delivery complete → `DELIVERED`
- Customer/Admin cancels → `CANCELLED`

---

## 💡 Best Practices

1. **Idempotency Key**: Luôn dùng unique key cho checkout để tránh duplicate orders
2. **Token Refresh**: Dùng refresh token khi access token hết hạn thay vì login lại
3. **Pagination**: Luôn dùng `page` và `size` khi query danh sách
4. **Error Handling**: Kiểm tra status code và response message
5. **Cart Expiry**: Cart có thời gian expire (24h), tạo mới nếu hết hạn

---

**Quick Reference Card - Print & Keep!** 📄

