package com.youtube.identityauthservice.domain.entities;

import com.youtube.identityauthservice.domain.valueobjects.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.time.Instant;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases for User domain entity.
 */
@DisplayName("User Tests")
class UserTest {

    @Nested
    @DisplayName("Creation Tests")
    class CreationTests {
        
        @Test
        @DisplayName("Should create user with basic information")
        void shouldCreateUserWithBasicInformation() {
            UserId userId = UserId.generate();
            Email email = Email.of("test@example.com");
            String aadSubject = "aad-subject-123";
            
            User user = User.create(userId, email, aadSubject);
            
            assertNotNull(user);
            assertEquals(userId, user.getId());
            assertEquals(email, user.getEmail());
            assertEquals(aadSubject, user.getAadSubject());
            assertEquals(UserStatus.ACTIVE, user.getStatus());
            assertEquals(Set.of(Role.USER), user.getRoles());
            assertFalse(user.isMfaEnabled());
            assertNull(user.getLastLoginAt());
        }
        
        @Test
        @DisplayName("Should create user with custom roles")
        void shouldCreateUserWithCustomRoles() {
            UserId userId = UserId.generate();
            Email email = Email.of("admin@example.com");
            String aadSubject = "aad-subject-456";
            Set<Role> roles = Set.of(Role.ADMIN, Role.CREATOR);
            
            User user = User.createWithRoles(userId, email, aadSubject, roles);
            
            assertNotNull(user);
            assertEquals(roles, user.getRoles());
        }
        
        @Test
        @DisplayName("Should throw exception for null parameters")
        void shouldThrowExceptionForNullParameters() {
            assertThrows(NullPointerException.class, () -> User.create(null, Email.of("test@example.com"), "subject"));
            assertThrows(NullPointerException.class, () -> User.create(UserId.generate(), null, "subject"));
            assertThrows(NullPointerException.class, () -> User.create(UserId.generate(), Email.of("test@example.com"), null));
        }
    }

    @Nested
    @DisplayName("Status Management Tests")
    class StatusManagementTests {
        
        @Test
        @DisplayName("Should lock user account")
        void shouldLockUserAccount() {
            User user = createTestUser();
            
            user.lock();
            
            assertEquals(UserStatus.LOCKED, user.getStatus());
            assertFalse(user.isActive());
        }
        
        @Test
        @DisplayName("Should unlock user account")
        void shouldUnlockUserAccount() {
            User user = createTestUser();
            user.lock();
            
            user.unlock();
            
            assertEquals(UserStatus.ACTIVE, user.getStatus());
            assertTrue(user.isActive());
        }
        
        @Test
        @DisplayName("Should check if user is active")
        void shouldCheckIfUserIsActive() {
            User user = createTestUser();
            
            assertTrue(user.isActive());
            
            user.lock();
            assertFalse(user.isActive());
        }
    }

    @Nested
    @DisplayName("MFA Management Tests")
    class MfaManagementTests {
        
        @Test
        @DisplayName("Should enable MFA")
        void shouldEnableMfa() {
            User user = createTestUser();
            
            user.enableMfa();
            
            assertTrue(user.isMfaEnabled());
        }
        
        @Test
        @DisplayName("Should check MFA status")
        void shouldCheckMfaStatus() {
            User user = createTestUser();
            
            assertFalse(user.isMfaEnabled());
            
            user.enableMfa();
            assertTrue(user.isMfaEnabled());
        }
    }

    @Nested
    @DisplayName("Role Management Tests")
    class RoleManagementTests {
        
        @Test
        @DisplayName("Should check if user has specific role")
        void shouldCheckIfUserHasSpecificRole() {
            User user = createTestUser();
            
            assertTrue(user.hasRole(Role.USER));
            assertFalse(user.hasRole(Role.ADMIN));
        }
        
        @Test
        @DisplayName("Should add role to user")
        void shouldAddRoleToUser() {
            User user = createTestUser();
            
            user.addRole(Role.CREATOR);
            
            assertTrue(user.hasRole(Role.CREATOR));
            assertTrue(user.hasRole(Role.USER));
        }
        
        @Test
        @DisplayName("Should remove role from user")
        void shouldRemoveRoleFromUser() {
            User user = createTestUser();
            user.addRole(Role.CREATOR);
            
            user.removeRole(Role.CREATOR);
            
            assertFalse(user.hasRole(Role.CREATOR));
            assertTrue(user.hasRole(Role.USER));
        }
    }

    @Nested
    @DisplayName("Login Tracking Tests")
    class LoginTrackingTests {
        
        @Test
        @DisplayName("Should update last login timestamp")
        void shouldUpdateLastLoginTimestamp() {
            User user = createTestUser();
            assertNull(user.getLastLoginAt());
            
            user.updateLastLogin();
            
            assertNotNull(user.getLastLoginAt());
            assertTrue(user.getLastLoginAt().isBefore(Instant.now().plusSeconds(1)));
        }
    }

    @Nested
    @DisplayName("Equality Tests")
    class EqualityTests {
        
        @Test
        @DisplayName("Should be equal for same user ID")
        void shouldBeEqualForSameUserId() {
            UserId userId = UserId.generate();
            User user1 = User.create(userId, Email.of("test1@example.com"), "subject1");
            User user2 = User.create(userId, Email.of("test2@example.com"), "subject2");
            
            assertEquals(user1, user2);
            assertEquals(user1.hashCode(), user2.hashCode());
        }
        
        @Test
        @DisplayName("Should not be equal for different user IDs")
        void shouldNotBeEqualForDifferentUserIds() {
            User user1 = createTestUser();
            User user2 = createTestUser();
            
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

    private User createTestUser() {
        return User.create(
            UserId.generate(),
            Email.of("test@example.com"),
            "test-subject"
        );
    }
}
