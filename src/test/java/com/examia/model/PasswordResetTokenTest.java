package com.examia.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PasswordResetTokenTest {

    @Test
    void builderShouldCreateTokenWithAllFields() {
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(15);

        PasswordResetToken token = PasswordResetToken.builder()
                .id("token-123")
                .email("usuario@ejemplo.com")
                .code("123456")
                .expiresAt(expiresAt)
                .used(false)
                .build();

        assertEquals("token-123", token.getId());
        assertEquals("usuario@ejemplo.com", token.getEmail());
        assertEquals("123456", token.getCode());
        assertEquals(expiresAt, token.getExpiresAt());
        assertFalse(token.isUsed());
    }

    @Test
    void settersShouldUpdateFields() {
        PasswordResetToken token = new PasswordResetToken();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(15);

        token.setId("token-456");
        token.setEmail("otro@ejemplo.com");
        token.setCode("654321");
        token.setExpiresAt(expiresAt);
        token.setUsed(true);

        assertEquals("token-456", token.getId());
        assertEquals("otro@ejemplo.com", token.getEmail());
        assertEquals("654321", token.getCode());
        assertEquals(expiresAt, token.getExpiresAt());
        assertTrue(token.isUsed());
    }

    @Test
    void allArgsConstructorShouldCreateToken() {
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(15);

        PasswordResetToken token = new PasswordResetToken(
                "id-123",
                "test@ejemplo.com",
                "999999",
                expiresAt,
                false
        );

        assertEquals("id-123", token.getId());
        assertEquals("test@ejemplo.com", token.getEmail());
        assertEquals("999999", token.getCode());
        assertEquals(expiresAt, token.getExpiresAt());
        assertFalse(token.isUsed());
    }

    @Test
    void noArgsConstructorShouldCreateEmptyToken() {
        PasswordResetToken token = new PasswordResetToken();

        assertNull(token.getId());
        assertNull(token.getEmail());
        assertNull(token.getCode());
        assertNull(token.getExpiresAt());
        assertFalse(token.isUsed());
    }

    @Test
    void equalsShouldReturnTrueForSameValues() {
        LocalDateTime expiresAt = LocalDateTime.now();

        PasswordResetToken token1 = PasswordResetToken.builder()
                .id("123")
                .email("test@test.com")
                .code("123456")
                .expiresAt(expiresAt)
                .used(false)
                .build();

        PasswordResetToken token2 = PasswordResetToken.builder()
                .id("123")
                .email("test@test.com")
                .code("123456")
                .expiresAt(expiresAt)
                .used(false)
                .build();

        assertEquals(token1, token2);
        assertEquals(token1.hashCode(), token2.hashCode());
    }

    @Test
    void equalsShouldReturnFalseForDifferentValues() {
        PasswordResetToken token1 = PasswordResetToken.builder()
                .id("123")
                .email("test@test.com")
                .build();

        PasswordResetToken token2 = PasswordResetToken.builder()
                .id("456")
                .email("other@test.com")
                .build();

        assertNotEquals(token1, token2);
    }

    @Test
    void toStringShouldContainFields() {
        PasswordResetToken token = PasswordResetToken.builder()
                .id("123")
                .email("test@test.com")
                .code("123456")
                .build();

        String str = token.toString();

        assertTrue(str.contains("123"));
        assertTrue(str.contains("test@test.com"));
        assertTrue(str.contains("123456"));
    }

    @Test
    void canEqualShouldReturnTrueForSameClass() {
        PasswordResetToken token1 = new PasswordResetToken();
        PasswordResetToken token2 = new PasswordResetToken();

        assertTrue(token1.equals(token2));
    }
}

