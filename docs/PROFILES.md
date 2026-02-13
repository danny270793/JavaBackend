# Environment Profiles

This application supports multiple Spring profiles for different environments.

## Available Profiles

### Development Profile (`dev`)
- **Database**: H2 in-memory database
- **Purpose**: Local development and testing
- **Features**:
  - H2 Console enabled at `/h2-console`
  - SQL logging enabled
  - Debug logging for application code
  - No external database required

### Production Profile (`prod`)
- **Database**: PostgreSQL
- **Purpose**: Production deployment
- **Features**:
  - PostgreSQL database connection
  - Minimal SQL logging
  - INFO level logging
  - Configurable via environment variables

## How to Use

### Running with Dev Profile (Default)

```bash
# Using Gradle
./gradlew bootRun

# Or explicitly specify dev profile
./gradlew bootRun --args='--spring.profiles.active=dev'

# Using JAR
java -jar backend.jar --spring.profiles.active=dev
```

**Access H2 Console**: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:analyticsdb`
- Username: `sa`
- Password: (leave empty)

### Running with Production Profile

```bash
# Using Gradle
./gradlew bootRun --args='--spring.profiles.active=prod'

# Using JAR
java -jar backend.jar --spring.profiles.active=prod

# Using environment variable
export SPRING_PROFILES_ACTIVE=prod
java -jar backend.jar
```

## Configuration

### Development (application-dev.properties)
- H2 in-memory database with PostgreSQL compatibility mode
- Liquibase migrations run on startup
- H2 console enabled for debugging
- Verbose SQL and debug logging

### Production (application-prod.properties)
- PostgreSQL database connection
- Environment variable support:
  - `SPRING_DATASOURCE_URL`: Database URL (default: `jdbc:postgresql://localhost:5432/analyticsdb`)
  - `SPRING_DATASOURCE_USERNAME`: Database username (default: `analytics`)
  - `SPRING_DATASOURCE_PASSWORD`: Database password (default: `analytics123`)
- Minimal logging for performance
- Liquibase migrations run on startup

### Shared Configuration (application.properties)
- JWT secret and expiration
- Actuator endpoints
- Default profile: `dev`

## Environment Variables

You can override any configuration using environment variables:

```bash
# Set active profile
export SPRING_PROFILES_ACTIVE=prod

# PostgreSQL connection (prod profile)
export SPRING_DATASOURCE_URL=jdbc:postgresql://db.example.com:5432/mydb
export SPRING_DATASOURCE_USERNAME=myuser
export SPRING_DATASOURCE_PASSWORD=mypassword

# JWT configuration
export JWT_SECRET=your-secure-secret-key-here
export JWT_EXPIRATION=86400000
```

## Docker Deployment

When deploying with Docker, set the profile via environment variable:

```yaml
# docker-compose.yml
services:
  backend:
    image: backend:latest
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/analyticsdb
      - SPRING_DATASOURCE_USERNAME=analytics
      - SPRING_DATASOURCE_PASSWORD=analytics123
```

## Notes

- The `dev` profile uses H2 with PostgreSQL compatibility mode to ensure migrations work consistently across environments
- All Liquibase changelogs are applied on startup for both profiles
- Audit fields and soft delete are enabled in both profiles
- The default profile is `dev` for local development convenience
