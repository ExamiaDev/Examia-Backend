package com.examia.exception;

import com.examia.dto.ErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Map;

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
    void handleUserAlreadyExistsShouldReturnConflictResponse() {
        ResponseEntity<ErrorResponse> response = handler.handleUserAlreadyExists(
                new UserAlreadyExistsException("El email ya está registrado"),
                request
        );

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertErrorResponse(response.getBody(), 409, "Conflict", "El email ya está registrado");
    }

    @Test
    void handleInvalidCredentialsShouldReturnUnauthorizedResponse() {
        ResponseEntity<ErrorResponse> response = handler.handleInvalidCredentials(
                new InvalidCredentialsException("La contraseña es incorrecta"),
                request
        );

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertErrorResponse(response.getBody(), 401, "Unauthorized", "La contraseña es incorrecta");
    }

    @Test
    void handleInvalidResetCodeShouldReturnBadRequestResponse() {
        ResponseEntity<ErrorResponse> response = handler.handleInvalidResetCode(
                new InvalidResetCodeException("El código ha expirado"),
                request
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertErrorResponse(response.getBody(), 400, "Bad Request", "El código ha expirado");
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
    void handleValidationExceptionsShouldReturnBadRequestWithErrors() {
        // Create a mock object for validation
        Object target = new Object();
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(target, "request");
        bindingResult.addError(new FieldError("request", "email", "El email es obligatorio"));
        bindingResult.addError(new FieldError("request", "password", "La contraseña es obligatoria"));

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<Map<String, Object>> response = handler.handleValidationExceptions(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(400, body.get("status"));
        assertEquals("Bad Request", body.get("error"));
        assertEquals("Error de validación", body.get("message"));
        assertNotNull(body.get("errors"));

        @SuppressWarnings("unchecked")
        Map<String, String> errors = (Map<String, String>) body.get("errors");
        assertEquals("El email es obligatorio", errors.get("email"));
        assertEquals("La contraseña es obligatoria", errors.get("password"));
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
