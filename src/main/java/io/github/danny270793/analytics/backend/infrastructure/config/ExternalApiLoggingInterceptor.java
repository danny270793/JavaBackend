package io.github.danny270793.analytics.backend.infrastructure.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

/**
 * Interceptor for RestTemplate to measure and log external API call duration.
 * Tracks performance of outbound HTTP requests to external services.
 */
public class ExternalApiLoggingInterceptor implements ClientHttpRequestInterceptor {
    private static final Logger log = LoggerFactory.getLogger(ExternalApiLoggingInterceptor.class);

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        long startTime = System.currentTimeMillis();
        String method = request.getMethod().name();
        String uri = request.getURI().toString();
        
        log.info("⟶ Outgoing Request: {} {}", method, uri);
        
        ClientHttpResponse response = null;
        boolean hasError = false;
        String errorMessage = null;
        
        try {
            response = execution.execute(request, body);
            return response;
        } catch (IOException e) {
            hasError = true;
            errorMessage = e.getMessage();
            throw e;
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            
            if (hasError) {
                log.error("⟵ ✗ External API Failed: {} {} - Error: {} - Duration: {}ms",
                        method, uri, errorMessage, duration);
            } else if (response != null) {
                int statusCode = response.getStatusCode().value();
                String statusIndicator = statusCode >= 400 ? "✗" : "✓";
                
                log.info("⟵ {} External API Response: {} {} - Status: {} - Duration: {}ms",
                        statusIndicator, method, uri, statusCode, duration);
                
                // Warn for slow external API calls (over 2 seconds)
                if (duration > 2000) {
                    log.warn("⚠ Slow External API Call: {} {} took {}ms", method, uri, duration);
                }
            }
        }
    }
}
