package com.youtube.identityauthservice.domain.valueobjects;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases for Email value object.
 */
@DisplayName("Email Tests")
class EmailTest {

    @Nested
    @DisplayName("Creation Tests")
    class CreationTests {
        
        @Test
        @DisplayName("Should create Email from valid address")
        void shouldCreateEmailFromValidAddress() {
            String address = "test@example.com";
            Email email = Email.of(address);
            
            assertNotNull(email);
            assertEquals(address, email.getAddress());
        }
        
        @Test
        @DisplayName("Should normalize email address")
        void shouldNormalizeEmailAddress() {
            String address = "TEST@EXAMPLE.COM";
            Email email = Email.of(address);
            
            assertEquals("test@example.com", email.normalized());
        }
        
        @Test
        @DisplayName("Should handle email with spaces")
        void shouldHandleEmailWithSpaces() {
            String address = " test@example.com ";
            Email email = Email.of(address);
            
            assertEquals("test@example.com", email.normalized());
        }
        
        @Test
        @DisplayName("Should throw exception for null address")
        void shouldThrowExceptionForNullAddress() {
            assertThrows(NullPointerException.class, () -> Email.of(null));
        }
        
        @Test
        @DisplayName("Should throw exception for empty address")
        void shouldThrowExceptionForEmptyAddress() {
            assertThrows(IllegalArgumentException.class, () -> Email.of(""));
            assertThrows(IllegalArgumentException.class, () -> Email.of("   "));
        }
        
        @Test
        @DisplayName("Should throw exception for invalid email format")
        void shouldThrowExceptionForInvalidEmailFormat() {
            assertThrows(IllegalArgumentException.class, () -> Email.of("invalid-email"));
            assertThrows(IllegalArgumentException.class, () -> Email.of("@example.com"));
            assertThrows(IllegalArgumentException.class, () -> Email.of("test@"));
            assertThrows(IllegalArgumentException.class, () -> Email.of("test@.com"));
        }
    }

    @Nested
    @DisplayName("Equality Tests")
    class EqualityTests {
        
        @Test
        @DisplayName("Should be equal for same normalized email")
        void shouldBeEqualForSameNormalizedEmail() {
            Email email1 = Email.of("test@example.com");
            Email email2 = Email.of("TEST@EXAMPLE.COM");
            
            assertEquals(email1, email2);
            assertEquals(email1.hashCode(), email2.hashCode());
        }
        
        @Test
        @DisplayName("Should not be equal for different emails")
        void shouldNotBeEqualForDifferentEmails() {
            Email email1 = Email.of("test1@example.com");
            Email email2 = Email.of("test2@example.com");
            
            assertNotEquals(email1, email2);
        }
        
        @Test
        @DisplayName("Should not be equal to null")
        void shouldNotBeEqualToString() {
            Email email = Email.of("test@example.com");
            
            assertNotEquals(email, null);
            assertNotEquals(email, "test@example.com");
        }
    }

    @Nested
    @DisplayName("Valid Email Formats")
    class ValidEmailFormats {
        
        @Test
        @DisplayName("Should accept various valid email formats")
        void shouldAcceptVariousValidEmailFormats() {
            String[] validEmails = {
                "test@example.com",
                "user.name@example.com",
                "user+tag@example.com",
                "user123@example123.com",
                "test@sub.example.com",
                "a@b.co",
                "user@example-domain.com"
            };
            
            for (String validEmail : validEmails) {
                assertDoesNotThrow(() -> Email.of(validEmail), 
                    "Should accept email: " + validEmail);
            }
        }
    }
}
