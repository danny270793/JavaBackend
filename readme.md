# Analytics Backend

Spring Boot backend application for analytics tracking with event management, user authentication, and external API integration.

## Features

### Core Functionality
- **User Authentication**: JWT-based authentication with secure BCrypt password hashing
- **Event Tracking**: Complete CRUD operations for analytics events (NAVIGATION, ACTION)
- **User Management**: User registration, login, profile management with soft delete support
- **External API Integration**: Posts endpoint fetching data from JSONPlaceholder API
- **Ownership-based Authorization**: Users can only access/modify their own resources
- **API Versioning**: URI-based versioning (v1) for backwards compatibility

### Architecture & Quality
- **Clean Architecture**: Separation of concerns across Domain, Application, Infrastructure, and Presentation layers
- **Comprehensive Testing**: 74% test coverage with 50+ unit tests (Security: 100%, Services: 96%, Entities: 99%)
- **Custom Exception Handling**: Domain-specific exceptions with global error handling
- **Audit Fields**: Automatic tracking of created/updated timestamps and user IDs
- **Soft Delete**: Non-destructive deletion with `deletedAt`/`deletedBy` tracking
- **Pagination Support**: All list endpoints support pageable results

### Infrastructure & DevOps
- **Database Migrations**: Liquibase-managed schema versioning with automatic execution
- **Multi-Environment Profiles**: Separate configurations for dev (H2) and prod (PostgreSQL)
- **Docker Support**: Multi-stage builds with development and production Dockerfiles
- **PostgreSQL**: Production-ready relational database with connection pooling
- **Security**: Spring Security with JWT tokens, BCrypt password encoding, and CORS configuration
- **Health Monitoring**: Spring Boot Actuator endpoints for application health checks
- **API Documentation**: Interactive Swagger/OpenAPI 3.0 documentation with authentication support

## Quick Start

### Prerequisites

- Java 17 or later
- PostgreSQL 16+ (or use Docker Compose)
- Gradle 8.5+

### Running Locally

#### Development Mode (H2 Database)
```bash
# Clone the repository
git clone <repository-url>
cd backend

# Run with dev profile (uses in-memory H2 database)
./gradlew bootRun --args='--spring.profiles.active=dev'
```

#### Production Mode (PostgreSQL)
```bash
# Start PostgreSQL with Docker Compose
docker compose -f compose.dev.yml up -d

# Run with prod profile
./gradlew bootRun --args='--spring.profiles.active=prod'
```

The application will be available at `http://localhost:8080`

### Environment Profiles

The application supports two profiles:
- **dev**: Uses H2 in-memory database (no setup required)
- **prod**: Uses PostgreSQL (requires running database)

See [Environment Profiles Documentation](docs/PROFILES.md) for detailed configuration.

### API Documentation

Interactive API documentation is available via Swagger UI at: **http://localhost:8080/swagger-ui.html**

See [Swagger Documentation](docs/SWAGGER.md) for detailed usage instructions including JWT authentication.

### Testing

```bash
# Run all tests
./gradlew test

# Run tests with coverage report
./gradlew test jacocoTestReport

# View coverage report
open build/reports/jacoco/html/index.html
```

Current test coverage: **74%** (50+ test cases)
- Security Layer: 100%
- Entity Layer: 99%
- Service Layer: 96%

See [Testing Documentation](docs/TESTING.md) for comprehensive testing guide.

### Default Admin Credentials

- **Username**: `admin`
- **Password**: `admin`

⚠️ **Important**: Change these credentials before deploying to production!

## Documentation

### Getting Started
- **[Environment Profiles](docs/PROFILES.md)** - Dev (H2) vs Prod (PostgreSQL) configuration
- **[Default Credentials](docs/DEFAULT_CREDENTIALS.md)** - Admin user credentials and security considerations
- **[Environment Configuration](docs/ENV_CONFIGURATION.md)** - Environment variables and configuration guide
- **[Docker Documentation](docs/DOCKER_DOCUMENTATION.md)** - Docker setup, deployment, and best practices

