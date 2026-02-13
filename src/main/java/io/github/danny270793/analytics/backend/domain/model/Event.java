package io.github.danny270793.analytics.backend.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Event {
    private UUID id;
    private EventType type;
    private String from;
    private String to;
    private UUID userId;
    private LocalDateTime createdAt;
    private UUID createdBy;
    private LocalDateTime updatedAt;
    private UUID updatedBy;

    public Event() {
    }

    /**
     * Constructor for creating new events (audit fields will be populated by JPA auditing).
     */
    public Event(UUID id, EventType type, String from, String to, UUID userId) {
        this.id = id;
        this.type = type;
        this.from = from;
        this.to = to;
        this.userId = userId;
    }

    /**
     * Full constructor including audit fields (used when mapping from EventEntity).
     */
    public Event(UUID id, EventType type, String from, String to, UUID userId, 
                 LocalDateTime createdAt, UUID createdBy, LocalDateTime updatedAt, UUID updatedBy) {
        this.id = id;
        this.type = type;
        this.from = from;
        this.to = to;
        this.userId = userId;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
        this.updatedAt = updatedAt;
        this.updatedBy = updatedBy;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public UUID getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(UUID createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public UUID getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(UUID updatedBy) {
        this.updatedBy = updatedBy;
    }
}
