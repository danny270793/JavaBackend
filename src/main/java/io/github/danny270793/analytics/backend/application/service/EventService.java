package io.github.danny270793.analytics.backend.application.service;

import io.github.danny270793.analytics.backend.application.dto.request.CreateEventRequest;
import io.github.danny270793.analytics.backend.application.dto.response.EventResponse;
import io.github.danny270793.analytics.backend.application.dto.request.UpdateEventRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface EventService {
    EventResponse createEvent(CreateEventRequest request);
    EventResponse findEventById(UUID id);
    Page<EventResponse> findAllEvents(Pageable pageable);
    EventResponse updateEvent(UUID id, UpdateEventRequest request);
    void deleteEvent(UUID id);
}
