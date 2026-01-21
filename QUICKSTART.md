# Quick Start Guide

## Prerequisites Check
```bash
# Check Java version (need 24+)
java -version

# Check MySQL (need 8+)
mysql --version

# Check Redis
redis-cli ping
# Should return: PONG

# Check Gradle
.\gradlew.bat --version
```

## Setup Steps

### 1. Database Setup
```bash
# Login to MySQL
mysql -u root -p

# Create database
CREATE DATABASE ecommerce CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# Exit MySQL
exit
```

### 2. Configure Application
Edit `src/main/resources/application.yaml`:
```yaml
spring:
  datasource:
    username: root
    password: YOUR_PASSWORD  # Change this!
```

### 3. Start Redis
```bash
# Windows (if Redis is installed)
redis-server

# Or use Docker
docker run -d -p 6379:6379 redis:latest
```

### 4. Build & Run
```bash
# Clean build
.\gradlew.bat clean build

# Run application
.\gradlew.bat bootRun
```

Application starts at: **http://localhost:8080**

### 5. Load Sample Data
```bash
# After application starts and creates tables
mysql -u root -p ecommerce < sample-data.sql
```

## Test the API

### Option 1: Postman
1. Import `postman-collection.json`
2. Run "Create Cart" request
3. Run "Add Item to Cart"
4. Run "Reserve Cart"
5. Run "Checkout"

### Option 2: cURL

**1. Create Cart**
```bash
curl -X POST http://localhost:8080/api/carts
```
Response:
```json
{
  "success": true,
  "data": {
    "cartToken": "abc-123-xyz"
  }
}
```

**2. Get Products**
```bash
curl http://localhost:8080/api/products?page=0&size=10
```

**3. Add Item to Cart**
```bash
curl -X POST http://localhost:8080/api/carts/abc-123-xyz/items \
  -H "Content-Type: application/json" \
  -d '{"skuId": 1, "quantity": 2}'
```

**4. Reserve Cart**
```bash
curl -X POST http://localhost:8080/api/carts/abc-123-xyz/reserve
```

**5. Checkout**
```bash
curl -X POST http://localhost:8080/api/checkout \
  -H "Content-Type: application/json" \
  -d '{
    "cartToken": "abc-123-xyz",
    "paymentMethod": "COD",
    "shippingAddress": "123 Main St",
    "idempotencyKey": "unique-key-001"
  }'
```

**6. Track Order**
```bash
curl http://localhost:8080/api/tracking/{trackingToken}
```

## Verify Reservation System

### Test Reservation TTL (15 minutes)
```bash
# 1. Create cart and add items
curl -X POST http://localhost:8080/api/carts
curl -X POST http://localhost:8080/api/carts/{token}/items -d '{"skuId":1,"quantity":2}'

# 2. Reserve
curl -X POST http://localhost:8080/api/carts/{token}/reserve

# 3. Check Redis
redis-cli
> KEYS reservation:*
> TTL reservation:{token}:1
> GET reservation:{token}:1

# 4. Check DB
mysql -u root -p
USE ecommerce;
SELECT * FROM stock_reservations WHERE status='RESERVED';

# 5. Wait 15+ minutes or manually cleanup
# Scheduled job runs every 5 minutes
```

### Test Atomic Stock Decrement (No Oversell)
```bash
# Create 2 carts with same item (only 1 in stock)
# Cart 1
curl -X POST http://localhost:8080/api/carts
curl -X POST http://localhost:8080/api/carts/{token1}/items -d '{"skuId":1,"quantity":1}'

# Cart 2
curl -X POST http://localhost:8080/api/carts
curl -X POST http://localhost:8080/api/carts/{token2}/items -d '{"skuId":1,"quantity":1}'

# Try to checkout both (second should fail)
curl -X POST http://localhost:8080/api/checkout -d '{"cartToken":"{token1}","paymentMethod":"COD","shippingAddress":"123","idempotencyKey":"key1"}'

curl -X POST http://localhost:8080/api/checkout -d '{"cartToken":"{token2}","paymentMethod":"COD","shippingAddress":"123","idempotencyKey":"key2"}'
# Expected: 409 Conflict "Insufficient stock"
```

### Test Idempotency
```bash
# Checkout with same idempotency key twice
curl -X POST http://localhost:8080/api/checkout -d '{"cartToken":"{token}","paymentMethod":"COD","shippingAddress":"123","idempotencyKey":"same-key"}'

curl -X POST http://localhost:8080/api/checkout -d '{"cartToken":"{token}","paymentMethod":"COD","shippingAddress":"123","idempotencyKey":"same-key"}'
# Expected: Second call returns same order without creating duplicate
```

## Common Issues

### "Cannot connect to MySQL"
```bash
# Check MySQL is running
mysql -u root -p

# Check credentials in application.yaml
# Check database exists
mysql> SHOW DATABASES;
```

### "Cannot connect to Redis"
```bash
# Check Redis is running
redis-cli ping

# If Redis not available, comment out Redis config
# System will work in DB-only mode
```

### "Port 8080 already in use"
Change port in `application.yaml`:
```yaml
server:
  port: 8081
```

### "Table doesn't exist"
```bash
# Check Hibernate is creating tables
# In application.yaml:
spring:
  jpa:
    hibernate:
      ddl-auto: update  # Should be 'update'
```

### Email not sending
```bash
# Check Mailtrap credentials in application.yaml
# Email failures don't block checkout
# Check logs for errors
```

## Monitoring

### Check Logs
```bash
# In application directory
tail -f logs/spring.log

# Or check console output
```

### Check Database State
```sql
-- View products and stock
SELECT p.name, pv.color, pv.size, pv.stock_quantity 
FROM products p 
JOIN product_variants pv ON p.id = pv.product_id;

-- View reservations
SELECT * FROM stock_reservations WHERE status='RESERVED';

-- View recent orders
SELECT * FROM orders ORDER BY created_at DESC LIMIT 10;

-- Check for orphaned reservations
SELECT * FROM stock_reservations 
WHERE status='RESERVED' AND expires_at < NOW();
```

### Check Redis State
```bash
redis-cli

# View all reservation keys
> KEYS reservation:*

# Check TTL
> TTL reservation:some-key

# View idempotency keys
> KEYS checkout:idempotency:*

# Clear all data (for testing)
> FLUSHDB
```

## Performance Tips

### For Development
- Use `spring.jpa.show-sql: true` to see queries
- Use `logging.level.com.example: DEBUG` for detailed logs

### For Production
- Set `spring.jpa.show-sql: false`
- Configure connection pool size in application.yaml
- Enable Redis persistence (AOF)
- Set up monitoring (Prometheus/Grafana)

## Next Steps

1. ✅ Application running
2. ✅ Sample data loaded
3. ✅ Basic APIs working
4. ⏭️ Add authentication (Spring Security)
5. ⏭️ Write integration tests
6. ⏭️ Deploy to production

## Support

- Check `README.md` for detailed documentation
- Check `IMPLEMENTATION.md` for technical details
- Check logs in console or `logs/` directory
- Check `requirement-summary.md` for specifications

---

**Happy coding!** 🚀

