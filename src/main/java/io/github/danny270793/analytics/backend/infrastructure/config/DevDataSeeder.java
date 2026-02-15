package io.github.danny270793.analytics.backend.infrastructure.config;

import io.github.danny270793.analytics.backend.domain.model.EventType;
import io.github.danny270793.analytics.backend.infrastructure.persistence.entity.EventEntity;
import io.github.danny270793.analytics.backend.infrastructure.persistence.entity.UserEntity;
import io.github.danny270793.analytics.backend.infrastructure.persistence.repository.EventJpaRepository;
import io.github.danny270793.analytics.backend.infrastructure.persistence.repository.UserJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * Database seeder for development environment.
 * Automatically populates database with random test data on application startup.
 * Only active in 'dev' profile.
 * 
 * Note: Admin user is created separately by AdminUserSeeder (active in all profiles).
 * This seeder creates additional test users (user1-user10) and their events.
 */
@Configuration
@Profile("dev")
public class DevDataSeeder {
    private static final Logger log = LoggerFactory.getLogger(DevDataSeeder.class);
    private static final Random random = new Random();

    // Sample data for generating realistic test content
    private static final String[] FIRST_NAMES = {
            "Alice", "Bob", "Charlie", "Diana", "Eve", "Frank", "Grace", "Henry",
            "Iris", "Jack", "Kate", "Leo", "Mary", "Nathan", "Olivia", "Peter"
    };

    private static final String[] LAST_NAMES = {
            "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller",
            "Davis", "Rodriguez", "Martinez", "Anderson", "Taylor", "Thomas", "Moore"
    };

    private static final String[] ROUTES = {
            "/home", "/dashboard", "/profile", "/settings", "/analytics", "/reports",
            "/users", "/events", "/admin", "/api-docs", "/help", "/about",
            "/products", "/cart", "/checkout", "/orders", "/search", "/notifications"
    };

    @Bean
    CommandLineRunner seedDatabase(
            UserJpaRepository userRepository,
            EventJpaRepository eventRepository,
            PasswordEncoder passwordEncoder) {
        return args -> {
            log.info("=".repeat(80));
            log.info("🌱 DEV PROFILE - Starting test data seeding...");
            log.info("=".repeat(80));

            // Check if test data already exists (skip admin user in count)
            long existingUsers = userRepository.count();
            long existingEvents = eventRepository.count();

            // Admin user will be created by AdminUserSeeder, so we check if there's more than 1 user
            if (existingUsers > 1 || existingEvents > 0) {
                log.info("📊 Database already contains test data (Users: {}, Events: {})", existingUsers, existingEvents);
                log.info("⏭️  Skipping seeding to avoid duplicates");
                log.info("💡 To reseed, drop the database and restart the application");
                log.info("=".repeat(80));
                return;
            }

            long startTime = System.currentTimeMillis();

            // Generate test users (user1-user10)
            List<UserEntity> users = generateUsers(10, passwordEncoder);
            List<UserEntity> savedUsers = userRepository.saveAll(users);
            log.info("✓ Created {} test users (user1-user10)", savedUsers.size());

            // Generate events for each test user
            List<EventEntity> events = generateEvents(savedUsers, 50);
            List<EventEntity> savedEvents = eventRepository.saveAll(events);
            log.info("✓ Created {} test events", savedEvents.size());

            long duration = System.currentTimeMillis() - startTime;
            log.info("=".repeat(80));
            log.info("✅ Test data seeding completed successfully in {}ms", duration);
            log.info("📊 Summary: {} test users, {} events", savedUsers.size(), savedEvents.size());
            log.info("🔑 Test credentials: username='user1' password='password' (and user2-user10)");
            log.info("👤 Admin credentials: username='admin' password='admin' (created by AdminUserSeeder)");
            log.info("=".repeat(80));
        };
    }

    /**
     * Generates random test users.
     */
    private List<UserEntity> generateUsers(int count, PasswordEncoder passwordEncoder) {
        List<UserEntity> users = new ArrayList<>();
        String encodedPassword = passwordEncoder.encode("password");

        for (int i = 1; i <= count; i++) {
            String firstName = FIRST_NAMES[random.nextInt(FIRST_NAMES.length)];
            String lastName = LAST_NAMES[random.nextInt(LAST_NAMES.length)];
            String username = "user" + i;
            String email = firstName.toLowerCase() + "." + lastName.toLowerCase() + i + "@example.com";

            UserEntity user = new UserEntity();
            user.setId(UUID.randomUUID());
            user.setUsername(username);
            user.setEmail(email);
            user.setPassword(encodedPassword);

            users.add(user);
        }

        return users;
    }

    /**
     * Generates random events for users.
     */
    private List<EventEntity> generateEvents(List<UserEntity> users, int eventsPerUser) {
        List<EventEntity> events = new ArrayList<>();
        EventType[] eventTypes = EventType.values();

        for (UserEntity user : users) {
            int eventCount = random.nextInt(eventsPerUser) + 10; // 10 to (10 + eventsPerUser) events per user

            for (int i = 0; i < eventCount; i++) {
                EventType type = eventTypes[random.nextInt(eventTypes.length)];
                String from = ROUTES[random.nextInt(ROUTES.length)];
                String to = ROUTES[random.nextInt(ROUTES.length)];

                // Avoid from and to being the same
                while (to.equals(from)) {
                    to = ROUTES[random.nextInt(ROUTES.length)];
                }

                EventEntity event = new EventEntity();
                event.setId(UUID.randomUUID());
                event.setType(type);
                event.setFrom(from);
                event.setTo(to);
                event.setUserId(user.getId());

                events.add(event);
            }
        }

        return events;
    }
}
