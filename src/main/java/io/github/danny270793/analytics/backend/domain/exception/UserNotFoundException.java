package io.github.danny270793.analytics.backend.domain.exception;

import java.util.UUID;

/**
 * Exception thrown when a user is not found in the system.
 * This is a runtime exception indicating a resource that should exist is missing.
 */
public class UserNotFoundException extends RuntimeException {

    private final Object identifier;

    public UserNotFoundException(UUID userId) {
        super("User not found with id: " + userId);
        this.identifier = userId;
    }

    public UserNotFoundException(String username) {
        super("User not found with username: " + username);
        this.identifier = username;
    }

    public UserNotFoundException(String message, Object identifier) {
        super(message);
        this.identifier = identifier;
    }

    public Object getIdentifier() {
        return identifier;
    }
}
