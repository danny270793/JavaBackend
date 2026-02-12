package io.github.danny270793.analytics.backend.presentation.controller;

import io.github.danny270793.analytics.backend.application.dto.request.CreateEventRequest;
import io.github.danny270793.analytics.backend.application.dto.response.EventResponse;
import io.github.danny270793.analytics.backend.application.dto.request.UpdateEventRequest;
import io.github.danny270793.analytics.backend.application.service.EventService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/events")
public class EventController {
    private static final Logger log = LoggerFactory.getLogger(EventController.class);

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping
    public ResponseEntity<EventResponse> createEvent(@Valid @RequestBody CreateEventRequest request) {
        log.info("POST /api/events - Received create event request: type={}", request.getType());
        EventResponse response = eventService.createEvent(request);
        log.info("POST /api/events - Event created successfully: id={}", response.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> getEventById(@PathVariable UUID id) {
        log.debug("GET /api/events/{} - Fetching event", id);
        EventResponse response = eventService.findEventById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<EventResponse>> getAllEvents() {
        log.debug("GET /api/events - Fetching all events");
        List<EventResponse> responses = eventService.findAllEvents();
        log.debug("GET /api/events - Returning {} events", responses.size());
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventResponse> updateEvent(
            @PathVariable UUID id,
            @RequestBody UpdateEventRequest request) {
        log.info("PUT /api/events/{} - Received update event request", id);
        EventResponse response = eventService.updateEvent(id, request);
        log.info("PUT /api/events/{} - Event updated successfully", id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable UUID id) {
        log.info("DELETE /api/events/{} - Received delete event request", id);
        eventService.deleteEvent(id);
        log.info("DELETE /api/events/{} - Event deleted successfully", id);
        return ResponseEntity.noContent().build();
    }
}
