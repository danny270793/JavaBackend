# Request Logging and Performance Monitoring

## Overview

The Analytics Backend automatically logs the processing time for every API request and external API call, providing valuable insights into application performance and helping identify slow endpoints and external dependencies.

## Features

### Internal API Request Logging
- ✅ **Automatic Timing**: Measures request processing time from start to finish
- ✅ **Structured Logging**: Logs HTTP method, URI, status code, and duration
- ✅ **Performance Warnings**: Alerts for slow requests (>1 second)
- ✅ **Error Tracking**: Logs exceptions that occur during request processing
- ✅ **Visual Indicators**: Uses symbols (✓/✗) for quick status identification

### External API Call Logging
- ✅ **Outbound Request Tracking**: Measures time for all external API calls
- ✅ **HTTP Client Monitoring**: Tracks RestTemplate requests to external services
- ✅ **Timeout Configuration**: 5s connect timeout, 10s read timeout
- ✅ **Slow Call Detection**: Warns when external calls take >2 seconds
- ✅ **Failure Tracking**: Logs connection failures and errors

## Implementation

### Components

1. **RequestLoggingInterceptor**: Intercepts all API requests to measure timing
2. **WebMvcConfig**: Registers the interceptor for `/api/**` endpoints
3. **ExternalApiLoggingInterceptor**: Intercepts RestTemplate calls to external APIs
4. **RestTemplateConfig**: Configures RestTemplate with timing interceptor and timeouts

### What Gets Logged

#### Incoming API Request
```
INFO  → Incoming Request: POST /api/v1/auth/login
```

#### Outgoing External API Request
```
INFO  ⟶ Outgoing Request: GET https://jsonplaceholder.typicode.com/posts
```

#### Successful API Response
```
INFO  ← ✓ Response: POST /api/v1/auth/login - Status: 200 - Duration: 45ms
```

#### Successful External API Response
```
INFO  ⟵ ✓ External API Response: GET https://jsonplaceholder.typicode.com/posts - Status: 200 - Duration: 234ms
```

#### Error Response
```
INFO  ← ✗ Response: POST /api/v1/auth/login - Status: 401 - Duration: 23ms
```

#### External API Error
```
ERROR ⟵ ✗ External API Failed: GET https://jsonplaceholder.typicode.com/posts/999 - Error: 404 Not Found - Duration: 156ms
```

#### Slow Request Warning (Internal)
```
WARN  ⚠ Slow Request Detected: GET /api/v1/events took 1234ms
```

#### Slow External API Call Warning
```
WARN  ⚠ Slow External API Call: GET https://jsonplaceholder.typicode.com/posts took 2345ms
```

#### Request Exception
```
ERROR ✗ Request Failed: GET /api/v1/events/{id} - Error: Event not found with id: abc123
```

## Log Format

### Internal API Request Log
```
← [✓/✗] Response: {METHOD} {URI} - Status: {HTTP_STATUS} - Duration: {TIME}ms
```

### External API Call Log
```
⟵ [✓/✗] External API Response: {METHOD} {URL} - Status: {HTTP_STATUS} - Duration: {TIME}ms
```

**Arrow Indicators**:
- **→**: Incoming request to your API
- **⟶**: Outgoing request to external API
- **←**: Response from your API
- **⟵**: Response from external API

**Status Indicators**:
- **✓**: Successful response (status < 400)
- **✗**: Error response (status >= 400)

**Components**:
- **METHOD**: HTTP method (GET, POST, PUT, DELETE)
- **URI/URL**: Request path or full URL
- **HTTP_STATUS**: HTTP status code (200, 201, 400, 401, 403, 404, 500, etc.)
- **TIME**: Duration in milliseconds

## Configuration

### Internal API Endpoints - Included

The interceptor applies to all `/api/**` endpoints:
- `/api/v1/auth/**` - Authentication
- `/api/v1/events/**` - Events
- `/api/v1/users/**` - Users
- `/api/v1/posts/**` - Posts

### Internal API Endpoints - Excluded

The following endpoints are excluded from logging:
- `/actuator/**` - Health and metrics endpoints
- `/swagger-ui/**` - Swagger UI assets
- `/v3/api-docs/**` - OpenAPI documentation

### External API Configuration

The `RestTemplate` is configured with:
- **Connect Timeout**: 5 seconds (time to establish connection)
- **Read Timeout**: 10 seconds (time to receive response)
- **Buffering**: Enabled (allows reading response multiple times)
- **Logging Interceptor**: Automatically measures all outbound calls

All external HTTP calls made through `RestTemplate` are automatically logged.

## Performance Thresholds

### Internal API - Slow Request Alert

Requests taking longer than **1000ms (1 second)** trigger a warning:

