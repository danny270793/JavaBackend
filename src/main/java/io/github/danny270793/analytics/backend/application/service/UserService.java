package io.github.danny270793.analytics.backend.application.service;

import io.github.danny270793.analytics.backend.application.dto.LoginRequest;
import io.github.danny270793.analytics.backend.application.dto.LoginResponse;
import io.github.danny270793.analytics.backend.application.dto.RegisterUserRequest;
import io.github.danny270793.analytics.backend.application.dto.UserResponse;

import java.util.List;
import java.util.UUID;

public interface UserService {
    UserResponse registerUser(RegisterUserRequest request);
    LoginResponse login(LoginRequest request);
    UserResponse getUserById(UUID id);
    UserResponse getUserByUsername(String username);
    List<UserResponse> getAllUsers();
    void deleteUser(UUID id);
}
