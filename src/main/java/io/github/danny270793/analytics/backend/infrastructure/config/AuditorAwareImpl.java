package io.github.danny270793.analytics.backend.infrastructure.config;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of AuditorAware to provide the current auditor (user ID) for JPA auditing.
 * Retrieves the authenticated user's ID from Spring Security context.
 */
@Component
public class AuditorAwareImpl implements AuditorAware<UUID> {

    /**
     * Returns the current auditor (authenticated user's UUID).
     * If no user is authenticated, returns empty Optional.
     *
     * @return Optional containing the current user's UUID, or empty if not authenticated
     */
    @Override
    public Optional<UUID> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return Optional.empty();
        }
        
        try {
            return Optional.of(UUID.fromString(authentication.getName()));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
