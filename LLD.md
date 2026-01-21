# LOW-LEVEL DESIGN (LLD)

## E-Commerce Platform - API Documentation & Sequence Diagrams

**Version:** 1.0  
**Last Updated:** January 20, 2026  
**Project:** E-Commerce Platform

---

## 📋 TABLE OF CONTENTS

1. [API Endpoints](#1-api-endpoints)
   - [Product APIs](#11-product-apis)
   - [Cart APIs](#12-cart-apis)
   - [Checkout APIs](#13-checkout-apis)
   - [Order APIs](#14-order-apis)
   - [Tracking APIs](#15-tracking-apis)
   - [Admin APIs](#16-admin-apis)
2. [Sequence Diagrams](#2-sequence-diagrams)
   - [Comprehensive UML 2.x Diagrams](#21-comprehensive-uml-2x-diagrams)
   - [Stock Reservation Flow](#22-stock-reservation-flow)
   - [Checkout Flow](#23-checkout-flow)
   - [Order Cancellation Flow](#24-order-cancellation-flow)
   - [Expired Reservation Cleanup](#25-expired-reservation-cleanup)

---

## 1. API ENDPOINTS

### 1.1 Product APIs

| Method | URL | Description |
|--------|-----|-------------|
| `GET` | `/api/products` | Get list of products with pagination and filters |
| `GET` | `/api/products/{id}` | Get product details by ID |

**Query Parameters for GET /api/products:**
- `categoryId` (optional): Filter by category
- `minPrice` (optional): Minimum price filter
- `maxPrice` (optional): Maximum price filter
- `page` (default: 0): Page number
- `size` (default: 20): Items per page

---

### 1.2 Cart APIs

| Method | URL | Description |
|--------|-----|-------------|
| `POST` | `/api/carts` | Create a new shopping cart |
| `GET` | `/api/carts/{cartToken}` | Get cart details by cart token |
| `POST` | `/api/carts/{cartToken}/items` | Add item to cart |
| `PATCH` | `/api/carts/{cartToken}/items/{itemId}` | Update cart item quantity |
| `DELETE` | `/api/carts/{cartToken}/items/{itemId}` | Remove item from cart |
| `POST` | `/api/carts/{cartToken}/reserve` | Reserve stock for cart items (15 min TTL) |
| `POST` | `/api/carts/{cartToken}/release` | Release stock reservation |

**Request Body for POST /api/carts/{cartToken}/items:**
```json
{
  "skuId": 1,
  "quantity": 2
}
```

**Request Body for PATCH /api/carts/{cartToken}/items/{itemId}:**
```json
{
  "quantity": 3
}
```

---

### 1.3 Checkout APIs

| Method | URL | Description |
|--------|-----|-------------|
| `POST` | `/api/checkout` | Process checkout and create order |

**Request Body for POST /api/checkout:**
```json
{
  "cartToken": "abc-123-xyz",
  "idempotencyKey": "uuid-456-def",
  "paymentMethod": "CREDIT_CARD",
  "shippingAddress": {
    "fullName": "John Doe",
    "phone": "0123456789",
    "address": "123 Main St",
    "city": "Hanoi",
    "district": "Hoan Kiem",
    "ward": "Trang Tien"
  }
}
```

---

### 1.4 Order APIs

| Method | URL | Description |
|--------|-----|-------------|
| `GET` | `/api/orders/{id}` | Get order details by order ID |
| `POST` | `/api/orders/{id}/cancel` | Cancel an order (customer-initiated) |

---

### 1.5 Tracking APIs

| Method | URL | Description |
|--------|-----|-------------|
| `GET` | `/api/tracking/{trackingToken}` | Track order status by tracking token |

---

### 1.6 Admin APIs

| Method | URL | Description |
|--------|-----|-------------|
| `GET` | `/api/admin/orders` | Get list of all orders with pagination |
| `PATCH` | `/api/admin/orders/{id}/status` | Update order status (admin-initiated) |

**Query Parameters for GET /api/admin/orders:**
- `status` (optional): Filter by order status (PENDING, CONFIRMED, SHIPPING, DELIVERED, CANCELLED)
- `page` (default: 0): Page number
- `size` (default: 20): Items per page

**Request Body for PATCH /api/admin/orders/{id}/status:**
```json
{
  "status": "CONFIRMED"
}
```

---

## 2. SEQUENCE DIAGRAMS

### 2.1 Comprehensive UML 2.x Diagrams (Optimized v1.1)

> **📘 COMPLETE UML 2.x SPECIFICATION - OPTIMIZED VERSION** ✨
> 
> **🎯 Main Document:** **[SEQUENCE-DIAGRAMS.md](./SEQUENCE-DIAGRAMS.md)** ⭐  
> Full UML 2.x compliant diagrams with PlantUML source code
>
> **🚀 Quick Start:** **[UML-START-HERE.md](./UML-START-HERE.md)** ⭐  
> Your entry point to all UML documentation
>
> **🎉 NEW in v1.1 - Optimized for Clarity:**
> - ✅ **70% smaller** - Reduced from 1,250 to 370 lines
> - ✅ **8 focused diagrams** - Split from 4 large diagrams
> - ✅ **Easier to read** - Each diagram fits on one screen
> - ✅ **100% compliant** - All requirements (1A-1O) still met
> - ✅ **Better organized** - Clear separation of concerns
>
> **📚 Complete Documentation Suite:**
> - **[SEQUENCE-DIAGRAMS.md](./SEQUENCE-DIAGRAMS.md)** - Main diagrams (8 PlantUML diagrams)
> - **[UML-DIAGRAM-SUMMARY.md](./UML-DIAGRAM-SUMMARY.md)** - Requirements & optimization summary
> - **[HOW-TO-USE-DIAGRAMS.md](./HOW-TO-USE-DIAGRAMS.md)** - Usage guide & rendering
> - **[UML-QUICK-NAV.md](./UML-QUICK-NAV.md)** - Quick navigation
> - **[UML-START-HERE.md](./UML-START-HERE.md)** - Entry point
>
> **📊 Optimization Results:**
> 
> | Flow | Before | After | Diagrams | Reduction |
> |------|--------|-------|----------|-----------|
> | Stock Reservation | 200 lines | 80 lines | 2 | 60% ⬇️ |
> | Checkout | 500 lines | 150 lines | 4 | 70% ⬇️ |
> | Cancellation | 250 lines | 60 lines | 1 | 76% ⬇️ |
> | Cleanup | 300 lines | 80 lines | 1 | 73% ⬇️ |
> | **TOTAL** | **1,250** | **370** | **8** | **70% ⬇️** |
>
> **✅ Full Compliance with Requirements (1A-1O):**
> - ✅ **UML 2.x Standard** - Complete standard compliance
> - ✅ **Actor Notation** - Stick figures for external entities
> - ✅ **Participant/Lifeline** - Dashed vertical lines
> - ✅ **Object Notation** - `:ClassName` format (anonymous objects)
> - ✅ **Activation Bars** - Focus of control visualization
> - ✅ **Message Notation** - Solid (sync), dashed (return)
> - ✅ **Message Numbering** - Hierarchical (1, 1.1, 2.1...)
> - ✅ **Alt Fragments** - Combined fragments with guard conditions
> - ✅ **Guard Conditions** - `[condition]` in square brackets
> - ✅ **Frame Boundaries** - UML-compliant frames for fragments
> - ✅ **Self-Messages** - Objects calling themselves
> - ✅ **UI Boundary** - Boundary-Control-Entity pattern
> - ✅ **Temporal Ordering** - Top-to-bottom time flow
> - ✅ **UML Notes** - Context explanations
> - ✅ **Role Separation** - Strict BCE pattern adherence
>
> **🎨 Features:**
> - 8 focused diagrams across 4 main flows
> - PlantUML format (render in VS Code, online, or CLI)
> - Split complex flows into manageable sub-diagrams
> - Simplified participants (`:Database` instead of multiple repos)
> - Transaction boundaries clearly marked
> - Rollback mechanisms visualized
> - 70% size reduction while maintaining all details
>
> **📖 Quick Access:**
> 1. **Entry Point:** [UML-START-HERE.md](./UML-START-HERE.md) - Start here!
> 2. **View Diagrams:** [SEQUENCE-DIAGRAMS.md](./SEQUENCE-DIAGRAMS.md) - Open in VS Code
> 3. **Get Summary:** [UML-DIAGRAM-SUMMARY.md](./UML-DIAGRAM-SUMMARY.md) - Overview
> 4. **Learn Usage:** [HOW-TO-USE-DIAGRAMS.md](./HOW-TO-USE-DIAGRAMS.md) - How to use
>
> The following sections provide simplified ASCII diagrams for quick reference.

---

### 2.2 Stock Reservation Flow

**Purpose:** Reserve stock temporarily for user during checkout process (15-minute TTL)

```
┌──────┐         ┌──────────┐         ┌─────────────────┐         ┌──────────┐         ┌───────┐
│Client│         │CartCtrl  │         │ReservationService│         │ProductDB │         │ Redis │
└──┬───┘         └────┬─────┘         └────────┬────────┘         └────┬─────┘         └───┬───┘
   │                  │                         │                       │                    │
   │ POST /carts/{token}/reserve                │                       │                    │
   │─────────────────>│                         │                       │                    │
   │                  │                         │                       │                    │
   │                  │ createReservation(cartToken)                    │                    │
   │                  │────────────────────────>│                       │                    │
   │                  │                         │                       │                    │
   │                  │                         │ Find Cart by token    │                    │
   │                  │                         │──────────────────────>│                    │
   │                  │                         │<──────────────────────│                    │
   │                  │                         │  Cart entity          │                    │
   │                  │                         │                       │                    │
   │                  │                         │ Validate: status=ACTIVE, not empty         │
   │                  │                         │──────────────┐        │                    │
   │                  │                         │              │        │                    │
   │                  │                         │<─────────────┘        │                    │
   │                  │                         │                       │                    │
   │                  │                         │ ╔══════════════════════════════════════╗   │
   │                  │                         │ ║ FOR EACH CartItem:                   ║   │
   │                  │                         │ ║                                      ║   │
   │                  │                         │ ║ 1. Check stock availability          ║   │
   │                  │                         │ ║────────────────────────────────────> ║   │
   │                  │                         │ ║ Query: stockQuantity >= requested    ║   │
   │                  │                         │ ║<──────────────────────────────────── ║   │
   │                  │                         │ ║                                      ║   │
   │                  │                         │ ║ 2. Create StockReservation in DB     ║   │
   │                  │                         │ ║────────────────────────────────────> ║   │
   │                  │                         │ ║    status = RESERVED                 ║   │
   │                  │                         │ ║    expiresAt = now + 15 min          ║   │
   │                  │                         │ ║<──────────────────────────────────── ║   │
   │                  │                         │ ║    reservationId                     ║   │
   │                  │                         │ ║                                      ║   │
   │                  │                         │ ║ 3. Store in Redis with TTL           ║   │
   │                  │                         │ ║────────────────────────────────────────> ║
   │                  │                         │ ║ SET reservation:{token}:{skuId}      ║   │
   │                  │                         │ ║     value: reservationId             ║   │
   │                  │                         │ ║     TTL: 15 minutes                  ║   │
   │                  │                         │ ║<──────────────────────────────────────── ║
   │                  │                         │ ╚══════════════════════════════════════╝   │
   │                  │                         │                       │                    │
   │                  │<────────────────────────│                       │                    │
   │                  │  ReservationResponse    │                       │                    │
   │                  │  (success, items[])     │                       │                    │
   │                  │                         │                       │                    │
   │<─────────────────│                         │                       │                    │
   │ 200 OK           │                         │                       │                    │
   │ {                │                         │                       │                    │
   │   "success": true│                         │                       │                    │
   │   "items": [...]│                         │                       │                    │
   │   "expiresAt": "2026-01-20T15:15:00"      │                       │                    │
   │ }                │                         │                       │                    │
   │                  │                         │                       │                    │
```

**Key Points:**
1. **Dual Storage:** Reservation saved in both MySQL (persistent) and Redis (cache + TTL)
2. **Atomicity:** Each SKU reservation is atomic
3. **TTL:** Redis auto-expires keys after 15 minutes
4. **No Stock Decrement:** Stock quantity NOT reduced during reservation (only marked as reserved)

---

### 2.3 Checkout Flow

**Purpose:** Complete purchase, create order, and consume stock reservations

```
┌──────┐    ┌─────────┐    ┌────────────┐    ┌────────────┐    ┌────────┐    ┌───────┐    ┌──────┐
│Client│    │Checkout │    │CartService │    │OrderService│    │Product │    │ Redis │    │Email │
│      │    │Ctrl     │    │            │    │            │    │DB      │    │       │    │Svc   │
└──┬───┘    └────┬────┘    └─────┬──────┘    └─────┬──────┘    └───┬────┘    └───┬───┘    └──┬───┘
   │             │                │                  │                │             │           │
   │ POST /api/checkout           │                  │                │             │           │
   │ (cartToken, idempotencyKey)  │                  │                │             │           │
   │────────────>│                │                  │                │             │           │
   │             │                │                  │                │             │           │
   │             │ ┌──────────────────────────────────────────────┐   │             │           │
   │             │ │ 1. Check Idempotency Key in Redis            │   │             │           │
   │             │ │    Key: "checkout:idempotency:{key}"         │   │             │           │
   │             │ └──────────────────────────────────────────────┘   │             │           │
   │             │                │                  │                │             │           │
   │             │ GET idempotency key               │                │             │           │
   │             │───────────────────────────────────────────────────────────────>│           │
   │             │<───────────────────────────────────────────────────────────────│           │
   │             │  null (not exists) OR orderCode                    │             │           │
   │             │                │                  │                │             │           │
   │             │ ┌───────────────────────────────────┐              │             │           │
   │             │ │ IF EXISTS:                        │              │             │           │
   │             │ │   Return cached order (200 OK)    │              │             │           │
   │             │ └───────────────────────────────────┘              │             │           │
   │             │                │                  │                │             │           │
   │             │ ┌──────────────────────────────────────────────┐   │             │           │
   │             │ │ 2. Load and Validate Cart                    │   │             │           │
   │             │ └──────────────────────────────────────────────┘   │             │           │
   │             │ getCart(cartToken)                 │                │             │           │
   │             │────────────────>│                  │                │             │           │
   │             │                │ Find by token    │                │             │           │
   │             │                │─────────────────────────────────>│             │           │
   │             │                │<─────────────────────────────────│             │           │
   │             │                │  Cart (status=ACTIVE)            │             │           │
   │             │<────────────────│                  │                │             │           │
   │             │  Cart entity   │                  │                │             │           │
   │             │                │                  │                │             │           │
   │             │ ╔══════════════════════════════════════════════════════════════╗ │           │
   │             │ ║ 3. CRITICAL SECTION: Atomic Stock Decrement                  ║ │           │
   │             │ ║                                                                ║ │           │
   │             │ ║ @Transactional                                                 ║ │           │
   │             │ ║ FOR EACH CartItem:                                             ║ │           │
   │             │ ║                                                                ║ │           │
   │             │ ║   UPDATE product_variants                                      ║ │           │
   │             │ ║   SET stock_quantity = stock_quantity - :quantity              ║ │           │
   │             │ ║   WHERE id = :id AND stock_quantity >= :quantity               ║ │           │
   │             │ ║──────────────────────────────────────────────────────────────────>           │
   │             │ ║   affectedRows = ?                                             ║ │           │
   │             │ ║<──────────────────────────────────────────────────────────────────           │
   │             │ ║                                                                ║ │           │
   │             │ ║   IF affectedRows == 0:                                        ║ │           │
   │             │ ║     ROLLBACK previous decrements                               ║ │           │
   │             │ ║──────────────────────────────────────────────────────────────────>           │
   │             │ ║     THROW OutOfStockException                                  ║ │           │
   │             │ ║     [Transaction auto-rollback]                                ║ │           │
   │             │ ╚══════════════════════════════════════════════════════════════╝ │           │
   │             │                │                  │                │             │           │
   │             │ ┌──────────────────────────────────────────────┐   │             │           │
   │             │ │ 4. Calculate Total Amount                    │   │             │           │
   │             │ │    totalMoney = Σ(price × quantity)          │   │             │           │
   │             │ └──────────────────────────────────────────────┘   │             │           │
   │             │                │                  │                │             │           │
   │             │                │ ┌───────────────────────────────────────────┐   │           │
   │             │                │ │ 5. Create Order                           │   │           │
   │             │                │ │    - orderCode (auto-generated)           │   │           │
   │             │                │ │    - status = PENDING                     │   │           │
   │             │                │ │    - totalMoney                           │   │           │
   │             │                │ │    - trackingToken (UUID)                 │   │           │
   │             │                │ │    - OrderItems[]                         │   │           │
   │             │                │ └───────────────────────────────────────────┘   │           │
   │             │ createOrder(cart, request)         │                │             │           │
   │             │────────────────────────────────────>│                │             │           │
   │             │                │                  │ Save Order      │             │           │
   │             │                │                  │────────────────>│             │           │
   │             │                │                  │<────────────────│             │           │
   │             │                │                  │  Order entity   │             │           │
   │             │<────────────────────────────────────│                │             │           │
   │             │  Order entity  │                  │                │             │           │
   │             │                │                  │                │             │           │
   │             │ ┌──────────────────────────────────────────────┐   │             │           │
   │             │ │ 6. Update Cart Status                        │   │             │           │
   │             │ └──────────────────────────────────────────────┘   │             │           │
   │             │ updateCartStatus(CHECKED_OUT)      │                │             │           │
   │             │────────────────>│                  │                │             │           │
   │             │                │ UPDATE cart      │                │             │           │
   │             │                │─────────────────────────────────>│             │           │
   │             │<────────────────│                  │                │             │           │
   │             │                │                  │                │             │           │
   │             │ ┌──────────────────────────────────────────────┐   │             │           │
   │             │ │ 7. Consume Stock Reservations                │   │             │           │
   │             │ │    - Update DB: status = CONSUMED            │   │             │           │
   │             │ │    - Delete from Redis                       │   │             │           │
   │             │ └──────────────────────────────────────────────┘   │             │           │
   │             │ consumeReservations(cartToken)     │                │             │           │
   │             │────────────────>│                  │                │             │           │
   │             │                │ Update reservations              │             │           │
   │             │                │─────────────────────────────────>│             │           │
   │             │                │ DELETE Redis keys│                │             │           │
   │             │                │───────────────────────────────────────────────>│           │
   │             │<────────────────│                  │                │             │           │
   │             │                │                  │                │             │           │
   │             │ ┌──────────────────────────────────────────────┐   │             │           │
   │             │ │ 8. Store Idempotency Key (24h TTL)          │   │             │           │
   │             │ └──────────────────────────────────────────────┘   │             │           │
   │             │ SET checkout:idempotency:{key} = orderCode        │             │           │
   │             │───────────────────────────────────────────────────────────────>│           │
   │             │<───────────────────────────────────────────────────────────────│           │
   │             │                │                  │                │             │           │
   │             │ ┌──────────────────────────────────────────────┐   │             │           │
   │             │ │ 9. Send Order Confirmation Email (Async)     │   │             │           │
   │             │ └──────────────────────────────────────────────┘   │             │           │
   │             │ sendOrderConfirmation(order)       │                │             │           │
   │             │─────────────────────────────────────────────────────────────────────────────>│
   │             │                │                  │                │             │           │
   │             │ (Fire-and-forget, error logged but not thrown)      │             │           │
   │             │                │                  │                │             │           │
   │<────────────│                │                  │                │             │           │
   │ 201 Created │                │                  │                │             │           │
   │ OrderResponse                │                  │                │             │           │
   │ {           │                │                  │                │             │           │
   │   "orderCode": "ORD-123"     │                  │                │             │           │
   │   "status": "PENDING"        │                  │                │             │           │
   │   "trackingToken": "uuid"    │                  │                │             │           │
   │ }           │                │                  │                │             │           │
   │             │                │                  │                │             │           │
```

**Key Points:**
1. **Idempotency:** Prevents duplicate orders from double-clicks/retries
2. **Atomic Stock Decrement:** Database-level WHERE clause ensures no overselling
3. **Rollback on Failure:** Any failure rolls back entire transaction (stock restored)
4. **Transaction Boundary:** All critical operations within single @Transactional method
5. **Async Email:** Email failure doesn't affect order creation

---

### 2.4 Order Cancellation Flow

**Purpose:** Cancel order and restore stock to inventory

```
┌──────┐         ┌──────────┐         ┌────────────┐         ┌─────────────┐
│Client│         │OrderCtrl │         │OrderService│         │ProductDB    │
└──┬───┘         └────┬─────┘         └─────┬──────┘         └──────┬──────┘
   │                  │                      │                       │
   │ POST /api/orders/{id}/cancel            │                       │
   │─────────────────>│                      │                       │
   │                  │                      │                       │
   │                  │ cancelOrder(orderId) │                       │
   │                  │─────────────────────>│                       │
   │                  │                      │                       │
   │                  │                      │ ┌─────────────────────────────┐
   │                  │                      │ │ 1. Find Order by ID         │
   │                  │                      │ └─────────────────────────────┘
   │                  │                      │ Find Order                   │
   │                  │                      │──────────────────────────────>│
   │                  │                      │<──────────────────────────────│
   │                  │                      │  Order entity                 │
   │                  │                      │                       │
   │                  │                      │ ┌─────────────────────────────┐
   │                  │                      │ │ 2. Validate Order Status    │
   │                  │                      │ │    - Not already CANCELLED  │
   │                  │                      │ │    - Not DELIVERED          │
   │                  │                      │ └─────────────────────────────┘
   │                  │                      │──────┐              │
   │                  │                      │      │ Validate     │
   │                  │                      │<─────┘              │
   │                  │                      │                       │
   │                  │                      │ ┌──────────────────────────────────────┐
   │                  │                      │ │ 3. @Transactional                    │
   │                  │                      │ │    FOR EACH OrderItem:               │
   │                  │                      │ │                                      │
   │                  │                      │ │    UPDATE product_variants           │
   │                  │                      │ │    SET stock_quantity =              │
   │                  │                      │ │        stock_quantity + :quantity    │
   │                  │                      │ │    WHERE id = :skuId                 │
   │                  │                      │ └──────────────────────────────────────┘
   │                  │                      │ Restore stock for each item           │
   │                  │                      │──────────────────────────────>│
   │                  │                      │<──────────────────────────────│
   │                  │                      │  Updated rows                 │
   │                  │                      │                       │
   │                  │                      │ ┌─────────────────────────────┐
   │                  │                      │ │ 4. Update Order Status      │
   │                  │                      │ │    status = CANCELLED       │
   │                  │                      │ └─────────────────────────────┘
   │                  │                      │ Update order status           │
   │                  │                      │──────────────────────────────>│
   │                  │                      │<──────────────────────────────│
   │                  │                      │  Updated order                │
   │                  │                      │                       │
   │                  │<─────────────────────│                       │
   │                  │  Success             │                       │
   │                  │                      │                       │
   │<─────────────────│                      │                       │
   │ 200 OK           │                      │                       │
   │ {                │                      │                       │
   │   "success": true│                      │                       │
   │   "message": "Order cancelled"          │                       │
   │ }                │                      │                       │
   │                  │                      │                       │
```

**Key Points:**
1. **Stock Restoration:** All order items' stock quantities are restored to inventory
2. **Atomic Operation:** Stock restoration and status update in single transaction
3. **Validation:** Only non-cancelled and non-delivered orders can be cancelled
4. **Immediate Effect:** Stock becomes available for other customers immediately

---

### 2.5 Expired Reservation Cleanup

**Purpose:** Scheduled task to clean up expired reservations (runs every 5 minutes)

```
┌───────────┐         ┌───────────────────┐         ┌─────────────────┐         ┌───────┐
│Scheduler  │         │ReservationService │         │ProductDB        │         │ Redis │
└─────┬─────┘         └─────────┬─────────┘         └────────┬────────┘         └───┬───┘
      │                         │                             │                      │
      │ @Scheduled(fixedRate = 300000)  // Every 5 minutes    │                      │
      │────┐                    │                             │                      │
      │    │ Trigger            │                             │                      │
      │<───┘                    │                             │                      │
      │                         │                             │                      │
      │ cleanupExpiredReservations()                          │                      │
      │────────────────────────>│                             │                      │
      │                         │                             │                      │
      │                         │ ┌───────────────────────────────────────────────┐  │
      │                         │ │ 1. Query Expired Reservations                 │  │
      │                         │ │    WHERE status = 'RESERVED'                  │  │
      │                         │ │      AND expiresAt < NOW()                    │  │
      │                         │ └───────────────────────────────────────────────┘  │
      │                         │ Find expired reservations                       │  │
      │                         │────────────────────────────────────────────────>│  │
      │                         │<────────────────────────────────────────────────│  │
      │                         │  List<StockReservation>                         │  │
      │                         │                             │                      │
      │                         │ ╔═══════════════════════════════════════════════════════╗
      │                         │ ║ 2. FOR EACH Expired Reservation:                      ║
      │                         │ ║                                                       ║
      │                         │ ║    a. Update Database Status                          ║
      │                         │ ║       status = RELEASED                               ║
      │                         │ ║───────────────────────────────────────────────────────────>
      │                         │ ║<───────────────────────────────────────────────────────────
      │                         │ ║                                                       ║
      │                         │ ║    b. Delete from Redis (cleanup orphan keys)         ║
      │                         │ ║       Key: reservation:{cartToken}:{skuId}            ║
      │                         │ ║───────────────────────────────────────────────────────────>
      │                         │ ║       DEL reservation:...                             ║
      │                         │ ║<───────────────────────────────────────────────────────────
      │                         │ ║       OK (or key already expired)                     ║
      │                         │ ║                                                       ║
      │                         │ ╚═══════════════════════════════════════════════════════╝
      │                         │                             │                      │
      │                         │ ┌───────────────────────────────────────────────┐  │
      │                         │ │ 3. Log Cleanup Results                        │  │
      │                         │ │    "Cleaned up X expired reservations"        │  │
      │                         │ └───────────────────────────────────────────────┘  │
      │                         │                             │                      │
      │<────────────────────────│                             │                      │
      │  Cleanup completed      │                             │                      │
      │                         │                             │                      │
      │                         │                             │                      │
      ┌─────────────────────────────────────────────────────────────────────────┐   │
      │ Timeline Visualization:                                                  │   │
      │                                                                          │   │
      │ T=0min:   User creates reservation → DB: RESERVED, Redis: stored        │   │
      │ T=15min:  Redis TTL expires → Redis: key deleted automatically          │   │
      │ T=20min:  Scheduled cleanup runs → DB: status updated to RELEASED       │   │
      │ T=25min:  Next cleanup (no action, already released)                    │   │
      └─────────────────────────────────────────────────────────────────────────┘   │
      │                         │                             │                      │
```

**Key Points:**
1. **Scheduled Execution:** Runs automatically every 5 minutes
2. **Grace Period:** 5-minute window between Redis expiry and DB cleanup (acceptable delay)
3. **Idempotent:** Safe to run multiple times (already released reservations skipped)
4. **Dual Cleanup:** Updates both database status and removes Redis keys
5. **No Stock Impact:** Reservations don't hold actual stock, just tracking records

---

## 3. ADDITIONAL TECHNICAL DETAILS

### 3.1 Database Schema (Key Tables)

**products**
- `id` (PK)
- `name`
- `description`
- `category_id` (FK)

**product_variants**
- `id` (PK)
- `product_id` (FK)
- `sku`
- `price`
- `stock_quantity` ← **Critical field for concurrency control**

**carts**
- `id` (PK)
- `cart_token` (UUID, Unique)
- `status` (ACTIVE, CHECKED_OUT, ABANDONED)
- `created_at`

**cart_items**
- `id` (PK)
- `cart_id` (FK)
- `sku_id` (FK to product_variants)
- `quantity`

**orders**
- `id` (PK)
- `order_code` (Unique, e.g., "ORD-20260120-ABC123")
- `cart_id` (FK)
- `status` (PENDING, CONFIRMED, SHIPPING, DELIVERED, CANCELLED)
- `tracking_token` (UUID, Unique)
- `payment_method`
- `total_money`
- `shipping_address`
- `created_at`

**order_items**
- `id` (PK)
- `order_id` (FK)
- `sku_id` (FK to product_variants)
- `quantity`
- `price` (Snapshot at order time)

**stock_reservations**
- `id` (PK)
- `cart_token`
- `sku_id` (FK to product_variants)
- `quantity`
- `status` (RESERVED, CONSUMED, RELEASED, EXPIRED)
- `expires_at`
- `created_at`

---

### 3.2 Redis Data Structures

**Reservation Keys**
```
Pattern: reservation:{cartToken}:{skuId}
Type: String
Value: reservationId (Long)
TTL: 15 minutes
Example: reservation:abc-123:456 → "789"
```

**Idempotency Keys**
```
Pattern: checkout:idempotency:{idempotencyKey}
Type: String
Value: orderCode (String)
TTL: 24 hours
Example: checkout:idempotency:uuid-456 → "ORD-20260120-ABC123"
```

---

### 3.3 Concurrency Control Strategies

| Strategy | Implementation | Use Case |
|----------|---------------|----------|
| **Atomic UPDATE** | `UPDATE ... WHERE ... AND stock >= qty` | Stock decrement during checkout |
| **Pessimistic Lock** | `@Lock(PESSIMISTIC_WRITE)` | Admin inventory management |
| **Optimistic Lock** | `@Version` column | Low-contention scenarios |
| **Idempotency Key** | Redis cache with TTL | Prevent duplicate orders |
| **Transaction Isolation** | `@Transactional(READ_COMMITTED)` | Default for all write operations |

---

### 3.4 Error Handling Summary

| Error Scenario | HTTP Status | Response | System Action |
|---------------|-------------|----------|---------------|
| Out of Stock | 409 Conflict | `OutOfStockException` | Rollback transaction, restore stock |
| Cart Not Found | 404 Not Found | `ResourceNotFoundException` | Return error message |
| Invalid Cart Status | 400 Bad Request | `InvalidCartException` | Return error message |
| Duplicate Checkout | 200 OK | Return existing order | No new order created |
| Order Not Found | 404 Not Found | `ResourceNotFoundException` | Return error message |
| Invalid Order Status | 400 Bad Request | `InvalidOrderStatusException` | Return error message |

---

## 4. PERFORMANCE CONSIDERATIONS

### 4.1 Optimization Techniques

1. **Database Indexing**
   - `cart_token` (Unique Index)
   - `order_code` (Unique Index)
   - `tracking_token` (Unique Index)
   - `product_variants.sku` (Unique Index)
   - `stock_reservations(cart_token, sku_id)` (Composite Index)

2. **Redis Caching**
   - Reservation lookups: O(1) time complexity
   - Auto-expiration reduces manual cleanup overhead
   - Idempotency checks: Fast duplicate detection

3. **Pagination**
   - All list endpoints support pagination (default: 20 items/page)
   - Reduces memory usage and response time

4. **Atomic Operations**
   - Single UPDATE statement for stock decrement
   - Eliminates need for distributed locks

### 4.2 Scalability Notes

- **Horizontal Scaling**: Stateless API servers (can add more instances)
- **Database Connection Pool**: Configured via Spring Boot
- **Redis Cluster**: Can be configured for high availability
- **Async Operations**: Email sending doesn't block main flow

---

## 5. SECURITY CONSIDERATIONS

### 5.1 Token Security

- **Cart Token**: UUID v4 (128-bit random, unpredictable)
- **Tracking Token**: UUID v4 (prevents order enumeration attacks)
- **Idempotency Key**: Client-generated UUID (prevents replay attacks)

### 5.2 Input Validation

- All request DTOs use `@Valid` annotation
- Jakarta Validation constraints:
  - `@NotNull`, `@NotBlank`
  - `@Min`, `@Max` for quantities
  - `@Pattern` for phone numbers, addresses

### 5.3 Future Enhancements

- Add authentication/authorization (JWT, OAuth2)
- Rate limiting for API endpoints
- HTTPS enforcement
- CSRF protection for web clients

---

## DOCUMENT METADATA

**Author:** Development Team  
**Version:** 1.0  
**Status:** Approved  
**Date:** January 20, 2026  

**Change Log:**
| Date | Version | Changes |
|------|---------|---------|
| 2026-01-20 | 1.0 | Initial LLD documentation |

---

**END OF DOCUMENT**