### API Documentation
- **[API Versioning](docs/API_VERSIONING.md)** - Version strategy, lifecycle, and migration guides
- **[Swagger/OpenAPI Documentation](docs/SWAGGER.md)** - Interactive API documentation and testing
- **[API Documentation](docs/API_DOCUMENTATION.md)** - Event CRUD API endpoints and examples
- **[User API Documentation](docs/USER_API_DOCUMENTATION.md)** - User management and authentication endpoints
- **[Authentication Documentation](docs/AUTHENTICATION_DOCUMENTATION.md)** - JWT authentication flow and security

### Development
- **[Testing Documentation](docs/TESTING.md)** - Comprehensive testing guide and coverage reports
- **[Database Migrations](docs/DATABASE_MIGRATIONS.md)** - Liquibase migration management guide
- **[Liquibase Quick Start](docs/LIQUIBASE_QUICK_START.md)** - Quick reference for creating migrations
- **[PostgreSQL Migration](docs/POSTGRESQL_MIGRATION.md)** - H2 to PostgreSQL migration guide

## Project Structure

```
backend/
├── src/
│   ├── main/
│   │   ├── java/io/github/danny270793/analytics/backend/
│   │   │   ├── domain/              # Domain models, exceptions, enums
│   │   │   │   ├── exception/       # Custom domain exceptions
│   │   │   │   └── model/           # Core business entities (User, Event)
│   │   │   ├── application/         # Application layer
│   │   │   │   ├── dto/             # Request/Response DTOs
│   │   │   │   └── service/         # Service interfaces
│   │   │   ├── infrastructure/      # Infrastructure implementation
│   │   │   │   ├── config/          # Spring configuration classes
│   │   │   │   ├── persistence/     # JPA entities, repositories, adapters
│   │   │   │   ├── security/        # JWT, authentication, user details
│   │   │   │   └── service/         # Service implementations
│   │   │   └── presentation/        # Presentation layer
│   │   │       ├── controller/      # REST controllers
│   │   │       └── exception/       # Global exception handler
│   │   └── resources/
│   │       ├── application.properties              # Base configuration
│   │       ├── application-dev.properties          # Dev profile (H2)
│   │       ├── application-prod.properties         # Prod profile (PostgreSQL)
│   │       └── db/changelog/                       # Liquibase migrations
│   │           ├── db.changelog-master.yaml        # Master changelog
│   │           └── changes/                        # Migration files
│   └── test/                                       # 50+ unit tests (74% coverage)
│       ├── java/io/github/danny270793/analytics/backend/
│       │   ├── domain/
│       │   ├── infrastructure/
│       │   │   ├── security/        # Security tests (100% coverage)
│       │   │   ├── service/         # Service tests (96% coverage)
│       │   │   └── persistence/     # Entity tests (99% coverage)
│       └── resources/
│           └── application-test.properties         # Test configuration
├── docs/                            # Comprehensive documentation
├── compose.dev.yml                  # Development Docker setup (H2)
├── compose.prod.yml                 # Production Docker setup (PostgreSQL)
├── Dockerfile.dev                   # Development Dockerfile
├── Dockerfile.prod                  # Production multi-stage Dockerfile
└── build.gradle                     # Gradle dependencies & JaCoCo config
```

## Technology Stack

### Core Framework & Language
- **Framework**: Spring Boot 4.0.2
- **Language**: Java 17
- **Build Tool**: Gradle 9.3

### Database & Persistence
- **Production Database**: PostgreSQL 16
- **Development Database**: H2 (in-memory)
- **ORM**: Spring Data JPA with Hibernate
- **Migrations**: Liquibase for version-controlled schema management
- **Connection Pooling**: HikariCP

### Security & Authentication
- **Security Framework**: Spring Security 6.x
- **Authentication**: JWT (JSON Web Tokens)
- **Token Library**: jjwt 0.12.5
- **Password Encoding**: BCrypt

### Testing & Quality
- **Testing Framework**: JUnit 5
- **Mocking**: Mockito
- **Assertions**: AssertJ
- **Coverage Tool**: JaCoCo (74% coverage)
- **Security Testing**: Spring Security Test

