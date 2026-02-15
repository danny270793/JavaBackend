package io.github.danny270793.analytics.backend.presentation.controller;

import io.github.danny270793.analytics.backend.application.dto.response.PostResponse;
import io.github.danny270793.analytics.backend.application.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@Tag(name = "Posts", description = "External posts integration - fetches data from JSONPlaceholder API - requires authentication")
@SecurityRequirement(name = "Bearer Authentication")
public class PostController {
    private static final Logger log = LoggerFactory.getLogger(PostController.class);
    
    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping
    @Operation(summary = "Get all posts", description = "Fetches all posts from JSONPlaceholder external API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Posts retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PostResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content),
            @ApiResponse(responseCode = "503", description = "External service unavailable",
                    content = @Content)
    })
    public ResponseEntity<List<PostResponse>> getAllPosts() {
        log.info("GET /api/posts - Fetching all posts from external service");
        List<PostResponse> posts = postService.getAllPosts();
        log.info("Returning {} posts", posts.size());
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get post by ID", description = "Fetches a specific post by ID from JSONPlaceholder external API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Post retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PostResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Post not found in external service",
                    content = @Content),
            @ApiResponse(responseCode = "503", description = "External service unavailable",
                    content = @Content)
    })
    public ResponseEntity<PostResponse> getPostById(
            @Parameter(description = "Post ID", required = true, example = "1") @PathVariable Long id) {
        log.info("GET /api/posts/{} - Fetching post from external service", id);
        PostResponse post = postService.getPostById(id);
        log.info("Returning post: id={}, title={}", post.getId(), post.getTitle());
        return ResponseEntity.ok(post);
    }
}
