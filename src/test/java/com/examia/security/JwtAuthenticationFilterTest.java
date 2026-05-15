package com.examia.security;

import com.examia.service.JwtService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternalWhenAuthorizationHeaderIsMissingShouldContinueWithoutAuthentication() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        UserDetailsService userDetailsService = mock(UserDetailsService.class);
        MockFilterChain filterChain = new MockFilterChain();
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService, userDetailsService);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertSame(request, filterChain.getRequest());
        assertSame(response, filterChain.getResponse());
        verifyNoInteractions(jwtService, userDetailsService);
    }

    @Test
    void doFilterInternalWhenAuthorizationHeaderIsNotBearerShouldContinueWithoutAuthentication() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        UserDetailsService userDetailsService = mock(UserDetailsService.class);
        MockFilterChain filterChain = new MockFilterChain();
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService, userDetailsService);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader("Authorization", "Basic credentials");

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertSame(request, filterChain.getRequest());
        assertSame(response, filterChain.getResponse());
        verifyNoInteractions(jwtService, userDetailsService);
    }

    @Test
    void doFilterInternalWhenTokenIsValidShouldSetAuthentication() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        UserDetailsService userDetailsService = mock(UserDetailsService.class);
        MockFilterChain filterChain = new MockFilterChain();
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService, userDetailsService);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader("Authorization", "Bearer valid-token");
        UserDetails userDetails = User.withUsername("usuario@ejemplo.com")
                .password("encoded-password")
                .roles("ALUMNO")
                .build();

        when(jwtService.extractEmail("valid-token")).thenReturn("usuario@ejemplo.com");
        when(userDetailsService.loadUserByUsername("usuario@ejemplo.com")).thenReturn(userDetails);
        when(jwtService.isTokenValid("valid-token", userDetails)).thenReturn(true);

        filter.doFilterInternal(request, response, filterChain);

        Authentication authentication = Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("usuario@ejemplo.com", authentication.getName());
        assertTrue(authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ALUMNO")));
        assertSame(request, filterChain.getRequest());
        assertSame(response, filterChain.getResponse());
    }

    @Test
    void doFilterInternalWhenTokenIsInvalidShouldContinueWithoutAuthentication() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        UserDetailsService userDetailsService = mock(UserDetailsService.class);
        MockFilterChain filterChain = new MockFilterChain();
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService, userDetailsService);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader("Authorization", "Bearer invalid-token");
        UserDetails userDetails = User.withUsername("usuario@ejemplo.com")
                .password("encoded-password")
                .roles("ALUMNO")
                .build();

        when(jwtService.extractEmail("invalid-token")).thenReturn("usuario@ejemplo.com");
        when(userDetailsService.loadUserByUsername("usuario@ejemplo.com")).thenReturn(userDetails);
        when(jwtService.isTokenValid("invalid-token", userDetails)).thenReturn(false);

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertSame(request, filterChain.getRequest());
        assertSame(response, filterChain.getResponse());
    }

    @Test
    void doFilterInternalWhenTokenParsingFailsShouldContinueWithoutAuthentication() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        UserDetailsService userDetailsService = mock(UserDetailsService.class);
        MockFilterChain filterChain = new MockFilterChain();
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService, userDetailsService);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader("Authorization", "Bearer malformed-token");
        when(jwtService.extractEmail("malformed-token"))
                .thenThrow(new IllegalArgumentException("invalid token"));

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertSame(request, filterChain.getRequest());
        assertSame(response, filterChain.getResponse());
        verifyNoInteractions(userDetailsService);
    }
}
