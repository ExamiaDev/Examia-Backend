package com.examia.security;

import com.examia.service.JwtService;
import com.examia.service.TokenBlacklistService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro que intercepta las solicitudes HTTP para validar el token JWT.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        log.info("[JwtFilter] {} {} - Auth header present: {}",
                request.getMethod(), request.getRequestURI(), authHeader != null);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("[JwtFilter] No Bearer token found, continuing without authentication");
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);
        log.info("[JwtFilter] Token extracted, length: {}", jwt.length());

        if (tokenBlacklistService.isRevoked(jwt)) {
            log.warn("[JwtFilter] Rejected revoked token");
            filterChain.doFilter(request, response);
            return;
        }

        try {
            userEmail = jwtService.extractEmail(jwt);
            log.info("[JwtFilter] Email extracted from token: {}", userEmail);

            // Si el email es válido y no hay autenticación previa
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);
                log.info("[JwtFilter] User loaded: {}, authorities: {}",
                        userDetails.getUsername(), userDetails.getAuthorities());

                // Validar el token
                if (jwtService.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.info("[JwtFilter] Authentication set successfully for user: {}", userEmail);
                } else {
                    log.warn("[JwtFilter] Token is invalid for user: {}", userEmail);
                }
            }
        } catch (Exception e) {
            // Si el token es inválido, simplemente continuamos sin autenticar
            // El endpoint protegido devolverá 401 si requiere autenticación
            log.error("[JwtFilter] Error processing token: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}

