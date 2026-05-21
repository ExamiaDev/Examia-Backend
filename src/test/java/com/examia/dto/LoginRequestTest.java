package com.examia.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LoginRequestTest {

    @Test
    void loginRequestBuilderAndGetters() {
        LoginRequest request = LoginRequest.builder()
                .email("test@ejemplo.com")
                .password("password123")
                .build();

        assertEquals("test@ejemplo.com", request.getEmail());
        assertEquals("password123", request.getPassword());
    }

    @Test
    void loginRequestNoArgsConstructor() {
        LoginRequest request = new LoginRequest();
        assertNull(request.getEmail());
        assertNull(request.getPassword());
    }

    @Test
    void loginRequestAllArgsConstructor() {
        LoginRequest request = new LoginRequest("test@ejemplo.com", "password123");
        assertEquals("test@ejemplo.com", request.getEmail());
        assertEquals("password123", request.getPassword());
    }

    @Test
    void loginRequestSetters() {
        LoginRequest request = new LoginRequest();
        request.setEmail("nuevo@ejemplo.com");
        request.setPassword("newpass");

        assertEquals("nuevo@ejemplo.com", request.getEmail());
        assertEquals("newpass", request.getPassword());
    }
}

