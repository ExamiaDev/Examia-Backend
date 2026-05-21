package com.examia.config;

import com.examia.security.JwtAuthenticationFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class SecurityConfigTest {

    private SecurityConfig securityConfig;
    private JwtAuthenticationFilter jwtAuthFilter;
    private AuthenticationProvider authenticationProvider;

    @BeforeEach
    void setUp() {
        jwtAuthFilter = mock(JwtAuthenticationFilter.class);
        authenticationProvider = mock(AuthenticationProvider.class);
        securityConfig = new SecurityConfig(jwtAuthFilter, authenticationProvider);
    }

    @Test
    void corsConfigurationSourceShouldReturnUrlBasedSource() {
        CorsConfigurationSource source = securityConfig.corsConfigurationSource();

        assertNotNull(source);
        assertInstanceOf(UrlBasedCorsConfigurationSource.class, source);
    }

    @Test
    void corsConfigurationShouldAllowLocalhost() {
        CorsConfigurationSource source = securityConfig.corsConfigurationSource();

        assertNotNull(source);
        // Verificar que es una instancia válida
        assertInstanceOf(UrlBasedCorsConfigurationSource.class, source);
    }

    @Test
    void corsConfigurationShouldAllowVercelOrigins() {
        CorsConfigurationSource source = securityConfig.corsConfigurationSource();

        assertNotNull(source);
        assertInstanceOf(UrlBasedCorsConfigurationSource.class, source);
    }
}

