package io.github.danny270793.analytics.backend.application.dto;

import java.util.UUID;

public class LoginResponse {
    private UUID userId;
    private String username;
    private String email;
    private String token;
    private String message;

    public LoginResponse() {
    }

    public LoginResponse(UUID userId, String username, String email, String token, String message) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.token = token;
        this.message = message;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
