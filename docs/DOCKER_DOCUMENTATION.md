# Docker Deployment Guide

Complete guide to building, running, and deploying the Analytics Backend using Docker.

## Overview

The project is fully dockerized with:
- **Multi-stage Dockerfile** - Optimized build and runtime layers
- **Docker Compose** - Easy orchestration
- **Health Checks** - Container health monitoring
- **Non-root User** - Security best practices
- **Alpine-based Runtime** - Minimal image size

## Quick Start

### 1. Build and Run with Docker Compose

```bash
# Build and start the application
docker-compose up -d

# View logs
docker-compose logs -f backend

# Stop the application
docker-compose down
```

The API will be available at: `http://localhost:8080`

### 2. Build Docker Image Manually

```bash
# Build the image
docker build -t analytics-backend:latest .

# Run the container
docker run -d \
  --name analytics-backend \
  -p 8080:8080 \
  -e JWT_SECRET=your-secret-key \
  analytics-backend:latest
```

## Dockerfile Architecture

### Multi-Stage Build

The Dockerfile uses a two-stage build process:

#### Stage 1: Builder
- Base: `gradle:8.5-jdk17`
- Purpose: Build the application JAR
- Caches Gradle dependencies for faster rebuilds
- Runs `gradle clean build` (skips tests in container)

#### Stage 2: Runtime
- Base: `eclipse-temurin:17-jre-alpine`
- Purpose: Run the application
- Minimal Alpine Linux (small image size)
- Non-root user for security
- Health check configuration

### Image Size Optimization

- **Builder stage**: ~800MB (not included in final image)
- **Final image**: ~200MB
- Uses Alpine Linux for minimal footprint
- Only JRE (not JDK) in runtime image

## Docker Compose Configuration

### Services

#### Backend Service
```yaml
services:
  backend:
    build: .
    ports:
      - "8080:8080"
    environment:
      - JWT_SECRET=${JWT_SECRET}
    networks:
      - analytics-network
    healthcheck:
      test: ["CMD", "wget", "--spider", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
```

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| SPRING_PROFILES_ACTIVE | docker | Active Spring profile |
| SPRING_DATASOURCE_URL | jdbc:h2:mem:analyticsdb | Database connection URL |
| JWT_SECRET | (provided) | JWT signing secret |
| JWT_EXPIRATION | 86400000 | Token expiration (24h in ms) |

### Custom Environment Variables

Create a `.env` file in the project root:

```bash
# .env
JWT_SECRET=your-production-secret-key-here
JWT_EXPIRATION=3600000
```

Then run:
```bash
docker-compose up -d
```

## Docker Commands

### Building

```bash
# Build with tag
docker build -t analytics-backend:1.0.0 .

# Build with no cache
docker build --no-cache -t analytics-backend:latest .

# Build with build args
docker build --build-arg GRADLE_VERSION=8.5 -t analytics-backend:latest .
```

### Running

```bash
# Run with environment variables
docker run -d \
  --name analytics-backend \
  -p 8080:8080 \
  -e JWT_SECRET=mysecret \
  -e JWT_EXPIRATION=86400000 \
  analytics-backend:latest

# Run with custom network
docker run -d \
  --name analytics-backend \
  --network my-network \
  -p 8080:8080 \
  analytics-backend:latest

# Run with volume mount (for logs)
docker run -d \
  --name analytics-backend \
  -p 8080:8080 \
  -v $(pwd)/logs:/app/logs \
  analytics-backend:latest
```

### Managing Containers

```bash
# Start container
docker start analytics-backend

# Stop container
docker stop analytics-backend

# Restart container
docker restart analytics-backend

# View logs
docker logs -f analytics-backend

# View last 100 lines
docker logs --tail 100 analytics-backend

# Execute command in container
docker exec -it analytics-backend sh

# Inspect container
docker inspect analytics-backend

# View resource usage
docker stats analytics-backend
```

### Cleanup

```bash
# Remove container
docker rm analytics-backend

# Remove image
docker rmi analytics-backend:latest

# Remove all stopped containers
docker container prune

# Remove unused images
docker image prune

# Remove everything (containers, images, networks, volumes)
docker system prune -a
```

## Health Checks

### Container Health Check

The Dockerfile includes a health check:

```dockerfile
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1
```

### Check Health Status

```bash
# Docker inspect
docker inspect --format='{{.State.Health.Status}}' analytics-backend

# Docker ps
docker ps

# Manual health check
curl http://localhost:8080/actuator/health
```

### Health Response

```json
{
  "status": "UP"
}
```

## Production Deployment

### Best Practices

1. **Use Specific Tags**
   ```bash
   docker build -t analytics-backend:1.0.0 .
   docker build -t analytics-backend:latest .
   ```

2. **Set Production Secrets**
   ```bash
   export JWT_SECRET=$(openssl rand -base64 32)
   docker run -d -e JWT_SECRET=$JWT_SECRET analytics-backend:latest
   ```

3. **Enable Resource Limits**
   ```yaml
   services:
     backend:
       deploy:
         resources:
           limits:
             cpus: '1.0'
             memory: 512M
           reservations:
             cpus: '0.5'
             memory: 256M
   ```

4. **Use External Database**
   ```yaml
   services:
     backend:
       environment:
         - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/analyticsdb
         - SPRING_DATASOURCE_USERNAME=analytics
         - SPRING_DATASOURCE_PASSWORD=secure-password
         - SPRING_JPA_HIBERNATE_DDL_AUTO=validate
   ```

