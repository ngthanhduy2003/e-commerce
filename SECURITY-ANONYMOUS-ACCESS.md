# 🔐 SecurityConfig - Anonymous Access Explained

## ✅ Câu trả lời: Anonymous users VẪN xem được products & categories!

### 📋 Cấu hình Security (Sau khi update):

```java
.authorizeHttpRequests(auth -> auth
    // Public authentication endpoints
    .requestMatchers("/api/auth/register", "/api/auth/login", "/api/auth/refresh").permitAll()
    
    // ⚠️ Token validation - REQUIRES authentication
    .requestMatchers("/api/auth/validate").authenticated()
    
    // ✅ Public browsing endpoints (anonymous users CAN access)
    .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
    .requestMatchers(HttpMethod.GET, "/api/categories/**").permitAll()
    .requestMatchers(HttpMethod.POST, "/api/carts").permitAll()
    .requestMatchers(HttpMethod.GET, "/api/tracking/**").permitAll()

    // Admin endpoints
    .requestMatchers("/api/admin/**").hasRole("ADMIN")

    // Authenticated user endpoints
    .requestMatchers("/api/checkout/**").authenticated()
    .requestMatchers("/api/orders/my").authenticated()
    .requestMatchers("/api/carts/**").authenticated()

    // All other requests require authentication
    .anyRequest().authenticated()
);
```

---

## 🎯 Access Control Matrix

| Endpoint | Method | Anonymous? | Login Required? | Admin Only? |
|----------|--------|------------|-----------------|-------------|
| **Authentication** |
| `/api/auth/register` | POST | ✅ Yes | ❌ No | ❌ No |
| `/api/auth/login` | POST | ✅ Yes | ❌ No | ❌ No |
| `/api/auth/refresh` | POST | ✅ Yes | ❌ No | ❌ No |
| `/api/auth/validate` | GET | ❌ **No** | ✅ **Yes** | ❌ No |
| **Public Browsing** |
| `/api/products` | GET | ✅ Yes | ❌ No | ❌ No |
| `/api/products/{id}` | GET | ✅ Yes | ❌ No | ❌ No |
| `/api/categories` | GET | ✅ Yes | ❌ No | ❌ No |
| `/api/categories/{id}` | GET | ✅ Yes | ❌ No | ❌ No |
| `/api/tracking/{token}` | GET | ✅ Yes | ❌ No | ❌ No |
| **Cart (Create only)** |
| `/api/carts` | POST | ✅ Yes | ❌ No | ❌ No |
| `/api/carts/{token}` | GET | ❌ No | ✅ Yes | ❌ No |
| `/api/carts/{token}/items` | POST | ❌ No | ✅ Yes | ❌ No |
| **Checkout & Orders** |
| `/api/checkout` | POST | ❌ No | ✅ Yes | ❌ No |
| `/api/orders/my` | GET | ❌ No | ✅ Yes | ❌ No |
| `/api/orders/{id}` | GET | ❌ No | ✅ Yes | ❌ No |
| `/api/orders/{id}/cancel` | PATCH | ❌ No | ✅ Yes | ❌ No |
| **Admin Panel** |
| `/api/admin/orders` | GET | ❌ No | ✅ Yes | ✅ **Yes** |
| `/api/admin/orders/{id}/status` | PATCH | ❌ No | ✅ Yes | ✅ **Yes** |

---

## 🛍️ Typical User Journey

### **Anonymous User (Không login):**

```
✅ Browse products          GET /api/products
✅ View product details     GET /api/products/1
✅ Browse categories        GET /api/categories
✅ Filter by category       GET /api/products?categoryId=1
✅ Search products          GET /api/products?search=shoes
✅ Create cart              POST /api/carts
✅ Track public order       GET /api/tracking/{token}

❌ Add items to cart        POST /api/carts/{token}/items  ← Need login
❌ Checkout                 POST /api/checkout              ← Need login
❌ View my orders           GET /api/orders/my              ← Need login
❌ Validate token           GET /api/auth/validate          ← Need login
```

### **Logged-in Customer:**

```
✅ Everything anonymous can do PLUS:
✅ Add items to cart        POST /api/carts/{token}/items
✅ Update cart items        PATCH /api/carts/{token}/items/{id}
✅ Remove cart items        DELETE /api/carts/{token}/items/{id}
✅ Checkout                 POST /api/checkout
✅ View my orders           GET /api/orders/my
✅ View order details       GET /api/orders/{id}
✅ Cancel order             PATCH /api/orders/{id}/cancel
✅ Validate token           GET /api/auth/validate

❌ Admin operations         /api/admin/**                   ← Need ADMIN role
```

### **Admin:**

```
✅ Everything customer can do PLUS:
✅ View all orders          GET /api/admin/orders
✅ Filter orders            GET /api/admin/orders?status=PENDING
✅ Update order status      PATCH /api/admin/orders/{id}/status
```

