package io.github.danny270793.analytics.backend.infrastructure.persistence.repository;

import io.github.danny270793.analytics.backend.infrastructure.persistence.entity.EventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface EventJpaRepository extends JpaRepository<EventEntity, UUID> {
}
