package com.examia.service;

import com.examia.dto.AuthResponse;
import com.examia.dto.LoginRequest;
import com.examia.dto.RegisterRequest;
import com.examia.exception.InvalidCredentialsException;
import com.examia.exception.UserAlreadyExistsException;
import com.examia.exception.UserNotFoundException;
import com.examia.model.Role;
import com.examia.model.User;
import com.examia.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    private static final String ENCODED_PASSWORD = "encoded-password";

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
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
        when(jwtService.generateToken(any(User.class))).thenReturn("token-123");

        AuthResponse response = authService.register(registerRequest);

        verify(userRepository, times(1)).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

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
        verify(userRepository, never()).save(any());
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
        when(jwtService.generateToken(existingUser)).thenReturn("token-login");

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
}
