package io.github.danny270793.analytics.backend.infrastructure.service;

import io.github.danny270793.analytics.backend.application.dto.CreateEventRequest;
import io.github.danny270793.analytics.backend.application.dto.EventResponse;
import io.github.danny270793.analytics.backend.application.dto.UpdateEventRequest;
import io.github.danny270793.analytics.backend.application.service.EventService;
import io.github.danny270793.analytics.backend.domain.model.Event;
import io.github.danny270793.analytics.backend.infrastructure.persistence.entity.EventEntity;
import io.github.danny270793.analytics.backend.infrastructure.persistence.repository.EventJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class EventServiceImpl implements EventService {
    private static final Logger log = LoggerFactory.getLogger(EventServiceImpl.class);

    private final EventJpaRepository eventJpaRepository;

    public EventServiceImpl(EventJpaRepository eventJpaRepository) {
        this.eventJpaRepository = eventJpaRepository;
    }

    @Override
    public EventResponse create(CreateEventRequest request) {
        log.info("Creating new event: type={}, from={}, to={}", request.getType(), request.getFrom(), request.getTo());
        Event event = new Event(null, request.getType(), request.getFrom(), request.getTo());
        EventEntity eventEntity = EventEntity.fromDomain(event);
        EventEntity savedEntity = eventJpaRepository.save(eventEntity);
        log.info("Event created successfully: id={}, type={}", savedEntity.getId(), savedEntity.getType());
        return EventResponse.fromDomain(savedEntity.toDomain());
    }

    @Override
    @Transactional(readOnly = true)
    public EventResponse read(UUID id) {
        log.debug("Fetching event by id: {}", id);
        EventEntity eventEntity = eventJpaRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Event not found with id: {}", id);
                    return new RuntimeException("Event not found with id: " + id);
                });
        log.debug("Event found: type={}", eventEntity.getType());
        return EventResponse.fromDomain(eventEntity.toDomain());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventResponse> read() {
        log.debug("Fetching all events");
        List<EventResponse> events = eventJpaRepository.findAll().stream()
                .map(entity -> EventResponse.fromDomain(entity.toDomain()))
                .collect(Collectors.toList());
        log.debug("Found {} events", events.size());
        return events;
    }

    @Override
    public EventResponse update(UUID id, UpdateEventRequest request) {
        log.info("Updating event: id={}", id);
        EventEntity eventEntity = eventJpaRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Update failed: Event not found with id: {}", id);
                    return new RuntimeException("Event not found with id: " + id);
                });

        if (request.getType() != null) {
            log.debug("Updating event type: {} -> {}", eventEntity.getType(), request.getType());
            eventEntity.setType(request.getType());
        }
        if (request.getFrom() != null) {
            log.debug("Updating event from: {} -> {}", eventEntity.getFrom(), request.getFrom());
            eventEntity.setFrom(request.getFrom());
        }
        if (request.getTo() != null) {
            log.debug("Updating event to: {} -> {}", eventEntity.getTo(), request.getTo());
            eventEntity.setTo(request.getTo());
        }

        EventEntity updatedEntity = eventJpaRepository.save(eventEntity);
        log.info("Event updated successfully: id={}", updatedEntity.getId());
        return EventResponse.fromDomain(updatedEntity.toDomain());
    }

    @Override
    public void delete(UUID id) {
        log.info("Attempting to delete event: id={}", id);
        if (!eventJpaRepository.existsById(id)) {
            log.warn("Delete failed: Event not found with id: {}", id);
            throw new RuntimeException("Event not found with id: " + id);
        }
        eventJpaRepository.deleteById(id);
        log.info("Event deleted successfully: id={}", id);
    }
}
