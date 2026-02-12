# Database Migrations Guide - Liquibase

This guide explains how to use Liquibase database migrations to manage schema changes.

## Overview

The project uses **Liquibase** for database version control and schema migrations. This ensures:
- ✅ **Version Control**: Track all database changes in Git
- ✅ **Reproducibility**: Apply same changes across environments
- ✅ **Rollback Support**: Easy rollback with built-in commands
- ✅ **Multiple Formats**: YAML, XML, JSON, or SQL
- ✅ **Safety**: Validates schema before application starts

## How It Works

### Changelog Files

Liquibase uses changelog files in `src/main/resources/db/changelog/`

**Structure:**
```
db/changelog/
├── db.changelog-master.yaml           # Master file
└── changes/
    ├── 001-create-users-table.yaml    # Changeset 1
    └── 002-create-events-table.yaml   # Changeset 2
```

### Master Changelog

The master file automatically includes all changesets from the `changes/` directory:

```yaml
databaseChangeLog:
  - includeAll:
      path: db/changelog/changes/
      relativeToChangelogFile: false
```

Liquibase loads all YAML files from `db/changelog/changes/` in alphabetical order, which is why we use numeric prefixes (001-, 002-, etc.).

### Changeset Format

Each changeset has a unique ID and author:

```yaml
databaseChangeLog:
  - changeSet:
      id: 001-create-users-table
      author: analytics-backend
      changes:
        - createTable:
            tableName: users
            columns:
              - column:
                  name: id
                  type: uuid
```

### Liquibase Metadata

Liquibase tracks migrations in `DATABASECHANGELOG` and `DATABASECHANGELOGLOCK` tables:

```sql
SELECT * FROM DATABASECHANGELOG;
```

Shows:
- ID
- Author
- Filename
- Date executed
- Order executed
- Checksum
- Description

## Current Migrations

### 001: Create Users Table

**File:** `001-create-users-table.yaml`

Creates the users table with:
- UUID primary key
- Username (unique, indexed)
- Email (unique, indexed)
- Password (BCrypt encrypted)
- Timestamps (created_at, updated_at)
- Rollback support

### 002: Create Events Table

**File:** `002-create-events-table.yaml`

Creates the events table with:
- UUID primary key
- Event type with CHECK constraint
- From/To values
- Type index
- Rollback support

## Creating New Migrations

### 1. Create Changeset File

```bash
# Navigate to changelog directory
cd src/main/resources/db/changelog/changes

# Create new changeset (increment number)
touch 003-add-user-active-status.yaml
```

### 2. Write YAML Changeset

```yaml
databaseChangeLog:
  - changeSet:
      id: 003-add-user-active-status
      author: your-name
      changes:
        - addColumn:
            tableName: users
            columns:
              - column:
                  name: active
                  type: boolean
                  defaultValueBoolean: true
                  constraints:
                    nullable: false
        - createIndex:
            indexName: idx_users_active
            tableName: users
            columns:
              - column:
                  name: active
      rollback:
        - dropIndex:
            indexName: idx_users_active
            tableName: users
        - dropColumn:
            tableName: users
            columnName: active
```

### 3. Test Migration (Auto-loaded via includeAll)

```bash
# Build the application
./gradlew clean build

# Start with Docker Compose
docker-compose up -d

# Check Liquibase applied the migration
docker exec -it analytics-postgres psql -U analytics -d analyticsdb \
  -c "SELECT id, author, filename, dateexecuted FROM DATABASECHANGELOG ORDER BY dateexecuted;"
```

### 5. Commit Migration

```bash
git add src/main/resources/db/changelog/changes/003-add-user-active-status.yaml
git commit -m "feat: Add active status column to users table"
```

## Migration Best Practices

### DO ✅

**Use Descriptive IDs**
```yaml
changeSet:
  id: 001-create-users-table  # Good
  id: 1                        # Bad
```

**Write Rollback Scripts**
```yaml
rollback:
  - dropTable:
      tableName: users
```

**Use Built-in Changes**
```yaml
# Good: Use Liquibase abstractions
- addColumn:
    tableName: users
    columns:
      - column:
          name: active
          type: boolean

# Avoid: Raw SQL when possible
- sql:
    sql: "ALTER TABLE users ADD COLUMN active BOOLEAN"
```

**Add Context and Labels**
```yaml
changeSet:
  id: 001-create-users-table
  author: analytics-backend
  context: production
  labels: schema,users
```

**Use Preconditions**
```yaml
changeSet:
  id: 002-add-index
  preConditions:
    - not:
        - indexExists:
            indexName: idx_users_email
  changes:
    - createIndex:
        indexName: idx_users_email
        tableName: users
```