### API & Documentation
- **API Documentation**: Swagger/OpenAPI 3.0
- **Implementation**: springdoc-openapi 2.7.0
- **HTTP Client**: RestTemplate (for external API integration)

### DevOps & Deployment
- **Containerization**: Docker
- **Orchestration**: Docker Compose
- **Health Monitoring**: Spring Boot Actuator
- **Logging**: SLF4J with Logback

## API Overview

### API Versioning

All API endpoints are versioned using URI-based versioning. Current version: **v1**

See [API Versioning Documentation](docs/API_VERSIONING.md) for details on versioning strategy and migration.

### Authentication
```bash
# Login
POST /api/v1/auth/login
{
  "username": "admin",
  "password": "admin"
}

# Register
POST /api/v1/auth/register
{
  "username": "newuser",
  "email": "user@example.com",
  "password": "password123"
}
```

### Events (Requires Authentication)
```bash
# Create Event
POST /api/v1/events
Authorization: Bearer <token>
{
  "type": "NAVIGATION",
  "from": "/home",
  "to": "/dashboard"
}

# Get All Events
GET /api/v1/events
Authorization: Bearer <token>
```

### Users (Requires Authentication)
```bash
# Get All Users (with pagination)
GET /api/v1/users?page=0&size=20&sortBy=username&direction=ASC
Authorization: Bearer <token>

# Get User by ID
GET /api/v1/users/{id}
Authorization: Bearer <token>

# Delete User (soft delete)
DELETE /api/v1/users/{id}
Authorization: Bearer <token>
```

### Posts (External API Integration)
```bash
# Get All Posts
GET /api/v1/posts
Authorization: Bearer <token>

# Get Post by ID
GET /api/v1/posts/{id}
Authorization: Bearer <token>
```

## Development

### Building

```bash
# Build the project
./gradlew clean build

# Run tests with coverage
./gradlew test jacocoTestReport

# Check test coverage meets threshold (70%)
./gradlew jacocoTestCoverageVerification

# Build Docker image (development)
docker compose -f compose.dev.yml build

# Build Docker image (production)
docker compose -f compose.prod.yml build
```

### Database Migrations

```bash
# Create a new migration
# 1. Create a new YAML file in src/main/resources/db/changelog/changes/
# 2. Name it: NNN-description.yaml (e.g., 004-add-user-roles.yaml)
# 3. Restart the application - migrations run automatically
```

See [Database Migrations](docs/DATABASE_MIGRATIONS.md) for detailed instructions.

### Configuration

Key configuration files:
- `src/main/resources/application.properties` - Base application configuration
- `src/main/resources/application-dev.properties` - Development profile (H2 database)
- `src/main/resources/application-prod.properties` - Production profile (PostgreSQL)
- `src/test/resources/application-test.properties` - Test configuration
- `.env.dev` - Development environment variables (gitignored)
- `.env.prod` - Production environment variables (gitignored)
- `.env.example` - Environment template
- `compose.dev.yml` - Development Docker services
- `compose.prod.yml` - Production Docker services

See [Environment Configuration](docs/ENV_CONFIGURATION.md) for detailed setup.

## Deployment

### Docker Compose (Recommended)

#### Development Environment
```bash
# Start development services (H2 database)
docker compose -f compose.dev.yml up -d

# View logs
docker compose -f compose.dev.yml logs -f backend

# Stop services
docker compose -f compose.dev.yml down
```

#### Production Environment
```bash
# Start production services (PostgreSQL)
docker compose -f compose.prod.yml up -d

# View logs
docker compose -f compose.prod.yml logs -f backend

# Stop services
docker compose -f compose.prod.yml down

# Stop and remove volumes (caution: deletes database data)
docker compose -f compose.prod.yml down -v
```

### Production Considerations

#### Security
1. **Change default admin credentials** (see [Default Credentials](docs/DEFAULT_CREDENTIALS.md))
2. **Set strong JWT secret** (minimum 256 bits for HS256 algorithm)
3. **Configure proper CORS settings** for your frontend domain
4. **Enable HTTPS/TLS** in production environment
5. **Review and restrict Actuator endpoints** (currently accessible without auth)
6. **Set secure cookie flags** if using cookie-based sessions

