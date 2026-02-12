# Database Migrations Guide

This guide explains how to use Flyway database migrations to manage schema changes.

## Overview

The project uses **Flyway** for database version control and schema migrations. This ensures:
- ✅ **Version Control**: Track all database changes in Git
- ✅ **Reproducibility**: Apply same changes across environments
- ✅ **Auditing**: Know who changed what and when
- ✅ **Safety**: Validate schema before application starts

## How It Works

### Migration Files

Migrations are SQL files located in `src/main/resources/db/migration/`

**Naming Convention:**
```
V{version}__{description}.sql
```

Examples:
- `V1__create_users_table.sql`
- `V2__create_events_table.sql`
- `V3__add_user_role_column.sql`

### Version Numbers

- **V1, V2, V3**: Major migrations (tables, columns)
- **V1.1, V1.2**: Minor migrations (indexes, constraints)
- Use sequential numbering
- Never modify applied migrations!

### Flyway Metadata

Flyway tracks migrations in the `flyway_schema_history` table:

```sql
SELECT * FROM flyway_schema_history;
```

Shows:
- Version number
- Description
- Script name
- Checksum
- Execution time
- Success status

## Current Migrations

### V1: Create Users Table

**File:** `V1__create_users_table.sql`

Creates the users table with:
- UUID primary key
- Username (unique, indexed)
- Email (unique, indexed)
- Password (BCrypt encrypted)
- Timestamps (created_at, updated_at)

### V2: Create Events Table

**File:** `V2__create_events_table.sql`

Creates the events table with:
- UUID primary key
- Event type (NAVIGATION or ACTION)
- From/To values
- Type constraint check
- Type index for queries

## Creating New Migrations

### 1. Create Migration File

```bash
# Navigate to migrations directory
cd src/main/resources/db/migration

# Create new migration (increment version number)
touch V3__add_user_active_status.sql
```

### 2. Write SQL Migration

```sql
-- V3__add_user_active_status.sql
ALTER TABLE users ADD COLUMN active BOOLEAN DEFAULT TRUE NOT NULL;

CREATE INDEX IF NOT EXISTS idx_users_active ON users(active);

COMMENT ON COLUMN users.active IS 'User account active status';
```

### 3. Test Migration Locally

```bash
# Build the application
./gradlew clean build

# Start with Docker Compose
docker-compose up -d

# Check Flyway applied the migration
docker exec -it analytics-postgres psql -U analytics -d analyticsdb \
  -c "SELECT version, description, success FROM flyway_schema_history ORDER BY installed_rank;"
```

### 4. Commit Migration

```bash
git add src/main/resources/db/migration/V3__add_user_active_status.sql
git commit -m "feat: Add active status column to users table"
```

## Migration Best Practices

### DO

✅ **Write Idempotent Migrations**
```sql
-- Good: Safe to run multiple times
CREATE TABLE IF NOT EXISTS users (...);
ALTER TABLE users ADD COLUMN IF NOT EXISTS active BOOLEAN;
```

✅ **Add Indexes**
```sql
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
```

✅ **Add Comments**
```sql
COMMENT ON TABLE users IS 'Application users';
COMMENT ON COLUMN users.email IS 'User email address';
```

✅ **Use Constraints**
```sql
ALTER TABLE events ADD CONSTRAINT chk_event_type 
  CHECK (type IN ('NAVIGATION', 'ACTION'));
```

✅ **Test Rollback Plan**
```sql
-- Keep rollback script in comments
-- ROLLBACK:
-- ALTER TABLE users DROP COLUMN active;
```

### DON'T

❌ **Never Modify Applied Migrations**
- Once applied in any environment, migrations are immutable
- Create new migration to fix issues

❌ **Don't Use Application Objects**
- Don't reference JPA entities
- Write pure SQL

❌ **Avoid Data Migrations in Schema Migrations**
- Separate schema (DDL) from data (DML)
- Use versioned data migrations if needed

