package io.github.danny270793.analytics.backend.application.service;

import io.github.danny270793.analytics.backend.application.dto.request.LoginRequest;
import io.github.danny270793.analytics.backend.application.dto.response.LoginResponse;
import io.github.danny270793.analytics.backend.application.dto.request.RegisterUserRequest;
import io.github.danny270793.analytics.backend.application.dto.response.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface UserService {
    UserResponse registerUser(RegisterUserRequest request);
    LoginResponse login(LoginRequest request);
    UserResponse getUserById(UUID id);
    UserResponse getUserByUsername(String username);
    Page<UserResponse> getAllUsers(Pageable pageable);
    void deleteUser(UUID id);
}
