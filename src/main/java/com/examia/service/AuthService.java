package com.examia.service;

import com.examia.dto.AuthResponse;
import com.examia.dto.LoginRequest;
import com.examia.dto.LoginUadeRequest;
import com.examia.dto.RegisterRequest;
import com.examia.exception.InvalidCredentialsException;
import com.examia.exception.UserAlreadyExistsException;
import com.examia.exception.UserNotFoundException;
import com.examia.model.Role;
import com.examia.model.User;
import com.examia.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException(
                    "Ya existe un usuario con el nombre de usuario '" + request.getUsername() + "'"
            );
        }

        // Si no se proporciona recoveryEmail, usar el email principal
        String recoveryEmail = (request.getRecoveryEmail() != null && !request.getRecoveryEmail().isBlank())
                ? request.getRecoveryEmail()
                : request.getEmail();

        User user = User.builder()
                .nombre(request.getNombre())
                .apellido(request.getApellido())
                .username(request.getUsername())
                .email(request.getEmail())
                .recoveryEmail(recoveryEmail)
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.ALUMNO)
                .enabled(true)
                .build();

        userRepository.save(user);

        String token = jwtService.generateToken(user);

        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .nombre(user.getNombre())
                .apellido(user.getApellido())
                .role(user.getRole())
                .message("Registro exitoso")
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        // Buscar el usuario por email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException(
                        "No existe un usuario con el email '" + request.getEmail() + "'"
                ));

        // Verificar la contraseña
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException(
                    "La contraseña es incorrecta para el usuario '" + request.getEmail() + "'"
            );
        }

        // Verificar si el usuario está habilitado
        if (!user.isEnabled()) {
            throw new InvalidCredentialsException("La cuenta está deshabilitada");
        }

        // Generar token JWT
        String token = jwtService.generateToken(user);

        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .nombre(user.getNombre())
                .apellido(user.getApellido())
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
        log.info("[LoginUade] Starting login for legajo: {}", request.getLegajo());
        long step1 = System.currentTimeMillis();

        // Buscar el usuario por legajo
        log.info("[LoginUade] Step 1: Finding user by legajo...");
        User user = userRepository.findByLegajo(request.getLegajo())
                .orElseThrow(() -> new UserNotFoundException(
                        "No existe un usuario con el legajo '" + request.getLegajo() + "'"
                ));
        log.info("[LoginUade] Step 1 completed in {} ms. User found: {}",
                System.currentTimeMillis() - step1, user.getEmail());

        long step2 = System.currentTimeMillis();
        // Verificar que el email coincida
        log.info("[LoginUade] Step 2: Verifying email match...");
        if (!user.getEmail().equals(request.getEmail())) {
            throw new InvalidCredentialsException(
                    "El email no coincide con el legajo '" + request.getLegajo() + "'"
            );
        }
        log.info("[LoginUade] Step 2 completed in {} ms. Email matches.",
                System.currentTimeMillis() - step2);

        long step3 = System.currentTimeMillis();
        // Verificar la contraseña
        log.info("[LoginUade] Step 3: Verifying password...");
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException(
                    "La contraseña es incorrecta para el usuario con legajo '" + request.getLegajo() + "'"
            );
        }
        log.info("[LoginUade] Step 3 completed in {} ms. Password matches.",
                System.currentTimeMillis() - step3);

        long step4 = System.currentTimeMillis();
        // Verificar si el usuario está habilitado
        log.info("[LoginUade] Step 4: Checking if user is enabled...");
        if (!user.isEnabled()) {
            throw new InvalidCredentialsException("La cuenta está deshabilitada");
        }
        log.info("[LoginUade] Step 4 completed in {} ms. User is enabled.",
                System.currentTimeMillis() - step4);

        long step5 = System.currentTimeMillis();
        // Generar token JWT
        log.info("[LoginUade] Step 5: Generating JWT token...");
        String token = jwtService.generateToken(user);
        log.info("[LoginUade] Step 5 completed in {} ms. Token generated.",
                System.currentTimeMillis() - step5);

        log.info("[LoginUade] Login successful for legajo: {}", request.getLegajo());
        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .nombre(user.getNombre())
                .apellido(user.getApellido())
                .role(user.getRole())
                .message("Inicio de sesión exitoso")
                .build();
    }
}

