# 📊 PHÂN TÍCH LUỒNG DỮ LIỆU & CÁCH THỨC HOẠT ĐỘNG

**Project:** E-Commerce Backend API  
**Version:** 2.0  
**Date:** January 21, 2026

---

## 📋 MỤC LỤC

1. [Tổng Quan Kiến Trúc](#tổng-quan-kiến-trúc)
2. [Luồng Authentication](#luồng-authentication)
3. [Luồng Quản Lý Sản Phẩm](#luồng-quản-lý-sản-phẩm)
4. [Luồng Giỏ Hàng](#luồng-giỏ-hàng)
5. [Luồng Đặt Hàng (Checkout)](#luồng-đặt-hàng-checkout)
6. [Luồng Quản Lý Tồn Kho](#luồng-quản-lý-tồn-kho)
7. [Luồng Admin](#luồng-admin)
8. [Luồng Tracking](#luồng-tracking)
9. [Data Models](#data-models)
10. [Race Condition Handling](#race-condition-handling)

---

## 🏗️ TỔNG QUAN KIẾN TRÚC

### Tech Stack

```
┌─────────────────────────────────────────────────┐
│              CLIENT (Browser/Mobile)             │
└──────────────────┬──────────────────────────────┘
                   │ HTTP/REST
                   ▼
┌─────────────────────────────────────────────────┐
│         Spring Boot Application (Port 8080)      │
│  ┌──────────────────────────────────────────┐  │
│  │          Controllers (REST API)           │  │
│  └────────────────┬─────────────────────────┘  │
│                   │                              │
│  ┌────────────────▼─────────────────────────┐  │
│  │        Security (JWT Filter)              │  │
│  └────────────────┬─────────────────────────┘  │
│                   │                              │
│  ┌────────────────▼─────────────────────────┐  │
│  │          Services (Business Logic)        │  │
│  └────────────────┬─────────────────────────┘  │
│                   │                              │
│  ┌────────────────▼─────────────────────────┐  │
│  │        Repositories (Data Access)         │  │
│  └────────────────┬─────────────────────────┘  │
└───────────────────┼──────────────────────────────┘
                    │
        ┌───────────┴───────────┐
        ▼                       ▼
┌────────────────┐    ┌──────────────────┐
│   MySQL DB     │    │   Redis Cache    │
│  (Persistent)  │    │  (Temporary)     │
└────────────────┘    └──────────────────┘
```

### Layers & Responsibilities

| Layer | Package | Responsibility |
|-------|---------|----------------|
| **Controller** | `controller/` | Handle HTTP requests, validation, response |
| **Security** | `security/` | JWT authentication, authorization |
| **Service** | `service/` | Business logic, orchestration |
| **Repository** | `repository/` | Database operations, queries |
| **Entity** | `model/entity/` | JPA entities, database mapping |
| **DTO** | `dto/` | Data transfer objects |
| **Config** | `config/` | Application configuration |

---

## 🔐 LUỒNG AUTHENTICATION

### 1. Register Flow

```
Client                Controller           Service              Repository         Database
  │                       │                    │                     │                 │
  │  POST /api/auth/register                   │                     │                 │
  ├──────────────────────>│                    │                     │                 │
  │  {email, password,    │                    │                     │                 │
  │   fullName}           │                    │                     │                 │
  │                       │                    │                     │                 │
  │                       │  validate input    │                     │                 │
  │                       │  (Jakarta Validation)                    │                 │
  │                       │                    │                     │                 │
  │                       │  register()        │                     │                 │
  │                       ├───────────────────>│                     │                 │
  │                       │                    │                     │                 │
  │                       │                    │  check email exists │                 │
  │                       │                    ├────────────────────>│                 │
  │                       │                    │                     │  SELECT users   │
  │                       │                    │                     ├────────────────>│
  │                       │                    │                     │<────────────────┤
  │                       │                    │<────────────────────┤                 │
  │                       │                    │                     │                 │
  │                       │                    │  hash password      │                 │
  │                       │                    │  (BCrypt)           │                 │
  │                       │                    │                     │                 │
  │                       │                    │  get USER role      │                 │
  │                       │                    ├────────────────────>│                 │
  │                       │                    │                     │  SELECT roles   │
  │                       │                    │                     ├────────────────>│
  │                       │                    │                     │<────────────────┤
  │                       │                    │<────────────────────┤                 │
  │                       │                    │                     │                 │
  │                       │                    │  create user        │                 │
  │                       │                    ├────────────────────>│                 │
  │                       │                    │                     │  INSERT users   │
  │                       │                    │                     ├────────────────>│
  │                       │                    │                     │<────────────────┤
  │                       │                    │<────────────────────┤                 │
  │                       │                    │                     │                 │
  │                       │                    │  generate JWT       │                 │
  │                       │                    │  (access + refresh) │                 │
  │                       │                    │                     │                 │
  │                       │<───────────────────┤                     │                 │
  │<──────────────────────┤                    │                     │                 │
  │  {accessToken,        │                    │                     │                 │
  │   refreshToken,       │                    │                     │                 │
  │   userId, role}       │                    │                     │                 │
```

**Data Flow:**
1. Client gửi email, password, fullName
2. Controller validate input (Jakarta Bean Validation)
3. Service check email đã tồn tại chưa
4. Service hash password với BCrypt
5. Service lấy role USER từ database
6. Service tạo User entity và save
7. Service generate JWT tokens (access 24h, refresh 7d)
8. Controller trả về tokens + user info

**Key Points:**
- Password KHÔNG BAO GIỜ lưu plaintext, chỉ lưu BCrypt hash
- JWT token chứa: userId, email, role
- Access token expire 24h, refresh token expire 7d

### 2. Login Flow

```
Client                Controller           Service              Repository         Database
  │                       │                    │                     │                 │
  │  POST /api/auth/login │                    │                     │                 │
  ├──────────────────────>│                    │                     │                 │
  │  {email, password}    │                    │                     │                 │
  │                       │                    │                     │                 │
  │                       │  authenticate      │                     │                 │
  │                       │  (Spring Security) │                     │                 │
  │                       ├───────────────────>│                     │                 │
  │                       │                    │                     │                 │
  │                       │                    │  load user by email │                 │
  │                       │                    ├────────────────────>│                 │
  │                       │                    │                     │  SELECT users   │
  │                       │                    │                     │  JOIN roles     │
  │                       │                    │                     ├────────────────>│
  │                       │                    │                     │<────────────────┤
  │                       │                    │<────────────────────┤                 │
  │                       │                    │                     │                 │
  │                       │                    │  compare password   │                 │
  │                       │                    │  (BCrypt.matches)   │                 │
  │                       │                    │                     │                 │
  │                       │                    │  generate JWT       │                 │
  │                       │                    │                     │                 │
  │                       │<───────────────────┤                     │                 │
  │<──────────────────────┤                    │                     │                 │
  │  {accessToken,        │                    │                     │                 │
  │   refreshToken,       │                    │                     │                 │
  │   userId, role}       │                    │                     │                 │
```

**Data Flow:**
1. Client gửi email + password
2. Spring Security authenticate
3. Load user từ database
4. Compare password hash với BCrypt
5. Generate JWT tokens
6. Return tokens

### 3. Protected Endpoint Flow

```
Client                JWT Filter           Service              Repository
  │                       │                    │                     │
  │  GET /api/orders/my   │                    │                     │
  │  Authorization: Bearer <token>             │                     │
  ├──────────────────────>│                    │                     │
  │                       │                    │                     │
  │                       │  extract token     │                     │
  │                       │  validate token    │                     │
  │                       │  (signature, exp)  │                     │
  │                       │                    │                     │
  │                       │  get userId from   │                     │
  │                       │  token claims      │                     │
  │                       │                    │                     │
  │                       │  set Authentication│                     │
  │                       │  in SecurityContext│                     │
  │                       │                    │                     │
  │                       ├───────────────────>│                     │
  │                       │                    │  get orders by userId
  │                       │                    ├────────────────────>│
  │                       │                    │<────────────────────┤
  │                       │<───────────────────┤                     │
  │<──────────────────────┤                    │                     │
  │  {orders[]}           │                    │                     │
```

---

## 📦 LUỒNG QUẢN LÝ SẢN PHẨM

### 1. Get Products (Public)

```
Client                Controller           Service              Repository         Database
  │                       │                    │                     │                 │
  │  GET /api/products?page=0&size=20          │                     │                 │
  ├──────────────────────>│                    │                     │                 │
  │                       │                    │                     │                 │
  │                       │  getProducts()     │                     │                 │
  │                       ├───────────────────>│                     │                 │
  │                       │                    │                     │                 │
  │                       │                    │  findAll(pageable)  │                 │
  │                       │                    ├────────────────────>│                 │
  │                       │                    │                     │  SELECT p.*     │
  │                       │                    │                     │  FROM products p│
  │                       │                    │                     │  LEFT JOIN      │
  │                       │                    │                     │  categories c   │
  │                       │                    │                     │  LEFT JOIN      │
  │                       │                    │                     │  product_variants
  │                       │                    │                     │  LIMIT 20 OFFSET 0
  │                       │                    │                     ├────────────────>│
  │                       │                    │                     │<────────────────┤
  │                       │                    │<────────────────────┤                 │
  │                       │                    │                     │                 │
  │                       │                    │  map to DTO         │                 │
  │                       │                    │                     │                 │
  │                       │<───────────────────┤                     │                 │
  │<──────────────────────┤                    │                     │                 │
  │  {products: [...],    │                    │                     │                 │
  │   totalPages: 1,      │                    │                     │                 │
  │   totalElements: 8}   │                    │                     │                 │
```

**Data Structure:**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "name": "iPhone 15 Pro",
        "description": "...",
        "categoryId": 1,
        "categoryName": "Electronics",
        "status": "ACTIVE",
        "variants": [
          {
            "id": 1,
            "size": 128,
            "color": "Black",
            "price": 999.00,
            "stockQuantity": 50
          }
        ]
      }
    ],
    "totalPages": 1,
    "totalElements": 8
  }
}
```

### 2. Filter Products

```
Client → Controller → Service → Repository → Database
                                    ↓
                          Build Dynamic Query
                          (Specification Pattern)
                                    ↓
                          WHERE clauses:
                          - category_id = ?
                          - price BETWEEN ? AND ?
                          - name LIKE %?%
```

---

## 🛒 LUỒNG GIỎ HÀNG

### 1. Create Cart (Anonymous)

```
Client                Controller           Service              Repository         Database
  │                       │                    │                     │                 │
  │  POST /api/carts      │                    │                     │                 │
  ├──────────────────────>│                    │                     │                 │
  │                       │                    │                     │                 │
  │                       │  createCart()      │                     │                 │
  │                       ├───────────────────>│                     │                 │
  │                       │                    │                     │                 │
  │                       │                    │  create Cart entity │                 │
  │                       │                    │  - generate UUID    │                 │
  │                       │                    │  - set status ACTIVE│                 │
  │                       │                    │  - expires_at = NOW + 24h
  │                       │                    │                     │                 │
  │                       │                    │  save cart          │                 │
  │                       │                    ├────────────────────>│                 │
  │                       │                    │                     │  INSERT carts   │
  │                       │                    │                     ├────────────────>│
  │                       │                    │                     │<────────────────┤
  │                       │                    │<────────────────────┤                 │
  │                       │<───────────────────┤                     │                 │
  │<──────────────────────┤                    │                     │                 │
  │  {cartToken: "uuid",  │                    │                     │                 │
  │   items: [],          │                    │                     │                 │
  │   totalAmount: 0,     │                    │                     │                 │
  │   expiresAt: "..."}   │                    │                     │                 │
```

**Key Points:**
- Cart có UUID token (không cần login)
- Cart expires sau 24 giờ
- Cart có thể link với user sau khi login

### 2. Add Item to Cart

```
Client                Controller           Service              Repository         Database      Redis
  │                       │                    │                     │                 │             │
  │  POST /api/carts/{token}/items             │                     │                 │             │
  │  {productVariantId: 1, quantity: 2}        │                     │                 │             │
  ├──────────────────────>│                    │                     │                 │             │
  │                       │                    │                     │                 │             │
  │                       │  addItem()         │                     │                 │             │
  │                       ├───────────────────>│                     │                 │             │
  │                       │                    │                     │                 │             │
  │                       │                    │  find cart by token │                 │             │
  │                       │                    ├────────────────────>│                 │             │
  │                       │                    │                     │  SELECT carts   │             │
  │                       │                    │                     ├────────────────>│             │
  │                       │                    │                     │<────────────────┤             │
  │                       │                    │<────────────────────┤                 │             │
  │                       │                    │                     │                 │             │
  │                       │                    │  check stock        │                 │             │
  │                       │                    ├────────────────────>│                 │             │
  │                       │                    │                     │  SELECT stock   │             │
  │                       │                    │                     │  FROM variants  │             │
  │                       │                    │                     │  WHERE id = ?   │             │
  │                       │                    │                     ├────────────────>│             │
  │                       │                    │                     │<────────────────┤             │
  │                       │                    │<────────────────────┤                 │             │
  │                       │                    │                     │                 │             │
  │                       │                    │  if (stock >= qty)  │                 │             │
  │                       │                    │    create cart_item │                 │             │
  │                       │                    ├────────────────────>│                 │             │
  │                       │                    │                     │  INSERT cart_items            │
  │                       │                    │                     ├────────────────>│             │
  │                       │                    │                     │<────────────────┤             │
  │                       │                    │<────────────────────┤                 │             │
  │                       │                    │                     │                 │             │
  │                       │                    │  reserve in Redis   │                 │             │
  │                       │                    │  (soft hold 15min)  │                 │             │
  │                       │                    ├────────────────────────────────────────────────────>│
  │                       │                    │  SET reservation:variant:1:cart:uuid qty TTL 900   │
  │                       │                    │<────────────────────────────────────────────────────┤
  │                       │                    │                     │                 │             │
  │                       │<───────────────────┤                     │                 │             │
  │<──────────────────────┤                    │                     │                 │             │
  │  {cart with items}    │                    │                     │                 │             │
```

**Reservation Logic:**
1. Check stock available
2. Add to cart_items
3. Reserve in Redis (TTL 15 minutes)
4. If checkout → convert to hard reservation
5. If timeout → release reservation

---

## 💳 LUỒNG ĐẶT HÀNG (CHECKOUT)

### Complete Checkout Flow

```
Client          Controller      Service         Reservation       Repository      Database      Redis      Email
  │                 │               │                 │                │              │            │           │
  │  POST /api/checkout             │                 │                │              │            │           │
  ├────────────────>│               │                 │                │              │            │           │
  │  {cartToken,    │               │                 │                │              │            │           │
  │   paymentMethod,│               │                 │                │              │            │           │
  │   address}      │               │                 │                │              │            │           │
  │                 │               │                 │                │              │            │           │
  │                 │  checkout()   │                 │                │              │            │           │
  │                 ├──────────────>│                 │                │              │            │           │
  │                 │               │                 │                │              │            │           │
  │                 │               │  START TRANSACTION              │              │            │           │
  │                 │               ├─────────────────────────────────>│              │            │           │
  │                 │               │                 │                │              │            │           │
  │                 │               │  1. Get cart    │                │              │            │           │
  │                 │               ├─────────────────────────────────>│              │            │           │
  │                 │               │                 │                │  SELECT cart │            │           │
  │                 │               │                 │                │  JOIN items  │            │           │
  │                 │               │                 │                ├─────────────>│            │           │
  │                 │               │                 │                │<─────────────┤            │           │
  │                 │               │<─────────────────────────────────┤              │            │           │
  │                 │               │                 │                │              │            │           │
  │                 │               │  2. Reserve stock (pessimistic lock)            │            │           │
  │                 │               ├─────────────────>│                │              │            │           │
  │                 │               │                 │  For each item:│              │            │           │
  │                 │               │                 ├───────────────>│              │            │           │
  │                 │               │                 │                │  SELECT * FROM variants   │           │
  │                 │               │                 │                │  WHERE id = ? FOR UPDATE  │           │
  │                 │               │                 │                ├─────────────>│            │           │
  │                 │               │                 │                │<─────────────┤            │           │
  │                 │               │                 │                │  (row locked)│            │           │
  │                 │               │                 │                │              │            │           │
  │                 │               │                 │  if (stock >= qty) {          │            │           │
  │                 │               │                 │    UPDATE variants            │            │           │
  │                 │               │                 │    SET stock = stock - qty    │            │           │
  │                 │               │                 │    WHERE id = ?               │            │           │
  │                 │               │                 │  }             │              │            │           │
  │                 │               │                 ├───────────────>│              │            │           │
  │                 │               │                 │                │  UPDATE      │            │           │
  │                 │               │                 │                ├─────────────>│            │           │
  │                 │               │                 │                │<─────────────┤            │           │
  │                 │               │                 │<───────────────┤              │            │           │
  │                 │               │<─────────────────┤                │              │            │           │
  │                 │               │                 │                │              │            │           │
  │                 │               │  3. Create order│                │              │            │           │
  │                 │               ├─────────────────────────────────>│              │            │           │
  │                 │               │                 │                │  INSERT orders            │           │
  │                 │               │                 │                │  INSERT order_items       │           │
  │                 │               │                 │                ├─────────────>│            │           │
  │                 │               │                 │                │<─────────────┤            │           │
  │                 │               │<─────────────────────────────────┤              │            │           │
  │                 │               │                 │                │              │            │           │
  │                 │               │  4. Clear cart  │                │              │            │           │
  │                 │               ├─────────────────────────────────>│              │            │           │
  │                 │               │                 │                │  UPDATE carts│            │           │
  │                 │               │                 │                │  SET status=CHECKED_OUT   │           │
  │                 │               │                 │                ├─────────────>│            │           │
  │                 │               │                 │                │<─────────────┤            │           │
  │                 │               │<─────────────────────────────────┤              │            │           │
  │                 │               │                 │                │              │            │           │
  │                 │               │  5. Clear Redis reservation      │              │            │           │
  │                 │               ├─────────────────────────────────────────────────────────────>│           │
  │                 │               │                 │                │              │  DEL keys  │           │
  │                 │               │<─────────────────────────────────────────────────────────────┤           │
  │                 │               │                 │                │              │            │           │
  │                 │               │  COMMIT TRANSACTION             │              │            │           │
  │                 │               ├─────────────────────────────────>│              │            │           │
  │                 │               │                 │                │  COMMIT      │            │           │
  │                 │               │                 │                ├─────────────>│            │           │
  │                 │               │<─────────────────────────────────┤              │            │           │
  │                 │               │                 │                │              │            │           │
  │                 │               │  6. Send email (async)           │              │            │           │
  │                 │               ├────────────────────────────────────────────────────────────────────────>│
  │                 │               │                 │                │              │            │  Send    │
  │                 │               │                 │                │              │            │  confirmation
  │                 │<──────────────┤                 │                │              │            │           │
  │  {orderId,      │               │                 │                │              │            │           │
  │   orderCode,    │               │                 │                │              │            │           │
  │   trackingToken}│               │                 │                │              │            │           │
```

**Critical Points:**

1. **Transaction Scope**: Toàn bộ checkout trong 1 transaction
2. **Pessimistic Lock**: `SELECT ... FOR UPDATE` để lock stock row
3. **Atomic Update**: Stock update atomic, không race condition
4. **Rollback**: Nếu fail ở bất kỳ step nào → rollback toàn bộ
5. **Email Async**: Gửi email không block response

### Race Condition Scenario

**Scenario:** 2 users cùng checkout sản phẩm cuối cùng

```
User A                  Database                User B
  │                        │                       │
  │  BEGIN TRANSACTION     │                       │
  ├───────────────────────>│                       │
  │                        │                       │
  │  SELECT ... FOR UPDATE │                       │
  │  (stock = 1)           │                       │
  ├───────────────────────>│                       │
  │  ROW LOCKED ✓          │                       │
  │<───────────────────────┤                       │
  │                        │   BEGIN TRANSACTION   │
  │                        │<──────────────────────┤
  │                        │                       │
  │                        │   SELECT ... FOR UPDATE
  │                        │   ⏳ WAITING (blocked) │
  │                        │<──────────────────────┤
  │                        │                       │
  │  UPDATE stock = 0      │                       │
  ├───────────────────────>│                       │
  │                        │                       │
  │  COMMIT                │                       │
  ├───────────────────────>│                       │
  │  ✓ SUCCESS             │                       │
  │                        │                       │
  │                        │   SELECT returns      │
  │                        │   (stock = 0)         │
  │                        ├──────────────────────>│
  │                        │                       │
  │                        │   if (0 < 1) → FALSE  │
  │                        │   ROLLBACK            │
  │                        │<──────────────────────┤
  │                        │   ❌ FAIL: Out of stock
```

**Kết quả:** 
- User A: ✓ Success
- User B: ❌ Out of stock (correct behavior)

---

## 📊 LUỒNG QUẢN LÝ TỒN KHO

### Reservation System Architecture

```
┌──────────────────────────────────────────────────────┐
│                RESERVATION SYSTEM                     │
├──────────────────────────────────────────────────────┤
│                                                       │
│  ┌─────────────────┐          ┌─────────────────┐   │
│  │  Redis Cache    │          │  MySQL DB       │   │
│  │  (Soft Hold)    │          │  (Hard Hold)    │   │
│  └────────┬────────┘          └────────┬────────┘   │
│           │                             │            │
│           │  TTL 15 minutes            │            │
│           │  Auto-expire               │  Permanent │
│           │                             │  (until order)
│           │                             │            │
│  ┌────────▼─────────────────────────────▼────────┐  │
│  │         Reservation Coordinator                │  │
│  │  - Check availability                          │  │
│  │  - Create reservation                          │  │
│  │  - Release on timeout/cancel                   │  │
│  │  - Convert soft → hard (checkout)              │  │
│  └────────────────────────────────────────────────┘  │
└──────────────────────────────────────────────────────┘
```

### Reservation States

```
┌──────────────┐
│  Available   │ stock_quantity = 100
└──────┬───────┘
       │ Add to cart
       ▼
┌──────────────┐
│ Soft Reserve │ Redis: reserved = 2 (TTL 15min)
│ (In Cart)    │ MySQL: stock_quantity = 100 (unchanged)
└──────┬───────┘
       │ Checkout
       ▼
┌──────────────┐
│ Hard Reserve │ MySQL: stock_quantity = 98
│ (In Order)   │ Redis: clear
└──────┬───────┘
       │ Complete/Cancel
       ▼
┌──────────────┐
│   Released   │ stock_quantity adjusted
└──────────────┘
```

### Stock Check Flow

```
Client              Service           Redis           Database
  │                    │                 │                │
  │  Check stock       │                 │                │
  ├───────────────────>│                 │                │
  │                    │                 │                │
  │                    │  1. Get DB stock│                │
  │                    ├─────────────────────────────────>│
  │                    │                 │  SELECT stock  │
  │                    │                 │  FROM variants │
  │                    │<─────────────────────────────────┤
  │                    │  stock = 100    │                │
  │                    │                 │                │
  │                    │  2. Get reserved from Redis      │
  │                    ├────────────────>│                │
  │                    │                 │  GET all keys  │
  │                    │                 │  reservation:* │
  │                    │<────────────────┤                │
  │                    │  reserved = 10  │                │
  │                    │                 │                │
  │                    │  3. Calculate   │                │
  │                    │  available = stock - reserved    │
  │                    │  available = 100 - 10 = 90       │
  │                    │                 │                │
  │<───────────────────┤                 │                │
  │  available: 90     │                 │                │
```

---

## 👨‍💼 LUỒNG ADMIN

### Update Order Status Flow

```
Admin               Controller         Service           Repository        Database
  │                     │                  │                  │                 │
  │  PATCH /api/admin/orders/{id}/status   │                  │                 │
  │  Authorization: Bearer <admin-token>   │                  │                 │
  ├────────────────────>│                  │                  │                 │
  │  {status: "SHIPPED"}│                  │                  │                 │
  │                     │                  │                  │                 │
  │                     │  @PreAuthorize("hasRole('ADMIN')")  │                 │
  │                     │  check role      │                  │                 │
  │                     │  ✓ ADMIN         │                  │                 │
  │                     │                  │                  │                 │
  │                     │  updateStatus()  │                  │                 │
  │                     ├─────────────────>│                  │                 │
  │                     │                  │                  │                 │
  │                     │                  │  find order      │                 │
  │                     │                  ├─────────────────>│                 │
  │                     │                  │                  │  SELECT orders  │
  │                     │                  │                  ├────────────────>│
  │                     │                  │                  │<────────────────┤
  │                     │                  │<─────────────────┤                 │
  │                     │                  │                  │                 │
  │                     │                  │  validate state  │                 │
  │                     │                  │  transition:     │                 │
  │                     │                  │  CONFIRMED → SHIPPED ✓             │
  │                     │                  │                  │                 │
  │                     │                  │  update status   │                 │
  │                     │                  ├─────────────────>│                 │
  │                     │                  │                  │  UPDATE orders  │
  │                     │                  │                  │  SET status=... │
  │                     │                  │                  ├────────────────>│
  │                     │                  │                  │<────────────────┤
  │                     │                  │<─────────────────┤                 │
  │                     │<─────────────────┤                  │                 │
  │<────────────────────┤                  │                  │                 │
  │  {success: true}    │                  │                  │                 │
```

**Valid State Transitions:**
```
PENDING → CONFIRMED → SHIPPED → DELIVERED
PENDING → CANCELLED
```

---

## 📦 LUỒNG TRACKING

### Public Tracking Flow (No Auth)

```
Anyone              Controller         Service           Repository        Database
  │                     │                  │                  │                 │
  │  GET /api/tracking/{token}             │                  │                 │
  ├────────────────────>│                  │                  │                 │
  │                     │                  │                  │                 │
  │                     │  NO AUTH REQUIRED│                  │                 │
  │                     │                  │                  │                 │
  │                     │  trackOrder()    │                  │                 │
  │                     ├─────────────────>│                  │                 │
  │                     │                  │                  │                 │
  │                     │                  │  find by token   │                 │
  │                     │                  ├─────────────────>│                 │
  │                     │                  │                  │  SELECT orders  │
  │                     │                  │                  │  JOIN items     │
  │                     │                  │                  │  WHERE tracking_token=?
  │                     │                  │                  ├────────────────>│
  │                     │                  │                  │<────────────────┤
  │                     │                  │<─────────────────┤                 │
  │                     │                  │                  │                 │
  │                     │                  │  build timeline  │                 │
  │                     │                  │  (status history)│                 │
  │                     │                  │                  │                 │
  │                     │<─────────────────┤                  │                 │
  │<────────────────────┤                  │                  │                 │
  │  {orderCode, status,│                  │                  │                 │
  │   items, timeline}  │                  │                  │                 │
```

**Response:**
```json
{
  "orderCode": "ORD-2026012100001",
  "status": "SHIPPED",
  "totalMoney": 1998.00,
  "shippingAddress": "123 Main St",
  "items": [...],
  "timeline": [
    {"status": "PENDING", "timestamp": "2026-01-21T10:00:00"},
    {"status": "CONFIRMED", "timestamp": "2026-01-21T11:00:00"},
    {"status": "SHIPPED", "timestamp": "2026-01-21T14:00:00"}
  ]
}
```

---

## 📐 DATA MODELS

### Entity Relationships

```
┌─────────────┐         ┌──────────────┐
│    User     │1      * │    Order     │
│─────────────│◄────────┤──────────────│
│ id (PK)     │         │ id (PK)      │
│ email       │         │ user_id (FK) │
│ password    │         │ order_code   │
│ role_id (FK)│         │ status       │
└──────┬──────┘         │ total_money  │
       │                └──────┬───────┘
       │ *                     │
       │                       │ 1
       │                       │
       │                       │ *
       │                ┌──────▼───────┐
       │                │  OrderItem   │
       │                │──────────────│
       │                │ id (PK)      │
       │                │ order_id (FK)│
       │                │ sku_id (FK)  │
       │                │ quantity     │
       │                │ price        │
       │                └──────────────┘
       │
       │ 1
       ▼ *
┌─────────────┐
│    Cart     │
│─────────────│
│ id (PK)     │         ┌──────────────────┐
│ cart_token  │1      * │   CartItem       │
│ user_id (FK)│◄────────┤──────────────────│
│ status      │         │ id (PK)          │
│ expires_at  │         │ cart_id (FK)     │
└─────────────┘         │ variant_id (FK)  │
                        │ quantity         │
                        └────────┬─────────┘
                                 │ *
                                 │
                                 │ 1
                          ┌──────▼──────────┐
                          │ ProductVariant  │
                          │─────────────────│
                          │ id (PK)         │
                          │ product_id (FK) │
                          │ size            │
                          │ color           │
                          │ price           │
                          │ stock_quantity  │
                          └────────┬────────┘
                                   │ *
                                   │
                                   │ 1
                            ┌──────▼──────┐
                            │   Product   │
                            │─────────────│
                            │ id (PK)     │
                            │ name        │
                            │ category_id │
                            │ status      │
                            └─────────────┘
```

### Database Schema

```sql
-- Users & Roles
users (
    id BIGINT PK AUTO_INCREMENT,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(255),
    role_id BIGINT FK → roles(id),
    status ENUM('ACTIVE','INACTIVE'),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
)

roles (
    id BIGINT PK AUTO_INCREMENT,
    name VARCHAR(50) UNIQUE,
    description TEXT
)

-- Products
products (
    id BIGINT PK AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    category_id BIGINT FK → categories(id),
    status ENUM('ACTIVE','INACTIVE'),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
)

product_variants (
    id BIGINT PK AUTO_INCREMENT,
    product_id BIGINT FK → products(id),
    size INTEGER,
    color VARCHAR(50),
    price DECIMAL(10,2) NOT NULL,
    stock_quantity INTEGER NOT NULL,
    status ENUM('ACTIVE','INACTIVE'),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    INDEX idx_product_stock (product_id, stock_quantity)
)

-- Carts
carts (
    id BIGINT PK AUTO_INCREMENT,
    cart_token VARCHAR(255) UNIQUE NOT NULL,
    user_id BIGINT FK → users(id),
    status ENUM('ACTIVE','CHECKED_OUT','EXPIRED'),
    expires_at TIMESTAMP,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    INDEX idx_token (cart_token),
    INDEX idx_expires (expires_at)
)

cart_items (
    id BIGINT PK AUTO_INCREMENT,
    cart_id BIGINT FK → carts(id),
    product_variant_id BIGINT FK → product_variants(id),
    quantity INTEGER NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
)

-- Orders
orders (
    id BIGINT PK AUTO_INCREMENT,
    order_code VARCHAR(50) UNIQUE NOT NULL,
    user_id BIGINT FK → users(id),
    status ENUM('PENDING','CONFIRMED','SHIPPED','DELIVERED','CANCELLED'),
    payment_method ENUM('COD','BANK_TRANSFER'),
    total_money DECIMAL(12,2) NOT NULL,
    currency VARCHAR(10) DEFAULT 'VND',
    shipping_address TEXT NOT NULL,
    tracking_token VARCHAR(255) UNIQUE,
    created_at TIMESTAMP,
    paid_at TIMESTAMP,
    INDEX idx_tracking (tracking_token),
    INDEX idx_user_status (user_id, status),
    INDEX idx_status_created (status, created_at)
)

order_items (
    id BIGINT PK AUTO_INCREMENT,
    order_id BIGINT FK → orders(id),
    sku_id BIGINT FK → product_variants(id),
    price_checkout DECIMAL(10,2) NOT NULL,
    quantity INTEGER NOT NULL
)
```

---

## 🔒 RACE CONDITION HANDLING

### Problem: Last Item Scenario

**Setup:**
- Product có 1 item cuối cùng
- User A và User B cùng lúc checkout

### Solution 1: Pessimistic Locking (IMPLEMENTED)

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT v FROM ProductVariant v WHERE v.id = :id")
Optional<ProductVariant> findByIdWithLock(@Param("id") Long id);
```

**Flow:**
```sql
-- User A's Transaction
BEGIN;
SELECT * FROM product_variants WHERE id = 1 FOR UPDATE;
-- Row locked, User B must wait

UPDATE product_variants 
SET stock_quantity = stock_quantity - 1 
WHERE id = 1 AND stock_quantity >= 1;
-- Success: 1 row updated

COMMIT;
-- Lock released

-- User B's Transaction (now can proceed)
BEGIN;
SELECT * FROM product_variants WHERE id = 1 FOR UPDATE;
-- Returns stock_quantity = 0

UPDATE product_variants 
SET stock_quantity = stock_quantity - 1 
WHERE id = 1 AND stock_quantity >= 1;
-- Fail: 0 rows updated (stock < 1)

ROLLBACK;
```

### Solution 2: Optimistic Locking (Alternative)

```java
@Version
private Long version;

// Hibernate will generate:
UPDATE product_variants 
SET stock_quantity = ?, version = version + 1
WHERE id = ? AND version = ?;
```

**If conflict:** `OptimisticLockException` → Retry or fail

### Solution 3: Atomic Updates

```java
@Query("UPDATE ProductVariant v " +
       "SET v.stockQuantity = v.stockQuantity - :quantity " +
       "WHERE v.id = :id AND v.stockQuantity >= :quantity")
@Modifying
int decrementStock(@Param("id") Long id, @Param("quantity") Integer quantity);
```

Returns:
- `1` = success (row updated)
- `0` = fail (insufficient stock)

---

## 🎯 PERFORMANCE OPTIMIZATIONS

### 1. Database Indexing

```sql
-- Fast product lookup
CREATE INDEX idx_product_status ON products(status);
CREATE INDEX idx_product_category ON products(category_id);

-- Fast variant stock check
CREATE INDEX idx_variant_stock ON product_variants(product_id, stock_quantity);

-- Fast order queries
CREATE INDEX idx_order_user ON orders(user_id, created_at DESC);
CREATE INDEX idx_order_status ON orders(status, created_at DESC);
CREATE INDEX idx_order_tracking ON orders(tracking_token);

-- Fast cart lookup
CREATE INDEX idx_cart_token ON carts(cart_token);
CREATE INDEX idx_cart_expires ON carts(expires_at);
```

### 2. Redis Caching

**Cached Data:**
- Reservations (TTL 15 minutes)
- Session data
- Rate limiting counters

**Cache Pattern:**
```java
// Try cache first
String cached = redis.get("product:" + id);
if (cached != null) {
    return deserialize(cached);
}

// Cache miss → query DB
Product product = repository.findById(id);

// Store in cache
redis.setex("product:" + id, 3600, serialize(product));

return product;
```

### 3. Connection Pooling

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
```

### 4. Async Processing

```java
@Async
public void sendOrderConfirmation(Order order) {
    // Email sending doesn't block checkout response
    emailService.send(order.getUser().getEmail(), order);
}
```

---

## 📊 MONITORING & METRICS

### Actuator Endpoints

```
GET /actuator/health          → Application health
GET /actuator/metrics         → JVM metrics
GET /actuator/prometheus      → Prometheus format metrics
```

### Key Metrics

- **Request Rate:** requests/second
- **Response Time:** p50, p95, p99
- **Error Rate:** 4xx, 5xx responses
- **DB Connection Pool:** active, idle, waiting
- **Redis Connection:** active connections
- **Stock Operations:** checkout success/failure rate

---

## 🚨 ERROR HANDLING

### Exception Hierarchy

```
RuntimeException
├── ResourceNotFoundException (404)
├── BadRequestException (400)
├── InsufficientStockException (409)
├── UnauthorizedException (401)
└── ForbiddenException (403)
```

### Global Exception Handler

```java
@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(404).body(
            ApiResponse.error(ex.getMessage())
        );
    }
    
    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<?> handleStockIssue(InsufficientStockException ex) {
        return ResponseEntity.status(409).body(
            ApiResponse.error("Out of stock: " + ex.getMessage())
        );
    }
}
```

### Error Response Format

```json
{
  "success": false,
  "message": "Product not found",
  "data": null,
  "timestamp": "2026-01-21T10:00:00"
}
```

---

## 🔄 BACKGROUND JOBS

### Scheduled Tasks

```java
@Scheduled(fixedDelay = 300000) // 5 minutes
public void cleanupExpiredReservations() {
    // 1. Find expired carts
    List<Cart> expiredCarts = cartRepository.findExpired();
    
    // 2. Release reservations in Redis
    for (Cart cart : expiredCarts) {
        reservationService.releaseReservations(cart);
    }
    
    // 3. Update cart status
    cartRepository.markExpired(expiredCarts);
}
```

### Cart Expiration Flow

```
Scheduler               Service              Redis               Database
   │                       │                    │                     │
   │ Every 5 minutes       │                    │                     │
   ├──────────────────────>│                    │                     │
   │                       │                    │                     │
   │                       │  Find expired carts│                     │
   │                       ├────────────────────────────────────────>│
   │                       │                    │  SELECT * FROM carts│
   │                       │                    │  WHERE expires_at < NOW
   │                       │<────────────────────────────────────────┤
   │                       │  expired = [cart1, cart2]                │
   │                       │                    │                     │
   │                       │  For each cart:    │                     │
   │                       │  Release reservations                    │
   │                       ├───────────────────>│                     │
   │                       │                    │  DEL reservation:*  │
   │                       │<───────────────────┤                     │
   │                       │                    │                     │
   │                       │  Update cart status│                     │
   │                       ├────────────────────────────────────────>│
   │                       │                    │  UPDATE carts       │
   │                       │                    │  SET status='EXPIRED'
   │                       │<────────────────────────────────────────┤
```

---

## 📝 SUMMARY

### Key Design Decisions

1. **JWT Authentication** - Stateless, scalable
2. **Pessimistic Locking** - Prevent race conditions
3. **Redis Reservations** - Fast, temporary holds
4. **Atomic Updates** - SQL-level guarantees
5. **Async Email** - Non-blocking operations
6. **Role-Based Access** - Security segregation
7. **Public Tracking** - Customer convenience

### Data Flow Principles

1. **Validate Early** - Input validation at controller
2. **Fail Fast** - Check constraints before processing
3. **Transactional** - ACID compliance for critical operations
4. **Idempotent** - Safe to retry
5. **Asynchronous** - Non-critical tasks don't block
6. **Monitored** - Metrics for all operations

### Scalability Considerations

- **Horizontal Scaling**: Stateless application (JWT)
- **Database**: Read replicas for heavy queries
- **Cache**: Redis cluster for high availability
- **Load Balancer**: Distribute traffic across instances
- **CDN**: Static assets (if frontend added)

---

**END OF DATA FLOW ANALYSIS**

**Version:** 2.0  
**Last Updated:** January 21, 2026  
**Status:** ✅ Complete

