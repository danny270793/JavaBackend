package io.github.danny270793.analytics.backend.infrastructure.service;

import io.github.danny270793.analytics.backend.application.dto.CreateEventRequest;
import io.github.danny270793.analytics.backend.application.dto.EventResponse;
import io.github.danny270793.analytics.backend.application.dto.UpdateEventRequest;
import io.github.danny270793.analytics.backend.application.service.EventService;
import io.github.danny270793.analytics.backend.domain.model.Event;
import io.github.danny270793.analytics.backend.infrastructure.persistence.entity.EventEntity;
import io.github.danny270793.analytics.backend.infrastructure.persistence.repository.EventJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class EventServiceImpl implements EventService {

    private final EventJpaRepository eventJpaRepository;

    public EventServiceImpl(EventJpaRepository eventJpaRepository) {
        this.eventJpaRepository = eventJpaRepository;
    }

    @Override
    public EventResponse create(CreateEventRequest request) {
        Event event = new Event(null, request.getType(), request.getFrom(), request.getTo());
        EventEntity eventEntity = EventEntity.fromDomain(event);
        EventEntity savedEntity = eventJpaRepository.save(eventEntity);
        return EventResponse.fromDomain(savedEntity.toDomain());
    }

    @Override
    @Transactional(readOnly = true)
    public EventResponse read(UUID id) {
        EventEntity eventEntity = eventJpaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + id));
        return EventResponse.fromDomain(eventEntity.toDomain());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventResponse> read() {
        return eventJpaRepository.findAll().stream()
                .map(entity -> EventResponse.fromDomain(entity.toDomain()))
                .collect(Collectors.toList());
    }

    @Override
    public EventResponse update(UUID id, UpdateEventRequest request) {
        EventEntity eventEntity = eventJpaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + id));

        if (request.getType() != null) {
            eventEntity.setType(request.getType());
        }
        if (request.getFrom() != null) {
            eventEntity.setFrom(request.getFrom());
        }
        if (request.getTo() != null) {
            eventEntity.setTo(request.getTo());
        }

        EventEntity updatedEntity = eventJpaRepository.save(eventEntity);
        return EventResponse.fromDomain(updatedEntity.toDomain());
    }

    @Override
    public void delete(UUID id) {
        if (!eventJpaRepository.existsById(id)) {
            throw new RuntimeException("Event not found with id: " + id);
        }
        eventJpaRepository.deleteById(id);
    }
}
