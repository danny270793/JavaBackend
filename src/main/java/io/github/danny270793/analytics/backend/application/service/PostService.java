package io.github.danny270793.analytics.backend.application.service;

import io.github.danny270793.analytics.backend.application.dto.response.PostResponse;

import java.util.List;

public interface PostService {
    /**
     * Fetches all posts from the external JSONPlaceholder API.
     *
     * @return list of posts from the external service
     */
    List<PostResponse> getAllPosts();

    /**
     * Fetches a specific post by ID from the external JSONPlaceholder API.
     *
     * @param id the post ID
     * @return the post with the specified ID
     */
    PostResponse getPostById(Long id);
}
