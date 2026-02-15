# Swagger/OpenAPI Documentation

This project uses **springdoc-openapi** for API documentation.

## Accessing Swagger UI

Once the application is running, you can access the Swagger UI at:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI Spec (JSON)**: http://localhost:8080/v3/api-docs
- **OpenAPI Spec (YAML)**: http://localhost:8080/v3/api-docs.yaml

## Using Swagger with JWT Authentication

Most endpoints in this API require JWT authentication. To test authenticated endpoints in Swagger UI:

1. **Register a new user** (if you don't have one):
   - Navigate to `Authentication` section
   - Use `POST /api/v1/auth/register`
   - Provide username, email, and password

2. **Login to get JWT token**:
   - Use `POST /api/v1/auth/login`
   - Enter your credentials
   - Copy the `token` from the response

3. **Authorize Swagger UI**:
   - Click the **"Authorize"** button (🔓) at the top right of Swagger UI
   - In the popup, enter: `Bearer YOUR_JWT_TOKEN` (replace `YOUR_JWT_TOKEN` with the actual token)
   - Click **"Authorize"** then **"Close"**

4. **Test authenticated endpoints**:
   - All subsequent requests will include the JWT token
   - You can now test endpoints under `Events`, `Users`, and `Posts` sections

## API Sections

### Authentication
- **POST /api/v1/auth/register**: Register a new user
- **POST /api/v1/auth/login**: Login and get JWT token

### Events
All event endpoints require authentication. Users can only access their own events.
- **POST /api/events**: Create a new event
- **GET /api/events**: Get all events (paginated) for the authenticated user
- **GET /api/events/{id}**: Get event by ID
- **PUT /api/events/{id}**: Update an event
- **DELETE /api/events/{id}**: Soft delete an event

### Users
All user endpoints require authentication.
- **GET /api/users**: Get all users (paginated)
- **GET /api/users/{id}**: Get user by ID
- **GET /api/users/username/{username}**: Get user by username
- **DELETE /api/users/{id}**: Soft delete a user

### Posts
All post endpoints require authentication. Posts are fetched from an external API (JSONPlaceholder).
- **GET /api/posts**: Get all posts from external service
- **GET /api/posts/{id}**: Get a specific post by ID from external service

## Configuration

The OpenAPI configuration is located in:
```
src/main/java/io/github/danny270793/analytics/backend/infrastructure/config/OpenApiConfig.java
```

### Customization

You can customize the OpenAPI configuration by modifying `OpenApiConfig.java`:

- **API Title & Description**: Change the info section
- **Contact Information**: Update contact details
- **License**: Modify license information
- **Servers**: Add or modify server URLs for different environments
- **Security Schemes**: Customize authentication methods

## Security Configuration

Swagger endpoints are publicly accessible (no authentication required) as configured in `SecurityConfig.java`:

```java
.requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
```

If you want to restrict Swagger UI access in production, you can:

1. Use Spring profiles to conditionally enable/disable Swagger
2. Add authentication to Swagger endpoints
3. Completely exclude springdoc-openapi dependency in production builds

## Environment-Specific Configuration

### Development
Swagger is fully enabled with access at http://localhost:8080/swagger-ui.html

### Production
For production environments, consider:

1. **Disabling Swagger** by adding to `application-prod.properties`:
   ```properties
   springdoc.swagger-ui.enabled=false
   springdoc.api-docs.enabled=false
   ```

2. **Or restricting access** by updating `SecurityConfig.java` to require authentication for Swagger endpoints in production.

## Dependencies

The Swagger/OpenAPI integration uses:

```gradle
implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0'
```

This dependency includes:
- Swagger UI
- OpenAPI 3.0 specification generation
- Integration with Spring Boot 3.x
- Automatic endpoint discovery

## Features

- ✅ Auto-generates API documentation from Spring annotations
- ✅ Interactive API testing via Swagger UI
- ✅ JWT authentication support
- ✅ Request/response schema validation
- ✅ Example values for all DTOs
- ✅ Detailed endpoint descriptions
- ✅ Error response documentation
- ✅ Pagination parameter documentation

## Tips

1. **Always authorize first**: Don't forget to use the Authorize button before testing authenticated endpoints
2. **Token format**: The token must include "Bearer " prefix (e.g., `Bearer eyJhbGci...`)
3. **Token expiration**: If you get 401 errors, your token may have expired - login again to get a new one
4. **Environment URLs**: Update server URLs in `OpenApiConfig.java` to match your actual deployment URLs
5. **Try it out**: Use the "Try it out" button on each endpoint to test directly from the UI

## Troubleshooting

### Swagger UI not loading
- Ensure the application is running
- Check that `springdoc-openapi-starter-webmvc-ui` dependency is in `build.gradle`
- Verify Security configuration allows access to `/swagger-ui/**`

### 401 Unauthorized on authenticated endpoints
- Click the Authorize button and enter your JWT token
- Ensure the token includes "Bearer " prefix
- Check that the token hasn't expired

### Changes not reflected in Swagger UI
- Restart the application
- Clear browser cache
- Check that OpenAPI annotations are correct on controllers

## Further Reading

- [springdoc-openapi Documentation](https://springdoc.org/)
- [OpenAPI Specification](https://swagger.io/specification/)
- [Swagger UI](https://swagger.io/tools/swagger-ui/)
