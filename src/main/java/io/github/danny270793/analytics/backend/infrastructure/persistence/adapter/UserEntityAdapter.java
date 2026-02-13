package io.github.danny270793.analytics.backend.infrastructure.persistence.adapter;

import io.github.danny270793.analytics.backend.domain.model.User;
import io.github.danny270793.analytics.backend.infrastructure.persistence.entity.UserEntity;

/**
 * Static utility adapter for converting between User domain model and UserEntity.
 * Follows the Adapter pattern to separate persistence concerns from domain logic.
 * 
 * This class is designed as a stateless utility and should not be instantiated.
 */
public final class UserEntityAdapter {

    /**
     * Private constructor to prevent instantiation.
     */
    private UserEntityAdapter() {
        throw new UnsupportedOperationException("Utility class - cannot be instantiated");
    }

    /**
     * Converts a domain User to a UserEntity for persistence.
     *
     * @param user the domain model
     * @return the persistence entity, or null if input is null
     */
    public static UserEntity toEntity(User user) {
        if (user == null) {
            return null;
        }
        return new UserEntity(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPassword(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    /**
     * Converts a UserEntity to a domain User.
     *
     * @param entity the persistence entity
     * @return the domain model, or null if input is null
     */
    public static User toDomain(UserEntity entity) {
        if (entity == null) {
            return null;
        }
        return new User(
                entity.getId(),
                entity.getUsername(),
                entity.getEmail(),
                entity.getPassword(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