❌ **Don't Skip Version Numbers**
- V1, V2, V3 ✅
- V1, V3, V5 ❌

## Common Migration Scenarios

### Add Column

```sql
-- V3__add_user_phone.sql
ALTER TABLE users ADD COLUMN phone VARCHAR(20);
CREATE INDEX IF NOT EXISTS idx_users_phone ON users(phone);
COMMENT ON COLUMN users.phone IS 'User phone number';
```

### Modify Column

```sql
-- V4__increase_username_length.sql
ALTER TABLE users ALTER COLUMN username TYPE VARCHAR(100);
```

### Add Foreign Key

```sql
-- V5__add_user_events_relation.sql
ALTER TABLE events ADD COLUMN user_id UUID;
ALTER TABLE events ADD CONSTRAINT fk_events_user 
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
CREATE INDEX IF NOT EXISTS idx_events_user_id ON events(user_id);
```

### Create New Table

```sql
-- V6__create_sessions_table.sql
CREATE TABLE sessions (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    token VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_sessions_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_sessions_token ON sessions(token);
CREATE INDEX idx_sessions_user_id ON sessions(user_id);
```

### Add Enum Type

```sql
-- V7__create_user_role_enum.sql
CREATE TYPE user_role AS ENUM ('USER', 'ADMIN', 'MODERATOR');

ALTER TABLE users ADD COLUMN role user_role DEFAULT 'USER' NOT NULL;
CREATE INDEX idx_users_role ON users(role);
```

### Data Migration

```sql
-- V8__populate_default_roles.sql
UPDATE users SET role = 'ADMIN' WHERE email = 'admin@example.com';
UPDATE users SET role = 'USER' WHERE role IS NULL;
```

## Flyway Configuration

### application.properties

```properties
# Flyway Configuration
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
spring.flyway.baseline-on-migrate=true
spring.flyway.baseline-version=0
spring.flyway.validate-on-migrate=true
```

### Configuration Options

| Property | Value | Description |
|----------|-------|-------------|
| `enabled` | `true` | Enable Flyway migrations |
| `locations` | `classpath:db/migration` | Migration scripts location |
| `baseline-on-migrate` | `true` | Create baseline for existing databases |
| `baseline-version` | `0` | Version to use as baseline |
| `validate-on-migrate` | `true` | Validate checksums before migrating |

## Checking Migration Status

### View Applied Migrations

```sql
-- Connect to database
docker exec -it analytics-postgres psql -U analytics -d analyticsdb

-- View migration history
SELECT 
    installed_rank,
    version,
    description,
    type,
    script,
    checksum,
    installed_on,
    execution_time,
    success
FROM flyway_schema_history
ORDER BY installed_rank;
```

### Check Pending Migrations

```bash
# Build application to check
./gradlew clean build

# Check logs for Flyway output
# Look for: "Migrating schema `public` to version X"
```

## Troubleshooting

### Migration Failed

**Problem:** Migration failed and application won't start

**Solution:**
```sql
-- 1. Check failed migration
SELECT * FROM flyway_schema_history WHERE success = false;

-- 2. Manual repair (if safe)
DELETE FROM flyway_schema_history WHERE version = 'X';

-- 3. Fix the migration file and retry
```

### Checksum Mismatch

**Problem:** `Validate failed: Migration checksum mismatch`

**Cause:** Migration file was modified after being applied

**Solution:**
```sql
-- Option 1: Repair checksum (if change is safe)
-- Run: ./gradlew flywayRepair

-- Option 2: Create new migration to fix issue
-- Don't modify existing migration!
```

### Missing Migration

**Problem:** Gap in version numbers

**Solution:**
- Check if migration file was deleted
- Restore from Git: `git checkout V3__*.sql`
- Never delete applied migrations

### Baseline Required

**Problem:** Existing database without Flyway history

**Solution:**
Already configured with `baseline-on-migrate=true`
- Flyway creates baseline automatically
- Applies only new migrations

