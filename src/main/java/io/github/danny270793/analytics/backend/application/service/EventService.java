package io.github.danny270793.analytics.backend.application.service;

import io.github.danny270793.analytics.backend.application.dto.CreateEventRequest;
import io.github.danny270793.analytics.backend.application.dto.EventResponse;
import io.github.danny270793.analytics.backend.application.dto.UpdateEventRequest;

import java.util.List;
import java.util.UUID;

public interface EventService {
    EventResponse create(CreateEventRequest request);
    EventResponse read(UUID id);
    List<EventResponse> read();
    EventResponse update(UUID id, UpdateEventRequest request);
    void delete(UUID id);
}
