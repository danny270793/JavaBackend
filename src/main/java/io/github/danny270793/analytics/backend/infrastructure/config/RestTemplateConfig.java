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
 * Configuration for RestTemplate with performance monitoring.
 * Configures HTTP client for external API calls with logging interceptor.
 */
@Configuration
public class RestTemplateConfig {

    /**
     * Creates a RestTemplate bean with performance monitoring interceptor.
     * 
     * @return configured RestTemplate with timing interceptor
     */
    @Bean
    public RestTemplate restTemplate() {
        // Create factory with timeouts
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);  // 5 seconds connection timeout
        factory.setReadTimeout(10000);    // 10 seconds read timeout
        
        // Use buffering factory to allow reading response body multiple times (useful for logging)
        BufferingClientHttpRequestFactory bufferingFactory = new BufferingClientHttpRequestFactory(factory);
        
        // Create RestTemplate
        RestTemplate restTemplate = new RestTemplate(bufferingFactory);
        
        // Add logging interceptor
        List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
        interceptors.add(new ExternalApiLoggingInterceptor());
        restTemplate.setInterceptors(interceptors);
        
        return restTemplate;
    }
}