```
WARN  ⚠ Slow Request Detected: GET /api/v1/events?page=0&size=100 took 1456ms
```

### External API - Slow Call Alert

External API calls taking longer than **2000ms (2 seconds)** trigger a warning:

```
WARN  ⚠ Slow External API Call: GET https://jsonplaceholder.typicode.com/posts took 2345ms
```

This helps identify:
- Internal performance bottlenecks
- Slow external dependencies
- Network latency issues
- External service degradation

## Example Logs

### Complete Request Flow with External API

```
2026-02-15 10:35:00.100  INFO  → Incoming Request: GET /api/v1/posts/1
2026-02-15 10:35:00.105  INFO  ⟶ Outgoing Request: GET https://jsonplaceholder.typicode.com/posts/1
2026-02-15 10:35:00.289  INFO  ⟵ ✓ External API Response: GET https://jsonplaceholder.typicode.com/posts/1 - Status: 200 - Duration: 184ms
2026-02-15 10:35:00.295  INFO  ← ✓ Response: GET /api/v1/posts/1 - Status: 200 - Duration: 195ms
```

### Successful Authentication Flow

```
2026-02-15 10:30:15.123  INFO  → Incoming Request: POST /api/v1/auth/register
2026-02-15 10:30:15.234  INFO  ← ✓ Response: POST /api/v1/auth/register - Status: 201 - Duration: 111ms

2026-02-15 10:30:20.456  INFO  → Incoming Request: POST /api/v1/auth/login
2026-02-15 10:30:20.501  INFO  ← ✓ Response: POST /api/v1/auth/login - Status: 200 - Duration: 45ms
```

### Event Creation

```
2026-02-15 10:31:00.789  INFO  → Incoming Request: POST /api/v1/events
2026-02-15 10:31:00.812  INFO  ← ✓ Response: POST /api/v1/events - Status: 201 - Duration: 23ms
```

### Error Scenario

```
2026-02-15 10:32:00.100  INFO  → Incoming Request: GET /api/v1/events/invalid-uuid
2026-02-15 10:32:00.115  INFO  ← ✗ Response: GET /api/v1/events/invalid-uuid - Status: 404 - Duration: 15ms
```

### External API Error

```
2026-02-15 10:36:00.200  INFO  → Incoming Request: GET /api/v1/posts/9999
2026-02-15 10:36:00.205  INFO  ⟶ Outgoing Request: GET https://jsonplaceholder.typicode.com/posts/9999
2026-02-15 10:36:00.361  INFO  ⟵ ✗ External API Response: GET https://jsonplaceholder.typicode.com/posts/9999 - Status: 404 - Duration: 156ms
2026-02-15 10:36:00.365  INFO  ← ✗ Response: GET /api/v1/posts/9999 - Status: 404 - Duration: 165ms
```

### Slow Request with Slow External API

```
2026-02-15 10:33:00.200  INFO  → Incoming Request: GET /api/v1/posts
2026-02-15 10:33:00.205  INFO  ⟶ Outgoing Request: GET https://jsonplaceholder.typicode.com/posts
2026-02-15 10:33:02.550  INFO  ⟵ ✓ External API Response: GET https://jsonplaceholder.typicode.com/posts - Status: 200 - Duration: 2345ms
2026-02-15 10:33:02.551  WARN  ⚠ Slow External API Call: GET https://jsonplaceholder.typicode.com/posts took 2345ms
2026-02-15 10:33:02.560  INFO  ← ✓ Response: GET /api/v1/posts - Status: 200 - Duration: 2360ms
2026-02-15 10:33:02.561  WARN  ⚠ Slow Request Detected: GET /api/v1/posts took 2360ms
```

## Use Cases

### Performance Monitoring

Track average response times for each endpoint:

```bash
# Filter logs for a specific endpoint
grep "GET /api/v1/events" logs/application.log | grep "Duration:"

# Example output
← ✓ Response: GET /api/v1/events - Status: 200 - Duration: 23ms
← ✓ Response: GET /api/v1/events - Status: 200 - Duration: 34ms
← ✓ Response: GET /api/v1/events - Status: 200 - Duration: 28ms
```

### Identifying Bottlenecks

Find slow requests:

```bash
# Find all slow requests
grep "Slow Request Detected" logs/application.log

# Example output
⚠ Slow Request Detected: GET /api/v1/events?page=0&size=1000 took 1367ms
⚠ Slow Request Detected: POST /api/v1/events took 1205ms
```

### Error Analysis

Track error rates by endpoint:

```bash
# Find all failed requests
grep "← ✗" logs/application.log

# Example output
← ✗ Response: POST /api/v1/auth/login - Status: 401 - Duration: 23ms
← ✗ Response: GET /api/v1/events/123 - Status: 404 - Duration: 15ms
```

