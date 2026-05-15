package com.examia.model;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void userDetailsMethodsShouldReturnConfiguredAccountStateAndAuthorities() {
        User user = User.builder()
                .email("usuario@ejemplo.com")
                .password("encoded-password")
                .role(Role.PROFESOR)
                .enabled(true)
                .build();

        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();

        assertEquals("usuario@ejemplo.com", user.getUsername());
        assertEquals("encoded-password", user.getPassword());
        assertTrue(user.isEnabled());
        assertTrue(user.isAccountNonExpired());
        assertTrue(user.isAccountNonLocked());
        assertTrue(user.isCredentialsNonExpired());
        assertTrue(authorities.stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_PROFESOR")));
    }
}
