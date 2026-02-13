package io.github.danny270793.analytics.backend.application.dto.request;

import io.github.danny270793.analytics.backend.domain.model.EventType;

public class UpdateEventRequest {
    private EventType type;
    private String from;
    private String to;

    public UpdateEventRequest() {
    }

    public UpdateEventRequest(EventType type, String from, String to) {
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
