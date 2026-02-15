# Default Credentials

## Admin User

A default admin user is created automatically on application startup via `AdminUserSeeder`.

### Credentials

- **Username**: `admin`
- **Password**: `admin`
- **Email**: `admin@analytics.local`
- **User ID**: `00000000-0000-0000-0000-000000000001`

### Active Profiles

✅ **All profiles** (dev, prod, test) - Admin user is always created for initial access

### Usage

#### 1. Login to Get JWT Token

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin"
  }'
```

**Response:**
```json
{
  "userId": "00000000-0000-0000-0000-000000000001",
  "username": "admin",
  "email": "admin@analytics.local",
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "message": "Login successful"
}
```

#### 2. Use Token for Protected Endpoints

```bash
# Save the token
TOKEN="<your-token-from-login-response>"

# Access protected endpoints
curl -X GET http://localhost:8080/api/users \
  -H "Authorization: Bearer $TOKEN"

curl -X GET http://localhost:8080/api/events \
  -H "Authorization: Bearer $TOKEN"
```

## Security Considerations

### ⚠️ IMPORTANT - Production Deployment

**DO NOT use default credentials in production!**

Before deploying to production:

1. **Change Admin Password Immediately**
   - Login with default credentials
   - Change password via application or database
   - Document the new password securely

2. **Disable AdminUserSeeder in Production (Optional)**
   - Remove or comment out `@Configuration` annotation
   - Or add `@Profile("!prod")` to skip in production
   
3. **Monitor First Access**
   - Log and audit admin user creation
   - Track when default password is changed

### Best Practices

1. **Development/Testing Only**
   - Use default credentials only for local development
   - Never commit production credentials
   - Change password immediately after first production deployment

2. **Automatic Creation**
   - Admin user created on every first startup
   - Idempotent (won't create duplicates)
   - Consistent UUID across deployments

3. **Environment-Specific Actions**
   - **Dev**: Default credentials are acceptable
   - **Test**: Default credentials for automated testing
   - **Prod**: **MUST** change password immediately

## Implementation Details

### AdminUserSeeder Component

**Location:** `io.github.danny270793.analytics.backend.infrastructure.config.AdminUserSeeder`

**Type:** Spring `@Configuration` with `CommandLineRunner`

**Code Structure:**
```java
@Configuration
public class AdminUserSeeder {
    @Bean
    CommandLineRunner seedAdminUser(
        UserJpaRepository userRepository,
        PasswordEncoder passwordEncoder
    ) {
        return args -> {
            // Check if admin user exists
            if (userRepository.findByUsername("admin").isPresent()) {
                log.info("👤 Admin user already exists - skipping creation");
                return;
            }
            
            // Create admin user with fixed UUID and credentials
            UserEntity adminUser = new UserEntity();
            adminUser.setId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
            adminUser.setUsername("admin");
            adminUser.setEmail("admin@analytics.local");
            adminUser.setPassword(passwordEncoder.encode("admin"));
            
            userRepository.save(adminUser);
            log.info("✅ Default admin user created successfully");
        };
    }
}
```

### Execution Flow

1. Spring Boot application starts
2. Liquibase creates database schema
3. `CommandLineRunner` beans execute
4. `AdminUserSeeder` checks if admin exists
5. If not exists, creates admin user with BCrypt password
6. Application logs creation status

### Password Encryption

- **Algorithm**: BCrypt
- **Plain Password**: `admin`
- **Encryption**: Performed dynamically by `PasswordEncoder` at runtime
- **Rounds**: 10 (Spring Security default)
- **Idempotent**: If admin exists, creation is skipped

## Testing

### Verify Admin User Exists

```bash
# Connect to database
docker exec -it analytics-postgres psql -U analytics -d analyticsdb

# Query admin user
SELECT id, username, email, created_at FROM users WHERE username = 'admin';

# Exit
\q
```

### Test Login

```bash
# Login as admin
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin"
  }'

# Should return JWT token and user info
```

## Removing Default Admin

### Option 1: Delete via API

```bash
# Login first to get token
TOKEN="<your-jwt-token>"

# Delete admin user
curl -X DELETE http://localhost:8080/api/v1/users/00000000-0000-0000-0000-000000000001 \
  -H "Authorization: Bearer $TOKEN"
```

### Option 2: Delete via Database

```sql
-- Connect to database
docker exec -it analytics-postgres psql -U analytics -d analyticsdb

-- Delete admin user
DELETE FROM users WHERE username = 'admin';
```

### Option 3: Disable AdminUserSeeder (Before Deployment)

**Method A: Profile-based**
```java
// Change @Configuration to only run in dev/test
@Configuration
@Profile({"dev", "test"})  // Skip in prod
public class AdminUserSeeder {
    // ...
}
```

**Method B: Remove annotation**
```java
// Comment out @Configuration
// @Configuration
public class AdminUserSeeder {
    // ...
}
```

## Quick Reference

```bash
# Default Credentials
Username: admin
Password: admin

# Login
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin"}'

# Use token for requests
curl -X GET http://localhost:8080/api/users \
  -H "Authorization: Bearer <token>"
```

## Summary

✅ **Default admin user created automatically via `AdminUserSeeder`**  
✅ **Active in all profiles** (dev, prod, test)  
✅ **Credentials**: admin/admin  
✅ **BCrypt encrypted password**  
✅ **Fixed UUID for consistency**  
✅ **Idempotent creation** (won't create duplicates)  
✅ **Ready to use immediately after startup**

⚠️ **Remember to change password in production!**
