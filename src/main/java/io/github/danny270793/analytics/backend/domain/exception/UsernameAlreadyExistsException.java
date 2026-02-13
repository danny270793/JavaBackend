package io.github.danny270793.analytics.backend.domain.exception;

/**
 * Exception thrown when attempting to register a user with a username
 * that already exists in the system.
 */
public class UsernameAlreadyExistsException extends RuntimeException {

    private final String username;

    public UsernameAlreadyExistsException(String username) {
        super("Username already exists: " + username);
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}
