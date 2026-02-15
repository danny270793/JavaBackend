package io.github.danny270793.analytics.backend.infrastructure.persistence.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("UserEntity Unit Tests")
class UserEntityTest {

    @Test
    @DisplayName("Should create UserEntity with constructor")
    void shouldCreateUserEntityWithConstructor() {
        // Given
        UUID id = UUID.randomUUID();

        // When
        UserEntity entity = new UserEntity(id, "testuser", "test@example.com", "password123");

        // Then
        assertThat(entity.getId()).isEqualTo(id);
        assertThat(entity.getUsername()).isEqualTo("testuser");
        assertThat(entity.getEmail()).isEqualTo("test@example.com");
        assertThat(entity.getPassword()).isEqualTo("password123");
    }

    @Test
    @DisplayName("Should set and get all properties")
    void shouldSetAndGetAllProperties() {
        // Given
        UserEntity entity = new UserEntity();
        UUID id = UUID.randomUUID();
        UUID createdBy = UUID.randomUUID();
        UUID updatedBy = UUID.randomUUID();
        UUID deletedBy = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime updatedAt = LocalDateTime.now();
        LocalDateTime deletedAt = LocalDateTime.now();

        // When
        entity.setId(id);
        entity.setUsername("newuser");
        entity.setEmail("new@example.com");
        entity.setPassword("newpassword");
        entity.setCreatedAt(createdAt);
        entity.setCreatedBy(createdBy);
        entity.setUpdatedAt(updatedAt);
        entity.setUpdatedBy(updatedBy);
        entity.setDeletedAt(deletedAt);
        entity.setDeletedBy(deletedBy);

        // Then
        assertThat(entity.getId()).isEqualTo(id);
        assertThat(entity.getUsername()).isEqualTo("newuser");
        assertThat(entity.getEmail()).isEqualTo("new@example.com");
        assertThat(entity.getPassword()).isEqualTo("newpassword");
        assertThat(entity.getCreatedAt()).isEqualTo(createdAt);
        assertThat(entity.getCreatedBy()).isEqualTo(createdBy);
        assertThat(entity.getUpdatedAt()).isEqualTo(updatedAt);
        assertThat(entity.getUpdatedBy()).isEqualTo(updatedBy);
        assertThat(entity.getDeletedAt()).isEqualTo(deletedAt);
        assertThat(entity.getDeletedBy()).isEqualTo(deletedBy);
    }
}
