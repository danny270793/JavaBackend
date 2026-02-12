package io.github.danny270793.analytics.backend.infrastructure.persistence.adapter;

import io.github.danny270793.analytics.backend.domain.model.Event;
import io.github.danny270793.analytics.backend.infrastructure.persistence.entity.EventEntity;
import org.springframework.stereotype.Component;

/**
 * Adapter for converting between Event domain model and EventEntity.
 * Follows the Adapter pattern to separate persistence concerns from domain logic.
 */
@Component
public class EventEntityAdapter {

    /**
     * Converts a domain Event to an EventEntity for persistence.
     *
     * @param event the domain model
     * @return the persistence entity
     */
    public EventEntity toEntity(Event event) {
        if (event == null) {
            return null;
        }
        return new EventEntity(
                event.getId(),
                event.getType(),
                event.getFrom(),
                event.getTo()
        );
    }

    /**
     * Converts an EventEntity to a domain Event.
     *
     * @param entity the persistence entity
     * @return the domain model
     */
    public Event toDomain(EventEntity entity) {
        if (entity == null) {
            return null;
        }
        return new Event(
                entity.getId(),
                entity.getType(),
                entity.getFrom(),
                entity.getTo()
        );
    }
}
