# 📚 Postman Testing Documentation

## Tài liệu hướng dẫn test API với Postman

### 🚀 Bắt đầu nhanh
**File:** [`POSTMAN-QUICKSTART.md`](./POSTMAN-QUICKSTART.md)
- Hướng dẫn từng bước test API
- 29 endpoints với ví dụ cụ thể
- Body templates cho từng request
- Sample data (SKU IDs, test accounts)
- FAQ & Troubleshooting

**Dành cho:** Người mới, cần hướng dẫn chi tiết từng bước

---

### 📋 Reference nhanh
**File:** [`API-ENDPOINTS-REFERENCE.md`](./API-ENDPOINTS-REFERENCE.md)
- Bảng tổng hợp 29 endpoints
- Phân loại theo auth level (Public/Authenticated/Admin)
- Request body templates
- Variables reference
- HTTP status codes
- Common use cases

**Dành cho:** Tra cứu nhanh, reference card

---

### 🔧 Chi tiết các fixes
**File:** [`POSTMAN-FIXES.md`](./POSTMAN-FIXES.md)
- 7 vấn đề đã được fix
- Code changes chi tiết
- Before/After comparison
- Migration notes
- Breaking changes

**Dành cho:** Developers, cần hiểu technical details

---

### 📊 Tổng hợp thay đổi
**File:** [`CHANGES-SUMMARY.md`](./CHANGES-SUMMARY.md)
- Tổng quan tất cả thay đổi
- Files modified/created
- Testing impact
- Data consistency
- Verification checklist

**Dành cho:** Project managers, code reviewers

---

## 📦 Files trong package

```
e-commerce/
├── postman-collection.json          # Postman collection (import vào Postman)
├── init-data.sql                    # Database sample data
├── start-app.bat                    # Script tự động start app
├── POSTMAN-QUICKSTART.md           # ⭐ Hướng dẫn test từng bước
├── API-ENDPOINTS-REFERENCE.md      # ⭐ Bảng tra cứu endpoints
├── POSTMAN-FIXES.md                # Chi tiết technical fixes
├── CHANGES-SUMMARY.md              # Tổng hợp thay đổi
└── README-POSTMAN.md               # File này (index)
```

---

## 🎯 Workflow đề xuất

### Lần đầu tiên (Setup)
1. ✅ Đọc [`POSTMAN-QUICKSTART.md`](./POSTMAN-QUICKSTART.md) - Phần "Chuẩn bị"
2. ✅ Run `start-app.bat` để start application
3. ✅ Import `postman-collection.json` vào Postman
4. ✅ Run `init-data.sql` trong MySQL
5. ✅ Test flow đầu tiên: Login → Products → Cart → Checkout

### Testing hàng ngày
1. ✅ Mở [`API-ENDPOINTS-REFERENCE.md`](./API-ENDPOINTS-REFERENCE.md) để tra cứu
2. ✅ Dùng Postman collection để test
3. ✅ Tham khảo [`POSTMAN-QUICKSTART.md`](./POSTMAN-QUICKSTART.md) khi cần examples

### Khi gặp lỗi
1. ✅ Kiểm tra FAQ trong [`POSTMAN-QUICKSTART.md`](./POSTMAN-QUICKSTART.md)
2. ✅ Đọc troubleshooting guide
3. ✅ Xem [`POSTMAN-FIXES.md`](./POSTMAN-FIXES.md) nếu lỗi liên quan đến backend

---

## 🔑 Quick Reference

### Test Accounts
```
Customer: customer@example.com / password123
Admin:    admin@ecommerce.com / password123
```

### Popular SKU IDs
```
1  - iPhone 15 Pro 128GB Black ($999)
15 - Nike Air Max size 40 Red ($150)
23 - Levi's Jeans size 30 Blue ($79.99)
```

### Variables (tự động lưu)
```
{{accessToken}}    - Customer login token
{{adminToken}}     - Admin login token
{{cartToken}}      - Cart identifier
{{orderId}}        - Order ID sau checkout
{{trackingToken}}  - Tracking token để track order
```

### Common Request Bodies

**Login:**
```json
{"email": "customer@example.com", "password": "password123"}
```

**Add to Cart:**
```json
{"skuId": 1, "quantity": 2}
```

**Checkout:**
```json
{
  "cartToken": "{{cartToken}}",
  "paymentMethod": "COD",
  "shippingAddress": "123 Street",
  "idempotencyKey": "{{$guid}}"
}
```

---

## 📞 Support

### Lỗi thường gặp & Giải pháp

| Lỗi | File tham khảo | Section |
|-----|----------------|---------|
| Token không tự động lưu | POSTMAN-QUICKSTART.md | FAQ → Q: Token không tự động lưu? |
| skuId is required | POSTMAN-QUICKSTART.md | FAQ → Q: Lỗi "skuId is required"? |
| idempotencyKey is required | POSTMAN-FIXES.md | Fix #4: Checkout Request |
| Categories trả về 401 | POSTMAN-FIXES.md | Fix #2: Categories Endpoint |
| BANK_TRANSFER not valid | POSTMAN-FIXES.md | Fix #1: PaymentMethod Enum |
| Get My Orders empty | POSTMAN-FIXES.md | Fix #7: Sample Order Data |

---

## ✅ Checklist trước khi test

- [ ] Application running (`netstat -ano | findstr :8080`)
- [ ] Database có data (`init-data.sql` đã run)
- [ ] Postman collection đã import
- [ ] baseUrl = `http://localhost:8080`
- [ ] Đã test Login Customer → có token

**Nếu tất cả ✅ → Ready to test! 🎉**

---

## 📝 Updates Log

### Version 2.0.0 (Current)
- ✅ 7 fixes applied (PaymentMethod, Categories, Validate Token, etc.)
- ✅ 29 endpoints documented
- ✅ Postman collection updated (skuId, idempotencyKey)
- ✅ Sample order data added
- ✅ Get My Orders endpoint implemented
- ✅ Comprehensive documentation

---

**Happy Testing! 🚀**

*Last updated: January 21, 2026*

