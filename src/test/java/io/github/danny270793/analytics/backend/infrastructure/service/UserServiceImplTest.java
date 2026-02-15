package io.github.danny270793.analytics.backend.infrastructure.service;

import io.github.danny270793.analytics.backend.application.dto.request.LoginRequest;
import io.github.danny270793.analytics.backend.application.dto.request.RegisterUserRequest;
import io.github.danny270793.analytics.backend.application.dto.response.LoginResponse;
import io.github.danny270793.analytics.backend.application.dto.response.UserResponse;
import io.github.danny270793.analytics.backend.domain.exception.EmailAlreadyExistsException;
import io.github.danny270793.analytics.backend.domain.exception.InvalidCredentialsException;
import io.github.danny270793.analytics.backend.domain.exception.UserNotFoundException;
import io.github.danny270793.analytics.backend.domain.exception.UsernameAlreadyExistsException;
import io.github.danny270793.analytics.backend.infrastructure.persistence.entity.UserEntity;
import io.github.danny270793.analytics.backend.infrastructure.persistence.repository.UserJpaRepository;
import io.github.danny270793.analytics.backend.infrastructure.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceImplTest {

    @Mock
    private UserJpaRepository userJpaRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UserServiceImpl userService;

    private UserEntity testUser;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testUser = new UserEntity();
        testUser.setId(testUserId);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encoded_password");
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should register new user successfully")
    void shouldRegisterUserSuccessfully() {
        // Given
        RegisterUserRequest request = new RegisterUserRequest("newuser", "new@example.com", "password123");
        when(userJpaRepository.existsByUsername(request.getUsername())).thenReturn(false);
        when(userJpaRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encoded_password");
        when(userJpaRepository.save(any(UserEntity.class))).thenReturn(testUser);

        // When
        UserResponse response = userService.registerUser(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getUsername()).isEqualTo(testUser.getUsername());
        assertThat(response.getEmail()).isEqualTo(testUser.getEmail());
        verify(userJpaRepository).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("Should throw UsernameAlreadyExistsException when username exists")
    void shouldThrowExceptionWhenUsernameExists() {
        // Given
        RegisterUserRequest request = new RegisterUserRequest("existinguser", "new@example.com", "password123");
        when(userJpaRepository.existsByUsername(request.getUsername())).thenReturn(true);

        // When/Then
        assertThatThrownBy(() -> userService.registerUser(request))
                .isInstanceOf(UsernameAlreadyExistsException.class)
                .hasMessageContaining("existinguser");
        verify(userJpaRepository, never()).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("Should throw EmailAlreadyExistsException when email exists")
    void shouldThrowExceptionWhenEmailExists() {
        // Given
        RegisterUserRequest request = new RegisterUserRequest("newuser", "existing@example.com", "password123");
        when(userJpaRepository.existsByUsername(request.getUsername())).thenReturn(false);
        when(userJpaRepository.existsByEmail(request.getEmail())).thenReturn(true);

        // When/Then
        assertThatThrownBy(() -> userService.registerUser(request))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessageContaining("existing@example.com");
        verify(userJpaRepository, never()).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("Should login successfully with valid credentials")
    void shouldLoginSuccessfully() {
        // Given
        LoginRequest request = new LoginRequest("testuser", "password123");
        when(userJpaRepository.findByUsername(request.getUsername())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(request.getPassword(), testUser.getPassword())).thenReturn(true);
        when(jwtUtil.generateToken(any())).thenReturn("jwt_token");

        // When
        LoginResponse response = userService.login(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("jwt_token");
        assertThat(response.getUsername()).isEqualTo(testUser.getUsername());
    }

    @Test
    @DisplayName("Should throw InvalidCredentialsException when password is incorrect")
    void shouldThrowExceptionWhenPasswordIsIncorrect() {
        // Given
        LoginRequest request = new LoginRequest("testuser", "wrongpassword");
        when(userJpaRepository.findByUsername(request.getUsername())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(request.getPassword(), testUser.getPassword())).thenReturn(false);

        // When/Then
        assertThatThrownBy(() -> userService.login(request))
                .isInstanceOf(InvalidCredentialsException.class);
        verify(jwtUtil, never()).generateToken(any());
    }

    @Test
    @DisplayName("Should throw InvalidCredentialsException when user not found")
    void shouldThrowExceptionWhenUserNotFoundDuringLogin() {
        // Given
        LoginRequest request = new LoginRequest("nonexistent", "password123");
        when(userJpaRepository.findByUsername(request.getUsername())).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> userService.login(request))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    @DisplayName("Should get user by ID successfully")
    void shouldGetUserByIdSuccessfully() {
        // Given
        when(userJpaRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

        // When
        UserResponse response = userService.getUserById(testUserId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(testUserId);
        assertThat(response.getUsername()).isEqualTo(testUser.getUsername());
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when user not found by ID")
    void shouldThrowExceptionWhenUserNotFoundById() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(userJpaRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> userService.getUserById(nonExistentId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining(nonExistentId.toString());
    }

    @Test
    @DisplayName("Should get user by username successfully")
    void shouldGetUserByUsernameSuccessfully() {
        // Given
        when(userJpaRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // When
        UserResponse response = userService.getUserByUsername("testuser");

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getUsername()).isEqualTo("testuser");
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when user not found by username")
    void shouldThrowExceptionWhenUserNotFoundByUsername() {
        // Given
        when(userJpaRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> userService.getUserByUsername("nonexistent"))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("nonexistent");
    }

    @Test
    @DisplayName("Should get all users with pagination")
    void shouldGetAllUsersWithPagination() {
        // Given
        List<UserEntity> users = List.of(testUser);
        Page<UserEntity> userPage = new PageImpl<>(users, PageRequest.of(0, 20), 1);
        Pageable pageable = PageRequest.of(0, 20);
        when(userJpaRepository.findAll(pageable)).thenReturn(userPage);

        // When
        Page<UserResponse> response = userService.getAllUsers(pageable);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getUsername()).isEqualTo("testuser");
        assertThat(response.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should delete user successfully (soft delete)")
    void shouldDeleteUserSuccessfully() {
        // Given
        when(userJpaRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userJpaRepository.save(any(UserEntity.class))).thenReturn(testUser);

        // When
        userService.deleteUser(testUserId);

        // Then
        verify(userJpaRepository).findById(testUserId);
        verify(userJpaRepository).save(testUser);
        assertThat(testUser.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when deleting non-existent user")
    void shouldThrowExceptionWhenDeletingNonExistentUser() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(userJpaRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> userService.deleteUser(nonExistentId))
                .isInstanceOf(UserNotFoundException.class);
        verify(userJpaRepository, never()).save(any(UserEntity.class));
    }
}
