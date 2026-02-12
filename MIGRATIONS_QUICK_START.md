# Database Migrations - Quick Start

## What Changed

✅ **Flyway Installed**: Database migrations are now managed by Flyway
✅ **Initial Migrations Created**: V1 (users table) and V2 (events table)
✅ **Hibernate DDL Disabled**: Changed from `update` to `validate` (Flyway handles schema)
✅ **Version Controlled**: All schema changes tracked in Git

## Quick Commands

### Apply Migrations

```bash
# Start with fresh database (migrations auto-apply)
docker-compose up -d

# Check migration status
docker exec -it analytics-postgres psql -U analytics -d analyticsdb \
  -c "SELECT version, description, installed_on FROM flyway_schema_history;"
```

### Create New Migration

```bash
# 1. Create file (increment version)
touch src/main/resources/db/migration/V3__add_my_feature.sql

# 2. Write SQL
cat > src/main/resources/db/migration/V3__add_my_feature.sql << 'EOF'
ALTER TABLE users ADD COLUMN last_login TIMESTAMP;
CREATE INDEX idx_users_last_login ON users(last_login);
EOF

# 3. Apply
docker-compose restart backend

# 4. Verify
docker exec -it analytics-postgres psql -U analytics -d analyticsdb \
  -c "SELECT * FROM flyway_schema_history WHERE version = '3';"
```

### View Schema

```bash
# Connect to database
docker exec -it analytics-postgres psql -U analytics -d analyticsdb

# List tables
\dt

# Describe table
\d+ users

# View migration history
SELECT * FROM flyway_schema_history;

# Exit
\q
```

## Migration Files

### Location
```
src/main/resources/db/migration/
├── V1__create_users_table.sql    # Users table with indexes
└── V2__create_events_table.sql   # Events table with constraints
```

### Naming Pattern
```
V{version}__{description}.sql

Examples:
V1__create_users_table.sql
V2__create_events_table.sql
V3__add_user_status.sql
V4__create_sessions_table.sql
```

## Current Schema

### Users Table
```sql
CREATE TABLE users (
    id UUID PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
-- Indexes: username, email
```

### Events Table
```sql
CREATE TABLE events (
    id UUID PRIMARY KEY,
    type VARCHAR(50) NOT NULL CHECK (type IN ('NAVIGATION', 'ACTION')),
    from_value VARCHAR(255) NOT NULL,
    to_value VARCHAR(255) NOT NULL
);
-- Indexes: type
```

## Important Rules

### DO ✅
- Create new migration for each change
- Use sequential version numbers (V1, V2, V3...)
- Test locally before committing
- Write idempotent SQL (CREATE IF NOT EXISTS...)
- Add comments to document changes
- Commit migration files to Git

### DON'T ❌
- Never modify applied migrations
- Don't skip version numbers
- Don't delete migration files
- Don't use JPA entities in migrations
- Don't commit without testing

## Troubleshooting

### Migration Failed

```bash
# Check what failed
docker logs analytics-backend | grep -i flyway

# View failed migrations
docker exec -it analytics-postgres psql -U analytics -d analyticsdb \
  -c "SELECT * FROM flyway_schema_history WHERE success = false;"

# Fix: Delete failed entry and fix migration
docker exec -it analytics-postgres psql -U analytics -d analyticsdb \
  -c "DELETE FROM flyway_schema_history WHERE version = 'X';"

# Restart to retry
docker-compose restart backend
```

### Checksum Mismatch

```bash
# Cause: Modified an applied migration (never do this!)
# Fix: Revert the change or create new migration
git checkout src/main/resources/db/migration/VX__file.sql
```

### Start Fresh

```bash
# Nuclear option: Delete everything and start over
docker-compose down -v
docker-compose up -d
# Flyway will apply all migrations from scratch
```

## Example: Adding a Column

```bash
# 1. Create migration
cat > src/main/resources/db/migration/V3__add_user_phone.sql << 'EOF'
-- Add phone column to users table
ALTER TABLE users ADD COLUMN phone VARCHAR(20);

-- Add index
CREATE INDEX IF NOT EXISTS idx_users_phone ON users(phone);

-- Add comment
COMMENT ON COLUMN users.phone IS 'User phone number';
EOF

# 2. Apply
docker-compose restart backend

# 3. Verify
docker exec -it analytics-postgres psql -U analytics -d analyticsdb \
  -c "\d+ users"

# 4. Commit
git add src/main/resources/db/migration/V3__add_user_phone.sql
git commit -m "feat: Add phone column to users table"
```

## Configuration

### application.properties
```properties
# Hibernate validates against Flyway schema
spring.jpa.hibernate.ddl-auto=validate

# Flyway enabled
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
spring.flyway.baseline-on-migrate=true
```

## Benefits

✅ **Version Control**: All schema changes in Git
✅ **Reproducible**: Same schema across environments
✅ **Auditable**: Know who changed what and when
✅ **Safe**: Validates before applying
✅ **Automated**: Runs on application startup
✅ **Team-Friendly**: No more manual SQL scripts

## Learn More

For detailed information, see:
- [DATABASE_MIGRATIONS.md](DATABASE_MIGRATIONS.md) - Complete guide
- [Flyway Documentation](https://flywaydb.org/documentation/)

## Summary

Database migrations are now managed by Flyway:
1. Schema changes → Create migration file
2. Commit to Git → Version controlled
3. Deploy → Flyway applies automatically
4. Validate → Hibernate validates schema

Never manually modify the database schema again! 🚀
