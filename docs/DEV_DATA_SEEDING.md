# Development Data Seeding

## Overview

In **dev profile**, the Analytics Backend automatically populates the database with random test data on startup. This provides realistic data for development and testing without manual data entry.

**Admin user is created in ALL profiles** (dev, prod, test) via `AdminUserSeeder` for consistent administrative access.

## Features

### Admin User Creation (All Profiles)
- ✅ **Automatic Creation**: Admin user created on first startup
- ✅ **Consistent Credentials**: Same admin/admin across environments
- ✅ **Idempotent**: Skips if admin already exists
- ✅ **Fixed UUID**: Predictable admin user ID across deployments

### Test Data Seeding (Dev Profile Only)

- ✅ **Automatic Seeding**: Runs automatically when `dev` profile is active
- ✅ **Random Data**: Generates realistic users and events
- ✅ **Smart Skip**: Detects existing data and skips seeding to avoid duplicates
- ✅ **Configurable**: 10 users with 10-60 events each
- ✅ **Realistic Names**: Uses common first and last names
- ✅ **Varied Events**: Random navigation and action events across different routes
- ✅ **Ready Credentials**: Test users with known passwords for immediate login

## Generated Data

### Admin User (All Profiles)

**Created by**: `AdminUserSeeder`  
**Active in**: dev, prod, test profiles  

**Credentials**:
- Username: `admin`
- Password: `admin`
- Email: `admin@analytics.local`
- UUID: `00000000-0000-0000-0000-000000000001` (fixed)

⚠️ **Security Warning**: Change admin password before production deployment!

### Test Users (Dev Profile Only)

**Username Pattern**: `user1`, `user2`, `user3`, ..., `user10`  
**Password**: `password` (for all test users)  
**Email Pattern**: `firstname.lastname{N}@example.com`

Examples:
- Username: `user1`, Email: `alice.smith1@example.com`, Password: `password`
- Username: `user2`, Email: `bob.johnson2@example.com`, Password: `password`
- Username: `user3`, Email: `charlie.williams3@example.com`, Password: `password`

### Events (10-60 per user, ~400 total)

**Event Types**: NAVIGATION, ACTION  
**Routes**: Random from 18 different routes:
- `/home`, `/dashboard`, `/profile`, `/settings`, `/analytics`, `/reports`
- `/users`, `/events`, `/admin`, `/api-docs`, `/help`, `/about`
- `/products`, `/cart`, `/checkout`, `/orders`, `/search`, `/notifications`

**Characteristics**:
- Each user gets between 10 and 60 random events
- Events have different types (NAVIGATION or ACTION)
- `from` and `to` routes are always different
- Events are associated with their user (userId field)

## Activation

### Automatic (Dev Profile)

The seeder runs automatically when starting with dev profile:

```bash
# Option 1: Using Gradle
./gradlew bootRun --args='--spring.profiles.active=dev'

# Option 2: Using environment variable
export SPRING_PROFILES_ACTIVE=dev
./gradlew bootRun

# Option 3: Using Docker Compose
docker compose -f compose.dev.yml up
```

### Startup Logs

#### Admin User Creation (All Profiles)

```
================================================================================
👤 Creating default admin user...
✅ Default admin user created successfully
🔑 Credentials: username='admin' password='admin'
⚠️  SECURITY WARNING: Change default admin password before production deployment!
================================================================================
```

Or if admin already exists:

```
👤 Admin user already exists - skipping creation
```

#### Test Data Seeding (Dev Profile Only)

When seeding occurs, you'll see:

```
================================================================================
🌱 DEV PROFILE - Starting test data seeding...
================================================================================
✓ Created 10 test users (user1-user10)
✓ Created 423 test events
================================================================================
✅ Test data seeding completed successfully in 234ms
📊 Summary: 10 test users, 423 events
🔑 Test credentials: username='user1' password='password' (and user2-user10)
👤 Admin credentials: username='admin' password='admin' (created by AdminUserSeeder)
================================================================================
```

### Skip Seeding (If Data Exists)

#### Admin User Already Exists

```
👤 Admin user already exists - skipping creation
```

#### Test Data Already Exists (Dev Profile)

If the database already has test data:

```
================================================================================
🌱 DEV PROFILE - Starting test data seeding...
================================================================================
📊 Database already contains test data (Users: 11, Events: 423)
⏭️  Skipping seeding to avoid duplicates
💡 To reseed, drop the database and restart the application
================================================================================
```

## Usage

### Authentication

#### Admin User (All Environments)

Use admin credentials for administrative access:

```bash
# Login as admin
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin"
  }'
```

⚠️ **Production**: Always change the default admin password!

#### Test Users (Dev Environment Only)

1. **Start application in dev mode**:
   ```bash
   ./gradlew bootRun --args='--spring.profiles.active=dev'
   ```

