package io.github.danny270793.analytics.backend.domain.exception;

/**
 * Exception thrown when attempting to register a user with an email
 * that already exists in the system.
 */
public class EmailAlreadyExistsException extends RuntimeException {

    private final String email;

    public EmailAlreadyExistsException(String email) {
        super("Email already exists: " + email);
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
