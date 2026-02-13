# PostgreSQL Migration Guide

This guide explains the migration from H2 in-memory database to PostgreSQL.

## What Changed

### Database Configuration
- **Before**: H2 in-memory database (data lost on restart)
- **After**: PostgreSQL persistent database (data survives restarts)

### Dependencies
- **Added**: PostgreSQL JDBC driver
- **Changed**: H2 moved to test scope only

### Configuration Files
- **application.properties**: Updated to use PostgreSQL
- **.env**: Updated with PostgreSQL connection details
- **docker-compose.yml**: Backend now depends on PostgreSQL

## Quick Start

### 1. Start PostgreSQL with Docker Compose

```bash
# Pull latest changes
git pull

# Start all services (PostgreSQL + Backend)
docker-compose up -d

# Check logs
docker-compose logs -f
```

The backend will automatically:
1. Wait for PostgreSQL to be healthy
2. Connect to PostgreSQL
3. Create tables on first run

### 2. Verify PostgreSQL Connection

```bash
# Check backend health
curl http://localhost:8080/actuator/health

# Check PostgreSQL is running
docker ps | grep postgres
```

### 3. Access PostgreSQL

```bash
# Connect via Docker
docker exec -it analytics-postgres psql -U analytics -d analyticsdb

# Or from host (if you have psql installed)
psql -h localhost -p 5432 -U analytics -d analyticsdb
```

## Database Schema

PostgreSQL will automatically create the following tables:

### users Table
```sql
CREATE TABLE users (
    id UUID PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
```

### events Table
```sql
CREATE TABLE events (
    id UUID PRIMARY KEY,
    type VARCHAR(50) NOT NULL,
    from_value VARCHAR(255) NOT NULL,
    to_value VARCHAR(255) NOT NULL
);
```

## Configuration Details

### Environment Variables (.env)

```bash
# PostgreSQL Connection
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/analyticsdb
SPRING_DATASOURCE_USERNAME=analytics
SPRING_DATASOURCE_PASSWORD=analytics123

# PostgreSQL Container
POSTGRES_DB=analyticsdb
POSTGRES_USER=analytics
POSTGRES_PASSWORD=analytics123
```

### application.properties

```properties
# PostgreSQL Configuration
spring.datasource.url=${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5432/analyticsdb}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME:analytics}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD:analytics123}
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA/Hibernate
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=update
```

## Data Persistence

### Data Volume

PostgreSQL data is stored in a Docker volume:

```bash
# List volumes
docker volume ls | grep postgres

# Inspect volume
docker volume inspect backend_postgres-data

# Backup data
docker exec analytics-postgres pg_dump -U analytics analyticsdb > backup.sql

# Restore data
docker exec -i analytics-postgres psql -U analytics -d analyticsdb < backup.sql
```

### Data Survives

✅ Container restarts: `docker-compose restart`
✅ Container recreates: `docker-compose down && docker-compose up`
✅ System reboots

❌ Data is lost if you run: `docker-compose down -v` (removes volumes)

## Development vs Production

### Development (Local)

Run PostgreSQL in Docker:

```bash
# Start only PostgreSQL
docker-compose up -d postgres

# Run backend locally
./gradlew bootRun
```

Backend will connect to `localhost:5432`.

### Production

Update `.env` with production credentials:

```bash
SPRING_DATASOURCE_URL=jdbc:postgresql://your-db-host:5432/productiondb
SPRING_DATASOURCE_USERNAME=prod_user
SPRING_DATASOURCE_PASSWORD=secure-password-here
```

## Testing

Tests use H2 in-memory database (no PostgreSQL required):

```bash
# Run tests (uses H2)
./gradlew test

# Tests use application-test.properties
# Which configures H2 automatically
```

## Common Operations

### View Data

```sql
-- Connect to database
docker exec -it analytics-postgres psql -U analytics -d analyticsdb

-- List all tables
\dt

-- View users
SELECT * FROM users;

-- View events
SELECT * FROM events;

-- Exit
\q
```

### Reset Database

```bash
# Option 1: Drop and recreate (loses data)
docker-compose down -v
docker-compose up -d

# Option 2: Drop tables only
docker exec -it analytics-postgres psql -U analytics -d analyticsdb -c "DROP TABLE IF EXISTS users, events CASCADE;"
docker-compose restart backend
```

### Change PostgreSQL Password

1. Update `.env`:
   ```bash
   POSTGRES_PASSWORD=new-secure-password
   SPRING_DATASOURCE_PASSWORD=new-secure-password
   ```

2. Recreate containers:
   ```bash
   docker-compose down -v
   docker-compose up -d
   ```

