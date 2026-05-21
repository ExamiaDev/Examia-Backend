package com.examia.dto;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ErrorResponseTest {

    @Test
    void errorResponseBuilderAndGetters() {
        LocalDateTime timestamp = LocalDateTime.now();
        ErrorResponse response = ErrorResponse.builder()
                .status(400)
                .error("Bad Request")
                .message("Validation failed")
                .timestamp(timestamp)
                .path("/api/auth/login")
                .build();

        assertEquals(400, response.getStatus());
        assertEquals("Bad Request", response.getError());
        assertEquals("Validation failed", response.getMessage());
        assertEquals(timestamp, response.getTimestamp());
        assertEquals("/api/auth/login", response.getPath());
    }

    @Test
    void errorResponseSetters() {
        LocalDateTime timestamp = LocalDateTime.now();
        ErrorResponse response = new ErrorResponse();
        response.setStatus(401);
        response.setError("Unauthorized");
        response.setMessage("Invalid credentials");
        response.setTimestamp(timestamp);
        response.setPath("/api/auth/login-uade");

        assertEquals(401, response.getStatus());
        assertEquals("Unauthorized", response.getError());
        assertEquals("Invalid credentials", response.getMessage());
        assertEquals(timestamp, response.getTimestamp());
        assertEquals("/api/auth/login-uade", response.getPath());
    }
}

