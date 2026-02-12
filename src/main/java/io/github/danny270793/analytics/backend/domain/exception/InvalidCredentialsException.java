package io.github.danny270793.analytics.backend.domain.exception;

/**
 * Exception thrown when user credentials are invalid during authentication.
 * This includes both invalid username and invalid password scenarios.
 */
public class InvalidCredentialsException extends RuntimeException {

    private final String username;

    public InvalidCredentialsException(String username) {
        super("Invalid username or password");
        this.username = username;
    }

    public InvalidCredentialsException(String username, String message) {
        super(message);
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}