### DON'T ❌

❌ **Never Modify Applied Changesets**
- Once applied, changesets are immutable
- Create new changeset to fix issues

❌ **Don't Use Complex Logic**
- Keep changesets simple
- Split complex changes into multiple changesets

❌ **Avoid Raw SQL**
- Use Liquibase abstractions for database independence
- Use SQL only when necessary

## Common Migration Scenarios

### Add Column

```yaml
databaseChangeLog:
  - changeSet:
      id: 003-add-user-phone
      author: analytics-backend
      changes:
        - addColumn:
            tableName: users
            columns:
              - column:
                  name: phone
                  type: varchar(20)
      rollback:
        - dropColumn:
            tableName: users
            columnName: phone
```

### Modify Column

```yaml
databaseChangeLog:
  - changeSet:
      id: 004-increase-username-length
      author: analytics-backend
      changes:
        - modifyDataType:
            tableName: users
            columnName: username
            newDataType: varchar(100)
```

### Add Foreign Key

```yaml
databaseChangeLog:
  - changeSet:
      id: 005-add-user-events-relation
      author: analytics-backend
      changes:
        - addColumn:
            tableName: events
            columns:
              - column:
                  name: user_id
                  type: uuid
        - addForeignKeyConstraint:
            constraintName: fk_events_user
            baseTableName: events
            baseColumnNames: user_id
            referencedTableName: users
            referencedColumnNames: id
            onDelete: CASCADE
        - createIndex:
            indexName: idx_events_user_id
            tableName: events
            columns:
              - column:
                  name: user_id
```

### Create New Table

```yaml
databaseChangeLog:
  - changeSet:
      id: 006-create-sessions-table
      author: analytics-backend
      changes:
        - createTable:
            tableName: sessions
            columns:
              - column:
                  name: id
                  type: uuid
                  constraints:
                    primaryKey: true
                    nullable: false
              - column:
                  name: user_id
                  type: uuid
                  constraints:
                    nullable: false
              - column:
                  name: token
                  type: varchar(255)
                  constraints:
                    nullable: false
              - column:
                  name: expires_at
                  type: timestamp
                  constraints:
                    nullable: false
        - createIndex:
            indexName: idx_sessions_token
            tableName: sessions
            columns:
              - column:
                  name: token
```

### Data Migration

```yaml
databaseChangeLog:
  - changeSet:
      id: 007-populate-default-roles
      author: analytics-backend
      changes:
        - update:
            tableName: users
            columns:
              - column:
                  name: role
                  value: ADMIN
            where: "email = 'admin@example.com'"
```

### Use SQL (When Necessary)

```yaml
databaseChangeLog:
  - changeSet:
      id: 008-create-custom-function
      author: analytics-backend
      changes:
        - sql:
            sql: |
              CREATE OR REPLACE FUNCTION update_updated_at_column()
              RETURNS TRIGGER AS $$
              BEGIN
                NEW.updated_at = NOW();
                RETURN NEW;
              END;
              $$ LANGUAGE plpgsql;
      rollback:
        - sql:
            sql: "DROP FUNCTION IF EXISTS update_updated_at_column()"
```

## Liquibase Configuration

### application.properties

```properties
# Liquibase Configuration
spring.liquibase.enabled=true
spring.liquibase.change-log=classpath:db/changelog/db.changelog-master.yaml
spring.liquibase.drop-first=false
```

### Configuration Options

| Property | Value | Description |
|----------|-------|-------------|
| `enabled` | `true` | Enable Liquibase migrations |
| `change-log` | `classpath:db/changelog/db.changelog-master.yaml` | Master changelog location |
| `drop-first` | `false` | Drop all database objects before migrating (dev only!) |

## Checking Migration Status

### View Applied Changesets

```sql
-- Connect to database
docker exec -it analytics-postgres psql -U analytics -d analyticsdb

-- View migration history
SELECT 
    id,
    author,
    filename,
    dateexecuted,
    orderexecuted,
    exectype,
    md5sum
FROM DATABASECHANGELOG
ORDER BY dateexecuted;
```

### Check Pending Migrations

```bash
# Build application to check
./gradlew clean build

# Check logs for Liquibase output
docker logs analytics-backend | grep -i liquibase
```

## Rollback

### Rollback Last Changeset

```bash
# Using Liquibase CLI (if installed)
liquibase rollback-count 1

# Or via SQL (not recommended)
docker exec -it analytics-postgres psql -U analytics -d analyticsdb \
  -c "DELETE FROM DATABASECHANGELOG WHERE id = '003-add-user-active-status';"
```

