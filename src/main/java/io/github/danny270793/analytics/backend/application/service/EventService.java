package io.github.danny270793.analytics.backend.application.service;

import io.github.danny270793.analytics.backend.application.dto.request.CreateEventRequest;
import io.github.danny270793.analytics.backend.application.dto.response.EventResponse;
import io.github.danny270793.analytics.backend.application.dto.request.UpdateEventRequest;

import java.util.List;
import java.util.UUID;

public interface EventService {
    EventResponse createEvent(CreateEventRequest request);
    EventResponse findEventById(UUID id);
    List<EventResponse> findAllEvents();
    EventResponse updateEvent(UUID id, UpdateEventRequest request);
    void deleteEvent(UUID id);
}