## Development Workflow

### Local Development

```bash
# 1. Create new migration
touch src/main/resources/db/migration/V3__my_changes.sql

# 2. Write SQL
vim src/main/resources/db/migration/V3__my_changes.sql

# 3. Test locally
docker-compose down -v  # Fresh database
docker-compose up -d    # Apply migrations

# 4. Verify
docker exec -it analytics-postgres psql -U analytics -d analyticsdb \
  -c "SELECT * FROM flyway_schema_history;"

# 5. Commit
git add src/main/resources/db/migration/V3__my_changes.sql
git commit -m "feat: Add my changes to database"
```

### Team Workflow

```bash
# 1. Pull latest changes
git pull origin main

# 2. Restart to apply new migrations
docker-compose restart backend

# 3. Check applied
docker logs analytics-backend | grep Flyway
```

## CI/CD Integration

### Build Pipeline

```yaml
# .github/workflows/build.yml
- name: Test with Flyway
  run: |
    docker-compose up -d postgres
    ./gradlew clean build
    # Flyway migrations applied automatically
```

### Deployment Pipeline

```yaml
# .github/workflows/deploy.yml
- name: Deploy with Flyway
  run: |
    # Migrations applied on application startup
    docker-compose up -d
```

## Production Deployment

### Pre-Deployment

1. **Review migrations**
   ```bash
   ls -la src/main/resources/db/migration/
   ```

2. **Test on staging**
   ```bash
   # Deploy to staging first
   # Verify migrations work correctly
   ```

3. **Backup production database**
   ```bash
   docker exec analytics-postgres pg_dump -U analytics analyticsdb > backup.sql
   ```

### Deployment

```bash
# 1. Deploy new version
docker-compose pull
docker-compose up -d

# 2. Check migration status
docker logs analytics-backend | grep Flyway

# 3. Verify schema
docker exec -it analytics-postgres psql -U analytics -d analyticsdb \
  -c "\dt"  # List tables
```

### Rollback

If migration fails:

```bash
# 1. Stop application
docker-compose stop backend

# 2. Restore database backup
docker exec -i analytics-postgres psql -U analytics -d analyticsdb < backup.sql

# 3. Revert code
git revert <commit-hash>

# 4. Redeploy
docker-compose up -d
```

## Advanced Topics

### Repeatable Migrations

For views, functions, procedures:

```sql
-- R__create_user_stats_view.sql
CREATE OR REPLACE VIEW user_stats AS
SELECT 
    COUNT(*) as total_users,
    COUNT(*) FILTER (WHERE active = true) as active_users
FROM users;
```

### Callbacks

```sql
-- beforeMigrate.sql
-- Runs before each migration
SET statement_timeout = '60s';
```

### Multiple Databases

```properties
# Profile-specific migrations
spring.flyway.locations=classpath:db/migration,classpath:db/migration/${spring.profiles.active}
```

## Monitoring

### Flyway Actuator Endpoint

```bash
# Check Flyway info via actuator
curl http://localhost:8080/actuator/flyway
```

### Migration Performance

```sql
SELECT 
    version,
    description,
    execution_time,
    installed_on
FROM flyway_schema_history
ORDER BY execution_time DESC
LIMIT 10;
```

## Summary

✅ **Migrations Configured**: Flyway tracks all schema changes
✅ **Version Control**: All migrations in Git
✅ **Automated**: Runs on application startup
✅ **Safe**: Validates before applying
✅ **Auditable**: Complete history in database

### Quick Reference

```bash
# Create migration
touch src/main/resources/db/migration/V{X}__{description}.sql

# Apply migrations
docker-compose up -d

# Check status
docker exec -it analytics-postgres psql -U analytics -d analyticsdb \
  -c "SELECT * FROM flyway_schema_history;"

# View schema
docker exec -it analytics-postgres psql -U analytics -d analyticsdb -c "\d+ users"
```

Database migrations are now fully configured and ready to use! 🚀
