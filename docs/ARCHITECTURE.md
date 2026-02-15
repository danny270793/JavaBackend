# Architecture Overview

## Introduction

The Analytics Backend is built following **Clean Architecture** principles, ensuring separation of concerns, testability, and maintainability. The application is structured in layers, with dependencies flowing inward toward the domain layer.

## Architecture Layers

### 1. Domain Layer (`domain/`)

The innermost layer containing core business logic and rules. This layer has no dependencies on other layers.

#### Components:

**Models** (`domain/model/`)
- `User`: Core user entity with authentication credentials
- `Event`: Analytics event with type, source, and destination
- `EventType`: Enum defining event types (NAVIGATION, ACTION)

**Exceptions** (`domain/exception/`)
- `UserNotFoundException`: User not found by ID or username
- `UsernameAlreadyExistsException`: Duplicate username during registration
- `EmailAlreadyExistsException`: Duplicate email during registration
- `InvalidCredentialsException`: Invalid username/password combination
- `EventNotFoundException`: Event not found by ID
- `UnauthorizedAccessException`: User attempting to access another user's resources

#### Characteristics:
- Pure Java objects (POJOs)
- No framework dependencies
- Business logic and validation
- Custom exceptions for domain errors

---

### 2. Application Layer (`application/`)

Defines use cases and orchestrates the flow of data between layers.

#### Components:

**DTOs** (`application/dto/`)

*Request DTOs:*
- `RegisterUserRequest`: User registration payload with validation
- `LoginRequest`: Login credentials
- `CreateEventRequest`: New event creation
- `UpdateEventRequest`: Event modification

*Response DTOs:*
- `LoginResponse`: JWT token and user details
- `UserResponse`: User information (excludes password)
- `EventResponse`: Event details
- `PostResponse`: External API post data

**Services** (`application/service/`)
- `UserService`: User management interface
- `EventService`: Event operations interface
- `PostService`: External API integration interface

#### Characteristics:
- Framework-agnostic interfaces
- Data transfer objects for API communication
- Bean validation annotations (`@NotBlank`, `@Email`, `@Size`)
- No implementation details

---

### 3. Infrastructure Layer (`infrastructure/`)

Implements application layer interfaces and handles external concerns.

#### Configuration (`infrastructure/config/`)
- `JpaAuditingConfig`: Enables JPA auditing for created/updated fields
- `SecurityConfig`: Spring Security configuration with JWT filter
- `OpenApiConfig`: Swagger/OpenAPI documentation setup

#### Persistence (`infrastructure/persistence/`)

**Entities** (`persistence/entity/`)
- `UserEntity`: JPA entity with soft delete and audit fields
- `EventEntity`: JPA entity with soft delete and audit fields
- `AuditableEntity`: Base class for audit fields (created/updated by/at)

**Repositories** (`persistence/repository/`)
- `UserJpaRepository`: Spring Data JPA repository for users
- `EventJpaRepository`: Spring Data JPA repository for events

**Adapters** (`persistence/adapter/`)
- `UserEntityAdapter`: Converts between User domain model and UserEntity
- `EventEntityAdapter`: Converts between Event domain model and EventEntity

#### Security (`infrastructure/security/`)
- `JwtUtil`: JWT token generation, validation, and parsing
- `CustomUserDetailsService`: Loads user details for authentication
- `JwtAuthenticationFilter`: Intercepts requests to validate JWT tokens
- `AuditorAwareImpl`: Provides current user for JPA auditing

#### Services (`infrastructure/service/`)
- `UserServiceImpl`: User management implementation
- `EventServiceImpl`: Event operations implementation
- `PostServiceImpl`: External API integration implementation

#### Characteristics:
- Spring framework components
- Database interactions via JPA
- Security implementation
- External API communication

---

### 4. Presentation Layer (`presentation/`)

Handles HTTP requests and responses, exposing REST APIs.

#### Controllers (`presentation/controller/`)
- `AuthController`: Registration and login endpoints
- `UserController`: User management endpoints
- `EventController`: Event CRUD endpoints
- `PostController`: External API proxy endpoints

#### Exception Handling (`presentation/exception/`)
- `GlobalExceptionHandler`: Centralized exception handling with `@RestControllerAdvice`
  - Maps domain exceptions to HTTP status codes
  - Handles validation errors
  - Returns consistent error responses

#### Characteristics:
- REST API endpoints with `@RestController`
- Request/response mapping
- HTTP status code management
- OpenAPI/Swagger annotations

---

## Design Patterns

### 1. Clean Architecture
- **Dependency Rule**: Dependencies point inward (Presentation → Application → Domain)
- **Independence**: Domain layer independent of frameworks and databases
- **Testability**: Easy to test business logic in isolation

### 2. Repository Pattern
- Abstracts data access logic
- `JpaRepository` provides CRUD operations
- Custom query methods for complex queries

