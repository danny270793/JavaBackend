package io.github.danny270793.analytics.backend.application.dto.response;

import io.github.danny270793.analytics.backend.domain.model.Event;
import io.github.danny270793.analytics.backend.domain.model.EventType;

import java.util.UUID;

public class EventResponse {
    private UUID id;
    private EventType type;
    private String from;
    private String to;
    private UUID userId;

    public EventResponse() {
    }

    public EventResponse(UUID id, EventType type, String from, String to, UUID userId) {
        this.id = id;
        this.type = type;
        this.from = from;
        this.to = to;
        this.userId = userId;
    }

    public static EventResponse fromDomain(Event event) {
        return new EventResponse(
                event.getId(),
                event.getType(),
                event.getFrom(),
                event.getTo(),
                event.getUserId()
        );
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
