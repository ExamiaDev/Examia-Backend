package com.examia.service;

import com.examia.dto.AuthResponse;
import com.examia.dto.LoginRequest;
import com.examia.dto.LoginUadeRequest;
import com.examia.dto.RegisterRequest;
import com.examia.exception.InvalidCredentialsException;
import com.examia.exception.UserAlreadyExistsException;
import com.examia.model.Role;
import com.examia.model.User;
import com.examia.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {
    private static final String ENCODED_PASSWORD = "encoded-password";

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private StubJwtService jwtService;
    private TokenBlacklistService tokenBlacklistService;
    private AuthService authService;
    private LoginRequest loginRequest;
    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        jwtService = new StubJwtService("token-123");
        tokenBlacklistService = mock(TokenBlacklistService.class);
        authService = new AuthService(userRepository, passwordEncoder, jwtService, tokenBlacklistService);

        loginRequest = LoginRequest.builder()
                .email("usuario@ejemplo.com")
                .password("miPassword123")
                .build();

        registerRequest = RegisterRequest.builder()
                .nombre("Juan")
                .apellido("Perez")
                .username("juanperez")
                .email("nuevo@ejemplo.com")
                .recoveryEmail("recovery@ejemplo.com")
                .password("password123")
                .build();
    }

    // ==================== TESTS DE REGISTRO ====================

    @Test
    void registerWhenValidRequestShouldReturnAuthResponse() {
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(userRepository.existsByDisplayUsername(registerRequest.getUsername())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn(ENCODED_PASSWORD);

        User savedUser = User.builder()
                .id("123")
                .nombre(registerRequest.getNombre())
                .apellido(registerRequest.getApellido())
                .displayUsername(registerRequest.getUsername())
                .email(registerRequest.getEmail())
                .recoveryEmail(registerRequest.getRecoveryEmail())
                .password(ENCODED_PASSWORD)
                .role(Role.ALUMNO)
                .enabled(true)
                .build();

        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        jwtService.setToken("token-register");

        AuthResponse response = authService.register(registerRequest);

        assertEquals("token-register", response.getToken());
        assertEquals(registerRequest.getEmail(), response.getEmail());
        assertEquals(registerRequest.getNombre(), response.getNombre());
        assertEquals(registerRequest.getApellido(), response.getApellido());
        assertEquals(Role.ALUMNO, response.getRole());
        assertEquals("Registro exitoso", response.getMessage());
    }

    @Test
    void registerWhenEmailAlreadyExistsShouldThrowUserAlreadyExistsException() {
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);

        UserAlreadyExistsException exception = assertThrows(
                UserAlreadyExistsException.class,
                () -> authService.register(registerRequest)
        );

        assertTrue(exception.getMessage().contains(registerRequest.getEmail()));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerWhenUsernameAlreadyExistsShouldThrowUserAlreadyExistsException() {
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(userRepository.existsByDisplayUsername(registerRequest.getUsername())).thenReturn(true);

        UserAlreadyExistsException exception = assertThrows(
                UserAlreadyExistsException.class,
                () -> authService.register(registerRequest)
        );

        assertTrue(exception.getMessage().contains(registerRequest.getUsername()));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerWhenNoRecoveryEmailShouldUseMainEmail() {
        RegisterRequest requestWithoutRecovery = RegisterRequest.builder()
                .nombre("Juan")
                .apellido("Perez")
                .username("juanperez")
                .email("nuevo@ejemplo.com")
                .password("password123")
                .build();

        when(userRepository.existsByEmail(requestWithoutRecovery.getEmail())).thenReturn(false);
        when(userRepository.existsByDisplayUsername(requestWithoutRecovery.getUsername())).thenReturn(false);
        when(passwordEncoder.encode(requestWithoutRecovery.getPassword())).thenReturn(ENCODED_PASSWORD);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            assertEquals(requestWithoutRecovery.getEmail(), user.getRecoveryEmail());
            return user;
        });
        jwtService.setToken("token-no-recovery");

        AuthResponse response = authService.register(requestWithoutRecovery);

        assertNotNull(response);
        verify(userRepository).save(argThat(user ->
            user.getRecoveryEmail().equals(requestWithoutRecovery.getEmail())
        ));
    }

    // ==================== TESTS DE LOGIN ====================

    @Test
    void loginWhenCredentialsAreValidShouldReturnAuthResponse() {
        User existingUser = User.builder()
                .email(loginRequest.getEmail())
                .password(ENCODED_PASSWORD)
                .nombre("Juan")
                .apellido("Vega")
                .role(Role.DOCENTE)
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

        InvalidCredentialsException exception = assertThrows(
                InvalidCredentialsException.class,
                () -> authService.login(loginRequest)
        );

        assertNotNull(exception.getMessage());
    }

    @Test
    void loginWhenPasswordIsIncorrectShouldThrowInvalidCredentialsException() {
        User existingUser = User.builder()
                .email(loginRequest.getEmail())
                .password(ENCODED_PASSWORD)
                .nombre("Juan")
                .apellido("Vega")
                .role(Role.DOCENTE)
                .enabled(true)
                .build();

        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches(loginRequest.getPassword(), existingUser.getPassword())).thenReturn(false);

        InvalidCredentialsException exception = assertThrows(
                InvalidCredentialsException.class,
                () -> authService.login(loginRequest)
        );

        assertNotNull(exception.getMessage());
    }

    @Test
    void loginWhenUserIsDisabledShouldThrowInvalidCredentialsException() {
        User existingUser = User.builder()
                .email(loginRequest.getEmail())
                .password(ENCODED_PASSWORD)
                .nombre("Juan")
                .apellido("Vega")
                .role(Role.DOCENTE)
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

    // ==================== TESTS DE LOGIN UADE ====================

    @Test
    void loginUadeWhenCredentialsAreValidShouldReturnAuthResponse() {
        LoginUadeRequest loginUadeRequest = LoginUadeRequest.builder()
                .legajo("123456")
                .email("usuario@uade.edu.ar")
                .password("miPassword123")
                .build();

        User existingUser = User.builder()
                .legajo("123456")
                .email("usuario@uade.edu.ar")
                .password(ENCODED_PASSWORD)
                .nombre("Juan")
                .apellido("Rodriguez")
                .role(Role.ALUMNO)
                .enabled(true)
                .build();

        when(userRepository.findByLegajo(loginUadeRequest.getLegajo())).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches(loginUadeRequest.getPassword(), existingUser.getPassword())).thenReturn(true);
        jwtService.setToken("token-uade");

        AuthResponse response = authService.loginUade(loginUadeRequest);

        assertEquals("token-uade", response.getToken());
        assertEquals(existingUser.getEmail(), response.getEmail());
        assertEquals(existingUser.getNombre(), response.getNombre());
        assertEquals(existingUser.getApellido(), response.getApellido());
        assertEquals(existingUser.getRole(), response.getRole());
        assertEquals("Inicio de sesión exitoso", response.getMessage());
    }

    @Test
    void loginUadeWhenLegajoNotFoundShouldThrowUserNotFoundException() {
        LoginUadeRequest loginUadeRequest = LoginUadeRequest.builder()
                .legajo("999999")
                .email("usuario@uade.edu.ar")
                .password("miPassword123")
                .build();

        when(userRepository.findByLegajo(loginUadeRequest.getLegajo())).thenReturn(Optional.empty());

        InvalidCredentialsException exception = assertThrows(
                InvalidCredentialsException.class,
                () -> authService.loginUade(loginUadeRequest)
        );

        assertNotNull(exception.getMessage());
    }

    @Test
    void loginUadeWhenEmailDoesNotMatchShouldThrowInvalidCredentialsException() {
        LoginUadeRequest loginUadeRequest = LoginUadeRequest.builder()
                .legajo("123456")
                .email("wrong@uade.edu.ar")
                .password("miPassword123")
                .build();

        User existingUser = User.builder()
                .legajo("123456")
                .email("usuario@uade.edu.ar")
                .password(ENCODED_PASSWORD)
                .nombre("Juan")
                .apellido("Rodriguez")
                .role(Role.ALUMNO)
                .enabled(true)
                .build();

        when(userRepository.findByLegajo(loginUadeRequest.getLegajo())).thenReturn(Optional.of(existingUser));

        InvalidCredentialsException exception = assertThrows(
                InvalidCredentialsException.class,
                () -> authService.loginUade(loginUadeRequest)
        );

        assertNotNull(exception.getMessage());
    }

    @Test
    void loginUadeWhenPasswordIsIncorrectShouldThrowInvalidCredentialsException() {
        LoginUadeRequest loginUadeRequest = LoginUadeRequest.builder()
                .legajo("123456")
                .email("usuario@uade.edu.ar")
                .password("wrongPassword")
                .build();

        User existingUser = User.builder()
                .legajo("123456")
                .email("usuario@uade.edu.ar")
                .password(ENCODED_PASSWORD)
                .nombre("Juan")
                .apellido("Rodriguez")
                .role(Role.ALUMNO)
                .enabled(true)
                .build();

        when(userRepository.findByLegajo(loginUadeRequest.getLegajo())).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches(loginUadeRequest.getPassword(), existingUser.getPassword())).thenReturn(false);

        InvalidCredentialsException exception = assertThrows(
                InvalidCredentialsException.class,
                () -> authService.loginUade(loginUadeRequest)
        );

        assertNotNull(exception.getMessage());
    }

    @Test
    void loginUadeWhenUserIsDisabledShouldThrowInvalidCredentialsException() {
        LoginUadeRequest loginUadeRequest = LoginUadeRequest.builder()
                .legajo("123456")
                .email("usuario@uade.edu.ar")
                .password("miPassword123")
                .build();

        User existingUser = User.builder()
                .legajo("123456")
                .email("usuario@uade.edu.ar")
                .password(ENCODED_PASSWORD)
                .nombre("Juan")
                .apellido("Rodriguez")
                .role(Role.ALUMNO)
                .enabled(false)
                .build();

        when(userRepository.findByLegajo(loginUadeRequest.getLegajo())).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches(loginUadeRequest.getPassword(), existingUser.getPassword())).thenReturn(true);

        InvalidCredentialsException exception = assertThrows(
                InvalidCredentialsException.class,
                () -> authService.loginUade(loginUadeRequest)
        );

        assertEquals("La cuenta está deshabilitada", exception.getMessage());
    }

    @Test
    void registerWhenVerifyUserSavedCalled() {
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(userRepository.existsByDisplayUsername(registerRequest.getUsername())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn(ENCODED_PASSWORD);

        User expectedUser = User.builder()
                .nombre(registerRequest.getNombre())
                .apellido(registerRequest.getApellido())
                .displayUsername(registerRequest.getUsername())
                .email(registerRequest.getEmail())
                .recoveryEmail(registerRequest.getRecoveryEmail())
                .password(ENCODED_PASSWORD)
                .role(Role.ALUMNO)
                .enabled(true)
                .build();

        when(userRepository.save(any(User.class))).thenReturn(expectedUser);
        jwtService.setToken("register-token");

        authService.register(registerRequest);

        verify(userRepository).save(any(User.class));
    }

    // ==================== TESTS DE LOGOUT ====================

    @Test
    void logoutShouldRevokeToken() {
        Date expiration = new Date(System.currentTimeMillis() + 3600_000);
        jwtService.setExpiration(expiration);

        authService.logout("some-jwt-token");

        verify(tokenBlacklistService).revokeToken("some-jwt-token", expiration);
    }

    // ==================== TESTS DE BLOQUEO DE CUENTA ====================

    @Test
    void loginWhenAccountIsLockedShouldThrowInvalidCredentialsException() {
        User existingUser = User.builder()
                .email(loginRequest.getEmail())
                .password(ENCODED_PASSWORD)
                .role(Role.DOCENTE)
                .enabled(true)
                .failedAttempts(4)
                .lockedUntil(LocalDateTime.now().plusMinutes(5))
                .build();

        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(existingUser));

        InvalidCredentialsException exception = assertThrows(
                InvalidCredentialsException.class,
                () -> authService.login(loginRequest)
        );

        assertTrue(exception.getMessage().contains("bloqueada"));
    }

    @Test
    void loginWhenLockExpiredShouldResetAttemptsAndProceed() {
        User existingUser = User.builder()
                .email(loginRequest.getEmail())
                .password(ENCODED_PASSWORD)
                .role(Role.DOCENTE)
                .enabled(true)
                .failedAttempts(4)
                .lockedUntil(LocalDateTime.now().minusMinutes(1))
                .build();

        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches(loginRequest.getPassword(), existingUser.getPassword())).thenReturn(true);
        jwtService.setToken("token-after-lock-expired");

        AuthResponse response = authService.login(loginRequest);

        assertEquals("token-after-lock-expired", response.getToken());
        verify(userRepository, atLeastOnce()).save(any(User.class));
    }

    @Test
    void loginWhenFourthFailedAttemptShouldLockAccount() {
        User existingUser = User.builder()
                .email(loginRequest.getEmail())
                .password(ENCODED_PASSWORD)
                .role(Role.DOCENTE)
                .enabled(true)
                .failedAttempts(3)
                .build();

        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches(loginRequest.getPassword(), existingUser.getPassword())).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> authService.login(loginRequest));

        verify(userRepository).save(argThat(u -> u.getLockedUntil() != null));
    }

    @Test
    void loginWhenSuccessfulAfterPreviousFailuresShouldResetAttempts() {
        User existingUser = User.builder()
                .email(loginRequest.getEmail())
                .password(ENCODED_PASSWORD)
                .role(Role.DOCENTE)
                .enabled(true)
                .failedAttempts(2)
                .build();

        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches(loginRequest.getPassword(), existingUser.getPassword())).thenReturn(true);
        jwtService.setToken("token-reset-attempts");

        AuthResponse response = authService.login(loginRequest);

        assertEquals("token-reset-attempts", response.getToken());
        verify(userRepository).save(argThat(u -> u.getFailedAttempts() == 0 && u.getLockedUntil() == null));
    }

    @Test
    void registerWhenRecoveryEmailSameAsMainEmailShouldThrowIllegalArgumentException() {
        RegisterRequest requestSameEmail = RegisterRequest.builder()
                .nombre("Juan")
                .apellido("Perez")
                .username("juanperez")
                .email("mismo@ejemplo.com")
                .recoveryEmail("mismo@ejemplo.com")
                .password("password123")
                .build();

        when(userRepository.existsByEmail(requestSameEmail.getEmail())).thenReturn(false);
        when(userRepository.existsByDisplayUsername(requestSameEmail.getUsername())).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> authService.register(requestSameEmail));
        verify(userRepository, never()).save(any(User.class));
    }

    private static class StubJwtService extends JwtService {
        private String token;
        private Date expiration = new Date(System.currentTimeMillis() + 3600_000);

        StubJwtService(String token) {
            this.token = token;
        }

        void setToken(String token) {
            this.token = token;
        }

        void setExpiration(Date expiration) {
            this.expiration = expiration;
        }

        @Override
        public String generateToken(User user) {
            return token;
        }

        @Override
        public Date extractExpiration(String token) {
            return expiration;
        }
    }
}
