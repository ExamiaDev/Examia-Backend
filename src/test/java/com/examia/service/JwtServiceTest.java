package com.examia.service;

import com.examia.model.Role;
import com.examia.model.User;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private static final long JWT_EXPIRATION = 3_600_000L; // 1 hour for tests
    private static final String USER_PASSWORD = "secret";
    private static final String SECRET_KEY = Base64.getEncoder().encodeToString(
            "01234567890123456789012345678901".getBytes(StandardCharsets.UTF_8)
    );

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", SECRET_KEY);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", JWT_EXPIRATION);
    }

    @Test
    void generateTokenShouldReturnValidTokenForUser() {
        User user = User.builder()
                .email("usuario@ejemplo.com")
                .displayUsername("usuario@ejemplo.com")
                .password(USER_PASSWORD)
                .nombre("Ana")
                .apellido("García")
                .role(Role.ALUMNO)
                .enabled(true)
                .build();

        String token = jwtService.generateToken(user);

        assertNotNull(token);
        assertEquals(user.getUsername(), jwtService.extractEmail(token));
        assertTrue(jwtService.isTokenValid(token, user));
        assertEquals(JWT_EXPIRATION, jwtService.getExpirationTime());
    }

    @Test
    void isTokenValidShouldReturnFalseForDifferentUser() {
        User user = User.builder()
                .email("usuario@ejemplo.com")
                .displayUsername("usuario@ejemplo.com")
                .password(USER_PASSWORD)
                .nombre("Ana")
                .apellido("García")
                .role(Role.ALUMNO)
                .enabled(true)
                .build();

        User otherUser = User.builder()
                .email("otro@ejemplo.com")
                .displayUsername("otro@ejemplo.com")
                .password(USER_PASSWORD)
                .nombre("Diego")
                .apellido("Pérez")
                .role(Role.DOCENTE)
                .enabled(true)
                .build();

        String token = jwtService.generateToken(user);

        assertFalse(jwtService.isTokenValid(token, otherUser));
    }

    @Test
    void generateTokenWithExtraClaimsShouldIncludeSubject() {
        User user = User.builder()
                .email("docente@ejemplo.com")
                .displayUsername("docente@ejemplo.com")
                .password(USER_PASSWORD)
                .role(Role.DOCENTE)
                .enabled(true)
                .build();

        String token = jwtService.generateToken(Map.of("custom", "value"), user);

        assertNotNull(token);
        assertEquals("docente@ejemplo.com", jwtService.extractEmail(token));
        assertTrue(jwtService.isTokenValid(token, user));
    }

    @Test
    void extractEmailShouldThrowWhenTokenExpired() {
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", -1_000L);
        User user = User.builder()
                .email("usuario@ejemplo.com")
                .displayUsername("usuario@ejemplo.com")
                .password(USER_PASSWORD)
                .role(Role.ALUMNO)
                .enabled(true)
                .build();

        String token = jwtService.generateToken(user);

        assertThrows(ExpiredJwtException.class, () -> jwtService.extractEmail(token));
    }
}
