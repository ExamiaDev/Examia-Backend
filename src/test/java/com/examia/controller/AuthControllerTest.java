package com.examia.controller;

import com.examia.dto.AuthResponse;
import com.examia.dto.LoginRequest;
import com.examia.exception.InvalidCredentialsException;
import com.examia.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthControllerTest {

    private final AuthService authService = mock(AuthService.class);
    private final AuthController controller = new AuthController(authService);

    @Test
    void loginWhenValidRequestShouldReturnAuthResponse() {
        LoginRequest loginRequest = validLoginRequest();
        AuthResponse authResponse = AuthResponse.builder()
                .token("jwt-token")
                .email("usuario@ejemplo.com")
                .nombre("Juan")
                .apellido("Perez")
                .message("Inicio de sesion exitoso")
                .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        ResponseEntity<AuthResponse> response = controller.login(loginRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        AuthResponse body = response.getBody();
        assertNotNull(body);
        assertEquals("jwt-token", body.getToken());
        assertEquals("usuario@ejemplo.com", body.getEmail());
        assertEquals("Juan", body.getNombre());
        assertEquals("Perez", body.getApellido());
        assertEquals("Inicio de sesion exitoso", body.getMessage());
    }

    @Test
    void loginWhenCredentialsAreInvalidShouldThrowInvalidCredentialsException() {
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new InvalidCredentialsException("La contrasena es incorrecta"));

        InvalidCredentialsException exception = assertThrows(
                InvalidCredentialsException.class,
                () -> controller.login(validLoginRequest())
        );

        assertEquals("La contrasena es incorrecta", exception.getMessage());
    }

    @Test
    void healthShouldReturnServiceStatus() {
        ResponseEntity<String> response = controller.health();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Auth service is running", response.getBody());
    }

    private LoginRequest validLoginRequest() {
        return LoginRequest.builder()
                .email("usuario@ejemplo.com")
                .password("password123")
                .build();
    }
}