2. **Login with test user**:
   ```bash
   curl -X POST http://localhost:8080/api/v1/auth/login \
     -H "Content-Type: application/json" \
     -d '{"username":"user1","password":"password"}'
   ```

3. **Get events for the user**:
   ```bash
   curl http://localhost:8080/api/v1/events \
     -H "Authorization: Bearer <token>"
   ```

4. **Browse users**:
   ```bash
   curl http://localhost:8080/api/v1/users?page=0&size=10 \
     -H "Authorization: Bearer <token>"
   ```

### Swagger UI Testing

1. Open Swagger UI: `http://localhost:8080/swagger-ui.html`
2. Login with test credentials:
   - Username: `user1` (or user2, user3, etc.)
   - Password: `password`
3. Copy the JWT token
4. Click "Authorize" button and enter: `Bearer <token>`
5. Test any endpoint - data is already populated!

### H2 Console Inspection

1. Access H2 Console: `http://localhost:8080/h2-console`
2. Use credentials from `application-dev.properties`:
   - JDBC URL: `jdbc:h2:mem:testdb`
   - Username: `sa`
   - Password: (empty)
3. Query the data:
   ```sql
   SELECT * FROM users LIMIT 10;
   SELECT * FROM events LIMIT 20;
   ```

## Clearing and Reseeding

### Option 1: Restart Application

In dev profile with H2 (in-memory), simply restart:
```bash
# Stop the application (Ctrl+C)
# Start again
./gradlew bootRun --args='--spring.profiles.active=dev'
```

H2 is in-memory, so stopping the app clears all data.

### Option 2: Drop and Recreate (PostgreSQL)

If using PostgreSQL in dev:
```bash
# Stop application
# Drop and recreate database
docker compose -f compose.dev.yml down -v
docker compose -f compose.dev.yml up -d

# Restart application
./gradlew bootRun --args='--spring.profiles.active=prod'
```

### Option 3: Liquibase Drop-First

In `application-test.properties`, Liquibase is configured with `drop-first=true`:
```properties
spring.liquibase.drop-first=true
```

You can temporarily add this to `application-dev.properties` to force recreation.

## Configuration

### Customizing Seed Data

Edit `DevDataSeeder.java` to customize:

```java
// Number of users (currently 10)
List<UserEntity> users = generateUsers(10, passwordEncoder);

// Events per user (currently 10-60 random range)
List<EventEntity> events = generateEvents(savedUsers, 50);
```

### Adding More Sample Routes

Add routes to the `ROUTES` array:
```java
private static final String[] ROUTES = {
    "/home", "/dashboard", "/profile", 
    "/your-custom-route"  // Add here
};
```

### Adding More Names

Extend the name arrays for more variety:
```java
private static final String[] FIRST_NAMES = {
    "Alice", "Bob", "Charlie",
    "YourName"  // Add here
};
```

## Profile-Specific Behavior

### All Profiles

**Admin User Creation**:
- **Runs on every startup**
- ✅ Creates admin user if not exists
- ✅ Idempotent (safe to run multiple times)
- ✅ Fixed UUID: `00000000-0000-0000-0000-000000000001`
- ✅ Credentials: `admin` / `admin`
- ⚠️ Change password in production!

### Dev Profile ✅

**Admin User**:
- ✅ Created via `AdminUserSeeder`

**Test Data Seeding**:
- ✅ Enabled via `DevDataSeeder`
- ✅ 10 test users (user1-user10)
- ✅ ~400 random events
- ✅ H2 (in-memory database)
- ⚠️ Data lost on restart (fresh data each time)

### Prod Profile ❌

**Admin User**:
- ✅ Created via `AdminUserSeeder`

**Test Data Seeding**:
- ❌ Disabled (`@Profile("dev")` on `DevDataSeeder`)
- ✅ PostgreSQL (persistent database)
- ✅ Clean start
- ✅ Production-ready

### Test Profile 🧪

**Admin User**:
- ✅ Created via `AdminUserSeeder`

