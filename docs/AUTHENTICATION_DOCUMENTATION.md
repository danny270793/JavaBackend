# JWT Authentication Documentation

Complete JWT-based authentication system protecting Event and User CRUD endpoints.

## Overview

The application now uses **JWT (JSON Web Token)** authentication to secure all CRUD operations on Event and User entities. Only the authentication endpoints (`/api/v1/auth/**`) remain public.

## Authentication Flow

### 1. Register a New User
First, create a user account:

```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "email": "john@example.com",
    "password": "password123"
  }'
```

**Response:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "username": "johndoe",
  "email": "john@example.com",
  "createdAt": "2026-02-11T14:00:00",
  "updatedAt": "2026-02-11T14:00:00"
}
```

### 2. Login to Obtain JWT Token
Authenticate with your credentials to receive a JWT token:

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "password": "password123"
  }'
```

**Response:**
```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "username": "johndoe",
  "email": "john@example.com",
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huZG9lIiwiaWF0IjoxNzA3NjcyMDAwLCJleHAiOjE3MDc3NTg0MDB9.abcdefg...",
  "message": "Login successful"
}
```

**Important:** Save the `token` value - you'll need it for all subsequent requests!

### 3. Access Protected Endpoints
Use the JWT token in the `Authorization` header with the `Bearer` prefix:

```bash
curl -X GET http://localhost:8080/api/events \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE"
```

## Protected Endpoints

All endpoints except authentication are now protected:

### Event Endpoints (Protected)
- `POST /api/events` - Create event
- `GET /api/events` - Get all events
- `GET /api/events/{id}` - Get event by ID
- `PUT /api/events/{id}` - Update event
- `DELETE /api/events/{id}` - Delete event

### User Endpoints (Protected)
- `GET /api/users` - Get all users
- `GET /api/users/{id}` - Get user by ID
- `GET /api/users/username/{username}` - Get user by username
- `DELETE /api/users/{id}` - Delete user

### Public Endpoints (No Authentication Required)
- `POST /api/v1/auth/register` - Register new user
- `POST /api/v1/auth/login` - Login
- `GET /h2-console/**` - H2 database console (development only)

## Complete Example Workflow

### Step 1: Register
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "alice",
    "email": "alice@example.com",
    "password": "securepass123"
  }'
```

### Step 2: Login and Extract Token
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "alice",
    "password": "securepass123"
  }'
```

Save the token from the response.

### Step 3: Create an Event (Authenticated)
```bash
curl -X POST http://localhost:8080/api/events \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "type": "NAVIGATION",
    "from": "/home",
    "to": "/dashboard"
  }'
```

### Step 4: Get All Events (Authenticated)
```bash
curl -X GET http://localhost:8080/api/events \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

### Step 5: Get User Info (Authenticated)
```bash
curl -X GET http://localhost:8080/api/users \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

## JWT Configuration

The JWT settings are configured in `application.properties`:

```properties
# JWT Configuration
jwt.secret=mySecretKeyForJWTTokenGenerationThatIsAtLeast256BitsLongForHS256Algorithm
jwt.expiration=86400000  # 24 hours in milliseconds
```

### Configuration Options

| Property | Default | Description |
|----------|---------|-------------|
| jwt.secret | (provided) | Secret key for signing tokens (must be at least 256 bits) |
| jwt.expiration | 86400000 | Token expiration time in milliseconds (24 hours) |

**Security Note:** In production, use environment variables for the JWT secret:
```bash
export JWT_SECRET=your-super-secure-secret-key-here
```

## Error Responses

### 401 Unauthorized
Occurs when no token is provided or the token is invalid:

```json
{
  "timestamp": "2026-02-11T14:00:00",
  "status": 401,
  "error": "Unauthorized",
  "path": "/api/events"
}
```

**Solution:** Ensure you include a valid JWT token in the Authorization header.

### 403 Forbidden
Occurs when the token is valid but the user doesn't have permission:

```json
{
  "timestamp": "2026-02-11T14:00:00",
  "status": 403,
  "error": "Forbidden",
  "path": "/api/events"
}
```

### Token Expired
When your token expires (after 24 hours by default), you'll receive a 401 error. Simply login again to get a new token.

## Security Architecture

### Components

1. **JwtUtil** (`infrastructure/security/JwtUtil.java`)
   - Generates JWT tokens
   - Validates tokens
   - Extracts user information from tokens

