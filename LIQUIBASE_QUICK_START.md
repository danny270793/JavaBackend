# Liquibase - Quick Start Guide

## What is Liquibase?

Liquibase is a database migration tool that tracks, manages, and applies database schema changes using changelog files.

## Project Structure

```
src/main/resources/db/changelog/
├── db.changelog-master.yaml              # Master file (includes all changesets)
└── changes/
    ├── 001-create-users-table.yaml       # Users table
    └── 002-create-events-table.yaml      # Events table
```

## Quick Commands

### Apply Migrations

```bash
# Start with fresh database (migrations auto-apply)
docker-compose up -d

# Check migration status
docker exec -it analytics-postgres psql -U analytics -d analyticsdb \
  -c "SELECT id, author, filename, dateexecuted FROM DATABASECHANGELOG ORDER BY dateexecuted;"
```

### Create New Migration

**Step 1: Create changeset file (use numeric prefix for ordering)**
```bash
touch src/main/resources/db/changelog/changes/003-add-user-phone.yaml
```

**Step 2: Write YAML changeset**
```yaml
databaseChangeLog:
  - changeSet:
      id: 003-add-user-phone
      author: your-name
      changes:
        - addColumn:
            tableName: users
            columns:
              - column:
                  name: phone
                  type: varchar(20)
        - createIndex:
            indexName: idx_users_phone
            tableName: users
            columns:
              - column:
                  name: phone
      rollback:
        - dropIndex:
            indexName: idx_users_phone
            tableName: users
        - dropColumn:
            tableName: users
            columnName: phone
```

**Step 3: Apply** (automatically loaded via `includeAll`)
```bash
docker-compose restart backend
# The master changelog automatically includes all files from changes/ directory
```

**Step 4: Commit**
```bash
git add src/main/resources/db/changelog/changes/003-add-user-phone.yaml
git commit -m "feat: Add phone column to users table"
```

**Note:** The master changelog uses `includeAll` to automatically load all YAML files from the `changes/` directory in alphabetical order. No need to manually update the master file!

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

## Common Changeset Types

### Add Column
```yaml
- addColumn:
    tableName: users
    columns:
      - column:
          name: status
          type: varchar(20)
          defaultValue: active
```

### Drop Column
```yaml
- dropColumn:
    tableName: users
    columnName: status
```

### Add Index
```yaml
- createIndex:
    indexName: idx_users_status
    tableName: users
    columns:
      - column:
          name: status
```

### Add Foreign Key
```yaml
- addForeignKeyConstraint:
    constraintName: fk_events_user
    baseTableName: events
    baseColumnNames: user_id
    referencedTableName: users
    referencedColumnNames: id
    onDelete: CASCADE
```

### Insert Data
```yaml
- insert:
    tableName: users
    columns:
      - column:
          name: id
          value: 550e8400-e29b-41d4-a716-446655440000
      - column:
          name: username
          value: admin
```

### Update Data
```yaml
- update:
    tableName: users
    columns:
      - column:
          name: status
          value: active
    where: "username = 'admin'"
```

### Create Table
```yaml
- createTable:
    tableName: roles
    columns:
      - column:
          name: id
          type: uuid
          constraints:
            primaryKey: true
      - column:
          name: name
          type: varchar(50)
          constraints:
            nullable: false
```

## View Database

### Connect to PostgreSQL

```bash
docker exec -it analytics-postgres psql -U analytics -d analyticsdb
```

### Useful PostgreSQL Commands

```sql
-- List tables
\dt

-- Describe table
\d+ users

-- View migration history
SELECT * FROM DATABASECHANGELOG;

-- Count changesets
SELECT COUNT(*) FROM DATABASECHANGELOG;

-- Latest changesets
SELECT id, author, dateexecuted 
FROM DATABASECHANGELOG 
ORDER BY dateexecuted DESC 
LIMIT 5;

-- Exit
\q
```

## Troubleshooting

### Migration Failed

```bash
# Check logs
docker logs analytics-backend | grep -i liquibase

# View failed changesets
docker exec -it analytics-postgres psql -U analytics -d analyticsdb \
  -c "SELECT * FROM DATABASECHANGELOG WHERE exectype = 'FAILED';"

# Clear and retry
docker exec -it analytics-postgres psql -U analytics -d analyticsdb \
  -c "DELETE FROM DATABASECHANGELOG WHERE id = 'failed-changeset-id';"

docker-compose restart backend
```

### Lock Not Released

```bash
# Clear lock
docker exec -it analytics-postgres psql -U analytics -d analyticsdb \
  -c "DELETE FROM DATABASECHANGELOGLOCK;"
```

### Start Fresh

```bash
# Delete everything and start over
docker-compose down -v
docker-compose up -d
# Liquibase will apply all changesets from scratch
```

## Important Rules

### DO ✅
- Create new changeset for each change
- Use descriptive changeset IDs
- Include rollback scripts
- Test locally before committing
- Add to master changelog
- Commit changeset files to Git

### DON'T ❌
- Never modify applied changesets
- Don't delete changeset files
- Don't use duplicate IDs
- Don't skip adding to master changelog

## Examples

### Example 1: Add Active Column

```yaml
databaseChangeLog:
  - changeSet:
      id: 003-add-user-active
      author: analytics-backend
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
      rollback:
        - dropColumn:
            tableName: users
            columnName: active
```

### Example 2: Add Timestamps to Events

```yaml
databaseChangeLog:
  - changeSet:
      id: 004-add-event-timestamps
      author: analytics-backend
      changes:
        - addColumn:
            tableName: events
            columns:
              - column:
                  name: created_at
                  type: timestamp
                  defaultValueComputed: CURRENT_TIMESTAMP
                  constraints:
                    nullable: false
```

### Example 3: Create Association Table

```yaml
databaseChangeLog:
  - changeSet:
      id: 005-create-user-roles-table
      author: analytics-backend
      changes:
        - createTable:
            tableName: user_roles
            columns:
              - column:
                  name: user_id
                  type: uuid
                  constraints:
                    nullable: false
              - column:
                  name: role
                  type: varchar(50)
                  constraints:
                    nullable: false
        - addPrimaryKey:
            tableName: user_roles
            columnNames: user_id, role
            constraintName: pk_user_roles
        - addForeignKeyConstraint:
            constraintName: fk_user_roles_user
            baseTableName: user_roles
            baseColumnNames: user_id
            referencedTableName: users
            referencedColumnNames: id
```

## Configuration

### application.properties
```properties
# Hibernate validates against Liquibase schema
spring.jpa.hibernate.ddl-auto=validate

# Liquibase enabled
spring.liquibase.enabled=true
spring.liquibase.change-log=classpath:db/changelog/db.changelog-master.yaml
```

## Benefits

✅ **Version Control**: All schema changes in Git
✅ **Database Independent**: Works with PostgreSQL, MySQL, Oracle, etc.
✅ **Rollback Support**: Built-in rollback functionality
✅ **Multiple Formats**: YAML, XML, JSON, or SQL
✅ **Advanced Features**: Contexts, labels, preconditions
✅ **Team-Friendly**: No more manual SQL scripts

## Learn More

- [DATABASE_MIGRATIONS.md](DATABASE_MIGRATIONS.md) - Complete guide
- [Liquibase Documentation](https://docs.liquibase.com/)

## Summary

Database migrations are now managed by Liquibase:
1. Schema changes → Create YAML changeset
2. Add to master → Include in db.changelog-master.yaml
3. Commit to Git → Version controlled
4. Deploy → Liquibase applies automatically
5. Rollback → Use rollback scripts if needed

Never manually modify the database schema again! 🚀
