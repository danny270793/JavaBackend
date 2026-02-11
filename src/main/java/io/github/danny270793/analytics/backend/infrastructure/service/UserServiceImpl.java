package io.github.danny270793.analytics.backend.infrastructure.service;

import io.github.danny270793.analytics.backend.application.dto.LoginRequest;
import io.github.danny270793.analytics.backend.application.dto.LoginResponse;
import io.github.danny270793.analytics.backend.application.dto.RegisterUserRequest;
import io.github.danny270793.analytics.backend.application.dto.UserResponse;
import io.github.danny270793.analytics.backend.application.service.UserService;
import io.github.danny270793.analytics.backend.domain.model.User;
import io.github.danny270793.analytics.backend.infrastructure.persistence.entity.UserEntity;
import io.github.danny270793.analytics.backend.infrastructure.persistence.repository.UserJpaRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserJpaRepository userJpaRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserJpaRepository userJpaRepository, PasswordEncoder passwordEncoder) {
        this.userJpaRepository = userJpaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserResponse registerUser(RegisterUserRequest request) {
        // Check if username already exists
        if (userJpaRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        // Check if email already exists
        if (userJpaRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // Create user with encoded password
        User user = new User(
                null,
                request.getUsername(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                null,
                null
        );

        UserEntity userEntity = UserEntity.fromDomain(user);
        UserEntity savedEntity = userJpaRepository.save(userEntity);

        return UserResponse.fromDomain(savedEntity.toDomain());
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        UserEntity userEntity = userJpaRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), userEntity.getPassword())) {
            throw new RuntimeException("Invalid username or password");
        }

        return new LoginResponse(
                userEntity.getId(),
                userEntity.getUsername(),
                userEntity.getEmail(),
                "Login successful"
        );
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID id) {
        UserEntity userEntity = userJpaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        return UserResponse.fromDomain(userEntity.toDomain());
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserByUsername(String username) {
        UserEntity userEntity = userJpaRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
        return UserResponse.fromDomain(userEntity.toDomain());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userJpaRepository.findAll().stream()
                .map(entity -> UserResponse.fromDomain(entity.toDomain()))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteUser(UUID id) {
        if (!userJpaRepository.existsById(id)) {
            throw new RuntimeException("User not found with id: " + id);
        }
        userJpaRepository.deleteById(id);
    }
}
