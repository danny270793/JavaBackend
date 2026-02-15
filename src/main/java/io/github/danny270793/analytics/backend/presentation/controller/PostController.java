package io.github.danny270793.analytics.backend.presentation.controller;

import io.github.danny270793.analytics.backend.application.dto.response.PostResponse;
import io.github.danny270793.analytics.backend.application.service.PostService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
public class PostController {
    private static final Logger log = LoggerFactory.getLogger(PostController.class);
    
    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    /**
     * GET /api/posts - Get all posts from external API
     *
     * @return list of all posts
     */
    @GetMapping
    public ResponseEntity<List<PostResponse>> getAllPosts() {
        log.info("GET /api/posts - Fetching all posts from external service");
        List<PostResponse> posts = postService.getAllPosts();
        log.info("Returning {} posts", posts.size());
        return ResponseEntity.ok(posts);
    }

    /**
     * GET /api/posts/{id} - Get a specific post by ID from external API
     *
     * @param id the post ID
     * @return the post with the specified ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getPostById(@PathVariable Long id) {
        log.info("GET /api/posts/{} - Fetching post from external service", id);
        PostResponse post = postService.getPostById(id);
        log.info("Returning post: id={}, title={}", post.getId(), post.getTitle());
        return ResponseEntity.ok(post);
    }
}
