# Analytics Backend

Spring Boot backend application for analytics tracking with event management and user authentication.

## Features

- **User Authentication**: JWT-based authentication with secure password hashing
- **Event Tracking**: CRUD operations for analytics events (NAVIGATION, ACTION)
- **Clean Architecture**: Separation of concerns across Domain, Application, Infrastructure, and Presentation layers
- **Database Migrations**: Liquibase-managed schema versioning
- **Docker Support**: Containerized deployment with Docker Compose
- **PostgreSQL**: Production-ready relational database
- **Security**: Spring Security with JWT tokens
- **Health Monitoring**: Spring Boot Actuator endpoints

## Quick Start

### Prerequisites

- Java 17 or later
- PostgreSQL 16+ (or use Docker Compose)
- Gradle 8.5+

### Running Locally

```bash
# Clone the repository
git clone <repository-url>
cd backend

# Start PostgreSQL with Docker Compose
docker compose up postgres -d

# Run the application
./gradlew bootRun
```

The application will be available at `http://localhost:8080`

### Default Admin Credentials

- **Username**: `admin`
- **Password**: `admin`

⚠️ **Important**: Change these credentials before deploying to production!

## Documentation

### Getting Started
- **[Default Credentials](docs/DEFAULT_CREDENTIALS.md)** - Admin user credentials and security considerations
- **[Environment Configuration](docs/ENV_CONFIGURATION.md)** - Environment variables and configuration guide
- **[Docker Documentation](docs/DOCKER_DOCUMENTATION.md)** - Docker setup, deployment, and best practices

### API Documentation
- **[API Documentation](docs/API_DOCUMENTATION.md)** - Event CRUD API endpoints and examples
- **[User API Documentation](docs/USER_API_DOCUMENTATION.md)** - User management and authentication endpoints
- **[Authentication Documentation](docs/AUTHENTICATION_DOCUMENTATION.md)** - JWT authentication flow and security

### Database
- **[Database Migrations](docs/DATABASE_MIGRATIONS.md)** - Liquibase migration management guide
- **[Liquibase Quick Start](docs/LIQUIBASE_QUICK_START.md)** - Quick reference for creating migrations
- **[PostgreSQL Migration](docs/POSTGRESQL_MIGRATION.md)** - H2 to PostgreSQL migration guide

## Project Structure

```
backend/
├── src/
│   ├── main/
│   │   ├── java/io/github/danny270793/analytics/backend/
│   │   │   ├── domain/              # Domain models and business logic
│   │   │   ├── application/         # DTOs and service interfaces
│   │   │   ├── infrastructure/      # JPA, repositories, security
│   │   │   └── presentation/        # REST controllers
│   │   └── resources/
│   │       ├── application.properties
│   │       └── db/changelog/        # Liquibase migrations
│   └── test/
├── docs/                            # Documentation
├── docker-compose.yml               # Docker orchestration
├── Dockerfile                       # Multi-stage Docker build
└── build.gradle                     # Gradle dependencies
```

## Technology Stack

- **Framework**: Spring Boot 4.0.2
- **Language**: Java 17
- **Database**: PostgreSQL 16 (H2 for testing)
- **Security**: Spring Security + JWT
- **Migrations**: Liquibase
- **Build Tool**: Gradle 9.3
- **Container**: Docker + Docker Compose

## API Overview

### Authentication
```bash
# Login
POST /api/auth/login
{
  "username": "admin",
  "password": "admin"
}

# Register
POST /api/auth/register
{
  "username": "newuser",
  "email": "user@example.com",
  "password": "password123"
}
```

### Events (Requires Authentication)
```bash
# Create Event
POST /api/events
Authorization: Bearer <token>
{
  "type": "NAVIGATION",
  "from": "/home",
  "to": "/dashboard"
}

# Get All Events
GET /api/events
Authorization: Bearer <token>
```

### Users (Requires Authentication)
```bash
# Get All Users
GET /api/users
Authorization: Bearer <token>

# Get User by ID
GET /api/users/{id}
Authorization: Bearer <token>
```

## Development

### Building

```bash
# Build the project
./gradlew clean build

# Run tests
./gradlew test

# Build Docker image
docker compose build
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
- `src/main/resources/application.properties` - Application configuration
- `.env` - Local environment variables (gitignored)
- `.env.example` - Environment template
- `docker-compose.yml` - Docker services configuration

## Deployment

### Docker Compose (Recommended)

```bash
# Start all services
docker compose up -d

# View logs
docker compose logs -f backend

# Stop services
docker compose down
```

### Production Considerations

1. **Change default admin credentials** (see [Default Credentials](docs/DEFAULT_CREDENTIALS.md))
2. **Set strong JWT secret** (min 256 bits for HS256)
3. **Configure proper CORS settings**
4. **Enable HTTPS/TLS**
5. **Set up database backups**
6. **Monitor application health** (`/actuator/health`)
7. **Review security configuration**

See [Docker Documentation](docs/DOCKER_DOCUMENTATION.md) for production deployment guide.

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
- Ensure PostgreSQL is running: `docker compose ps`
- Check connection string in `.env` or `application.properties`
- Verify credentials match PostgreSQL configuration

### Authentication Issues
- Check JWT secret is configured correctly
- Verify token expiration settings
- Ensure Authorization header format: `Bearer <token>`

### Migration Issues
- Check Liquibase logs in application startup
- Verify changelog files are in correct format
- See [Liquibase Quick Start](docs/LIQUIBASE_QUICK_START.md)

For more troubleshooting guidance, see the relevant documentation in the `docs/` directory.

## Contributing

1. Create a feature branch
2. Make your changes
3. Write/update tests
4. Update documentation
5. Submit a pull request

## License

[Add your license here]

## Support

For issues and questions:
- Check the documentation in `docs/`
- Review application logs
- Check Docker container logs: `docker compose logs backend`

---

**Last Updated**: February 12, 2026
