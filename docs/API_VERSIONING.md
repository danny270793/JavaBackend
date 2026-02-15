# API Versioning

## Overview

The Analytics Backend API uses **URI-based versioning** to ensure backwards compatibility and smooth evolution of the API. All endpoints are versioned with a major version number in the URL path.

## Versioning Strategy

### URI-Based Versioning

All API endpoints include a version prefix in their path:

```
/api/v{version}/{resource}
```

**Current Version**: `v1`

### Example Endpoints

```
# Version 1 (Current)
POST   /api/v1/auth/register
POST   /api/v1/auth/login
GET    /api/v1/events
POST   /api/v1/events
GET    /api/v1/users
GET    /api/v1/posts
```

## Why URI-Based Versioning?

We chose URI-based versioning because it:

✅ **Clear and Explicit**: Version is visible in the URL  
✅ **Easy to Test**: Can test different versions in browser/Postman  
✅ **Cache-Friendly**: Different versions have different URLs  
✅ **Simple to Route**: Easy to implement with Spring Boot  
✅ **Client-Friendly**: No special headers required  
✅ **Documentation-Friendly**: Swagger/OpenAPI naturally supports it  

### Alternative Approaches (Not Used)

- **Header-Based**: `Accept: application/vnd.analytics.v1+json` - More complex for clients
- **Query Parameter**: `/api/events?version=1` - Not RESTful
- **Media Type**: `Content-Type: application/vnd.analytics-v1+json` - Harder to test

## Version Lifecycle

### Current Versions

| Version | Status | Released | Deprecated | Sunset |
|---------|--------|----------|------------|--------|
| v1 | **Current** | Feb 2026 | - | - |

### Version Support Policy

- **Current Version**: Fully supported with new features and bug fixes
- **Previous Version**: Supported for 12 months after new version release
- **Deprecated Version**: 6 months notice before sunset
- **Sunset**: Version removed, clients must upgrade

## Migration Between Versions

### When a New Version is Released

1. **Announcement**: New version announced with changelog
2. **Parallel Support**: Both versions available simultaneously
3. **Deprecation Notice**: Old version marked deprecated
4. **Grace Period**: Minimum 6 months for migration
5. **Sunset**: Old version removed after grace period

### Backwards Compatibility

Within the same major version (e.g., v1), we maintain backwards compatibility:

✅ **Safe Changes**:
- Adding new optional fields to requests
- Adding new fields to responses
- Adding new endpoints
- Adding new query parameters (optional)

❌ **Breaking Changes** (Require New Version):
- Removing fields from responses
- Changing field types
- Renaming fields
- Making optional fields required
- Removing endpoints
- Changing authentication mechanism

## Version Detection

### Client Implementation

Clients should:

1. **Always specify version** in the URL
2. **Pin to a specific version** in production
3. **Monitor deprecation notices**
4. **Test new versions** before upgrading
5. **Have a rollback plan**

### Server Implementation

The server:

1. **Routes by URL path** using `@RequestMapping("/api/v1/...")`
2. **Returns version in response headers** (optional)
3. **Logs version usage** for analytics
4. **Monitors deprecated version usage**

## API Changes by Version

### v1 (Current - February 2026)

**Initial Release**

Features:
- User authentication (JWT)
- Event tracking (NAVIGATION, ACTION)
- User management
- External posts integration
- Pagination support
- Audit fields
- Soft delete

Endpoints:
- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`
- `GET /api/v1/users`
- `GET /api/v1/users/{id}`
- `DELETE /api/v1/users/{id}`
- `POST /api/v1/events`
- `GET /api/v1/events`
- `GET /api/v1/events/{id}`
- `PUT /api/v1/events/{id}`
- `DELETE /api/v1/events/{id}`
- `GET /api/v1/posts`
- `GET /api/v1/posts/{id}`

## Deprecation Process

### How We Deprecate

When an endpoint or version is deprecated:

1. **Documentation**: Mark as deprecated in Swagger/OpenAPI
2. **Response Headers**: Add `Sunset` header with date
3. **Logging**: Log usage for monitoring
4. **Communication**: Email/blog post announcement
5. **Migration Guide**: Provide upgrade documentation

### Deprecation Header Example

```http
HTTP/1.1 200 OK
Sunset: Sat, 31 Dec 2027 23:59:59 GMT
Deprecation: true
Link: </docs/migration/v1-to-v2>; rel="deprecation"
```

## Testing Different Versions

### Using cURL

```bash
# Version 1
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin"}'
```

### Using Swagger UI

Access versioned documentation:
- v1: `http://localhost:8080/swagger-ui.html` (default shows current version)

### Using Postman

1. Create separate collections for each version
2. Use environment variables for base URL
3. Set `{{baseUrl}}/api/v1/events` in requests

## Version-Specific Configuration

### application.properties

```properties
# API Configuration
api.current.version=v1
api.deprecated.versions=
api.supported.versions=v1

# Sunset dates (ISO 8601)
api.sunset.v0=2026-12-31T23:59:59Z
```

### Response Headers

All API responses include version information:

```http
HTTP/1.1 200 OK
Content-Type: application/json
X-API-Version: v1
```

## Best Practices for API Evolution

### For API Consumers

1. ✅ **Always use versioned URLs** - Never use unversioned endpoints in production
2. ✅ **Pin to a specific version** - Don't use "latest"
3. ✅ **Handle new fields gracefully** - Ignore unknown fields in responses
4. ✅ **Test against new versions early** - Don't wait until deprecation
5. ✅ **Subscribe to API announcements** - Stay informed about changes

### For API Providers

1. ✅ **Maintain backwards compatibility** within major versions
2. ✅ **Document all changes** in changelog
3. ✅ **Provide migration guides** for breaking changes
4. ✅ **Give adequate deprecation notice** (minimum 6 months)
5. ✅ **Monitor usage of deprecated versions**
6. ✅ **Version your database migrations** separately from API versions

## Common Questions

### Q: Do I need to update my client immediately when v2 is released?

**A**: No. v1 will be supported for at least 12 months after v2 release. You have time to plan and test the migration.

### Q: What if I discover a bug in v1 after v2 is released?

**A**: Critical bugs and security issues are fixed in all supported versions. Feature development focuses on the current version.

### Q: Can I use features from v2 in v1?

**A**: No. Each version is independent. New features are added to new versions.

### Q: What happens if I call an endpoint without a version?

**A**: Legacy support may redirect to v1, but this is not guaranteed. Always specify the version explicitly.

### Q: How do I know which version to use?

**A**: Use the latest version (v1 currently) for new projects. Existing projects should upgrade during the support window.

### Q: Will internal APIs change?

**A**: API versioning only affects public REST endpoints. Internal service interfaces can change independently.

## Monitoring & Analytics

### Metrics We Track

- Version usage by endpoint
- Deprecated version usage
- Migration progress
- Error rates by version

### Dashboard

Access API version metrics at:
```
http://localhost:8080/actuator/metrics/api.version.usage
```

## Future Versions (Planned)

### v2 (Tentative - 2027)

Potential breaking changes under consideration:
- OAuth2 support (breaking authentication)
- GraphQL endpoint
- Event schema changes
- Real-time WebSocket support

**Note**: This is not finalized and subject to change based on user feedback.

## Support

For version-specific issues:
1. Check the [changelog](../CHANGELOG.md)
2. Review [migration guides](./migrations/)
3. Contact support with version information

---

**Document Version**: 1.0.0  
**API Version**: v1  
**Last Updated**: February 15, 2026  
**Next Review**: August 2026
