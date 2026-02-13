package io.github.danny270793.analytics.backend.domain.model;

import java.util.UUID;

public class Event {
    private UUID id;
    private EventType type;
    private String from;
    private String to;

    public Event() {
    }

    public Event(UUID id, EventType type, String from, String to) {
        this.id = id;
        this.type = type;
        this.from = from;
        this.to = to;
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
}
