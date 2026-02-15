package io.github.danny270793.analytics.backend.presentation.controller;

import io.github.danny270793.analytics.backend.application.dto.request.CreateEventRequest;
import io.github.danny270793.analytics.backend.application.dto.response.EventResponse;
import io.github.danny270793.analytics.backend.application.dto.request.UpdateEventRequest;
import io.github.danny270793.analytics.backend.application.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/events")
@Tag(name = "Events", description = "Event management endpoints - requires authentication")
@SecurityRequirement(name = "Bearer Authentication")
public class EventController {
    private static final Logger log = LoggerFactory.getLogger(EventController.class);

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping
    @Operation(summary = "Create a new event", description = "Creates a new event for the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Event created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = EventResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid",
                    content = @Content)
    })
    public ResponseEntity<EventResponse> createEvent(@Valid @RequestBody CreateEventRequest request) {
        log.info("POST /api/events - Received create event request: type={}", request.getType());
        EventResponse response = eventService.createEvent(request);
        log.info("POST /api/events - Event created successfully: id={}", response.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get event by ID", description = "Retrieves an event by its ID. User can only access their own events.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Event found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = EventResponse.class))),
            @ApiResponse(responseCode = "404", description = "Event not found",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - Event belongs to another user",
                    content = @Content)
    })
    public ResponseEntity<EventResponse> getEventById(
            @Parameter(description = "Event ID", required = true) @PathVariable UUID id) {
        log.debug("GET /api/events/{} - Fetching event", id);
        EventResponse response = eventService.findEventById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Get all events", description = "Retrieves paginated list of events for the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Events retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content)
    })
    public ResponseEntity<Page<EventResponse>> getAllEvents(
            @Parameter(description = "Page number (0-indexed)", example = "0") 
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20") 
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field", example = "id") 
            @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Sort direction (ASC/DESC)", example = "DESC") 
            @RequestParam(defaultValue = "DESC") String sortDir) {
        
        log.debug("GET /api/events - Fetching paginated events: page={}, size={}, sort={}:{}", 
                page, size, sortBy, sortDir);
        
        Sort.Direction direction = sortDir.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<EventResponse> responses = eventService.findAllEvents(pageable);
        
        log.debug("GET /api/events - Returning {} events (page {} of {})", 
                responses.getNumberOfElements(), 
                responses.getNumber() + 1, 
                responses.getTotalPages());
        
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an event", description = "Updates an existing event. User can only update their own events.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Event updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = EventResponse.class))),
            @ApiResponse(responseCode = "404", description = "Event not found",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - Event belongs to another user",
                    content = @Content)
    })
    public ResponseEntity<EventResponse> updateEvent(
            @Parameter(description = "Event ID", required = true) @PathVariable UUID id,
            @RequestBody UpdateEventRequest request) {
        log.info("PUT /api/events/{} - Received update event request", id);
        EventResponse response = eventService.updateEvent(id, request);
        log.info("PUT /api/events/{} - Event updated successfully", id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an event (soft delete)", description = "Soft deletes an event. User can only delete their own events.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Event deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Event not found",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - Event belongs to another user",
                    content = @Content)
    })
    public ResponseEntity<Void> deleteEvent(
            @Parameter(description = "Event ID", required = true) @PathVariable UUID id) {
        log.info("DELETE /api/events/{} - Received delete event request", id);
        eventService.deleteEvent(id);
        log.info("DELETE /api/events/{} - Event deleted successfully", id);
        return ResponseEntity.noContent().build();
    }
}
