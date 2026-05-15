package com.examia.service;

import com.examia.dto.AuthResponse;
import com.examia.dto.LoginRequest;
import com.examia.exception.InvalidCredentialsException;
import com.examia.exception.UserNotFoundException;
import com.examia.model.User;
import com.examia.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Servicio para la autenticación de usuarios.
 *
 * Nota: Los usuarios son cargados directamente en la base de datos
 * por un administrador. No hay registro público.
 */
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
}

