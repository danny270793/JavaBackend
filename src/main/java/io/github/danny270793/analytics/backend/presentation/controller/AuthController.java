package io.github.danny270793.analytics.backend.presentation.controller;

import io.github.danny270793.analytics.backend.application.dto.request.LoginRequest;
import io.github.danny270793.analytics.backend.application.dto.response.LoginResponse;
import io.github.danny270793.analytics.backend.application.dto.request.RegisterUserRequest;
import io.github.danny270793.analytics.backend.application.dto.response.UserResponse;
import io.github.danny270793.analytics.backend.application.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterUserRequest request) {
        log.info("POST /api/auth/register - Received registration request for username: {}", request.getUsername());
        UserResponse response = userService.registerUser(request);
        log.info("POST /api/auth/register - Registration successful for user: {}", response.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("POST /api/auth/login - Received login request for username: {}", request.getUsername());
        LoginResponse response = userService.login(request);
        log.info("POST /api/auth/login - Login successful for user: {}", response.getUsername());
        return ResponseEntity.ok(response);
    }
}
