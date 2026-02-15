package io.github.danny270793.analytics.backend.presentation.controller;

import io.github.danny270793.analytics.backend.application.dto.response.PostResponse;
import io.github.danny270793.analytics.backend.application.service.PostService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PostController Unit Tests")
class PostControllerTest {

    @Mock
    private PostService postService;

    private PostController postController;

    private PostResponse testPostResponse;

    @BeforeEach
    void setUp() {
        // Explicit constructor injection
        postController = new PostController(postService);

        testPostResponse = new PostResponse(1L, 1L, "Test Title", "Test Body");
    }

    @Test
    @DisplayName("GET /api/posts - Should get all posts successfully")
    void shouldGetAllPostsSuccessfully() {
        // Given
        List<PostResponse> posts = List.of(testPostResponse);
        when(postService.getAllPosts()).thenReturn(posts);

        // When
        ResponseEntity<List<PostResponse>> response = postController.getAllPosts();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getId()).isEqualTo(1L);
        assertThat(response.getBody().get(0).getTitle()).isEqualTo("Test Title");
        verify(postService).getAllPosts();
    }

    @Test
    @DisplayName("GET /api/posts/{id} - Should get post by ID successfully")
    void shouldGetPostByIdSuccessfully() {
        // Given
        when(postService.getPostById(1L)).thenReturn(testPostResponse);

        // When
        ResponseEntity<PostResponse> response = postController.getPostById(1L);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(1L);
        assertThat(response.getBody().getTitle()).isEqualTo("Test Title");
        assertThat(response.getBody().getBody()).isEqualTo("Test Body");
        verify(postService).getPostById(1L);
    }
}
