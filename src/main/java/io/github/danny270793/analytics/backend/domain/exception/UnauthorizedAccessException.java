package io.github.danny270793.analytics.backend.domain.exception;

import java.util.UUID;

/**
 * Exception thrown when a user attempts to access or modify a resource
 * that belongs to another user.
 */
public class UnauthorizedAccessException extends RuntimeException {

    private final UUID resourceId;
    private final UUID userId;

    public UnauthorizedAccessException(UUID resourceId, UUID userId) {
        super(String.format("User %s is not authorized to access resource %s", userId, resourceId));
        this.resourceId = resourceId;
        this.userId = userId;
    }

    public UnauthorizedAccessException(String message, UUID resourceId, UUID userId) {
        super(message);
        this.resourceId = resourceId;
        this.userId = userId;
    }

    public UUID getResourceId() {
        return resourceId;
    }

    public UUID getUserId() {
        return userId;
    }
}
