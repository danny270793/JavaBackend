package io.github.danny270793.analytics.backend.infrastructure.service;

import io.github.danny270793.analytics.backend.application.dto.request.CreateEventRequest;
import io.github.danny270793.analytics.backend.application.dto.request.UpdateEventRequest;
import io.github.danny270793.analytics.backend.application.dto.response.EventResponse;
import io.github.danny270793.analytics.backend.domain.model.EventType;
import io.github.danny270793.analytics.backend.domain.exception.EventNotFoundException;
import io.github.danny270793.analytics.backend.domain.exception.UnauthorizedAccessException;
import io.github.danny270793.analytics.backend.infrastructure.persistence.entity.EventEntity;
import io.github.danny270793.analytics.backend.infrastructure.persistence.entity.UserEntity;
import io.github.danny270793.analytics.backend.infrastructure.persistence.repository.EventJpaRepository;
import io.github.danny270793.analytics.backend.infrastructure.persistence.repository.UserJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EventService Unit Tests")
class EventServiceImplTest {

    @Mock
    private EventJpaRepository eventJpaRepository;

    @Mock
    private UserJpaRepository userJpaRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private EventServiceImpl eventService;

    private EventEntity testEvent;
    private UUID testEventId;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testEventId = UUID.randomUUID();
        testUserId = UUID.randomUUID();
        
        testEvent = new EventEntity();
        testEvent.setId(testEventId);
        testEvent.setType(EventType.NAVIGATION);
        testEvent.setFrom("/home");
        testEvent.setTo("/dashboard");
        testEvent.setUserId(testUserId);
        testEvent.setCreatedAt(LocalDateTime.now());
        testEvent.setUpdatedAt(LocalDateTime.now());

        // Mock SecurityContext properly
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("testuser");
        
        // Mock UserJpaRepository to return a user when looking up by username
        UserEntity mockUser = new UserEntity();
        mockUser.setId(testUserId);
        mockUser.setUsername("testuser");
        when(userJpaRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));
    }

    @Test
    @DisplayName("Should create event successfully")
    void shouldCreateEventSuccessfully() {
        // Given
        CreateEventRequest request = new CreateEventRequest(EventType.NAVIGATION, "/home", "/profile");
        when(eventJpaRepository.save(any(EventEntity.class))).thenReturn(testEvent);

        // When
        EventResponse response = eventService.createEvent(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getType()).isEqualTo(EventType.NAVIGATION);
        assertThat(response.getUserId()).isEqualTo(testUserId);
        verify(eventJpaRepository).save(any(EventEntity.class));
    }

    @Test
    @DisplayName("Should find event by ID successfully when owned by user")
    void shouldFindEventByIdSuccessfully() {
        // Given
        when(eventJpaRepository.findById(testEventId)).thenReturn(Optional.of(testEvent));

        // When
        EventResponse response = eventService.findEventById(testEventId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(testEventId);
        assertThat(response.getType()).isEqualTo(EventType.NAVIGATION);
    }

    @Test
    @DisplayName("Should throw EventNotFoundException when event not found")
    void shouldThrowExceptionWhenEventNotFound() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(eventJpaRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> eventService.findEventById(nonExistentId))
                .isInstanceOf(EventNotFoundException.class)
                .hasMessageContaining(nonExistentId.toString());
    }

    @Test
    @DisplayName("Should throw UnauthorizedAccessException when accessing other user's event")
    void shouldThrowExceptionWhenAccessingOtherUsersEvent() {
        // Given
        UUID otherUserId = UUID.randomUUID();
        testEvent.setUserId(otherUserId);
        when(eventJpaRepository.findById(testEventId)).thenReturn(Optional.of(testEvent));

        // When/Then
        assertThatThrownBy(() -> eventService.findEventById(testEventId))
                .isInstanceOf(UnauthorizedAccessException.class);
    }

    @Test
    @DisplayName("Should find all events for current user with pagination")
    void shouldFindAllEventsWithPagination() {
        // Given
        List<EventEntity> events = List.of(testEvent);
        Page<EventEntity> eventPage = new PageImpl<>(events, PageRequest.of(0, 20), 1);
        Pageable pageable = PageRequest.of(0, 20);
        when(eventJpaRepository.findByUserId(testUserId, pageable)).thenReturn(eventPage);

        // When
        Page<EventResponse> response = eventService.findAllEvents(pageable);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getUserId()).isEqualTo(testUserId);
        assertThat(response.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should update event successfully when owned by user")
    void shouldUpdateEventSuccessfully() {
        // Given
        UpdateEventRequest request = new UpdateEventRequest(EventType.ACTION, "/profile", "/settings");
        when(eventJpaRepository.findById(testEventId)).thenReturn(Optional.of(testEvent));
        when(eventJpaRepository.save(any(EventEntity.class))).thenReturn(testEvent);

        // When
        EventResponse response = eventService.updateEvent(testEventId, request);

        // Then
        assertThat(response).isNotNull();
        verify(eventJpaRepository).save(testEvent);
    }

    @Test
    @DisplayName("Should throw UnauthorizedAccessException when updating other user's event")
    void shouldThrowExceptionWhenUpdatingOtherUsersEvent() {
        // Given
        UUID otherUserId = UUID.randomUUID();
        testEvent.setUserId(otherUserId);
        UpdateEventRequest request = new UpdateEventRequest(EventType.ACTION, "/profile", "/settings");
        when(eventJpaRepository.findById(testEventId)).thenReturn(Optional.of(testEvent));

        // When/Then
        assertThatThrownBy(() -> eventService.updateEvent(testEventId, request))
                .isInstanceOf(UnauthorizedAccessException.class);
        verify(eventJpaRepository, never()).save(any(EventEntity.class));
    }

    @Test
    @DisplayName("Should delete event successfully (soft delete) when owned by user")
    void shouldDeleteEventSuccessfully() {
        // Given
        when(eventJpaRepository.findById(testEventId)).thenReturn(Optional.of(testEvent));
        when(eventJpaRepository.save(any(EventEntity.class))).thenReturn(testEvent);

        // When
        eventService.deleteEvent(testEventId);

        // Then
        verify(eventJpaRepository).findById(testEventId);
        verify(eventJpaRepository).save(testEvent);
        assertThat(testEvent.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("Should throw UnauthorizedAccessException when deleting other user's event")
    void shouldThrowExceptionWhenDeletingOtherUsersEvent() {
        // Given
        UUID otherUserId = UUID.randomUUID();
        testEvent.setUserId(otherUserId);
        when(eventJpaRepository.findById(testEventId)).thenReturn(Optional.of(testEvent));

        // When/Then
        assertThatThrownBy(() -> eventService.deleteEvent(testEventId))
                .isInstanceOf(UnauthorizedAccessException.class);
        verify(eventJpaRepository, never()).save(any(EventEntity.class));
    }

    @Test
    @DisplayName("Should throw EventNotFoundException when deleting non-existent event")
    void shouldThrowExceptionWhenDeletingNonExistentEvent() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(eventJpaRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> eventService.deleteEvent(nonExistentId))
                .isInstanceOf(EventNotFoundException.class);
    }
}
