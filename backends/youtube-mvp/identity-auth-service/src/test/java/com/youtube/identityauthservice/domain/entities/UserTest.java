package com.youtube.identityauthservice.domain.entities;

import com.youtube.common.domain.shared.valueobjects.UserId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("User Entity Tests")
class UserTest {

    private static final Instant NOW = Instant.parse("2024-01-01T00:00:00Z");
    private static final UserId TEST_USER_ID = UserId.from("user-123");

    @Nested
    @DisplayName("Creation Tests")
    class CreationTests {

        @Test
        @DisplayName("Should create user with builder")
        void shouldCreateUserWithBuilder() {
            User user = User.builder()
                    .id(TEST_USER_ID)
                    .email("test@example.com")
                    .normalizedEmail("test@example.com")
                    .displayName("Test User")
                    .emailVerified(true)
                    .status((short) 1)
                    .createdAt(NOW)
                    .updatedAt(NOW)
                    .version(0)
                    .build();

            assertNotNull(user);
            assertEquals(TEST_USER_ID, user.getId());
            assertEquals("test@example.com", user.getEmail());
            assertEquals("test@example.com", user.getNormalizedEmail());
            assertEquals("Test User", user.getDisplayName());
            assertTrue(user.isEmailVerified());
            assertEquals((short) 1, user.getStatus());
            assertEquals(0, user.getVersion());
        }

        @Test
        @DisplayName("Should create user with minimal fields")
        void shouldCreateUserWithMinimalFields() {
            User user = User.builder()
                    .id(TEST_USER_ID)
                    .email("test@example.com")
                    .normalizedEmail("test@example.com")
                    .displayName("User")
                    .emailVerified(false)
                    .status((short) 0)
                    .createdAt(NOW)
                    .updatedAt(NOW)
                    .version(0)
                    .build();

            assertNotNull(user);
            assertEquals(TEST_USER_ID, user.getId());
            assertFalse(user.isEmailVerified());
        }
    }

    @Nested
    @DisplayName("Immutable Update Tests")
    class ImmutableUpdateTests {

        @Test
        @DisplayName("Should create new user instance with updated email")
        void shouldCreateNewUserInstanceWithUpdatedEmail() {
            User original = createTestUser();
            String newEmail = "newemail@example.com";

            User updated = original.withEmail(newEmail);

            assertNotSame(original, updated);
            assertEquals(newEmail, updated.getEmail());
            assertEquals(original.getNormalizedEmail(), updated.getNormalizedEmail());
            assertEquals(original.getId(), updated.getId());
            assertNotEquals(original.getUpdatedAt(), updated.getUpdatedAt());
        }

        @Test
        @DisplayName("Should create new user instance with updated display name")
        void shouldCreateNewUserInstanceWithUpdatedDisplayName() {
            User original = createTestUser();
            String newDisplayName = "Updated Name";

            User updated = original.withDisplayName(newDisplayName);

            assertNotSame(original, updated);
            assertEquals(newDisplayName, updated.getDisplayName());
            assertEquals(original.getEmail(), updated.getEmail());
            assertNotEquals(original.getUpdatedAt(), updated.getUpdatedAt());
        }

        @Test
        @DisplayName("Should create new user instance with updated email verified status")
        void shouldCreateNewUserInstanceWithUpdatedEmailVerified() {
            User original = createTestUser();
            assertTrue(original.isEmailVerified());

            User updated = original.withEmailVerified(false);

            assertNotSame(original, updated);
            assertFalse(updated.isEmailVerified());
            assertEquals(original.getEmail(), updated.getEmail());
        }

        @Test
        @DisplayName("Should increment version when marking updated")
        void shouldIncrementVersionWhenMarkingUpdated() {
            User original = createTestUser();
            long originalVersion = original.getVersion();

            User updated = original.markUpdated();

            assertNotSame(original, updated);
            assertEquals(originalVersion + 1, updated.getVersion());
            assertNotEquals(original.getUpdatedAt(), updated.getUpdatedAt());
        }
    }

    @Nested
    @DisplayName("Equality Tests")
    class EqualityTests {

        @Test
        @DisplayName("Should be equal for same user ID")
        void shouldBeEqualForSameUserId() {
            User user1 = User.builder()
                    .id(TEST_USER_ID)
                    .email("test1@example.com")
                    .normalizedEmail("test1@example.com")
                    .displayName("User 1")
                    .emailVerified(true)
                    .status((short) 1)
                    .createdAt(NOW)
                    .updatedAt(NOW)
                    .version(0)
                    .build();

            User user2 = User.builder()
                    .id(TEST_USER_ID)
                    .email("test2@example.com")
                    .normalizedEmail("test2@example.com")
                    .displayName("User 2")
                    .emailVerified(false)
                    .status((short) 0)
                    .createdAt(NOW)
                    .updatedAt(NOW)
                    .version(1)
                    .build();

            assertEquals(user1, user2);
            assertEquals(user1.hashCode(), user2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal for different user IDs")
        void shouldNotBeEqualForDifferentUserIds() {
            User user1 = createTestUser();
            User user2 = User.builder()
                    .id(UserId.from("user-456"))
                    .email("test@example.com")
                    .normalizedEmail("test@example.com")
                    .displayName("Test User")
                    .emailVerified(true)
                    .status((short) 1)
                    .createdAt(NOW)
                    .updatedAt(NOW)
                    .version(0)
                    .build();

            assertNotEquals(user1, user2);
        }

        @Test
        @DisplayName("Should not be equal to null")
        void shouldNotBeEqualToString() {
            User user = createTestUser();

            assertNotEquals(user, null);
            assertNotEquals(user, "string");
        }
    }

    @Nested
    @DisplayName("AggregateRoot Tests")
    class AggregateRootTests {

        @Test
        @DisplayName("Should extend AggregateRoot with UserId")
        void shouldExtendAggregateRootWithUserId() {
            User user = createTestUser();

            assertTrue(user instanceof com.youtube.common.domain.core.AggregateRoot);
            assertEquals(TEST_USER_ID, user.getId());
            assertNotNull(user.getVersion());
        }

        @Test
        @DisplayName("Should track version for optimistic concurrency")
        void shouldTrackVersionForOptimisticConcurrency() {
            User user = createTestUser();
            assertEquals(0, user.getVersion());

            User updated = user.markUpdated();
            assertEquals(1, updated.getVersion());
        }
    }

    private User createTestUser() {
        return User.builder()
                .id(TEST_USER_ID)
                .email("test@example.com")
                .normalizedEmail("test@example.com")
                .displayName("Test User")
                .emailVerified(true)
                .status((short) 1)
                .createdAt(NOW)
                .updatedAt(NOW)
                .version(0)
                .build();
    }
}
