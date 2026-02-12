package io.github.danny270793.analytics.backend.infrastructure.service;

import io.github.danny270793.analytics.backend.application.dto.LoginRequest;
import io.github.danny270793.analytics.backend.application.dto.LoginResponse;
import io.github.danny270793.analytics.backend.application.dto.RegisterUserRequest;
import io.github.danny270793.analytics.backend.application.dto.UserResponse;
import io.github.danny270793.analytics.backend.application.service.UserService;
import io.github.danny270793.analytics.backend.domain.model.User;
import io.github.danny270793.analytics.backend.infrastructure.persistence.entity.UserEntity;
import io.github.danny270793.analytics.backend.infrastructure.persistence.repository.UserJpaRepository;
import io.github.danny270793.analytics.backend.infrastructure.security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserServiceImpl implements UserService {
    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserJpaRepository userJpaRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public UserServiceImpl(UserJpaRepository userJpaRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userJpaRepository = userJpaRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public UserResponse registerUser(RegisterUserRequest request) {
        log.info("Attempting to register new user: username={}, email={}", request.getUsername(), request.getEmail());
        
        // Check if username already exists
        if (userJpaRepository.existsByUsername(request.getUsername())) {
            log.warn("Registration failed: Username already exists - {}", request.getUsername());
            throw new RuntimeException("Username already exists");
        }

        // Check if email already exists
        if (userJpaRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration failed: Email already exists - {}", request.getEmail());
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
        
        log.info("User registered successfully: id={}, username={}", savedEntity.getId(), savedEntity.getUsername());

        return UserResponse.fromDomain(savedEntity.toDomain());
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        log.info("Login attempt: username={}", request.getUsername());
        
        UserEntity userEntity = userJpaRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> {
                    log.warn("Login failed: User not found - username={}", request.getUsername());
                    return new RuntimeException("Invalid username or password");
                });

        log.debug("User found in database: id={}, username={}", userEntity.getId(), userEntity.getUsername());
        
        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), userEntity.getPassword())) {
            log.warn("Login failed: Invalid password for user - username={}", request.getUsername());
            throw new RuntimeException("Invalid username or password");
        }

        log.debug("Password verified successfully for user: {}", request.getUsername());
        
        // Generate JWT token
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                userEntity.getUsername(),
                userEntity.getPassword(),
                new ArrayList<>()
        );
        String token = jwtUtil.generateToken(userDetails);
        
        log.info("Login successful: userId={}, username={}", userEntity.getId(), userEntity.getUsername());

        return new LoginResponse(
                userEntity.getId(),
                userEntity.getUsername(),
                userEntity.getEmail(),
                token,
                "Login successful"
        );
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID id) {
        log.debug("Fetching user by id: {}", id);
        UserEntity userEntity = userJpaRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("User not found with id: {}", id);
                    return new RuntimeException("User not found with id: " + id);
                });
        log.debug("User found: username={}", userEntity.getUsername());
        return UserResponse.fromDomain(userEntity.toDomain());
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserByUsername(String username) {
        log.debug("Fetching user by username: {}", username);
        UserEntity userEntity = userJpaRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("User not found with username: {}", username);
                    return new RuntimeException("User not found with username: " + username);
                });
        return UserResponse.fromDomain(userEntity.toDomain());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        log.debug("Fetching all users");
        List<UserResponse> users = userJpaRepository.findAll().stream()
                .map(entity -> UserResponse.fromDomain(entity.toDomain()))
                .collect(Collectors.toList());
        log.debug("Found {} users", users.size());
        return users;
    }

    @Override
    public void deleteUser(UUID id) {
        log.info("Attempting to delete user: id={}", id);
        if (!userJpaRepository.existsById(id)) {
            log.warn("Delete failed: User not found with id: {}", id);
            throw new RuntimeException("User not found with id: " + id);
        }
        userJpaRepository.deleteById(id);
        log.info("User deleted successfully: id={}", id);
    }
}
