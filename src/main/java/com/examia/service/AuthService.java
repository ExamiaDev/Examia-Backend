package com.examia.service;

import com.examia.dto.AuthResponse;
import com.examia.dto.LoginRequest;
import com.examia.dto.LoginUadeRequest;
import com.examia.dto.RegisterRequest;
import com.examia.exception.InvalidCredentialsException;
import com.examia.exception.UserAlreadyExistsException;
import com.examia.exception.UserNotFoundException;
import java.util.Date;
import com.examia.model.Role;
import com.examia.model.User;
import com.examia.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Servicio para la autenticación de usuarios.
 *
 * Nota: Los usuarios son cargados directamente en la base de datos
 * por un administrador. No hay registro público.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;

    /**
     * Autentica un usuario existente.
     *
     * @param request datos del login
     * @return AuthResponse con el token y datos del usuario
     * @throws UserNotFoundException si el usuario no existe
     * @throws InvalidCredentialsException si la contraseña es incorrecta
     */
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException(
                    "Ya existe un usuario con el email '" + request.getEmail() + "'"
            );
        }
        if (userRepository.existsByDisplayUsername(request.getUsername())) {
            throw new UserAlreadyExistsException(
                    "Ya existe un usuario con el nombre de usuario '" + request.getUsername() + "'"
            );
        }

        if (request.getRecoveryEmail() != null
                && !request.getRecoveryEmail().isBlank()
                && request.getRecoveryEmail().equalsIgnoreCase(request.getEmail())) {
            throw new IllegalArgumentException("El mail de recupero debe ser diferente al mail principal");
        }

        // Si no se proporciona recoveryEmail, usar el email principal
        String recoveryEmail = (request.getRecoveryEmail() != null && !request.getRecoveryEmail().isBlank())
                ? request.getRecoveryEmail()
                : request.getEmail();

        User user = User.builder()
                .nombre(request.getNombre())
                .apellido(request.getApellido())
                .displayUsername(request.getUsername())
                .email(request.getEmail())
                .recoveryEmail(recoveryEmail)
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole() != null ? request.getRole() : Role.ALUMNO)
                .enabled(true)
                .build();

        userRepository.save(user);

        String token = jwtService.generateToken(user);

        return AuthResponse.builder()
                .id(user.getId())
                .token(token)
                .email(user.getEmail())
                .nombre(user.getNombre())
                .apellido(user.getApellido())
                .legajo(user.getLegajo())
                .role(user.getRole())
                .message("Registro exitoso")
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Credenciales incorrectas"));

        checkAccountLocked(user);

        if (!user.isEnabled()) {
            throw new InvalidCredentialsException("La cuenta está deshabilitada");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            handleFailedAttempt(user);
            throw new InvalidCredentialsException("Credenciales incorrectas");
        }

        resetLoginAttempts(user);

        String token = jwtService.generateToken(user);

        return AuthResponse.builder()
                .id(user.getId())
                .token(token)
                .email(user.getEmail())
                .nombre(user.getNombre())
                .apellido(user.getApellido())
                .legajo(user.getLegajo())
                .role(user.getRole())
                .message("Inicio de sesión exitoso")
                .build();
    }

    /**
     * Autentica un usuario UADE utilizando legajo, email y contraseña.
     *
     * @param request datos del login UADE (legajo, email, password)
     * @return AuthResponse con el token y datos del usuario
     * @throws UserNotFoundException si el usuario con ese legajo no existe
     * @throws InvalidCredentialsException si el email no coincide o la contraseña es incorrecta
     */
    public AuthResponse loginUade(LoginUadeRequest request) {
        log.info("[LoginUade] Attempt for legajo: {}", request.getLegajo());

        User user = userRepository.findByLegajo(request.getLegajo())
                .orElseThrow(() -> new InvalidCredentialsException("Credenciales incorrectas"));

        checkAccountLocked(user);

        if (!user.isEnabled()) {
            throw new InvalidCredentialsException("La cuenta está deshabilitada");
        }

        if (!user.getEmail().equals(request.getEmail())) {
            handleFailedAttempt(user);
            throw new InvalidCredentialsException("Credenciales incorrectas");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            handleFailedAttempt(user);
            throw new InvalidCredentialsException("Credenciales incorrectas");
        }

        resetLoginAttempts(user);

        String token = jwtService.generateToken(user);

        log.info("[LoginUade] Login successful for legajo: {}", request.getLegajo());
        return AuthResponse.builder()
                .id(user.getId())
                .token(token)
                .email(user.getEmail())
                .nombre(user.getNombre())
                .apellido(user.getApellido())
                .legajo(user.getLegajo())
                .role(user.getRole())
                .message("Inicio de sesión exitoso")
                .build();
    }

    public void logout(String token) {
        Date expiration = jwtService.extractExpiration(token);
        tokenBlacklistService.revokeToken(token, expiration);
    }

    private static final int MAX_FAILED_ATTEMPTS = 4;
    private static final int LOCKOUT_MINUTES = 10;

    private void checkAccountLocked(User user) {
        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now())) {
            long minutesLeft = ChronoUnit.MINUTES.between(LocalDateTime.now(), user.getLockedUntil()) + 1;
            throw new InvalidCredentialsException(
                    "Cuenta bloqueada temporalmente. Intente nuevamente en " + minutesLeft + " minuto(s)."
            );
        }
        if (user.getLockedUntil() != null && user.getLockedUntil().isBefore(LocalDateTime.now())) {
            user.setFailedAttempts(0);
            user.setLockedUntil(null);
            userRepository.save(user);
        }
    }

    private void handleFailedAttempt(User user) {
        int attempts = user.getFailedAttempts() + 1;
        user.setFailedAttempts(attempts);
        if (attempts >= MAX_FAILED_ATTEMPTS) {
            user.setLockedUntil(LocalDateTime.now().plusMinutes(LOCKOUT_MINUTES));
            log.warn("[Auth] Account locked for user {} after {} failed attempts", user.getEmail(), attempts);
        }
        userRepository.save(user);
    }

    private void resetLoginAttempts(User user) {
        if (user.getFailedAttempts() > 0 || user.getLockedUntil() != null) {
            user.setFailedAttempts(0);
            user.setLockedUntil(null);
            userRepository.save(user);
        }
    }
}