#### Database
7. **Set up automated database backups** with retention policy
8. **Configure connection pool settings** for production load
9. **Enable database query logging** for monitoring (disable in production)

#### Monitoring & Observability
10. **Monitor application health** via `/actuator/health` endpoint
11. **Set up log aggregation** (ELK stack, CloudWatch, etc.)
12. **Configure alerts** for error rates and performance metrics
13. **Enable application metrics** via Actuator `/actuator/metrics`

#### Performance
14. **Review and optimize database indexes** based on query patterns
15. **Configure appropriate JVM heap size** for your workload
16. **Enable HTTP/2** for improved performance
17. **Consider caching strategy** for frequently accessed data

See [Docker Documentation](docs/DOCKER_DOCUMENTATION.md) for comprehensive production deployment guide.

## Health Check

```bash
curl http://localhost:8080/actuator/health
```

Response:
```json
{
  "groups": ["liveness", "readiness"],
  "status": "UP"
}
```

## Troubleshooting

### Database Connection Issues
- **PostgreSQL not running**: Check with `docker compose -f compose.prod.yml ps`
- **Connection refused**: Ensure PostgreSQL container is healthy and port 5432 is not in use
- **Authentication failed**: Verify credentials in `.env.prod` match PostgreSQL configuration
- **H2 console not accessible**: Ensure dev profile is active and H2 console is enabled

### Authentication Issues
- **Invalid token**: Check JWT secret matches between services
- **Token expired**: Default expiration is 24 hours, verify `jwt.expiration` property
- **401 Unauthorized**: Ensure Authorization header format is `Bearer <token>` (with space)
- **403 Forbidden**: User doesn't have permission to access the resource (ownership check)

### Migration Issues
- **Liquibase checksum mismatch**: Don't modify existing migration files, create new ones
- **Migration failed**: Check Liquibase logs in application startup output
- **Changelog format error**: Verify YAML syntax in changelog files
- **Missing columns**: Ensure migrations are running in correct order
- See [Liquibase Quick Start](docs/LIQUIBASE_QUICK_START.md) for common issues

### Docker Issues
- **Container won't start**: Check logs with `docker compose logs backend`
- **Port already in use**: Change port mapping in `compose.*.yml`
- **Volume permissions**: On Linux, may need to adjust user/group ownership
- **Image build fails**: Ensure Java 17 is installed and `./gradlew build` succeeds locally

### Test Failures
- **Test database issues**: Verify H2 is available and test profile is active
- **Coverage below threshold**: Run `./gradlew jacocoTestReport` to see detailed coverage
- **Intermittent failures**: Some tests may fail due to timing issues, run again

For more troubleshooting guidance, see the relevant documentation in the `docs/` directory.

## Contributing

We welcome contributions! Please follow these guidelines:

1. **Create a feature branch** from `main`
   ```bash
   git checkout -b feature/your-feature-name
   ```

2. **Make your changes** following existing code style and architecture
   - Follow Clean Architecture principles
   - Use constructor injection over field injection
   - Create custom exceptions instead of throwing RuntimeException
   - Add audit fields to new entities
   
3. **Write/update tests** to maintain >70% coverage
   - Unit tests for services and business logic
   - Test both success and error cases
   - Use descriptive test names following: `should{ExpectedBehavior}When{Condition}`
   
4. **Update documentation** as needed
   - Update README.md for new features
   - Add/update docs in `docs/` folder
   - Update API documentation if endpoints change
   
5. **Run quality checks** before committing
   ```bash
   ./gradlew clean build test jacocoTestReport
   ```
   
6. **Commit with clear messages**
   - Use descriptive commit messages
   - Reference issue numbers if applicable
   
7. **Submit a pull request**
   - Provide clear description of changes
   - Link related issues
   - Ensure CI/CD checks pass

## License

[Add your license here]

## Support

For issues and questions:
- Check the documentation in `docs/`
- Review application logs
- Check Docker container logs: `docker compose logs backend`

---

**Last Updated**: February 15, 2026
**Version**: 1.0.0
**Test Coverage**: 74%
