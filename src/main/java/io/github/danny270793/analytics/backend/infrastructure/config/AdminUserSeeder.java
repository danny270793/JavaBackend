package io.github.danny270793.analytics.backend.infrastructure.config;

import io.github.danny270793.analytics.backend.infrastructure.persistence.entity.UserEntity;
import io.github.danny270793.analytics.backend.infrastructure.persistence.repository.UserJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

/**
 * Default admin user seeder.
 * Creates default admin user on startup if it doesn't exist.
 * Active in ALL profiles (dev, prod, test) for consistent admin access.
 * Executes first (Order = 1) to ensure admin user exists before other seeders run.
 */
@Configuration
public class AdminUserSeeder {
    private static final Logger log = LoggerFactory.getLogger(AdminUserSeeder.class);

    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_EMAIL = "admin@analytics.local";
    private static final String ADMIN_PASSWORD = "admin";

    @Bean
    @Order(1)  // Run first to ensure admin user exists before other seeders
    CommandLineRunner seedAdminUser(
            UserJpaRepository userRepository,
            PasswordEncoder passwordEncoder) {
        return args -> {
            // Check if admin user already exists
            if (userRepository.findByUsername(ADMIN_USERNAME).isPresent()) {
                log.info("👤 Admin user already exists - skipping creation");
                return;
            }

            log.info("=".repeat(80));
            log.info("👤 Creating default admin user...");

            // Create new admin user entity (UUID will be auto-generated)
            UserEntity adminUser = new UserEntity();
            adminUser.setUsername(ADMIN_USERNAME);
            adminUser.setEmail(ADMIN_EMAIL);
            adminUser.setPassword(passwordEncoder.encode(ADMIN_PASSWORD));

            UserEntity savedUser = userRepository.save(adminUser);

            log.info("✅ Default admin user created successfully");
            log.info("🔑 Credentials: username='{}' password='{}'", ADMIN_USERNAME, ADMIN_PASSWORD);
            log.info("🆔 Admin user ID: {}", savedUser.getId());
            log.warn("⚠️  SECURITY WARNING: Change default admin password before production deployment!");
            log.info("=".repeat(80));
        };
    }
}