### 3. Adapter Pattern
- `EntityAdapter` classes convert between domain models and persistence entities
- Separates domain models from JPA concerns

### 4. Service Layer Pattern
- Business logic encapsulated in service classes
- Services depend on repository interfaces
- Transactional boundaries at service level

### 5. DTO Pattern
- Separate request/response objects from domain models
- Prevents over-exposure of domain details
- Allows API contract evolution independent of domain

### 6. Factory Pattern (Static)
- `fromDomain()` methods in DTOs
- Simplifies object creation and mapping

---

## Data Flow

### Typical Request Flow

```
1. HTTP Request
   ↓
2. Controller (Presentation Layer)
   - Maps request to DTO
   - Validates input
   ↓
3. Service Interface (Application Layer)
   - Defines contract
   ↓
4. Service Implementation (Infrastructure Layer)
   - Business logic execution
   - Authorization checks
   ↓
5. Repository (Infrastructure Layer)
   - Data access via JPA
   ↓
6. Entity Adapter (Infrastructure Layer)
   - Converts between Entity ↔ Domain Model
   ↓
7. Database
   - Persistence
   ↓
8. Response flows back through layers
   - Domain Model → DTO → HTTP Response
```

### Example: Create Event

```
POST /api/events
├── EventController.createEvent()
│   ├── Validates CreateEventRequest
│   └── Calls EventService.createEvent()
│       ├── EventServiceImpl.createEvent()
│       │   ├── Gets current user from SecurityContext
│       │   ├── Creates Event domain model
│       │   ├── Converts to EventEntity via Adapter
│       │   ├── Saves to database via Repository
│       │   └── Converts back to EventResponse
│       └── Returns EventResponse
└── Returns HTTP 201 Created with EventResponse
```

---

## Security Architecture

### Authentication Flow

1. **User Registration/Login**
   - Password hashed with BCrypt (strength 10)
   - JWT token generated on successful authentication
   - Token includes user ID and username as claims

2. **Request Authentication**
   - `JwtAuthenticationFilter` intercepts all requests
   - Extracts JWT from Authorization header
   - Validates token signature and expiration
   - Loads user details via `CustomUserDetailsService`
   - Sets authentication in `SecurityContext`

3. **Authorization**
   - Service layer checks resource ownership
   - Throws `UnauthorizedAccessException` for unauthorized access
   - Global exception handler maps to HTTP 403

### Security Configuration

- **Public Endpoints**: `/api/auth/**`, `/swagger-ui/**`, `/v3/api-docs/**`
- **Protected Endpoints**: All others require valid JWT
- **CSRF**: Disabled (stateless JWT authentication)
- **CORS**: Configured for allowed origins
- **Session Management**: Stateless (no server-side sessions)

---

## Database Schema

### Tables

#### users
- `id` (UUID, PK)
- `username` (VARCHAR, UNIQUE, NOT NULL)
- `email` (VARCHAR, UNIQUE, NOT NULL)
- `password` (VARCHAR, NOT NULL) - BCrypt hashed
- `created_at` (TIMESTAMP)
- `created_by` (UUID)
- `updated_at` (TIMESTAMP)
- `updated_by` (UUID)
- `deleted_at` (TIMESTAMP) - Soft delete
- `deleted_by` (UUID)

#### events
- `id` (UUID, PK)
- `type` (VARCHAR, NOT NULL) - NAVIGATION or ACTION
- `from_location` (VARCHAR, NOT NULL)
- `to_location` (VARCHAR, NOT NULL)
- `user_id` (UUID, FK → users.id)
- `created_at` (TIMESTAMP)
- `created_by` (UUID)
- `updated_at` (TIMESTAMP)
- `updated_by` (UUID)
- `deleted_at` (TIMESTAMP) - Soft delete
- `deleted_by` (UUID)

### Soft Delete Implementation

- Uses `@SQLRestriction("deleted_at IS NULL")` annotation
- Queries automatically filter out deleted records
- Deletion sets `deleted_at` and `deleted_by` instead of removing row
- Allows data recovery and audit trails

### Audit Fields

- Automatically populated via JPA Auditing
- `@CreatedDate`, `@LastModifiedDate` annotations
- `@CreatedBy`, `@LastModifiedBy` populated by `AuditorAwareImpl`
- Current user extracted from Spring Security context

---

## External Integrations

### JSONPlaceholder API

**Purpose**: Demonstrates external API integration

**Endpoints**:
- GET `/posts` - Fetches all posts
- GET `/posts/{id}` - Fetches specific post

**Implementation**:
- Uses Spring `RestTemplate`
- Error handling for network failures
- Null checks for missing data

---

## Testing Strategy

### Unit Tests (50+ tests, 74% coverage)

