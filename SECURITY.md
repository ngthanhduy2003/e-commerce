# 🔐 SECURITY DOCUMENTATION

**Last Updated:** January 21, 2026  
**Version:** 1.0  
**Status:** ✅ Implemented

---

## 📋 TABLE OF CONTENTS

1. [Overview](#overview)
2. [Authentication](#authentication)
3. [Authorization](#authorization)
4. [JWT Token Management](#jwt-token-management)
5. [Password Security](#password-security)
6. [Input Validation](#input-validation)
7. [HTTPS/TLS Configuration](#httpstls-configuration)
8. [API Security](#api-security)
9. [Security Best Practices](#security-best-practices)
10. [Troubleshooting](#troubleshooting)

---

## 🎯 OVERVIEW

### Security Features Implemented

| Feature | Status | Description |
|---------|--------|-------------|
| **JWT Authentication** | ✅ | Token-based authentication |
| **Role-Based Authorization** | ✅ | ADMIN, USER roles |
| **Password Encryption** | ✅ | BCrypt hashing |
| **HTTPS Support** | ✅ | SSL/TLS configuration |
| **Input Validation** | ✅ | Jakarta Validation |
| **CORS Protection** | ✅ | Configured origins |
| **CSRF Protection** | ✅ | Disabled for stateless API |
| **Rate Limiting** | ⚠️ | Planned for Phase 2 |

---

## 🔑 AUTHENTICATION

### JWT Token-Based Authentication

The application uses JWT (JSON Web Tokens) for stateless authentication.

#### Login Flow

```
1. User sends credentials to POST /api/auth/login
2. Server validates credentials
3. Server generates JWT access token + refresh token
4. Client stores tokens (localStorage/sessionStorage)
5. Client includes token in Authorization header for subsequent requests
```

#### Token Structure

```json
{
  "sub": "123",           // User ID
  "email": "user@example.com",
  "roles": "ROLE_USER",
  "iat": 1705881600,      // Issued at
  "exp": 1705968000       // Expiration (24 hours)
}
```

### Authentication Endpoints

#### 1. Register

```http
POST /api/auth/register
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "SecurePass123",
  "fullName": "John Doe"
}
```

**Response:**
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIs...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
    "tokenType": "Bearer",
    "userId": 123,
    "email": "user@example.com",
    "fullName": "John Doe",
    "role": "USER"
  }
}
```

#### 2. Login

```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "SecurePass123"
}
```

**Response:** Same as register

#### 3. Refresh Token

```http
POST /api/auth/refresh
Content-Type: application/json

{
  "refreshToken": "eyJhbGciOiJIUzI1NiIs..."
}
```

**Response:**
```json
{
  "success": true,
  "message": "Token refreshed successfully",
  "data": {
    "accessToken": "new_access_token...",
    "refreshToken": "same_refresh_token...",
    ...
  }
}
```

#### 4. Validate Token

```http
GET /api/auth/validate
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
```

**Response:**
```json
{
  "success": true,
  "message": "Token is valid",
  "data": null
}
```

---

## 🛡️ AUTHORIZATION

### Role-Based Access Control (RBAC)

The application supports two roles:
- **USER**: Regular customer
- **ADMIN**: Administrator

### Endpoint Security Matrix

| Endpoint | Method | Access |
|----------|--------|--------|
| `/api/auth/**` | ALL | 🌐 Public |
| `/api/products` | GET | 🌐 Public |
| `/api/carts` | POST | 🌐 Public (create cart) |
| `/api/tracking/**` | GET | 🌐 Public (anonymous tracking) |
| `/actuator/**` | ALL | 🌐 Public (dev only) |
| `/api/checkout` | POST | 🔒 Authenticated |
| `/api/orders/my` | GET | 🔒 Authenticated |
| `/api/carts/**` | ALL | 🔒 Authenticated |
| `/api/admin/**` | ALL | 🔐 ADMIN only |

### Using Authorization in Code

#### Controller Level

```java
@RestController
@RequestMapping("/api/admin/orders")
@PreAuthorize("hasRole('ADMIN')")
public class AdminOrderController {
    // All methods require ADMIN role
}
```

#### Method Level

```java
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
@GetMapping("/api/orders/my")
public ResponseEntity<?> getMyOrders() {
    // Authenticated users only
}
```

#### Getting Current User

```java
@GetMapping("/api/profile")
public ResponseEntity<?> getProfile(@AuthenticationPrincipal UserPrincipal currentUser) {
    Long userId = currentUser.getId();
    String email = currentUser.getEmail();
    // ...
}
```

---

## 🎫 JWT TOKEN MANAGEMENT

### Configuration

```yaml
app:
  security:
    jwt:
      secret: ${JWT_SECRET}  # 256-bit secret key
      expiration-ms: 86400000  # 24 hours
      refresh-expiration-ms: 604800000  # 7 days
```

### Token Expiration

| Token Type | Lifetime | Purpose |
|------------|----------|---------|
| **Access Token** | 24 hours | API access |
| **Refresh Token** | 7 days | Renew access token |

### Token Refresh Strategy

```javascript
// Frontend example
async function apiCall(url, options) {
  let response = await fetch(url, {
    ...options,
    headers: {
      'Authorization': `Bearer ${accessToken}`,
      ...options.headers
    }
  });
  
  if (response.status === 401) {
    // Token expired, refresh it
    const newTokens = await refreshToken(refreshToken);
    
    // Retry original request
    response = await fetch(url, {
      ...options,
      headers: {
        'Authorization': `Bearer ${newTokens.accessToken}`,
        ...options.headers
      }
    });
  }
  
  return response;
}
```

### Token Security Best Practices

✅ **DO:**
- Store tokens in httpOnly cookies (backend-rendered apps)
- Store in sessionStorage for better security (cleared on tab close)
- Use HTTPS in production
- Implement token refresh mechanism
- Validate token on every request

❌ **DON'T:**
- Store tokens in localStorage (XSS vulnerable)
- Expose tokens in URLs
- Share tokens between domains
- Use weak JWT secrets
- Use long expiration times

---

## 🔒 PASSWORD SECURITY

### Password Hashing

The application uses **BCrypt** with default strength (10 rounds).

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

### Password Requirements

| Requirement | Value |
|-------------|-------|
| Minimum length | 6 characters |
| Maximum length | 100 characters |
| Complexity | No enforced (can add in Phase 2) |

### Password Validation (Recommended Phase 2)

```java
@Pattern(
    regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
    message = "Password must contain at least 8 characters, one uppercase, one lowercase, one number and one special character"
)
private String password;
```

### Password Reset Flow (TODO - Phase 2)

```
1. User requests password reset
2. System generates reset token (UUID)
3. Token stored in Redis with 1-hour expiration
4. Email sent with reset link
5. User clicks link, provides new password
6. System validates token, updates password
7. Token invalidated
```

---

## ✅ INPUT VALIDATION

### Validation Annotations

The application uses Jakarta Validation (Bean Validation 3.0).

#### Common Validations

```java
public class RegisterRequest {
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid", 
           regexp = "^[A-Za-z0-9+_.-]+@(.+)$")
    private String email;
    
    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100, 
          message = "Password must be between 6 and 100 characters")
    private String password;
    
    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, 
          message = "Full name must be between 2 and 100 characters")
    private String fullName;
}
```

### Validation Error Response

```json
{
  "success": false,
  "message": "Validation failed",
  "data": {
    "email": "Email should be valid",
    "password": "Password must be at least 6 characters"
  }
}
```

### SQL Injection Protection

✅ **Protected by JPA/Hibernate:**
- All queries use parameterized statements
- Named parameters (@Param) prevent injection

```java
// Safe - parameterized query
@Query("SELECT o FROM Order o WHERE o.trackingToken = :token")
Optional<Order> findByTrackingToken(@Param("token") String token);
```

### XSS Protection

⚠️ **Current Status:** Basic protection via Spring Security

**Recommended additions (Phase 2):**
- HTML sanitization library (OWASP Java HTML Sanitizer)
- Content Security Policy headers
- X-XSS-Protection header

---

## 🔐 HTTPS/TLS CONFIGURATION

### Recommended Approach: Reverse Proxy

For production deployments, **HTTPS should be handled at the reverse proxy level** (Nginx, Apache, or cloud load balancer), not at the application level.

**Benefits:**
- ✅ Centralized SSL certificate management
- ✅ Better performance (hardware acceleration)
- ✅ Easier certificate renewal (Let's Encrypt)
- ✅ Load balancing capabilities
- ✅ DDoS protection

### Development (HTTP)

```yaml
server:
  port: 8080
  # No SSL configuration - handled by reverse proxy
```

### Production with Nginx

**Nginx Configuration:**
```nginx
server {
    listen 80;
    server_name yourdomain.com;
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name yourdomain.com;

    ssl_certificate /etc/letsencrypt/live/yourdomain.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/yourdomain.com/privkey.pem;
    
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;
    ssl_prefer_server_ciphers on;

    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

### Get Free SSL Certificate (Let's Encrypt)

```bash
# Install Certbot
sudo apt-get install certbot python3-certbot-nginx

# Generate certificate
sudo certbot --nginx -d yourdomain.com

# Auto-renewal (certificates expire every 90 days)
sudo certbot renew --dry-run
```

### Cloud Deployment (AWS/Azure/GCP)

Most cloud platforms provide managed SSL/TLS:
- **AWS**: Use Application Load Balancer with ACM certificates
- **Azure**: Azure Application Gateway with managed certificates
- **GCP**: Google Cloud Load Balancing with managed certificates
- **Cloudflare**: Free SSL with CDN

**Application listens on HTTP internally, reverse proxy handles HTTPS externally.**

---

## 🌐 API SECURITY

### CORS Configuration

```yaml
app:
  cors:
    allowed-origins: ${CORS_ORIGINS:http://localhost:3000}
    allowed-methods: GET,POST,PUT,PATCH,DELETE,OPTIONS
    allowed-headers: "*"
    allow-credentials: true
```

### CSRF Protection

**Status:** ❌ Disabled (stateless API)

Rationale:
- Using JWT tokens (stateless)
- No session cookies
- CSRF not applicable for pure REST APIs

### Security Headers

**Recommended additions (Phase 2):**

```java
@Configuration
public class SecurityHeadersConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        http
            .headers(headers -> headers
                .contentSecurityPolicy(csp -> 
                    csp.policyDirectives("default-src 'self'"))
                .frameOptions(frame -> frame.deny())
                .xssProtection(xss -> xss.headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK))
                .contentTypeOptions(Customizer.withDefaults())
            );
        return http.build();
    }
}
```

---

## 📚 SECURITY BEST PRACTICES

### 1. Environment Variables

✅ **DO:** Store secrets in environment variables

```bash
# .env (never commit!)
JWT_SECRET=your-256-bit-secret-key-here
DB_PASSWORD=your-database-password
SSL_KEYSTORE_PASSWORD=your-keystore-password
```

❌ **DON'T:** Hardcode secrets in application.yaml

### 2. Token Storage

✅ **DO:**
- Use httpOnly cookies for SSR apps
- Use sessionStorage for SPAs
- Clear tokens on logout

❌ **DON'T:**
- Use localStorage (XSS vulnerable)
- Store in plain cookies

### 3. API Rate Limiting (Phase 2)

**Recommended implementation:**
```java
@RateLimit(value = 10, duration = 60) // 10 requests per minute
@PostMapping("/api/auth/login")
public ResponseEntity<?> login(...) {
    // ...
}
```

### 4. Logging & Monitoring

✅ **DO:**
- Log authentication failures
- Log authorization failures
- Monitor suspicious patterns

❌ **DON'T:**
- Log passwords
- Log JWT tokens
- Log sensitive user data

### 5. Database Security

✅ **DO:**
- Use read-only database user for queries
- Use connection pooling
- Enable SSL for DB connections (production)

### 6. Dependency Management

✅ **DO:**
- Keep dependencies updated
- Use Dependabot/Renovate
- Scan for vulnerabilities

```bash
# Check for vulnerable dependencies
./gradlew dependencyCheckAnalyze
```

---

## 🔧 TROUBLESHOOTING

### Common Issues

#### 1. 401 Unauthorized

**Symptoms:**
- API returns 401 Unauthorized
- Valid credentials but still unauthorized

**Solutions:**
```
1. Check JWT secret is set correctly
2. Verify token hasn't expired
3. Check Authorization header format: "Bearer <token>"
4. Verify token signature
5. Check user role matches endpoint requirement
```

#### 2. CORS Error

**Symptoms:**
- Browser console shows CORS error
- Request blocked by CORS policy

**Solutions:**
```
1. Add frontend origin to allowed-origins
2. Check allowed-methods includes your HTTP method
3. Verify allow-credentials is true if sending cookies
4. Check preflight OPTIONS request succeeds
```

#### 3. Invalid JWT Signature

**Symptoms:**
- "Invalid JWT signature" in logs
- Token validation fails

**Solutions:**
```
1. Verify JWT_SECRET matches between instances
2. Check secret is at least 256 bits (32 characters)
3. Ensure secret hasn't changed (invalidates old tokens)
4. Verify token wasn't tampered with
```

#### 4. Password Not Matching

**Symptoms:**
- Correct password but login fails
- "Invalid email or password" error

**Solutions:**
```
1. Verify BCrypt is configured
2. Check password was hashed during registration
3. Verify no trimming/encoding issues
4. Check database field type (VARCHAR, not TEXT)
```

---

## 📊 SECURITY CHECKLIST

### Pre-Production

- [x] JWT authentication implemented
- [x] Role-based authorization configured
- [x] Password encryption enabled (BCrypt)
- [x] HTTPS configuration documented (reverse proxy approach)
- [x] Input validation on all endpoints
- [x] CORS configured
- [ ] Rate limiting implemented (Phase 2)
- [ ] Security headers added (Phase 2)
- [ ] Dependency vulnerability scan (Phase 2)
- [ ] Penetration testing (Phase 2)

### Production Deployment

- [ ] Change JWT secret to strong random value (256-bit)
- [ ] Set up reverse proxy (Nginx/Apache) with SSL/TLS
- [ ] Configure Let's Encrypt for automatic certificate renewal
- [ ] Set secure database password
- [ ] Protect or disable actuator endpoints
- [ ] Enable production logging
- [ ] Configure monitoring alerts
- [ ] Backup encryption keys
- [ ] Document incident response plan
- [ ] Test SSL configuration (SSLLabs.com)

---

## 📞 SUPPORT

For security issues:
1. Check this documentation
2. Review logs in `/var/log/ecommerce/`
3. Check application.yaml configuration
4. Consult SecurityConfig.java

**Security Incident Response:**
1. Isolate affected systems
2. Collect logs and evidence
3. Patch vulnerability
4. Notify affected users (if data breach)
5. Document incident

---

**END OF SECURITY DOCUMENTATION**

**Version:** 1.0  
**Last Updated:** January 21, 2026  
**Next Review:** After Phase 2 completion

