package io.github.danny270793.analytics.backend.infrastructure.service;

import io.github.danny270793.analytics.backend.application.dto.response.PostResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PostService Unit Tests")
class PostServiceImplTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private PostServiceImpl postService;

    private PostResponse[] mockPosts;
    private PostResponse mockPost;

    @BeforeEach
    void setUp() {
        mockPost = new PostResponse(1L, 1L, "Test Title", "Test Body");
        mockPosts = new PostResponse[]{
                mockPost,
                new PostResponse(2L, 1L, "Test Title 2", "Test Body 2")
        };
    }

    @Test
    @DisplayName("Should get all posts successfully")
    void shouldGetAllPostsSuccessfully() {
        // Given
        ResponseEntity<List<PostResponse>> responseEntity = ResponseEntity.ok(List.of(mockPost, mockPosts[1]));
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)
        )).thenReturn(responseEntity);

        // When
        List<PostResponse> response = postService.getAllPosts();

        // Then
        assertThat(response).isNotNull();
        assertThat(response).hasSize(2);
        assertThat(response.get(0).getTitle()).isEqualTo("Test Title");
        verify(restTemplate).exchange(anyString(), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class));
    }

    @Test
    @DisplayName("Should return empty list when external API returns null")
    void shouldReturnEmptyListWhenApiReturnsNull() {
        // Given
        ResponseEntity<List<PostResponse>> responseEntity = ResponseEntity.ok(null);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)))
                .thenReturn(responseEntity);

        // When
        List<PostResponse> response = postService.getAllPosts();

        // Then
        assertThat(response).isNull();
    }

    @Test
    @DisplayName("Should handle RestClientException when getting all posts")
    void shouldHandleExceptionWhenGettingAllPosts() {
        // Given
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)))
                .thenThrow(new RuntimeException("Connection error"));

        // When/Then
        assertThatThrownBy(() -> postService.getAllPosts())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to fetch posts from external service");
    }

    @Test
    @DisplayName("Should get post by ID successfully")
    void shouldGetPostByIdSuccessfully() {
        // Given
        Long postId = 1L;
        when(restTemplate.getForObject(
                eq("https://jsonplaceholder.typicode.com/posts/" + postId),
                eq(PostResponse.class)
        )).thenReturn(mockPost);

        // When
        PostResponse response = postService.getPostById(postId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTitle()).isEqualTo("Test Title");
        verify(restTemplate).getForObject(anyString(), eq(PostResponse.class));
    }

    @Test
    @DisplayName("Should return null when post not found")
    void shouldReturnNullWhenPostNotFound() {
        // Given
        Long postId = 999L;
        when(restTemplate.getForObject(anyString(), eq(PostResponse.class))).thenReturn(null);

        // When
        PostResponse response = postService.getPostById(postId);

        // Then
        assertThat(response).isNull();
    }

    @Test
    @DisplayName("Should handle RestClientException when getting post by ID")
    void shouldHandleExceptionWhenGettingPostById() {
        // Given
        Long postId = 1L;
        when(restTemplate.getForObject(anyString(), eq(PostResponse.class)))
                .thenThrow(new RuntimeException("Connection error"));

        // When/Then
        assertThatThrownBy(() -> postService.getPostById(postId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to fetch post from external service");
    }
}
