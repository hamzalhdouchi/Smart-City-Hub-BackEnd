package com.smartlogi.smart_city_hub.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PasswordGeneratorServiceTest {

    private PasswordGeneratorService passwordGeneratorService;

    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL = "!@#$%^&*()_+-=[]{}|;:,.<>?";

    @BeforeEach
    void setUp() {
        passwordGeneratorService = new PasswordGeneratorService();
    }

    // ========== generateSecurePassword ==========

    @Nested
    @DisplayName("generateSecurePassword")
    class GenerateSecurePasswordTests {

        @Test
        void should_GeneratePassword_WithLengthBetween10And12() {
            String password = passwordGeneratorService.generateSecurePassword();

            assertNotNull(password);
            assertTrue(password.length() >= 10, "Password length should be at least 10, got: " + password.length());
            assertTrue(password.length() <= 12, "Password length should be at most 12, got: " + password.length());
        }

        @Test
        void should_ContainAtLeastOneUppercaseCharacter() {
            String password = passwordGeneratorService.generateSecurePassword();

            boolean hasUppercase = password.chars()
                    .anyMatch(c -> UPPERCASE.indexOf(c) >= 0);
            assertTrue(hasUppercase, "Password should contain at least one uppercase letter: " + password);
        }

        @Test
        void should_ContainAtLeastOneLowercaseCharacter() {
            String password = passwordGeneratorService.generateSecurePassword();

            boolean hasLowercase = password.chars()
                    .anyMatch(c -> LOWERCASE.indexOf(c) >= 0);
            assertTrue(hasLowercase, "Password should contain at least one lowercase letter: " + password);
        }

        @Test
        void should_ContainAtLeastOneDigit() {
            String password = passwordGeneratorService.generateSecurePassword();

            boolean hasDigit = password.chars()
                    .anyMatch(c -> DIGITS.indexOf(c) >= 0);
            assertTrue(hasDigit, "Password should contain at least one digit: " + password);
        }

        @Test
        void should_ContainAtLeastOneSpecialCharacter() {
            String password = passwordGeneratorService.generateSecurePassword();

            boolean hasSpecial = password.chars()
                    .anyMatch(c -> SPECIAL.indexOf(c) >= 0);
            assertTrue(hasSpecial, "Password should contain at least one special character: " + password);
        }

        @Test
        void should_GenerateDifferentPasswordsEachTime() {
            Set<String> passwords = new HashSet<>();
            for (int i = 0; i < 20; i++) {
                passwords.add(passwordGeneratorService.generateSecurePassword());
            }
            // With 20 generated passwords, at least 2 should be different
            assertTrue(passwords.size() > 1, "Passwords should not all be identical");
        }

        @Test
        void should_OnlyContainAllowedCharacters() {
            String allAllowedChars = UPPERCASE + LOWERCASE + DIGITS + SPECIAL;
            String password = passwordGeneratorService.generateSecurePassword();

            for (char c : password.toCharArray()) {
                assertTrue(allAllowedChars.indexOf(c) >= 0,
                        "Character '" + c + "' is not in the allowed character set");
            }
        }

        @Test
        void should_MeetAllComplexityRequirements_ForMultipleGenerations() {
            for (int i = 0; i < 50; i++) {
                String password = passwordGeneratorService.generateSecurePassword();

                assertTrue(password.length() >= 10 && password.length() <= 12,
                        "Invalid length " + password.length() + " for: " + password);

                assertTrue(password.chars().anyMatch(c -> UPPERCASE.indexOf(c) >= 0),
                        "Missing uppercase in: " + password);
                assertTrue(password.chars().anyMatch(c -> LOWERCASE.indexOf(c) >= 0),
                        "Missing lowercase in: " + password);
                assertTrue(password.chars().anyMatch(c -> DIGITS.indexOf(c) >= 0),
                        "Missing digit in: " + password);
                assertTrue(password.chars().anyMatch(c -> SPECIAL.indexOf(c) >= 0),
                        "Missing special char in: " + password);
            }
        }
    }
}
