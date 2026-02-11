package io.github.danny270793.analytics.backend.application.dto;

import io.github.danny270793.analytics.backend.domain.model.EventType;
import jakarta.validation.constraints.NotNull;

public class CreateEventRequest {
    @NotNull(message = "Type is required")
    private EventType type;

    @NotNull(message = "From is required")
    private String from;

    @NotNull(message = "To is required")
    private String to;

    public CreateEventRequest() {
    }

    public CreateEventRequest(EventType type, String from, String to) {
        this.type = type;
        this.from = from;
        this.to = to;
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
