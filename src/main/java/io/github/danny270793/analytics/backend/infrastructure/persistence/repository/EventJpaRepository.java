package io.github.danny270793.analytics.backend.infrastructure.persistence.repository;

import io.github.danny270793.analytics.backend.infrastructure.persistence.entity.EventEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface EventJpaRepository extends JpaRepository<EventEntity, UUID> {
    /**
     * Finds all events for a specific user with pagination support.
     *
     * @param userId the user ID to filter by
     * @param pageable pagination information (page, size, sort)
     * @return a page of events belonging to the user
     */
    Page<EventEntity> findByUserId(UUID userId, Pageable pageable);
}
