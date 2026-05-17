package com.examia.service;

import com.examia.dto.AuthResponse;
import com.examia.dto.LoginRequest;
import com.examia.exception.InvalidCredentialsException;
import com.examia.exception.UserNotFoundException;
import com.examia.model.Role;
import com.examia.model.User;
import com.examia.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {
    private static final String ENCODED_PASSWORD = "encoded-password";

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private StubJwtService jwtService;
    private AuthService authService;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        jwtService = new StubJwtService("token-123");
        authService = new AuthService(userRepository, passwordEncoder, jwtService);

        loginRequest = LoginRequest.builder()
                .email("usuario@ejemplo.com")
                .password("miPassword123")
                .build();
    }

    @Test
    void loginWhenCredentialsAreValidShouldReturnAuthResponse() {
        User existingUser = User.builder()
                .email(loginRequest.getEmail())
                .password(ENCODED_PASSWORD)
                .nombre("Juan")
                .apellido("Vega")
                .role(Role.PROFESOR)
                .enabled(true)
                .build();

        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches(loginRequest.getPassword(), existingUser.getPassword())).thenReturn(true);
        jwtService.setToken("token-login");

        AuthResponse response = authService.login(loginRequest);

        assertEquals("token-login", response.getToken());
        assertEquals(existingUser.getEmail(), response.getEmail());
        assertEquals(existingUser.getNombre(), response.getNombre());
        assertEquals(existingUser.getApellido(), response.getApellido());
        assertEquals(existingUser.getRole(), response.getRole());
        assertEquals("Inicio de sesi\u00f3n exitoso", response.getMessage());
    }

    @Test
    void loginWhenUserNotFoundShouldThrowUserNotFoundException() {
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> authService.login(loginRequest)
        );

        assertTrue(exception.getMessage().contains(loginRequest.getEmail()));
    }

    @Test
    void loginWhenPasswordIsIncorrectShouldThrowInvalidCredentialsException() {
        User existingUser = User.builder()
                .email(loginRequest.getEmail())
                .password(ENCODED_PASSWORD)
                .nombre("Juan")
                .apellido("Vega")
                .role(Role.PROFESOR)
                .enabled(true)
                .build();

        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches(loginRequest.getPassword(), existingUser.getPassword())).thenReturn(false);

        InvalidCredentialsException exception = assertThrows(
                InvalidCredentialsException.class,
                () -> authService.login(loginRequest)
        );

        assertTrue(exception.getMessage().contains("contrase\u00f1a"));
    }

    @Test
    void loginWhenUserIsDisabledShouldThrowInvalidCredentialsException() {
        User existingUser = User.builder()
                .email(loginRequest.getEmail())
                .password(ENCODED_PASSWORD)
                .nombre("Juan")
                .apellido("Vega")
                .role(Role.PROFESOR)
                .enabled(false)
                .build();

        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches(loginRequest.getPassword(), existingUser.getPassword())).thenReturn(true);

        InvalidCredentialsException exception = assertThrows(
                InvalidCredentialsException.class,
                () -> authService.login(loginRequest)
        );

        assertEquals("La cuenta est\u00e1 deshabilitada", exception.getMessage());
    }

    private static class StubJwtService extends JwtService {
        private String token;

        StubJwtService(String token) {
            this.token = token;
        }

        void setToken(String token) {
            this.token = token;
        }

        @Override
        public String generateToken(User user) {
            return token;
        }
    }
}
