package io.github.danny270793.analytics.backend.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration for REST client beans with performance monitoring.
 * Configures RestTemplate with logging interceptor to track external API calls.
 */
@Configuration
public class RestClientConfig {

    /**
     * Creates a RestTemplate bean for making HTTP requests to external services.
     * Includes performance monitoring interceptor and timeout configuration.
     *
     * @return configured RestTemplate instance with timing interceptor and timeout settings
     */
    @Bean
    public RestTemplate restTemplate() {
        // Create factory with timeouts
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);  // 5 seconds connection timeout
        factory.setReadTimeout(10000);    // 10 seconds read timeout
        
        // Use buffering factory to allow reading response body multiple times
        BufferingClientHttpRequestFactory bufferingFactory = new BufferingClientHttpRequestFactory(factory);
        
        // Create RestTemplate
        RestTemplate restTemplate = new RestTemplate(bufferingFactory);
        
        // Add logging interceptor for performance monitoring
        List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
        interceptors.add(new ExternalApiLoggingInterceptor());
        restTemplate.setInterceptors(interceptors);
        
        return restTemplate;
    }
}

