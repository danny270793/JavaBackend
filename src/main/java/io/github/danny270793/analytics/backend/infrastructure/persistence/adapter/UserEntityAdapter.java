package io.github.danny270793.analytics.backend.infrastructure.persistence.adapter;

import io.github.danny270793.analytics.backend.domain.model.User;
import io.github.danny270793.analytics.backend.infrastructure.persistence.entity.UserEntity;
import org.springframework.stereotype.Component;

/**
 * Adapter for converting between User domain model and UserEntity.
 * Follows the Adapter pattern to separate persistence concerns from domain logic.
 */
@Component
public class UserEntityAdapter {

    /**
     * Converts a domain User to a UserEntity for persistence.
     *
     * @param user the domain model
     * @return the persistence entity
     */
    public UserEntity toEntity(User user) {
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
     * @return the domain model
     */
    public User toDomain(UserEntity entity) {
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
