package com.examia.dto;

import com.examia.model.Role;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthResponseTest {

    @Test
    void authResponseBuilderAndGetters() {
        AuthResponse response = AuthResponse.builder()
                .token("jwt-token")
                .email("test@ejemplo.com")
                .nombre("Juan")
                .apellido("Perez")
                .role(Role.ALUMNO)
                .message("Success")
                .build();

        assertEquals("jwt-token", response.getToken());
        assertEquals("test@ejemplo.com", response.getEmail());
        assertEquals("Juan", response.getNombre());
        assertEquals("Perez", response.getApellido());
        assertEquals(Role.ALUMNO, response.getRole());
        assertEquals("Success", response.getMessage());
    }

    @Test
    void authResponseNoArgsConstructor() {
        AuthResponse response = new AuthResponse();
        assertNull(response.getToken());
        assertNull(response.getEmail());
        assertNull(response.getNombre());
        assertNull(response.getApellido());
        assertNull(response.getRole());
        assertNull(response.getMessage());
    }

    @Test
    void authResponseAllArgsConstructor() {
        AuthResponse response = new AuthResponse(
                "id-123",
                "token",
                "email@test.com",
                "Juan",
                "Perez",
                "12345",
                Role.DOCENTE,
                "Message"
        );
        assertEquals("id-123", response.getId());
        assertEquals("token", response.getToken());
        assertEquals("email@test.com", response.getEmail());
        assertEquals("Juan", response.getNombre());
        assertEquals("Perez", response.getApellido());
        assertEquals("12345", response.getLegajo());
        assertEquals(Role.DOCENTE, response.getRole());
        assertEquals("Message", response.getMessage());
    }

    @Test
    void authResponseSetters() {
        AuthResponse response = new AuthResponse();
        response.setToken("new-token");
        response.setEmail("new@test.com");
        response.setNombre("Pedro");
        response.setApellido("Lopez");
        response.setRole(Role.DOCENTE);
        response.setMessage("New Message");

        assertEquals("new-token", response.getToken());
        assertEquals("new@test.com", response.getEmail());
        assertEquals("Pedro", response.getNombre());
        assertEquals("Lopez", response.getApellido());
        assertEquals(Role.DOCENTE, response.getRole());
        assertEquals("New Message", response.getMessage());
    }
}

