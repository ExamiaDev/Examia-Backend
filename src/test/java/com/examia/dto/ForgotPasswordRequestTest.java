package com.examia.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ForgotPasswordRequestTest {

    @Test
    void forgotPasswordRequestBuilderAndGetters() {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("test@ejemplo.com");

        assertEquals("test@ejemplo.com", request.getEmail());
    }

    @Test
    void forgotPasswordRequestNoArgsConstructor() {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        assertNull(request.getEmail());
    }

    @Test
    void forgotPasswordRequestSetters() {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("nuevo@ejemplo.com");

        assertEquals("nuevo@ejemplo.com", request.getEmail());
    }
}
