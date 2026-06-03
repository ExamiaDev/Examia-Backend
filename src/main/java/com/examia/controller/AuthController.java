package com.examia.controller;

import com.examia.dto.AuthResponse;
import com.examia.dto.LoginRequest;
import com.examia.dto.LoginUadeRequest;
import com.examia.dto.RegisterRequest;
import com.examia.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para operaciones de autenticación.
 * Endpoint para inicio de sesión de usuarios.
 *
 * Nota: Los usuarios son cargados directamente en la base de datos
 * por un administrador. No hay registro público.
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
  
    /**
     * Autentica un usuario existente.
     *
     * POST /api/auth/login
     *
     * @param request datos del login (email, password)
     * @return AuthResponse con token JWT y datos del usuario
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Autentica un usuario UADE usando legajo, email y contraseña.
     *
     * POST /api/auth/login-uade
     *
     * @param request datos del login UADE (legajo, email, password)
     * @return AuthResponse con token JWT y datos del usuario
     */
    @PostMapping("/login-uade")
    public ResponseEntity<AuthResponse> loginUade(@Valid @RequestBody LoginUadeRequest request) {
        log.info("=== LOGIN UADE REQUEST RECEIVED ===");
        log.info("Legajo: {}, Email: {}", request.getLegajo(), request.getEmail());
        long startTime = System.currentTimeMillis();

        try {
            log.info("Calling authService.loginUade...");
            AuthResponse response = authService.loginUade(request);
            long elapsed = System.currentTimeMillis() - startTime;
            log.info("Login UADE successful. Elapsed time: {} ms", elapsed);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - startTime;
            log.error("Login UADE failed after {} ms. Error: {}", elapsed, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Endpoint de verificación de estado del servicio de auth.

     * GET /api/auth/health

     * @return mensaje de estado
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            authService.logout(authHeader.substring(7));
        }
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Auth service is running");
    }
}

