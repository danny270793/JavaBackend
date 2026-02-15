# Default Credentials

## Admin User

A default admin user is created automatically via database migration.

### Credentials

- **Username**: `admin`
- **Password**: `admin`
- **Email**: `admin@analytics.local`
- **User ID**: `00000000-0000-0000-0000-000000000001`

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

1. **Delete or Disable the Migration**
   ```bash
   # Remove the migration file
   rm src/main/resources/db/changelog/changes/003-insert-default-admin-user.yaml
   
   # Or add a context to skip in production
   ```

2. **Or Change the Password**
   ```bash
   # After first deployment, login and change password
   # Or create a migration to update the admin password
   ```

3. **Or Use Production-Only Migration**
   ```yaml
   # Add context to skip in production
   changeSet:
     id: 003-insert-default-admin-user
     author: analytics-backend
     context: dev, test  # Skips in production
   ```

### Best Practices

1. **Development/Testing Only**
   - Use default credentials only for local development
   - Never commit production credentials

2. **Change Immediately**
   ```bash
   # After first login, change the password via API
   # (Would need to implement password change endpoint)
   ```

3. **Environment-Specific**
   ```properties
   # application-prod.properties
   spring.liquibase.contexts=prod
   ```

## Implementation Details

### Migration File

**Location:** `src/main/resources/db/changelog/changes/003-insert-default-admin-user.yaml`

**Changeset:**
```yaml
databaseChangeLog:
  - changeSet:
      id: 003-insert-default-admin-user
      author: analytics-backend
      changes:
        - insert:
            tableName: users
            columns:
              - column:
                  name: id
                  value: 00000000-0000-0000-0000-000000000001
              - column:
                  name: username
                  value: admin
              - column:
                  name: email
                  value: admin@analytics.local
              - column:
                  name: password
                  value: $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
              - column:
                  name: created_at
                  valueComputed: CURRENT_TIMESTAMP
              - column:
                  name: updated_at
                  valueComputed: CURRENT_TIMESTAMP
```

### Password Encryption

- **Algorithm**: BCrypt
- **Plain Password**: `admin`
- **BCrypt Hash**: `$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy`
- **Rounds**: 10 (default)

### Rollback

The changeset includes a rollback script:
```yaml
rollback:
  - delete:
      tableName: users
      where: username = 'admin'
```

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
curl -X DELETE http://localhost:8080/api/users/00000000-0000-0000-0000-000000000001 \
  -H "Authorization: Bearer $TOKEN"
```

### Option 2: Delete via Database

```sql
-- Connect to database
docker exec -it analytics-postgres psql -U analytics -d analyticsdb

-- Delete admin user
DELETE FROM users WHERE username = 'admin';
```

### Option 3: Remove Migration (Before Deployment)

```bash
# Delete the migration file
rm src/main/resources/db/changelog/changes/003-insert-default-admin-user.yaml

# Rebuild
./gradlew clean build
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

✅ **Default admin user created automatically via Liquibase migration**
✅ **Credentials**: admin/admin
✅ **BCrypt encrypted password**
✅ **Ready to use immediately after startup**

⚠️ **Remember to remove or change in production!**
