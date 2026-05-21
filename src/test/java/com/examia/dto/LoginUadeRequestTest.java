package com.examia.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LoginUadeRequestTest {

    @Test
    void loginUadeRequestBuilderAndGetters() {
        LoginUadeRequest request = LoginUadeRequest.builder()
                .legajo("123456")
                .email("test@uade.edu.ar")
                .password("password123")
                .build();

        assertEquals("123456", request.getLegajo());
        assertEquals("test@uade.edu.ar", request.getEmail());
        assertEquals("password123", request.getPassword());
    }

    @Test
    void loginUadeRequestNoArgsConstructor() {
        LoginUadeRequest request = new LoginUadeRequest();
        assertNull(request.getLegajo());
        assertNull(request.getEmail());
        assertNull(request.getPassword());
    }

    @Test
    void loginUadeRequestAllArgsConstructor() {
        LoginUadeRequest request = new LoginUadeRequest("123456", "test@uade.edu.ar", "password123");
        assertEquals("123456", request.getLegajo());
        assertEquals("test@uade.edu.ar", request.getEmail());
        assertEquals("password123", request.getPassword());
    }

    @Test
    void loginUadeRequestSetters() {
        LoginUadeRequest request = new LoginUadeRequest();
        request.setLegajo("789012");
        request.setEmail("nuevo@uade.edu.ar");
        request.setPassword("newpass");

        assertEquals("789012", request.getLegajo());
        assertEquals("nuevo@uade.edu.ar", request.getEmail());
        assertEquals("newpass", request.getPassword());
    }
}