---

## 🔍 Tại sao `/api/auth/validate` cần authentication?

### **Trước đây (Sai):**
```json
// Anonymous user call /api/auth/validate
{
  "success": true,
  "message": "Token is valid",
  "data": {
    "valid": true,
    "username": "anonymousUser",
    "authorities": [{"authority": "ROLE_ANONYMOUS"}]
  }
}
```
❌ **Vấn đề:** Luôn trả `valid: true` ngay cả khi không có token!

### **Sau khi fix (Đúng):**
```json
// Anonymous user call /api/auth/validate
HTTP 401 Unauthorized
{
  "success": false,
  "message": "Unauthorized",
  "data": null
}

// Logged-in user call /api/auth/validate
HTTP 200 OK
{
  "success": true,
  "message": "Token is valid",
  "data": {
    "valid": true,
    "userId": 1,
    "email": "customer@example.com",
    "fullName": "John Doe",
    "role": "CUSTOMER"
  }
}
```
✅ **Đúng:** Chỉ trả `valid: true` khi có token hợp lệ!

---

## 🎯 Design Philosophy

### **Public endpoints (permitAll):**
- **Browse products** → Khách hàng cần xem sản phẩm trước khi quyết định mua
- **View categories** → Giúp người dùng lọc sản phẩm
- **Create cart** → Cho phép "Add to cart" ngay cả khi chưa login
- **Track order** → Link tracking công khai, không cần login

### **Authenticated endpoints:**
- **Add to cart** → Tránh spam, đảm bảo mỗi cart có owner
- **Checkout** → Cần thông tin user để tạo order
- **View orders** → Chỉ user mới xem được orders của mình
- **Validate token** → Chỉ có ý nghĩa khi có token

### **Admin only:**
- **Manage orders** → Chỉ admin mới có quyền thay đổi order status
- **View all orders** → Bảo mật thông tin khách hàng

---

## 🔒 Security Best Practices

### ✅ Làm đúng:
1. **Public browsing** → Products & categories public
2. **Authentication for actions** → Cart operations, checkout require login
3. **Validate endpoint protected** → Chỉ validate khi có token
4. **Admin endpoints protected** → hasRole("ADMIN")
5. **Public tracking** → Link công khai, user-friendly

### ❌ Tránh:
1. **Tất cả endpoints public** → Không bảo mật
2. **Products require login** → Mất khách hàng (phải login mới xem)
3. **Validate always returns true** → Vô nghĩa
4. **Admin endpoints chỉ dùng JWT** → Cần check role

---

## 📊 Response Codes

| Scenario | HTTP Code | Response |
|----------|-----------|----------|
| Anonymous view products | 200 OK | ✅ Product list |
| Anonymous view categories | 200 OK | ✅ Category list |
| Anonymous add to cart | 401 Unauthorized | ❌ Login required |
| Anonymous validate token | 401 Unauthorized | ❌ Unauthorized |
| Logged-in user checkout | 201 Created | ✅ Order created |
| Customer access admin | 403 Forbidden | ❌ Access denied |
| Expired token | 401 Unauthorized | ❌ Token expired |

---

## 🚀 Testing Guide

### **Test Anonymous Access:**
```bash
# ✅ Should work
curl http://localhost:8080/api/products
curl http://localhost:8080/api/categories
curl -X POST http://localhost:8080/api/carts
curl http://localhost:8080/api/tracking/track-abc123

# ❌ Should return 401
curl http://localhost:8080/api/auth/validate
curl http://localhost:8080/api/orders/my
curl -X POST http://localhost:8080/api/checkout
```

### **Test in Postman:**

**Without Login:**
1. Get All Products → ✅ Success
2. Get All Categories → ✅ Success
3. Create Cart → ✅ Success
4. Validate Token → ❌ 401 Unauthorized
5. Add Item to Cart → ❌ 401 Unauthorized
6. Checkout → ❌ 401 Unauthorized

**After Login:**
1. Login Customer → ✅ Get tokens
2. Validate Token → ✅ Returns user info
3. Add Item to Cart → ✅ Success
4. Checkout → ✅ Success
5. Get My Orders → ✅ Success

---

## ✅ Summary

### **Sau khi update SecurityConfig:**

1. ✅ **Anonymous users CÓ thể:**
   - Browse products & categories
   - Create cart
   - Track orders (public link)

2. ❌ **Anonymous users KHÔNG thể:**
   - Validate token (401)
   - Add to cart (401)
   - Checkout (401)
   - View orders (401)

3. ✅ **Validate endpoint:**
   - Chỉ work khi có token hợp lệ
   - Anonymous → 401 Unauthorized
   - Logged-in → 200 OK + user info

4. ✅ **Security:**
   - Public browsing enabled
   - Actions require authentication
   - Admin operations protected

**Perfect balance giữa user experience và security!** 🎯🔒

