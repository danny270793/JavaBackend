package io.github.danny270793.analytics.backend.infrastructure.security;

import io.github.danny270793.analytics.backend.infrastructure.persistence.entity.UserEntity;
import io.github.danny270793.analytics.backend.infrastructure.persistence.repository.UserJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private static final Logger log = LoggerFactory.getLogger(CustomUserDetailsService.class);

    private final UserJpaRepository userJpaRepository;

    public CustomUserDetailsService(UserJpaRepository userJpaRepository) {
        this.userJpaRepository = userJpaRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Loading user details for username: {}", username);
        UserEntity userEntity = userJpaRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("User not found for authentication: {}", username);
                    return new UsernameNotFoundException("User not found: " + username);
                });

        log.debug("User details loaded successfully for: {}", username);
        return new User(
                userEntity.getUsername(),
                userEntity.getPassword(),
                new ArrayList<>()
        );
    }
}
