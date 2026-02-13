# Docker Deployment Guide

This guide covers running the application using Docker Compose in both development and production environments.

## Prerequisites

- Docker Engine 20.10+
- Docker Compose v2.0+

## Quick Start

### Development Environment

```bash
# Start development environment with H2 database
docker compose -f compose.dev.yml up -d

# View logs
docker compose -f compose.dev.yml logs -f backend-dev

# Stop
docker compose -f compose.dev.yml down
```

### Production Environment

```bash
# Create production environment file
cp .env.prod.example .env.prod
# Edit .env.prod with production values

# Start production environment
docker compose -f compose.prod.yml --env-file .env.prod up -d

# View logs
docker compose -f compose.prod.yml logs -f

# Stop
docker compose -f compose.prod.yml down
```

---

## Development Setup (compose.dev.yml)

### Features

- **H2 In-Memory Database**: No external database required
- **Hot Reload**: Spring Boot DevTools enabled
- **Remote Debugging**: Port 5005 exposed for IDE debugging
- **Volume Mounting**: Source code mounted for instant changes
- **H2 Console**: Access at `http://localhost:8080/h2-console`

### Configuration

The development setup uses `.env.dev`:

```bash
SPRING_PROFILES_ACTIVE=dev
JWT_SECRET=dev-secret-key-for-local-development-only
JWT_EXPIRATION=86400000
BACKEND_PORT=8080
```

### Commands

```bash
# Start in detached mode
docker compose -f compose.dev.yml up -d

# Start with build
docker compose -f compose.dev.yml up --build -d

# View logs (follow)
docker compose -f compose.dev.yml logs -f backend-dev

# Restart backend only
docker compose -f compose.dev.yml restart backend-dev

# Stop and remove containers
docker compose -f compose.dev.yml down

# Stop and remove volumes
docker compose -f compose.dev.yml down -v
```

### Remote Debugging

1. Configure your IDE to connect to `localhost:5005`
2. Set breakpoints in your code
3. Debug!

**IntelliJ IDEA:**
- Run → Edit Configurations
- Add New Configuration → Remote JVM Debug
- Host: localhost, Port: 5005

**VS Code:**
Add to `.vscode/launch.json`:
```json
{
  "type": "java",
  "name": "Debug (Attach)",
  "request": "attach",
  "hostName": "localhost",
  "port": 5005
}
```

### Accessing H2 Console

URL: `http://localhost:8080/h2-console`

Settings:
- JDBC URL: `jdbc:h2:mem:analyticsdb`
- Username: `sa`
- Password: (leave empty)

---

## Production Setup (compose.prod.yml)

### Features

- **PostgreSQL Database**: Persistent data storage
- **Resource Limits**: CPU and memory constraints
- **Health Checks**: Automated health monitoring
- **Logging**: JSON file logging with rotation
- **Optimized JVM**: Production-tuned garbage collection
- **PostgreSQL Tuning**: Performance-optimized database settings
- **Automatic Restart**: Containers restart on failure

### Configuration

1. Create production environment file:
```bash
cp .env.prod.example .env.prod
```

2. Edit `.env.prod` with secure values:
```bash
# Database
POSTGRES_PASSWORD=<strong-password-here>

# JWT Secret (generate with: openssl rand -base64 64)
JWT_SECRET=<strong-secret-key-here>

# Build version
BUILD_VERSION=1.0.0
```

**CRITICAL**: Never commit `.env.prod` to version control!

### Commands

```bash
# Start production stack
docker compose -f compose.prod.yml --env-file .env.prod up -d

# Start with fresh build
docker compose -f compose.prod.yml --env-file .env.prod up --build -d

# View all logs
docker compose -f compose.prod.yml logs -f

# View backend logs only
docker compose -f compose.prod.yml logs -f backend

# View PostgreSQL logs only
docker compose -f compose.prod.yml logs -f postgres

# Check service status
docker compose -f compose.prod.yml ps

# Restart services
docker compose -f compose.prod.yml restart

# Stop services
docker compose -f compose.prod.yml down

# Stop and remove volumes (WARNING: deletes database!)
docker compose -f compose.prod.yml down -v
```

### Resource Configuration

The production setup includes resource limits:

