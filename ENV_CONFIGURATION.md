# Environment Configuration Guide

This guide explains how to configure the application using environment variables.

## Quick Start

### 1. Copy the Example File

```bash
cp .env.example .env
```

### 2. Edit Your Configuration

```bash
# Open .env in your editor
nano .env
# or
vim .env
```

### 3. Run with Docker Compose

```bash
docker-compose up -d
```

Docker Compose will automatically load variables from the `.env` file.

## Environment Variables

### Application Configuration

| Variable | Default | Description |
|----------|---------|-------------|
| `SPRING_PROFILES_ACTIVE` | `docker` | Active Spring profile |
| `SPRING_DATASOURCE_URL` | `jdbc:h2:mem:analyticsdb` | Database connection URL |

### JWT Configuration

| Variable | Default | Description |
|----------|---------|-------------|
| `JWT_SECRET` | (see .env.example) | Secret key for signing JWT tokens (min 256 bits) |
| `JWT_EXPIRATION` | `86400000` | Token expiration time in milliseconds (24 hours) |

**⚠️ IMPORTANT**: Always change `JWT_SECRET` in production!

### Container Configuration

| Variable | Default | Description |
|----------|---------|-------------|
| `BACKEND_CONTAINER_NAME` | `analytics-backend` | Docker container name for backend |
| `BACKEND_PORT` | `8080` | Host port for backend service |

### PostgreSQL Configuration (Optional)

| Variable | Default | Description |
|----------|---------|-------------|
| `POSTGRES_DB` | `analyticsdb` | PostgreSQL database name |
| `POSTGRES_USER` | `analytics` | PostgreSQL username |
| `POSTGRES_PASSWORD` | `analytics123` | PostgreSQL password |
| `POSTGRES_CONTAINER_NAME` | `analytics-postgres` | Docker container name for PostgreSQL |
| `POSTGRES_PORT` | `5432` | Host port for PostgreSQL |

## Production Configuration

### Generate Secure JWT Secret

```bash
# Generate a secure random secret (256 bits)
openssl rand -base64 32
```

Copy the output and set it as `JWT_SECRET` in your `.env` file.

### Example Production .env

```bash
# Application Configuration
SPRING_PROFILES_ACTIVE=prod

# Use PostgreSQL instead of H2
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/analyticsdb

# JWT Configuration
JWT_SECRET=<your-generated-secret-here>
JWT_EXPIRATION=3600000  # 1 hour for production

# Container Configuration
BACKEND_CONTAINER_NAME=analytics-backend
BACKEND_PORT=8080

# PostgreSQL Configuration
POSTGRES_DB=analyticsdb
POSTGRES_USER=analytics
POSTGRES_PASSWORD=<strong-secure-password>
POSTGRES_CONTAINER_NAME=analytics-postgres
POSTGRES_PORT=5432
```

## Using with Docker Compose

### Default Configuration (H2 Database)

```bash
# Uses .env file automatically
docker-compose up -d
```

### With PostgreSQL

1. Uncomment the `postgres` service in `docker-compose.yml`
2. Update `.env` to use PostgreSQL:
   ```bash
   SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/analyticsdb
   ```
3. Start services:
   ```bash
   docker-compose up -d
   ```

### Override Environment Variables

```bash
# Override specific variables
JWT_SECRET=my-custom-secret docker-compose up -d

# Or set them in shell
export JWT_SECRET=my-custom-secret
docker-compose up -d
```

## Switching Between Databases

### Use H2 (In-Memory)
```bash
SPRING_DATASOURCE_URL=jdbc:h2:mem:analyticsdb
```

### Use PostgreSQL
```bash
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/analyticsdb
```

Don't forget to update `build.gradle` dependencies and `application.properties` if switching database types!

## Environment Variables in Kubernetes

For Kubernetes deployments, use ConfigMaps and Secrets:

### ConfigMap Example
```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: backend-config
data:
  SPRING_PROFILES_ACTIVE: "prod"
  SPRING_DATASOURCE_URL: "jdbc:postgresql://postgres:5432/analyticsdb"
  JWT_EXPIRATION: "3600000"
```

### Secret Example
```yaml
apiVersion: v1
kind: Secret
metadata:
  name: backend-secrets
type: Opaque
stringData:
  JWT_SECRET: "your-secret-here"
  POSTGRES_PASSWORD: "secure-password"
```

## Best Practices

### Security
- ✅ Never commit `.env` to version control
- ✅ Use `.env.example` as a template
- ✅ Generate strong random secrets for production
- ✅ Use different secrets for different environments
- ✅ Rotate secrets periodically

### Configuration
- ✅ Use environment-specific `.env` files (`.env.dev`, `.env.prod`)
- ✅ Document all required variables in `.env.example`
- ✅ Provide sensible defaults where possible
- ✅ Validate required variables on startup

### Docker Compose
- ✅ Keep sensitive data in `.env`, not in `docker-compose.yml`
- ✅ Use `${VARIABLE}` syntax in docker-compose.yml
- ✅ Provide defaults with `${VARIABLE:-default}` when appropriate

## Troubleshooting

### Variables Not Loading

1. **Check file location**: `.env` must be in the same directory as `docker-compose.yml`

2. **Check file format**: No spaces around `=`
   ```bash
   # Correct
   JWT_SECRET=mySecret
   
   # Wrong
   JWT_SECRET = mySecret
   ```

3. **Rebuild containers** if you changed environment variables:
   ```bash
   docker-compose down
   docker-compose up -d
   ```

### Variable Substitution Not Working

```bash
# Check variable substitution
docker-compose config

# This shows the final configuration with variables replaced
```

### Invalid JWT Secret

If you get JWT errors:
- Ensure `JWT_SECRET` is at least 256 bits (32 characters for base64)
- Check for special characters that might need escaping
- Regenerate with: `openssl rand -base64 32`

## Validation Checklist

Before deploying to production:

- [ ] Changed `JWT_SECRET` from default
- [ ] Set strong `POSTGRES_PASSWORD`
- [ ] Configured appropriate `JWT_EXPIRATION`
- [ ] Verified all required variables are set
- [ ] Tested configuration with `docker-compose config`
- [ ] Checked container logs for startup errors
- [ ] Confirmed health check endpoint responds

## Example Commands

```bash
# Copy example file
cp .env.example .env

# Generate secure JWT secret
openssl rand -base64 32 > jwt_secret.txt

# Edit .env file
nano .env

# Validate configuration
docker-compose config

# Start with .env
docker-compose up -d

# View loaded environment
docker-compose exec backend env | grep -E "JWT|POSTGRES|SPRING"

# Test connection
curl http://localhost:8080/actuator/health
```

## Related Documentation

- [Docker Documentation](DOCKER_DOCUMENTATION.md) - Complete Docker deployment guide
- [Authentication Documentation](AUTHENTICATION_DOCUMENTATION.md) - JWT configuration details
- [API Documentation](API_DOCUMENTATION.md) - API endpoints and usage
