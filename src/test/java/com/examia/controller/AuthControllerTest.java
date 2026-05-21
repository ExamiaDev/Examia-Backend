package com.examia.controller;

import com.examia.dto.AuthResponse;
import com.examia.dto.LoginRequest;
import com.examia.dto.LoginUadeRequest;
import com.examia.dto.RegisterRequest;
import com.examia.exception.InvalidCredentialsException;
import com.examia.exception.UserAlreadyExistsException;
import com.examia.exception.UserNotFoundException;
import com.examia.model.Role;
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
    void registerWhenValidRequestShouldReturnCreatedStatus() {
        RegisterRequest registerRequest = validRegisterRequest();
        AuthResponse authResponse = AuthResponse.builder()
                .token("jwt-token")
                .email("nuevo@ejemplo.com")
                .nombre("Juan")
                .apellido("Perez")
                .role(Role.ALUMNO)
                .message("Registro exitoso")
                .build();

        when(authService.register(any(RegisterRequest.class))).thenReturn(authResponse);

        ResponseEntity<AuthResponse> response = controller.register(registerRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        AuthResponse body = response.getBody();
        assertNotNull(body);
        assertEquals("jwt-token", body.getToken());
        assertEquals("nuevo@ejemplo.com", body.getEmail());
        assertEquals("Juan", body.getNombre());
        assertEquals("Perez", body.getApellido());
        assertEquals(Role.ALUMNO, body.getRole());
        assertEquals("Registro exitoso", body.getMessage());
    }

    @Test
    void registerWhenEmailAlreadyExistsShouldThrowUserAlreadyExistsException() {
        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new UserAlreadyExistsException("Ya existe un usuario con el email 'nuevo@ejemplo.com'"));

        RegisterRequest request = validRegisterRequest();
        UserAlreadyExistsException exception = assertThrows(
                UserAlreadyExistsException.class,
                () -> controller.register(request)
        );

        assertTrue(exception.getMessage().contains("Ya existe un usuario"));
    }

    @Test
    void registerWhenUsernameAlreadyExistsShouldThrowUserAlreadyExistsException() {
        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new UserAlreadyExistsException("Ya existe un usuario con el nombre de usuario 'juanperez'"));

        RegisterRequest request = validRegisterRequest();
        UserAlreadyExistsException exception = assertThrows(
                UserAlreadyExistsException.class,
                () -> controller.register(request)
        );

        assertTrue(exception.getMessage().contains("nombre de usuario"));
    }

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
        var loginRequest = validLoginRequest();

        InvalidCredentialsException exception = assertThrows(
                InvalidCredentialsException.class,
                () -> controller.login(loginRequest)
        );

        assertEquals("La contrasena es incorrecta", exception.getMessage());
    }

    @Test
    void loginWhenUserNotFoundShouldThrowUserNotFoundException() {
        UserNotFoundException exception = new UserNotFoundException("Usuario no encontrado");
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(exception);

        UserNotFoundException thrownException = assertThrows(
                UserNotFoundException.class,
                () -> controller.login(validLoginRequest())
        );

        assertEquals("Usuario no encontrado", thrownException.getMessage());
    }

    @Test
    void healthShouldReturnServiceStatus() {
        ResponseEntity<String> response = controller.health();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Auth service is running", response.getBody());
    }

    @Test
    void loginUadeWhenValidRequestShouldReturnAuthResponse() {
        LoginUadeRequest loginUadeRequest = validLoginUadeRequest();
        AuthResponse authResponse = AuthResponse.builder()
                .token("jwt-token-uade")
                .email("usuario@uade.edu.ar")
                .nombre("Juan")
                .apellido("Rodriguez")
                .role(Role.ALUMNO)
                .message("Inicio de sesion exitoso")
                .build();

        when(authService.loginUade(any(LoginUadeRequest.class))).thenReturn(authResponse);

        ResponseEntity<AuthResponse> response = controller.loginUade(loginUadeRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        AuthResponse body = response.getBody();
        assertNotNull(body);
        assertEquals("jwt-token-uade", body.getToken());
        assertEquals("usuario@uade.edu.ar", body.getEmail());
        assertEquals("Juan", body.getNombre());
        assertEquals("Rodriguez", body.getApellido());
        assertEquals(Role.ALUMNO, body.getRole());
        assertEquals("Inicio de sesion exitoso", body.getMessage());
    }

    @Test
    void loginUadeWhenCredentialsAreInvalidShouldThrowInvalidCredentialsException() {
        when(authService.loginUade(any(LoginUadeRequest.class)))
                .thenThrow(new InvalidCredentialsException("El email no coincide con el legajo"));

        LoginUadeRequest request = validLoginUadeRequest();
        InvalidCredentialsException exception = assertThrows(
                InvalidCredentialsException.class,
                () -> controller.loginUade(request)
        );

        assertTrue(exception.getMessage().contains("email"));
    }

    @Test
    void loginUadeWhenLegajoNotFoundShouldThrowUserNotFoundException() {
        UserNotFoundException exception = new UserNotFoundException("Legajo no encontrado");
        when(authService.loginUade(any(LoginUadeRequest.class)))
                .thenThrow(exception);

        LoginUadeRequest request = validLoginUadeRequest();
        UserNotFoundException thrownException = assertThrows(
                UserNotFoundException.class,
                () -> controller.loginUade(request)
        );

        assertEquals("Legajo no encontrado", thrownException.getMessage());
    }

    @Test
    void loginUadeWhenPasswordIncorrectShouldThrowInvalidCredentialsException() {
        when(authService.loginUade(any(LoginUadeRequest.class)))
                .thenThrow(new InvalidCredentialsException("La contraseña es incorrecta"));

        LoginUadeRequest request = validLoginUadeRequest();
        InvalidCredentialsException exception = assertThrows(
                InvalidCredentialsException.class,
                () -> controller.loginUade(request)
        );

        assertTrue(exception.getMessage().contains("contraseña"));
    }

    private LoginRequest validLoginRequest() {
        return LoginRequest.builder()
                .email("usuario@ejemplo.com")
                .password("password123")
                .build();
    }

    private RegisterRequest validRegisterRequest() {
        return RegisterRequest.builder()
                .nombre("Juan")
                .apellido("Perez")
                .username("juanperez")
                .email("nuevo@ejemplo.com")
                .recoveryEmail("recovery@ejemplo.com")
                .password("password123")
                .build();
    }

    private LoginUadeRequest validLoginUadeRequest() {
        return LoginUadeRequest.builder()
                .legajo("123456")
                .email("usuario@uade.edu.ar")
                .password("password123")
                .build();
    }
}
