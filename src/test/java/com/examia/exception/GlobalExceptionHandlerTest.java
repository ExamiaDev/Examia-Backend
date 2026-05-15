package com.examia.exception;

import com.examia.dto.ErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.BadCredentialsException;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler = new GlobalExceptionHandler();
    private MockHttpServletRequest request = new MockHttpServletRequest();

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = new MockHttpServletRequest("POST", "/api/auth/login");
    }

    @Test
    void handleUserNotFoundShouldReturnNotFoundResponse() {
        ResponseEntity<ErrorResponse> response = handler.handleUserNotFound(
                new UserNotFoundException("Usuario no encontrado"),
                request
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertErrorResponse(response.getBody(), 404, "Not Found", "Usuario no encontrado");
    }

    @Test
    void handleBadCredentialsShouldReturnUnauthorizedResponse() {
        ResponseEntity<ErrorResponse> response = handler.handleBadCredentials(
                new BadCredentialsException("bad credentials"),
                request
        );

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertErrorResponse(response.getBody(), 401, "Unauthorized", "Credenciales incorrectas");
    }

    @Test
    void handleGenericExceptionShouldReturnInternalServerErrorResponse() {
        ResponseEntity<ErrorResponse> response = handler.handleGenericException(
                new RuntimeException("database unavailable"),
                request
        );

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        ErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(500, body.getStatus());
        assertEquals("Internal Server Error", body.getError());
        assertNotNull(body.getMessage());
        assertEquals("/api/auth/login", body.getPath());
        assertNotNull(body.getTimestamp());
    }

    private void assertErrorResponse(ErrorResponse body, int status, String error, String message) {
        assertNotNull(body);
        assertEquals(status, body.getStatus());
        assertEquals(error, body.getError());
        assertEquals(message, body.getMessage());
        assertEquals("/api/auth/login", body.getPath());
        assertNotNull(body.getTimestamp());
    }
}