**Service Layer** (96% coverage)
- `UserServiceImplTest`: Registration, login, CRUD operations
- `EventServiceImplTest`: Event operations with ownership checks
- `PostServiceImplTest`: External API integration
- Mocks repositories and dependencies
- Tests both success and error scenarios

**Security Layer** (100% coverage)
- `JwtUtilTest`: Token generation, validation, expiration
- `CustomUserDetailsServiceTest`: User loading
- `JwtAuthenticationFilterTest`: Request filtering and authentication

**Persistence Layer** (99% coverage)
- `EventEntityTest`: Entity construction and property access
- `UserEntityTest`: Entity construction and property access

### Test Configuration

- Uses H2 in-memory database
- Separate `application-test.properties`
- Liquibase drops and recreates schema for each test run
- Minimal logging for cleaner test output

### Coverage Tool

- **JaCoCo**: Code coverage reporting
- Configured in `build.gradle`
- Excludes DTOs and config classes
- Minimum threshold: 70% (current: 74%)

---

## Configuration Management

### Application Profiles

**dev** (Development)
- H2 in-memory database
- Console enabled at `/h2-console`
- Verbose logging
- No external dependencies

**prod** (Production)
- PostgreSQL database
- Connection pooling optimized
- Minimal logging
- Health checks enabled

**test** (Testing)
- H2 in-memory database
- Liquibase drop-first enabled
- Test-specific JWT secrets

### Environment Variables

Managed via `.env.dev` and `.env.prod` files:
- Database credentials
- JWT secrets
- Server ports
- External API URLs

---

## Performance Considerations

### Database Optimization
- HikariCP connection pooling (max 10 connections)
- Lazy loading for JPA relationships
- Indexes on username, email, user_id foreign keys

### Caching
- Currently none (future consideration for user details, JWT validation)

### API Response
- Pagination for list endpoints (default 20 items per page)
- Selective field exposure via DTOs (excludes sensitive data)

---

## Scalability Considerations

### Horizontal Scaling
- Stateless architecture (JWT tokens)
- No server-side sessions
- Database connection pooling

### Vertical Scaling
- Configurable JVM heap size
- Thread pool configuration
- Database connection limits

### Future Enhancements
- Redis for distributed caching
- Message queue for async processing
- Database read replicas
- API rate limiting

---

## Monitoring & Observability

### Health Checks
- Spring Boot Actuator: `/actuator/health`
- Liveness and readiness probes for Kubernetes

### Logging
- SLF4J with Logback
- Structured logging with MDC
- Request/response logging in security filter

### Metrics
- Actuator metrics endpoint: `/actuator/metrics`
- JVM metrics, HTTP metrics, database pool metrics

---

## Deployment Architecture

### Docker Multi-Stage Build

**Stage 1: Builder**
- Uses Gradle to build the application
- Runs tests and generates JAR

**Stage 2: Runtime**
- Uses slim JRE image
- Copies only JAR file
- Minimal attack surface

### Docker Compose

**Development** (`compose.dev.yml`)
- Single container (app only)
- Uses H2 database (no external DB)
- Hot reload with volume mounts

**Production** (`compose.prod.yml`)
- Two containers: backend + PostgreSQL
- Persistent volumes for database
- Health checks and restart policies
- Environment-specific configuration

---

## Security Best Practices

### Implemented
✅ BCrypt password hashing
✅ JWT token-based authentication
✅ HTTPS ready (configure in deployment)
✅ Input validation with Bean Validation
✅ SQL injection protection (JPA/Hibernate)
✅ XSS protection (Spring Security defaults)
✅ CORS configuration
✅ Exception handling without stack trace exposure

### Recommended for Production
- [ ] Rate limiting on authentication endpoints
- [ ] Account lockout after failed login attempts
- [ ] Password complexity requirements
- [ ] JWT token refresh mechanism
- [ ] HTTPS enforcement
- [ ] Security headers (CSP, HSTS, X-Frame-Options)
- [ ] Regular dependency updates
- [ ] Penetration testing

---

## Future Roadmap

### Short Term
- [ ] Add refresh token mechanism
- [ ] Implement role-based access control (RBAC)
- [ ] Add email verification for registration
- [ ] Implement password reset flow
- [ ] Add API rate limiting

### Medium Term
- [ ] Redis caching layer
- [ ] WebSocket support for real-time events
- [ ] Elasticsearch for event search
- [ ] GraphQL API alongside REST
- [ ] Admin dashboard endpoints

### Long Term
- [ ] Multi-tenancy support
- [ ] Event-driven architecture with Kafka
- [ ] Microservices decomposition
- [ ] gRPC for internal service communication
- [ ] Kubernetes deployment manifests

---

**Document Version**: 1.0.0  
**Last Updated**: February 15, 2026  
**Author**: Analytics Backend Team