## Troubleshooting

### Backend Can't Connect to PostgreSQL

**Error**: `Connection refused` or `Unknown host`

**Solution**:
```bash
# Check PostgreSQL is running
docker ps | grep postgres

# Check PostgreSQL logs
docker logs analytics-postgres

# Verify network
docker network inspect backend_analytics-network
```

### PostgreSQL Authentication Failed

**Error**: `password authentication failed`

**Solution**:
1. Check credentials in `.env` match
2. Verify `POSTGRES_PASSWORD` = `SPRING_DATASOURCE_PASSWORD`
3. Recreate PostgreSQL container:
   ```bash
   docker-compose down -v postgres
   docker-compose up -d postgres
   ```

### Tables Not Created

**Error**: Tables don't exist in PostgreSQL

**Solution**:
1. Check `spring.jpa.hibernate.ddl-auto=update` in application.properties
2. Check backend logs: `docker logs analytics-backend`
3. Manually create tables or set `ddl-auto=create` (⚠️ drops existing data)

### Data Lost After Restart

**Cause**: Running `docker-compose down -v` removes volumes

**Solution**:
- Use `docker-compose down` (without `-v`)
- Backup regularly: `pg_dump -U analytics analyticsdb > backup.sql`

## Migration from H2 (Existing Data)

If you have existing H2 data to migrate:

### 1. Export from H2

```sql
-- Connect to H2 console
-- Execute: SELECT * FROM users;
-- Save as CSV
```

### 2. Import to PostgreSQL

```sql
-- Connect to PostgreSQL
docker exec -it analytics-postgres psql -U analytics -d analyticsdb

-- Copy data
COPY users(id, username, email, password, created_at, updated_at)
FROM '/path/to/users.csv'
DELIMITER ','
CSV HEADER;
```

## Performance Considerations

### Indexes

Consider adding indexes for better performance:

```sql
-- Username lookups
CREATE INDEX idx_users_username ON users(username);

-- Email lookups
CREATE INDEX idx_users_email ON users(email);

-- Event type queries
CREATE INDEX idx_events_type ON events(type);
```

### Connection Pooling

Spring Boot uses HikariCP by default (already optimized).

Optional tuning in application.properties:

```properties
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=20000
```

## Backup Strategy

### Automated Backups

Create a backup script:

```bash
#!/bin/bash
# backup.sh
DATE=$(date +%Y%m%d_%H%M%S)
docker exec analytics-postgres pg_dump -U analytics analyticsdb > "backup_${DATE}.sql"
echo "Backup created: backup_${DATE}.sql"
```

Run daily via cron:
```bash
0 2 * * * /path/to/backup.sh
```

### Restore Backup

```bash
# Stop backend
docker-compose stop backend

# Restore database
docker exec -i analytics-postgres psql -U analytics -d analyticsdb < backup_20260212.sql

# Start backend
docker-compose start backend
```

## Monitoring

### Database Size

```sql
-- Connect to PostgreSQL
docker exec -it analytics-postgres psql -U analytics -d analyticsdb

-- Check database size
SELECT pg_size_pretty(pg_database_size('analyticsdb'));

-- Check table sizes
SELECT 
    schemaname,
    tablename,
    pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) AS size
FROM pg_tables
WHERE schemaname = 'public'
ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;
```

### Active Connections

```sql
SELECT count(*) FROM pg_stat_activity WHERE datname = 'analyticsdb';
```

## Security Best Practices

1. **Change Default Password**
   ```bash
   # In production .env
   POSTGRES_PASSWORD=<strong-random-password>
   ```

2. **Restrict Network Access**
   ```yaml
   # docker-compose.yml
   postgres:
     ports:
       - "127.0.0.1:5432:5432"  # Only localhost
   ```

3. **Use Secrets**
   ```bash
   # Docker Swarm
   docker secret create postgres_password password.txt
   ```

4. **Regular Backups**
   - Automated daily backups
   - Test restore procedures
   - Store backups securely

## Summary

✅ **Migrated**: H2 → PostgreSQL
✅ **Persistent**: Data survives restarts
✅ **Production-Ready**: Suitable for deployment
✅ **Developer-Friendly**: Tests still use H2
✅ **Documented**: Complete migration guide

### Quick Commands

```bash
# Start everything
docker-compose up -d

# Check status
docker-compose ps

# View logs
docker-compose logs -f backend

# Access database
docker exec -it analytics-postgres psql -U analytics -d analyticsdb

# Backup
docker exec analytics-postgres pg_dump -U analytics analyticsdb > backup.sql

# Stop everything
docker-compose down
```

The application is now running on PostgreSQL! 🎉
