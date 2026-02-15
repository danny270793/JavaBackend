# User Authentication & Management API

Complete user authentication and management system with password encryption using BCrypt.

## Architecture

Following the same Clean Architecture pattern as the Event CRUD:

### 1. Domain Layer (`domain/model`)
- **User**: Core user entity with UUID, username, email, password, timestamps

### 2. Application Layer (`application`)
- **UserService**: Service interface for user operations
- **DTOs**:
  - `RegisterUserRequest`: For user registration
  - `LoginRequest`: For user authentication
  - `LoginResponse`: For login success response
  - `UserResponse`: For returning user data (without password)

### 3. Infrastructure Layer (`infrastructure`)
- **UserEntity**: JPA entity with automatic timestamp management
- **UserJpaRepository**: Spring Data JPA repository with custom queries
- **UserServiceImpl**: Service implementation with BCrypt password encryption
- **SecurityConfig**: Spring Security configuration for password encoding

### 4. Presentation Layer (`presentation`)
- **AuthController**: Authentication endpoints (register, login)
- **UserController**: User management endpoints

## Authentication Endpoints

### Register User
Create a new user account with encrypted password.

```http
POST /api/v1/auth/register
Content-Type: application/json

{
  "username": "johndoe",
  "email": "john@example.com",
  "password": "password123"
}
```

**Response (201 Created):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "username": "johndoe",
  "email": "john@example.com",
  "createdAt": "2026-02-11T14:00:00",
  "updatedAt": "2026-02-11T14:00:00"
}
```

**Validation Rules:**
- Username: required, 3-50 characters, unique
- Email: required, valid email format, unique
- Password: required, minimum 6 characters

### Login
Authenticate user with username and password.

```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "username": "johndoe",
  "password": "password123"
}
```

**Response (200 OK):**
```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "username": "johndoe",
  "email": "john@example.com",
  "message": "Login successful"
}
```

**Error Response (400 Bad Request):**
```json
{
  "status": 404,
  "message": "Invalid username or password",
  "timestamp": "2026-02-11T14:00:00"
}
```

## User Management Endpoints

### Get User by ID
```http
GET /api/users/{id}
```

**Response (200 OK):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "username": "johndoe",
  "email": "john@example.com",
  "createdAt": "2026-02-11T14:00:00",
  "updatedAt": "2026-02-11T14:00:00"
}
```

### Get User by Username
```http
GET /api/users/username/{username}
```

### Get All Users
```http
GET /api/users
```

**Response (200 OK):**
```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "username": "johndoe",
    "email": "john@example.com",
    "createdAt": "2026-02-11T14:00:00",
    "updatedAt": "2026-02-11T14:00:00"
  }
]
```

### Delete User
```http
DELETE /api/users/{id}
```

**Response (204 No Content)**

## User Model

| Field | Type | Description |
|-------|------|-------------|
| id | UUID | Unique identifier (auto-generated) |
| username | String | Unique username (3-50 characters) |
| email | String | Unique email address |
| password | String | Encrypted password (BCrypt) |
| createdAt | LocalDateTime | Account creation timestamp |
| updatedAt | LocalDateTime | Last update timestamp |

## Security Features

### Password Encryption
- Passwords are encrypted using **BCrypt** algorithm
- Password strength: minimum 6 characters (configurable)
- Passwords are never returned in API responses

### Security Configuration
- CSRF protection disabled for API endpoints
- All endpoints currently permit all requests (for development)
- BCryptPasswordEncoder with default strength (10 rounds)

### Validation
- Username and email uniqueness is enforced at database level
- Request validation using Jakarta Validation annotations
- Proper error messages for validation failures

## Example Usage

### 1. Register a New User
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "email": "john@example.com",
    "password": "securepassword123"
  }'
```

### 2. Login
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "password": "securepassword123"
  }'
```

### 3. Get User by ID
```bash
curl http://localhost:8080/api/users/550e8400-e29b-41d4-a716-446655440000
```

### 4. Get All Users
```bash
curl http://localhost:8080/api/users
```

### 5. Delete User
```bash
curl -X DELETE http://localhost:8080/api/users/550e8400-e29b-41d4-a716-446655440000
```

## Error Responses

### Username Already Exists
```json
{
  "status": 404,
  "message": "Username already exists",
  "timestamp": "2026-02-11T14:00:00"
}
```

### Email Already Exists
```json
{
  "status": 404,
  "message": "Email already exists",
  "timestamp": "2026-02-11T14:00:00"
}
```

### Invalid Credentials
```json
{
  "status": 404,
  "message": "Invalid username or password",
  "timestamp": "2026-02-11T14:00:00"
}
```

### Validation Errors
```json
{
  "status": 400,
  "errors": {
    "username": "Username must be between 3 and 50 characters",
    "email": "Email must be valid",
    "password": "Password must be at least 6 characters"
  },
  "timestamp": "2026-02-11T14:00:00"
}
```

## Database Schema

### users Table
```sql
CREATE TABLE users (
    id UUID PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
```

## Future Enhancements

Potential improvements for production use:
1. **JWT Token Authentication**: Implement token-based authentication
2. **Role-Based Access Control**: Add user roles and permissions
3. **Password Reset**: Email-based password recovery
4. **Account Verification**: Email verification for new accounts
5. **Session Management**: Track active user sessions
6. **Rate Limiting**: Prevent brute force attacks
7. **OAuth2 Integration**: Support social login (Google, GitHub, etc.)
8. **Audit Logging**: Track user activities

## Testing

You can test the endpoints using:
- **cURL** (command line)
- **Postman** (GUI)
- **HTTPie** (command line)
- Browser DevTools for GET requests

Example test flow:
1. Register a new user
2. Login with the registered credentials
3. Retrieve user information
4. List all users
5. Delete the test user