### API Usage Tracking

Monitor which endpoints are being used:

```bash
# Count requests by endpoint
grep "→ Incoming Request" logs/application.log | cut -d' ' -f5- | sort | uniq -c | sort -rn

# Example output
  150 POST /api/v1/events
  120 GET /api/v1/events
   45 POST /api/v1/auth/login
   30 GET /api/v1/users
   12 PUT /api/v1/events/{id}
```

## Integration with Monitoring Tools

### ELK Stack (Elasticsearch, Logstash, Kibana)

The structured log format is designed for easy parsing by log aggregation tools:

**Logstash Pattern**:
```
%{TIMESTAMP_ISO8601:timestamp} %{LOGLEVEL:level} %{DATA:direction} %{WORD:indicator} Response: %{WORD:method} %{DATA:uri} - Status: %{NUMBER:status} - Duration: %{NUMBER:duration}ms
```

### Prometheus Metrics

Consider adding custom metrics based on these logs:
- `http_request_duration_milliseconds` - Histogram of request durations
- `http_requests_total` - Counter of total requests by endpoint and status
- `http_slow_requests_total` - Counter of slow requests

### CloudWatch / DataDog

Export logs to cloud monitoring platforms for:
- Real-time dashboards
- Alert configuration
- Trend analysis
- SLA monitoring

## Troubleshooting

### No Logs Appearing

1. Check log level is set to INFO or lower:
   ```properties
   logging.level.io.github.danny270793.analytics.backend=INFO
   ```

2. Verify interceptor is registered:
   ```bash
   # Should see WebMvcConfig initialization in startup logs
   grep "WebMvcConfig" logs/application.log
   ```

### Too Much Logging

Adjust log level to WARN to see only slow requests and errors:

```properties
# Show only warnings and errors
logging.level.io.github.danny270793.analytics.backend.infrastructure.config.RequestLoggingInterceptor=WARN
```

### Logs Not Showing Duration

Ensure the interceptor's `afterCompletion` method is being called. This can fail if:
- Application crashes during request processing
- Thread is interrupted
- Response is already committed

## Best Practices

### Production Environment

1. **Log Rotation**: Configure log rotation to prevent disk space issues
   ```properties
   logging.file.name=logs/application.log
   logging.file.max-size=10MB
   logging.file.max-history=7
   ```

2. **Structured Logging**: Consider using JSON format for easier parsing
   ```xml
   <!-- logback-spring.xml -->
   <encoder class="net.logstash.logback.encoder.LogstashEncoder"/>
   ```

3. **Sampling**: For high-traffic applications, consider sampling (log 1 in 10 requests)

4. **Sensitive Data**: Ensure no sensitive data (passwords, tokens) appears in URIs

### Development Environment

1. **Verbose Logging**: Set to DEBUG for detailed information
   ```properties
   logging.level.io.github.danny270793.analytics.backend=DEBUG
   ```

2. **Console Output**: Enable colored output for better readability

## Configuration Properties

### Custom Slow Request Threshold

To customize the slow request threshold, modify `RequestLoggingInterceptor`:

```java
private static final long SLOW_REQUEST_THRESHOLD_MS = 1000; // Change this value
```

### Disable Request Logging

To disable request logging temporarily:

```properties
# Set to ERROR to disable INFO and WARN logs
logging.level.io.github.danny270793.analytics.backend.infrastructure.config.RequestLoggingInterceptor=ERROR
```

Or remove the interceptor registration in `WebMvcConfig`.

## Performance Impact

The interceptor has minimal performance impact:

- **Overhead**: ~1-2ms per request
- **Memory**: Negligible (only stores timestamp)
- **CPU**: Minimal (simple arithmetic and string formatting)

For high-traffic applications (>10,000 requests/second), consider:
- Async logging to prevent blocking
- Log sampling to reduce volume
- Buffered logging for better throughput

## Future Enhancements

Potential improvements to consider:

- [ ] Add request/response payload logging (configurable)
- [ ] Track user information from JWT token
- [ ] Add request ID for distributed tracing
- [ ] Export metrics to Prometheus
- [ ] Add response size tracking
- [ ] Implement log sampling for high-traffic endpoints
- [ ] Add custom attributes (user agent, IP address)
- [ ] Create performance dashboard

## Related Documentation

- [Architecture Documentation](./ARCHITECTURE.md) - System architecture overview
- [Monitoring Setup](./MONITORING.md) - Application monitoring guide
- [Logging Configuration](./LOGGING.md) - Detailed logging configuration

---

**Document Version**: 1.0.0  
**Feature**: Request Performance Logging  
**Last Updated**: February 15, 2026  
**Author**: Analytics Backend Team
