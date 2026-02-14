package io.github.danny270793.analytics.backend.infrastructure.service;

import io.github.danny270793.analytics.backend.application.dto.response.PostResponse;
import io.github.danny270793.analytics.backend.application.service.PostService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class PostServiceImpl implements PostService {
    private static final Logger log = LoggerFactory.getLogger(PostServiceImpl.class);
    private static final String BASE_URL = "https://jsonplaceholder.typicode.com";
    
    private final RestTemplate restTemplate;

    public PostServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public List<PostResponse> getAllPosts() {
        log.info("Fetching all posts from external API");
        String url = BASE_URL + "/posts";
        
        try {
            ResponseEntity<List<PostResponse>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<PostResponse>>() {}
            );
            
            List<PostResponse> posts = response.getBody();
            log.info("Successfully fetched {} posts from external API", posts != null ? posts.size() : 0);
            
            return posts;
        } catch (Exception e) {
            log.error("Error fetching posts from external API: {}", e.getMessage());
            throw new RuntimeException("Failed to fetch posts from external service", e);
        }
    }

    @Override
    public PostResponse getPostById(Long id) {
        log.info("Fetching post with id={} from external API", id);
        String url = BASE_URL + "/posts/" + id;
        
        try {
            PostResponse post = restTemplate.getForObject(url, PostResponse.class);
            log.info("Successfully fetched post: id={}, title={}", post.getId(), post.getTitle());
            
            return post;
        } catch (Exception e) {
            log.error("Error fetching post with id={}: {}", id, e.getMessage());
            throw new RuntimeException("Failed to fetch post from external service", e);
        }
    }
}