**Backend:**
- CPU: 0.5-2 cores
- Memory: 512MB-1.5GB
- JVM: 512MB initial, 1GB max heap

**PostgreSQL:**
- CPU: 0.25-1 core
- Memory: 256MB-512MB
- Optimized for up to 100 connections

Adjust in `compose.prod.yml` under `deploy.resources` if needed.

### Database Backups

```bash
# Backup database
docker compose -f compose.prod.yml exec postgres pg_dump -U analytics analyticsdb > backup_$(date +%Y%m%d_%H%M%S).sql

# Restore database
docker compose -f compose.prod.yml exec -T postgres psql -U analytics analyticsdb < backup.sql
```

### Monitoring

```bash
# Check health status
curl http://localhost:8080/actuator/health

# View resource usage
docker stats

# View container logs in real-time
docker compose -f compose.prod.yml logs -f --tail=100
```

---

## Comparison: Dev vs Prod

| Feature | Development | Production |
|---------|------------|------------|
| Database | H2 (in-memory) | PostgreSQL (persistent) |
| Hot Reload | ✅ Enabled | ❌ Disabled |
| Debug Port | ✅ 5005 | ❌ Not exposed |
| Resource Limits | ❌ None | ✅ CPU/Memory limits |
| Logging | DEBUG level | INFO level |
| Health Checks | Basic | Advanced |
| Restart Policy | unless-stopped | always |
| JVM Tuning | Default | Optimized |
| Build Type | Gradle bootRun | Optimized JAR |

---

## Troubleshooting

### Development Issues

**Container won't start:**
```bash
# Check logs
docker compose -f compose.dev.yml logs backend-dev

# Rebuild from scratch
docker compose -f compose.dev.yml down -v
docker compose -f compose.dev.yml up --build
```

**Changes not reflecting:**
- Ensure source code is mounted: `docker compose -f compose.dev.yml ps`
- Check DevTools is enabled in build.gradle
- Restart: `docker compose -f compose.dev.yml restart backend-dev`

### Production Issues

**Database connection failed:**
```bash
# Check PostgreSQL is healthy
docker compose -f compose.prod.yml ps postgres

# Check PostgreSQL logs
docker compose -f compose.prod.yml logs postgres

# Verify environment variables
docker compose -f compose.prod.yml config
```

**Out of memory:**
- Increase memory limits in `compose.prod.yml`
- Adjust JVM heap size in `JAVA_OPTS`

**Performance issues:**
- Check resource usage: `docker stats`
- Review PostgreSQL logs for slow queries
- Consider scaling horizontally (multiple backend instances)

---

## Security Best Practices

### Development
- ✅ Use `.env.dev` with dummy credentials
- ✅ H2 console only accessible locally
- ✅ Never use dev credentials in production

### Production
- ✅ Use strong passwords (min 20 characters)
- ✅ Generate secure JWT secret: `openssl rand -base64 64`
- ✅ Never commit `.env.prod` to git
- ✅ Use secrets management in cloud deployments
- ✅ Enable firewall rules
- ✅ Regular security updates
- ✅ Rotate credentials periodically
- ✅ Use TLS/SSL for connections

---

## Cloud Deployment

### AWS ECS

```bash
# Build for specific architecture
docker buildx build --platform linux/amd64 -t backend:latest .

# Push to ECR
aws ecr get-login-password | docker login --username AWS --password-stdin <account>.dkr.ecr.region.amazonaws.com
docker tag backend:latest <account>.dkr.ecr.region.amazonaws.com/backend:latest
docker push <account>.dkr.ecr.region.amazonaws.com/backend:latest
```

### Google Cloud Run

```bash
# Build and push
gcloud builds submit --tag gcr.io/PROJECT_ID/backend

# Deploy
gcloud run deploy backend --image gcr.io/PROJECT_ID/backend --platform managed
```

### Kubernetes

See `k8s/` directory for Kubernetes manifests (to be created).

---

## Additional Resources

- [Spring Boot Docker Guide](https://spring.io/guides/topicals/spring-boot-docker/)
- [Docker Compose Documentation](https://docs.docker.com/compose/)
- [PostgreSQL Docker Hub](https://hub.docker.com/_/postgres)
