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
import org.mockito.Answers;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {
    private static final String ENCODED_PASSWORD = "encoded-password";

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private StubJwtService jwtService;
    private AuthService authService;
    private AtomicReference<User> savedUserReference;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        savedUserReference = new AtomicReference<>();
        userRepository = mock(UserRepository.class, invocation -> {
            if ("save".equals(invocation.getMethod().getName())) {
                User savedUser = invocation.getArgument(0, User.class);
                savedUserReference.set(savedUser);
                return savedUser;
            }
            return Answers.RETURNS_DEFAULTS.answer(invocation);
        });
        passwordEncoder = mock(PasswordEncoder.class);
        jwtService = new StubJwtService("token-123");
        authService = new AuthService(userRepository, passwordEncoder, jwtService);

        registerRequest = RegisterRequest.builder()
                .email("usuario@ejemplo.com")
                .password("miPassword123")
                .nombre("Juan")
                .apellido("Vega")
                .role(Role.ALUMNO)
                .build();

        loginRequest = LoginRequest.builder()
                .email("usuario@ejemplo.com")
                .password("miPassword123")
                .build();
    }

    @Test
    void registerWhenEmailDoesNotExistShouldSaveUserAndReturnAuthResponse() {
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn(ENCODED_PASSWORD);

        AuthResponse response = authService.register(registerRequest);

        User savedUser = savedUserReference.get();

        assertNotNull(savedUser);
        assertEquals(registerRequest.getEmail(), savedUser.getEmail());
        assertEquals(ENCODED_PASSWORD, savedUser.getPassword());
        assertEquals(registerRequest.getNombre(), savedUser.getNombre());
        assertEquals(registerRequest.getApellido(), savedUser.getApellido());
        assertEquals(registerRequest.getRole(), savedUser.getRole());
        assertTrue(savedUser.isEnabled());
        assertNotNull(savedUser.getCreatedAt());
        assertTrue(savedUser.getCreatedAt().isBefore(LocalDateTime.now().plusSeconds(1)));

        assertEquals("token-123", response.getToken());
        assertEquals(registerRequest.getEmail(), response.getEmail());
        assertEquals(registerRequest.getNombre(), response.getNombre());
        assertEquals(registerRequest.getApellido(), response.getApellido());
        assertEquals(registerRequest.getRole(), response.getRole());
        assertEquals("Usuario registrado exitosamente", response.getMessage());
    }

    @Test
    void registerWhenEmailAlreadyExistsShouldThrowUserAlreadyExistsException() {
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);

        UserAlreadyExistsException exception = assertThrows(
                UserAlreadyExistsException.class,
                () -> authService.register(registerRequest)
        );

        assertTrue(exception.getMessage().contains(registerRequest.getEmail()));
        assertNull(savedUserReference.get());
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
        assertEquals("Inicio de sesión exitoso", response.getMessage());
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

        assertTrue(exception.getMessage().contains("contraseña"));
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

        assertEquals("La cuenta está deshabilitada", exception.getMessage());
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
