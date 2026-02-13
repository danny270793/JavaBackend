package io.github.danny270793.analytics.backend.infrastructure.service;

import io.github.danny270793.analytics.backend.application.dto.request.CreateEventRequest;
import io.github.danny270793.analytics.backend.application.dto.response.EventResponse;
import io.github.danny270793.analytics.backend.application.dto.request.UpdateEventRequest;
import io.github.danny270793.analytics.backend.application.service.EventService;
import io.github.danny270793.analytics.backend.domain.exception.EventNotFoundException;
import io.github.danny270793.analytics.backend.domain.exception.UnauthorizedAccessException;
import io.github.danny270793.analytics.backend.domain.model.Event;
import io.github.danny270793.analytics.backend.infrastructure.persistence.adapter.EventEntityAdapter;
import io.github.danny270793.analytics.backend.infrastructure.persistence.entity.EventEntity;
import io.github.danny270793.analytics.backend.infrastructure.persistence.entity.UserEntity;
import io.github.danny270793.analytics.backend.infrastructure.persistence.repository.EventJpaRepository;
import io.github.danny270793.analytics.backend.infrastructure.persistence.repository.UserJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final UserJpaRepository userJpaRepository;

    public EventServiceImpl(EventJpaRepository eventJpaRepository, UserJpaRepository userJpaRepository) {
        this.eventJpaRepository = eventJpaRepository;
        this.userJpaRepository = userJpaRepository;
    }

    @Override
    public EventResponse createEvent(CreateEventRequest request) {
        log.info("Creating new event: type={}, from={}, to={}", request.getType(), request.getFrom(), request.getTo());
        
        // Get the logged-in user from Spring Security context
        UUID userId = getCurrentUserId();
        log.debug("Creating event for user: {}", userId);
        
        Event event = new Event(null, request.getType(), request.getFrom(), request.getTo(), userId);
        EventEntity eventEntity = EventEntityAdapter.toEntity(event);
        EventEntity savedEntity = eventJpaRepository.save(eventEntity);
        log.info("Event created successfully: id={}, type={}, userId={}", savedEntity.getId(), savedEntity.getType(), savedEntity.getUserId());
        return EventResponse.fromDomain(EventEntityAdapter.toDomain(savedEntity));
    }

    /**
     * Gets the current logged-in user's ID from the Spring Security context.
     *
     * @return the user ID
     * @throws RuntimeException if user is not authenticated or not found
     */
    private UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            log.error("No authenticated user found in security context");
            throw new RuntimeException("User not authenticated");
        }
        
        String username = authentication.getName();
        log.debug("Fetching user ID for username: {}", username);
        
        UserEntity user = userJpaRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.error("Authenticated user not found in database: {}", username);
                    return new RuntimeException("Authenticated user not found: " + username);
                });
        
        return user.getId();
    }

    @Override
    @Transactional(readOnly = true)
    public EventResponse findEventById(UUID id) {
        log.debug("Fetching event by id: {}", id);
        UUID currentUserId = getCurrentUserId();
        
        EventEntity eventEntity = eventJpaRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Event not found with id: {}", id);
                    return new EventNotFoundException(id);
                });
        
        // Check if the event belongs to the current user
        verifyEventOwnership(eventEntity, currentUserId);
        
        log.debug("Event found: type={}, userId={}", eventEntity.getType(), eventEntity.getUserId());
        return EventResponse.fromDomain(EventEntityAdapter.toDomain(eventEntity));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventResponse> findAllEvents() {
        log.debug("Fetching all events for current user");
        UUID currentUserId = getCurrentUserId();
        
        // Only return events that belong to the current user
        List<EventResponse> events = eventJpaRepository.findAll().stream()
                .filter(entity -> entity.getUserId().equals(currentUserId))
                .map(entity -> EventResponse.fromDomain(EventEntityAdapter.toDomain(entity)))
                .collect(Collectors.toList());
        
        log.debug("Found {} events for user: {}", events.size(), currentUserId);
        return events;
    }

    @Override
    public EventResponse updateEvent(UUID id, UpdateEventRequest request) {
        log.info("Updating event: id={}", id);
        UUID currentUserId = getCurrentUserId();
        
        EventEntity eventEntity = eventJpaRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Update failed: Event not found with id: {}", id);
                    return new EventNotFoundException(id);
                });
        
        // Check if the event belongs to the current user
        verifyEventOwnership(eventEntity, currentUserId);

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
        log.info("Event updated successfully: id={}, userId={}", updatedEntity.getId(), updatedEntity.getUserId());
        return EventResponse.fromDomain(EventEntityAdapter.toDomain(updatedEntity));
    }

    @Override
    public void deleteEvent(UUID id) {
        log.info("Attempting to delete event: id={}", id);
        UUID currentUserId = getCurrentUserId();
        
        EventEntity eventEntity = eventJpaRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Delete failed: Event not found with id: {}", id);
                    return new EventNotFoundException(id);
                });
        
        // Check if the event belongs to the current user
        verifyEventOwnership(eventEntity, currentUserId);
        
        eventJpaRepository.deleteById(id);
        log.info("Event deleted successfully: id={}, userId={}", id, currentUserId);
    }

    /**
     * Verifies that the event belongs to the specified user.
     *
     * @param eventEntity the event to check
     * @param userId the user ID to verify against
     * @throws UnauthorizedAccessException if the event does not belong to the user
     */
    private void verifyEventOwnership(EventEntity eventEntity, UUID userId) {
        if (!eventEntity.getUserId().equals(userId)) {
            log.warn("Unauthorized access attempt: user {} tried to access event {} owned by user {}", 
                    userId, eventEntity.getId(), eventEntity.getUserId());
            throw new UnauthorizedAccessException(eventEntity.getId(), userId);
        }
        log.debug("Event ownership verified: eventId={}, userId={}", eventEntity.getId(), userId);
    }
}
