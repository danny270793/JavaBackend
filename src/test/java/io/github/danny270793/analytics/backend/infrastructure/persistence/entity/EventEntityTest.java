package io.github.danny270793.analytics.backend.infrastructure.persistence.entity;

import io.github.danny270793.analytics.backend.domain.model.EventType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("EventEntity Unit Tests")
class EventEntityTest {

    @Test
    @DisplayName("Should create EventEntity with constructor")
    void shouldCreateEventEntityWithConstructor() {
        // Given
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        // When
        EventEntity entity = new EventEntity(id, EventType.NAVIGATION, "/home", "/profile", userId);

        // Then
        assertThat(entity.getId()).isEqualTo(id);
        assertThat(entity.getType()).isEqualTo(EventType.NAVIGATION);
        assertThat(entity.getFrom()).isEqualTo("/home");
        assertThat(entity.getTo()).isEqualTo("/profile");
        assertThat(entity.getUserId()).isEqualTo(userId);
    }

    @Test
    @DisplayName("Should set and get all properties")
    void shouldSetAndGetAllProperties() {
        // Given
        EventEntity entity = new EventEntity();
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID createdBy = UUID.randomUUID();
        UUID updatedBy = UUID.randomUUID();
        UUID deletedBy = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime updatedAt = LocalDateTime.now();
        LocalDateTime deletedAt = LocalDateTime.now();

        // When
        entity.setId(id);
        entity.setType(EventType.ACTION);
        entity.setFrom("/dashboard");
        entity.setTo("/settings");
        entity.setUserId(userId);
        entity.setCreatedAt(createdAt);
        entity.setCreatedBy(createdBy);
        entity.setUpdatedAt(updatedAt);
        entity.setUpdatedBy(updatedBy);
        entity.setDeletedAt(deletedAt);
        entity.setDeletedBy(deletedBy);

        // Then
        assertThat(entity.getId()).isEqualTo(id);
        assertThat(entity.getType()).isEqualTo(EventType.ACTION);
        assertThat(entity.getFrom()).isEqualTo("/dashboard");
        assertThat(entity.getTo()).isEqualTo("/settings");
        assertThat(entity.getUserId()).isEqualTo(userId);
        assertThat(entity.getCreatedAt()).isEqualTo(createdAt);
        assertThat(entity.getCreatedBy()).isEqualTo(createdBy);
        assertThat(entity.getUpdatedAt()).isEqualTo(updatedAt);
        assertThat(entity.getUpdatedBy()).isEqualTo(updatedBy);
        assertThat(entity.getDeletedAt()).isEqualTo(deletedAt);
        assertThat(entity.getDeletedBy()).isEqualTo(deletedBy);
    }
}