5. **Enable Logging**
   ```yaml
   services:
     backend:
       logging:
         driver: "json-file"
         options:
           max-size: "10m"
           max-file: "3"
   ```

### Security Recommendations

1. **Non-root User** ✅
   - Container runs as `spring:spring` user
   - Implemented in Dockerfile

2. **Secrets Management**
   ```bash
   # Use Docker secrets (Swarm mode)
   echo "your-secret" | docker secret create jwt_secret -
   ```

3. **Network Isolation**
   ```yaml
   networks:
     backend:
       driver: bridge
       internal: true  # No external access
     frontend:
       driver: bridge
   ```

4. **Read-only Filesystem**
   ```yaml
   services:
     backend:
       read_only: true
       tmpfs:
         - /tmp
   ```

## Docker Compose Profiles

### Development
```bash
docker-compose --profile dev up -d
```

### Production
```bash
docker-compose --profile prod up -d
```

### With PostgreSQL
```bash
# Uncomment postgres service in docker-compose.yml
docker-compose up -d
```

## Monitoring

### Container Logs

```bash
# Follow logs
docker-compose logs -f backend

# View recent logs
docker-compose logs --tail=100 backend

# Logs since timestamp
docker logs --since 2026-02-11T14:00:00 analytics-backend
```

### Container Metrics

```bash
# Real-time stats
docker stats analytics-backend

# Export metrics
docker stats --no-stream --format "table {{.Container}}\t{{.CPUPerc}}\t{{.MemUsage}}"
```

## Troubleshooting

### Container Won't Start

```bash
# Check logs
docker logs analytics-backend

# Check health status
docker inspect --format='{{json .State.Health}}' analytics-backend

# Check if port is already in use
lsof -i :8080
```

### Out of Memory

```bash
# Increase memory limit
docker run -d --memory="1g" analytics-backend:latest

# Or in docker-compose.yml
services:
  backend:
    mem_limit: 1g
```

### Build Failures

```bash
# Clear build cache
docker builder prune

# Build with verbose output
docker build --progress=plain -t analytics-backend:latest .

# Check disk space
docker system df
```

### Connection Issues

```bash
# Test from inside container
docker exec -it analytics-backend sh
wget http://localhost:8080/actuator/health

# Test from host
curl http://localhost:8080/actuator/health

# Check network
docker network inspect analytics-network
```

## CI/CD Integration

### GitHub Actions Example

```yaml
name: Build and Push Docker Image

on:
  push:
    branches: [main]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Build Docker image
        run: docker build -t analytics-backend:${{ github.sha }} .
      
      - name: Run tests
        run: docker run --rm analytics-backend:${{ github.sha }} ./gradlew test
      
      - name: Push to registry
        run: |
          docker tag analytics-backend:${{ github.sha }} myregistry/analytics-backend:latest
          docker push myregistry/analytics-backend:latest
```

### GitLab CI Example

```yaml
docker-build:
  stage: build
  image: docker:latest
  services:
    - docker:dind
  script:
    - docker build -t $CI_REGISTRY_IMAGE:$CI_COMMIT_SHA .
    - docker push $CI_REGISTRY_IMAGE:$CI_COMMIT_SHA
```

## Advanced Configuration

### Multi-container Setup

```yaml
version: '3.8'

services:
  backend:
    build: .
    depends_on:
      postgres:
        condition: service_healthy
    environment:
      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/analyticsdb
  
  postgres:
    image: postgres:16-alpine
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U analytics"]
  
  nginx:
    image: nginx:alpine
    ports:
      - "80:80"
    depends_on:
      - backend
```

### Kubernetes Deployment

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: analytics-backend
spec:
  replicas: 3
  selector:
    matchLabels:
      app: analytics-backend
  template:
    metadata:
      labels:
        app: analytics-backend
    spec:
      containers:
      - name: backend
        image: analytics-backend:latest
        ports:
        - containerPort: 8080
        env:
        - name: JWT_SECRET
          valueFrom:
            secretKeyRef:
              name: jwt-secret
              key: secret
```

## Testing the Deployment

### 1. Health Check
```bash
curl http://localhost:8080/actuator/health
```

### 2. Register User
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123"
  }'
```

### 3. Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123"
  }'
```

### 4. Access Protected Endpoint
```bash
TOKEN="your-jwt-token"
curl -X GET http://localhost:8080/api/events \
  -H "Authorization: Bearer $TOKEN"
```

## Summary

✅ **Multi-stage build** - Optimized image size
✅ **Health checks** - Container monitoring
✅ **Security** - Non-root user
✅ **Orchestration** - Docker Compose ready
✅ **Production-ready** - Best practices implemented

The application is now fully containerized and ready for deployment!

### Quick Commands Reference

```bash
# Development
docker-compose up -d          # Start
docker-compose logs -f        # View logs
docker-compose down           # Stop

# Production
docker build -t analytics-backend:1.0.0 .
docker run -d -p 8080:8080 analytics-backend:1.0.0

# Monitoring
docker ps                     # List containers
docker stats                  # Resource usage
docker logs -f <container>    # Follow logs

# Cleanup
docker-compose down -v        # Stop and remove volumes
docker system prune -a        # Clean everything
```