2. **CustomUserDetailsService** (`infrastructure/security/CustomUserDetailsService.java`)
   - Loads user details from database
   - Used by Spring Security for authentication

3. **JwtAuthenticationFilter** (`infrastructure/security/JwtAuthenticationFilter.java`)
   - Intercepts all requests
   - Extracts and validates JWT tokens
   - Sets authentication context

4. **SecurityConfig** (`infrastructure/config/SecurityConfig.java`)
   - Configures Spring Security
   - Defines public and protected endpoints
   - Registers JWT filter

### Security Features

✅ **Stateless Authentication** - No session storage required
✅ **BCrypt Password Encryption** - Passwords securely hashed
✅ **Token-Based Authorization** - JWT tokens validate requests
✅ **Automatic Token Validation** - Filter checks every request
✅ **Protected CRUD Operations** - All Event and User endpoints secured

## Testing with Postman

### 1. Create a Collection
- Name: "Analytics API"

### 2. Add Environment Variables
- `base_url`: `http://localhost:8080`
- `jwt_token`: (will be set after login)

### 3. Login Request Setup
- Method: POST
- URL: `{{base_url}}/api/v1/auth/login`
- Body (JSON):
  ```json
  {
    "username": "johndoe",
    "password": "password123"
  }
  ```
- Tests Tab (Auto-save token):
  ```javascript
  var jsonData = pm.response.json();
  pm.environment.set("jwt_token", jsonData.token);
  ```

### 4. Protected Request Setup
For all protected endpoints, add to Headers:
- Key: `Authorization`
- Value: `Bearer {{jwt_token}}`

## Development vs Production

### Development Mode (Current)
- JWT secret in application.properties
- H2 console accessible
- All endpoints use HTTP
- CSRF disabled for API testing

### Production Recommendations
1. **Use Environment Variables:**
   ```bash
   JWT_SECRET=your-production-secret
   JWT_EXPIRATION=3600000  # 1 hour
   ```

2. **Enable HTTPS:**
   - Use TLS/SSL certificates
   - Redirect HTTP to HTTPS

3. **Rotate JWT Secrets:**
   - Change secrets periodically
   - Use strong, randomly generated secrets

4. **Add Refresh Tokens:**
   - Implement refresh token mechanism
   - Shorter access token expiration

5. **Rate Limiting:**
   - Prevent brute force attacks
   - Limit login attempts

6. **Logging and Monitoring:**
   - Log authentication attempts
   - Monitor suspicious activities

## Troubleshooting

### Problem: "Authorization header is missing"
**Solution:** Add the Authorization header with Bearer token:
```bash
-H "Authorization: Bearer YOUR_TOKEN"
```

### Problem: "Token has expired"
**Solution:** Login again to get a new token. Tokens expire after 24 hours.

### Problem: "Invalid token"
**Solution:** 
- Ensure the token is copied correctly
- Check that you're using the latest token from login
- Verify no extra spaces or characters

### Problem: "403 Forbidden after successful login"
**Solution:**
- Verify the token is being sent correctly
- Check that the username in the token matches a valid user

## Token Structure

JWT tokens consist of three parts separated by dots:

```
eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huZG9lIiwiaWF0IjoxNzA3NjcyMDAwLCJleHAiOjE3MDc3NTg0MDB9.signature
```

1. **Header** - Algorithm and token type
2. **Payload** - User data and claims (username, expiration)
3. **Signature** - Cryptographic signature

You can decode tokens at [jwt.io](https://jwt.io) (for debugging only, never share production tokens!).

## Best Practices

1. **Never Share Tokens** - Treat tokens like passwords
2. **Use HTTPS** - Encrypt token transmission
3. **Short Expiration** - Minimize token lifetime in production
4. **Refresh Tokens** - Implement token refresh mechanism
5. **Logout** - Clear tokens from client storage
6. **Secure Storage** - Store tokens securely (not in localStorage for sensitive apps)
7. **Validate on Server** - Always validate tokens server-side

## Summary

The API now requires JWT authentication for all CRUD operations:

1. **Register** → Create account
2. **Login** → Receive JWT token
3. **Use Token** → Include in Authorization header for all requests
4. **Token Expires** → Login again after 24 hours

All Event and User management operations are now protected, ensuring only authenticated users can access and modify data!
