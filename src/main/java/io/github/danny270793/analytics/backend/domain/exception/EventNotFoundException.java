package io.github.danny270793.analytics.backend.domain.exception;

import java.util.UUID;

/**
 * Exception thrown when an event is not found in the system.
 * This is a runtime exception indicating a resource that should exist is missing.
 */
public class EventNotFoundException extends RuntimeException {

    private final UUID eventId;

    public EventNotFoundException(UUID eventId) {
        super("Event not found with id: " + eventId);
        this.eventId = eventId;
    }

    public EventNotFoundException(UUID eventId, String message) {
        super(message);
        this.eventId = eventId;
    }

    public UUID getEventId() {
        return eventId;
    }
}
