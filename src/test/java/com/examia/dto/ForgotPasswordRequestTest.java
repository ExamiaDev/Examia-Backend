package com.examia.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ForgotPasswordRequestTest {

    @Test
    void forgotPasswordRequestBuilderAndGetters() {
        ForgotPasswordRequest request = ForgotPasswordRequest.builder()
                .email("test@ejemplo.com")
                .build();

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