**Test Data Seeding**:
- ❌ Disabled (test profile doesn't activate dev profile)
- ✅ H2 (in-memory database)
- ✅ Clean slate for each test run
- ✅ Liquibase `drop-first=true`

## Implementation Details

### Component: AdminUserSeeder

**Location**: `io.github.danny270793.analytics.backend.infrastructure.config.AdminUserSeeder`

**Functionality**:
- Creates default admin user on application startup
- Active in **ALL profiles** (dev, prod, test)
- Idempotent (checks if admin already exists)
- Fixed UUID for consistency across deployments

**Code Structure**:
```java
@Configuration
public class AdminUserSeeder {
    @Bean
    CommandLineRunner seedAdminUser(
        UserJpaRepository userRepository,
        PasswordEncoder passwordEncoder
    ) {
        // Check if admin exists
        // Create if not exists
        // Log credentials
    }
}
```

### Component: DevDataSeeder

**Type**: `@Configuration` with `@Profile("dev")`  
**Trigger**: `CommandLineRunner` (runs after Spring context initialization)  
**Dependencies**: 
- `UserJpaRepository` - For saving users
- `EventJpaRepository` - For saving events
- `PasswordEncoder` - For hashing passwords

### Execution Order

1. Spring Boot starts
2. Application context initializes
3. Liquibase migrations run (create tables)
4. `CommandLineRunner` beans execute:
   - `AdminUserSeeder` creates admin user (all profiles)
   - `DevDataSeeder` creates test data (dev profile only)
5. Application ready to accept requests

### Data Generation Strategy

**Admin User** (All Profiles):
- Fixed UUID: `00000000-0000-0000-0000-000000000001`
- Username: `admin`
- Email: `admin@analytics.local`
- Password: `admin` (BCrypt hashed)

**Test Users** (Dev Profile Only):
- Sequential usernames: `user1`, `user2`, etc.
- Random name combinations from predefined lists
- Consistent password: `password` (BCrypt hashed)
- Realistic email addresses

**Events**:
- Random event types (NAVIGATION or ACTION)
- Random route pairs (from ≠ to)
- Associated with user (userId foreign key)
- Distributed across users (10-60 events each)

## Benefits

### For Development
✅ **No Manual Setup**: Immediate data availability  
✅ **Realistic Testing**: Variety of users and events  
✅ **Quick Iterations**: Fresh data on each restart  
✅ **API Testing**: Ready-to-use credentials  
✅ **UI Development**: Data for frontend integration  

### For Demos
✅ **Instant Demo**: No preparation needed  
✅ **Realistic Scenarios**: Multiple users and interactions  
✅ **Reproducible**: Same data structure each time  

### For Testing
✅ **Manual Testing**: Populated database for exploratory testing  
✅ **Performance Testing**: Realistic data volume  
✅ **Integration Testing**: Test with actual data relationships  

## Example Data

### Sample User
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "username": "user1",
  "email": "alice.smith1@example.com",
  "password": "$2a$10$..." // BCrypt hash of "password"
}
```

### Sample Event
```json
{
  "id": "6ba7b810-9dad-11d1-80b4-00c04fd430c8",
  "type": "NAVIGATION",
  "from": "/home",
  "to": "/dashboard",
  "userId": "550e8400-e29b-41d4-a716-446655440000"
}
```

## Troubleshooting

### Seeding Not Running

**Check Profile**:
```bash
# Verify dev profile is active in logs
grep "The following 1 profile is active: \"dev\"" logs/application.log
```

**Check Configuration**:
```properties
# Ensure dev profile is set
spring.profiles.active=dev
```

### Duplicate Data

If you see duplicate data after multiple starts:
- **H2 In-Memory**: Should not happen (data clears on restart)
- **PostgreSQL**: Seeder skips if data exists
- **Solution**: Drop database and restart

### Seeding Fails

**Database Not Ready**:
- Ensure Liquibase migrations complete before seeding
- Check for migration errors in logs

**Constraint Violations**:
- Unique constraint on username/email
- Foreign key violations
- Check logs for specific SQL errors

### Slow Startup

Seeding adds ~200-500ms to startup time. To disable temporarily:

```bash
# Run without dev profile
./gradlew bootRun

# Or use prod profile
./gradlew bootRun --args='--spring.profiles.active=prod'
```

## Best Practices

### Development
1. ✅ **Always use dev profile** for local development
2. ✅ **Use test credentials** (user1/password) for quick testing
3. ✅ **Restart for fresh data** when needed
4. ✅ **Check H2 console** to verify data structure

### Production
1. ❌ **Never use dev profile** in production
2. ❌ **Don't seed production databases** 
3. ✅ **Use proper user registration** flow
4. ✅ **Implement data backup** and migration strategies

### Testing
1. ✅ **Use test profile** for automated tests
2. ✅ **Let tests create their own data** (don't rely on seeded data)
3. ✅ **Clean state between tests** (test profile uses drop-first)

## Future Enhancements

Potential improvements:

- [ ] Make user/event counts configurable via properties
- [ ] Add more entity types (if added to domain)
- [ ] Support seeding from JSON/YAML files
- [ ] Add realistic timestamps (spread over time)
- [ ] Generate user relationships (followers, friends)
- [ ] Add configurable data volume (small/medium/large)
- [ ] Support incremental seeding (add more data without dropping)

## Related Documentation

- [Environment Profiles](./PROFILES.md) - Profile configuration
- [Database Migrations](./DATABASE_MIGRATIONS.md) - Liquibase setup
- [Testing Documentation](./TESTING.md) - Test data strategies

---

**Document Version**: 1.0.0  
**Feature**: Development Data Seeding  
**Last Updated**: February 15, 2026  
**Active In**: Dev Profile Only
