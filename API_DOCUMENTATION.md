# Event Analytics Backend

Spring Boot application implementing a CRUD API for Event management using Clean Architecture.

## Architecture

The application follows Clean Architecture principles with the following layers:

### 1. Domain Layer (`domain/model`)
- **Event**: Core business entity with UUID, EventType, from, and to fields
- **EventType**: Enum with NAVIGATION and ACTION values

### 2. Application Layer (`application`)
- **EventService**: Service interface defining business operations
- **DTOs**: Data Transfer Objects for requests and responses
  - `CreateEventRequest`: For creating new events
  - `UpdateEventRequest`: For updating existing events
  - `EventResponse`: For returning event data

### 3. Infrastructure Layer (`infrastructure`)
- **EventEntity**: JPA entity for database persistence
- **EventJpaRepository**: Spring Data JPA repository
- **EventServiceImpl**: Implementation of EventService

### 4. Presentation Layer (`presentation`)
- **EventController**: REST API endpoints
- **GlobalExceptionHandler**: Centralized error handling

## API Endpoints

### Create Event
```http
POST /api/events
Content-Type: application/json

{
  "type": "NAVIGATION",
  "from": "/home",
  "to": "/dashboard"
}
```

### Get Event by ID
```http
GET /api/events/{id}
```

### Get All Events
```http
GET /api/events
```

### Update Event
```http
PUT /api/events/{id}
Content-Type: application/json

{
  "type": "ACTION",
  "from": "/dashboard",
  "to": "/profile"
}
```

### Delete Event
```http
DELETE /api/events/{id}
```

## Event Model

| Field | Type | Description |
|-------|------|-------------|
| id | UUID | Unique identifier (auto-generated) |
| type | EventType | NAVIGATION or ACTION |
| from | String | Source location/state |
| to | String | Destination location/state |

## Technology Stack

- **Java 17**
- **Spring Boot 4.0.2**
- **Spring Data JPA**
- **H2 Database** (in-memory)
- **Gradle**

## Running the Application

1. Build the project:
```bash
./gradlew build
```

2. Run the application:
```bash
./gradlew bootRun
```

The application will start on `http://localhost:8080`

## H2 Console

Access the H2 database console at: `http://localhost:8080/h2-console`

- JDBC URL: `jdbc:h2:mem:analyticsdb`
- Username: `sa`
- Password: (leave empty)

## Example Usage

### Create a Navigation Event
```bash
curl -X POST http://localhost:8080/api/events \
  -H "Content-Type: application/json" \
  -d '{
    "type": "NAVIGATION",
    "from": "/home",
    "to": "/about"
  }'
```

### Get All Events
```bash
curl http://localhost:8080/api/events
```

### Update an Event
```bash
curl -X PUT http://localhost:8080/api/events/{id} \
  -H "Content-Type: application/json" \
  -d '{
    "type": "ACTION",
    "from": "/about",
    "to": "/contact"
  }'
```

### Delete an Event
```bash
curl -X DELETE http://localhost:8080/api/events/{id}
```

## Error Handling

The API returns appropriate HTTP status codes:
- `200 OK`: Successful GET/PUT requests
- `201 Created`: Successful POST requests
- `204 No Content`: Successful DELETE requests
- `400 Bad Request`: Validation errors
- `404 Not Found`: Resource not found

Error responses include:
```json
{
  "status": 404,
  "message": "Event not found with id: ...",
  "timestamp": "2026-02-10T..."
}
```