### Rollback to Specific Tag

```yaml
# Tag a specific point
databaseChangeLog:
  - changeSet:
      id: tag-v1.0
      author: analytics-backend
      changes:
        - tagDatabase:
            tag: v1.0
```

```bash
# Rollback to tag
liquibase rollback v1.0
```

## Troubleshooting

### Changeset Failed

**Problem:** Changeset failed and application won't start

**Solution:**
```sql
-- 1. Check failed changeset
SELECT * FROM DATABASECHANGELOG WHERE exectype = 'FAILED';

-- 2. Clear lock (if stuck)
DELETE FROM DATABASECHANGELOGLOCK;

-- 3. Remove failed changeset entry
DELETE FROM DATABASECHANGELOG WHERE id = 'failed-changeset-id';

-- 4. Fix the changeset file and retry
```

### Checksum Mismatch

**Problem:** `Validation Failed: Checksum mismatch`

**Cause:** Changeset file was modified after being applied

**Solution:**
```bash
# Option 1: Clear checksums (if change is safe)
liquibase clear-checksums

# Option 2: Create new changeset to fix issue
# Don't modify existing changeset!
```

### Lock Not Released

**Problem:** `Waiting for changelog lock`

**Solution:**
```sql
-- Force release lock
DELETE FROM DATABASECHANGELOGLOCK;
```

## Development Workflow

### Local Development

```bash
# 1. Create new changeset
touch src/main/resources/db/changelog/changes/003-my-changes.yaml

# 2. Write YAML
vim src/main/resources/db/changelog/changes/003-my-changes.yaml

# 3. Add to master
vim src/main/resources/db/changelog/db.changelog-master.yaml

# 4. Test locally
docker-compose down -v  # Fresh database
docker-compose up -d    # Apply migrations

# 5. Verify
docker exec -it analytics-postgres psql -U analytics -d analyticsdb \
  -c "SELECT * FROM DATABASECHANGELOG WHERE id = '003-my-changes';"

# 6. Commit
git add src/main/resources/db/changelog/
git commit -m "feat: Add my changes to database"
```

### Team Workflow

```bash
# 1. Pull latest changes
git pull origin main

# 2. Restart to apply new migrations
docker-compose restart backend

# 3. Check applied
docker logs analytics-backend | grep Liquibase
```

## Advanced Features

### Contexts

Run different changesets for different environments:

```yaml
changeSet:
  id: 001-dev-data
  author: analytics-backend
  context: dev
  changes:
    - insert:
        tableName: users
        columns:
          - column:
              name: username
              value: testuser
```

```properties
# application-dev.properties
spring.liquibase.contexts=dev,default
```

### Labels

Organize changesets by feature:

```yaml
changeSet:
  id: 001-auth-feature
  author: analytics-backend
  labels: authentication,security
```

### Preconditions

Only run if conditions met:

```yaml
changeSet:
  id: 002-conditional-change
  preConditions:
    - tableExists:
        tableName: users
    - not:
        columnExists:
          tableName: users
          columnName: active
```

### Custom Properties

Define reusable properties:

```yaml
databaseChangeLog:
  - property:
      name: users.tablename
      value: users
  - changeSet:
      id: 001-use-property
      changes:
        - createTable:
            tableName: ${users.tablename}
```

## Liquibase CLI Commands

If you have Liquibase CLI installed:

```bash
# Generate changelog from existing database
liquibase generate-changelog

# Generate diff between databases
liquibase diff

# Rollback last N changes
liquibase rollback-count N

# Rollback to date
liquibase rollback-to-date 2026-01-01

# Update SQL (see what will run)
liquibase update-sql

# Validate changelog
liquibase validate
```

## Summary

✅ **Migrations Configured**: Liquibase tracks all schema changes
✅ **Version Control**: All migrations in Git
✅ **Automated**: Runs on application startup
✅ **Rollback Support**: Built-in rollback commands
✅ **Flexible**: YAML, XML, JSON, or SQL formats
✅ **Database Independent**: Works across different databases

### Quick Reference

```bash
# Create changeset
touch src/main/resources/db/changelog/changes/00X-description.yaml

# Apply migrations
docker-compose up -d

# Check status
docker exec -it analytics-postgres psql -U analytics -d analyticsdb \
  -c "SELECT id, author, dateexecuted FROM DATABASECHANGELOG;"

# View schema
docker exec -it analytics-postgres psql -U analytics -d analyticsdb -c "\d+ users"
```

Database migrations are now fully configured with Liquibase! 🚀
