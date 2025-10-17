package com.youtube.identityauthservice.domain.valueobjects;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases for UserId value object.
 */
@DisplayName("UserId Tests")
class UserIdTest {

    @Nested
    @DisplayName("Creation Tests")
    class CreationTests {
        
        @Test
        @DisplayName("Should create UserId from valid UUID")
        void shouldCreateUserIdFromValidUuid() {
            UUID uuid = UUID.randomUUID();
            UserId userId = UserId.of(uuid);
            
            assertNotNull(userId);
            assertEquals(uuid, userId.getValue());
        }
        
        @Test
        @DisplayName("Should generate new UserId")
        void shouldGenerateNewUserId() {
            UserId userId = UserId.generate();
            
            assertNotNull(userId);
            assertNotNull(userId.getValue());
        }
        
        @Test
        @DisplayName("Should create UserId from valid string")
        void shouldCreateUserIdFromValidString() {
            String uuidString = UUID.randomUUID().toString();
            UserId userId = UserId.fromString(uuidString);
            
            assertNotNull(userId);
            assertEquals(uuidString, userId.getValue().toString());
        }
        
        @Test
        @DisplayName("Should throw exception for null UUID")
        void shouldThrowExceptionForNullUuid() {
            assertThrows(NullPointerException.class, () -> UserId.of(null));
        }
        
        @Test
        @DisplayName("Should throw exception for invalid string format")
        void shouldThrowExceptionForInvalidStringFormat() {
            assertThrows(IllegalArgumentException.class, () -> UserId.fromString("invalid-uuid"));
        }
    }

    @Nested
    @DisplayName("Equality Tests")
    class EqualityTests {
        
        @Test
        @DisplayName("Should be equal for same UUID")
        void shouldBeEqualForSameUuid() {
            UUID uuid = UUID.randomUUID();
            UserId userId1 = UserId.of(uuid);
            UserId userId2 = UserId.of(uuid);
            
            assertEquals(userId1, userId2);
            assertEquals(userId1.hashCode(), userId2.hashCode());
        }
        
        @Test
        @DisplayName("Should not be equal for different UUIDs")
        void shouldNotBeEqualForDifferentUuids() {
            UserId userId1 = UserId.generate();
            UserId userId2 = UserId.generate();
            
            assertNotEquals(userId1, userId2);
        }
        
        @Test
        @DisplayName("Should not be equal to null")
        void shouldNotBeEqualToString() {
            UserId userId = UserId.generate();
            
            assertNotEquals(userId, null);
            assertNotEquals(userId, "string");
        }
    }

    @Nested
    @DisplayName("String Representation Tests")
    class StringRepresentationTests {
        
        @Test
        @DisplayName("Should return UUID string representation")
        void shouldReturnUuidStringRepresentation() {
            UUID uuid = UUID.randomUUID();
            UserId userId = UserId.of(uuid);
            
            assertEquals(uuid.toString(), userId.toString());
        }
    }
}
