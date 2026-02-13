package io.github.danny270793.analytics.backend.infrastructure.persistence.entity;

import io.github.danny270793.analytics.backend.domain.model.EventType;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "events")
public class EventEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventType type;

    @Column(nullable = false, name = "from_value")
    private String from;

    @Column(nullable = false, name = "to_value")
    private String to;

    @Column(nullable = false, name = "user_id")
    private UUID userId;

    public EventEntity() {
    }

    public EventEntity(UUID id, EventType type, String from, String to, UUID userId) {
        this.id = id;
        this.type = type;
        this.from = from;
        this.to = to;
        this.userId = userId;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public EventType getType() {
        return type;
    }

    public void setType(EventType type) {
        this.type = type;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }
}
