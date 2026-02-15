package io.github.danny270793.analytics.backend.infrastructure.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor to log request processing time.
 * Logs HTTP method, URI, status code, and duration in milliseconds.
 */
@Component
public class RequestLoggingInterceptor implements HandlerInterceptor {
    private static final Logger log = LoggerFactory.getLogger(RequestLoggingInterceptor.class);
    private static final String START_TIME_ATTRIBUTE = "startTime";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        long startTime = System.currentTimeMillis();
        request.setAttribute(START_TIME_ATTRIBUTE, startTime);
        
        log.info("→ Incoming Request: {} {}", request.getMethod(), request.getRequestURI());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        Long startTime = (Long) request.getAttribute(START_TIME_ATTRIBUTE);
        
        if (startTime != null) {
            long duration = System.currentTimeMillis() - startTime;
            String statusIndicator = response.getStatus() >= 400 ? "✗" : "✓";
            
            log.info("← {} Response: {} {} - Status: {} - Duration: {}ms",
                    statusIndicator,
                    request.getMethod(),
                    request.getRequestURI(),
                    response.getStatus(),
                    duration);
            
            // Log warning for slow requests (over 1 second)
            if (duration > 1000) {
                log.warn("⚠ Slow Request Detected: {} {} took {}ms", 
                        request.getMethod(), 
                        request.getRequestURI(), 
                        duration);
            }
        }
        
        // Log exception if present
        if (ex != null) {
            log.error("✗ Request Failed: {} {} - Error: {}", 
                    request.getMethod(), 
                    request.getRequestURI(), 
                    ex.getMessage());
        }
    }
}
