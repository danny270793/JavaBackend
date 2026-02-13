package io.github.danny270793.analytics.backend.infrastructure.persistence.adapter;

import io.github.danny270793.analytics.backend.domain.model.Event;
import io.github.danny270793.analytics.backend.infrastructure.persistence.entity.EventEntity;

/**
 * Static utility adapter for converting between Event domain model and EventEntity.
 * Follows the Adapter pattern to separate persistence concerns from domain logic.
 * 
 * This class is designed as a stateless utility and should not be instantiated.
 */
public final class EventEntityAdapter {

    /**
     * Private constructor to prevent instantiation.
     */
    private EventEntityAdapter() {
        throw new UnsupportedOperationException("Utility class - cannot be instantiated");
    }

    /**
     * Converts a domain Event to an EventEntity for persistence.
     *
     * @param event the domain model
     * @return the persistence entity, or null if input is null
     */
    public static EventEntity toEntity(Event event) {
        if (event == null) {
            return null;
        }
        return new EventEntity(
                event.getId(),
                event.getType(),
                event.getFrom(),
                event.getTo(),
                event.getUserId()
        );
    }

    /**
     * Converts an EventEntity to a domain Event.
     *
     * @param entity the persistence entity
     * @return the domain model, or null if input is null
     */
    public static Event toDomain(EventEntity entity) {
        if (entity == null) {
            return null;
        }
        return new Event(
                entity.getId(),
                entity.getType(),
                entity.getFrom(),
                entity.getTo(),
                entity.getUserId()
        );
    }
}
